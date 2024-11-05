package it.ness.queryable.plugin.v3;

import it.ness.queryable.plugin.QuerableBaseMojo;
import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "build-lang",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class BuildLangV3Mojo extends QuerableBaseMojo {

    public void execute() {
        init(getLog());
        this.parameters.sourceVersion = "v3";
        MojoUtils.langtranslate(parameters, log);
    }
}