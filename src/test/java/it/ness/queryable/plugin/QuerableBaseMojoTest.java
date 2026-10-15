package it.ness.queryable.plugin;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.junit.Assert.*;

public class QuerableBaseMojoTest {
    @Test
    public void exposesOptionWithFalseDefaultInBothMavenGoals() throws Exception {
        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new File("target/classes/META-INF/maven/plugin.xml"));
        var mojos = document.getElementsByTagName("mojo");
        int checked = 0;
        for (int i = 0; i < mojos.getLength(); i++) {
            Element mojo = (Element) mojos.item(i);
            String goal = mojo.getElementsByTagName("goal").item(0).getTextContent();
            if (!goal.equals("source") && !goal.equals("sourcev4")) {
                continue;
            }
            Element option = (Element) mojo.getElementsByTagName("useModelSubPackage").item(0);
            assertNotNull(goal, option);
            assertEquals("false", option.getAttribute("default-value"));
            assertEquals("${useModelSubPackage}", option.getTextContent());
            checked++;
        }
        assertEquals(2, checked);
    }

    @Test
    public void propagatesSubpackageOptionAndDefaultsToFalse() {
        QuerableBaseMojo mojo = new QuerableBaseMojo() {
            @Override
            public void execute() {
            }
        };
        mojo.groupId = "example";
        mojo.artifactId = "application";
        mojo.sourceModelDirectory = "model";
        mojo.sourceRestDirectory = "service/rs";
        mojo.outputDirectory = "target/generated-sources";
        mojo.init(new SystemStreamLog());
        assertFalse(mojo.parameters.useModelSubPackage);
        mojo.useModelSubPackage = true;
        mojo.init(new SystemStreamLog());
        assertTrue(mojo.parameters.useModelSubPackage);
    }
}
