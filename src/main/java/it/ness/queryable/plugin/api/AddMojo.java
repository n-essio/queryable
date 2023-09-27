package it.ness.queryable.plugin.api;

import it.ness.queryable.plugin.QuerableBaseMojo;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import static it.ness.queryable.util.MojoUtils.*;

@Mojo(name = "add", requiresProject = true, requiresDirectInvocation = true, aggregator = true, threadSafe = true)
public class AddMojo extends QuerableBaseMojo {

    @Parameter(property = "project", readonly = false)
    private MavenProject project;


    public void execute() throws MojoExecutionException {
        getLog().info("adding to pom queryable: " + project.getFile().getAbsolutePath());
        try {
            Model model = parsePomXmlFileToMavenPomModel(project.getFile().getAbsolutePath());
            addQueryableDependency(model);
            addQueryablePlugin(model);
            parseMavenPomModelToXmlString(project.getFile().getAbsolutePath(), model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
