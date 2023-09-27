package io.quarkus.qeex.api.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Target({METHOD})
@Retention(RUNTIME)
@Documented
public @interface QeexMessage {

    int id() default -1;

    String message() default "";

    int code() default 400;
}
