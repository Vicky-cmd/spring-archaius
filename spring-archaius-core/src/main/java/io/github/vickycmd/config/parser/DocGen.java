package io.github.vickycmd.config.parser;

import com.github.javaparser.JavaParser;
import io.github.vickycmd.config.parser.model.DocGenArguments;
import io.github.vickycmd.config.parser.model.MarkdownGenResult;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.github.vickycmd.config.parser.Config.*;

/**
 * The DocGen class provides functionality to generate documentation for configuration fields
 * by parsing Java source files and extracting field metadata into a markdown report.
 *
 * <p>Features include:</p>
 * <ul>
 *   <li>Recursive scanning of Java source files in a directory</li>
 *   <li>Extraction of field metadata using JavaParser</li>
 *   <li>Generation of markdown documentation with field details</li>
 *   <li>Support for custom output directory configuration</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * DocGenArguments args = new DocGenArguments();
 * args.setSourceDir("/path/to/source");
 * args.setOutputDir("/path/to/output");
 * 
 * DocGen docGen = new DocGen();
 * Optional<String> outputPath = docGen.generateDoc(args);
 * }
 * </pre>
 *
 * <p>The generated documentation includes:</p>
 * <ul>
 *   <li>Field names and types</li>
 *   <li>Default values and constraints</li>
 *   <li>Description and usage examples</li>
 *   <li>Validation rules and allowed values</li>
 * </ul>
 *
 * <p>This class works in conjunction with:</p>
 * <ul>
 *   <li>{@link DocGenArguments} - For input parameters</li>
 *   <li>{@link FieldVisitor} - For parsing field metadata</li>
 *   <li>{@link MarkdownGenResult} - For markdown generation</li>
 * </ul>
 *
 * @author Vicky CMD
 * @version 1.0
 * @see io.github.vickycmd.config.parser.model.DocGenArguments
 * @see io.github.vickycmd.config.parser.FieldVisitor
 * @see io.github.vickycmd.config.parser.model.MarkdownGenResult
 * @since 1.0
 */
@Slf4j
@Service
public class DocGen {

    private static final String OUTPUT_FILE_NAME = "archaius-config-report.md";

    /**
     * Generates documentation by extracting metadata from Java source files and creating a markdown report.
     * This method performs the following steps:
     * <ol>
     *   <li>Scans the source directory recursively for Java files</li>
     *   <li>Extracts field metadata from each Java file using {@link FieldVisitor}</li>
     *   <li>Generates a markdown report with the extracted metadata</li>
     *   <li>Writes the report to the configured output directory</li>
     * </ol>
     *
     * <p>The method uses error handling with {@link io.vavr.control.Try} to gracefully handle failures
     * during document generation. If any step fails, the error is logged and an empty Optional is returned.</p>
     *
     * @param arguments The {@link DocGenArguments} containing:
     *                 <ul>
     *                   <li>sourceDir - Root directory containing Java source files</li>
     *                   <li>outputDir - Directory where markdown report will be written</li>
     *                   <li>sourceSubDir - Optional subdirectory under sourceDir to process</li>
     *                 </ul>
     * @return An Optional containing the path to the generated markdown file if successful,
     *         or empty Optional if document generation fails
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DocGenArguments args = new DocGenArguments();
     * args.setSourceDir("/path/to/source");
     * args.setOutputDir("/path/to/output");
     * 
     * DocGen docGen = new DocGen();
     * Optional<String> result = docGen.generateDoc(args);
     * result.ifPresent(path -> System.out.println("Documentation generated at: " + path));
     * }
     * </pre>
     *
     * @see DocGenArguments
     * @see FieldVisitor
     * @see #getAllJavaFiles(File)
     * @see #parseJavaFile(File) 
     * @see #writeMarkdownFile(DocGenArguments, List)
     */
    public Optional<String> generateDoc(DocGenArguments arguments) {
        return Try.of(() -> {
            log.info("Starting metadata extraction from Java files in {}", arguments.getSourceDir());
            List<File> javaFiles = getAllJavaFiles(new File(arguments.getSourceDir()));

            List<Map<String, String>> extractedFields = new ArrayList<>();
            for (File file : javaFiles) {
                extractedFields.addAll(parseJavaFile(file));
            }

            writeMarkdownFile(arguments, extractedFields);
            var outputDir = getOutputFilePath(arguments).toString();
            log.info("Extraction complete! Metadata written to {}", outputDir);
            return Optional.of(outputDir);
        }).onFailure(error ->
            log.error("Document Generation Failed with the error {} and message {}",
                    error.getClass().getSimpleName(), error.getMessage())
        ).getOrElse(Optional.empty());
    }

    /**
     * Retrieves all Java files from a specified directory, searching recursively.
     *
     * @param directory the root directory to search for Java files
     * @return a list of Java files found within the directory and its subdirectories
     * @throws IllegalArgumentException if the specified directory does not exist
     */
    private List<File> getAllJavaFiles(File directory) {
        if (!directory.exists()) {
            throw new IllegalArgumentException("Source directory does not exist: " + directory.getAbsolutePath());
        }
        return Arrays.stream(Optional.ofNullable(directory.listFiles()).orElse(new File[0]))
                .flatMap(file -> file.isDirectory() ? getAllJavaFiles(file).stream() : Stream.of(file))
                .filter(file -> file.getName().endsWith(".java"))
                .toList();
    }

    /**
     * Parses a Java file to extract field details using a JavaParser.
     *
     * <p>This method attempts to parse the provided Java file and extract
     * field information by utilizing the `FieldVisitor` class. The extracted
     * data is stored in a list of maps, where each map represents a field
     * with its associated properties.</p>
     *
     * <p>If the parsing operation fails, error logs are generated with details
     * about the failure, including the error type and message.</p>
     *
     * @param file the Java file to be parsed
     * @return a list of maps containing field details extracted from the file
     */
    private List<Map<String, String>> parseJavaFile(File file) {
        List<Map<String, String>> fieldDataList = new ArrayList<>();
        Try.run(() ->
            new JavaParser().parse(file).getResult()
                    .ifPresent(cu -> cu.accept(new FieldVisitor(fieldDataList), null))
        ).onFailure(err -> {
            log.error("Failed to parse file: {}", file.getAbsolutePath());
            log.error("File parsing failed with the error {} and message {}",
                    err.getClass().getSimpleName(), err.getMessage());
        });
        return fieldDataList;
    }

    /**
     * Extracts application details from a POM file specified by the given arguments.
     * <p>
     * This method attempts to read a POM file located in the source directory
     * specified by the {@link DocGenArguments}. If a subdirectory is provided,
     * it will look for the POM file within that subdirectory. The method extracts
     * details such as groupId, artifactId, version, and name from the POM file.
     * If the name is not specified, it formats the artifactId as the name.
     * <p>
     * Usage example:
     * <pre>
     * {@code
     * DocGenArguments args = new DocGenArguments("/path/to/source", "subdir", "/output", "MyApp");
     * Map<String, String> details = extractApplicationDetails(args);
     * }
     * </pre>
     * <p>
     * @param arguments The {@link DocGenArguments} containing directory paths.
     * @return A map containing application details such as groupId, artifactId, version, and name.
     *         Returns an empty map if the POM file is not found or an error occurs.
     */
    private Map<String, String> extractApplicationDetails(DocGenArguments arguments) {
        return Try.of(() -> {
            Map<String, String> details = new HashMap<>();
            Path pomFilePath = StringUtils.hasText(arguments.getSourceSubDir())?
                    Paths.get(arguments.getSourceDir(), arguments.getSourceSubDir(), "pom.xml")
                    : Paths.get(arguments.getSourceDir(), "pom.xml");
            if (!Files.exists(pomFilePath)) {
                log.warn("POM file not found at {}", pomFilePath);
                return details;
            }
            File pomFile = pomFilePath.toFile();
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(pomFile));
            details.put("groupId", model.getGroupId());
            details.put("artifactId", model.getArtifactId());
            details.put("version", model.getVersion());
            details.put("name", StringUtils.hasText(model.getName())
                    ?model.getName():formatArtifactId(model.getArtifactId()));
            return details;
        }).getOrElse(Collections.emptyMap());
    }

    /**
     * Formats an artifact ID by capitalizing the first letter of each word.
     * <p>
     * This method splits the given artifact ID string by hyphens and capitalizes
     * the first letter of each resulting word. It then concatenates these words
     * with a space in between to form a formatted string.
     * </p>
     * <p>
     * Example: "my-artifact-id" becomes "My Artifact Id".
     * </p>
     *
     * @param artifactId the artifact ID to format, typically in hyphen-separated lowercase form
     * @return a formatted string with each word capitalized and separated by spaces
     */
    public String formatArtifactId(String artifactId) {
        String[] words = artifactId.split("-"); // Split by hyphen
        StringBuilder formatted = new StringBuilder();
        Arrays.stream(words).filter(StringUtils::hasText)
                .forEach(word ->
                    formatted.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1))
                            .append(" ")
                );
        return formatted.toString().trim();
    }

    /**
     * Writes extracted metadata to a Markdown file in tabular format.
     * <p>
     * This method generates a Markdown document containing configuration metadata
     * for a Spring Archaius-based application. It writes a high-level summary,
     * field configuration details, lambda expression details, and validator
     * expressions to the file. The output file path is determined based on the
     * provided {@link DocGenArguments}.
     * </p>
     *
     * @param arguments The {@link DocGenArguments} containing directory paths and
     *                  application name for document generation.
     * @param fields    A list of maps where each map represents field metadata
     *                  including name, display name, description, and other
     *                  attributes.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    private void writeMarkdownFile(DocGenArguments arguments, List<Map<String, String>> fields) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(this.getOutputFilePath(arguments))) {
            writeHighLevelSummaryOfDocument(arguments, writer);
            MarkdownGenResult result = writeFieldDetailsToFile(fields, writer);
            writeLambdaExpressionDetailsToFile(fields, result, writer);
            writeValidatorDetailsToTheFile(fields, result, writer);

        }
    }

    /**
     * Determines the output file path for the generated documentation based on the provided arguments.
     * <p>
     * This method evaluates the output directory specified in the {@link DocGenArguments}.
     * If the output directory is provided and not empty, it constructs the path using this directory
     * and the predefined output file name. If the output directory is not specified, it checks the
     * validity of the source directory. If the source directory is not a valid directory, it defaults
     * to the user's home directory if available, or the current directory otherwise.
     * <p>
     * Additionally, it checks for the existence of specific subdirectories within the source directory
     * (e.g., "src/main/resources" or "src") and constructs the path accordingly if they exist.
     * <p>
     * @param arguments The {@link DocGenArguments} containing the directory paths and application name.
     * @return The {@link Path} to the output file where the documentation will be saved.
     */
    private Path getOutputFilePath(DocGenArguments arguments) {
        if (StringUtils.hasText(arguments.getOutputDir())) {
            return Paths.get(arguments.getOutputDir(), OUTPUT_FILE_NAME);
        }

        File sourceDir = new File(arguments.getSourceDir());
        if (!sourceDir.isDirectory()) {
            if (StringUtils.hasText(System.getProperty("user.home"))) {
                return Paths.get(System.getProperty("user.home"), OUTPUT_FILE_NAME);
            }
            return Paths.get(OUTPUT_FILE_NAME);
        } else if (Path.of(arguments.getSourceDir(), "src", "main", "resources").toFile().exists()) {
            return Paths.get(arguments.getSourceDir(), "src", "main", "resources", OUTPUT_FILE_NAME);
        } else if (Path.of(arguments.getSourceDir(), "src").toFile().exists()) {
            return Paths.get(arguments.getSourceDir(), "src", OUTPUT_FILE_NAME);
        }
        return Paths.get(arguments.getSourceDir(), OUTPUT_FILE_NAME);
    }

    /**
     * Writes validator details to a Markdown file if validators are present in the fields.
     * <p>
     * This method checks if the {@link MarkdownGenResult} indicates the presence of validators.
     * If so, it iterates over the provided list of field metadata, extracting and writing
     * validator expressions to the given {@link BufferedWriter}. Each field's validator
     * expressions are formatted and written under a dedicated section in the Markdown document.
     * </p>
     * <p>
     * The method uses a regex pattern to validate the format of the validator definitions
     * and matches them against known validator descriptions from {@link Config#VALIDATORS_DESCRIPTION}.
     * If a match is found, the corresponding description is written to the file.
     * </p>
     * <p>
     * Error handling is implemented using {@link io.vavr.control.Try} to log any failures
     * encountered during the writing process.
     * </p>
     *
     * @param fields A list of maps containing field metadata, including validator details.
     * @param result The {@link MarkdownGenResult} indicating if validators are present.
     * @param writer The {@link BufferedWriter} used to write the validator details to the file.
     * @throws IOException If an I/O error occurs during writing.
     */
    private void writeValidatorDetailsToTheFile(List<Map<String, String>> fields, MarkdownGenResult result, BufferedWriter writer) throws IOException {
        if (!result.hasValidator()) return;
        String validatorsRegex = "^[^();\n\r]*$";
        Pattern validatorPattern = Pattern.compile(validatorsRegex);
        writer.write("## Validator Expressions\n\n");
        for (Map<String, String> field : fields) {
            if (!field.containsKey(VALIDATOR_ARG) || field.get(VALIDATOR_ARG).trim().isEmpty()) continue;
            String validatorDefinitions = field.get(VALIDATOR_ARG);

            writer.write(String.format("### Field: %s%s%s",
                    field.getOrDefault("name", ""),
                    System.lineSeparator(),
                    System.lineSeparator()));
            if (validatorPattern.matcher(validatorDefinitions).matches()) {
                Arrays.stream(validatorDefinitions.split(","))
                        .map(String::trim)
                        .filter(Config.VALIDATORS_DESCRIPTION::containsKey)
                        .forEach(validator ->
                            Try.run(() ->
                                writer.write(String.format("#### %s%s%s%s%s%s",
                                        validator,
                                        System.lineSeparator(),
                                        System.lineSeparator(),
                                        Config.VALIDATORS_DESCRIPTION.get(validator),
                                        System.lineSeparator(),
                                        System.lineSeparator())))
                                .onFailure(error -> log.error("Failed to write validator description for {} with error {}", validator, error.getMessage()))
                        );
            }
            writer.write(String.format("```java%s%s%s```%s",
                    System.lineSeparator(),
                    field.get(VALIDATOR_ARG),
                    System.lineSeparator(),
                    System.lineSeparator()));
        }

    }

    /**
     * Writes lambda expression details to a Markdown file if present in the fields.
     * <p>
     * This method checks the {@link MarkdownGenResult} to determine if any lambda expressions
     * are present. If so, it iterates over the list of field metadata, extracting and writing
     * lambda expressions to the provided {@link BufferedWriter}. Each field's lambda expression
     * is formatted and written under a dedicated section in the Markdown document.
     * </p>
     * <p>
     * The method ensures that only fields containing non-empty lambda expressions are processed.
     * Each lambda expression is enclosed in a code block for clarity in the Markdown output.
     * </p>
     *
     * @param fields A list of maps containing field metadata, including lambda expression details.
     * @param result The {@link MarkdownGenResult} indicating if lambda expressions are present.
     * @param writer The {@link BufferedWriter} used to write the lambda expression details to the file.
     * @throws IOException If an I/O error occurs during writing.
     */
    private void writeLambdaExpressionDetailsToFile(List<Map<String, String>> fields, MarkdownGenResult result, BufferedWriter writer) throws IOException {
        if (!result.hasLambdaExpression()) return;

        writer.write("## Lambda Expressions\n\n");
        for (Map<String, String> field : fields) {
            if (!field.containsKey(LAMBDA_EXP_ARG) || field.get(LAMBDA_EXP_ARG).trim().isEmpty()) continue;
            writer.write(String.format("### Field: %s%s%s```java%s%s%s```%s",
                    field.getOrDefault("name", ""),
                    System.lineSeparator(),
                    System.lineSeparator(),
                    System.lineSeparator(),
                    field.get(LAMBDA_EXP_ARG),
                    System.lineSeparator(),
                    System.lineSeparator()));
        }
        writer.write("\n\n");
    }

    /**
     * Writes field configuration metadata to a Markdown file in tabular format.
     * <p>
     * This method iterates over a list of field metadata, writing each field's
     * details such as name, display name, description, requirement status, type,
     * importance, validation status, default value, and allowed values to the
     * provided {@link BufferedWriter}. The metadata is formatted into a Markdown
     * table for clarity and organization.
     * </p>
     * <p>
     * The method also determines if any fields contain lambda expressions or
     * validators, returning a {@link MarkdownGenResult} indicating their presence.
     * </p>
     *
     * @param fields A list of maps where each map contains metadata for a field.
     * @param writer The {@link BufferedWriter} used to write the field details to the file.
     * @return A {@link MarkdownGenResult} indicating the presence of lambda expressions
     *         and validators in the field metadata.
     * @throws IOException If an I/O error occurs during writing.
     */
    private MarkdownGenResult writeFieldDetailsToFile(List<Map<String, String>> fields, BufferedWriter writer) throws IOException {
        writer.write(String.format("## Field Configuration Metadata%s%s", System.lineSeparator(), System.lineSeparator()));
        writer.write("| Name | Display Name | Description | Required | Type | Importance | Validated Field | Default Value | Allowed Values |\n");
        writer.write("|------|--------------|-------------|----------|------|------------|-----------------|---------------|----------------|\n");

        boolean hasLambdaExpression = false;
        boolean hasValidator = false;
        for (Map<String, String> field : fields) {
            writer.write(String.format("| %s | %s | %s | %s | %s | %s | %s | %s | %s |%s",
                    field.getOrDefault("name", ""),
                    field.getOrDefault("displayName", ""),
                    field.getOrDefault("desc", ""),
                    Boolean.TRUE.equals(Boolean.parseBoolean(field.getOrDefault("required", "false")))?"Yes":"No",
                    field.getOrDefault("type", ""),
                    field.getOrDefault("importance", ""),
                    field.containsKey(VALIDATOR_ARG) && !field.get(VALIDATOR_ARG).isEmpty()? "Yes": "No",
                    field.getOrDefault("defaultValue", field.getOrDefault("defaultValueGenerator", "")),
                    field.getOrDefault(ALLOWED_VALUES_ARG, ""),
                    System.lineSeparator()));
            hasValidator = hasValidator || (field.containsKey(VALIDATOR_ARG) && !field.get(VALIDATOR_ARG).trim().isEmpty());
            hasLambdaExpression = hasLambdaExpression || (field.containsKey(LAMBDA_EXP_ARG) && !field.get(LAMBDA_EXP_ARG).trim().isEmpty());
        }
        writer.write("\n\n");
        return new MarkdownGenResult(hasLambdaExpression, hasValidator);
    }

    /**
     * Writes a high-level summary of the document to the provided BufferedWriter.
     * <p>
     * This method generates a structured reference document for managing configuration
     * metadata in Spring Archaius-based applications. It begins by extracting application
     * details from the POM file using the provided {@link DocGenArguments}. The summary
     * includes the application name, a description of the document's purpose, and details
     * about configuration entries such as display name, description, type, importance,
     * and validation rules.
     * <p>
     * The document also highlights the use of lambda expressions for dynamic default values
     * and validator expressions for ensuring data integrity. If the application name is
     * specified in the arguments, it is included in the summary. The Maven coordinates
     * (groupId, artifactId, version) are also documented, or "N/A" if details are unavailable.
     * <p>
     * @param arguments The {@link DocGenArguments} containing directory paths and application name.
     * @param writer The {@link BufferedWriter} to which the document summary is written.
     * @throws IOException If an I/O error occurs during writing.
     */
    private void writeHighLevelSummaryOfDocument(DocGenArguments arguments, BufferedWriter writer) throws IOException {
        Map<String, String> applicationDetails = extractApplicationDetails(arguments);
        writer.write(String.format("## %s%s%s",
                applicationDetails.getOrDefault("name",
                        StringUtils.hasText(arguments.getApplicationName())?
                                arguments.getApplicationName():"Spring Archaius Configuration Details"),
                System.lineSeparator(),
                System.lineSeparator()));
        writer.write(String.format("This document provides a structured reference for managing configuration metadata in Spring Archaius-based applications. It outlines key field configurations, their default values, validation rules, and lambda-based default generators used within the system. The document helps developers and administrators understand how different application settings are structured, validated, and dynamically generated.%s%s", System.lineSeparator(), System.lineSeparator()));
        writer.write(String.format("Each configuration entry includes details such as display name, description, type, importance, and validation rules, ensuring clarity in how these configurations impact the application. Additionally, lambda expressions are used for dynamic default values, while validator expressions ensure data integrity.%s", System.lineSeparator()));
        writer.write(String.format("## Application Details%s", System.lineSeparator()));
        if (StringUtils.hasText(arguments.getApplicationName())) {
            writer.write(String.format("**Application Name**: %s%s%s", arguments.getApplicationName(), System.lineSeparator(), System.lineSeparator()));
        }
        writer.write(String.format("Project Maven coordinates for the application are as follows:%s%s",
                System.lineSeparator(), System.lineSeparator()));
        if (applicationDetails.isEmpty()) {
            writer.write(String.format("N/A.%s%s", System.lineSeparator(), System.lineSeparator()));
            return;
        }
        writer.write(String.format("**Group Id**: `%s`%s%s", applicationDetails.get("groupId"),
                System.lineSeparator(), System.lineSeparator()));
        writer.write(String.format("**Artifact Id**: `%s`%s%s", applicationDetails.get("artifactId"),
                System.lineSeparator(), System.lineSeparator()));
        writer.write(String.format("**Version**: `%s`%s%s%s", applicationDetails.get("version"),
                System.lineSeparator(), System.lineSeparator(), System.lineSeparator()));
    }

}
