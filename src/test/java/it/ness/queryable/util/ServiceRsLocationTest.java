package it.ness.queryable.util;

import it.ness.queryable.builder.QueryableV3Builder;
import it.ness.queryable.builder.QueryableV4Builder;
import it.ness.queryable.model.api.Parameters;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ServiceRsLocationTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void generatesFlatServicesByDefaultInBothVersions() throws Exception {
        for (boolean v4 : new boolean[]{false, true}) {
            Parameters parameters = parameters("model", "service/rs");
            assertFalse(parameters.useModelSubPackage);
            writeModel(parameters, "finance/monthly/Report.java", "finance.monthly");
            generate(parameters, v4);
            assertService(parameters, "finance/monthly/Report.java", "", v4);
            assertFalse(new File(parameters.outputDirectory,
                    "example/application/service/rs/finance/monthly/ReportServiceRs.java").exists());
        }
    }

    @Test
    public void mirrorsArbitraryAndDeepSubpackagesButKeepsRootEntitiesFlat() throws Exception {
        for (boolean v4 : new boolean[]{false, true}) {
            for (String suffix : new String[]{"", "personale", "finance/monthly"}) {
                Parameters parameters = parameters("model", "service/rs");
                parameters.useModelSubPackage = true;
                String relative = suffix.isEmpty() ? "Report.java" : suffix + "/Report.java";
                writeModel(parameters, relative, suffix.replace('/', '.'));
                generate(parameters, v4);
                assertService(parameters, relative, suffix.replace('/', '.'), v4);
                if (!suffix.isEmpty()) {
                    assertFalse(new File(parameters.outputDirectory,
                            "example/application/service/rs/ReportServiceRs.java").exists());
                }
            }
        }
    }

    @Test
    public void supportsConfiguredRootsAndWindowsRelativeSeparators() throws Exception {
        for (boolean v4 : new boolean[]{false, true}) {
            Parameters parameters = parameters("domain/entities", "web/rest");
            parameters.useModelSubPackage = true;
            writeModel(parameters, "sales/archive/Report.java", "sales.archive");
            generate(parameters, v4);
            assertService(parameters, "sales/archive/Report.java", "sales.archive", v4);
            assertEquals(new ServiceRsLocation(parameters, "sales/archive/Report.java").getTarget(),
                    new ServiceRsLocation(parameters, "sales\\archive\\Report.java").getTarget());
        }
    }

    @Test
    public void relocatesFlatServiceAndPreservesManualMethodsAndExclusions() throws Exception {
        for (boolean v4 : new boolean[]{false, true}) {
            Parameters parameters = parameters("model", "service/rs");
            String relative = "finance/monthly/Report.java";
            writeModel(parameters, relative, "finance.monthly");
            generate(parameters, v4);
            File flat = new ServiceRsLocation(parameters, relative).getTarget();
            JavaClassSource service = Roaster.parse(JavaClassSource.class, flat);
            service.addMethod("public String manual() { return \"keep\"; }");
            service.getMethod("getDefaultOrderBy").addAnnotation("it.ness.queryable.annotations.QExclude");
            service.getMethod("getDefaultOrderBy").setBody("return \"custom asc\";");
            if (!v4) {
                service.addImport("it.ness.queryable.annotations.QPreservedFilter");
                service.addMethod("""
                        @QPreservedFilter("Report.obj.code")
                        private void applyManual(PanacheQuery<Report> search) {
                            search.filter("Report.obj.code", Map.of("manual", "keep"));
                        }
                        """);
            }
            Files.writeString(flat.toPath(), service.toString());
            parameters.useModelSubPackage = true;
            generate(parameters, v4);
            File nested = new ServiceRsLocation(parameters, relative).getTarget();
            JavaClassSource relocated = Roaster.parse(JavaClassSource.class, nested);
            assertFalse(flat.exists());
            assertEquals("example.application.service.rs.finance.monthly", relocated.getPackage());
            assertEquals("return \"keep\";", relocated.getMethod("manual").getBody().trim());
            assertEquals("return \"custom asc\";", relocated.getMethod("getDefaultOrderBy").getBody().trim());
            if (!v4) {
                assertTrue(relocated.getMethod("getSearch", String.class).getBody().contains("applyManual(search)"));
            }
            String first = Files.readString(nested.toPath());
            generate(parameters, v4);
            assertEquals(first, Files.readString(nested.toPath()));
            parameters.useModelSubPackage = false;
            generate(parameters, v4);
            assertTrue(flat.exists());
            assertFalse(nested.exists());
            JavaClassSource flattened = Roaster.parse(JavaClassSource.class, flat);
            assertEquals("example.application.service.rs", flattened.getPackage());
            assertEquals("return \"keep\";", flattened.getMethod("manual").getBody().trim());
        }
    }

    @Test
    public void rejectsConflictingServicesWithoutWritingOrDeletingEitherFile() throws Exception {
        for (boolean v4 : new boolean[]{false, true}) {
            Parameters parameters = parameters("model", "service/rs");
            String relative = "personale/Report.java";
            writeModel(parameters, relative, "personale");
            generate(parameters, v4);
            File flat = new ServiceRsLocation(parameters, relative).getTarget();
            String original = Files.readString(flat.toPath());
            parameters.useModelSubPackage = true;
            File nested = new ServiceRsLocation(parameters, relative).getTarget();
            Files.createDirectories(nested.toPath().getParent());
            Files.writeString(nested.toPath(), original);
            assertThrows(IllegalStateException.class, () -> generate(parameters, v4));
            assertEquals(original, Files.readString(flat.toPath()));
            assertEquals(original, Files.readString(nested.toPath()));
        }
    }

    private Parameters parameters(String modelDirectory, String restDirectory) throws Exception {
        File output = temporaryFolder.newFolder();
        Parameters parameters = new Parameters(new SystemStreamLog(), "example", "application", false,
                modelDirectory, restDirectory, output.getAbsolutePath(), false, true, true, null);
        parameters.modelPath = new File(output, "example/application/" + modelDirectory).getAbsolutePath();
        parameters.serviceRsPath = new File(output, "example/application/" + restDirectory).getAbsolutePath();
        return parameters;
    }

    private void writeModel(Parameters parameters, String relative, String suffix) throws Exception {
        File file = new File(parameters.modelPath, relative);
        Files.createDirectories(file.toPath().getParent());
        String packageName = "example.application." + parameters.sourceModelDirectory.replace('/', '.')
                + (suffix.isEmpty() ? "" : "." + suffix);
        Files.writeString(file.toPath(), """
                package %s;
                import jakarta.persistence.*;
                import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
                import it.ness.queryable.annotations.*;
                import org.hibernate.annotations.*;
                @Entity
                @QRs("REPORTS")
                @QOrderBy("uuid asc")
                @QPreserveFilters("Report.obj.code")
                @FilterDef(name = "Report.obj.code", parameters = @ParamDef(name = "manual", type = String.class))
                @Filter(name = "Report.obj.code", condition = "code = :manual")
                public class Report extends PanacheEntityBase {
                    @Id public String uuid;
                    @Q public String code;
                    @Q public String title;
                }
                """.formatted(packageName));
    }

    private void assertService(Parameters parameters, String relative, String suffix, boolean v4) throws Exception {
        ServiceRsLocation location = new ServiceRsLocation(parameters, relative);
        assertTrue(location.getTarget().isFile());
        JavaClassSource service = Roaster.parse(JavaClassSource.class, location.getTarget());
        assertEquals("example.application." + parameters.sourceRestDirectory.replace('/', '.')
                + (suffix.isEmpty() ? "" : "." + suffix), service.getPackage());
        String modelSuffix = new File(relative).getParent();
        String entity = "example.application." + parameters.sourceModelDirectory.replace('/', '.')
                + (modelSuffix == null ? "" : "." + modelSuffix.replace('/', '.')) + ".Report";
        assertTrue(service.hasImport(entity));
        assertTrue(Files.readString(location.getTarget().toPath())
                .contains("import static example.application.management.AppConstants.REPORTS;"));
        assertNotNull(service.getMethod("getDefaultOrderBy"));
        assertTrue(service.getMethods().stream().anyMatch(m -> m.getName().equals(v4 ? "query" : "getSearch")));
        assertTrue(service.getSyntaxErrors().toString(), service.getSyntaxErrors().isEmpty());
    }

    private void generate(Parameters parameters, boolean v4) throws Exception {
        SystemStreamLog log = new SystemStreamLog();
        if (v4) {
            ModelFilesV4 files = new ModelFilesV4(log, parameters);
            assertTrue(files.isParsingSuccessful);
            QueryableV4Builder.generateSources(files, log, parameters);
        } else {
            ModelFilesV3 files = new ModelFilesV3(log, parameters);
            assertTrue(files.isParsingSuccessful);
            QueryableV3Builder.generateSources(files, log, parameters);
        }
    }
}
