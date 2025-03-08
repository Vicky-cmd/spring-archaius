package io.github.vickycmd.docgen.commands;


import io.github.vickycmd.config.parser.DocGen;
import io.github.vickycmd.config.parser.model.DocGenArguments;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Optional;

/**
 * Handles shell commands for generating documentation of field configurations in a project.
 * <p>
 * This class is a Spring Shell component that provides a command-line interface for generating
 * documentation of configuration fields using the Spring Archaius framework. It leverages the
 * {@link DocGen} class to perform the actual documentation generation process.
 * </p>
 * <p>
 * The primary command provided by this handler is "generate", which requires specifying the
 * source directory of configuration files. Optional parameters include a subdirectory, application
 * name, and output directory for the generated documentation.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * shell:> generate -source /path/to/source -subdir config -appName MyApp -output /path/to/output
 * }
 * </pre>
 * <p>
 * The command logs the start of the documentation generation process and provides feedback on
 * the success or failure of the operation. If successful, it returns the path to the generated
 * documentation.
 * </p>
 *
 * Related classes:
 * <ul>
 *   <li>{@link DocGen} - Responsible for the actual generation of documentation.</li>
 *   <li>{@link DocGenArguments} - Encapsulates the arguments required for documentation generation.</li>
 * </ul>
 *
 * <p>
 * Author: Vicky CMD
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@ShellComponent
public class CommandsHandler {

    private final DocGen docGen;

    /**
     * Constructs a CommandsHandler instance with the provided DocGen instance.
     *
     * @param docGen The DocGen instance used for documentation generation.
     */
    @Autowired
    public CommandsHandler(DocGen docGen) {
        this.docGen = docGen;
    }

    /**
     * Generates documentation for field configurations used in the project.
     * <p>
     * This method is a shell command that triggers the documentation generation process
     * for configuration fields in a project. It requires specifying the source directory
     * where the configuration files are located. Optional parameters include a subdirectory
     * within the source directory, the application name, and the output directory for the
     * generated documentation.
     * </p>
     * <p>
     * Usage example:
     * <pre>
     * {@code
     * shell:> generate -source /path/to/source -subdir config -appName MyApp -output /path/to/output
     * }
     * </pre>
     * <p>
     * The method logs the start of the documentation generation process and provides feedback
     * on the success or failure of the operation. If successful, it returns the path to the
     * generated documentation. In case of failure, it logs an error and returns a failure message.
     * </p>
     *
     * @param source The source directory where the configuration files are located.
     * @param subDir The subdirectory within the source directory where the configuration files are located.
     * @param appName The application name for which the documentation is generated.
     * @param output The output directory where the documentation will be generated.
     * @return A message indicating the success or failure of the documentation generation process.
     */
    @ShellMethod(key = "generate", value = "Generate the documentation for the field configurations used in the project")
    public String generateDoc(@ShellOption(value = {"-source", "-s"}, help = "The source directory where the configuration files are located") String source,
                              @ShellOption(value = {"-subdir", "-sd"}, defaultValue = "", help = "The sub directory within the source directory where the configuration files are located") String subDir,
                              @ShellOption(value = "-appName", defaultValue = "", help = "The application name") String appName,
                              @ShellOption(value = {"-output", "-o"}, defaultValue = "", help = "The output directory where the documentation will be generated") String output) {
        log.info("Spring Archaius Config Docgen Application started successfully");
        log.info("Generating documentation...");
        log.info("Generating documentation for the configuration files located at {}", source);
        Optional<String> outputFile = docGen.generateDoc(new DocGenArguments(source, subDir, output, appName));
        if (outputFile.isPresent()) {
            log.debug("Documentation generated successfully at {}", outputFile.get());
            return "Documentation generated successfully at " + outputFile.get();
        }
        log.error("Failed to generate documentation");
        return "Document Generation Failed for the configuration files located at " + source;
    }
}
