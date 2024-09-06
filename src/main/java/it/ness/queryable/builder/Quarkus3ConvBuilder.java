package it.ness.queryable.builder;

import it.ness.queryable.model.quex.ModelQuex;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Quarkus3ConvBuilder {

    protected static String ANNOTATION_ID = "Id";

    public static void readAndConvert(ModelQuex modelQuex) throws Exception {
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
                out.append(replaceModelTypeAnnotations(content));
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


}
