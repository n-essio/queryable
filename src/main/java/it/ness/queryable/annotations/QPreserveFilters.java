package it.ness.queryable.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Protects named Hibernate FilterDef/Filter pairs from V3 source regeneration.
 * Values must be literal, complete filter names.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QPreserveFilters {
    String[] value();
}
