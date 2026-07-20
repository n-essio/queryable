package it.ness.queryable.util;

import org.apache.maven.plugin.logging.Log;
import org.atteo.evo.inflector.English;

import java.io.File;

public class StringUtil {

    protected Log log;

    public StringUtil() {

    }

    public static String removeQuotes(final String str) {
        if (str == null) return null;
        return str.replaceAll("\"", "");
    }


    public static String getClassNameFromFileName(final String modelFileName) {
        String fileName = new File(modelFileName).getName();
        return fileName.substring(0, fileName.indexOf(".java"));
    }

    public static String toJavaGroupId(final String groupId) {
        return groupId.replaceAll("[^A-Za-z0-9.]", "");
    }

    public static String toJavaArtifactId(final String artifactId) {
        return artifactId.replaceAll("[^A-Za-z0-9]", "");
    }

    public static String getPlural(String str) {
        return English.plural(str);
    }
}
