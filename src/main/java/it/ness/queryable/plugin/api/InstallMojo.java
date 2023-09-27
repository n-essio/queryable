package it.ness.queryable.plugin.api;

import it.ness.queryable.plugin.QuerableBaseMojo;
import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "install",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class InstallMojo extends QuerableBaseMojo {


    public void execute() throws MojoExecutionException {
        init(getLog());
        if (logging) log.info(String.format("Begin install api and greeting"));
        MojoUtils.addApi(parameters, log);
        MojoUtils.greeting(parameters, log);
        if (logging) log.info("Done install api and greeting");
    }


}