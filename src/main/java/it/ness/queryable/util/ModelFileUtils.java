package it.ness.queryable.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

final class ModelFileUtils {

    private ModelFileUtils() {
    }

    static String[] findJavaFiles(File modelPath) {
        List<String> modelFileNames = new ArrayList<>();
        collectJavaFiles(modelPath, modelPath, modelFileNames);
        Collections.sort(modelFileNames);
        return modelFileNames.toArray(new String[0]);
    }

    static String[] filterByClassName(String[] modelFileNames, Set<String> classNames) {
        return filterByClassName(modelFileNames, classNames, Collections.emptySet());
    }

    static String[] filterByClassName(String[] modelFileNames, Set<String> classNames, Set<String> excludedClassNames) {
        List<String> filteredModelFiles = new ArrayList<>();
        for (String fileName : modelFileNames) {
            String className = StringUtil.getClassNameFromFileName(fileName);
            if ((classNames.isEmpty() || classNames.contains(className)) && !excludedClassNames.contains(className)) {
                filteredModelFiles.add(fileName);
            }
        }
        return filteredModelFiles.toArray(new String[0]);
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