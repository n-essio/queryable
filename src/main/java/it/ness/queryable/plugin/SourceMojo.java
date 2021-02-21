package it.ness.queryable.plugin;

import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "source",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class SourceMojo extends QuerableBaseMojo {

    public void execute() {
        init(getLog());
        MojoUtils.source(parameters, log);
    }
}