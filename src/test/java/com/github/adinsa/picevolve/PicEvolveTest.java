package com.github.adinsa.picevolve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;
import com.github.adinsa.picevolve.expression.Variable;
import com.github.adinsa.picevolve.random.Random;

public class PicEvolveTest {

    @Test
    public void testParseExpression() {

        final PicEvolve picEvolve = new PicEvolve();

        final Expression expr = picEvolve.parse("(abs (- x 0.3))");

        assertNull(expr.getParent(), "Root node's parent should be null");

        assertEquals("abs", ((Function) expr).getName());

        final Function minus = (Function) expr.getChildren().get(0);

        assertEquals("-", minus.getName());

        assertTrue(minus.getParent() == expr);

        final VariableNode x = (VariableNode) minus.getChildren().get(0);
        final ScalarNode scalar = (ScalarNode) minus.getChildren().get(1);

        assertEquals(Variable.X, x.getValue());
        assertEquals(0.3, scalar.getValue(), 0);
    }

    @Test
    public void testVectorToStringRoundTripsPrecisely() {

        final PicEvolve picEvolve = new PicEvolve();

        final List<Double> values = Arrays.asList(0.123456789012345, 0.987654321098765, 0.555555555555555);
        final VectorNode vectorNode = new VectorNode(new ArrayList<>(values));

        final Expression parsed = picEvolve.parse(vectorNode.toString());

        final VectorNode result = (VectorNode) parsed;
        assertEquals(values.get(0), result.getValue().get(0), 0);
        assertEquals(values.get(1), result.getValue().get(1), 0);
        assertEquals(values.get(2), result.getValue().get(2), 0);
    }

    @Test
    public void testInitializePopulationRejectsNonPositiveSize() {

        assertThrows(IllegalArgumentException.class, () -> new PicEvolve().initializePopulation(0));
        assertThrows(IllegalArgumentException.class, () -> new PicEvolve().initializePopulation(-1));
    }

    @Test
    public void testMutateRejectsNonPositiveSize() {

        final PicEvolve picEvolve = new PicEvolve();
        final Expression expr = picEvolve.parse("x");

        assertThrows(IllegalArgumentException.class, () -> picEvolve.mutate(expr, 0));
        assertThrows(IllegalArgumentException.class, () -> picEvolve.mutate(expr, -1));
    }

    @Test
    public void testCrossoverRejectsNonPositiveSize() {

        final PicEvolve picEvolve = new PicEvolve();
        final Expression expr = picEvolve.parse("x");

        assertThrows(IllegalArgumentException.class, () -> picEvolve.crossover(expr, expr, 0));
        assertThrows(IllegalArgumentException.class, () -> picEvolve.crossover(expr, expr, -1));
    }

    @Test
    public void testMutateTerminatesWhenNoChangeIsPossible() {

        final Random random = mock(Random.class);
        when(random.shouldMutate(any(), anyDouble())).thenReturn(false);

        final PicEvolve picEvolve = new PicEvolve(random);
        final Expression expr = picEvolve.parse("x");

        final List<Expression> result = picEvolve.mutate(expr, 5);

        assertTrue(result.size() < 5, "Mutate must terminate even when no mutation changes the expression");
    }

    @Test
    public void testCrossoverTerminatesWhenNoChangeIsPossible() {

        final Random random = mock(Random.class);
        final PicEvolve picEvolve = new PicEvolve(random);

        final Expression expr = picEvolve.parse("x");

        when(random.nextNode(any())).thenAnswer(invocation -> (Expression) invocation.getArgument(0));

        final List<Expression> result = picEvolve.crossover(expr, expr, 5);

        assertTrue(result.size() < 5, "Crossover must terminate even when no change occurs");
    }
}