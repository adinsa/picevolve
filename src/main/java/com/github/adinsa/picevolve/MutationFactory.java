package com.github.adinsa.picevolve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.adinsa.picevolve.Mutation.AdjustScalarMutation;
import com.github.adinsa.picevolve.Mutation.AdjustVectorMutation;
import com.github.adinsa.picevolve.Mutation.BecomeArgumentMutation;
import com.github.adinsa.picevolve.Mutation.BecomeNodeCopyMutation;
import com.github.adinsa.picevolve.Mutation.ChangeFunctionMutation;
import com.github.adinsa.picevolve.Mutation.RandomExpressionMutation;
import com.github.adinsa.picevolve.Mutation.ReplaceWithArgumentMutation;
import com.github.adinsa.picevolve.Terminal.ScalarNode;
import com.github.adinsa.picevolve.Terminal.VariableNode;
import com.github.adinsa.picevolve.Terminal.VectorNode;

/**
 * Defines which {@link Mutation} implementations may be applied to each type of
 * {@link Expression} node and the relative frequencies with which they are to
 * be applied
 *
 * @author amar
 *
 */
public class MutationFactory {

    private final Map<Class<? extends Expression>, MutationFrequency[]> mutationFrequencyMap = new HashMap<>();

    public MutationFactory(final Random random) {

        this.setMutationFrequencies(ScalarNode.class,
                new MutationFrequency(new RandomExpressionMutation(random)),
                new MutationFrequency(new AdjustScalarMutation(random)),
                new MutationFrequency(new BecomeArgumentMutation(random)),
                new MutationFrequency(new BecomeNodeCopyMutation(random)));
        this.setMutationFrequencies(VectorNode.class,
                new MutationFrequency(new RandomExpressionMutation(random)),
                new MutationFrequency(new AdjustVectorMutation(random)),
                new MutationFrequency(new BecomeArgumentMutation(random)),
                new MutationFrequency(new BecomeNodeCopyMutation(random)));
        this.setMutationFrequencies(VariableNode.class,
                new MutationFrequency(new RandomExpressionMutation(random)),
                new MutationFrequency(new BecomeArgumentMutation(random)),
                new MutationFrequency(new BecomeNodeCopyMutation(random)));
        this.setMutationFrequencies(Function.class,
                new MutationFrequency(new RandomExpressionMutation(random)),
                new MutationFrequency(new ChangeFunctionMutation(random)),
                new MutationFrequency(new ReplaceWithArgumentMutation(random)),
                new MutationFrequency(new BecomeArgumentMutation(random)),
                new MutationFrequency(new BecomeNodeCopyMutation(random)));
    }

    /**
     * Returns {@link Mutation}s that may be applied to the given type of
     * {@link Expression} node paired with the relative frequencies with which
     * they should be applied.
     *
     * @param nodeType
     * @return
     */
    public List<MutationFrequency> getMutationFrequencies(
            final Class<? extends Expression> nodeType) {
        return Collections.unmodifiableList(new ArrayList<>(
                Arrays.asList(this.mutationFrequencyMap.get(nodeType))));
    }

    private final void setMutationFrequencies(
            final Class<? extends Expression> nodeType,
            final MutationFrequency... frequencies) {
        this.mutationFrequencyMap.put(nodeType, frequencies);
    }

    /**
     * A {@link Mutation} instance paired with its relative frequency
     */
    public static class MutationFrequency {

        private static final int DEFAULT_RELATIVE_FREQUENCY = 1;

        private final int relativeFrequency;
        private final Mutation mutation;

        public MutationFrequency(final Mutation mutation) {
            this(mutation, DEFAULT_RELATIVE_FREQUENCY);
        }

        public MutationFrequency(final Mutation mutation,
                final int relativeFrequency) {
            this.mutation = mutation;
            this.relativeFrequency = relativeFrequency;
        }

        public int getRelativeFrequency() {
            return this.relativeFrequency;
        }

        public Mutation getMutation() {
            return this.mutation;
        }
    }
}
