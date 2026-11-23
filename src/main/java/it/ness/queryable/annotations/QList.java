package it.ness.queryable.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates an {@code IN} filter for a comma-separated query parameter.
 *
 * <p>Supported field types are {@link String}, {@link Integer}, {@link Long},
 * {@link java.math.BigInteger}, and {@link java.math.BigDecimal}. V3 uses a
 * pluralized Hibernate filter parameter name; V4 generates a typed criteria
 * predicate. Values are converted to the entity field type before the
 * predicate is evaluated.</p>
 *
 * @see <a href="https://queryable.dev/docs/#qlist">Queryable QList documentation</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QList {
	/**
	 * Query parameter prefix. The default is {@code obj}.
	 */
	String prefix() default "";

	/**
	 * Entity field or path to filter. The annotated field name is used by default.
	 */
	String name() default "";

	/**
	 * Custom predicate condition. The default generated condition is
	 * {@code field IN (:pluralFieldName)} for V3.
	 */
	String condition() default "";

	/**
	 * Generation options that control how the search method is emitted.
	 */
	QOption[] options() default {};
}
