package it.ness.queryable.plugin;

import it.ness.queryable.templates.FreeMarkerTemplates;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static it.ness.queryable.builder.Constants.JAVA_FOLDER;

/**
 * Queryable is maven plugin for filter defs.
 *
 */
@Mojo(name = "add-api",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class AddApiMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "logging", defaultValue = "true")
    boolean logging;

    @Parameter(property = "outputDirectory", defaultValue = JAVA_FOLDER)
    String outputDirectory;

    public void execute() throws MojoExecutionException
    {
        final String groupId = project.getGroupId();

        Log log = getLog();

        if (logging) log.info(String.format("Begin generating api sources for groupId {%s}", groupId));
        File outputDir = new File(outputDirectory);

        String path = groupId.replaceAll("\\.", "/");
        path += String.format("/%s/", "api");

        File apiPath = createPath(outputDir, path);
        File exceptionPath = createPath(apiPath, "/exception/");
        File filterPath = createPath(apiPath, "/filter/");
        File managementPath = createPath(apiPath, "/management/");
        File servicePath = createPath(apiPath, "/service/");
        File utilPath = createPath(apiPath, "/util/");

        final Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);

        copyTemplate(exceptionPath, "AccessDeniedException", data);
        copyTemplate(filterPath, "CorsFilter", data);
        copyTemplate(managementPath, "AppConstants", data);
        copyTemplate(servicePath, "RsRepositoryServiceV3", data);
        copyTemplate(servicePath, "RsResponseService", data);
        copyTemplate(utilPath, "Base64", data);
        copyTemplate(utilPath, "DateUtils", data);
        copyTemplate(utilPath, "FileUtils", data);
        copyTemplate(utilPath, "HttpUtils", data);

        if (logging) log.info("Done generating api sources");
    }


    private File createPath(File dir, final String spath) {
        Log log = getLog();

        File path = new File(dir, spath);
        if (!path.exists()) {
            if (logging) log.info("Creating path: " + spath);
            path.mkdirs();
        }
        return path;
    }

    private void copyTemplate(File dir, final String file, final Map<String, Object> data) {
        Log log = getLog();

        String apiClass = FreeMarkerTemplates.processTemplate(file, data);

        File filePath = new File(dir, file + ".java");
        if (!filePath.exists()) {
            try (FileWriter out = new FileWriter(filePath)) {
                if (logging) log.info("writing file " + filePath.getPath());
                out.append(apiClass);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            if (logging) log.info("file: " + filePath.getPath() + " already exists.");
        }
    }

}