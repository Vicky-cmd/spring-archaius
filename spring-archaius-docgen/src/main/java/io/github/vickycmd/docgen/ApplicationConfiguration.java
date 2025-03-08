package io.github.vickycmd.docgen;


import io.github.vickycmd.config.parser.DocGen;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up beans in the Spring application context.
 *
 * <p>This class is annotated with {@link Configuration}, indicating that it
 * contains one or more {@link Bean} methods. These methods are used to define
 * beans that will be managed by the Spring container.</p>
 *
 * <p>The {@link #docGen()} method defines a {@link Bean} for the {@link DocGen}
 * class, which is responsible for generating documentation for configuration
 * fields by parsing Java source files and extracting field metadata into a
 * markdown report.</p>
 *
 * <p>Related classes and usage:</p>
 * <ul>
 *   <li>{@link DocGen} - Provides the core functionality for documentation generation.</li>
 *   <li>{@link io.github.vickycmd.config.parser.model.DocGenArguments} - Used for input parameters.</li>
 *   <li>{@link io.github.vickycmd.config.parser.FieldVisitor} - For parsing field metadata.</li>
 *   <li>{@link io.github.vickycmd.config.parser.model.MarkdownGenResult} - For markdown generation.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
 * DocGen docGen = context.getBean(DocGen.class);
 * }
 * </pre>
 *
 * <p>Author: Vicky CMD</p>
 * <p>Version: 1.0</p>
 * <p>Since: 1.0</p>
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * Creates a {@link DocGen} bean, which is responsible for generating documentation
     * for configuration fields.
     *
     * @return A new instance of the {@link DocGen} class.
     */
    @Bean
    public DocGen docGen() {
        return new DocGen();
    }
}
