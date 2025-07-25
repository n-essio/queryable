package it.ness.queryable.builder;

import it.ness.queryable.model.quex.ModelQuex;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Quarkus3ConvBuilder {

    protected static String ANNOTATION_ID = "Id";
    protected static boolean isReplaceIdOperation = false;

    public static void readAndConvert(ModelQuex modelQuex) throws Exception {
        if (isReplaceIdOperation) {
            System.out.println("running id replace");
        }
        List<Path> files = new ArrayList<>();
        Files.walk(Paths.get("src/main/java/")).filter(Files::isRegularFile).forEach(filePath -> {
            if (filePath.toAbsolutePath().toString().endsWith("java")){
                files.add(filePath);
            }
        });

        for (Path filePath : files) {
            String content = Files.readString(filePath);
            FileWriter out = new FileWriter(filePath.toFile());
            try {
                if (isReplaceIdOperation) {
                    out.append(replaceIds(content, filePath));
                } else {
                    out.append(replaceModelTypeAnnotations(content));
                }
            } finally {
                out.flush();
                out.close();
            }
        }
    }

    private static String replaceModelTypeAnnotations(String modelClazz) {
        return modelClazz.replaceAll("import javax", "import jakarta")
                .replaceAll("type = \"string\"", "type = String.class")
                .replaceAll("type = \"LocalDate\"", "type = LocalDate.class")
                .replaceAll("type = \"LocalDateTime\"", "type = LocalDateTime.class")
                .replaceAll("type = \"boolean\"", "type = Boolean.class")
                .replaceAll("type = \"int\"", "type = Integer.class")
                .replaceAll("type = \"long\"", "type = Long.class")
                .replaceAll("type = \"big_decimal\"", "type = BigDecimal.class")
                .replaceAll("type = \"big_integer\"", "type = BigInteger.class");
    }

    private static String replaceIds(String modelClazz, Path filePath) {
        int base = 7000;
        int nbase = 3000;
        for (int i=0; i<999; i++) {
            String id = String.format("%04d", base + i);
            String rid = String.format("%04d", nbase +i);
            modelClazz = modelClazz.replaceAll("msg_"+id, "msg_"+rid);
        }
        if (filePath.getFileName().toAbsolutePath().toString().contains("ExceptionBundle")) {
            for (int i=0; i<999; i++) {
                String id = String.format("%d", base + i);
                String rid = String.format("%d", nbase +i);
                modelClazz = modelClazz.replaceAll("id = "+id, "id = "+rid);
                modelClazz = modelClazz.replaceAll("id="+id, "id="+rid);
            }
        }
        return modelClazz;
    }

}
