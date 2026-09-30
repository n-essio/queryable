package it.ness.queryable.plugin.v3;

import it.ness.queryable.plugin.QuerableBaseMojo;
import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "source",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class SourceV3Mojo extends QuerableBaseMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "classes")
    String classes;

    @Parameter(property = "excludeClasses")
    String excludeClasses;

    public void execute() {
        init(getLog());
        this.parameters.sourceVersion = "v3";
        this.parameters.setClasses(classes);
        this.parameters.setExcludedClasses(excludeClasses);
        MojoUtils.sourceV3(parameters, log, MojoUtils.hasOidcDependency(project.getDependencies()));
    }
}