package com.github.adinsa.picevolve.evolution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.FunctionSet;
import com.github.adinsa.picevolve.expression.Terminal;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;

public class RandomImplTest {

    private RandomImpl random;

    @BeforeEach
    public void setup() {
        random = new RandomImpl(FunctionSet.createDefault());
    }

    @Test
    public void testNextScalarInUnitRange() {
        for (int i = 0; i < 100; i++) {
            final double value = random.nextScalar().getValue();
            assertTrue(value >= 0.0 && value < 1.0, "Scalar value should be in [0, 1): " + value);
        }
    }

    @Test
    public void testNextVectorHasThreeComponentsInUnitRange() {
        for (int i = 0; i < 100; i++) {
            final VectorNode vector = random.nextVector();
            assertEquals(3, vector.getValue().size());
            for (final double component : vector.getValue()) {
                assertTrue(component >= 0.0 && component < 1.0);
            }
        }
    }

    @Test
    public void testNextTerminalReturnsTerminal() {
        final Set<Class<?>> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            final Expression terminal = random.nextTerminal();
            assertTrue(terminal instanceof Terminal, "nextTerminal should return a Terminal node");
            seen.add(terminal.getClass());
        }
        assertTrue(seen.contains(ScalarNode.class), "should generate scalar terminals");
        assertTrue(seen.contains(VectorNode.class), "should generate vector terminals");
        assertTrue(seen.contains(VariableNode.class), "should generate variable terminals");
    }

    @Test
    public void testNextTerminalGeneratesBothVariables() {
        final Set<com.github.adinsa.picevolve.expression.Variable> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            final Expression terminal = random.nextTerminal();
            if (terminal instanceof VariableNode) {
                seen.add(((VariableNode) terminal).getValue());
            }
        }
        assertTrue(seen.contains(com.github.adinsa.picevolve.expression.Variable.X), "should generate X variables");
        assertTrue(seen.contains(com.github.adinsa.picevolve.expression.Variable.Y), "should generate Y variables");
    }

    @Test
    public void testNextFunctionReturnsKnownFunction() {
        final Set<String> names = FunctionSet.createDefault().names();
        for (int i = 0; i < 100; i++) {
            final Function function = random.nextFunction();
            assertNotNull(function);
            assertTrue(names.contains(function.getName()), "Unknown function generated: " + function.getName());
        }
    }

    @Test
    public void testNextExpressionIsWellFormed() {
        for (int i = 0; i < 200; i++) {
            final Expression expression = random.nextExpression();
            assertTrue(expression instanceof Function, "nextExpression should produce a Function root");

            final Function function = (Function) expression;
            assertEquals(function.getArity(), function.getChildren().size(),
                    "Function children count should match arity");

            final Expression reparsed = randomParser().parse(expression.toString());
            assertEquals(expression.toString(), reparsed.toString(), "Expression should round-trip through its toString()");
        }
    }

    @Test
    public void testNextExpressionGeneratesAllTerminalTypes() {
        final Set<Class<?>> terminalTypes = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            collectTerminalTypes(random.nextExpression(), terminalTypes);
        }
        assertTrue(terminalTypes.contains(ScalarNode.class), "should generate scalar terminals");
        assertTrue(terminalTypes.contains(VectorNode.class), "should generate vector terminals");
        assertTrue(terminalTypes.contains(VariableNode.class), "should generate variable terminals");
    }

    @Test
    public void testNextNodeReturnsNodeWithinTree() {
        final Expression root = randomParser().parse("(abs (- x 0.3))");
        final Set<String> subtrees = collectSubtreeStrings(root);

        for (int i = 0; i < 100; i++) {
            final Expression node = random.nextNode(root);
            assertNotNull(node);
            assertTrue(subtrees.contains(node.toString()), "nextNode should return a node within the tree: " + node);
        }
    }

    @Test
    public void testNextNodeCanReturnNonRootNodes() {
        final Expression root = randomParser().parse("(abs (- x 0.3))");
        final String rootString = root.toString();

        final Set<String> returned = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            returned.add(random.nextNode(root).toString());
        }
        assertTrue(returned.size() > 1, "nextNode should eventually return nodes other than the root: " + returned);
        assertTrue(returned.contains(rootString), "nextNode should also return the root node");
    }

    @Test
    public void testNextMutationReturnsVariedMutations() {
        final Set<Class<?>> scalarMutations = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            scalarMutations.add(random.nextMutation(ScalarNode.class).getClass());
        }
        assertTrue(scalarMutations.size() > 1, "nextMutation should not always return the same mutation type");
    }

    @Test
    public void testNextChildReturnsImmediateChild() {
        final Expression root = randomParser().parse("(abs (- x 0.3))");
        final Expression minus = root.getChildren().get(0);

        for (int i = 0; i < 100; i++) {
            final Expression child = random.nextChild(minus);
            assertTrue(minus.getChildren().contains(child), "nextChild should return an immediate child");
        }
    }

    @Test
    public void testNextMutationReturnsApplicableMutation() {
        for (int i = 0; i < 100; i++) {
            assertNotNull(random.nextMutation(ScalarNode.class));
            assertNotNull(random.nextMutation(VectorNode.class));
            assertNotNull(random.nextMutation(VariableNode.class));
            assertNotNull(random.nextMutation(Function.class));
        }
    }

    @Test
    public void testShouldMutateReturnsFalseForZeroFrequency() {
        final Expression expr = randomParser().parse("x");
        for (int i = 0; i < 100; i++) {
            assertFalse(random.shouldMutate(expr, 0.0), "zero frequency should never mutate");
            assertFalse(random.shouldMutate(expr, -0.5), "negative frequency should never mutate");
        }
    }

    @Test
    public void testShouldMutateReturnsTrueForUnitFrequencyAndLeafHeight() {
        final Expression expr = randomParser().parse("x");
        assertEquals(1, expr.getHeight());
        for (int i = 0; i < 100; i++) {
            assertTrue(random.shouldMutate(expr, 1.0), "frequency 1.0 and height 1 should always mutate");
        }
    }

    @Test
    public void testShouldMutateScalesFrequencyByHeight() {
        final RandomImpl seeded = new RandomImpl(FunctionSet.createDefault(), new java.util.Random(0));
        final Expression expr = randomParser().parse("x");
        expr.setHeight(4);

        // new Random(0).nextDouble() == 0.730967787376657, threshold = 0.5 * (1/4) = 0.125
        assertFalse(seeded.shouldMutate(expr, 0.5),
                "mutation frequency should scale inversely with expression height");
    }

    private com.github.adinsa.picevolve.expression.ExpressionParser randomParser() {
        return new com.github.adinsa.picevolve.expression.ExpressionParser(FunctionSet.createDefault());
    }

    private Set<String> collectSubtreeStrings(final Expression root) {
        final Set<String> result = new HashSet<>();
        final Stack<Expression> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            final Expression node = stack.pop();
            result.add(node.toString());
            stack.addAll(node.getChildren());
        }
        return result;
    }

    private void collectTerminalTypes(final Expression root, final Set<Class<?>> terminalTypes) {
        final Stack<Expression> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            final Expression node = stack.pop();
            if (node instanceof Terminal) {
                terminalTypes.add(node.getClass());
            }
            stack.addAll(node.getChildren());
        }
    }
}
