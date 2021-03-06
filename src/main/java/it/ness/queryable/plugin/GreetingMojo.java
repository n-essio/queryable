package it.ness.queryable.plugin;

import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "greeting",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class GreetingMojo extends QuerableBaseMojo {


    public void execute() throws MojoExecutionException {
        init(getLog());
        if (logging)
            log.info(String.format("Begin generating Greating entity in groupId {%s} and artifactId", parameters.groupId, parameters.artifactId));
        MojoUtils.greeting(parameters, log);
        if (logging) log.info("Done generating Greeeting class");
    }

}