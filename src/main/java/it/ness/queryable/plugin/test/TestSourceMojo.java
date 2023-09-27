package it.ness.queryable.plugin.test;

import it.ness.queryable.plugin.QuerableBaseMojo;
import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "testsources",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class TestSourceMojo extends QuerableBaseMojo {

    public void execute() {
        init(getLog());
        MojoUtils.testsource(parameters, log);
    }
}