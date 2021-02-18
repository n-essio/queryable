package it.ness.queryable.util;

import org.apache.maven.plugin.logging.Log;
import org.atteo.evo.inflector.English;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class StringUtil {

    protected Log log;

    public StringUtil() {

    }

    public static String removeQuotes(final String str) {
        return str.replaceAll("\"", "");
    }


    public static String getClassNameFromFileName(final String modelFileName) {
        return modelFileName.substring(0, modelFileName.indexOf(".java"));
    }

    public static String getPlural(String str) {
        return English.plural(str);
    }
}
