package io.quarkus.qeex.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface QeexExceptionBundle {

    String project();
    int id() default 100;
    int code() default 400;
    String language();
    String message() default "";
}
