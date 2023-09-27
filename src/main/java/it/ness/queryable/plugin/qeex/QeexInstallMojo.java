package it.ness.queryable.plugin.qeex;

import it.ness.queryable.plugin.QuerableBaseMojo;
import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "qeexinstall",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class QeexInstallMojo extends QuerableBaseMojo {


    public void execute() throws MojoExecutionException {
        init(getLog());
        if (logging) log.info(String.format("Begin install qeex api"));
        MojoUtils.addqeexapi(parameters, log);
        MojoUtils.addqeexbundle(parameters, log);
        if (logging) log.info("Done install  qeex api");
    }


}