package it.ness.queryable.plugin;

import it.ness.queryable.builder.QueryableBuilder;
import it.ness.queryable.util.ModelFiles;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
* Queryable is maven plugin for filter defs.
* @goal source
* @execute lifecycle="queryable" phase="process-sources"
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
     * Location of the file.
     */
    @Parameter( defaultValue = "null", property = "pluralsJsonFile", required = false )
    private File pluralsJsonFile;


    private StringUtil stringUtil;

    public void execute() throws MojoExecutionException
    {
        final String groupId = project.getGroupId();

        Log log = getLog();
        stringUtil = new StringUtil(log, pluralsJsonFile);
        if (!stringUtil.isParsingSuccessful) return;

        log.info(String.format("Begin generating sources for groupId {%s}", groupId));

        ModelFiles mf = new ModelFiles(log, groupId);
        if (!mf.isParsingSuccessful) return;

        QueryableBuilder queryableBuilder = new QueryableBuilder(log, stringUtil);
        try {
            queryableBuilder.generateSources(mf, groupId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        log.info("Done generating sources");
    }
}