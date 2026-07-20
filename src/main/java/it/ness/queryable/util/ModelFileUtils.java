package it.ness.queryable.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ModelFileUtils {

    private ModelFileUtils() {
    }

    static String[] findJavaFiles(File modelPath) {
        List<String> modelFileNames = new ArrayList<>();
        collectJavaFiles(modelPath, modelPath, modelFileNames);
        Collections.sort(modelFileNames);
        return modelFileNames.toArray(new String[0]);
    }

    private static void collectJavaFiles(File modelPath, File directory, List<String> modelFileNames) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectJavaFiles(modelPath, file, modelFileNames);
            } else if (file.getName().endsWith(".java")) {
                modelFileNames.add(modelPath.toPath().relativize(file.toPath()).toString());
            }
        }
    }
}