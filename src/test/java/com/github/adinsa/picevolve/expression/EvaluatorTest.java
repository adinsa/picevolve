package com.github.adinsa.picevolve.expression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EvaluatorTest {

    private ExpressionParser parser;

    @BeforeEach
    public void setup() {
        parser = new ExpressionParser(FunctionSet.createDefault());
    }

    @Test
    public void testX() {

        final Evaluator evaluator = new Evaluator(3, 3);
        parser.parse("x").accept(evaluator);

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

        final Evaluator evaluator = new Evaluator(3, 3);
        parser.parse("y").accept(evaluator);

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

        final Evaluator evaluator = new Evaluator(3, 3);
        parser.parse("(abs (- x y))").accept(evaluator);

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

        final Evaluator evaluator = new Evaluator(3, 3);
        parser.parse("(- x y)").accept(evaluator);

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
