package com.github.adinsa.picevolve;

import java.util.ArrayList;
import java.util.List;

import com.github.adinsa.picevolve.Terminal.ScalarNode;
import com.github.adinsa.picevolve.Terminal.VectorNode;

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

    public Mutation(final Random random,
            final Class<? extends Expression> nodeType) {
        this.random = random;
        this.picEvolve = new PicEvolve(random);
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
            newChildren.set(
                    newChildren.indexOf(newChildren.stream()
                            .filter(child -> child == node).findAny().get()),
                    this.random.nextExpression());
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
            final Terminal.ScalarNode scalarNode = (Terminal.ScalarNode) this.nodeType
                    .asSubclass(Expression.class).cast(node);
            final Terminal.ScalarNode randomScalar = this.random.nextScalar();
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
            final Terminal.VectorNode vectorNode = (Terminal.VectorNode) this.nodeType
                    .asSubclass(Expression.class).cast(node);
            final VectorNode randomVector = this.random.nextVector();
            vectorNode.setValue(randomVector.getValue());
        }
    }

    /**
     * Make a node an argument to a new random function (generating new random
     * terminal arguments if necessary).
     *
     */
    public static class BecomeArgumentMutation extends Mutation {

        public BecomeArgumentMutation(final Random random) {
            super(random, Expression.class);
        }

        @Override
        public void mutate(final Expression node) {

            final Function randomFunc = this.random.nextFunction();

            final List<Expression> children = new ArrayList<Expression>();
            children.add(node);
            for (int i = 0; i < randomFunc.getArity() - 1; i++) {
                children.add(this.random.nextTerminal());
            }

            final List<Expression> newChildren = node.getParent().getChildren();
            newChildren.set(
                    newChildren.indexOf(newChildren.stream()
                            .filter(child -> child == node).findAny().get()),
                    randomFunc);
            randomFunc.setChildren(children);
        }
    }

    /**
     * Change a function node into a different type of function node (generating
     * new random terminal arguments if necessary).
     *
     */
    public static class ChangeFunctionMutation extends Mutation {

        public ChangeFunctionMutation(final Random random) {
            super(random, Function.class);
        }

        @Override
        public void mutate(final Expression node) {

            final Function functionNode = (Function) this.nodeType
                    .asSubclass(Expression.class).cast(node);

            final Function randomFunc = this.random.nextFunction();

            final List<Expression> children = new ArrayList<Expression>();

            for (int i = 0; i < functionNode.getArity(); i++) {
                if (i < randomFunc.getArity()) {
                    children.add(functionNode.getChildren().get(i));
                }
            }
            for (int i = 0; i < Math.max(0,
                    randomFunc.getArity() - functionNode.getArity()); i++) {
                children.add(this.random.nextTerminal());
            }

            final List<Expression> newChildren = functionNode.getParent()
                    .getChildren();
            newChildren.set(newChildren.indexOf(newChildren.stream()
                    .filter(child -> child == functionNode).findAny().get()),
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

            final Function functionNode = (Function) this.nodeType
                    .asSubclass(Expression.class).cast(node);

            final List<Expression> newChildren = functionNode.getParent()
                    .getChildren();
            newChildren.set(newChildren.indexOf(newChildren.stream()
                    .filter(child -> child == functionNode).findAny().get()),
                    this.random.nextChild(functionNode));
            functionNode.getParent().setChildren(newChildren);
        }
    }

    /**
     * Replace a node with a deep copy of any other node in the parent
     * expression.
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
            newChildren.set(
                    newChildren.indexOf(newChildren.stream()
                            .filter(child -> child == node).findAny().get()),
                    this.picEvolve
                            .parse(this.random.nextNode(root).toString()));
            node.getParent().setChildren(newChildren);
        }
    }
}
