package it.ness.queryable.templates;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public class FreeMarkerTemplates {

	public static Template getTemplate(final String templateFileName, final String templateName) throws IOException {

		final InputStream inputStream = FreeMarkerTemplates.class.getResourceAsStream(templateFileName);

		final String templateText = new BufferedReader(new InputStreamReader(inputStream)).lines()
				.parallel().collect(Collectors.joining("\n"));

		final Configuration cfg = new Configuration();
		final StringTemplateLoader tloader = new StringTemplateLoader();
		cfg.setTemplateLoader(tloader);
		tloader.putTemplate(templateName, templateText);
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		final Template temp = cfg.getTemplate(templateName);
		return temp;
	}
	
	public static String processTemplate(final String templateName, final Map<String, Object> data){
		try {
			final Template temp = getTemplate(String.format("/templates/%s.ftl", templateName), templateName);
	
			final Writer writer = new StringWriter();
			temp.process(data, writer);
			return writer.toString();
		} catch (Exception e){
			
		}
		return null;
	}

}