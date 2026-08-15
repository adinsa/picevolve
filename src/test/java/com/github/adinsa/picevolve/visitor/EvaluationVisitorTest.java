package com.github.adinsa.picevolve.visitor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.PicEvolve;

public class EvaluationVisitorTest {

    private PicEvolve picEvolve;

    @BeforeEach
    public void setup() {
        picEvolve = new PicEvolve();
    }

    @Test
    public void testX() {

        final EvaluatorVisitor evaluator = new EvaluatorVisitor(3, 3);
        picEvolve.parse("x").accept(evaluator);

        // @formatter:off
        assertArrayEquals(new double[] {
                -1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0,
                -1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0,
                -1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0
        }, evaluator.getImage().asDoubleArray(), 0);
        // @formatter:on
    }

    @Test
    public void testY() {

        final EvaluatorVisitor evaluator = new EvaluatorVisitor(3, 3);
        picEvolve.parse("y").accept(evaluator);

        // @formatter:off
        assertArrayEquals(new double[] {
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,
        }, evaluator.getImage().asDoubleArray(), 0);
        // @formatter:on
    }

    @Test
    public void testExpression() {

        final EvaluatorVisitor evaluator = new EvaluatorVisitor(3, 3);
        picEvolve.parse("(abs (- x y))").accept(evaluator);

        // @formatter:off
        assertArrayEquals(new double[] {
                2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0,
                1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0,
                0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0,
        }, evaluator.getImage().asDoubleArray(), 0);
        // @formatter:on
    }

    @Test
    public void testNonCommutativeFunctionPreservesArgumentOrder() {

        final EvaluatorVisitor evaluator = new EvaluatorVisitor(3, 3);
        picEvolve.parse("(- x y)").accept(evaluator);

        // (- x y) must evaluate as x - y, not y - x
        // @formatter:off
        assertArrayEquals(new double[] {
                -2.0, -2.0, -2.0, -1.0, -1.0, -1.0, 0.0, 0.0, 0.0,
                -1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0,
                0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0,
        }, evaluator.getImage().asDoubleArray(), 0);
        // @formatter:on
    }
}
