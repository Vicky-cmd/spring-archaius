package io.github.vickycmd.config.aspects;

import io.github.vickycmd.config.errors.ConfigException;
import io.github.vickycmd.config.fields.Field;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.Arrays;


/**
 * The ValidationAspects class provides aspect-oriented validation for configuration fields.
 * It intercepts method calls annotated with {@link io.github.vickycmd.config.ValidateField} 
 * and validates the {@link io.github.vickycmd.config.fields.Field} parameters according to their defined constraints.
 *
 * <p>Features include:</p>
 * <ul>
 *   <li>Validation of field values against allowed values</li>
 *   <li>Custom validation using field validators</li>
 *   <li>Sensitive data handling</li>
 *   <li>Detailed error reporting through {@link io.github.vickycmd.config.errors.ConfigException}</li>
 * </ul>
 *
 * <p>This class requires:</p>
 * <ul>
 *   <li>Spring AOP support enabled</li>
 *   <li>Property 'spring.archaius.config.validation.enabled' set to true (default)</li>
 * </ul>
 *
 * <p>This class works in conjunction with:</p>
 * <ul>
 *   <li>{@link io.github.vickycmd.config.ValidateField} - Annotation to mark methods for validation</li>
 *   <li>{@link io.github.vickycmd.config.fields.Field} - Field definitions with validation rules</li>
 *   <li>{@link io.github.vickycmd.config.Configuration} - Main configuration handling class</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @ValidateField
 * public <T> T get(Field property, Class<T> targetType) {
 *     // Method implementation
 * }
 * }
 * </pre>
 *
 * @author Vicky CMD
 * @version 1.0
 * @see io.github.vickycmd.config.ValidateField
 * @see io.github.vickycmd.config.fields.Field
 * @see io.github.vickycmd.config.Configuration
 * @since 1.0
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(prefix = "spring.archaius.config.validation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ValidationAspects {

    /**
     * Intercepts method calls annotated with {@link io.github.vickycmd.config.ValidateField}
     * and validates the {@link io.github.vickycmd.config.fields.Field} parameters according to their defined constraints.
     *
     * @param joinPoint the method execution join point
     * @return the result of the method execution if validation passes, otherwise throws a ConfigException
     * @throws Throwable if an error occurs during method execution or validation fails
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * @ValidateField
     * public <T> T get(Field property, Class<T> targetType) {
     *     // Method implementation
     * }
     * }
     * </pre>
     */
    @Around("@annotation(io.github.vickycmd.config.ValidateField) && execution(* *(.., io.github.vickycmd.config.fields.Field, ..))")
    public Object validateField(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        String[] fieldArgs = extractArgumentsForExtractingPropertyName(parameters, args);
        Field field = extractFieldDetails(args, fieldArgs, methodName);

        Object result = joinPoint.proceed();
        if (null == field) return result;

        if (field.validator()!=null && !field.validate(result)) {
            log.debug("Invalid value {} accessed for the property {} using method {}", field.type().isSensitive()?"****": result, this.getPropertyName(field.name(), fieldArgs), methodName);
            log.error("Invalid value {} configured for the field: {}", field.type().isSensitive()?"****": result, this.getPropertyName(field.name(), fieldArgs));
            throw new ConfigException(String.format("Invalid configuration for field: %s", this.getPropertyName(field.name(), fieldArgs)), "Please validate the configurations for - " + this.getPropertyName(field.name(), fieldArgs));
        }

        if (!field.isAllowed(result)) {
            log.debug("Invalid value {} accessed for the property {} using method {}", field.type().isSensitive()?"****": result, this.getPropertyName(field.name(), fieldArgs), methodName);
            throw new ConfigException(String.format("Invalid value for field: %s", this.getPropertyName(field.name(), fieldArgs)), String.format("Please validate the configurations for %s. Configured value %s not part of the allowed values %s", this.getPropertyName(field.name(), fieldArgs), field.type().isSensitive()?"****":result, Arrays.toString(field.allowedValues().toArray())));
        }

        return result;
    }

    private Field extractFieldDetails(Object[] args, String[] fieldArgs, String methodName) {
        for (Object argument : args) {
            if (argument instanceof Field field) {
                log.debug("Validating usage of field: {} with method: {}", this.getPropertyName(field.name(), fieldArgs), methodName);
                return field;
            }
        }
        return null;
    }

    private static String[] extractArgumentsForExtractingPropertyName(Parameter[] parameters, Object[] args) {
        if (null != parameters && args.length > 1
                && args[args.length - 1] instanceof String[] currFieldArgs
                && "args".equals(parameters[args.length - 1].getName())) {
            return currFieldArgs;
        }
        return null;
    }

    private String getPropertyName(String name, String[] args) {
        if (null == args || args.length == 0) return name;

        return String.format(name, (Object[]) args);
    }
}
