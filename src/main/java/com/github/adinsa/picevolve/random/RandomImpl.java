package com.github.adinsa.picevolve.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.IntStream;

import com.github.adinsa.picevolve.PicEvolve;
import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;
import com.github.adinsa.picevolve.expression.Variable;
import com.github.adinsa.picevolve.mutation.Mutation;
import com.github.adinsa.picevolve.mutation.MutationFactory;
import com.github.adinsa.picevolve.mutation.MutationFactory.MutationFrequency;

public class RandomImpl implements Random {

    private final java.util.Random random;
    private final PicEvolve picEvolve;

    public RandomImpl() {
        random = new java.util.Random();
        picEvolve = new PicEvolve(this);
    }

    @Override
    public ScalarNode nextScalar() {
        return new ScalarNode(random.nextDouble());
    }

    @Override
    public VectorNode nextVector() {
        return new VectorNode(new ArrayList<>(Arrays.asList(random.nextDouble(), random.nextDouble(), random.nextDouble())));
    }

    @Override
    public Expression nextTerminal() {

        final double rand = random.nextDouble();

        if (rand >= 0 && rand < 1 / 3.0) {
            return nextScalar();
        } else if (rand >= 1 / 3.0 && rand < 2 / 3.0) {
            return nextVector();
        } else {
            return new VariableNode(random.nextDouble() < 0.5 ? Variable.X : Variable.Y);
        }
    }

    @Override
    public Function nextFunction() {
        final Iterator<String> iter = picEvolve.getFunctionNames().iterator();
        IntStream.range(0, random.nextInt(picEvolve.getFunctionNames().size())).forEach(i -> iter.next());

        return picEvolve.getFunction(iter.next()).get();
    }

    /**
     * Generates a random s-expression. Chooses a random function from the function set and then generates the required number of arguments, each of
     * which may take one of the following forms:
     *
     * <pre>
     * 1.) Random scalar value (between 0.0 and 1.0)
     * 2.) Random 3-element vector (formatted like '#0.35,0.93,0.82')
     * 3.) Random {@link Variable} (X or Y pixel coordinate)
     * 4.) Another random s-expression
     * </pre>
     *
     */
    @Override
    public Expression nextExpression() {

        final StringBuilder sb = new StringBuilder("(");

        final Function randFunc = nextFunction();

        sb.append(randFunc.getName()).append(" ");

        for (int i = 0; i < randFunc.getArity(); i++) {

            final double rand = random.nextDouble();

            if (rand >= 0 && rand < 0.25) {
                sb.append(random.nextDouble());
            } else if (rand >= 0.25 && rand < 0.50) {
                sb.append("#").append(random.nextDouble()).append(",").append(random.nextDouble()).append(",").append(random.nextDouble());
            } else if (rand >= 0.50 && rand < 0.75) {
                sb.append(random.nextDouble() > 0.5 ? Variable.X : Variable.Y);
            } else {
                sb.append(nextExpression());
            }
            sb.append(" ");
        }
        final String exprStr = sb.toString().trim() + ")";

        return picEvolve.parse(exprStr);
    }

    /**
     * Uses reservoir sampling algorithm to choose random node from {@link Expression} tree.
     */
    @Override
    public Expression nextNode(final Expression root) {

        final Stack<Expression> exprStack = new Stack<>();
        exprStack.push(root);
        Expression randomNode = root;
        int i = 1;
        while (!exprStack.isEmpty()) {
            final Expression curNode = exprStack.pop();
            if (random.nextDouble() < 1.0 / i++) {
                randomNode = curNode;
            }
            for (final Expression child : curNode.getChildren()) {
                exprStack.push(child);
            }
        }

        return randomNode;
    }

    @Override
    public Expression nextChild(final Expression node) {
        return node.getChildren().get(random.nextInt(node.getChildren().size()));
    }

    @Override
    public Mutation nextMutation(final Class<? extends Expression> nodeType) {

        final List<MutationFrequency> freqs = new MutationFactory(this).getMutationFrequencies(nodeType);

        final int totalWeight = freqs.stream().mapToInt(freq -> freq.getRelativeFrequency()).sum();

        final int rand = random.nextInt(totalWeight);

        int sum = 0;
        int i = 0;
        while (sum <= rand) {
            sum += freqs.get(i++).getRelativeFrequency();
        }
        return freqs.get(Math.max(0, i - 1)).getMutation();
    }

    /**
     * Scales overall mutation frequency inversely in proportion to length of parent expression
     */
    @Override
    public boolean shouldMutate(final Expression expression, final double globalMutationFrequency) {
        return random.nextDouble() < globalMutationFrequency * (1.0 / getHeight(expression));
    }

    private int getHeight(final Expression expression) {

        int height = 0;
        final Queue<Expression> currentLevel = new LinkedList<>();
        final Queue<Expression> nextLevel = new LinkedList<>();

        currentLevel.add(expression);

        while (!currentLevel.isEmpty()) {
            final Expression cur = currentLevel.remove();
            for (final Expression child : cur.getChildren()) {
                nextLevel.add(child);
            }
            if (currentLevel.isEmpty()) {
                height++;
                currentLevel.addAll(nextLevel);
                nextLevel.clear();
            }
        }
        return height;
    }
}