package com.github.adinsa.picevolve.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class FunctionSetTest {

    @Test
    public void testCreateDefaultContainsExpectedFunctions() {
        final FunctionSet functions = FunctionSet.createDefault();

        final Set<String> names = functions.names();
        assertEquals(25, names.size());

        assertTrue(names.contains("+"));
        assertTrue(names.contains("-"));
        assertTrue(names.contains("*"));
        assertTrue(names.contains("/"));
        assertTrue(names.contains("abs"));
        assertTrue(names.contains("min"));
        assertTrue(names.contains("max"));
        assertTrue(names.contains("noise"));
        assertTrue(names.contains("warped-noise"));
        assertTrue(names.contains("blur"));
        assertTrue(names.contains("sharpen"));
        assertTrue(names.contains("emboss"));
        assertTrue(names.contains("sin"));
        assertTrue(names.contains("cos"));
        assertTrue(names.contains("tan"));
        assertTrue(names.contains("log"));
        assertTrue(names.contains("expt"));
        assertTrue(names.contains("round"));
        assertTrue(names.contains("mod"));
        assertTrue(names.contains("int-and"));
        assertTrue(names.contains("int-or"));
        assertTrue(names.contains("int-xor"));
        assertTrue(names.contains("float-and"));
        assertTrue(names.contains("float-or"));
        assertTrue(names.contains("float-xor"));
    }

    @Test
    public void testGetReturnsCopy() {
        final FunctionSet functions = FunctionSet.createDefault();

        final Function plus = functions.get("+").get();
        assertNotNull(plus);
        assertEquals("+", plus.getName());
        assertEquals(2, plus.getArity());

        final Function plusAgain = functions.get("+").get();
        assertNotSame(plus, plusAgain, "get() should return a fresh copy each time");
    }

    @Test
    public void testGetUnknownReturnsEmpty() {
        final FunctionSet functions = FunctionSet.createDefault();

        assertEquals(Optional.empty(), functions.get("does-not-exist"));
    }

    @Test
    public void testAddDuplicateThrows() {
        final FunctionSet functions = new FunctionSet();
        functions.add(new Function.Plus());

        assertThrows(IllegalArgumentException.class, () -> functions.add(new Function.Plus()));
    }

    @Test
    public void testAddAndGetRoundTrip() {
        final FunctionSet functions = new FunctionSet();
        functions.add(new Function.Abs());

        final Function abs = functions.get("abs").get();
        assertEquals("abs", abs.getName());
        assertEquals(1, abs.getArity());
    }

    @Test
    public void testNamesReturnsUnmodifiableSet() {
        final FunctionSet functions = new FunctionSet();
        functions.add(new Function.Plus());

        final Set<String> names = functions.names();
        assertThrows(UnsupportedOperationException.class, () -> names.add("nope"));
        assertThrows(UnsupportedOperationException.class, () -> names.remove("+"));
    }

    @Test
    public void testNamesIsEmptyInitially() {
        final FunctionSet functions = new FunctionSet();
        assertTrue(functions.names().isEmpty());
        assertFalse(functions.get("+").isPresent());
    }
}
