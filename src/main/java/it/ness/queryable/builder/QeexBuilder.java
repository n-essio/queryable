package it.ness.queryable.builder;

import it.ness.queryable.model.quex.QeexWebExceptionMethod;
import it.ness.queryable.util.ModelQuex;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class QeexBuilder {

    public static void findInterfacesAnnotatedWithQeexExceptionBundle(ModelQuex modelQuex) throws Exception {

    }

    public static void generateClassesImplementsExceptionBundle(ModelQuex modelQuex) throws Exception {

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
                String field = line.substring(line.lastIndexOf(".") + 1, line.indexOf("=")).trim();
                String value = line.substring(line.indexOf("=") + 1).trim();
                Map<String, String> msgMap = modelQuex.messages.getOrDefault(idx, new LinkedHashMap<>());
                msgMap.put(field, value);
                modelQuex.messages.put(idx, msgMap);
                continue;
            }
        }
        int msg_idx = 101;
        for (Integer i : modelQuex.messages.keySet()) {
            Map<String, String> msgMap = modelQuex.messages.get(i);
            int id = msg_idx;

            if (msgMap.containsKey("id")) {
                id = Integer.parseInt(msgMap.get("id"));
            } else {
                msgMap.put("id", String.valueOf(id));
                msg_idx++;
            }
            var qeexWebExceptionMethod = modelQuex.qeexWebExceptionMethods.getOrDefault(id, new QeexWebExceptionMethod(id));
            qeexWebExceptionMethod.code = modelQuex.defaultCode;
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

}
