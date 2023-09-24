package it.ness.queryable.plugin;

import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "sourcev4",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class SourceV4Mojo extends QuerableBaseMojo {

    public void execute() {
        init(getLog());
        this.parameters.sourceVersion = "v4";
        MojoUtils.sourceV4(parameters, log);
    }
}