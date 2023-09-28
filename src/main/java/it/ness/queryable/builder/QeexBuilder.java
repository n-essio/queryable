package it.ness.queryable.builder;

import it.ness.queryable.model.api.Parameters;
import it.ness.queryable.model.quex.QeexWebExceptionMethod;
import it.ness.queryable.util.FileUtils;
import it.ness.queryable.model.quex.ModelQuex;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

public class QeexBuilder {

    public static List<Path> findInterfacesAnnotatedWithQeexExceptionBundle() throws Exception {
        String sDir = "src/main/java";
        List<Path> paths = Files.find(Paths.get(sDir), 999, (p, bfa) -> bfa.isRegularFile()).toList();
        List<Path> interfacesList = new ArrayList<>();
        for (Path path : paths) {
            if (path.toString().endsWith("java")) {
                String cnt = Files.readString(path);
                if (cnt != null && cnt.contains("@QeexExceptionBundle") && cnt.contains("interface")) {
                    interfacesList.add(path);
                }
            }
        }
        return interfacesList;

    }

    public static void generateClassesImplementsExceptionBundle(Parameters parameters, Log log, ModelQuex modelQuex) throws Exception {
        List<Path> interfacesList = findInterfacesAnnotatedWithQeexExceptionBundle();
        for (Path path : interfacesList) {
            JavaInterfaceSource javaInterfaceSource = Roaster.parse(JavaInterfaceSource.class, new FileInputStream(path.toFile()));
            AnnotationSource<JavaInterfaceSource> annoQeexExceptionBundle = javaInterfaceSource.getAnnotation("QeexExceptionBundle");
            if (annoQeexExceptionBundle == null) {
                log.info("Ignoring qeex interface path : " + path);
                continue;
            }
            String project = getAnnotationValue(annoQeexExceptionBundle, "project", null);
            String language = getAnnotationValue(annoQeexExceptionBundle, "language", null);
            int defaultId = 100;
            String defaultIdStr = getAnnotationValue(annoQeexExceptionBundle, "id", null);
            if (defaultIdStr != null) {
                defaultId = Integer.parseInt(defaultIdStr);
            }
            defaultId++;

            Set<Integer> usedIds = new HashSet<>();

            List<String> methodImplementations = new ArrayList<>();
            List<MethodSource<JavaInterfaceSource>> methodList = javaInterfaceSource.getMethods();
            for (MethodSource<JavaInterfaceSource> method : methodList) {
                AnnotationSource<JavaInterfaceSource> a = method.getAnnotation("QeexMessage");
                if (a == null) {
                    continue;
                }
                String methodName = method.getName();

                String id = getAnnotationValue(a, "id", null);
                if (id == null) {
                    while (usedIds.contains(defaultId)) {
                        defaultId++;
                    }
                    id = String.valueOf(defaultId);
                }
                usedIds.add(Integer.valueOf(id));
                List<ParameterSource<JavaInterfaceSource>> paramList = method.getParameters();
                Map<String, String> arguments = new LinkedHashMap<>();
                if (paramList != null && !paramList.isEmpty()) {
                    for (ParameterSource<JavaInterfaceSource> p : paramList) {
                        arguments.put(p.getName(), p.getType().getSimpleName());
                    }
                }

                String code = getAnnotationValue(a, "code", null);
                String message = getAnnotationValue(a, "message", null);

                log.info(String.format("method: %s, arguments: %s, id: %s, code: %s, message: %s",
                        methodName, arguments, id, code, message));

                QeexWebExceptionMethod qeexWebExceptionMethod = new QeexWebExceptionMethod();
                qeexWebExceptionMethod.methodName = methodName;
                qeexWebExceptionMethod.id = Integer.parseInt(id);
                if (code != null) {
                    qeexWebExceptionMethod.code = Integer.parseInt(code);
                } else {
                    qeexWebExceptionMethod.code = 500;
                }
                qeexWebExceptionMethod.message = message;
                qeexWebExceptionMethod.arguments = arguments;

                String methodImp = getMethodAsString(qeexWebExceptionMethod, a);
                methodImplementations.add(methodImp);
            }
            String className = path.getFileName().toString().split("\\.")[0];
            className = className + "Impl";
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("className", className);
            data.put("interfaceName", javaInterfaceSource.getName());
            data.put("packageName", javaInterfaceSource.getPackage());
            data.put("creationDate", LocalDateTime.now().toString());
            data.put("methods", methodImplementations);
            data.put("groupId", parameters.groupId);
            data.put("project", project);
            data.put("language", language);
            FileUtils.deleteJavaClassFromTemplate(path.getParent().toFile(), "ExceptionBundleImpl", className);
            FileUtils.createJavaClassFromTemplate(path.getParent().toFile(), "qeex-bundle", "ExceptionBundleImpl",
                    className, data, log);
        }
    }

    public static void readQexFromApplicationProperties(ModelQuex modelQuex) throws Exception {
        List<String> lines = Files.readAllLines(Path.of("src/main/resources/application.properties"), StandardCharsets.UTF_8);
//
//# global configuration
//        qeex.project=FLW
//        qeex.default.id=100
//        qeex.default.code=400
//        qeex.default.message=default message for FLW project
//#override of 101 execption configuration
//        qeex.messages[0].id=101
//        qeex.messages[0].code=400
//        qeex.messages[0].message=non va ancora 101
//#override of 102 execption configuration
//        qeex.messages[1].id=102
//        qeex.messages[1].code=500
//        qeex.messages[1].message=non va ancora 102

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("qeex.project")) {
                modelQuex.project = line.split("=")[1].trim();
                continue;
            }
            if (line.startsWith("qeex.default.id")) {
                var default_id = line.split("=")[1].trim();
                modelQuex.defaultId = Integer.parseInt(default_id);
                continue;
            }
            if (line.startsWith("qeex.default.code")) {
                var default_code = line.split("=")[1].trim();
                modelQuex.defaultCode = Integer.parseInt(default_code);
                continue;
            }
            if (line.startsWith("qeex.messages")) {
                Integer idx = Integer.parseInt(line.substring(line.indexOf("[") + 1, line.indexOf("]")));
                String field = line.substring(line.lastIndexOf("]") + 2, line.indexOf("=")).trim();
                String value = line.substring(line.indexOf("=") + 1).trim();
                modelQuex.log.info(field + " : " + value);
                Map<String, String> msgMap = modelQuex.messages.getOrDefault(idx, new LinkedHashMap<>());
                msgMap.put(field, value);
                modelQuex.messages.put(idx, msgMap);
                continue;
            }
        }
        int msg_idx = 101;
        for (Map<String, String> msgMap : modelQuex.messages.values()) {
            int id = msg_idx;

            if (msgMap.containsKey("id")) {
                id = Integer.parseInt(msgMap.get("id"));
            } else {
                msgMap.put("id", String.valueOf(id));
                msg_idx++;
            }

            var qeexWebExceptionMethod = modelQuex.qeexWebExceptionMethods.getOrDefault(id, new QeexWebExceptionMethod(id));
            qeexWebExceptionMethod.code = modelQuex.defaultCode;
            qeexWebExceptionMethod.msgMap = msgMap;
            if (msgMap.containsKey("code")) {
                qeexWebExceptionMethod.code = Integer.parseInt(msgMap.get("code"));
            }
            if (msgMap.containsKey("message")) {
                qeexWebExceptionMethod.message = msgMap.get("message");
            }
            // we need to ovveride????
            modelQuex.qeexWebExceptionMethods.put(id, qeexWebExceptionMethod);
        }

        modelQuex.log.info("project : " + modelQuex.project);
        modelQuex.log.info("defaultId : " + modelQuex.defaultId);
        modelQuex.log.info("defaultCode : " + modelQuex.defaultCode);
        modelQuex.log.info("messages : " + modelQuex.qeexWebExceptionMethods);
    }

    protected static String getAnnotationValue(final AnnotationSource<JavaInterfaceSource> a, final String fieldName, final String defaultValue) {
        if (a == null || fieldName == null) {
            return defaultValue;
        }
        String value = a.getLiteralValue(fieldName);
        if (null == value) {
            value = defaultValue;
        } else {
            value = StringUtil.removeQuotes(value);
        }
        return value;
    }

    protected static String getMethodAsString(QeexWebExceptionMethod qeexWebExceptionMethod, AnnotationSource<JavaInterfaceSource> a) {
        String methodName = qeexWebExceptionMethod.methodName;
        String anno = a.toString();
        int code = qeexWebExceptionMethod.code;
        int id = qeexWebExceptionMethod.id;
        String message = qeexWebExceptionMethod.message;
        String arguments = "";
        Map<String, String> argumentsMap = qeexWebExceptionMethod.arguments;
        if (argumentsMap != null && !argumentsMap.isEmpty()) {
            message = "String.format(\"" + message + "\"," + String.join(",", argumentsMap.keySet()) + ")";
            List<String> argList = new ArrayList<>();
            for (String field : argumentsMap.keySet()) {
                argList.add(argumentsMap.get(field) + " " + field);
            }
            arguments = String.join(", ", argList);
        } else {
            message = "\"" + message + "\"";
        }

        String methodFormat =
                "public QeexWebException %s(%s) {\n" +
                        "            // from annotation  %s\n" +
                        "            //this id IS AUTOMATICALLY GENERATED by method position inside interface\n" +
                        "            int _id = %d;\n" +
                        "            Integer code = %d;\n" +
                        "            String message = %s;\n" +
                        "            String language = null;\n" +
                        "            return QeexWebException.builder(qeexConfig.get_project(classProjectName))\n" +
                        "                .code(_id)\n" +
                        "                .code(qeexConfig.get_code(_id, code))\n" +
                        "                .message(qeexConfig.get_message(_id, message, languageInterceptor.getLanguage()))\n" +
                        "                .language(qeexConfig.get_language(_id, languageInterceptor.getLanguage()));\n" +
                        "        }\n";
        return String.format(methodFormat, methodName, arguments, anno, id, code, message);
    }

}
