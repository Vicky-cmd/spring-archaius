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
 * Maven Mojo for generating documentation for Spring Archaius configuration files.
 * <p>
 * This class extends {@link AbstractMojo} and is responsible for executing the
 * "generate-doc" goal during the Maven build lifecycle. It utilizes the {@link DocGen}
 * class to parse Java source files and generate a markdown report containing
 * configuration metadata.
 * </p>
 * <p>
 * The Mojo can be configured with the following parameters:
 * </p>
 * <ul>
 *   <li>{@code outputDirectory}: The directory where the generated documentation will be saved.</li>
 *   <li>{@code sourceDirectory}: The main directory containing Java source files. This parameter is required.</li>
 *   <li>{@code sourceSubDirectory}: An optional subdirectory within the source directory to process.</li>
 *   <li>{@code applicationName}: The name of the application for which documentation is generated.</li>
 * </ul>
 *
 * <p>
 * The Mojo logs the start of the documentation generation process and attempts to
 * generate the documentation using {@link DocGen#generateDoc(DocGenArguments)}. If
 * the generation fails, it logs the error and throws a {@link MojoExecutionException}.
 * </p>
 * <p>
 * Example usage in a Maven POM file:
 * </p>
 * <pre>
 * {@code
 * <plugin>
 *   <groupId>io.github.vickycmd</groupId>
 *   <artifactId>spring-archaius-plugin</artifactId>
 *   <version>1.0</version>
 *   <executions>
 *     <execution>
 *       <goals>
 *         <goal>generate-doc</goal>
 *       </goals>
 *     </execution>
 *   </executions>
 *   <configuration>
 *     <sourceDirectory>${project.basedir}/src/main/java</sourceDirectory>
 *     <outputDirectory>${project.build.directory}/docs</outputDirectory>
 *   </configuration>
 * </plugin>
 * }
 * </pre>
 *
 * <p>
 * This class is annotated with {@link Slf4j} for logging and {@link Mojo} to define
 * the goal name and lifecycle phase. It does not require a Maven project to execute.
 * </p>
 *
 * @see DocGen
 * @see DocGenArguments
 * @see AbstractMojo
 * @see MojoExecutionException
 * @author Vicky CMD
 * @since 1.0
 * @version 1.0
 */
@Slf4j
@Mojo( name = "generate-doc", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = false )
public class GenerateDocMojo extends AbstractMojo {

    /**
     * The directory where the generated documentation will be output.
     * This parameter is optional and can be specified via the Maven property "outputDirectory".
     */
    @Parameter( property = "outputDirectory", required = false )
    private String outputDirectory;

    /**
     * The directory where the source files are located.
     * This parameter is required and defaults to the current directory.
     * It can be configured using the 'sourceDirectory' property.
     */
    @Parameter(defaultValue = "./", property = "sourceDirectory", required = true)
    private File sourceDirectory;

    /**
     * The subdirectory within the source directory to process.
     * This parameter is optional and can be specified via the Maven property "sourceSubDirectory".
     */
    @Parameter(property = "sourceSubDirectory", required = false)
    private String sourceSubDirectory;

    /**
     * The name of the application for which documentation is generated.
     * This parameter is optional and defaults to the project name.
     * It can be configured using the 'applicationName' property.
     */
    @Parameter(defaultValue = "${project.name}", property = "applicationName", required = false)
    private String applicationName;

    /**
     * The main execution method of the Mojo.
     * It logs the start of the documentation generation process and attempts to
     * generate the documentation using {@link DocGen#generateDoc(DocGenArguments)}.
     * If the generation fails, it logs the error and throws a {@link MojoExecutionException}.
     *
     * @throws MojoExecutionException if the documentation generation fails
     */
    @Override
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
