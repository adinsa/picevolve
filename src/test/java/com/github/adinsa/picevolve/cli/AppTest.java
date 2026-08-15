package com.github.adinsa.picevolve.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    public void testExpressionIndexOutOfBoundsExceptionMessage() {
        final App.ExpressionIndexOutOfBoundsException ex = new App.ExpressionIndexOutOfBoundsException("boom");
        assertEquals("boom", ex.getMessage());
        assertTrue(ex instanceof IndexOutOfBoundsException);
    }

    @Test
    public void testMutateWithInvalidIndexThrows() throws IOException {
        final App app = new App();
        assertThrows(App.ExpressionIndexOutOfBoundsException.class, () -> app.mutate(0, 5));
    }

    @Test
    public void testMutateWithNegativeIndexThrows() throws IOException {
        final App app = new App();
        assertThrows(App.ExpressionIndexOutOfBoundsException.class, () -> app.mutate(-1, 5));
    }

    @Test
    public void testCrossoverWithInvalidIndexThrows() throws IOException {
        final App app = new App();
        assertThrows(App.ExpressionIndexOutOfBoundsException.class, () -> app.crossover(0, 0, 5));
    }

    @Test
    public void testGenerateWithInvalidIndexThrows() throws IOException {
        final App app = new App();
        assertThrows(App.ExpressionIndexOutOfBoundsException.class, () -> app.generate(0, 100, 100, "out.png"));
    }

    @Test
    public void testMutateReportsIndexInMessage() throws IOException {
        final App app = new App();
        final App.ExpressionIndexOutOfBoundsException ex = assertThrows(App.ExpressionIndexOutOfBoundsException.class,
                () -> app.mutate(3, 5));
        assertTrue(ex.getMessage().contains("3"));
    }
}
