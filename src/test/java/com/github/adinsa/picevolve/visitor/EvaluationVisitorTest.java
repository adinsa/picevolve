package com.github.adinsa.picevolve.visitor;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.Test;

import com.github.adinsa.picevolve.PicEvolve;
import com.github.adinsa.picevolve.visitor.EvaluatorVisitor;

public class EvaluationVisitorTest {

    private PicEvolve picEvolve;
    
    @Before
    public void setup() {
        picEvolve = new PicEvolve();
    }
    
    @Test
    public void testX() {

        EvaluatorVisitor evaluator = new EvaluatorVisitor(3, 3);
        picEvolve.parse("x").accept(evaluator);
        
        assertArrayEquals(new double[] {
             -1.0, -1.0, -1.0,  0.0, 0.0, 0.0,  1.0, 1.0, 1.0,
             -1.0, -1.0, -1.0,  0.0, 0.0, 0.0,  1.0, 1.0, 1.0,
             -1.0, -1.0, -1.0,  0.0, 0.0, 0.0,  1.0, 1.0, 1.0
        }, evaluator.getImage().asDoubleArray(), 0);
    }
    
    @Test
    public void testY() {

        EvaluatorVisitor evaluator = new EvaluatorVisitor(3, 3);
        picEvolve.parse("y").accept(evaluator);
        
        assertArrayEquals(new double[] {
              1.0,  1.0,  1.0,   1.0,  1.0,  1.0,   1.0,  1.0,  1.0,
              0.0,  0.0,  0.0,   0.0,  0.0,  0.0,   0.0,  0.0,  0.0, 
             -1.0, -1.0, -1.0,  -1.0, -1.0, -1.0,  -1.0, -1.0, -1.0,
        },  evaluator.getImage().asDoubleArray(), 0);
    }

    @Test
    public void testExpression() {

        EvaluatorVisitor evaluator = new EvaluatorVisitor(3, 3);
        picEvolve.parse("(abs (- x y))").accept(evaluator);
        
        assertArrayEquals(new double[] {
              2.0, 2.0, 2.0,  1.0, 1.0, 1.0,  0.0, 0.0, 0.0,
              1.0, 1.0, 1.0,  0.0, 0.0, 0.0,  1.0, 1.0, 1.0,
              0.0, 0.0, 0.0,  1.0, 1.0, 1.0,  2.0, 2.0, 2.0,
        },  evaluator.getImage().asDoubleArray(), 0);
    }
}
