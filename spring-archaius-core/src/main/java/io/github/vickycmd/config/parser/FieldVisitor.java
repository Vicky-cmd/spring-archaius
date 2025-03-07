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
 * JavaParser Visitor to extract `Field.builder()` details.
 */
public class FieldVisitor extends VoidVisitorAdapter<Void> {
    private final List<Map<String, String>> fieldDataList;

    public FieldVisitor(List<Map<String, String>> fieldDataList) {
        this.fieldDataList = fieldDataList;
    }

    @Override
    public void visit(MethodCallExpr m, Void arg) {
        this.extractFieldDetails(m);
        super.visit(m, arg);
    }

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

    private String extractLambdaContent(Expression argument) {
        if (!argument.isLambdaExpr() || argument.asLambdaExpr().getBody() == null) return "";
        else if (argument.asLambdaExpr().getBody().isBlockStmt()) {
            return argument.asLambdaExpr().getBody().toString();
        }
        return argument.toString();
    }

    /**
     * Checks if the method call is `Field.builder()`
     */
    private boolean isFieldBuilder(MethodCallExpr methodCall) {
        return methodCall.getNameAsString().equals("builder") &&
                this.isFieldMethod(methodCall);
    }

    private boolean isFieldMethod(MethodCallExpr methodCall) {
        return methodCall.getScope().isPresent() &&
                methodCall.getScope().get().toString().equals("Field");
    }

    /**
     * Extracts actual values from method arguments.
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

    private String extractValidatorValues(MethodCallExpr methodCallExpr) {
        return methodCallExpr.getArguments().stream()
                .map(this::extractValue)
                .collect(Collectors.joining(", "));
    }
}
