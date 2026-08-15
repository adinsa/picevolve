package com.github.adinsa.picevolve.evolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.ExpressionParser;
import com.github.adinsa.picevolve.expression.Function;
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

/**
 * Defines which {@link Mutation} implementations may be applied to each type of {@link Expression} node and the relative frequencies with which they
 * are to be applied
 *
 * @author amar
 *
 */
public class MutationFactory {

    private final Map<Class<? extends Expression>, MutationFrequency[]> mutationFrequencyMap = new HashMap<>();

    public MutationFactory(final ExpressionParser parser, final Random random) {

        setMutationFrequencies(ScalarNode.class, new MutationFrequency(new RandomExpressionMutation(random, parser)),
                new MutationFrequency(new AdjustScalarMutation(random, parser)), new MutationFrequency(new BecomeArgumentMutation(random, parser)),
                new MutationFrequency(new BecomeNodeCopyMutation(random, parser)));
        setMutationFrequencies(VectorNode.class, new MutationFrequency(new RandomExpressionMutation(random, parser)),
                new MutationFrequency(new AdjustVectorMutation(random, parser)), new MutationFrequency(new BecomeArgumentMutation(random, parser)),
                new MutationFrequency(new BecomeNodeCopyMutation(random, parser)));
        setMutationFrequencies(VariableNode.class, new MutationFrequency(new RandomExpressionMutation(random, parser)),
                new MutationFrequency(new BecomeArgumentMutation(random, parser)), new MutationFrequency(new BecomeNodeCopyMutation(random, parser)));
        setMutationFrequencies(Function.class, new MutationFrequency(new RandomExpressionMutation(random, parser)),
                new MutationFrequency(new ChangeFunctionMutation(random, parser)), new MutationFrequency(new ReplaceWithArgumentMutation(random, parser)),
                new MutationFrequency(new BecomeArgumentMutation(random, parser)), new MutationFrequency(new BecomeNodeCopyMutation(random, parser)));
    }

    /**
     * Returns {@link Mutation}s that may be applied to the given type of {@link Expression} node paired with the relative frequencies with which they
     * should be applied.
     *
     * @param nodeType
     * @return
     */
    public List<MutationFrequency> getMutationFrequencies(final Class<? extends Expression> nodeType) {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(mutationFrequencyMap.get(nodeType))));
    }

    private final void setMutationFrequencies(final Class<? extends Expression> nodeType, final MutationFrequency... frequencies) {
        mutationFrequencyMap.put(nodeType, frequencies);
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

        public MutationFrequency(final Mutation mutation, final int relativeFrequency) {
            this.mutation = mutation;
            this.relativeFrequency = relativeFrequency;
        }

        public int getRelativeFrequency() {
            return relativeFrequency;
        }

        public Mutation getMutation() {
            return mutation;
        }
    }
}
