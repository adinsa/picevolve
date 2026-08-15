package com.github.adinsa.picevolve.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;

public class ExpressionTest {

    @Test
    public void testGetParentIsNullForRoot() {
        final Expression expr = new ScalarNode(1.0);
        assertNull(expr.getParent());
    }

    @Test
    public void testDefaultHeightIsOne() {
        assertEquals(1, new ScalarNode(1.0).getHeight());
    }

    @Test
    public void testSetAndGetHeight() {
        final Expression expr = new ScalarNode(1.0);
        expr.setHeight(5);
        assertEquals(5, expr.getHeight());
    }

    @Test
    public void testGetChildrenIsEmptyWhenUnset() {
        final Expression expr = new ScalarNode(1.0);
        assertNotNull(expr.getChildren());
        assertTrue(expr.getChildren().isEmpty());
    }

    @Test
    public void testSetChildrenAssignsParentPointers() {
        final Function plus = new Function.Plus();
        final ScalarNode left = new ScalarNode(1.0);
        final ScalarNode right = new ScalarNode(2.0);

        plus.setChildren(new ArrayList<>(List.of(left, right)));

        assertSame(plus, left.getParent());
        assertSame(plus, right.getParent());
        assertEquals(2, plus.getChildren().size());
    }

    @Test
    public void testReplaceWithSwapsNodeInParent() {
        final Function plus = new Function.Plus();
        final ScalarNode left = new ScalarNode(1.0);
        final ScalarNode right = new ScalarNode(2.0);
        plus.setChildren(new ArrayList<>(List.of(left, right)));

        final ScalarNode replacement = new ScalarNode(99.0);
        left.replaceWith(replacement);

        assertEquals(2, plus.getChildren().size());
        assertSame(replacement, plus.getChildren().get(0));
        assertSame(right, plus.getChildren().get(1));
        assertSame(plus, replacement.getParent());
    }

    @Test
    public void testReplaceWithOnRootThrows() {
        final Expression root = new ScalarNode(1.0);
        assertThrows(IllegalStateException.class, () -> root.replaceWith(new ScalarNode(2.0)));
    }
}
