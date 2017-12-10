package com.github.adinsa.picevolve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal;
import com.github.adinsa.picevolve.expression.Variable;
import com.github.adinsa.picevolve.random.Random;
import com.github.adinsa.picevolve.random.RandomImpl;
import com.github.adinsa.picevolve.visitor.EvaluatorVisitor;
import com.github.adinsa.picevolve.visitor.MutationVisitor;
import com.github.adinsa.picevolve.visitor.Visitor;

public class PicEvolve {

    private static final Logger logger = LoggerFactory.getLogger(PicEvolve.class);

    private final Map<String, Function> functionMap = new HashMap<>();
    private final Random random;

    public PicEvolve() {
        this(new RandomImpl());
    }

    public PicEvolve(final Random random) {

        this.random = random;

        this.addFunctions(new Function[] {
                new Function.Plus(),
                new Function.Minus(),
                new Function.Multiply(),
                new Function.Divide(),
                new Function.Round(),
                new Function.Expt(),
                new Function.Log(),
                new Function.Sine(),
                new Function.Cosine(),
                new Function.Tangent(),
                new Function.Min(),
                new Function.Max(),
                new Function.Abs(),
                new Function.Mod(),
                new Function.IntAnd(),
                new Function.IntOr(),
                new Function.IntXor(),
                new Function.FloatAnd(),
                new Function.FloatOr(),
                new Function.FloatXor(),
                new Function.Noise(),
                new Function.WarpedNoise(),
                new Function.Blur(),
                new Function.Sharpen(),
                new Function.Emboss()
        });
    }

    private final void addFunctions(final Function... functions) {
        Arrays.asList(functions).stream().forEach(func -> addFunction(func));
    }

    private final void addFunction(final Function function) {
        if (functionMap.containsKey(function.getName())) {
            throw new IllegalArgumentException(
                    String.format("Function with name '%s' already exists", function.getName()));
        }
        functionMap.put(function.getName(), function);
    }

    public Optional<Function> getFunction(final String name) {
        return Optional.ofNullable(functionMap.get(name)).map(function -> function.copy());
    }

    public Set<String> getFunctionNames() {
        return Collections.unmodifiableSet(functionMap.keySet());
    }

    /**
     * Parse the input s-expression string and return the AST as an {@link Expression}.
     *
     * @param expressionString
     * @return
     */
    public Expression parse(final String expressionString) {

        final Stack<Expression> exprStack = new Stack<>();

        final List<String> tokens = new ArrayList<>(Arrays.asList(
                expressionString.replace("(", "").replace(")", "").replaceAll("\\s{2,}", " ").trim().split(" ")));

        Collections.reverse(tokens);

        for (final String token : tokens) {
            if (getFunction(token).isPresent()) {
                final Function func = getFunction(token).get();
                final List<Expression> children = new ArrayList<>(func.getArity());
                for (int i = 0; i < func.getArity(); i++) {
                    final Expression child = exprStack.pop();
                    children.add(child);
                }
                func.setChildren(children);
                exprStack.push(func);
            } else if (Variable.fromString(token).isPresent()) {
                exprStack.push(new Terminal.VariableNode(Variable.fromString(token).get()));
            } else if (token.startsWith("#")) {
                final String[] vecParts = token.replace("#", "").split(",");
                exprStack.push(new Terminal.VectorNode(new ArrayList<>(Arrays.asList(Double.valueOf(vecParts[0]),
                        Double.valueOf(vecParts[1]), Double.valueOf(vecParts[2])))));
            } else {
                try {
                    final double val = Double.valueOf(token);
                    exprStack.push(new Terminal.ScalarNode(val));
                } catch (final NumberFormatException e) {
                    throw new RuntimeException(String.format("Invalid token: '%s'", token));
                }
            }
        }
        return exprStack.pop();
    }

    /**
     * Returns a population of random expressions
     *
     * @param populationSize
     * @return
     */
    public List<Expression> initializePopulation(final int populationSize) {

        final List<Expression> population = new ArrayList<>();
        IntStream.range(0, populationSize).forEach(i -> population.add(random.nextExpression()));

        return population;
    }

    /**
     * Returns new generation of expressions containing mutations of the provided parent expression.
     *
     * @param parent
     * @param populationSize
     * @return
     */
    public List<Expression> mutate(final Expression parent, final int populationSize) {

        final List<Expression> nextGeneration = new ArrayList<>();

        do {
            final Visitor visitor = new MutationVisitor();
            final Expression mutant = parse(parent.toString());
            mutant.accept(visitor);

            if (!mutant.toString().equalsIgnoreCase(parent.toString())) {
                nextGeneration.add(mutant);
            }
        } while (nextGeneration.size() != populationSize);

        return nextGeneration;
    }

    /**
     * Returns new generation of expressions generated by performing crossover between two provided parent expressions.
     *
     * @param mom
     * @param dad
     * @param populationSize
     * @return
     */
    public List<Expression> crossover(final Expression mom, final Expression dad, final int populationSize) {

        logger.debug("Mom expression: {}", mom.toString());
        logger.debug("Dad expression: {}", dad.toString());

        final List<Expression> children = new ArrayList<>();

        do {
            Expression momCopy = parse(mom.toString());
            final Expression dadCopy = parse(dad.toString());

            final Expression momSubtree = random.nextNode(momCopy);
            final Expression dadSubtree = random.nextNode(dadCopy);

            if (momSubtree.getParent() == null) {
                momCopy = dadSubtree;
            } else {
                final List<Expression> newChildren = momSubtree.getParent().getChildren();
                newChildren.set(
                        newChildren
                                .indexOf(newChildren.stream().filter(sibling -> sibling == momSubtree).findAny().get()),
                        dadSubtree);
                momSubtree.getParent().setChildren(newChildren);
            }

            if (!momCopy.toString().equalsIgnoreCase(mom.toString())) {
                children.add(momCopy);
            }

        } while (children.size() != populationSize);

        return children;
    }

    /**
     * Evaluates input {@link Expression} into an {@link Image} whose values are normalized between 0 and 1.
     *
     * @param expression
     * @param width
     * @param height
     * @return
     */
    public Image evaluate(final Expression expression, final int width, final int height) {

        final EvaluatorVisitor visitor = new EvaluatorVisitor(width, height);
        expression.accept(visitor);

        return visitor.getImage().scaled();
    }
}
