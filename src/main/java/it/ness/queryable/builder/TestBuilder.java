package it.ness.queryable.builder;

import it.ness.queryable.model.TField;
import it.ness.queryable.model.pojo.Parameters;
import it.ness.queryable.model.pojo.TestDataPojo;
import it.ness.queryable.templates.FreeMarkerTemplates;
import it.ness.queryable.util.FileUtils;
import it.ness.queryable.util.ModelFiles;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import java.io.File;
import java.util.*;

import static it.ness.queryable.builder.Constants.TEST_FOLDER;

public class TestBuilder {

    protected static String ANNOTATION_FIELD = "TField";
    protected static String ANNOTATION_ID = "Id";

    public static void generateSources(ModelFiles mf, Log log, Parameters parameters,
                                       String packageName) throws Exception {
        String[] modelFiles = mf.getModelFileNames();
        List<String> created = getCreated(log, modelFiles, mf);
        StringBuilder sb = new StringBuilder();
        int order = 1;
        for (String modelFileName : modelFiles) {
            String className = StringUtil.getClassNameFromFileName(modelFileName);
            if (!mf.excludeClass(className)) {
                try {
                    TestDataPojo testDataPojo = readModel(log, modelFileName, parameters);
                    String methods = printValues(testDataPojo, className, order);
                    if (methods != null) {
                        sb.append(methods);
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
            order += 2;
        }
        String allMethods = sb.toString();

        Map<String, Object> map = new HashMap<>();

        map.put("packageName", packageName);
        map.put("createdItems", created);
        map.put("allMethods", allMethods);

        String serviceRsClass = FreeMarkerTemplates.processTemplate("TestServiceRs", map);
        System.out.println(serviceRsClass);

        FileUtils.createJavaClassFromTemplate(new File(TEST_FOLDER), "TestServiceRs", "SimpleServiceRsTest", map, null);

        if (log != null) log.info("Done generating sources");
    }

    private static TestDataPojo readModel(Log log, String modelFileName, Parameters parameters) throws Exception {
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

    public static String printValues(TestDataPojo testDataPojo, String className, int order) {

        List<TField> tFieldList = testDataPojo.tFieldList;

        if (tFieldList.size() > 0) {
            Map<String, Object> map = new HashMap<>();
            String classInstance = className.toLowerCase();

            map.put("className", className);
            map.put("classInstance", classInstance);
            map.put("rsPath", testDataPojo.rsPath);

            map.put("insertItems", getInsertFields(tFieldList, className, classInstance));
            map.put("putItems", getPutFields(tFieldList, className, classInstance, "34343-4343"));

            map.put("createdInstance", "created" + className);
            map.put("updatedInstance", "updated" + className);

            map.put("addMethod", "shouldAdd" + className + "Item");
            map.put("putMethod", "shouldPut" + className + "Item");

            map.put("addMethodOrder", order);
            map.put("putMethodOrder", order+1);


            for (TField tField : tFieldList) {
                if (tField.isId) {
                    map.put("id", tField.field.getName());
                }
            }

            String serviceRsClass = FreeMarkerTemplates.processTemplate("TestShouldAdd", map);
            return serviceRsClass;
        }
        return null;
    }

    public static List<String> getCreated(Log log, String[] modelFiles, ModelFiles mf) {
        List<String> statements = new ArrayList<>();
        for (String modelFileName : modelFiles) {
            String className = StringUtil.getClassNameFromFileName(modelFileName);
            if (!mf.excludeClass(className)) {
                try {
                    statements.add(String.format("%s %s;", className, "created" + className));
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        return statements;
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
                statements.add(String.format("%s.%s = LocalDateTime.parse(\"%s\");", classInstance,
                        tField.field.getName(),
                        tField.defaultValue));
            }
            if (tField.field.getType().getName().equals("BigDecimal")) {
                statements.add(String.format("%s.%s = BigDecimal.valueOf(%s);", classInstance,
                        tField.field.getName(),
                        tField.defaultValue));
            }
        }
        return statements;
    }

    public static List<String> getPutFields(List<TField> tFieldList, String className, String classInstance,
                                               String uuid) {
        List<String> statements = new ArrayList<>();
        if (uuid != null) {
            statements.add(String.format("%s %s = created%s;", className, classInstance, className));
        }

        for (TField tField : tFieldList) {
            if (tField.isId) {
                continue;
            }
            if (tField.field.getType().getName().equals("String")) {
                statements.add(String.format("%s.%s = \"%s\";", classInstance,
                        tField.field.getName(),
                        tField.updatedValue));
            }
            if (tField.field.getType().getName().equals("int")) {
                statements.add(String.format("%s.%s = %s;", classInstance,
                        tField.field.getName(),
                        tField.updatedValue));
            }
            if (tField.field.getType().getName().equals("LocalDateTime")) {
                statements.add(String.format("%s.%s = LocalDateTime.parse(\"%s\");", classInstance,
                        tField.field.getName(),
                        tField.updatedValue));
            }
            if (tField.field.getType().getName().equals("BigDecimal")) {
                statements.add(String.format("%s.%s = BigDecimal.valueOf(%s);", classInstance,
                        tField.field.getName(),
                        tField.defaultValue));
            }
        }
        return statements;
    }
}
