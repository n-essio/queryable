package it.ness.queryable.builder;

import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.model.pojo.Data;
import it.ness.queryable.model.pojo.Parameters;
import it.ness.queryable.model.predicates.FilterBase;
import it.ness.queryable.model.predicates.QLikeListFilter;
import it.ness.queryable.templates.FreeMarkerTemplates;
import it.ness.queryable.util.GetSearchMethodV4;
import it.ness.queryable.util.ModelFilesV4;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class QueryableV4Builder {


    public static void generateSources(ModelFilesV4 mf, Log log, Parameters parameters) throws Exception {
        String[] modelFiles = mf.getModelFileNames();
        for (String modelFileName : modelFiles) {
            String className = StringUtil.getClassNameFromFileName(modelFileName);
            if (!mf.excludeClass(className)) {
                try {
                    log.info(mf.toString());
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

    private static void createModel(Log log, ModelFilesV4 mf, String modelFileName, Parameters parameters) throws Exception {
        String className = StringUtil.getClassNameFromFileName(modelFileName);
        String path = parameters.modelPath;
        JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(path, modelFileName));

        Set<FilterBase> allFilderDefs = new LinkedHashSet<>();
        Set<FilterBase> preQueryDefs = mf.getFilter(className, FilterType.PREQUERY);
        Set<FilterBase> postQueryDefs = mf.getFilter(className, FilterType.POSTQUERY);
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
        if (log != null) log.debug("WE don't need to creating a new model for class : " + className);
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

    private static void createRsService(String version, ModelFilesV4 mf, String className, String groupId, String artefactId, String
            orderBy, String rsPath, Parameters parameters, Log log) throws Exception {

        String idFieldName = mf.getIdFieldName(className);
        String idFieldType = mf.getIdFieldType(className);
        System.out.println("idFieldName : " + idFieldName + " className : " + className);
        Data data = Data.with("packageName", groupId + "." + artefactId)
                .and("groupId", groupId)
                .and("className", className)
                .and("idFieldName", idFieldName)
                .and("idFieldType", idFieldType);
        if (orderBy != null && !"NOT_SET".equals(orderBy)) {
            data = data.and("defaultSort", orderBy);
        }
        if (rsPath != null && !"NOT_SET".equals(rsPath)) {
            data = data.and("rsPath", rsPath);
        }
        Map<String, Object> map = data.map();
        String serviceRsClass = FreeMarkerTemplates.processTemplate("servicersv4", map);
        JavaClassSource javaClassTemplate = Roaster.parse(JavaClassSource.class, serviceRsClass);
        Collection<FilterBase> preQueryFilters = mf.getFilter(className, FilterType.PREQUERY);
        Collection<FilterBase> postQueryFilters = mf.getFilter(className, FilterType.POSTQUERY);

        GetSearchMethodV4 getSearchMethodV4 = new GetSearchMethodV4(log, preQueryFilters, postQueryFilters, className);
        addImportsToClass(javaClassTemplate, preQueryFilters, groupId);
        addImportsToClass(javaClassTemplate, postQueryFilters, groupId);
        MethodSource<JavaClassSource> templateMethod = getMethodByName(javaClassTemplate, "query");
        templateMethod.setBody(getSearchMethodV4.create());

        String packagePath = getPathFromPackage(javaClassTemplate.getPackage());
        File pd = new File(parameters.outputDirectory, packagePath);
        File filePath = new File(pd, className + "ServiceRs.java");
        if (filePath.exists()) {
            JavaClassSource javaClassOriginal = Roaster.parse(JavaClassSource.class, filePath);
            // add imports to original
            addImportsToClass(javaClassOriginal, preQueryFilters, groupId);
            addImportsToClass(javaClassOriginal, postQueryFilters, groupId);
            MethodSource<JavaClassSource> method = getMethodByName(javaClassOriginal, "query");
            if (method != null) {
                if (!excludeMethodByName(javaClassOriginal, "query")) {
                    method.setBody(templateMethod.getBody());
                } else {
                    log.info(String.format("query in class %s is excluded from queryable plugin", className));
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

    private static void addImportsToClass(JavaClassSource javaClassSource, Collection<FilterBase> fd, String groupId) {
        if (fd == null) return;
        for (FilterBase f : fd) {
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
            if (f instanceof QLikeListFilter) {
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
