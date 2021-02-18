package it.ness.queryable.plugin;

import it.ness.queryable.templates.FreeMarkerTemplates;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
* Queryable is maven plugin for filter defs.
 *
*/
public class AddApiMojo extends AbstractMojo
{
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0
     */
    MavenProject project;

    /**
     * @parameter default-value="true"
     */
    boolean logging;

    /**
     * @parameter default-value="src/main/java"
     */
    String outputDirectory;

    public void execute() throws MojoExecutionException
    {
        final String groupId = project.getGroupId();

        Log log = getLog();

        if (logging) log.info(String.format("Begin generating api sources for groupId {%s}", groupId));
        File outputDir = new File(outputDirectory);

        File apiPath = createPath(outputDir, "/it/coopservice/api/");
        File exceptionPath = createPath(apiPath, "/exception/");
        File filterPath = createPath(apiPath, "/filter/");
        File managementPath = createPath(apiPath, "/management/");
        File servicePath = createPath(apiPath, "/service/");
        File utilPath = createPath(apiPath, "/util/");

        copyTemplate(exceptionPath, "AccessDeniedException");
        copyTemplate(filterPath, "CorsFilter");
        copyTemplate(managementPath, "AppConstants");
        copyTemplate(servicePath, "RsRepositoryServiceV3");
        copyTemplate(servicePath, "RsResponseService");
        copyTemplate(utilPath, "Base64");
        copyTemplate(utilPath, "DateUtils");
        copyTemplate(utilPath, "FileUtils");
        copyTemplate(utilPath, "HttpUtils");

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

    private void copyTemplate(File dir, final String file) {
        Log log = getLog();
        final Map<String, Object> data = new HashMap<>();
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