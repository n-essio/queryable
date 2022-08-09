package it.ness.queryable.builder;

import it.ness.queryable.model.QApi;
import it.ness.queryable.model.QMethodApi;
import it.ness.queryable.model.pojo.ApiDataPojo;
import it.ness.queryable.model.pojo.Parameters;
import it.ness.queryable.util.FileUtils;
import it.ness.queryable.util.ModelFiles;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Annotation;
import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenApiBuilder {

    protected static String ANNOTATION_ID = "Id";

    public static void generateSources(ModelFiles mf, Log log, Parameters parameters,
                                       String packageName) throws Exception {

        String[] modelFiles = mf.getModelFileNames();
        List<ApiDataPojo> apiDataPojoList = new ArrayList<>();
        Set<String> classSet = new LinkedHashSet<>();
        Set<String> simpleClassSet = new LinkedHashSet<>();
        Set<String> enumPojoSet = new LinkedHashSet<>();

        for (String modelFileName : modelFiles) {
            String className = StringUtil.getClassNameFromFileName(modelFileName);
            classSet.add(mf.getQualifiedClassName(className));
            simpleClassSet.add(className);
        }

        for (String modelFileName : modelFiles) {
            StringBuilder sb = new StringBuilder();
            String className = StringUtil.getClassNameFromFileName(modelFileName);
            String qualifiedClassName = mf.getQualifiedClassName(className);
            if (!mf.excludeClass(className)) {
                try {
                    ApiDataPojo apiDataPojo = readModel(log, modelFileName, parameters, qualifiedClassName);
                    apiDataPojoList.add(apiDataPojo);
                    enumPojoSet.addAll(getEnumPojoTypes(log, apiDataPojo, packageName, classSet, enumPojoSet));
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }

        if (log != null) log.info("classSet : " + classSet);

        Set<String> toParseEnumPojoSet = new LinkedHashSet<>(enumPojoSet);

        int i = 1;
        while (toParseEnumPojoSet.size() > 0 && i <= 10) {
            if (log != null) log.info("parse iteration : " + i);

            Set<String> newToParseEnumPojoSet = new LinkedHashSet<>();
            for (String className : toParseEnumPojoSet) {
                ApiDataPojo apiDataPojo = readEnumPojo(log, className, parameters, packageName);
                Set<String> qualifiedNameSet = getEnumPojoTypes(log, apiDataPojo, packageName, classSet, enumPojoSet);
                for (String qualifiedName : qualifiedNameSet) {
                    if (!enumPojoSet.contains(qualifiedName)) {
                        newToParseEnumPojoSet.add(qualifiedName);
                    }
                }
                apiDataPojoList.add(apiDataPojo);
            }

            enumPojoSet.addAll(newToParseEnumPojoSet);
            toParseEnumPojoSet.clear();
            toParseEnumPojoSet.addAll(newToParseEnumPojoSet);
            i++;
        }

        for (String className : enumPojoSet) {
            apiDataPojoList.add(readEnumPojo(log, className, parameters, packageName));
        }

        if (log != null) log.info("apiDataPojoList size : " + apiDataPojoList.size());


        for (ApiDataPojo apiDataPojo : apiDataPojoList) {
            print(log, apiDataPojo, classSet, simpleClassSet, enumPojoSet);
        }
        for (ApiDataPojo apiDataPojo : apiDataPojoList) {
            printServices(log, apiDataPojo, classSet, simpleClassSet, enumPojoSet);
        }
        for (ApiDataPojo apiDataPojo : apiDataPojoList) {
            printComponents(log, apiDataPojo, classSet, simpleClassSet, enumPojoSet);
        }

        if (log != null) log.info("Done generating openapi sources");
    }

    private List<String> getServiceRsFiles(Path filePath) throws IOException {
        try (Stream<Path> walk = Files.walk(filePath)) {
            return walk
                    .filter(p -> !Files.isDirectory(p))   // not a directory
                    .map(Path::toString) // convert path to string
                    .filter(f -> f.endsWith("ServiceRs.java"))       // check end with
                    .collect(Collectors.toList());
        }
    }

    private static void printServices(Log log, ApiDataPojo apiDataPojo, Set<String> classSet, Set<String> simpleClassSet, Set<String> enumPojoSet) {
        if (apiDataPojo.rsPath == null) {
            return;
        }
        if (log != null)
            log.info("******************** printServices ");

        if (log != null)
            log.info(getListMethod(log, apiDataPojo, classSet, simpleClassSet, enumPojoSet));

    }

    private static void printComponents(Log log, ApiDataPojo apiDataPojo, Set<String> classSet, Set<String> simpleClassSet, Set<String> enumPojoSet) {

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("\n%s:\n", apiDataPojo.className));
        if (apiDataPojo.qualifiedClassName.contains("enum")) {
            sb.append(getIndent(1) + "enum:\n");
            for (QApi qApi : apiDataPojo.apiFieldList) {
                String enumName = qApi.enumField.getName();
                sb.append(getIndent(3) + String.format("- %s:\n", enumName));
            }
            sb.append(getIndent(1) + "type: string\n");
        } else {
            sb.append(getIndent(1) + "type : object\n");
            sb.append(getIndent(2) + "properties:\n");
            for (QApi qApi : apiDataPojo.apiFieldList) {
                String fieldName = qApi.field.getName();
                String fieldType = qApi.field.getType().getName();
                String fieldQualifiedType = qApi.field.getType().getQualifiedName();

                sb.append(getIndent(3) + String.format("%s:\n", fieldName));
                String openApiType = getOpenApiType(fieldType);
                String openApiFormat = getOpenApiFormat(fieldType);
                if (openApiType != null) {
                    sb.append(getIndent(4) + String.format("type: %s\n", openApiType));
                    if (openApiFormat != null) {
                        sb.append(getIndent(4) + String.format("format: %s\n", openApiFormat));
                    }
                } else {
                    sb.append(getIndent(4) + String.format("$ref: '#/components/schemas/%s'\n", fieldType));
                }
            }
        }
        if (log != null)
            log.info(sb.toString());
    }

    private static void print(Log log, ApiDataPojo apiDataPojo, Set<String> classSet, Set<String> simpleClassSet, Set<String> enumPojoSet) {
        if (log != null)
            log.info("qualifiedClassName : "  + apiDataPojo.qualifiedClassName);

        for (QApi qApiField : apiDataPojo.apiFieldList) {
            if (qApiField.field != null) {
                FieldSource<JavaClassSource> field = qApiField.field;
                Type<JavaClassSource> fieldType = field.getType();
                String fieldTypeName = fieldType.getName();

                List<AnnotationSource<JavaClassSource>> list = qApiField.annotations;
                String fieldName = qApiField.field.getName();

                String simpleType = isKnownSimpleType(fieldType);
                if (qApiField.isId) {
                    if (log != null)
                        log.info("id field : " + fieldName);
                }
                if (simpleType != null) {
                    if (log != null)
                        log.info("simple type : " + qApiField.field.getType().getName() + " : " + fieldName);
                    continue;
                }

                if (fieldTypeName.equals("List") || fieldTypeName.equals("Set")) {
                    Type<JavaClassSource> param = qApiField.field.getType().getTypeArguments().get(0);

                    String paramSimpleType = isKnownSimpleType(param);
                    if (paramSimpleType != null) {
                        if (log != null) log.info("simple type list : " + param.getName() + " : " + fieldName);
                        continue;
                    }
                    if (classSet.contains(param.getQualifiedName())) {
                        if (log != null)
                            log.info("complex type list : " + param.getQualifiedName() + " : " + fieldName);
                        continue;
                    }
                    if (enumPojoSet.contains(param.getQualifiedName())) {
                        if (log != null)
                            log.info("complex type list : " + param.getQualifiedName() + " : " + fieldName);
                        continue;
                    }
                    if (simpleClassSet.contains(param.getName())) {
                        if (log != null)
                            log.info("complex type list : " + param.getName() + " : " + fieldName);
                        continue;
                    }
                    throw new RuntimeException("ERROR: not know type: " + param.getQualifiedName());
                }
                if (classSet.contains(fieldType.getQualifiedName())) {
                    if (log != null)
                        log.info("complex type : " + fieldType.getQualifiedName() + " : " + fieldName);
                    continue;
                }
                if (enumPojoSet.contains(fieldType.getQualifiedName())) {
                    if (log != null)
                        log.info("complex type : " + fieldType.getQualifiedName() + " : " + fieldName);
                    continue;
                }
                if (simpleClassSet.contains(fieldType.getName())) {
                    if (log != null)
                        log.info("complex type list : " + fieldType.getName() + " : " + fieldName);
                    continue;
                }
                throw new RuntimeException("ERROR: not know type: " + fieldType.getQualifiedName());
            } else {
                String fieldName = qApiField.enumField.getName();
                if (log != null)
                    log.info("enum type : " + fieldName);
            }
        }

        if (apiDataPojo.apiMethodList == null || apiDataPojo.apiMethodList.size() == 0) {
            return;
        }
        if (log != null)
            log.info("****************method list ");

        for (QMethodApi qMethodApi : apiDataPojo.apiMethodList) {
            if (qMethodApi.methodPath != null) {
                if (log != null)
                    log.info("methodPath : " + qMethodApi.methodPath);
            }
        }

        if (apiDataPojo.filterDefs != null && apiDataPojo.filterDefs.size() > 0) {
            log.info("filterDefs : " + apiDataPojo.filterDefs);
        }
    }

    private static String isKnownSimpleType(Type<JavaClassSource> fieldType) {
        String fieldTypeName = fieldType.getName();
        String fieldTypeQualifiedName = fieldType.getQualifiedName();

        if (fieldTypeName.equals("String")) {
            return fieldTypeName;
        }
        if (fieldTypeName.equalsIgnoreCase("int") ||
                fieldTypeName.equalsIgnoreCase("integer") ||
                fieldTypeName.equalsIgnoreCase("long") ||
                fieldTypeName.equalsIgnoreCase("boolean") ||
                fieldTypeName.equalsIgnoreCase("double") ||
                fieldTypeName.equalsIgnoreCase("float")) {
            return fieldTypeName;
        }

        if ("LocalDateTime".equals(fieldTypeName) || "LocalDate".equals(fieldTypeName) || "java.util.Date".equals(fieldTypeQualifiedName)) {
            return fieldTypeName;
        }
        if (fieldTypeName.equals("BigDecimal")) {
            return fieldTypeName;
        }
        if (fieldTypeName.equals("BigInteger")) {
            return fieldTypeName;
        }
        return null;
    }

    private static boolean isKnownSimpleType(String fieldTypeName) {

        if (fieldTypeName.equalsIgnoreCase("String")) {
            return true;
        }
        if (fieldTypeName.equalsIgnoreCase("int") ||
                fieldTypeName.equalsIgnoreCase("integer") ||
                fieldTypeName.equalsIgnoreCase("long") ||
                fieldTypeName.equalsIgnoreCase("boolean") ||
                fieldTypeName.equalsIgnoreCase("double") ||
                fieldTypeName.equalsIgnoreCase("float")) {
            return true;
        }

        if ("LocalDateTime".equals(fieldTypeName) || "LocalDate".equals(fieldTypeName) || "java.util.Date".equals(fieldTypeName)) {
            return true;
        }
        if (fieldTypeName.equals("BigDecimal")) {
            return true;
        }
        if (fieldTypeName.equals("BigInteger")) {
            return true;
        }
        return false;
    }

    private static String getOpenApiType(String fieldTypeName) {

        if (fieldTypeName.equalsIgnoreCase("String")) {
            return "string";
        }
        if (fieldTypeName.equalsIgnoreCase("int") ||
                fieldTypeName.equalsIgnoreCase("integer") ||
                fieldTypeName.equalsIgnoreCase("long")) {
            return "integer";
        }
        if (fieldTypeName.equalsIgnoreCase("double") ||
                fieldTypeName.equalsIgnoreCase("float")) {
            return "number";
        }

        if (fieldTypeName.equalsIgnoreCase("boolean")) {
            return "boolean";
        }

        if ("LocalDateTime".equals(fieldTypeName) || "LocalDate".equals(fieldTypeName) || "java.util.Date".equals(fieldTypeName)) {
            return "string";
        }
        if (fieldTypeName.equals("BigDecimal")) {
            return "number";
        }
        if (fieldTypeName.equals("BigInteger")) {
            return "integer";
        }
        return null;
    }

    private static String getOpenApiFormat(String fieldTypeName) {

        if (fieldTypeName.equalsIgnoreCase("String")) {
            return null;
        }
        if (fieldTypeName.equalsIgnoreCase("int") ||
                fieldTypeName.equalsIgnoreCase("integer")) {
            return "int32";
        }
        if (fieldTypeName.equalsIgnoreCase("long")) {
            return "int64";
        }
        if (fieldTypeName.equalsIgnoreCase("double")) {
            return "double";
        }
        if (fieldTypeName.equalsIgnoreCase("float")) {
            return "float";
        }
        if (fieldTypeName.equalsIgnoreCase("boolean")) {
            return null;
        }
        if ("LocalDateTime".equals(fieldTypeName)) {
            return "date-time";
        }
        if ("LocalDate".equals(fieldTypeName) || "java.util.Date".equals(fieldTypeName)) {
            return "date";
        }
        if (fieldTypeName.equals("BigDecimal")) {
            return "double";
        }
        if (fieldTypeName.equals("BigInteger")) {
            return "int64";
        }
        return null;
    }


    private static Set<String> getEnumPojoTypes(Log log, ApiDataPojo apiDataPojo, String packageName, Set<String> classSet, Set<String> enumPojoSet) {
        Set<String> pojoEnumSet = new LinkedHashSet<>();

        for (QApi qApiField : apiDataPojo.apiFieldList) {
            if (qApiField.field == null) {
                continue;
            }
            String fieldTypeName = qApiField.field.getType().getQualifiedName();
            String fieldName = qApiField.field.getName();

            if (fieldTypeName.startsWith(packageName + ".model")) {
                if (log != null) log.info(fieldTypeName + " : " + fieldName);
                if (classSet.contains(fieldTypeName)) {
                    continue;
                }
                if (enumPojoSet.contains(fieldTypeName)) {
                    continue;
                }
                pojoEnumSet.add(fieldTypeName);
                continue;
            }
            if (qApiField.field.getType().getName().equals("List") || qApiField.field.getType().getName().equals("Set")) {
                if (log != null) log.info(qApiField.field.getType().getName() + " : " + fieldName);
                Type<JavaClassSource> param = qApiField.field.getType().getTypeArguments().get(0);
                String paramQualifiedName = param.getQualifiedName();
                if (paramQualifiedName.startsWith(packageName + ".model")) {
                    if (log != null) log.info(paramQualifiedName + " : " + fieldName);
                    if (classSet.contains(paramQualifiedName)) {
                        continue;
                    }
                    if (enumPojoSet.contains(paramQualifiedName)) {
                        continue;
                    }
                    pojoEnumSet.add(paramQualifiedName);
                    continue;
                }
            }
        }
        return pojoEnumSet;
    }

    private static ApiDataPojo readModel(Log log, String modelFileName, Parameters parameters, String qualifiedClassName) throws Exception {
        String path = parameters.modelPath;
        JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(path, modelFileName));

        List<FieldSource<JavaClassSource>> fieldsList = javaClass.getFields();
        ApiDataPojo apiDataPojo = new ApiDataPojo();
        apiDataPojo.qualifiedClassName = qualifiedClassName;
        apiDataPojo.className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);
        apiDataPojo.filterDefs = new HashMap<>();
        for (AnnotationSource<JavaClassSource> anno : javaClass.getAnnotations()) {
            if (anno.getName().equals("QRs")) {
                apiDataPojo.rsPath = getConstantValue(log, parameters, anno.getStringValue(), javaClass);
            }
            if (anno.getName().equals("FilterDef")) {
                String filterName = anno.getStringValue("name");
                Annotation<JavaClassSource> filterParameter = anno.getAnnotationValue("parameters");
                String paramName = filterParameter.getStringValue("name");
                String paramType = filterParameter.getStringValue("type");
                String[] filterNamesToken = filterName.split("\\.");

                if (filterNamesToken.length > 1) {
                    filterName = filterNamesToken[filterNamesToken.length - 2] + "." + filterNamesToken[filterNamesToken.length - 1];
                }

                String ref = null;
                if (apiDataPojo.apiFieldList != null && apiDataPojo.apiFieldList.size() > 0) {
                    String fieldName = filterName.substring(filterName.indexOf('.') + 1);
                    for (QApi qApis : apiDataPojo.apiFieldList) {
                        if (qApis.field.getName().equals(fieldName)) {
                            if (qApis.enumField != null) {
                                ref = qApis.enumField.getName();
                            }
                        }
                    }
                }

                if (ref == null) {
                    apiDataPojo.filterDefs.put(filterName, paramType);
                } else {
                    apiDataPojo.filterDefs.put(filterName, ref);
                }
            }
        }

        apiDataPojo.apiFieldList = new ArrayList<>();

        for (FieldSource<JavaClassSource> field : fieldsList) {
            List<AnnotationSource<JavaClassSource>> list = field.getAnnotations();
            QApi qApiField = getDefaultsField(field, list);
            if (qApiField != null) {
                apiDataPojo.apiFieldList.add(qApiField);
            }
        }

        String serviceFileName = modelFileName.substring(0, modelFileName.length() - ".java".length()) +
                "ServiceRs.java";
        JavaClassSource javaServiceRsClass = Roaster.parse(JavaClassSource.class, new File(parameters.serviceRsPath, serviceFileName));


        apiDataPojo.apiMethodList = new ArrayList<>();
        List<MethodSource<JavaClassSource>> methodsList = javaServiceRsClass.getMethods();
        for (MethodSource<JavaClassSource> method : methodsList) {
            List<AnnotationSource<JavaClassSource>> methodAnnotations = method.getAnnotations();
            boolean isRsMethod = false;
            String methodPath = null;
            for (AnnotationSource<JavaClassSource> anno : methodAnnotations) {
                if (anno.getName().equalsIgnoreCase("GET") ||
                        anno.getName().equalsIgnoreCase("POST") ||
                        anno.getName().equalsIgnoreCase("PUT") ||
                        anno.getName().equalsIgnoreCase("DELETE")) {
                    isRsMethod = true;
                }
                if (anno.getName().equalsIgnoreCase("Path")) {
                    methodPath = anno.getStringValue();
                }
            }
            if (!isRsMethod) {
                continue;
            }

            List<ParameterSource<JavaClassSource>> methodParameters = method.getParameters();

            QMethodApi qMethodApi = new QMethodApi();
            qMethodApi.methodParameters = methodParameters;
            qMethodApi.methodAnnotations = methodAnnotations;
            qMethodApi.methodPath = methodPath;
            apiDataPojo.apiMethodList.add(qMethodApi);

            if (log != null) log.info(" ****** qMethodApi : " + qMethodApi);

        }
        return apiDataPojo;
    }

    private static ApiDataPojo readEnumPojo(Log log, String className,
                                             Parameters parameters, String packageName) throws Exception {
        String classPath = className.substring(packageName.length() + ".model".length());
        String filePath = parameters.modelPath + classPath.replaceAll("\\.", "/") + ".java";

        if (log != null) log.info(" className : " + className + " filePath :" + filePath);

        try {
            ApiDataPojo apiDataPojo = new ApiDataPojo();
            apiDataPojo.qualifiedClassName = className;
            apiDataPojo.className = className.substring(className.lastIndexOf('.') + 1);
            JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(filePath));

            List<FieldSource<JavaClassSource>> fieldsList = javaClass.getFields();

            apiDataPojo.apiFieldList = new ArrayList<>();

            for (FieldSource<JavaClassSource> field : fieldsList) {
                List<AnnotationSource<JavaClassSource>> list = field.getAnnotations();
                QApi qApiField = getDefaultsField(field, list);
                if (qApiField != null) {
                    if (log != null) log.info(" qApiField : " + qApiField);
                    apiDataPojo.apiFieldList.add(qApiField);
                }
            }
            return apiDataPojo;
        } catch (Exception e) {

        }

        ApiDataPojo apiDataPojo = new ApiDataPojo();
        apiDataPojo.qualifiedClassName = className;
        apiDataPojo.className = className.substring(className.lastIndexOf('.') + 1);
        JavaEnumSource javaClass = Roaster.parse(JavaEnumSource.class, new File(filePath));

        List<EnumConstantSource> fieldsList = javaClass.getEnumConstants();

        apiDataPojo.apiFieldList = new ArrayList<>();

        for (EnumConstantSource field : fieldsList) {
            QApi qApiField = getDefaultsField(field);
            apiDataPojo.apiFieldList.add(qApiField);
        }
        return apiDataPojo;
    }

    public static QApi getDefaultsField(FieldSource<JavaClassSource> field,
                                      List<AnnotationSource<JavaClassSource>> list) {

        if (field.isPublic() && field.isStatic() && field.isFinal()) {
            return null;
        }
        if (field.isTransient() || field.isVolatile()) {
            return null;
        }
        boolean isId = false;
        if (list != null && list.size() > 0) {
            for (AnnotationSource<JavaClassSource> anno : list) {
                if (anno.getName().equals(ANNOTATION_ID)) {
                    isId = true;
                }
            }
        }

        QApi qApiField = new QApi();
        qApiField.field = field;
        qApiField.annotations = list;
        qApiField.isId = isId;
        return qApiField;
    }

    public static QApi getDefaultsField(EnumConstantSource field) {
        boolean isId = false;

        QApi qApiField = new QApi();
        qApiField.enumField = field;
        qApiField.annotations = null;
        qApiField.isId = isId;
        return qApiField;
    }

    public static String getPath() {
        return null;
    }

    public static String getListMethod(Log log, ApiDataPojo apiDataPojo, Set<String> classSet, Set<String> simpleClassSet, Set<String> enumPojoSet) {
        StringBuilder sb = new StringBuilder();

        sb.append("\npaths:\n");
        sb.append(getIndent(1) + String.format("%s\n", apiDataPojo.rsPath));
        sb.append(getIndent(2) + "get:\n");
        sb.append(getIndent(3) + String.format("summary: Ritorna la lista degli %s\n", apiDataPojo.className));
        sb.append(getIndent(3) + "tags:\n");
        sb.append(getIndent(4) + String.format("- %s\n", apiDataPojo.className));
        sb.append(getIndent(3) + "parameters:\n");


        getQueryParam(sb, "orderBy", null, null, null,
                "false", "string", "description: ordinamento della query (compreso di ASC/DESC)");

        getQueryParam(sb, "startRow", null, "int32", "0",
                "false", "integer", "offset per la paginazione ");

        if (apiDataPojo.filterDefs != null && apiDataPojo.filterDefs.size() > 0) {
            Set<String> keySet = apiDataPojo.filterDefs.keySet();
            for (String key : keySet) {
                String fieldType = apiDataPojo.filterDefs.get(key);
                String ref = null;
                String format = null;
                if (isKnownSimpleType(fieldType)) {
                    format = fieldType;
                } else {
                    ref = fieldType;
                }
                getQueryParam(sb, key, ref, format, null,
                        "false", null, null);
            }
        }
        sb.append(getListResponse(log, apiDataPojo, classSet, simpleClassSet, enumPojoSet));

        return sb.toString();
    }

    public static String getListResponse(Log log, ApiDataPojo apiDataPojo, Set<String> classSet, Set<String> simpleClassSet, Set<String> enumPojoSet){
        StringBuilder sb = new StringBuilder();

        sb.append(getIndent(3) + "responses:\n");
        sb.append(getIndent(4) + "'200':\n");
        sb.append(getIndent(4) + "description: OK\n");
        sb.append(getIndent(4) + "headers:\n");
        sb.append(getIndent(5) + "listSize:\n");
        sb.append(getIndent(6) + "description: il numero di elementi restituiti dalla chiamata\n");
        sb.append(getIndent(6) + "schema:\n");
        sb.append(getIndent(7) + "type: integer\n");
        sb.append(getIndent(5) + "startRow:\n");
        sb.append(getIndent(6) + "description: l'offset per la paginazione\n");
        sb.append(getIndent(6) + "schema:\n");
        sb.append(getIndent(7) + "type: integer\n");
        sb.append(getIndent(5) + "pageSize:\n");
        sb.append(getIndent(6) + "description: il numero di elementi per pagina \n");
        sb.append(getIndent(6) + "schema:\n");
        sb.append(getIndent(7) + "type: integer\n");
        sb.append(getIndent(4) + "content:\n");
        sb.append(getIndent(5) + "application/json:\n");
        sb.append(getIndent(6) + "schema\n");
        sb.append(getIndent(7) + String.format("$ref: '#/components/schemas/%s'\n", apiDataPojo.className));
        sb.append(getIndent(3) + "security:\n");
        sb.append(getIndent(4) + "- kimeraToken: []\n");

        return sb.toString();
    }


    public static void getQueryParam(StringBuilder sb,
                                       String name, String ref, String format, String defaultValue,
                                       String required, String type, String description) {

        sb.append(getIndent(4) + String.format("- name: %s\n", name));
        sb.append(getIndent(5) + "in: query\n");
        sb.append(getIndent(5) + "schema:\n");
        if (ref != null) {
            sb.append(getIndent(6) + String.format("$ref: '#/components/schemas/%s'\n", ref));
        }

        if (format != null) {
            sb.append(getIndent(6) + String.format("format: %s\n", format));
        }
        if (defaultValue != null) {
            sb.append(getIndent(6) + String.format("default: \"%s\"\n", defaultValue));
        }
        if (type != null) {
            sb.append(getIndent(6) + String.format("type: %s\n", type));
        }
        if (required != null) {
            sb.append(getIndent(5) + String.format("required: %s\n", required));
        }
        if (description != null) {
            sb.append(getIndent(5) + String.format("description: %s\n", description));
        }
    }

    public static String getIndent(int level) {
        return " ".repeat(Math.max(0, level*2));
    }

    private static String getConstantValue(Log log, Parameters parameters, String constantName, JavaClassSource javaClass) {

        for (Import imp : javaClass.getImports()) {
            String constantImport = imp.getQualifiedName();
            if (constantImport.endsWith(constantName)) {

                String filePath = parameters.serviceRsPath;
                String constantPath = filePath.substring("src/main/java/".length(), filePath.length() - "/service/rs".length())
                        .replaceAll("/", "\\.");

                String subConstantPath = constantImport.replaceAll(constantPath, "").substring(1);

                String[] tokens = subConstantPath.split("\\.");
                StringBuilder appConstantFilePath = new StringBuilder(parameters.serviceRsPath.substring(0,
                        parameters.serviceRsPath.length() - "/service/rs".length()));
                for (int i=0; i<tokens.length-1; i++) {
                    appConstantFilePath.append("/").append(tokens[i]);
                }
                appConstantFilePath.append(".java");

                try {
                    JavaClassSource javaAppContantsClass = Roaster.parse(JavaClassSource.class,
                            new File(appConstantFilePath.toString()));

                    Map<String, String> constantsMap = new HashMap<>();

                    int i = 1;
                    while (i < 10) {
                        for (FieldSource<JavaClassSource> field : javaAppContantsClass.getFields()) {
                            String fieldName = field.getName().trim();
                            if (!constantsMap.containsKey(fieldName)) {
                                String fieldDeclaration = field.toString().split("=")[1];
                                if (!fieldDeclaration.contains("+")) {
                                    fieldDeclaration = fieldDeclaration.replaceAll("\"", "")
                                            .replaceAll(";", "").trim();
                                    constantsMap.put(fieldName, fieldDeclaration);
                                } else {
                                    String[] fieldTokens = fieldDeclaration.split("\\+");
                                    boolean allFieldsFound = true;
                                    for (String fToken : fieldTokens) {
                                        fToken = fToken.trim();
                                        if (!fToken.contains("\"") && !constantsMap.containsKey(fToken)) {
                                            allFieldsFound = false;
                                            break;
                                        }
                                    }

                                    if (allFieldsFound) {
                                        StringBuilder sb = new StringBuilder();
                                        for (String fToken : fieldTokens) {
                                            fToken = fToken.trim();
                                            if (fToken.contains("\"")) {
                                                sb.append(fToken.replaceAll("\"", "")
                                                        .replaceAll(";", "").trim());
                                            } else {
                                                sb.append(constantsMap.get(fToken));
                                            }
                                        }
                                        constantsMap.put(fieldName, sb.toString());
                                    }
                                }
                            }
                        }
                        i++;
                    }

                    if (log != null) log.info(" constantName :" + constantsMap.get(constantName));

                    return constantsMap.get(constantName);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

}
