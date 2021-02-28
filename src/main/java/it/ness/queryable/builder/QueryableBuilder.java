package it.ness.queryable.builder;

import it.ness.queryable.model.FilterDefBase;
import it.ness.queryable.model.QLikeListFilterDef;
import it.ness.queryable.model.QListFilterDef;
import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.model.pojo.Data;
import it.ness.queryable.model.pojo.Parameters;
import it.ness.queryable.templates.FreeMarkerTemplates;
import it.ness.queryable.util.GetSearchMethod;
import it.ness.queryable.util.ModelFiles;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static it.ness.queryable.builder.Constants.*;

public class QueryableBuilder {


    public static void generateSources(ModelFiles mf, Log log, Parameters parameters) throws Exception {
        String[] modelFiles = mf.getModelFileNames();
        for (String modelFileName : modelFiles) {
            String className = StringUtil.getClassNameFromFileName(modelFileName);
            if (!mf.excludeClass(className)) {
                try {
                    createModel(log, mf, modelFileName, parameters);
                    String orderBy = mf.getDefaultOrderBy(className);
                    String rsPath = mf.getRsPath(className);
                    createRsService(mf, className, parameters.groupId, parameters.artifactId, orderBy, rsPath, parameters, log);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        if (log != null) log.info("Done generating sources");
    }

    private static void createModel(Log log, ModelFiles mf, String modelFileName, Parameters parameters) throws Exception {
        String className = StringUtil.getClassNameFromFileName(modelFileName);
        String path = parameters.modelPath;
        JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(path, modelFileName));

        Set<FilterDefBase> allFilderDefs = new LinkedHashSet<>();
        Set<FilterDefBase> preQueryDefs = mf.getFilterDef(className, FilterType.PREQUERY);
        Set<FilterDefBase> postQueryDefs = mf.getFilterDef(className, FilterType.POSTQUERY);
        if (null != preQueryDefs) {
            allFilderDefs.addAll(preQueryDefs);
        }
        if (null != postQueryDefs) {
            allFilderDefs.addAll(postQueryDefs);
        }

        if (allFilderDefs.size() == 0) {
            if (log != null) log.debug("Not defined Q filterdefs for class : " + className);
            return;
        }
        if (log != null) log.debug("Creating model for class : " + className);

        javaClass.addImport(H_FILTER);
        javaClass.addImport(H_FILTERDEF);
        javaClass.addImport(H_PARAMDEF);

        for (FilterDefBase fd : allFilderDefs) {
            fd.addAnnotationToModelClass(javaClass);
        }

        // remove imports if has org.hibernate.annotations.*
        if (javaClass.hasImport(H_ANNOTATIONS)) {
            javaClass.removeImport(H_FILTER);
            javaClass.removeImport(H_FILTERDEF);
            javaClass.removeImport(H_PARAMDEF);
        }

        String packagePath = getPathFromPackage(javaClass.getPackage());
        File pd = new File(parameters.outputDirectory, packagePath);
        pd.mkdirs();

        FileWriter out = new FileWriter(new File(pd, modelFileName));
        try {
            out.append(javaClass.toString());
        } finally {
            out.flush();
            out.close();
        }
    }

    private static void createRsService(ModelFiles mf, String className, String groupId, String artefactId, String
            orderBy, String rsPath, Parameters parameters, Log log) throws Exception {

        Map<String, Object> data = Data.with("packageName", groupId + "." + artefactId)
                .and("groupId", groupId)
                .and("className", className)
                .and("rsPath", rsPath)
                .and("defaultSort", orderBy).map();

        String serviceRsClass = FreeMarkerTemplates.processTemplate("servicers", data);
        JavaClassSource javaClassTemplate = Roaster.parse(JavaClassSource.class, serviceRsClass);
        Collection<FilterDefBase> preQueryFilters = mf.getFilterDef(className, FilterType.PREQUERY);
        Collection<FilterDefBase> postQueryFilters = mf.getFilterDef(className, FilterType.POSTQUERY);

        GetSearchMethod getSearchMethod = new GetSearchMethod(log, preQueryFilters, postQueryFilters, className);
        addImportsToClass(javaClassTemplate, preQueryFilters);
        addImportsToClass(javaClassTemplate, postQueryFilters);
        MethodSource<JavaClassSource> templateMethod = getMethodByName(javaClassTemplate, "getSearch");
        templateMethod.setBody(getSearchMethod.create());

        String packagePath = getPathFromPackage(javaClassTemplate.getPackage());
        File pd = new File(parameters.outputDirectory, packagePath);
        File filePath = new File(pd, className + "ServiceRs.java");
        if (filePath.exists()) {
            JavaClassSource javaClassOriginal = Roaster.parse(JavaClassSource.class, filePath);
            // add imports to original
            addImportsToClass(javaClassOriginal, preQueryFilters);
            addImportsToClass(javaClassOriginal, postQueryFilters);
            MethodSource<JavaClassSource> method = getMethodByName(javaClassOriginal, "getSearch");
            if (method != null) {
                if (!excludeMethodByName(javaClassOriginal, "getSearch")) {
                    method.setBody(templateMethod.getBody());
                } else {
                    log.info(String.format("getSearch in class %s is excluded from queryable plugin", className));
                }
            } else {
                javaClassOriginal.addMethod(templateMethod);
            }
            try (FileWriter out = new FileWriter(filePath)) {
                out.append(javaClassOriginal.toString());
            }
        } else {
            pd.mkdirs();
            try (FileWriter out = new FileWriter(filePath)) {
                out.append(javaClassTemplate.toString());
            }
        }
    }

    private static void addImportsToClass(JavaClassSource javaClassSource, Collection<FilterDefBase> fd) {
        if (fd == null) return;
        for (FilterDefBase f : fd) {
            if ("Date".equals(f.fieldType)) {
                javaClassSource.addImport("java.util.Date");
                javaClassSource.addImport("it.plab.api.util.DateUtils");
            }
            if ("LocalDateTime".equals(f.fieldType)) {
                javaClassSource.addImport("java.time.LocalDateTime");
            }
            if ("LocalDate".equals(f.fieldType)) {
                javaClassSource.addImport("java.time.LocalDate");
            }
            if ("BigDecimal".equals(f.fieldType) || "big_decimal".equals(f.fieldType)) {
                javaClassSource.addImport("java.math.BigDecimal");
            }
            if (f instanceof QListFilterDef) {
                javaClassSource.addImport("org.hibernate.Session");
            }
            if (f instanceof QLikeListFilterDef) {
                javaClassSource.addImport("java.util.HashMap");
                javaClassSource.addImport("java.util.Map");
            }
        }
    }

    private static String getPathFromPackage(String packageName) {
        return packageName.replace(".", "/");
    }

    private static MethodSource<JavaClassSource> getMethodByName(JavaClassSource javaClassSource, String name) {
        for (MethodSource<JavaClassSource> method : javaClassSource.getMethods()) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

    private static boolean excludeMethodByName(JavaClassSource javaClassSource, String name) {
        for (MethodSource<JavaClassSource> method : javaClassSource.getMethods()) {
            if (name.equals(method.getName())) {
                List<AnnotationSource<JavaClassSource>> classAn = method.getAnnotations();
                for (AnnotationSource<JavaClassSource> f : classAn) {
                    if (f.getName().startsWith("QExclude")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
