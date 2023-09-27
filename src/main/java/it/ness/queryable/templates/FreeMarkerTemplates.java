package it.ness.queryable.templates;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public class FreeMarkerTemplates {

    public static Template getTemplate(String templateFileName, String templateName) throws IOException {

        var inputStream = FreeMarkerTemplates.class.getResourceAsStream(templateFileName);
        var templateText = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .parallel().collect(Collectors.joining("\n"));
        var cfg = new Configuration(Configuration.VERSION_2_3_30);
        var tloader = new StringTemplateLoader();
        cfg.setTemplateLoader(tloader);
        tloader.putTemplate(templateName, templateText);
        cfg.setObjectWrapper(new DefaultObjectWrapper(Configuration.VERSION_2_3_30));
        return cfg.getTemplate(templateName);
    }

    public static String processTemplate(String templateName, Map<String, Object> data) {
        return processTemplate(null, templateName, data);
    }

    public static String processTemplate(String templateFolder, String templateName, Map<String, Object> data) {
        try {
            Template temp;
            if (templateFolder != null && !templateFolder.isEmpty()) {
                temp = getTemplate(String.format("/templates/%s/%s.ftl", templateFolder, templateName), templateName);
            } else {
                temp = getTemplate(String.format("/templates/%s.ftl", templateName), templateName);
            }
            var writer = new StringWriter();
            temp.process(data, writer);
            return writer.toString();
        } catch (Exception e) {

        }
        return null;
    }

}