package it.ness.queryable.builder;

import it.ness.queryable.model.test.QT;
import it.ness.queryable.model.pojo.Parameters;
import it.ness.queryable.model.pojo.TestDataPojo;
import it.ness.queryable.templates.FreeMarkerTemplates;
import it.ness.queryable.util.FileUtils;
import it.ness.queryable.util.ModelFilesV3;
import it.ness.queryable.util.StringUtil;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TestBuilder {

    protected static String ANNOTATION_FIELD = QT.class.getSimpleName();
    protected static String ANNOTATION_ID = "Id";

    public static void generateSources(ModelFilesV3 mf, Log log, Parameters parameters,
                                       String packageName) throws Exception {
        String[] modelFiles = mf.getModelFileNames();
        for (String modelFileName : modelFiles) {
            int order = 1;
            StringBuilder sb = new StringBuilder();
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
            String allMethods = sb.toString();

            Map<String, Object> map = new HashMap<>();

            map.put("packageName", packageName);
            List<String> created = getCreated(log, modelFileName, mf);
            map.put("createdItems", created);
            map.put("allMethods", allMethods);
            String testClassName = className + "ServiceRsTest";
            map.put("testClassName", testClassName);

            File testfile = new File(parameters.testPath + "/");
            FileUtils.deleteJavaClassFromTemplate(testfile, "TestServiceRs", testClassName);
            FileUtils.createJavaClassFromTemplate(testfile, "TestServiceRs", testClassName, map, log);
        }
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
        List<FieldSource<JavaClassSource>> defaultFields = new ArrayList<>();
        for (FieldSource<JavaClassSource> field : fieldsList) {
            List<AnnotationSource<JavaClassSource>> list = field.getAnnotations();
            boolean addDefaults = true;
            for (AnnotationSource<JavaClassSource> anno : list) {
                if (anno.getName().equals(ANNOTATION_FIELD)) {
                    QT tField = new QT();
                    tField.defaultValue = anno.getStringValue("defaultValue");
                    tField.updatedValue = anno.getStringValue("updatedValue");
                    tField.field = field;
                    tField.annotations = list;
                    tField.isId = false;
                    testDataPojo.tFieldList.add(tField);
                    addDefaults = false;
                }
                if (anno.getName().equals(ANNOTATION_ID)) {
                    QT tField = new QT();
                    tField.defaultValue = null;
                    tField.updatedValue = null;
                    tField.field = field;
                    tField.annotations = list;
                    tField.isId = true;
                    testDataPojo.tFieldList.add(tField);
                    addDefaults = false;
                }
            }
            if (addDefaults) {
                defaultFields.add(field);
            }
        }

        for (FieldSource<JavaClassSource> field : defaultFields) {
            List<AnnotationSource<JavaClassSource>> list = field.getAnnotations();
            QT tField = getDefaultsField(log, field, list);
            if (tField != null) {
                testDataPojo.tFieldList.add(tField);
            }
        }

        return testDataPojo;
    }

    public static String printValues(TestDataPojo testDataPojo, String className, int order) {

        List<QT> tFieldList = testDataPojo.tFieldList;

        if (tFieldList.size() > 0) {
            Map<String, Object> map = new HashMap<>();
            String classInstance = className.toLowerCase();

            map.put("className", className);
            map.put("classInstance", classInstance);
            map.put("rsPath", testDataPojo.rsPath);

            map.put("insertItems", getInsertFields(tFieldList, className, classInstance));
            map.put("putItems", getPutFields(tFieldList, className, classInstance));

            map.put("createdInstance", "created" + className);
            map.put("updatedInstance", "updated" + className);

            map.put("addMethod", "shouldAdd" + className + "Item");
            map.put("putMethod", "shouldPut" + className + "Item");
            map.put("deleteMethod", "shouldDelete" + className + "Item");

            map.put("addMethodOrder", order);
            map.put("putMethodOrder", order+1);
            map.put("deleteMethodOrder", order+2);

            for (QT tField : tFieldList) {
                if (tField.isId) {
                    map.put("id", tField.field.getName());
                }
            }

            String serviceRsClass = FreeMarkerTemplates.processTemplate("TestShouldAdd", map);
            return serviceRsClass;
        }
        return null;
    }

    public static List<String> getCreated(Log log, String modelFileName, ModelFilesV3 mf) {
        List<String> statements = new ArrayList<>();
        String className = StringUtil.getClassNameFromFileName(modelFileName);
        if (!mf.excludeClass(className)) {
            try {
                statements.add(String.format("public static %s %s;", className, "created" + className));
            } catch (Exception e) {
                log.error(e);
            }
        }
        return statements;
    }

    public static List<String> getInsertFields(List<QT> tFieldList, String className, String classInstance) {
        List<String> statements = new ArrayList<>();
        statements.add(String.format("%s %s = new %s();", className, classInstance, className));

        for (QT tField : tFieldList) {
            if (tField.isId) {
                continue;
            }
            if (tField.field.getType().getName().equals("String")) {
                statements.add(String.format("%s.%s = \"%s\";", classInstance,
                        tField.field.getName(),
                        tField.defaultValue));
            }
            if (tField.field.getType().getName().equalsIgnoreCase("int") ||
                    tField.field.getType().getName().equalsIgnoreCase("integer") ||
                    tField.field.getType().getName().equalsIgnoreCase("long") ||
                    tField.field.getType().getName().equalsIgnoreCase("boolean")) {
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
            if (tField.field.getType().getName().equals("BigInteger")) {
                statements.add(String.format("%s.%s = BigInteger.valueOf(%s);", classInstance,
                        tField.field.getName(),
                        tField.defaultValue));
            }
        }
        return statements;
    }

    public static List<String> getPutFields(List<QT> tFieldList, String className, String classInstance) {
        List<String> statements = new ArrayList<>();
        statements.add(String.format("%s %s = created%s;", className, classInstance, className));

        for (QT tField : tFieldList) {
            if (tField.isId) {
                continue;
            }
            if (tField.field.getType().getName().equals("String")) {
                statements.add(String.format("%s.%s = \"%s\";", classInstance,
                        tField.field.getName(),
                        tField.updatedValue));
            }
            if (tField.field.getType().getName().equalsIgnoreCase("int") ||
                    tField.field.getType().getName().equalsIgnoreCase("integer") ||
                    tField.field.getType().getName().equalsIgnoreCase("long") ||
                    tField.field.getType().getName().equalsIgnoreCase("boolean")) {
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
            if (tField.field.getType().getName().equals("BigInteger")) {
                statements.add(String.format("%s.%s = BigInteger.valueOf(%s);", classInstance,
                        tField.field.getName(),
                        tField.defaultValue));
            }
        }
        return statements;
    }

    public static QT getDefaultsField(Log log, FieldSource<JavaClassSource> field,
                                      List<AnnotationSource<JavaClassSource>> list) {
        boolean isId = false;
        for (AnnotationSource<JavaClassSource> anno : list) {
            if (anno.getName().equals(ANNOTATION_ID)) {
                isId = true;
            }
        }

        if (field.getType().getName().equals("String")) {
            QT tField = new QT();
            tField.defaultValue = "defaultValue_" + field.getName();
            tField.updatedValue = "updatedValue_" + field.getName();
            tField.field = field;
            tField.annotations = list;
            tField.isId = isId;
            return tField;
        }
        if (field.getType().getName().equals("int") ||
                field.getType().getName().equals("Integer") ||
                field.getType().getName().equals("long") ||
                field.getType().getName().equals("Long")) {
            QT tField = new QT();
            tField.defaultValue = "0";
            tField.updatedValue = "1";
            tField.field = field;
            tField.annotations = list;
            tField.isId = isId;
            return tField;
        }
        if (field.getType().getName().equals("boolean") ||
                field.getType().getName().equals("Boolean")) {
            QT tField = new QT();
            tField.defaultValue = "false";
            tField.updatedValue = "true";
            tField.field = field;
            tField.annotations = list;
            tField.isId = isId;
            return tField;
        }
        if (field.getType().getName().equals("LocalDateTime")) {
            QT tField = new QT();
            tField.defaultValue = LocalDateTime.now().toString();
            tField.updatedValue = LocalDateTime.now().plusDays(1).toString();
            tField.field = field;
            tField.annotations = list;
            tField.isId = isId;
            return tField;
        }
        if (field.getType().getName().equals("LocalDate")) {
            QT tField = new QT();
            tField.defaultValue = LocalDate.now().toString();
            tField.updatedValue = LocalDate.now().plusDays(1).toString();
            tField.field = field;
            tField.annotations = list;
            tField.isId = isId;
            return tField;
        }
        if (field.getType().getName().equals("BigDecimal")) {
            QT tField = new QT();
            tField.defaultValue = "0";
            tField.updatedValue = "1";
            tField.field = field;
            tField.annotations = list;
            tField.isId = isId;
            return tField;
        }
        if (field.getType().getName().equals("BigInteger")) {
            QT tField = new QT();
            tField.defaultValue = "0";
            tField.updatedValue = "1";
            tField.field = field;
            tField.annotations = list;
            tField.isId = isId;
            return tField;
        }
        if (log != null) log.warn("Unknown field type :" + field.getType().getName());
        return null;
    }
}
