package it.ness.queryable.util;

import it.ness.queryable.model.api.Parameters;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ModelFilesV3Test {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void doesNotParseMetadataForExplicitlyExcludedClasses() throws Exception {
        File modelPath = temporaryFolder.newFolder("model");
        Files.writeString(
                new File(modelPath, "Customer.java").toPath(),
                "package example; public class Customer {}"
        );
        Parameters parameters = new Parameters(
                new SystemStreamLog(),
                "example",
                "application",
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
        parameters.modelPath = modelPath.getAbsolutePath();
        parameters.setExcludedClasses("Customer");

        ModelFilesV3 modelFiles = new ModelFilesV3(new SystemStreamLog(), parameters);

        assertTrue(modelFiles.isParsingSuccessful);
        assertArrayEquals(new String[0], modelFiles.getModelFileNames());
        assertNull(modelFiles.getQualifiedClassName("Customer"));
    }
}