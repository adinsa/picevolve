package com.github.adinsa.picevolve.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.image.Image;

public class TerminalTest {

    @Test
    public void testScalarNodeInterpret() {
        final Image image = new Terminal.ScalarNode(0.5).interpret(2, 2, List.of());

        assertEquals(0.5, image.get(0, 0).r(), 0);
        assertEquals(0.5, image.get(1, 1).g(), 0);
    }

    @Test
    public void testVectorNodeInterpret() {
        final Image image = new Terminal.VectorNode(Arrays.asList(0.1, 0.2, 0.3)).interpret(1, 1, List.of());

        assertEquals(0.1, image.get(0, 0).r(), 0);
        assertEquals(0.2, image.get(0, 0).g(), 0);
        assertEquals(0.3, image.get(0, 0).b(), 0);
    }

    @Test
    public void testVariableNodeInterpret() {
        final Image image = new Terminal.VariableNode(Variable.X).interpret(3, 1, List.of());

        assertEquals(-1.0, image.get(0, 0).r(), 1e-9);
        assertEquals(0.0, image.get(1, 0).r(), 1e-9);
        assertEquals(1.0, image.get(2, 0).r(), 1e-9);
    }
}
