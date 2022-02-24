package it.ness.queryable.builder;

import it.ness.queryable.model.TField;
import it.ness.queryable.model.pojo.Parameters;
import it.ness.queryable.model.pojo.TestDataPojo;
import it.ness.queryable.templates.FreeMarkerTemplates;
import it.ness.queryable.util.ModelFiles;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import java.io.File;
import java.util.*;

public class TestBuilder {

    protected static String ANNOTATION_FIELD = "TField";
    protected static String ANNOTATION_ID = "Id";

    public static void generateSources(ModelFiles mf, Log log, Parameters parameters) throws Exception {
        String[] modelFiles = mf.getModelFileNames();
        for (String modelFileName : modelFiles) {
            String className = StringUtil.getClassNameFromFileName(modelFileName);
            if (!mf.excludeClass(className)) {
                try {
                    TestDataPojo testDataPojo = readModel(log, modelFileName, parameters);
                    printValues(testDataPojo, className);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        if (log != null) log.info("Done generating sources");
    }

    private static TestDataPojo readModel(Log log, String modelFileName, Parameters parameters) throws Exception {
        System.out.println("Creating model for class : " + modelFileName);
        String className = StringUtil.getClassNameFromFileName(modelFileName);
        String path = parameters.modelPath;
        JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(path, modelFileName));

        List<FieldSource<JavaClassSource>> fieldsList = javaClass.getFields();
        TestDataPojo testDataPojo = new TestDataPojo();
        for (AnnotationSource<JavaClassSource> anno : javaClass.getAnnotations()) {
            if (anno.getName().equals("QRs")) {
                testDataPojo.rsPath = anno.getStringValue();
            }
        }

        testDataPojo.tFieldList = new ArrayList<>();

        for (FieldSource<JavaClassSource> field : fieldsList) {
            List<AnnotationSource<JavaClassSource>> list = field.getAnnotations();
            for (AnnotationSource<JavaClassSource> anno : list) {
                if (anno.getName().equals(ANNOTATION_FIELD)) {
                    TField tField = new TField();
                    tField.defaultValue = anno.getStringValue("defaultValue");
                    tField.updatedValue = anno.getStringValue("updatedValue");
                    tField.field = field;
                    testDataPojo.tFieldList.add(tField);
                }
                if (anno.getName().equals(ANNOTATION_ID)) {
                    TField tField = new TField();
                    tField.defaultValue = null;
                    tField.updatedValue = null;
                    tField.field = field;
                    tField.isId = true;
                    testDataPojo.tFieldList.add(tField);
                }
            }
        }
        return testDataPojo;
    }

    public static void printValues(TestDataPojo testDataPojo, String className) {

        String defaultValue = "    private static final %s DEFAULT_%s = \"%s\";";
        String updatedValue = "    private static final %s UPDATED_%s = \"%s\";";

        List<TField> tFieldList = testDataPojo.tFieldList;

        for (TField tfield : tFieldList) {
            String name = tfield.field.getName().toUpperCase(Locale.ROOT);
            String type = tfield.field.getType().toString();
            System.out.println(String.format(defaultValue, type, name, tfield.defaultValue));
            System.out.println(String.format(updatedValue, type, name, tfield.defaultValue));
            System.out.println(tfield);
        }

        if (tFieldList.size() > 0) {
            System.out.println("tFieldList.size() = " + tFieldList.size());
            Map<String, Object> map = new HashMap<>();
            String classInstance = className.toLowerCase();

            map.put("className", className);
            map.put("insert", getInsertFields(tFieldList, className, classInstance));

            String serviceRsClass = FreeMarkerTemplates.processTemplate("TestShouldAdd", map);
            System.out.println("serviceRsClass" + serviceRsClass);
        }
    }

    public static List<String> getInsertFields(List<TField> tFieldList, String className, String classInstance) {
        List<String> statements = new ArrayList<>();
        statements.add(String.format("%s %s = new %s();", className, classInstance, className));
        for (TField tField : tFieldList) {
            if (tField.isId) {
                continue;
            }
            if (tField.field.getType().getName().equals("String")) {
                statements.add(String.format("%s.%s = \"%s\";", classInstance,
                        tField.field.getName(),
                        tField.defaultValue));
            }
            if (tField.field.getType().getName().equals("int")) {
                statements.add(String.format("%s.%s = %s;", classInstance,
                        tField.field.getName(),
                        tField.defaultValue));
            }
            if (tField.field.getType().getName().equals("LocalDateTime")) {
                statements.add(String.format("%s.%s = %s;", classInstance,
                        tField.field.getName(),
                        tField.defaultValue));
            }
        }
        return statements;
    }

}
