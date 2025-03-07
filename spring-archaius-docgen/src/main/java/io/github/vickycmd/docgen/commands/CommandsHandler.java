package io.github.vickycmd.docgen.commands;


import io.github.vickycmd.config.parser.DocGen;
import io.github.vickycmd.config.parser.model.DocGenArguments;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Optional;

@Slf4j
@ShellComponent
public class CommandsHandler {

    private final DocGen docGen;

    @Autowired
    public CommandsHandler(DocGen docGen) {
        this.docGen = docGen;
    }

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
