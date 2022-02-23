package it.ness.queryable.builder;

import it.ness.queryable.model.TField;
import it.ness.queryable.model.pojo.Parameters;
import it.ness.queryable.util.ModelFiles;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import java.io.File;
import java.util.List;

public class TestBuilder {

    protected static String ANNOTATION_NAME = "TField";

    public static void generateSources(ModelFiles mf, Log log, Parameters parameters) throws Exception {
        String[] modelFiles = mf.getModelFileNames();
        for (String modelFileName : modelFiles) {
            String className = StringUtil.getClassNameFromFileName(modelFileName);
            if (!mf.excludeClass(className)) {
                try {
                    readModel(log, modelFileName, parameters);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        if (log != null) log.info("Done generating sources");
    }

    private static void readModel(Log log, String modelFileName, Parameters parameters) throws Exception {
        System.out.println("Creating model for class : " + modelFileName);
        String className = StringUtil.getClassNameFromFileName(modelFileName);
        String path = parameters.modelPath;
        JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(path, modelFileName));

        List<FieldSource<JavaClassSource>> fieldsList = javaClass.getFields();
        for (FieldSource<JavaClassSource> field : fieldsList) {
            List<AnnotationSource<JavaClassSource>> list = field.getAnnotations();
            for (AnnotationSource<JavaClassSource> anno : list) {
                if (anno.getName().equals(ANNOTATION_NAME)) {
                    TField tField = new TField();
                    tField.defaultValue = anno.getStringValue("defaultValue");
                    tField.updatedValue = anno.getStringValue("updatedValue");
                    tField.fieldName = field.getName();
                    System.out.println(tField.toString());
                }
            }
        }
    }
}
