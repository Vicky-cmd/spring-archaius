package io.github.vickycmd.docgen;


import io.github.vickycmd.config.parser.DocGen;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public DocGen docGen() {
        return new DocGen();
    }
}
