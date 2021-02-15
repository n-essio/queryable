package it.ness.queryable.util;

import org.apache.maven.plugin.logging.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class StringUtil {

    protected Log log;
    protected boolean logging;
    private Map<String, String> pluralMap = null;
    private Set<String> plurals = null;
    public boolean isParsingSuccessful;

    public StringUtil(Log log, boolean logging, File pluralsJsonFile) {
        this.log = log;
        this.logging = logging;
        isParsingSuccessful = false;

        JSONParser jsonParser = new JSONParser();
        InputStream is = StringUtil.class.getClassLoader().getResourceAsStream("plurals.json");
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(
                    new InputStreamReader(is, "UTF-8"));
            pluralMap = new HashMap<>();
            plurals = new HashSet<>();
            if (jsonObject != null) {
                for (Object key : jsonObject.keySet()) {
                    if (key instanceof String) {
                        pluralMap.put((String)key, (String)jsonObject.get(key));
                        plurals.add((String)jsonObject.get(key));
                    }
                }
            }
            if (pluralsJsonFile != null) {
                if (logging) log.info("Parsing json plurals file : " + pluralsJsonFile.getName());
                if (!pluralsJsonFile.exists()) {
                    log.error("Not found json plurals file: " + pluralsJsonFile.getPath());
                    return;
                }
                FileInputStream fis = new FileInputStream(pluralsJsonFile);
                jsonObject = (JSONObject) jsonParser.parse(
                        new InputStreamReader(fis, "UTF-8"));
                if (jsonObject != null) {
                    for (Object key : jsonObject.keySet()) {
                        if (key instanceof String) {
                            pluralMap.put((String)key, (String)jsonObject.get(key));
                            plurals.add((String)jsonObject.get(key));
                        }
                    }
                }
            }
            else {
                if (logging) log.info("No additional json plurals file configured.");
            }
            isParsingSuccessful = true;
        } catch (Exception e) {
            log.error(e.toString());
            return;
        }
    }

    public static String removeQuotes(final String str) {
        return str.replaceAll("\"", "");
    }


    public static String getClassNameFromFileName(final String modelFileName) {
        return modelFileName.substring(0, modelFileName.indexOf(".java"));
    }

    public String getPlural(String str) {
        if (plurals.contains(str)) {
            if (logging) log.warn(str + " is already in plural. Returning the same value.");
            return str;
        }

        if (!pluralMap.containsKey(str)) {
            if (logging) log.warn("plural for " + str + " not found. Adding s to make the plural.");
            return str + "s";
        }
        return pluralMap.get(str);
    }
}
