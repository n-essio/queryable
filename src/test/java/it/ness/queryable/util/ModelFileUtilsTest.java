package it.ness.queryable.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ModelFileUtilsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void findsJavaFilesInModelAndNestedPackages() throws Exception {
        File modelPath = temporaryFolder.newFolder("model");
        temporaryFolder.newFile("model/Root.java");
        temporaryFolder.newFolder("model", "eggs");
        temporaryFolder.newFile("model/eggs/Egg.java");
        temporaryFolder.newFolder("model", "eggs", "fresh");
        temporaryFolder.newFile("model/eggs/fresh/FreshEgg.java");
        temporaryFolder.newFile("model/eggs/notes.txt");

        assertArrayEquals(
            new String[]{
                "Root.java",
                "eggs" + File.separator + "Egg.java",
                "eggs" + File.separator + "fresh" + File.separator + "FreshEgg.java"
            },
                ModelFileUtils.findJavaFiles(modelPath)
        );
    }

        @Test
        public void findsJavaFilesWhenModelRootContainsOnlyPackages() throws Exception {
        File modelPath = temporaryFolder.newFolder("nested-only-model");
        temporaryFolder.newFolder("nested-only-model", "eggs");
        temporaryFolder.newFile("nested-only-model/eggs/Egg.java");

        assertArrayEquals(
            new String[]{"eggs" + File.separator + "Egg.java"},
            ModelFileUtils.findJavaFiles(modelPath)
        );
        }

    @Test
    public void extractsClassNameFromNestedModelPath() {
        assertEquals("Egg", StringUtil.getClassNameFromFileName("eggs" + File.separator + "Egg.java"));
    }
}