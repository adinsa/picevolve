package com.github.adinsa.picevolve.evolution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.FunctionSet;

public class EvolverTest {

    @Test
    public void testInitializePopulationRejectsNonPositiveSize() {

        assertThrows(IllegalArgumentException.class, () -> new Evolver().initializePopulation(0));
        assertThrows(IllegalArgumentException.class, () -> new Evolver().initializePopulation(-1));
    }

    @Test
    public void testInitializePopulationReturnsRequestedSize() {

        final Evolver evolver = new Evolver(new RandomImpl(FunctionSet.createDefault(), new java.util.Random(1)));
        final List<Expression> population = evolver.initializePopulation(5);

        assertEquals(5, population.size());
    }

    @Test
    public void testMutateRejectsNonPositiveSize() {

        final Evolver evolver = new Evolver();
        final Expression expr = evolver.getParser().parse("x");

        assertThrows(IllegalArgumentException.class, () -> evolver.mutate(expr, 0));
        assertThrows(IllegalArgumentException.class, () -> evolver.mutate(expr, -1));
    }

    @Test
    public void testMutateProducesChangedExpressions() {

        final Evolver evolver = new Evolver(new RandomImpl(FunctionSet.createDefault(), new java.util.Random(2)));
        final Expression parent = evolver.getParser().parse("(abs (- x 0.3))");

        final List<Expression> result = evolver.mutate(parent, 10);

        assertEquals(10, result.size(), "mutate should produce the requested population size");
        for (final Expression mutant : result) {
            assertNotEquals(parent.toString(), mutant.toString(), "mutants should differ from their parent");
        }
    }

    @Test
    public void testCrossoverRejectsNonPositiveSize() {

        final Evolver evolver = new Evolver();
        final Expression expr = evolver.getParser().parse("x");

        assertThrows(IllegalArgumentException.class, () -> evolver.crossover(expr, expr, 0));
        assertThrows(IllegalArgumentException.class, () -> evolver.crossover(expr, expr, -1));
    }

    @Test
    public void testCrossoverProducesChangedExpressions() {

        final Evolver evolver = new Evolver(new RandomImpl(FunctionSet.createDefault(), new java.util.Random(3)));
        final Expression mom = evolver.getParser().parse("(abs (- x 0.3))");
        final Expression dad = evolver.getParser().parse("(noise x y 0.5)");

        final List<Expression> result = evolver.crossover(mom, dad, 10);

        assertEquals(10, result.size(), "crossover should produce the requested population size");
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
