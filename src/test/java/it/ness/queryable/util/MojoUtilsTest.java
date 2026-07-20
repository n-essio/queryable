package it.ness.queryable.util;

import org.apache.maven.model.Model;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MojoUtilsTest {

    @Test
    public void addsCurrentQueryableVersionToDependencyAndPlugin() {
        Model model = new Model();
        model.setBuild(new org.apache.maven.model.Build());

        MojoUtils.addQueryableDependency(model);
        MojoUtils.addQueryablePlugin(model);

        assertEquals("it.n-ess.queryable", model.getDependencies().get(0).getGroupId());
        assertEquals("queryable-maven-plugin", model.getDependencies().get(0).getArtifactId());
        assertEquals("3.0.6", model.getDependencies().get(0).getVersion());
        assertEquals("it.n-ess.queryable", model.getBuild().getPlugins().get(0).getGroupId());
        assertEquals("queryable-maven-plugin", model.getBuild().getPlugins().get(0).getArtifactId());
        assertEquals("3.0.6", model.getBuild().getPlugins().get(0).getVersion());
    }
}