package com.github.adinsa.picevolve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal;
import com.github.adinsa.picevolve.expression.Variable;

/**
 * Registry of the {@link Function}s available to expressions, together with the parser that builds expression trees from s-expressions.
 *
 * @author amar
 *
 */
public class FunctionSet {

    private final Map<String, Function> functionMap = new HashMap<>();

    public void add(final Function function) {
        if (functionMap.containsKey(function.getName())) {
            throw new IllegalArgumentException(String.format("Function with name '%s' already exists", function.getName()));
        }
        functionMap.put(function.getName(), function);
    }

    public Optional<Function> get(final String name) {
        return Optional.ofNullable(functionMap.get(name)).map(function -> function.copy());
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(functionMap.keySet());
    }

    /**
     * Returns the default function set used by PicEvolve.
     *
     * @return
     */
    public static FunctionSet createDefault() {

        final FunctionSet functions = new FunctionSet();

        // @formatter:off
        functions.add(new Function.Plus());
        functions.add(new Function.Minus());
        functions.add(new Function.Multiply());
        functions.add(new Function.Divide());
        functions.add(new Function.Round());
        functions.add(new Function.Expt());
        functions.add(new Function.Log());
        functions.add(new Function.Sine());
        functions.add(new Function.Cosine());
        functions.add(new Function.Tangent());
        functions.add(new Function.Min());
        functions.add(new Function.Max());
        functions.add(new Function.Abs());
        functions.add(new Function.Mod());
        functions.add(new Function.IntAnd());
        functions.add(new Function.IntOr());
        functions.add(new Function.IntXor());
        functions.add(new Function.FloatAnd());
        functions.add(new Function.FloatOr());
        functions.add(new Function.FloatXor());
        functions.add(new Function.Noise());
        functions.add(new Function.WarpedNoise());
        functions.add(new Function.Blur());
        functions.add(new Function.Sharpen());
        functions.add(new Function.Emboss());
        // @formatter:on

        return functions;
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
            final Optional<Function> function = get(token);
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
