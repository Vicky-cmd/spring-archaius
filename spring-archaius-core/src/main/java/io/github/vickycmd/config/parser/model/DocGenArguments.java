package io.github.vickycmd.config.parser.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the arguments required for document generation.
 * <p>
 * This class holds the directory paths and application name
 * necessary for generating documentation. It includes:
 * <ul>
 *   <li>sourceDir: The main directory containing source files.</li>
 *   <li>sourceSubDir: A subdirectory within the source directory.</li>
 *   <li>outputDir: The directory where the generated documentation will be saved.</li>
 *   <li>applicationName: The name of the application for which documentation is generated.</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public class DocGenArguments {
    private final String sourceDir;
    private final String sourceSubDir;
    private final String outputDir;
    private String applicationName;
}
