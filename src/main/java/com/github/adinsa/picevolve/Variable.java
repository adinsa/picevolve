package com.github.adinsa.picevolve;

import java.util.Arrays;
import java.util.Optional;

/**
 * Cartesian pixel coordinates
 *
 * @author amar
 *
 */
public enum Variable {

    X,
    Y;

    public static Optional<Variable> fromString(final String token) {
        return Arrays.stream(Variable.values())
                .filter(var -> var.toString().equalsIgnoreCase(token))
                .findAny();
    }
}