package it.ness.queryable.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Links a manual void method taking PanacheQuery of the entity to a preserved
 * filter. V3 getSearch calls it after automatic filters, in declaration order.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface QPreservedFilter {
    String value();
}
