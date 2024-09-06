package it.ness.queryable.plugin.quarkus3conv;

import it.ness.queryable.plugin.QuerableBaseMojo;
import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "quarkus3conv",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class Quarkus3ConvMojo extends QuerableBaseMojo {

    public void execute() {
        init(getLog());
        MojoUtils.quarkus3conv(parameters, log);
    }
}