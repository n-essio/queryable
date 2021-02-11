package it.ness.queryable.util;

import it.ness.queryable.model.FilterDefBase;
import it.ness.queryable.model.*;
import it.ness.queryable.model.enums.FilterType;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public class ModelFiles {

    protected Log log;
    protected StringUtil stringUtil;
    private String[] modelFileNames;
    private String path;
    public boolean isParsingSuccessful;

    private Map<String, Map<FilterType, LinkedHashSet<FilterDefBase>>> filterDefMap = new LinkedHashMap<>();
    private Map<String, String> rsPathMap = new LinkedHashMap<>();
    private Map<String, String> defaultOrderByMap = new LinkedHashMap<>();
    private Map<String, Boolean> excludeClassMap = new LinkedHashMap<>();

    public ModelFiles(Log log, StringUtil stringUtil, String groupId) {
        this.log = log;
        this.stringUtil = stringUtil;
        isParsingSuccessful = false;

        path = "src/main/java/";
        path += groupId.replaceAll("\\.", "/");
        path += "/model/";

        log.info("path = " + path);

        File f = new File(path);
        if (!f.exists()) {
            log.error(String.format("Path %s doesn't exist.", path));
            return;
        }

        modelFileNames = f.list(new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".java");
            }
        });
        if (modelFileNames != null && modelFileNames.length > 0) {
            log.info("Total model classes found : " + modelFileNames.length);
            log.info("model class file names : " + Arrays.toString(modelFileNames));
            resolveConstant();
            isParsingSuccessful = resolveFilterDefs();
        }
        else {
            log.error("No model classes found in path :" + path);
        }
    }

    public String getPath() {
        return path;
    }

    public Set<FilterDefBase> getFilterDef(final String className, final FilterType filterType) {
        if (excludeClass(className)) return null;
        Map<FilterType, LinkedHashSet<FilterDefBase>> allFilterDefs = filterDefMap.get(className);
        if (allFilterDefs == null) return null;
        return allFilterDefs.get(filterType);
    }

    public String[] getModelFileNames() {
        return modelFileNames;
    }

    public String getRsPath(final String className) {
        return rsPathMap.get(className);
    }

    public String getDefaultOrderBy(final String className) {
        return defaultOrderByMap.get(className);
    }

    public Boolean excludeClass(final String className) {
        return excludeClassMap.get(className);
    }

    private void resolveConstant() {
        for (String fileName : modelFileNames) {
            String className = StringUtil.getClassNameFromFileName(fileName);
            final String defaultRsPath = "NOT_SET";
            final String defaultOrderBy = "not_set";
            String rsPath = defaultRsPath;
            String orderBy = defaultOrderBy;
            Boolean excludeClass = false;
            // override if annotation is present
            try {
                JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(path, fileName));
                AnnotationSource<JavaClassSource> a = javaClass.getAnnotation("QExclude");
                if (null != a) {
                    excludeClass = true;
                }
                a = javaClass.getAnnotation("QRs");
                if (null != a) {
                    rsPath = a.getLiteralValue("path");
                    rsPath = StringUtil.removeQuotes(rsPath);
                }
                a = javaClass.getAnnotation("QOrderBy");
                if (null != a) {
                    orderBy = a.getLiteralValue("orderBy");
                    orderBy = StringUtil.removeQuotes(orderBy);
                }
            } catch (Exception e) {
                log.error(e);
            }
            rsPathMap.put(className, rsPath);
            defaultOrderByMap.put(className, orderBy);
            excludeClassMap.put(className, excludeClass);
            if (!excludeClass) {
                if (defaultOrderBy.equals(orderBy)) {
                    log.warn(String.format("orderBy for class %s : %s", className, orderBy));
                }
                if (defaultRsPath.equals(rsPath)) {
                    log.warn(String.format("rsPath for class %s : %s", className, rsPath));
                }
            }
        }
    }

    private boolean resolveFilterDefs() {
        Set<String> resolvedModels = new HashSet<>();

        Set<FilterDefBase> filterDefBases = new HashSet<>();

        // add all supported filterdef bases to parse
        filterDefBases.add(new QFilterDef(log, stringUtil));
        filterDefBases.add(new QLikeFilterDef(log, stringUtil));
        filterDefBases.add(new QNilFilterDef(log, stringUtil));
        filterDefBases.add(new QNotNilFilterDef(log, stringUtil));
        filterDefBases.add(new QLogicalDeleteFilterDef(log, stringUtil));
        filterDefBases.add(new QListFilterDef(log, stringUtil));
        filterDefBases.add(new QLikeListFilterDef(log, stringUtil));

        // loop while in the resolvedModels all modelFile are resolved
        int i = 1;
        final int maxInterations = 5;
        while (resolvedModels.size() < modelFileNames.length && i<=maxInterations) {
            log.info("parsing iteration : " + i);
            for (String fileName : modelFileNames) {
                final String modelName = StringUtil.getClassNameFromFileName(fileName);
                if (resolvedModels.contains(modelName)) {
                    continue;
                }
                if (excludeClass(modelName)) {
                    log.info(String.format("Class %s is excluded from parsing", modelName));
                    resolvedModels.add(modelName);
                    continue;
                }
                // if this class is not resolved, parse it
                try {
                    log.info("Parsing : " + fileName);
                    JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(path, fileName));
                    String superClassName = javaClass.getSuperType();
                    if (superClassName.contains(".")) {
                        superClassName = superClassName.substring(superClassName.lastIndexOf('.') + 1);
                    }
                    if (!superClassName.equals("PanacheEntityBase") && !resolvedModels.contains(superClassName)) {
                        continue;
                    }
                    // if the superclass is PanacheEntityBase or is resolved, continue with parsing filterdef
                    Map<FilterType, LinkedHashSet<FilterDefBase>> allFilterDefs = new LinkedHashMap<>();
                    if (resolvedModels.contains(superClassName)) {
                        log.info("Inheriting ALL FilterDefs for class " + modelName + " from " + superClassName);
                        // inherit all filterdefs
                        Map<FilterType, LinkedHashSet<FilterDefBase>> allInheritedFilterDefs = filterDefMap.get(superClassName);
                        for (FilterType filterType : allInheritedFilterDefs.keySet()) {
                            LinkedHashSet<FilterDefBase> filterTypeSet = allFilterDefs.get(filterType);
                            if (filterTypeSet == null) {
                                filterTypeSet = new LinkedHashSet<>();
                            }
                            filterTypeSet.addAll(allInheritedFilterDefs.get(filterType));
                            allFilterDefs.put(filterType, filterTypeSet);
                        }
                    }
                    List<FieldSource<JavaClassSource>> fields = javaClass.getFields();
                    for (FieldSource<JavaClassSource> f : fields) {
                        for (FilterDefBase fdbase : filterDefBases) {
                            FilterDefBase fd = fdbase.parseQFilterDef(f);
                            if (null != fd) {
                                final FilterType filterType = fd.getFilterType();
                                LinkedHashSet<FilterDefBase> filterTypeSet = allFilterDefs.get(filterType);
                                if (filterTypeSet == null) {
                                    filterTypeSet = new LinkedHashSet<>();
                                }

                                if (fd.overrideOnSameFilterName()) {
                                    if (filterTypeSet.contains(fd)) {
                                        log.info("Override of filterdef " + fd.toString());
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
                        LinkedHashSet<FilterDefBase> filterTypeSet = allFilterDefs.get(filterType);
                        for (FilterDefBase fd : filterTypeSet) {
                            log.info(String.format("class %s extends %s: %s", modelName, superClassName, fd.toString()));
                        }
                    }
                    filterDefMap.put(modelName, allFilterDefs);
                    resolvedModels.add(modelName);
                } catch (Exception e) {
                    log.error(e);
                }
            }
            i++;
        }
        if (i>maxInterations) {
            log.error("Parsing failed : Not all model files parsed. ALL Model classes must extend PanacheEntityBase.");
            return false;
        }
        log.info("All model files parsed.");
        return true;
    }
}
