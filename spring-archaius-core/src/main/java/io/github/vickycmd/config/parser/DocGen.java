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

@Slf4j
@Service
public class DocGen {

    private static final String OUTPUT_FILE_NAME = "archaius-config-report.md";

    public static void main(String[] args) {
        final String SOURCE_DIRECTORY = "src/main/java/io/github/vickycmd/config/examples"; // Update this path as needed
        final String OUTPUT_FILE = "src/main/resources";

        new DocGen().generateDoc(new DocGenArguments(SOURCE_DIRECTORY, "", OUTPUT_FILE, ""));
    }

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
     * Recursively finds all Java files in a directory.
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
     * Parses a Java file and extracts field metadata.
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
     */
    private void writeMarkdownFile(DocGenArguments arguments, List<Map<String, String>> fields) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(this.getOutputFilePath(arguments))) {
            writeHighLevelSummaryOfDocument(arguments, writer);
            MarkdownGenResult result = writeFieldDetailsToFile(fields, writer);
            writeLambdaExpressionDetailsToFile(fields, result, writer);
            writeValidatorDetailsToTheFile(fields, result, writer);

        }
    }

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
