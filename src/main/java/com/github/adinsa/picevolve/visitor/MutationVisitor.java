package com.github.adinsa.picevolve.visitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;
import com.github.adinsa.picevolve.mutation.Mutation;
import com.github.adinsa.picevolve.random.Random;
import com.github.adinsa.picevolve.random.RandomImpl;

/**
 * {@link Visitor} implementation that applies genetic {@link Mutation} operations on an {@link Expression} tree
 *
 * @author amar
 *
 */
public class MutationVisitor implements Visitor {

    private static final Logger logger = LoggerFactory.getLogger(MutationVisitor.class);

    private static final double DEFAULT_GLOBAL_MUTATION_FREQUENCY = 0.4;

    private final Random random;
    private final double globalMutationFrequency;

    public MutationVisitor() {
        this(new RandomImpl(), DEFAULT_GLOBAL_MUTATION_FREQUENCY);
    }

    public MutationVisitor(final Random random) {
        this(random, DEFAULT_GLOBAL_MUTATION_FREQUENCY);
    }

    public MutationVisitor(final double globalMutationFrequency) {
        this(new RandomImpl(), globalMutationFrequency);
    }

    public MutationVisitor(final Random random, final double globalMutationFrequency) {
        this.random = random;
        this.globalMutationFrequency = globalMutationFrequency;
    }

    @Override
    public void visit(final ScalarNode scalarNode) {

        if (random.shouldMutate(scalarNode, globalMutationFrequency)) {

            final Mutation mutation = random.nextMutation(ScalarNode.class);

            logger.debug(mutation.getClass().getSimpleName() + ": {}", scalarNode);

            mutation.mutate(scalarNode);

        }
    }

    @Override
    public void visit(final VectorNode vectorNode) {

        if (random.shouldMutate(vectorNode, globalMutationFrequency)) {

            final Mutation mutation = random.nextMutation(VectorNode.class);

            logger.debug(mutation.getClass().getSimpleName() + ": {}", vectorNode);

            mutation.mutate(vectorNode);
        }
    }

    @Override
    public void visit(final VariableNode variableNode) {

        if (random.shouldMutate(variableNode, globalMutationFrequency)) {

            final Mutation mutation = random.nextMutation(VariableNode.class);

            logger.debug(mutation.getClass().getSimpleName() + ": {}", variableNode);

            mutation.mutate(variableNode);
        }
    }

    @Override
    public void visit(final Function functionNode) {

        if (random.shouldMutate(functionNode, globalMutationFrequency) && functionNode.getParent() != null) {

            final Mutation mutation = random.nextMutation(Function.class);

            logger.debug(mutation.getClass().getSimpleName() + ": {}", functionNode);

            mutation.mutate(functionNode);
        }
    }
}
