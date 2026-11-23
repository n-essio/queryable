package it.ness.queryable.builder;

import it.ness.queryable.model.api.Parameters;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpenApiBuilderTest {

    @Test
    public void resolvesConstantFileFromUnixStylePaths() {
        Parameters parameters = parameters();

        assertEquals("src/main/java/example/application/management/AppConstants.java",
                OpenApiBuilder.appConstantsFilePath(parameters,
                        "example.application.management.AppConstants.REPORTS"));
    }

    @Test
    public void resolvesConstantFileFromWindowsStylePaths() {
        Parameters parameters = parameters();
        parameters.serviceRsPath = "src\\main\\java\\example\\application\\service\\rs";
        parameters.sourceRestDirectory = "service\\rs";

        assertEquals("src/main/java/example/application/management/AppConstants.java",
                OpenApiBuilder.appConstantsFilePath(parameters,
                        "example.application.management.AppConstants.REPORTS"));
    }

    @Test
    public void resolvesConstantFileWithCustomRestDirectory() {
        Parameters parameters = new Parameters(new SystemStreamLog(), "example", "application", false,
                "domain/entities", "rest/endpoints", "src/main/java", false, true, true, null);

        assertEquals("src/main/java/example/application/management/AppConstants.java",
                OpenApiBuilder.appConstantsFilePath(parameters,
                        "example.application.management.AppConstants.REPORTS"));
    }

    private Parameters parameters() {
        return new Parameters(new SystemStreamLog(), "example", "application", false,
                "model", "service/rs", "src/main/java", false, true, true, null);
    }
}
