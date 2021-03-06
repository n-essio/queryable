package it.ness.queryable.plugin;

import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "add-api",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class AddApiMojo extends QuerableBaseMojo {

    public void execute() throws MojoExecutionException {
        init(getLog());
        if (logging) log.info(String.format("Begin generating api sources for groupId {%s}", parameters.groupId));
        MojoUtils.addApi(parameters, log);
        if (logging) log.info("Done generating api sources");
    }


}