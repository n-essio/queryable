package it.ness.queryable.util;

import it.ness.queryable.model.api.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class ServiceRsLocation {
    private final String packageName;
    private final File target;
    private final File legacy;

    public ServiceRsLocation(Parameters parameters, String modelFileName) {
        String relativeName = modelFileName.replace('\\', '/');
        String className = StringUtil.getClassNameFromFileName(relativeName);
        String basePackage = parameters.groupId + "." + parameters.artifactId + "."
                + parameters.sourceRestDirectory.replace('\\', '.').replace('/', '.');
        int lastSlash = relativeName.lastIndexOf('/');
        String suffix = lastSlash >= 0
                ? "." + relativeName.substring(0, lastSlash).replace('/', '.') : "";
        packageName = basePackage + (parameters.useModelSubPackage ? suffix : "");
        target = new File(parameters.outputDirectory,
                packageName.replace('.', '/') + "/" + className + "ServiceRs.java");
        String previousPackage = basePackage + (parameters.useModelSubPackage ? "" : suffix);
        legacy = new File(parameters.outputDirectory,
                previousPackage.replace('.', '/') + "/" + className + "ServiceRs.java");
    }

    public String getPackageName() {
        return packageName;
    }

    public File getTarget() {
        return target;
    }

    public File getExisting() {
        if (!target.equals(legacy) && target.exists() && legacy.exists()) {
            throw new IllegalStateException("Both REST services exist: " + target + " and " + legacy
                    + ". Merge them explicitly before regenerating.");
        }
        return target.exists() ? target : legacy;
    }

    public void removeRelocatedSource() throws IOException {
        if (!target.equals(legacy) && legacy.exists()) {
            Files.delete(legacy.toPath());
        }
    }
}
