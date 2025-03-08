package io.github.vickycmd.config.parser.model;

/**
 * Represents the result of generating a markdown document.
 *
 * @param hasLambdaExpression Indicates if the markdown contains a lambda expression.
 * @param hasValidator Indicates if the markdown includes a validator.
 */
public record MarkdownGenResult(boolean hasLambdaExpression, boolean hasValidator) {
}
