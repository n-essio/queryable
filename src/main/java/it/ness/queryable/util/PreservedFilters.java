package it.ness.queryable.util;

import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PreservedFilters {
    private static final String STRING_LITERAL = "\"(?:[^\"\\\\]|\\\\.)*\"";

    private PreservedFilters() {
    }

    public static Set<String> names(JavaClassSource entity) {
        AnnotationSource<JavaClassSource> annotation = entity.getAnnotation("QPreserveFilters");
        if (annotation == null) {
            return Set.of();
        }
        String literal = annotation.getLiteralValue();
        if (literal == null || !literal.matches("\\s*(?:" + STRING_LITERAL + "|\\{\\s*"
                + STRING_LITERAL + "(?:\\s*,\\s*" + STRING_LITERAL + ")*\\s*,?\\s*\\})\\s*")) {
            throw new IllegalArgumentException("QPreserveFilters requires literal filter names on " + entity.getName());
        }
        String[] values = annotation.getStringArrayValue();
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("QPreserveFilters requires literal filter names on " + entity.getName());
        }
        Set<String> names = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank() || !value.equals(value.trim())) {
                throw new IllegalArgumentException("Invalid preserved filter name on " + entity.getName());
            }
            names.add(value);
        }
        return names;
    }

    public static boolean contains(JavaClassSource entity, String name) {
        return names(entity).contains(name);
    }

    public static void validateDefinitions(JavaClassSource entity, Set<String> names) {
        for (String name : names) {
            if (!hasDefinition(entity, "FilterDef", "FilterDefs", name)
                    || !hasDefinition(entity, "Filter", "Filters", name)) {
                throw new IllegalArgumentException("Preserved filter " + name
                        + " requires both @FilterDef and @Filter on " + entity.getName());
            }
        }
    }

    private static boolean hasDefinition(JavaClassSource entity, String single, String container, String name) {
        for (AnnotationSource<JavaClassSource> annotation : entity.getAnnotations()) {
            if (simpleName(annotation.getName()).equals(single)
                    && name.equals(annotation.getStringValue("name"))) {
                return true;
            }
            if (simpleName(annotation.getName()).equals(container)) {
                for (AnnotationSource<JavaClassSource> nested : annotation.getAnnotationArrayValue()) {
                    if (name.equals(nested.getStringValue("name"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<String> hooks(JavaClassSource service, Set<String> names, String entityQualifiedName) {
        List<String> hooks = new ArrayList<>();
        Set<String> linked = new LinkedHashSet<>();
        for (MethodSource<JavaClassSource> method : service.getMethods()) {
            AnnotationSource<JavaClassSource> annotation = method.getAnnotation("QPreservedFilter");
            if (annotation == null) {
                continue;
            }
            if (annotation.getLiteralValue() == null
                    || !annotation.getLiteralValue().matches(STRING_LITERAL)) {
                throw new IllegalArgumentException("QPreservedFilter requires a literal filter name: " + method.getName());
            }
            String name = annotation.getStringValue();
            if (!names.contains(name) || !linked.add(name)) {
                throw new IllegalArgumentException("Unknown or duplicate @QPreservedFilter: " + name);
            }
            if (!method.isReturnTypeVoid() || method.isStatic() || method.isAbstract()
                    || method.getParameters().size() != 1 || method.getName().equals("getSearch")) {
                throw new IllegalArgumentException("Invalid preserved filter method: " + method.getName());
            }
            var type = method.getParameters().get(0).getType();
            if (type.isArray() || !type.getQualifiedName().equals("io.quarkus.hibernate.orm.panache.PanacheQuery")
                    || type.getTypeArguments().size() != 1
                    || !type.getTypeArguments().get(0).getQualifiedName().equals(entityQualifiedName)) {
                throw new IllegalArgumentException("Preserved filter method " + method.getName()
                        + " must take PanacheQuery<" + entityQualifiedName + ">");
            }
            hooks.add(method.getName());
        }
        return hooks;
    }

    private static String simpleName(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
