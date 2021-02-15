package it.ness.queryable.plugin;

import it.ness.queryable.builder.QueryableBuilder;
import it.ness.queryable.util.ModelFiles;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
* Queryable is maven plugin for filter defs.
* @goal source
 * @execute goal="source"
 *
*/
public class QueryableMojo extends AbstractMojo
{
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @since 1.0
     */
    MavenProject project;

    /**
     * @parameter default-value="null"
     */
    File pluralsJsonFile;

    /**
     * @parameter default-value="false"
     */
    boolean removeAnnotations;

    /**
     * @parameter default-value="model"
     */
    String sourceModelDirectory;

    /**
     * @parameter default-value="service/rs"
     */
    String sourceRestDirectory;

    /**
     * @parameter default-value="src/main/java"
     */
    String outputDirectory;

    /**
     * @parameter default-value="true"
     */
    boolean logging;

    /**
     * @parameter default-value="true"
     */
    boolean overideAnnotations;

    /**
     * @parameter default-value="true"
     */
    boolean overideSearchMethod;

    public void execute() throws MojoExecutionException
    {
        final String groupId = project.getGroupId();

        Log log = getLog();
        StringUtil stringUtil = new StringUtil(log, logging, pluralsJsonFile);
        if (!stringUtil.isParsingSuccessful) return;

        if (logging) log.info(String.format("Begin generating sources for groupId {%s}", groupId));

        ModelFiles mf = new ModelFiles(log, logging, stringUtil, groupId, sourceModelDirectory);
        if (!mf.isParsingSuccessful) return;

        QueryableBuilder queryableBuilder = new QueryableBuilder(log, logging, sourceModelDirectory, stringUtil);
        try {
            queryableBuilder.generateSources(mf, groupId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (logging) log.info("Done generating sources");
    }
}