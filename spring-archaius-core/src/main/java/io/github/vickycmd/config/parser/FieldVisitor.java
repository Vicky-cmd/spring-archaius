package io.github.vickycmd.config.parser;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.vickycmd.config.parser.Config.*;

/**
 * A visitor class that extends JavaParser's VoidVisitorAdapter to extract details from
 * method call expressions related to `Field.builder()` invocations. This class is designed
 * to traverse the Abstract Syntax Tree (AST) of Java source code and collect information
 * about field configurations specified using a builder pattern.
 *
 * <p>The primary functionality of this class is to identify method call chains that
 * culminate in a `build()` method call, starting from `Field.builder()`. It extracts
 * relevant data from these method calls, such as argument values and lambda expressions,
 * and stores them in a list of maps for further processing.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Extracting argument values from method calls, including handling of string literals,
 *   name expressions, field access expressions, array initializers, and lambda expressions.</li>
 *   <li>Identifying and processing specific method calls like `allowedValues`, `validator`,
 *   and `required` to capture their argument details.</li>
 *   <li>Maintaining the order of method calls using a LinkedHashMap to ensure the sequence
 *   of field configurations is preserved.</li>
 * </ul>
 *
 * <p>This class is part of the `io.github.vickycmd.config.parser` package and relies on
 * constants defined in the `Config` class for argument keys and values.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * List<Map<String, String>> fieldDataList = new ArrayList<>();
 * FieldVisitor visitor = new FieldVisitor(fieldDataList);
 * // Assume `methodCallExpr` is a parsed MethodCallExpr from JavaParser
 * visitor.visit(methodCallExpr, null);
 * // fieldDataList now contains extracted field configuration details
 * }
 * </pre>
 *
 * <p>Related Classes:</p>
 * <ul>
 *   <li>{@link io.github.vickycmd.config.parser.Config}</li>
 * </ul>
 *
 * <p>Author: Vicky CMD</p>
 * <p>Version: 1.0</p>
 * <p>Since: 1.0</p>
 */
public class FieldVisitor extends VoidVisitorAdapter<Void> {
    private final List<Map<String, String>> fieldDataList;

    /**
     * Constructs a FieldVisitor instance with a list to store extracted field data.
     *
     * @param fieldDataList the list to store extracted field data
     */
    public FieldVisitor(List<Map<String, String>> fieldDataList) {
        this.fieldDataList = fieldDataList;
    }

    /**
     * Visits a method call expression node in the AST and extracts field details
     * from method call chains that culminate in a `build()` method call. This method
     * is part of the traversal process and is invoked for each method call expression
     * encountered in the AST.
     *
     * <p>The method first attempts to extract field configuration details from the
     * provided method call expression using the `extractFieldDetails` method. It then
     * delegates to the superclass's `visit` method to continue the traversal process.
     *
     * <p>This method is specifically designed to work with method call expressions
     * related to field configurations using a builder pattern, as defined in the
     * `io.github.vickycmd.config.parser` package.</p>
     *
     * <p>Related Classes:</p>
     * <ul>
     *   <li>{@link io.github.vickycmd.config.parser.Config}</li>
     * </ul>
     *
     * @param m the method call expression to visit
     * @param arg an optional argument, not used in this implementation
     * @see #extractFieldDetails(MethodCallExpr)
     */
    @Override
    public void visit(MethodCallExpr m, Void arg) {
        this.extractFieldDetails(m);
        super.visit(m, arg);
    }

    /**
     * Extracts field configuration details from a method call expression that ends with a `build()` call.
     * This method initiates the extraction process by verifying if the provided method call expression
     * represents a `build()` invocation. If so, it creates a LinkedHashMap to maintain the order of
     * extracted field data and traverses up the method call chain to identify the originating `Field.builder()`
     * call. During this traversal, it collects relevant argument data from each method call in the chain
     * using the `extractDataForArgument` method.
     *
     * <p>The method is designed to work with method call chains that utilize a builder pattern for field
     * configurations, as defined in the `io.github.vickycmd.config.parser` package. It ensures that
     * the extracted field data is stored in the provided list for further processing.</p>
     *
     * <p>Related Classes:</p>
     * <ul>
     *   <li>{@link io.github.vickycmd.config.parser.Config}</li>
     * </ul>
     *
     * @param m the method call expression to process, expected to end with a `build()` call
     * @see #extractDataForArgument(MethodCallExpr, Map)
     */
    private void extractFieldDetails(MethodCallExpr m) {
        if (!m.getNameAsString().equals("build")) return;

        // Start processing from `.build()`
        Map<String, String> fieldData = new LinkedHashMap<>(); // Maintain order

        // Walk up the chain to find `Field.builder()`
        MethodCallExpr current = m;
        while (current.getScope().isPresent() && current.getScope().get().isMethodCallExpr()) {
            MethodCallExpr parent = current.getScope().get().asMethodCallExpr();
            if (extractDataForArgument(parent, fieldData)) break;

            // Move up the chain
            current = parent;
        }

    }

    /**
     * Extracts data from a method call expression and populates the provided field data map.
     *
     * <p>This method processes a given {@link MethodCallExpr} to extract relevant argument data
     * and store it in the provided {@link Map} of field data. It handles various argument types,
     * including lambda expressions, allowed values, and validators, and adds them to the map
     * with appropriate keys. If the method call is identified as `Field.builder()`, the field
     * data map is added to the {@code fieldDataList} and the method returns {@code true} to
     * indicate completion.</p>
     *
     * <p>Usage involves passing a method call expression and a map to store extracted data.
     * The method checks for specific argument keys and values, such as {@code LAMBDA_EXP_ARG},
     * {@code ALLOWED_VALUES_ARG}, and {@code VALIDATOR_ARG}, and processes them accordingly.
     * It also handles the special case where the key is "required" with no arguments, setting
     * its value to "true".</p>
     *
     * @param parent The method call expression from which to extract data.
     * @param fieldData The map to populate with extracted field data.
     * @return {@code true} if the method call is `Field.builder()`, indicating the end of processing;
     *         {@code false} otherwise.
     *
     * @see #extractValue(Expression)
     * @see #extractLambdaContent(Expression)
     * @see #isFieldBuilder(MethodCallExpr)
     */
    private boolean extractDataForArgument(MethodCallExpr parent, Map<String, String> fieldData) {
        String key = parent.getNameAsString();

        // Extract argument value if present
        if (parent.getArguments().size() == 1) {
            String value = extractValue(parent.getArgument(0));
            if (value.equals(LAMBDA_EXP_ARG_VALUE)) {
                fieldData.put(LAMBDA_EXP_ARG, this.extractLambdaContent(parent.getArgument(0)));
            }
            fieldData.put(key, value);
        } else if (ALLOWED_VALUES_ARG.equals(key) || VALIDATOR_ARG.equals(key)) {
            fieldData.put(key, parent.getArguments().stream().map(this::extractValue).collect(Collectors.joining(", ")));
        } else if ("required".equals(key) && parent.getArguments().isEmpty()) {
            fieldData.put(key, "true");
        }

        // Stop if we've reached `Field.builder()`
        if (isFieldBuilder(parent)) {
            fieldDataList.add(fieldData);
            return true;
        }
        return false;
    }

    /**
     * Extracts the content of a lambda expression from the given expression argument.
     *
     * <p>This method checks if the provided {@link Expression} is a lambda expression
     * and whether it contains a body. If the body is a block statement, it returns the
     * string representation of the block. Otherwise, it returns the string representation
     * of the entire argument.</p>
     *
     * <p>Usage involves passing an expression that may represent a lambda, and the method
     * will return the relevant content or an empty string if the expression is not a valid
     * lambda or lacks a body.</p>
     *
     * <p>Related Classes:</p>
     * <ul>
     *   <li>{@link io.github.vickycmd.config.parser.FieldVisitor}</li>
     * </ul>
     *
     * @param argument The expression to evaluate for lambda content.
     * @return A string representing the lambda's body content, or an empty string if not applicable.
     */
    private String extractLambdaContent(Expression argument) {
        if (!argument.isLambdaExpr() || argument.asLambdaExpr().getBody() == null) return "";
        else if (argument.asLambdaExpr().getBody().isBlockStmt()) {
            return argument.asLambdaExpr().getBody().toString();
        }
        return argument.toString();
    }

    /**
     * Determines if the given method call expression represents a 'builder' method
     * associated with a 'Field' scope.
     *
     * @param methodCall the method call expression to evaluate
     * @return true if the method call is named 'builder' and is associated with a 'Field' scope, false otherwise
     */
    private boolean isFieldBuilder(MethodCallExpr methodCall) {
        return methodCall.getNameAsString().equals("builder") &&
                this.isFieldMethod(methodCall);
    }

    /**
     * Determines if the given method call expression is a method of the "Field" class.
     *
     * @param methodCall the method call expression to check
     * @return true if the method call is scoped to "Field", false otherwise
     */
    private boolean isFieldMethod(MethodCallExpr methodCall) {
        return methodCall.getScope().isPresent() &&
                methodCall.getScope().get().toString().equals("Field");
    }

    /**
     * Extracts a string representation of the given expression based on its type.
     * This method handles various types of expressions, including string literals,
     * name expressions (constants), field access expressions (enums), array initializers,
     * lambda expressions, and specific method calls related to field validation.
     *
     * @param argExpr the expression to extract a value from
     * @return a string representation of the expression, or a specific constant
     *         for lambda expressions, or the result of extracting validator values
     *         for certain method calls
     */
    private String extractValue(Expression argExpr) {
        if (argExpr.isStringLiteralExpr()) {
            return argExpr.asStringLiteralExpr().asString();
        } else if (argExpr.isNameExpr()) {
            return argExpr.asNameExpr().getNameAsString(); // Handles constants
        } else if (argExpr.isFieldAccessExpr()) {
            return argExpr.asFieldAccessExpr().getNameAsString(); // Handles enums like Field.Type.STRING
        } else if (argExpr.isArrayInitializerExpr()) {
            return argExpr.asArrayInitializerExpr().getValues().toString();
        } else if (argExpr.isLambdaExpr()) {
            return LAMBDA_EXP_ARG_VALUE;
        } else if (argExpr.isMethodCallExpr()
                && this.isFieldMethod(argExpr.asMethodCallExpr())
                && "createValidator".equals(argExpr.asMethodCallExpr().getNameAsString())) {
            return this.extractValidatorValues(argExpr.asMethodCallExpr());
        }
        return argExpr.toString(); // Fallback case
    }

    /**
     * Extracts validator values from a method call expression.
     * This method is specifically designed to handle method calls related to field validation,
     * such as "createValidator" and "withValidator".
     *
     * @param methodCallExpr the method call expression to extract validator values from
     * @return a string representation of the validator values
     */
    private String extractValidatorValues(MethodCallExpr methodCallExpr) {
        return methodCallExpr.getArguments().stream()
                .map(this::extractValue)
                .collect(Collectors.joining(", "));
    }
}
