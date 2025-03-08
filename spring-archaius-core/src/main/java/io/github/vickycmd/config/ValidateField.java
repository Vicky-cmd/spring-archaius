package io.github.vickycmd.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Annotation to indicate that a method's fields should be validated.
 *
 * <p>This annotation is used to mark methods whose fields require validation
 * at runtime. It is retained at runtime and can be applied to methods containing the
 * {@link io.github.vickycmd.config.fields.Field} class as one of the arguments.</p>
 *
 * <p>See also:</p>
 * <ul>
 *   <li>{@link io.github.vickycmd.config.fields.Field}</li>
 *   <li>{@link io.github.vickycmd.config.Configuration}</li>
 *   <li>{@link io.github.vickycmd.config.aspects.ValidationAspects}</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateField {

}
