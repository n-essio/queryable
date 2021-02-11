package it.ness.queryable.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //on class field
public @interface QList {
	String prefix() default "";
	String name() default "";
	String condition() default "";
	QOption[] options() default {};
}
