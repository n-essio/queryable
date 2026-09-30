package it.ness.queryable.util;

import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.SystemStreamLog;
import it.ness.queryable.model.api.Parameters;
import it.ness.queryable.templates.FreeMarkerTemplates;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MojoUtilsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void detectsOnlyQuarkusOidcApplicationDependency() {
        Dependency dependency = new Dependency();
        dependency.setGroupId("io.quarkus");
        dependency.setArtifactId("quarkus-oidc");
        assertTrue(MojoUtils.hasOidcDependency(List.of(dependency)));
        dependency.setScope("test");
        assertFalse(MojoUtils.hasOidcDependency(List.of(dependency)));
        dependency.setScope("compile");
        dependency.setGroupId("other");
        assertFalse(MojoUtils.hasOidcDependency(List.of(dependency)));
        dependency.setGroupId("io.quarkus");
        dependency.setArtifactId("quarkus-oidc-client");
        assertFalse(MojoUtils.hasOidcDependency(List.of(dependency)));
        assertFalse(MojoUtils.hasOidcDependency(List.of()));
    }

    @Test
    public void enablesOidcIdentityInExistingApiIdempotently() throws Exception {
        Parameters parameters = createApi();
        File apiFile = apiFile(parameters);
        MojoUtils.updateV3SecurityIdentity(parameters, true);
        JavaClassSource api = Roaster.parse(JavaClassSource.class, apiFile);

        assertTrue(api.hasImport("io.quarkus.security.identity.SecurityIdentity"));
        assertTrue(api.hasImport("jakarta.inject.Inject"));
        assertTrue(api.getField("securityIdentity").hasAnnotation("Inject"));
        assertEquals("SecurityIdentity", api.getField("securityIdentity").getType().getName());
        assertEquals("return securityIdentity;", api.getMethod("getCurrentUser").getBody().trim());
        String username = api.getMethod("getCurrentUsername").getBody();
        assertTrue(username.contains("securityIdentity != null"));
        assertTrue(username.contains("securityIdentity.getPrincipal() != null"));
        assertTrue(username.contains("securityIdentity.getPrincipal().getName()"));
        assertTrue(username.contains("\"system\""));

        String firstUpdate = Files.readString(apiFile.toPath());
        MojoUtils.updateV3SecurityIdentity(parameters, true);
        assertEquals(firstUpdate, Files.readString(apiFile.toPath()));
        api = Roaster.parse(JavaClassSource.class, apiFile);
        assertEquals(1, api.getImports().stream().filter(importSource ->
                "io.quarkus.security.identity.SecurityIdentity".equals(importSource.getQualifiedName())).count());
        assertEquals(1, api.getImports().stream().filter(importSource ->
                "jakarta.inject.Inject".equals(importSource.getQualifiedName())).count());
    }

    @Test
    public void addsMissingIdentityAndInjectImportsWithoutChangingOtherImports() throws Exception {
        Parameters parameters = createApi();
        File apiFile = apiFile(parameters);
        JavaClassSource api = Roaster.parse(JavaClassSource.class, apiFile);
        api.removeImport("io.quarkus.security.identity.SecurityIdentity");
        api.removeImport("jakarta.inject.Inject");
        Files.writeString(apiFile.toPath(), api.toString());

        MojoUtils.updateV3SecurityIdentity(parameters, true);

        api = Roaster.parse(JavaClassSource.class, apiFile);
        assertTrue(api.hasImport("io.quarkus.security.identity.SecurityIdentity"));
        assertTrue(api.hasImport("jakarta.inject.Inject"));
        assertTrue(api.hasImport("jakarta.persistence.EntityManager"));
        assertTrue(api.hasImport("io.quarkus.panache.common.Sort"));
        assertTrue(api.getField("securityIdentity").hasAnnotation("Inject"));
    }

    @Test
    public void leavesApiUntouchedWithoutOidc() throws Exception {
        Parameters parameters = createApi();
        String original = Files.readString(apiFile(parameters).toPath());

        MojoUtils.updateV3SecurityIdentity(parameters, false);

        assertEquals(original, Files.readString(apiFile(parameters).toPath()));
    }

    @Test
    public void preservesCustomIdentityMethodsAndUnrelatedCode() throws Exception {
        Parameters parameters = createApi();
        File apiFile = apiFile(parameters);
        JavaClassSource api = Roaster.parse(JavaClassSource.class, apiFile);
        api.getMethod("getCurrentUser").setBody("return customIdentity();");
        api.addMethod("protected String getCurrentUsername() { return \"custom\"; }");
        String getList = api.getMethod("getList", "Integer", "Integer", "String", "UriInfo").getBody();
        Files.writeString(apiFile.toPath(), api.toString());

        MojoUtils.updateV3SecurityIdentity(parameters, true);
        api = Roaster.parse(JavaClassSource.class, apiFile);

        assertEquals("return customIdentity();", api.getMethod("getCurrentUser").getBody().trim());
        assertEquals("return \"custom\";", api.getMethod("getCurrentUsername").getBody().trim());
        assertEquals(getList, api.getMethod("getList", "Integer", "Integer", "String", "UriInfo").getBody());
    }

    @Test
    public void respectsExcludedCurrentUserMethod() throws Exception {
        Parameters parameters = createApi();
        File apiFile = apiFile(parameters);
        JavaClassSource api = Roaster.parse(JavaClassSource.class, apiFile);
        api.getMethod("getCurrentUser").addAnnotation("it.ness.queryable.annotations.QExclude");
        Files.writeString(apiFile.toPath(), api.toString());

        MojoUtils.updateV3SecurityIdentity(parameters, true);

        api = Roaster.parse(JavaClassSource.class, apiFile);
        assertEquals("return null;", api.getMethod("getCurrentUser").getBody().trim());
    }

    @Test
    public void doesNotCreateMissingApiWhenEnablingOidc() throws Exception {
        Parameters parameters = createApi();
        Files.delete(apiFile(parameters).toPath());

        MojoUtils.updateV3SecurityIdentity(parameters, true);

        assertFalse(apiFile(parameters).exists());
    }

    private Parameters createApi() throws Exception {
        Parameters parameters = new Parameters(new SystemStreamLog(), "example", "application", false,
                "model", "service/rs", temporaryFolder.newFolder().getAbsolutePath(), false, true, true, null);
        File apiFile = apiFile(parameters);
        Files.createDirectories(apiFile.toPath().getParent());
        Files.writeString(apiFile.toPath(), FreeMarkerTemplates.processTemplate("v3", "RsRepositoryServiceV3",
                Map.of("groupId", parameters.groupId)));
        return parameters;
    }

    private File apiFile(Parameters parameters) {
        return new File(parameters.outputDir, parameters.apiPath + "service/RsRepositoryServiceV3.java");
    }

    @Test
    public void addsCurrentQueryableVersionToDependencyAndPlugin() {
        Model model = new Model();
        model.setBuild(new org.apache.maven.model.Build());

        MojoUtils.addQueryableDependency(model);
        MojoUtils.addQueryablePlugin(model);

        assertEquals("it.n-ess.queryable", model.getDependencies().get(0).getGroupId());
        assertEquals("queryable-maven-plugin", model.getDependencies().get(0).getArtifactId());
        assertEquals("3.0.9", model.getDependencies().get(0).getVersion());
        assertEquals("it.n-ess.queryable", model.getBuild().getPlugins().get(0).getGroupId());
        assertEquals("queryable-maven-plugin", model.getBuild().getPlugins().get(0).getArtifactId());
        assertEquals("3.0.9", model.getBuild().getPlugins().get(0).getVersion());
    }
}