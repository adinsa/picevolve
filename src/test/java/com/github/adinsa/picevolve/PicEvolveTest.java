package com.github.adinsa.picevolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Variable;

public class PicEvolveTest {

    @Test
    public void testParseExpression() {

        final PicEvolve picEvolve = new PicEvolve();

        final Expression expr = picEvolve.parse("(abs (- x 0.3))");

        assertNull("Root node's parent should be null", expr.getParent());

        assertEquals("abs", ((Function) expr).getName());

        final Function minus = (Function) expr.getChildren().get(0);

        assertEquals("-", minus.getName());

        assertTrue(minus.getParent() == expr);

        final VariableNode x = (VariableNode) minus.getChildren().get(0);
        final ScalarNode scalar = (ScalarNode) minus.getChildren().get(1);

        assertEquals(Variable.X, x.getValue());
        assertEquals(0.3, scalar.getValue(), 0);
    }
}