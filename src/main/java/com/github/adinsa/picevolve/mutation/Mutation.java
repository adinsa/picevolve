package com.github.adinsa.picevolve.mutation;

import java.util.ArrayList;
import java.util.List;

import com.github.adinsa.picevolve.PicEvolve;
import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;
import com.github.adinsa.picevolve.random.Random;
import com.github.adinsa.picevolve.random.RandomImpl;

/**
 * Genetic operator used to evolve {@link Expression}s
 *
 * @author amar
 *
 */
public abstract class Mutation {

    protected Random random;
    protected PicEvolve picEvolve;
    protected Class<? extends Expression> nodeType;

    public Mutation(final Class<? extends Expression> nodeType) {
        this(new RandomImpl(), nodeType);
    }

    public Mutation(final Random random, final Class<? extends Expression> nodeType) {
        this.random = random;
        picEvolve = new PicEvolve(random);
        this.nodeType = nodeType;
    }

    public abstract void mutate(Expression node);

    /**
     * Replace a node with a random expression
     */
    public static class RandomExpressionMutation extends Mutation {

        public RandomExpressionMutation(final Random random) {
            super(random, Expression.class);
        }

        @Override
        public void mutate(final Expression node) {
            final List<Expression> newChildren = node.getParent().getChildren();
            newChildren.set(newChildren.indexOf(newChildren.stream().filter(child -> child == node).findAny().get()),
                    random.nextExpression());
            node.getParent().setChildren(newChildren);
        }
    }

    /**
     * Adjust a scalar node's value by a random amount.
     *
     */
    public static class AdjustScalarMutation extends Mutation {

        public AdjustScalarMutation(final Random random) {
            super(random, ScalarNode.class);
        }

        @Override
        public void mutate(final Expression node) {
            final Terminal.ScalarNode scalarNode = (Terminal.ScalarNode) nodeType.asSubclass(Expression.class)
                    .cast(node);
            final Terminal.ScalarNode randomScalar = random.nextScalar();
            scalarNode.setValue(randomScalar.getValue());
        }
    }

    /**
     * Adjust a vector node's values by random amounts.
     *
     */
    public static class AdjustVectorMutation extends Mutation {

        public AdjustVectorMutation(final Random random) {
            super(random, VectorNode.class);
        }

        @Override
        public void mutate(final Expression node) {
            final Terminal.VectorNode vectorNode = (Terminal.VectorNode) nodeType.asSubclass(Expression.class)
                    .cast(node);
            final VectorNode randomVector = random.nextVector();
            vectorNode.setValue(randomVector.getValue());
        }
    }

    /**
     * Make a node an argument to a new random function (generating new random terminal arguments if necessary).
     *
     */
    public static class BecomeArgumentMutation extends Mutation {

        public BecomeArgumentMutation(final Random random) {
            super(random, Expression.class);
        }

        @Override
        public void mutate(final Expression node) {

            final Function randomFunc = random.nextFunction();

            final List<Expression> children = new ArrayList<>();
            children.add(node);
            for (int i = 0; i < randomFunc.getArity() - 1; i++) {
                children.add(random.nextTerminal());
            }

            final List<Expression> newChildren = node.getParent().getChildren();
            newChildren.set(newChildren.indexOf(newChildren.stream().filter(child -> child == node).findAny().get()),
                    randomFunc);
            randomFunc.setChildren(children);
        }
    }

    /**
     * Change a function node into a different type of function node (generating new random terminal arguments if
     * necessary).
     *
     */
    public static class ChangeFunctionMutation extends Mutation {

        public ChangeFunctionMutation(final Random random) {
            super(random, Function.class);
        }

        @Override
        public void mutate(final Expression node) {

            final Function functionNode = (Function) nodeType.asSubclass(Expression.class).cast(node);

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

            final List<Expression> newChildren = functionNode.getParent().getChildren();
            newChildren.set(
                    newChildren.indexOf(newChildren.stream().filter(child -> child == functionNode).findAny().get()),
                    randomFunc);
            randomFunc.setChildren(children);
        }
    }

    /**
     * Replace a function node with one of its arguments.
     *
     */
    public static class ReplaceWithArgumentMutation extends Mutation {

        public ReplaceWithArgumentMutation(final Random random) {
            super(random, Function.class);
        }

        @Override
        public void mutate(final Expression node) {

            final Function functionNode = (Function) nodeType.asSubclass(Expression.class).cast(node);

            final List<Expression> newChildren = functionNode.getParent().getChildren();
            newChildren.set(
                    newChildren.indexOf(newChildren.stream().filter(child -> child == functionNode).findAny().get()),
                    random.nextChild(functionNode));
            functionNode.getParent().setChildren(newChildren);
        }
    }

    /**
     * Replace a node with a deep copy of any other node in the parent expression.
     *
     */
    public static class BecomeNodeCopyMutation extends Mutation {

        public BecomeNodeCopyMutation(final Random random) {
            super(random, Expression.class);
        }

        @Override
        public void mutate(final Expression node) {
            Expression root = node;
            while (root.getParent() != null) {
                root = root.getParent();
            }
            final List<Expression> newChildren = node.getParent().getChildren();
            newChildren.set(newChildren.indexOf(newChildren.stream().filter(child -> child == node).findAny().get()),
                    picEvolve.parse(random.nextNode(root).toString()));
            node.getParent().setChildren(newChildren);
        }
    }
}
