package com.github.adinsa.picevolve.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

/**
 * Parses s-expression strings into {@link Expression} trees using the {@link FunctionSet} to resolve function names.
 *
 * @author amar
 *
 */
public class ExpressionParser {

    private final FunctionSet functions;

    public ExpressionParser(final FunctionSet functions) {
        this.functions = functions;
    }

    /**
     * Parse the input s-expression string and return the AST as an {@link Expression}.
     *
     * @param expressionString
     * @return
     */
    public Expression parse(final String expressionString) {

        if (expressionString == null || expressionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot parse empty expression");
        }
        validateBalancedParentheses(expressionString);

        final Stack<Expression> exprStack = new Stack<>();

        final List<String> tokens = new ArrayList<>(
                Arrays.asList(expressionString.replace("(", "").replace(")", "").replaceAll("\\s{2,}", " ").trim().split(" ")));

        Collections.reverse(tokens);

        for (final String token : tokens) {
            final Optional<Function> function = functions.get(token);
            if (function.isPresent()) {
                final Function func = function.get();
                final List<Expression> children = new ArrayList<>(func.getArity());
                for (int i = 0; i < func.getArity(); i++) {
                    if (exprStack.isEmpty()) {
                        throw new IllegalArgumentException(
                                String.format("Not enough arguments for function '%s' in: %s", token, expressionString));
                    }
                    children.add(exprStack.pop());
                }
                func.setChildren(children);
                exprStack.push(func);
            } else if (Variable.fromString(token).isPresent()) {
                exprStack.push(new Terminal.VariableNode(Variable.fromString(token).get()));
            } else if (token.startsWith("#")) {
                exprStack.push(new Terminal.VectorNode(parseVector(token)));
            } else {
                try {
                    final double val = Double.valueOf(token);
                    exprStack.push(new Terminal.ScalarNode(val));
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("Invalid token: '%s'", token));
                }
            }
        }

        if (exprStack.size() != 1) {
            throw new IllegalArgumentException(
                    String.format("Expression must have a single root node (found %d): %s", exprStack.size(), expressionString));
        }
        return exprStack.pop();
    }

    private List<Double> parseVector(final String token) {
        final String[] vecParts = token.replace("#", "").split(",");
        if (vecParts.length != 3) {
            throw new IllegalArgumentException(String.format("Vector must have exactly 3 components: '%s'", token));
        }
        return new ArrayList<>(
                Arrays.asList(Double.valueOf(vecParts[0]), Double.valueOf(vecParts[1]), Double.valueOf(vecParts[2])));
    }

    private void validateBalancedParentheses(final String expression) {
        int depth = 0;
        for (final char c : expression.toCharArray()) {
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth < 0) {
                    throw new IllegalArgumentException("Unbalanced parentheses in: " + expression);
                }
            }
        }
        if (depth != 0) {
            throw new IllegalArgumentException("Unbalanced parentheses in: " + expression);
        }
    }
}
