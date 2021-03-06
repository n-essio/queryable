package it.ness.queryable.util;

import org.apache.maven.plugin.logging.Log;
import org.atteo.evo.inflector.English;

public class StringUtil {

    protected Log log;

    public StringUtil() {

    }

    public static String removeQuotes(final String str) {
        if (str == null) return null;
        return str.replaceAll("\"", "");
    }


    public static String getClassNameFromFileName(final String modelFileName) {
        return modelFileName.substring(0, modelFileName.indexOf(".java"));
    }

    public static String getPlural(String str) {
        return English.plural(str);
    }
}
