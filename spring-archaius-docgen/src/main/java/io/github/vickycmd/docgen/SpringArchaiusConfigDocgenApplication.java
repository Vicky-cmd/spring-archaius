package io.github.vickycmd.docgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the Spring Archaius Config Docgen Application.
 * This class is responsible for bootstrapping the application and launching the
 * Spring Boot application context.
 *
 * @author Vicky CMD
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class SpringArchaiusConfigDocgenApplication {

    /**
     * The main method that starts the Spring Boot application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringArchaiusConfigDocgenApplication.class, args);
    }

}
