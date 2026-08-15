package com.github.adinsa.picevolve.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class VariableTest {

    @Test
    public void testFromStringLowerCase() {
        assertEquals(Variable.X, Variable.fromString("x").get());
        assertEquals(Variable.Y, Variable.fromString("y").get());
    }

    @Test
    public void testFromStringUpperCase() {
        assertEquals(Variable.X, Variable.fromString("X").get());
        assertEquals(Variable.Y, Variable.fromString("Y").get());
    }

    @Test
    public void testFromStringInvalidReturnsEmpty() {
        assertFalse(Variable.fromString("z").isPresent());
        assertFalse(Variable.fromString("").isPresent());
        assertFalse(Variable.fromString("xx").isPresent());
    }
}
