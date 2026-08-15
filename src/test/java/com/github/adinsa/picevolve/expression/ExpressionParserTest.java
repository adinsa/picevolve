package com.github.adinsa.picevolve.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;

public class ExpressionParserTest {

    private ExpressionParser parser;

    @BeforeEach
    public void setup() {
        parser = new ExpressionParser(FunctionSet.createDefault());
    }

    @Test
    public void testParseExpression() {

        final Expression expr = parser.parse("(abs (- x 0.3))");

        assertNull(expr.getParent(), "Root node's parent should be null");

        assertEquals("abs", ((Function) expr).getName());

        final Function minus = (Function) expr.getChildren().get(0);

        assertEquals("-", minus.getName());

        assertTrue(minus.getParent() == expr);

        final VariableNode x = (VariableNode) minus.getChildren().get(0);
        final ScalarNode scalar = (ScalarNode) minus.getChildren().get(1);

        assertEquals(Variable.X, x.getValue());
        assertEquals(0.3, scalar.getValue(), 0);
    }

    @Test
    public void testParseEmptyExpressionThrows() {

        assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   "));
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
        assertEquals("Cannot parse empty expression", ex.getMessage());
    }

    @Test
    public void testParseMissingArgumentThrows() {

        assertThrows(IllegalArgumentException.class, () -> parser.parse("(+ 1)"));
    }

    @Test
    public void testParseExtraTokenThrows() {

        assertThrows(IllegalArgumentException.class, () -> parser.parse("(+ 1 2 3)"));
    }

    @Test
    public void testParseUnbalancedParenthesesThrows() {

        assertThrows(IllegalArgumentException.class, () -> parser.parse("(abs (- x 0.3)"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("(abs (- x 0.3)))"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse(")"));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parser.parse(")("));
        assertEquals("Unbalanced parentheses in: )(", ex.getMessage());
    }

    @Test
    public void testParseMalformedVectorThrows() {

        assertThrows(IllegalArgumentException.class, () -> parser.parse("#0.1,0.2"));
    }

    @Test
    public void testVectorToStringRoundTripsPrecisely() {

        final List<Double> values = Arrays.asList(0.123456789012345, 0.987654321098765, 0.555555555555555);
        final VectorNode vectorNode = new VectorNode(new ArrayList<>(values));

        final Expression parsed = parser.parse(vectorNode.toString());

        final VectorNode result = (VectorNode) parsed;
        assertEquals(values.get(0), result.getValue().get(0), 0);
        assertEquals(values.get(1), result.getValue().get(1), 0);
        assertEquals(values.get(2), result.getValue().get(2), 0);
    }
}
