package it.ness.queryable.util;

import it.ness.queryable.model.enums.FilterType;
import it.ness.queryable.model.api.Parameters;
import it.ness.queryable.model.v4.*;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.io.File;
import java.util.*;

public class ModelFilesV4 {

    private String[] modelFileNames;
    public boolean isParsingSuccessful;

    private Map<String, Map<FilterType, LinkedHashSet<FilterBase>>> filterMap = new LinkedHashMap<>();
    private Map<String, String> rsPathMap = new LinkedHashMap<>();
    private Map<String, String> defaultOrderByMap = new LinkedHashMap<>();
    private Map<String, Boolean> excludeClassMap = new LinkedHashMap<>();
    private Map<String, Boolean> includeClassMap = new LinkedHashMap<>();
    private Map<String, String> qualifiedClassName = new LinkedHashMap<>();
    private Map<String, String> idFieldNameMap = new LinkedHashMap<>();
    private Map<String, String> idFieldTypeMap = new LinkedHashMap<>();
    private Map<String, String> tableNameMap = new LinkedHashMap<>();

    public ModelFilesV4(Log log, Parameters parameters) {
        isParsingSuccessful = false;

        if (log != null) log.info("path = " + parameters.modelPath);
        File f = new File(parameters.modelPath);
        if (!f.exists()) {
            log.error(String.format("Path %s doesn't exist.", parameters.modelPath));
            return;
        }
        modelFileNames = f.list((f1, name) -> name.endsWith(".java"));
        if (modelFileNames != null && modelFileNames.length > 0) {
            if (log != null) log.info("Total model classes found : " + modelFileNames.length);
            if (log != null) log.info("model class file names : " + Arrays.toString(modelFileNames));
            resolveConstant(log, parameters);
            isParsingSuccessful = resolveFilterDefs(log, parameters);
        } else {
            log.error("No model classes found in path :" + parameters.modelPath);
        }
    }

    public Set<FilterBase> getFilter(final String className, final FilterType filterType) {
        if (excludeClass(className)) return null;
        Map<FilterType, LinkedHashSet<FilterBase>> allFilterDefs = filterMap.get(className);
        if (allFilterDefs == null) return null;
        return allFilterDefs.get(filterType);
    }

    public String[] getModelFileNames() {
        if (includeClassMap.isEmpty()) {
            return modelFileNames;
        }
        String[] filteredModelFiles = new String[includeClassMap.size()];
        int i = 0;
        for (String fileName : modelFileNames) {
            String className = StringUtil.getClassNameFromFileName(fileName);
            if (includeClassMap.containsKey(className)) {
                filteredModelFiles[i++] = fileName;
            }
        }
        return filteredModelFiles;
    }

    public String getIdFieldName(String className) {
        return idFieldNameMap.get(className);
    }

    public String getIdFieldType(String className) {
        return idFieldTypeMap.get(className);
    }
    public String getTableName(String className) {
        return tableNameMap.get(className);
    }

    public String getRsPath(final String className) {
        return rsPathMap.get(className);
    }

    public String getDefaultOrderBy(final String className) {
        return defaultOrderByMap.get(className);
    }

    public String getQualifiedClassName(final String className) {
        return qualifiedClassName.get(className);
    }

    public Boolean excludeClass(final String className) {
        return excludeClassMap.get(className);
    }

    private void resolveConstant(Log log, Parameters parameters) {
        for (String fileName : modelFileNames) {
            String className = StringUtil.getClassNameFromFileName(fileName);
            final String defaultRsPath = "NOT_SET";
            final String defaultOrderBy = "NOT_SET";
            String rsPath = defaultRsPath;
            String orderBy = defaultOrderBy;
            Boolean excludeClass = false;
            Boolean includeClass = null;
            // override if annotation is present
            try {
                JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(parameters.modelPath, fileName));
                AnnotationSource<JavaClassSource> a = javaClass.getAnnotation("QExclude");
                if (null != a) {
                    excludeClass = true;
                }
                a = javaClass.getAnnotation("QInclude");
                if (null != a) {
                    includeClass = true;
                }
                a = javaClass.getAnnotation("QRs");
                if (null != a) {
                    rsPath = a.getStringValue();
                    rsPath = StringUtil.removeQuotes(rsPath);
                }
                a = javaClass.getAnnotation("QOrderBy");
                if (null != a) {
                    orderBy = a.getStringValue();
                    orderBy = StringUtil.removeQuotes(orderBy);
                }
                a = javaClass.getAnnotation("Table");
                if (null != a) {
                    String tableName = a.getStringValue();
                    if (tableName == null) {
                        tableName = a.getStringValue("name");
                    }
                    tableName = StringUtil.removeQuotes(tableName);
                    if (tableName.contains(".")) {
                        String fieldName = tableName.split("\\.")[1];
                        for (FieldSource<JavaClassSource> fieldSource : javaClass.getFields()) {
                            if (fieldName.equalsIgnoreCase(fieldSource.getName())) {
                                tableName = fieldSource.getLiteralInitializer();
                                tableName = StringUtil.removeQuotes(tableName);
                            }
                        }
                    }
                    tableNameMap.put(className, tableName);
                }
                String idFieldName = "NOT_SET";
                String idFieldType = "String";
                for (FieldSource<JavaClassSource> fieldSource : javaClass.getFields()) {
                    a = fieldSource.getAnnotation("Id");
                    if (null != a) {
                        idFieldName = fieldSource.getName();
                        idFieldType = fieldSource.getType().getName();
                    }
                }
                idFieldTypeMap.put(className, idFieldType);
                idFieldNameMap.put(className, idFieldName);
                qualifiedClassName.put(className, javaClass.getQualifiedName());
            } catch (Exception e) {
                log.error(e);
            }
            rsPathMap.put(className, rsPath);
            defaultOrderByMap.put(className, orderBy);
            excludeClassMap.put(className, excludeClass);
            if (includeClass != null) {
                includeClassMap.put(className, excludeClass);
            }
            if (!excludeClass) {
                if (defaultOrderBy.equals(orderBy)) {
                    if (log != null) log.warn(String.format("orderBy for class %s : %s", className, orderBy));
                }
                if (defaultRsPath.equals(rsPath)) {
                    if (log != null) log.warn(String.format("rsPath for class %s : %s", className, rsPath));
                }
            }
        }
    }

    private boolean resolveFilterDefs(Log log, Parameters parameters) {
        Set<String> resolvedModels = new HashSet<>();

        Set<FilterBase> FilterBases = new HashSet<>();

        // add all supported filterdef bases to parse
        FilterBases.add(new QFilter(log));
        FilterBases.add(new QLikeFilter(log));
        FilterBases.add(new QNilFilter(log));
        FilterBases.add(new QNotNilFilter(log));
        FilterBases.add(new QLogicalDeleteFilter(log));
        FilterBases.add(new QListFilter(log));

        // loop while in the resolvedModels all modelFile are resolved
        int i = 1;
        final int maxInterations = 5;
        while (resolvedModels.size() < modelFileNames.length && i <= maxInterations) {
            if (log != null) log.info("parsing iteration : " + i);
            for (String fileName : modelFileNames) {
                final String modelName = StringUtil.getClassNameFromFileName(fileName);
                if (resolvedModels.contains(modelName)) {
                    continue;
                }
                if (excludeClass(modelName)) {
                    if (log != null) log.info(String.format("Class %s is excluded from parsing", modelName));
                    resolvedModels.add(modelName);
                    continue;
                }
                // if this class is not resolved, parse it
                try {
                    if (log != null) log.info("Parsing : " + fileName);
                    JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(parameters.modelPath, fileName));
                    String superClassName = javaClass.getSuperType();
                    if (superClassName.contains(".")) {
                        superClassName = superClassName.substring(superClassName.lastIndexOf('.') + 1);
                    }
                    /*
                    if (!superClassName.equals("PanacheEntityBase") && !resolvedModels.contains(superClassName)) {
                        continue;
                    }
                     */
                    // if the superclass is PanacheEntityBase or is resolved, continue with parsing filterdef
                    Map<FilterType, LinkedHashSet<FilterBase>> allFilterDefs = new LinkedHashMap<>();
                    if (resolvedModels.contains(superClassName)) {
                        if (log != null)
                            log.info("Inheriting ALL FilterDefs for class " + modelName + " from " + superClassName);
                        // inherit all filterdefs
                        Map<FilterType, LinkedHashSet<FilterBase>> allInheritedFilterDefs = filterMap.get(superClassName);
                        for (FilterType filterType : allInheritedFilterDefs.keySet()) {
                            LinkedHashSet<FilterBase> filterTypeSet = allFilterDefs.get(filterType);
                            if (filterTypeSet == null) {
                                filterTypeSet = new LinkedHashSet<>();
                            }
                            filterTypeSet.addAll(allInheritedFilterDefs.get(filterType));
                            allFilterDefs.put(filterType, filterTypeSet);
                        }
                    }
                    // add, if any, Q class level annotation
                    boolean qClassLevelAnnotation = false;
                    for (AnnotationSource<JavaClassSource> a : javaClass.getAnnotations()) {
                        if (a.getName().equals("Q")) {
                            if (log != null)
                                log.info(String.format("Class %s has Q annotation at class level", modelName));
                            qClassLevelAnnotation = true;
                        }
                    }
                    List<FieldSource<JavaClassSource>> fields = javaClass.getFields();
                    String entityName = javaClass.getName(); // gets the simple name of the class
                    for (FieldSource<JavaClassSource> f : fields) {
                        for (FilterBase fdbase : FilterBases) {
                            FilterBase fd = fdbase.parseQFilter(entityName, f, qClassLevelAnnotation);
                            if (null != fd) {
                                final FilterType filterType = fd.getFilterType();
                                LinkedHashSet<FilterBase> filterTypeSet = allFilterDefs.get(filterType);
                                if (filterTypeSet == null) {
                                    filterTypeSet = new LinkedHashSet<>();
                                }

                                if (fd.overrideOnSameFilterName()) {
                                    if (filterTypeSet.contains(fd)) {
                                        if (log != null) log.info("Override of filterdef " + fd.toString());
                                        filterTypeSet.remove(fd);
                                    }
                                }
                                filterTypeSet.add(fd);
                                allFilterDefs.put(filterType, filterTypeSet);
                            }
                        }
                    }
                    // print log info
                    for (FilterType filterType : allFilterDefs.keySet()) {
                        LinkedHashSet<FilterBase> filterTypeSet = allFilterDefs.get(filterType);
                        for (FilterBase fd : filterTypeSet) {
                            if (log != null)
                                log.info(String.format("class %s extends %s: %s", modelName, superClassName, fd.toString()));
                        }
                    }
                    filterMap.put(modelName, allFilterDefs);
                    resolvedModels.add(modelName);
                } catch (Exception e) {
                    log.error(e);
                }
            }
            i++;
        }
        if (i > maxInterations) {
            log.error("Parsing failed : Not all model files parsed. ALL Model classes must extend PanacheEntityBase.");
            return false;
        }
        if (log != null) log.info("All model files parsed.");
        return true;
    }

    @Override
    public String toString() {
        return "ModelFilesV4{" +
                "modelFileNames=" + Arrays.toString(modelFileNames) +
                ", isParsingSuccessful=" + isParsingSuccessful +
                ", filterMap=" + filterMap +
                ", rsPathMap=" + rsPathMap +
                ", defaultOrderByMap=" + defaultOrderByMap +
                ", excludeClassMap=" + excludeClassMap +
                ", qualifiedClassName=" + qualifiedClassName +
                ", idFieldNameMap=" + idFieldNameMap +
                ", idFieldTypeMap=" + idFieldTypeMap +
                ", tableNameMap=" + tableNameMap +
                '}';
    }
}
