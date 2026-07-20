package it.ness.queryable.model.api;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParametersTest {

    @Test
    public void removesSymbolsFromGeneratedJavaPackagesAndPaths() {
        Parameters parameters = new Parameters(
                new SystemStreamLog(),
                "it.n-ess.queryable",
                "queryable-maven_plugin",
                false,
                "model",
                "service/rs",
                "target/generated-sources",
                "service/exception",
                false,
                true,
                true,
                null
        );

        assertEquals("it.ness.queryable", parameters.groupId);
        assertEquals("queryablemavenplugin", parameters.artifactId);
        assertEquals("it/ness/queryable/api/", parameters.apiPath);
        assertEquals("it/ness/queryable/queryablemavenplugin/", parameters.projectPath);
        assertEquals(
                "src/main/java/it/ness/queryable/queryablemavenplugin/model",
                parameters.modelPath
        );
    }
}