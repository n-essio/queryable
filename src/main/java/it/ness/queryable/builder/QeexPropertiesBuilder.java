package it.ness.queryable.builder;

import it.ness.queryable.model.api.Parameters;
import it.ness.queryable.model.quex.ModelQuex;
import it.ness.queryable.model.quex.QeexWebExceptionMethod;
import it.ness.queryable.util.FileUtils;
import it.ness.queryable.util.StringUtil;
import it.ness.queryable.util.TranslateService;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.DocFlavor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

public class QeexPropertiesBuilder {

    private static final Logger log = LoggerFactory.getLogger(QeexPropertiesBuilder.class);

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

    public static List<QeexWebExceptionMethod> getQeexWebExceptionMethods(Log log) throws Exception {
        List<Path> interfacesList = findInterfacesAnnotatedWithQeexExceptionBundle();
        List<QeexWebExceptionMethod> qeexWebExceptionMethods = new ArrayList<>();
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

                qeexWebExceptionMethods.add(qeexWebExceptionMethod);
            }
        }
        return qeexWebExceptionMethods;
    }

    public static void readAndTranslate(ModelQuex modelQuex, String google_translate_apikey) throws Exception {
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

        List<String> nLines = new ArrayList<>();
        for (int lineno = 0; lineno < lines.size(); lineno++) {
            String line = lines.get(lineno);
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
            nLines.add(line);
        }

        Map<String, Map<String, String>> existingMessages = new LinkedHashMap<>();
        for (Integer idx : modelQuex.messages.keySet()) {
            Map<String, String> msgMap = modelQuex.messages.getOrDefault(idx, new LinkedHashMap<>());
            existingMessages.put(msgMap.get("id"), msgMap);
        }

        modelQuex.log.info("project : " + modelQuex.project);
        modelQuex.log.info("defaultId : " + modelQuex.defaultId);
        modelQuex.log.info("defaultCode : " + modelQuex.defaultCode);
// messages : {101={id=101, code=400, message=non va ancora 101, language.en=is not ok 101, language.fr=c'esta pas ok 101}}

        List<QeexWebExceptionMethod> qeexWebExceptionMethodList = getQeexWebExceptionMethods(modelQuex.log);
        Map<String, Map<String, String>> messages = new LinkedHashMap<>();
        for (QeexWebExceptionMethod method : qeexWebExceptionMethodList) {
            String id = String.valueOf(method.id);
            String message = method.message;
            String code = String.valueOf(method.code);
            Map<String, String> msgMap = existingMessages.getOrDefault(id, new LinkedHashMap<>());
            String existingMessage = msgMap.get("message");
            String message_it = msgMap.get("language.it");
            String message_de = msgMap.get("language.de");

            log.info("id : " + id);
            log.info("code : " + code);
            log.info("message : " + message);
            log.info("message_it : " + message_it);
            log.info("message_de : " + message_de);
            log.info("");

            if (existingMessage == null || message_it == null || message_de == null
                    || !existingMessage.trim().equalsIgnoreCase(message.trim())) {
                message_it = TranslateService.translateText(message, "IT", modelQuex.log, google_translate_apikey);
                message_de = TranslateService.translateText(message, "DE", modelQuex.log, google_translate_apikey);
            }
            Map<String, String> nMsgMap = new LinkedHashMap<>();
            nMsgMap.put("id", id);
            nMsgMap.put("code", code);
            nMsgMap.put("message", message);
            nMsgMap.put("language.en", message);
            nMsgMap.put("language.it", message_it);
            nMsgMap.put("language.de", message_de);
            messages.put(id, nMsgMap);
        }
        int i = 0;
        for (String id : messages.keySet()) {
            Map<String, String> msgMap = messages.get(id);
            String code = msgMap.get("code");
            String message = msgMap.get("message");
            String message_it = msgMap.get("language.it");
            String message_de = msgMap.get("language.de");

            nLines.add(String.format("qeex.messages[%d].id=%s", i, id));
            nLines.add(String.format("qeex.messages[%d].code=%s", i, code));
            nLines.add(String.format("qeex.messages[%d].message=%s", i, message));
            nLines.add(String.format("qeex.messages[%d].language.it=%s", i, message_it));
            nLines.add(String.format("qeex.messages[%d].language.de=%s", i, message_de));
            nLines.add("");
            i++;
        }
        String str = String.join("\n", nLines);  // Substitute "" with your desired separator.
        Files.writeString(Path.of("src/main/resources/application.properties"), str);
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


    private static void appendUsingFileOutputStream(String fileName, String data) {
        OutputStream os = null;
        try {
            // below true flag tells OutputStream to append
            os = new FileOutputStream(new File(fileName), true);
            os.write(data.getBytes(), 0, data.length());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
