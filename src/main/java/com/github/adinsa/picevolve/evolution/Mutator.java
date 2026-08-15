package com.github.adinsa.picevolve.evolution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.ExpressionVisitor;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.FunctionSet;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;

/**
 * Applies random {@link Mutation}s to the nodes of an {@link Expression} tree.
 *
 * @author amar
 *
 */
public class Mutator implements ExpressionVisitor {

    private static final Logger logger = LoggerFactory.getLogger(Mutator.class);

    private static final double DEFAULT_GLOBAL_MUTATION_FREQUENCY = 0.4;

    private final Random random;
    private final double globalMutationFrequency;

    public Mutator() {
        this(new RandomImpl(FunctionSet.createDefault()), DEFAULT_GLOBAL_MUTATION_FREQUENCY);
    }

    public Mutator(final Random random) {
        this(random, DEFAULT_GLOBAL_MUTATION_FREQUENCY);
    }

    public Mutator(final double globalMutationFrequency) {
        this(new RandomImpl(FunctionSet.createDefault()), globalMutationFrequency);
    }

    public Mutator(final Random random, final double globalMutationFrequency) {
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

        int maxChildHeight = 0;
        for (final Expression child : functionNode.getChildren()) {
            maxChildHeight = Math.max(maxChildHeight, child.getHeight());
        }
        functionNode.setHeight(maxChildHeight + 1);

        if (random.shouldMutate(functionNode, globalMutationFrequency) && functionNode.getParent() != null) {

            final Mutation mutation = random.nextMutation(Function.class);

            logger.debug(mutation.getClass().getSimpleName() + ": {}", functionNode);

            mutation.mutate(functionNode);
        }
    }
}
