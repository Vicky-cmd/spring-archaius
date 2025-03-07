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

@Slf4j
@Aspect
@Component
@ConditionalOnProperty(prefix = "spring.archaius.config.validation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ValidationAspects {

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
