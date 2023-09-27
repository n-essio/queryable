package it.ness.queryable.util;

import it.ness.queryable.model.api.Parameters;
import it.ness.queryable.model.quex.QeexWebExceptionMethod;
import org.apache.maven.plugin.logging.Log;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.util.*;

public class ModelFilesQeex {

    private String[] exceptionFileNames;
    public boolean isParsingSuccessful;

    private Map<String, List<QeexWebExceptionMethod>> interfacesMap = new LinkedHashMap<>();

    public ModelFilesQeex(Log log, Parameters parameters) {
        isParsingSuccessful = false;

        if (log != null) log.info("path = " + parameters.exceptionPath);
        File f = new File(parameters.exceptionPath);
        if (!f.exists()) {
            log.error(String.format("Path %s doesn't exist.", parameters.exceptionPath));
            return;
        }
        exceptionFileNames = f.list((f1, name) -> name.endsWith(".java"));
        if (exceptionFileNames != null && exceptionFileNames.length > 0) {
            if (log != null) log.info("Total exception classes found : " + exceptionFileNames.length);
            if (log != null) log.info("exception class file names : " + Arrays.toString(exceptionFileNames));
            resolveMethodsWithAnnotations(log, parameters);
        } else {
            log.error("No exception classes found in path :" + parameters.exceptionPath);
        }
    }

    public String[] getExceptionFileNames() {
        return exceptionFileNames;
    }

    private void resolveMethodsWithAnnotations(Log log, Parameters parameters) {
        for (String fileName : exceptionFileNames) {
            String className = StringUtil.getClassNameFromFileName(fileName);
            // override if annotation is present
            try {
                JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, new File(parameters.exceptionPath, fileName));
                AnnotationSource<JavaClassSource> a = javaClass.getAnnotation("QeexExceptionBundle");
                if (null != a) {
                    interfacesMap.put(fileName, new ArrayList<QeexWebExceptionMethod>());
                }
                // TODO FIX
                for (MethodSource<JavaClassSource> methodSource : javaClass.getMethods()) {
                    // we can have something like
                    AnnotationSource<JavaClassSource> qeexM = methodSource.getAnnotation("QeexMessage");
//                    @QeexMessage(id = 103, code = 500, message = "non sovrascitto")
//                    QeexWebException notOverrideException();
                    var qeexWebExceptionMethod = new QeexWebExceptionMethod();
                    qeexWebExceptionMethod.methodName = qeexM.getName();

//                    @QeexMessage(id = 104, code = 500, message = "one: %s - two: %s")
//                    QeexWebException withArguments(String one, String two);
                    interfacesMap.get(fileName).add(qeexWebExceptionMethod);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    @Override
    public String toString() {
        return "ModelQeex{" +
                "exceptionFileNames=" + Arrays.toString(exceptionFileNames) +
                ", isParsingSuccessful=" + isParsingSuccessful +
                ", interfacesMap=" + interfacesMap +
                '}';
    }
}
