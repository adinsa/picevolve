package com.github.adinsa.picevolve.evolution;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.expression.Expression;

public class EvolverTest {

    @Test
    public void testInitializePopulationRejectsNonPositiveSize() {

        assertThrows(IllegalArgumentException.class, () -> new Evolver().initializePopulation(0));
        assertThrows(IllegalArgumentException.class, () -> new Evolver().initializePopulation(-1));
    }

    @Test
    public void testMutateRejectsNonPositiveSize() {

        final Evolver evolver = new Evolver();
        final Expression expr = evolver.getParser().parse("x");

        assertThrows(IllegalArgumentException.class, () -> evolver.mutate(expr, 0));
        assertThrows(IllegalArgumentException.class, () -> evolver.mutate(expr, -1));
    }

    @Test
    public void testCrossoverRejectsNonPositiveSize() {

        final Evolver evolver = new Evolver();
        final Expression expr = evolver.getParser().parse("x");

        assertThrows(IllegalArgumentException.class, () -> evolver.crossover(expr, expr, 0));
        assertThrows(IllegalArgumentException.class, () -> evolver.crossover(expr, expr, -1));
    }

    @Test
    public void testMutateTerminatesWhenNoChangeIsPossible() {

        final Random random = mock(Random.class);
        when(random.shouldMutate(any(), anyDouble())).thenReturn(false);

        final Evolver evolver = new Evolver(random);
        final Expression expr = evolver.getParser().parse("x");

        final List<Expression> result = evolver.mutate(expr, 5);

        assertTrue(result.size() < 5, "Mutate must terminate even when no mutation changes the expression");
    }

    @Test
    public void testCrossoverTerminatesWhenNoChangeIsPossible() {

        final Random random = mock(Random.class);
        final Evolver evolver = new Evolver(random);

        final Expression expr = evolver.getParser().parse("x");

        when(random.nextNode(any())).thenAnswer(invocation -> (Expression) invocation.getArgument(0));

        final List<Expression> result = evolver.crossover(expr, expr, 5);

        assertTrue(result.size() < 5, "Crossover must terminate even when no change occurs");
    }
}
