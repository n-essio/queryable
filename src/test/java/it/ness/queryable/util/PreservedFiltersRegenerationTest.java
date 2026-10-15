package it.ness.queryable.util;

import it.ness.queryable.builder.QueryableV3Builder;
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

public class PreservedFiltersRegenerationTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void regeneratesInPlaceTwiceWithoutChangingManualHooksOrDefinitions() throws Exception {
        Parameters parameters = parameters();
        File model = new File(parameters.modelPath, "Customer.java");
        File service = new File(parameters.serviceRsPath, "CustomerServiceRs.java");
        Files.writeString(model.toPath(), entity("@Q public String name; @Q public String code;"));
        Files.writeString(service.toPath(), service("""
                @QPreservedFilter("Customer.obj.name")
                private void applyManual(PanacheQuery<Customer> search) throws Exception {
                    if (nn("custom.name")) {
                        String value = get("custom.name").trim();
                        search.filter("Customer.obj.name", Map.of("manual", value));
                    }
                }
                """));
        JavaClassSource original = Roaster.parse(JavaClassSource.class, service);
        String manual = original.getMethods().stream().filter(m -> m.getName().equals("applyManual"))
                .findFirst().orElseThrow().toString();
        generate(parameters);
        JavaClassSource generated = Roaster.parse(JavaClassSource.class, service);
        String body = generated.getMethod("getSearch", String.class).getBody();
        assertTrue(body.contains("applyManual(search)"));
        assertTrue(body.contains("Customer.obj.code"));
        assertFalse(body.contains("Customer.obj.name"));
        assertEquals(manual, generated.getMethods().stream().filter(m -> m.getName().equals("applyManual"))
                .findFirst().orElseThrow().toString());
        assertTrue(Files.readString(model.toPath()).contains("lower(name) = :manual"));
        String firstModel = Files.readString(model.toPath());
        String firstService = Files.readString(service.toPath());
        generate(parameters);
        assertEquals(firstModel, Files.readString(model.toPath()));
        assertEquals(firstService, Files.readString(service.toPath()));
    }

    @Test
    public void handlesManualOnlyEntityNewServiceAndWholeMethodExclusion() throws Exception {
        Parameters parameters = parameters();
        File model = new File(parameters.modelPath, "Customer.java");
        File service = new File(parameters.serviceRsPath, "CustomerServiceRs.java");
        Files.writeString(model.toPath(), entity("public String name;"));
        generate(parameters);
        assertTrue(service.exists());
        assertTrue(Files.readString(model.toPath()).contains("lower(name) = :manual"));
        JavaClassSource source = Roaster.parse(JavaClassSource.class, service);
        source.getMethod("getSearch", String.class).addAnnotation("it.ness.queryable.annotations.QExclude");
        source.getMethod("getSearch", String.class).setBody("return Customer.find(\"name = ?1\", \"manual\");");
        Files.writeString(service.toPath(), source.toString());
        String body = source.getMethod("getSearch", String.class).getBody();
        generate(parameters);
        assertEquals(body, Roaster.parse(JavaClassSource.class, service).getMethod("getSearch", String.class).getBody());
    }

    @Test
    public void failsBeforeWritingWhenHookOrDefinitionIsInvalid() throws Exception {
        Parameters parameters = parameters();
        File model = new File(parameters.modelPath, "Customer.java");
        File service = new File(parameters.serviceRsPath, "CustomerServiceRs.java");
        String original = entity("@Q public String name;");
        Files.writeString(model.toPath(), original);
        String invalid = service("@QPreservedFilter(\"typo\") void manual(PanacheQuery<Customer> q) {}");
        Files.writeString(service.toPath(), invalid);
        assertThrows(IllegalArgumentException.class, () -> generate(parameters));
        assertEquals(original, Files.readString(model.toPath()));
        assertEquals(invalid, Files.readString(service.toPath()));
        Files.delete(service.toPath());
        Files.writeString(model.toPath(), original.replace("@Filter(name =", "@NotAFilter(name ="));
        assertThrows(IllegalArgumentException.class, () -> generate(parameters));
        assertFalse(service.exists());
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> MojoUtils.sourceV3(parameters, new SystemStreamLog()));
        assertTrue(failure.getCause() instanceof IllegalArgumentException);
    }

    private Parameters parameters() throws Exception {
        File output = temporaryFolder.newFolder();
        File models = new File(output, "example/application/model");
        File services = new File(output, "example/application/service/rs");
        assertTrue(models.mkdirs());
        assertTrue(services.mkdirs());
        Parameters parameters = new Parameters(new SystemStreamLog(), "example", "application", false,
                "model", "service/rs", output.getAbsolutePath(), false, true, true, null);
        parameters.modelPath = models.getAbsolutePath();
        parameters.serviceRsPath = services.getAbsolutePath();
        return parameters;
    }

    private void generate(Parameters parameters) throws Exception {
        SystemStreamLog log = new SystemStreamLog();
        ModelFilesV3 files = new ModelFilesV3(log, parameters);
        assertTrue(files.isParsingSuccessful);
        QueryableV3Builder.generateSources(files, log, parameters);
    }

    private String entity(String fields) {
        return """
                package example.application.model;
                import jakarta.persistence.*;
                import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
                import it.ness.queryable.annotations.*;
                import org.hibernate.annotations.*;
                @Entity
                @QPreserveFilters("Customer.obj.name")
                @FilterDef(name = "Customer.obj.name", parameters = @ParamDef(name = "manual", type = String.class))
                @Filter(name = "Customer.obj.name", condition = "lower(name) = :manual")
                public class Customer extends PanacheEntityBase {
                    @Id public String uuid;
                """ + fields + "}";
    }

    private String service(String methods) {
        return """
                package example.application.service.rs;
                import example.application.model.Customer;
                import io.quarkus.hibernate.orm.panache.PanacheQuery;
                import it.ness.queryable.annotations.QPreservedFilter;
                import java.util.Map;
                public class CustomerServiceRs {
                    public PanacheQuery<Customer> getSearch(String orderBy) throws Exception { return null; }
                """ + methods + "}";
    }
}
