package com.github.adinsa.picevolve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adinsa.picevolve.Terminal.ScalarNode;
import com.github.adinsa.picevolve.Terminal.VariableNode;
import com.github.adinsa.picevolve.Terminal.VectorNode;

/**
 * {@link Visitor} implementation that applies genetic {@link Mutation}
 * operations on an {@link Expression} tree
 *
 * @author amar
 *
 */
public class MutationVisitor implements Visitor {

    private static final Logger logger = LoggerFactory
            .getLogger(MutationVisitor.class);

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

    public MutationVisitor(final Random random,
            final double globalMutationFrequency) {
        this.random = random;
        this.globalMutationFrequency = globalMutationFrequency;
    }

    @Override
    public void visit(final ScalarNode scalarNode) {

        if (this.random.shouldMutate(scalarNode,
                this.globalMutationFrequency)) {

            final Mutation mutation = this.random
                    .nextMutation(ScalarNode.class);

            logger.debug(mutation.getClass().getSimpleName() + ": {}",
                    scalarNode);

            mutation.mutate(scalarNode);

        }
    }

    @Override
    public void visit(final VectorNode vectorNode) {

        if (this.random.shouldMutate(vectorNode,
                this.globalMutationFrequency)) {

            final Mutation mutation = this.random
                    .nextMutation(VectorNode.class);

            logger.debug(mutation.getClass().getSimpleName() + ": {}",
                    vectorNode);

            mutation.mutate(vectorNode);
        }
    }

    @Override
    public void visit(final VariableNode variableNode) {

        if (this.random.shouldMutate(variableNode,
                this.globalMutationFrequency)) {

            final Mutation mutation = this.random
                    .nextMutation(VariableNode.class);

            logger.debug(mutation.getClass().getSimpleName() + ": {}",
                    variableNode);

            mutation.mutate(variableNode);
        }
    }

    @Override
    public void visit(final Function functionNode) {

        if (this.random.shouldMutate(functionNode, this.globalMutationFrequency)
                && functionNode.getParent() != null) {

            final Mutation mutation = this.random.nextMutation(Function.class);

            logger.debug(mutation.getClass().getSimpleName() + ": {}",
                    functionNode);

            mutation.mutate(functionNode);
        }
    }
}
