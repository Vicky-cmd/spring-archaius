package io.github.vickycmd.config;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ArchaiusConfiguration.class)
public @interface EnableSpringArchaius {
}
