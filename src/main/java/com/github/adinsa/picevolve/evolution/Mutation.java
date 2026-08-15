package com.github.adinsa.picevolve.evolution;

import java.util.ArrayList;
import java.util.List;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.ExpressionParser;
import com.github.adinsa.picevolve.expression.Terminal;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;

/**
 * Genetic operator used to evolve {@link Expression}s
 *
 * @author amar
 *
 */
public abstract class Mutation {

    protected Random random;
    protected ExpressionParser parser;

    public Mutation(final Random random, final ExpressionParser parser) {
        this.random = random;
        this.parser = parser;
    }

    public abstract void mutate(Expression node);

    /**
     * Replace a node with a random expression
     */
    public static class RandomExpressionMutation extends Mutation {

        public RandomExpressionMutation(final Random random, final ExpressionParser parser) {
            super(random, parser);
        }

        @Override
        public void mutate(final Expression node) {
            node.replaceWith(random.nextExpression());
        }
    }

    /**
     * Adjust a scalar node's value by a random amount.
     *
     */
    public static class AdjustScalarMutation extends Mutation {

        public AdjustScalarMutation(final Random random, final ExpressionParser parser) {
            super(random, parser);
        }

        @Override
        public void mutate(final Expression node) {
            final Terminal.ScalarNode scalarNode = (Terminal.ScalarNode) node;
            scalarNode.setValue(random.nextScalar().getValue());
        }
    }

    /**
     * Adjust a vector node's values by random amounts.
     *
     */
    public static class AdjustVectorMutation extends Mutation {

        public AdjustVectorMutation(final Random random, final ExpressionParser parser) {
            super(random, parser);
        }

        @Override
        public void mutate(final Expression node) {
            final Terminal.VectorNode vectorNode = (Terminal.VectorNode) node;
            vectorNode.setValue(random.nextVector().getValue());
        }
    }

    /**
     * Make a node an argument to a new random function (generating new random terminal arguments if necessary).
     *
     */
    public static class BecomeArgumentMutation extends Mutation {

        public BecomeArgumentMutation(final Random random, final ExpressionParser parser) {
            super(random, parser);
        }

        @Override
        public void mutate(final Expression node) {

            final Function randomFunc = random.nextFunction();

            final List<Expression> children = new ArrayList<>();
            children.add(node);
            for (int i = 0; i < randomFunc.getArity() - 1; i++) {
                children.add(random.nextTerminal());
            }

            node.replaceWith(randomFunc);
            randomFunc.setChildren(children);
        }
    }

    /**
     * Change a function node into a different type of function node (generating new random terminal arguments if necessary).
     *
     */
    public static class ChangeFunctionMutation extends Mutation {

        public ChangeFunctionMutation(final Random random, final ExpressionParser parser) {
            super(random, parser);
        }

        @Override
        public void mutate(final Expression node) {

            final Function functionNode = (Function) node;

            final Function randomFunc = random.nextFunction();

            final List<Expression> children = new ArrayList<>();

            for (int i = 0; i < functionNode.getArity(); i++) {
                if (i < randomFunc.getArity()) {
                    children.add(functionNode.getChildren().get(i));
                }
            }
            for (int i = 0; i < Math.max(0, randomFunc.getArity() - functionNode.getArity()); i++) {
                children.add(random.nextTerminal());
            }

            randomFunc.setChildren(children);
            node.replaceWith(randomFunc);
        }
    }

    /**
     * Replace a function node with one of its arguments.
     *
     */
    public static class ReplaceWithArgumentMutation extends Mutation {

        public ReplaceWithArgumentMutation(final Random random, final ExpressionParser parser) {
            super(random, parser);
        }

        @Override
        public void mutate(final Expression node) {
            node.replaceWith(random.nextChild(node));
        }
    }

    /**
     * Replace a node with a deep copy of any other node in the parent expression.
     *
     */
    public static class BecomeNodeCopyMutation extends Mutation {

        public BecomeNodeCopyMutation(final Random random, final ExpressionParser parser) {
            super(random, parser);
        }

        @Override
        public void mutate(final Expression node) {
            Expression root = node;
            while (root.getParent() != null) {
                root = root.getParent();
            }
            node.replaceWith(parser.parse(random.nextNode(root).toString()));
        }
    }
}
