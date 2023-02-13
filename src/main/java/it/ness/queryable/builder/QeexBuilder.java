package it.ness.queryable.builder;

import it.ness.queryable.model.pojo.Parameters;
import org.apache.maven.plugin.logging.Log;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class QeexBuilder {

    protected static String ANNOTATION_ID = "Id";

    public static void generateSources(Log log, Parameters parameters, String packageName) throws Exception {
        List<String> lines = Files.readAllLines(Path.of("src/main/resources/application.properties"), StandardCharsets.UTF_8);

        //[INFO] qeex.project=FLW, qeex.default.id=100, qeex.default.code=400, qeex.messages[0].id=101, qeex.messages[0].code=400, qeex.messages[0].message=non va ancora 101, qeex.messages[1].id=102, qeex.messages[1].code=400, qeex.messages[1].message=non va ancora 101
        String project = null;
        String defaultId = null;
        String defaultCode = null;

        Map<Integer, Map<String, String>> messages = new LinkedHashMap<>();

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("qeex.project")) {
                project = line.split("=")[1].trim();
                continue;
            }
            if (line.startsWith("qeex.default.id")) {
                defaultId = line.split("=")[1].trim();
                continue;
            }
            if (line.startsWith("qeex.default.code")) {
                defaultCode = line.split("=")[1].trim();
                continue;
            }
            if (line.startsWith("qeex.messages")) {
                Integer idx = Integer.parseInt(line.substring(line.indexOf("[")+1, line.indexOf("]")));
                String field = line.substring(line.lastIndexOf(".")+1, line.indexOf("=")).trim();
                String value = line.substring(line.indexOf("=")+1).trim();

                Map<String, String> msgMap = messages.getOrDefault(idx, new LinkedHashMap<>());
                msgMap.put(field, value);
                messages.put(idx, msgMap);
                continue;
            }
        }
        log.info("project : " + project);
        log.info("defaultId : " + defaultId);
        log.info("defaultCode : " + defaultCode);
        log.info("messages : " + messages);
    }

}
