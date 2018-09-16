package com.github.adinsa.picevolve.visitor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.adinsa.picevolve.PicEvolve;
import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Function.Minus;
import com.github.adinsa.picevolve.expression.Function.Noise;
import com.github.adinsa.picevolve.expression.Function.Plus;
import com.github.adinsa.picevolve.expression.Terminal;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;
import com.github.adinsa.picevolve.expression.Variable;
import com.github.adinsa.picevolve.mutation.Mutation;
import com.github.adinsa.picevolve.random.Random;

public class MutationVisitorTest {

    private PicEvolve picEvolve;
    private Random random;

    @Before
    public void setup() {
        picEvolve = new PicEvolve();
        random = mock(Random.class);
    }

    @Test
    public void testScalarToRandomExpressionMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(ScalarNode.class));

        when(random.nextExpression()).thenReturn(picEvolve.parse("(+ 1.0 2.0)"));

        when(random.nextMutation(any())).thenReturn(new Mutation.RandomExpressionMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- X (+ 1.0 2.0)))", expr.toString());
    }

    @Test
    public void testAdjustScalarMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(ScalarNode.class));

        when(random.nextScalar()).thenReturn(new ScalarNode(0.1));

        when(random.nextMutation(any())).thenReturn(new Mutation.AdjustScalarMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- X 0.1))", expr.toString());
    }

    @Test
    public void testScalarBecomeArgumentMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(ScalarNode.class));

        when(random.nextFunction()).thenReturn(new Function.Noise());
        when(random.nextTerminal()).thenReturn(new Terminal.ScalarNode(0.1)).thenReturn(new Terminal.VariableNode(Variable.X));

        when(random.nextMutation(any())).thenReturn(new Mutation.BecomeArgumentMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- X (noise 0.3 0.1 X)))", expr.toString());
    }

    @Test
    public void testScalarBecomeNodeCopyMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(ScalarNode.class));

        when(random.nextNode(any())).thenReturn(picEvolve.parse(expr.getChildren().get(0).toString()));

        when(random.nextMutation(any())).thenReturn(new Mutation.BecomeNodeCopyMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- X (- X 0.3)))", expr.toString());
    }

    @Test
    public void testVectorToRandomExpressionMutation() {

        final Expression expr = picEvolve.parse("(abs (- X #0.123456,0.678101,0.121315))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(VectorNode.class));

        when(random.nextExpression()).thenReturn(picEvolve.parse("(+ 0.1 0.2)"));

        when(random.nextMutation(any())).thenReturn(new Mutation.RandomExpressionMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- X (+ 0.1 0.2)))", expr.toString());
    }

    @Test
    public void testAdjustVectorMutation() {

        final Expression expr = picEvolve.parse("(abs (- X #0.123456,0.678101,0.121315))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(VectorNode.class));

        when(random.nextVector()).thenReturn(new VectorNode(new ArrayList<>(Arrays.asList(0.1, 0.2, 0.3))));

        when(random.nextMutation(any())).thenReturn(new Mutation.AdjustVectorMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- X #0.100000,0.200000,0.300000))", expr.toString());
    }

    @Test
    public void testVectorBecomeArgumentMutation() {

        final Expression expr = picEvolve.parse("(abs (- X #0.123456,0.678101,0.121315))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(VectorNode.class));

        when(random.nextFunction()).thenReturn(new Function.Plus());
        when(random.nextTerminal()).thenReturn(new Terminal.ScalarNode(0.1));

        when(random.nextMutation(any())).thenReturn(new Mutation.BecomeArgumentMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- X (+ #0.123456,0.678101,0.121315 0.1)))", expr.toString());
    }

    @Test
    public void testVectorBecomeNodeCopyMutation() {

        final Expression expr = picEvolve.parse("(abs (- X #0.123456,0.678101,0.121315))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(VectorNode.class));

        when(random.nextNode(any())).thenReturn(picEvolve.parse(expr.getChildren().get(0).toString()));

        when(random.nextMutation(any())).thenReturn(new Mutation.BecomeNodeCopyMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- X (- X #0.123456,0.678101,0.121315)))", expr.toString());
    }

    @Test
    public void testVariableToRandomExpressionMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(VariableNode.class));

        when(random.nextExpression()).thenReturn(picEvolve.parse("(+ 1.0 2.0)"));

        when(random.nextMutation(any())).thenReturn(new Mutation.RandomExpressionMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- (+ 1.0 2.0) 0.3))", expr.toString());
    }

    @Test
    public void testVariableBecomeArgumentMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(VariableNode.class));

        when(random.nextFunction()).thenReturn(new Function.Plus());
        when(random.nextTerminal()).thenReturn(new Terminal.ScalarNode(0.1));

        when(random.nextMutation(any())).thenReturn(new Mutation.BecomeArgumentMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- (+ X 0.1) 0.3))", expr.toString());
    }

    @Test
    public void testVariableBecomeNodeCopyMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(VariableNode.class));

        when(random.nextNode(any())).thenReturn(picEvolve.parse(expr.getChildren().get(0).toString()));

        when(random.nextMutation(any())).thenReturn(new Mutation.BecomeNodeCopyMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (- (- X 0.3) 0.3))", expr.toString());
    }

    @Test
    public void testFunctionToRandomExpressionMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(Minus.class));

        when(random.nextExpression()).thenReturn(picEvolve.parse("(+ 1.0 2.0)"));

        when(random.nextMutation(any())).thenReturn(new Mutation.RandomExpressionMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (+ 1.0 2.0))", expr.toString());
    }

    @Test
    public void testFunctionChangeFunctionMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(Minus.class));

        when(random.nextFunction()).thenReturn(new Noise());
        when(random.nextTerminal()).thenReturn(new VectorNode(new ArrayList<>(Arrays.asList(0.1, 0.2, 0.3))));

        when(random.nextMutation(any())).thenReturn(new Mutation.ChangeFunctionMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (noise X 0.3 #0.100000,0.200000,0.300000))", expr.toString());
    }

    @Test
    public void testFunctionReplaceWithArgumentMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(Minus.class));

        final Expression minusNode = expr.getChildren().get(0);
        when(random.nextChild(any())).thenReturn(minusNode.getChildren().get(1));

        when(random.nextMutation(any())).thenReturn(new Mutation.ReplaceWithArgumentMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs 0.3)", expr.toString());
    }

    @Test
    public void testFunctionBecomeArgumentMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(Minus.class));

        when(random.nextFunction()).thenReturn(new Plus());
        when(random.nextTerminal()).thenReturn(new Terminal.ScalarNode(0.1));

        when(random.nextMutation(any())).thenReturn(new Mutation.BecomeArgumentMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (+ (- X 0.3) 0.1))", expr.toString());
    }

    @Test
    public void testFunctionBecomeNodeCopyMutation() {

        final Expression expr = picEvolve.parse("(abs (- X 0.3))");

        when(random.shouldMutate(any(), anyDouble())).then(new ShouldMutateAnswer(Minus.class));

        when(random.nextNode(any())).thenReturn(picEvolve.parse(expr.toString()));

        when(random.nextMutation(any())).thenReturn(new Mutation.BecomeNodeCopyMutation(random));

        final MutationVisitor visitor = new MutationVisitor(random);

        expr.accept(visitor);

        assertEquals("(abs (abs (- X 0.3)))", expr.toString());
    }

    /**
     * An {@link Answer} to {@link Random#shouldMutate(Expression, double)} that returns true when the specified type of {@link Expression} node is
     * provided
     */
    private static class ShouldMutateAnswer implements Answer<Boolean> {

        private final Class<? extends Expression> nodeType;

        public ShouldMutateAnswer(final Class<? extends Expression> nodeType) {
            this.nodeType = nodeType;
        }

        @Override
        public Boolean answer(final InvocationOnMock invocation) throws Throwable {
            if (invocation.getArguments()[0].getClass().equals(nodeType)) {
                return true;
            }
            return false;
        }
    }
}
