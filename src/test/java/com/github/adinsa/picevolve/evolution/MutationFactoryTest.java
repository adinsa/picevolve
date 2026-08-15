package com.github.adinsa.picevolve.evolution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.adinsa.picevolve.expression.ExpressionParser;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.FunctionSet;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;
import com.github.adinsa.picevolve.evolution.Mutation.AdjustScalarMutation;
import com.github.adinsa.picevolve.evolution.Mutation.AdjustVectorMutation;
import com.github.adinsa.picevolve.evolution.Mutation.BecomeArgumentMutation;
import com.github.adinsa.picevolve.evolution.Mutation.BecomeNodeCopyMutation;
import com.github.adinsa.picevolve.evolution.Mutation.ChangeFunctionMutation;
import com.github.adinsa.picevolve.evolution.Mutation.RandomExpressionMutation;
import com.github.adinsa.picevolve.evolution.Mutation.ReplaceWithArgumentMutation;

public class MutationFactoryTest {

    private final ExpressionParser parser = new ExpressionParser(FunctionSet.createDefault());
    private final Random random = Mockito.mock(Random.class);

    @Test
    public void testScalarMutations() {
        final List<MutationFactory.MutationFrequency> frequencies = new MutationFactory(parser, random)
                .getMutationFrequencies(ScalarNode.class);

        assertEquals(4, frequencies.size());
        assertContainsMutationType(frequencies, RandomExpressionMutation.class);
        assertContainsMutationType(frequencies, AdjustScalarMutation.class);
        assertContainsMutationType(frequencies, BecomeArgumentMutation.class);
        assertContainsMutationType(frequencies, BecomeNodeCopyMutation.class);
    }

    @Test
    public void testVectorMutations() {
        final List<MutationFactory.MutationFrequency> frequencies = new MutationFactory(parser, random)
                .getMutationFrequencies(VectorNode.class);

        assertEquals(4, frequencies.size());
        assertContainsMutationType(frequencies, RandomExpressionMutation.class);
        assertContainsMutationType(frequencies, AdjustVectorMutation.class);
        assertContainsMutationType(frequencies, BecomeArgumentMutation.class);
        assertContainsMutationType(frequencies, BecomeNodeCopyMutation.class);
    }

    @Test
    public void testVariableMutations() {
        final List<MutationFactory.MutationFrequency> frequencies = new MutationFactory(parser, random)
                .getMutationFrequencies(VariableNode.class);

        assertEquals(3, frequencies.size());
        assertContainsMutationType(frequencies, RandomExpressionMutation.class);
        assertContainsMutationType(frequencies, BecomeArgumentMutation.class);
        assertContainsMutationType(frequencies, BecomeNodeCopyMutation.class);
    }

    @Test
    public void testFunctionMutations() {
        final List<MutationFactory.MutationFrequency> frequencies = new MutationFactory(parser, random)
                .getMutationFrequencies(Function.class);

        assertEquals(5, frequencies.size());
        assertContainsMutationType(frequencies, RandomExpressionMutation.class);
        assertContainsMutationType(frequencies, ChangeFunctionMutation.class);
        assertContainsMutationType(frequencies, ReplaceWithArgumentMutation.class);
        assertContainsMutationType(frequencies, BecomeArgumentMutation.class);
        assertContainsMutationType(frequencies, BecomeNodeCopyMutation.class);
    }

    @Test
    public void testGetMutationFrequenciesReturnsUnmodifiableList() {
        final List<MutationFactory.MutationFrequency> frequencies = new MutationFactory(parser, random)
                .getMutationFrequencies(ScalarNode.class);

        assertThrows(UnsupportedOperationException.class, () -> frequencies.add(frequencies.get(0)));
        assertThrows(UnsupportedOperationException.class, () -> frequencies.remove(0));
    }

    @Test
    public void testMutationFrequencyDefaultsToOne() {
        final Mutation mutation = new Mutation.RandomExpressionMutation(random, parser);
        final MutationFactory.MutationFrequency frequency = new MutationFactory.MutationFrequency(mutation);

        assertEquals(1, frequency.getRelativeFrequency());
        assertSame(mutation, frequency.getMutation());
    }

    @Test
    public void testMutationFrequencyWithExplicitValue() {
        final Mutation mutation = new Mutation.AdjustScalarMutation(random, parser);
        final MutationFactory.MutationFrequency frequency = new MutationFactory.MutationFrequency(mutation, 7);

        assertEquals(7, frequency.getRelativeFrequency());
        assertSame(mutation, frequency.getMutation());
    }

    private void assertContainsMutationType(final List<MutationFactory.MutationFrequency> frequencies, final Class<?> mutationType) {
        assertTrue(frequencies.stream().anyMatch(freq -> mutationType.isInstance(freq.getMutation())),
                "Expected a mutation of type " + mutationType.getSimpleName());
    }
}
