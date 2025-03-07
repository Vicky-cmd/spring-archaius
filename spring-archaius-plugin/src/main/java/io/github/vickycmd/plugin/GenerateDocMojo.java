package io.github.vickycmd.plugin;


import io.github.vickycmd.config.parser.DocGen;
import io.github.vickycmd.config.parser.model.DocGenArguments;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Goal which touches a timestamp file.
 */
@Slf4j
@Mojo( name = "generate-doc", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = false )
public class GenerateDocMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter( property = "outputDirectory", required = false )
    private String outputDirectory;

    @Parameter(defaultValue = "./", property = "sourceDirectory", required = true)
    private File sourceDirectory;

    @Parameter(property = "sourceSubDirectory", required = false)
    private String sourceSubDirectory;

    @Parameter(defaultValue = "${project.name}", property = "applicationName", required = false)
    private String applicationName;

    public void execute() throws MojoExecutionException {
        log.info("Spring Archaius Config Docgen Plugin started successfully");
        log.info("Generating documentation...");
        log.info("Generating documentation for the configuration files located at {}", sourceDirectory);
        Try.run(() ->
             new DocGen().generateDoc(new DocGenArguments(sourceDirectory.getAbsolutePath(), sourceSubDirectory, outputDirectory, applicationName)).ifPresent(dir -> {
                 this.outputDirectory = dir;
             })
        ).getOrElseThrow(error -> {
            log.error("Failed to generate documentation. Failed with the error {}", error.getMessage());
            return new MojoExecutionException("Document Generation Failed for the configuration files located at " + sourceDirectory);
        });
    }
}
