package it.ness.queryable.util;

import it.ness.queryable.model.api.Parameters;
import it.ness.queryable.builder.QueryableV3Builder;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ModelFilesV3Test {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

        @Test
        public void generatesStudentSearchWithMultipleDefaultSortProperties() throws Exception {
        File modelPath = temporaryFolder.newFolder("student-model");
        File outputPath = temporaryFolder.newFolder("student-output");
        Files.writeString(new File(modelPath, "Student.java").toPath(), """
            package example.application.model;
            import jakarta.persistence.Entity;
            import jakarta.persistence.Id;
            import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
            import it.ness.queryable.annotations.QOrderBy;
            @Entity
            @QOrderBy("surname asc, name asc")
            public class Student extends PanacheEntityBase {
                @Id public String uuid;
                public String name;
                public String surname;
            }
            """);
        SystemStreamLog log = new SystemStreamLog();
        Parameters parameters = new Parameters(log, "example", "application", false,
            "model", "service/rs", outputPath.getAbsolutePath(), false, true, true, null);
        parameters.modelPath = modelPath.getAbsolutePath();
        parameters.serviceRsPath = new File(outputPath, "example/application/service/rs").getAbsolutePath();
        ModelFilesV3 modelFiles = new ModelFilesV3(log, parameters);

        assertTrue(modelFiles.isParsingSuccessful);
        assertEquals("surname asc, name asc", modelFiles.getDefaultOrderBy("Student"));
        QueryableV3Builder.generateSources(modelFiles, log, parameters);
        JavaClassSource service = Roaster.parse(JavaClassSource.class,
            new File(parameters.serviceRsPath, "StudentServiceRs.java"));

        assertEquals("return \"surname asc, name asc\";", service.getMethod("getDefaultOrderBy").getBody().trim());
        assertTrue(service.getMethod("getSearch", String.class).getBody().contains("sort(orderBy)"));
        assertTrue(service.hasImport("example.application.model.Student"));
        }

    @Test
    public void generatesAndRepairsEntityImportFromDeclaredSubpackage() throws Exception {
        File modelPath = temporaryFolder.newFolder("badge-model");
        File nestedModelPath = new File(modelPath, "personale");
        assertTrue(nestedModelPath.mkdirs());
        File outputPath = temporaryFolder.newFolder("badge-output");
        Files.writeString(new File(nestedModelPath, "TipologiaBadge.java").toPath(), """
            package example.application.model.personale;
            import jakarta.persistence.Entity;
            import jakarta.persistence.Id;
            import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
            import it.ness.queryable.annotations.QRs;
            @Entity
            @QRs("BADGE_PATH")
            public class TipologiaBadge extends PanacheEntityBase {
                @Id public String uuid;
            }
            """);
        SystemStreamLog log = new SystemStreamLog();
        Parameters parameters = new Parameters(log, "example", "application", false,
            "model", "service/rs", outputPath.getAbsolutePath(), false, true, true, null);
        parameters.modelPath = modelPath.getAbsolutePath();
        ModelFilesV3 modelFiles = new ModelFilesV3(log, parameters);
        String entityQualifiedName = "example.application.model.personale.TipologiaBadge";
        String wrongEntityQualifiedName = "example.application.model.TipologiaBadge";
        File serviceFile = new File(outputPath, "example/application/service/rs/TipologiaBadgeServiceRs.java");

        assertTrue(modelFiles.isParsingSuccessful);
        assertEquals(entityQualifiedName, modelFiles.getQualifiedClassName("TipologiaBadge"));
        QueryableV3Builder.generateSources(modelFiles, log, parameters);
        JavaClassSource service = Roaster.parse(JavaClassSource.class, serviceFile);

        assertTrue(service.hasImport(entityQualifiedName));
        assertFalse(service.hasImport(wrongEntityQualifiedName));
        assertEquals("example.application.service.rs", service.getPackage());
        assertTrue(Files.readString(serviceFile.toPath())
            .contains("import static example.application.management.AppConstants.BADGE_PATH;"));

        Files.writeString(serviceFile.toPath(), Files.readString(serviceFile.toPath())
            .replace(entityQualifiedName, wrongEntityQualifiedName));
        QueryableV3Builder.generateSources(modelFiles, log, parameters);
        JavaClassSource regeneratedService = Roaster.parse(JavaClassSource.class, serviceFile);

        assertTrue(regeneratedService.hasImport(entityQualifiedName));
        assertFalse(regeneratedService.hasImport(wrongEntityQualifiedName));
    }

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