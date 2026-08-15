package com.github.adinsa.picevolve.expression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry of the {@link Function}s available for building expressions.
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
     * Returns the default function set.
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
}
