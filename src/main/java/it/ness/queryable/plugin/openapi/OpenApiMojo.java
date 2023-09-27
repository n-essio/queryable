package it.ness.queryable.plugin.openapi;

import it.ness.queryable.plugin.QuerableBaseMojo;
import it.ness.queryable.util.MojoUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Queryable is maven plugin for filter defs.
 */
@Mojo(name = "openapi",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true)
public class OpenApiMojo extends QuerableBaseMojo {

    public void execute() {
        init(getLog());
        // example
        // https://github.com/n-essio/kimera_app_api/blob/main/docs/openapi.yaml
        MojoUtils.openapisource(parameters, log);
    }
}