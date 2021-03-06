package it.ness.queryable.util;

import it.ness.queryable.templates.FreeMarkerTemplates;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class FileUtils {

    public static File createPath(File dir, String spath, Log log) {
        File path = new File(dir, spath);
        if (!path.exists()) {
            if (log != null) log.info("Creating path: " + spath);
            path.mkdirs();
        }
        return path;
    }

    public static void createJavaClassFromTemplate(File dir, String templateName, String replaceWithName, final Map<String, Object> data, Log log) {
        String apiClass = FreeMarkerTemplates.processTemplate(templateName, data);
        File filePath;
        if (replaceWithName != null) {
            filePath = new File(dir, replaceWithName + ".java");
        } else {
            filePath = new File(dir, templateName + ".java");
        }
        if (!filePath.exists()) {
            try (FileWriter out = new FileWriter(filePath)) {
                if (log != null) log.info("writing file " + filePath.getPath());
                out.append(apiClass);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (log != null) log.info("file: " + filePath.getPath() + " already exists.");
        }
    }
}
