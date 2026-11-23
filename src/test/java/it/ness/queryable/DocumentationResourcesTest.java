package it.ness.queryable;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DocumentationResourcesTest {

    @Test
    public void packagesMachineReadableDocumentationInJarResources() throws IOException {
        for (String resource : new String[]{"README.md", "AGENTS.md", "API.MD", "llms.txt", "llms-full.txt"}) {
            try (InputStream input = getClass().getResourceAsStream("/META-INF/queryable/" + resource)) {
                assertNotNull(resource, input);
                String contents = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(resource, !contents.isBlank());
            }
        }
    }
}
