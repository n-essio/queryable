package it.ness.queryable.builder;

import it.ness.queryable.model.filters.FilterDefBase;
import it.ness.queryable.model.filters.QLikeListFilterDef;
import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.model.pojo.Data;
import it.ness.queryable.model.pojo.Parameters;
import it.ness.queryable.templates.FreeMarkerTemplates;
import it.ness.queryable.util.GetSearchMethodV3;
import it.ness.queryable.util.ModelFilesV3;
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

public class QueryableV3Builder {


    public static void generateSources(ModelFilesV3 mf, Log log, Parameters parameters) throws Exception {
        String[] modelFiles = mf.getModelFileNames();
        for (String modelFileName : modelFiles) {
            String className = StringUtil.getClassNameFromFileName(modelFileName);
            if (!mf.excludeClass(className)) {
                try {
                    createModel(log, mf, modelFileName, parameters);
                    String orderBy = mf.getDefaultOrderBy(className);
                    String rsPath = mf.getRsPath(className);
                    createRsService(parameters.sourceVersion, mf, className, parameters.groupId, parameters.artifactId, orderBy, rsPath, parameters, log);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        if (log != null) log.info("Done generating sources");
    }

    private static void createModel(Log log, ModelFilesV3 mf, String modelFileName, Parameters parameters) throws Exception {
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
            out.append(replaceModelTypeAnnotations(javaClass.toString()));
        } finally {
            out.flush();
            out.close();
        }
    }

    private static String replaceModelTypeAnnotations(String modelClazz) {
        return modelClazz.replaceAll("@String", "String.class")
                .replaceAll("@java.time.LocalDateTime", "java.time.LocalDateTime.class")
                .replaceAll("@java.time.ZonedDateTime", "java.time.ZonedDateTime.class")
                .replaceAll("@java.time.LocalDate", "java.time.LocalDate.class")
                .replaceAll("@Boolean", "Boolean.class")
                .replaceAll("@java.math.BigDecimal", "java.math.BigDecimal.class")
                .replaceAll("@java.math.BigInteger", "java.math.BigInteger.class")
                .replaceAll("@Integer", "Integer.class")
                .replaceAll("@Long", "Long.class");
    }

    private static void createRsService(String version, ModelFilesV3 mf, String className, String groupId, String artefactId, String
            orderBy, String rsPath, Parameters parameters, Log log) throws Exception {

        String idFieldName = mf.getIdFieldName(className);
        String idFieldType = mf.getIdFieldType(className);
        String tableName = mf.getTableName(className);

        Data data = Data.with("packageName", groupId + "." + artefactId)
                .and("groupId", groupId)
                .and("className", className)
                .and("idFieldName", idFieldName)
                .and("idFieldType", idFieldType);
        if (orderBy != null) {
            if (!"NOT_SET".equals(orderBy)) {
                data = data.and("defaultSort", orderBy);
            } else {
                data = data.and("defaultSort", idFieldName + " ASC");
            }
        }
        if (rsPath != null) {
            if (!"NOT_SET".equals(rsPath)) {
                data = data.and("rsPath", rsPath);
                data = data.and("rsPathIsAppConstant", true);
            } else {
                data = data.and("rsPath", "\"/api/v1/" + tableName + "\"");
                data = data.and("rsPathIsAppConstant", false);
            }
        }
        Map<String, Object> map = data.map();
        String serviceRsClass = FreeMarkerTemplates.processTemplate("servicersv3", map);

        JavaClassSource javaClassTemplate = Roaster.parse(JavaClassSource.class, serviceRsClass);
        Collection<FilterDefBase> preQueryFilters = mf.getFilterDef(className, FilterType.PREQUERY);
        Collection<FilterDefBase> postQueryFilters = mf.getFilterDef(className, FilterType.POSTQUERY);

        GetSearchMethodV3 getSearchMethodV3 = new GetSearchMethodV3(log, preQueryFilters, postQueryFilters, className);
        addImportsToClass(javaClassTemplate, preQueryFilters, groupId);
        addImportsToClass(javaClassTemplate, postQueryFilters, groupId);
        MethodSource<JavaClassSource> templateMethod = getMethodByName(javaClassTemplate, "getSearch");
        templateMethod.setBody(getSearchMethodV3.create());

        String packagePath = getPathFromPackage(javaClassTemplate.getPackage());
        File pd = new File(parameters.outputDirectory, packagePath);
        File filePath = new File(pd, className + "ServiceRs.java");
        if (filePath.exists()) {
            JavaClassSource javaClassOriginal = Roaster.parse(JavaClassSource.class, filePath);
            // add imports to original
            addImportsToClass(javaClassOriginal, preQueryFilters, groupId);
            addImportsToClass(javaClassOriginal, postQueryFilters, groupId);
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

    private static void addImportsToClass(JavaClassSource javaClassSource, Collection<FilterDefBase> fd, String groupId) {
        if (fd == null) return;
        for (FilterDefBase f : fd) {
            if ("java.util.Date".equals(f.fieldType)) {
                javaClassSource.addImport("java.util.Date");
                javaClassSource.addImport(String.format("%s.api.util.DateUtils", groupId));
            }
            if ("LocalDateTime".equals(f.fieldType)) {
                javaClassSource.addImport("java.time.LocalDateTime");
            }
            if ("ZonedDateTime".equals(f.fieldType)) {
                javaClassSource.addImport("java.time.ZonedDateTime");
            }
            if ("LocalDate".equals(f.fieldType)) {
                javaClassSource.addImport("java.time.LocalDate");
            }
            if ("BigDecimal".equals(f.fieldType) || "big_decimal".equals(f.fieldType)) {
                javaClassSource.addImport("java.math.BigDecimal");
            }
            if ("BigInteger".equals(f.fieldType) || "big_integer".equals(f.fieldType)) {
                javaClassSource.addImport("java.math.BigInteger");
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
