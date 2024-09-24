package it.ness.queryable.util;

import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class FilterUtils {

    public static AnnotationSource<JavaClassSource> addFilterDef(JavaClassSource javaClass, String filterName) {
        AnnotationSource<JavaClassSource> filterDefAnnotation = javaClass.addAnnotation();
        filterDefAnnotation.setName("FilterDef");
        filterDefAnnotation.setStringValue("name", filterName);
        return filterDefAnnotation;
    }

    public static AnnotationSource<JavaClassSource> addFilter(JavaClassSource javaClass, String filterName, String condition) {
        AnnotationSource<JavaClassSource> filterAnnotation = javaClass.addAnnotation();
        filterAnnotation.setName("Filter");
        filterAnnotation.setStringValue("name", filterName);
        filterAnnotation.setStringValue("condition", condition);
        return filterAnnotation;
    }

    public static void addParamDef(AnnotationSource<JavaClassSource> filterDefAnnotation, String name, String type) {
        AnnotationSource<JavaClassSource> paramAnnotation = filterDefAnnotation.addAnnotationValue("parameters");
        paramAnnotation.setName("ParamDef");
        paramAnnotation.setStringValue("name", name);
        paramAnnotation.addAnnotationValue("type", getClass(type));
    }

    public static Class getClass(String value) {
        System.out.println("**** : " + value);
        if ("LocalDateTime".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : LocalDateTime");
            return LocalDateTime.class;
        }
        if ("ZonedDateTime".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : ZonedDateTime");
            return ZonedDateTime.class;
        }
        if ("LocalDate".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : LocalDate");
            return LocalDate.class;
        }
        if ("Date".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : Date");
            return java.util.Date.class;
        }
        if ("java.util.Date".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : Date");
            return java.util.Date.class;
        }
        else if ("big_decimal".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : BigDecimal");
            return BigDecimal.class;
        }
        else if ("big_integer".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : BigInteger");
            return BigInteger.class;
        }
        else if ("boolean".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : Boolean");
            return Boolean.class;
        }
        else if ("int".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : Integer");
            return Integer.class;
        }
        else if ("long".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : Long");
            return Long.class;
        }
        else if ("string".equalsIgnoreCase(value)) {
            System.out.println("getClass for column : " + value + " as : String");
            return String.class;
        }
        System.out.println("getClass for column : " + value + " as : String");
        return String.class;
    }

}
