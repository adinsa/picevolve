package com.github.adinsa.picevolve.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.image.Image;

public class ArgumentTest {

    @Test
    public void testScalarArgumentFillsImage() {
        final Argument.ScalarArgument argument = new Argument.ScalarArgument(0.5);

        assertEquals(0.5, argument.getValue(), 0);

        final Image image = argument.toImage(2, 3);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 2; x++) {
                assertEquals(0.5, image.get(x, y).r(), 0);
                assertEquals(0.5, image.get(x, y).g(), 0);
                assertEquals(0.5, image.get(x, y).b(), 0);
            }
        }
    }

    @Test
    public void testVectorArgumentFillsImageWithComponents() {
        final Argument.VectorArgument argument = new Argument.VectorArgument(Arrays.asList(0.1, 0.2, 0.3));

        assertEquals(Arrays.asList(0.1, 0.2, 0.3), argument.getValue());

        final Image image = argument.toImage(2, 2);
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                assertEquals(0.1, image.get(x, y).r(), 0);
                assertEquals(0.2, image.get(x, y).g(), 0);
                assertEquals(0.3, image.get(x, y).b(), 0);
            }
        }
    }

    @Test
    public void testVariableArgumentXGradient() {
        final Argument.VariableArgument argument = new Argument.VariableArgument(Variable.X);
        final Image image = argument.toImage(3, 3);

        assertEquals(-1.0, image.get(0, 0).r(), 1e-9);
        assertEquals(0.0, image.get(1, 0).r(), 1e-9);
        assertEquals(1.0, image.get(2, 0).r(), 1e-9);
        assertEquals(-1.0, image.get(0, 2).r(), 1e-9);
        assertEquals(1.0, image.get(2, 2).r(), 1e-9);
    }

    @Test
    public void testVariableArgumentYGradient() {
        final Argument.VariableArgument argument = new Argument.VariableArgument(Variable.Y);
        final Image image = argument.toImage(3, 3);

        assertEquals(1.0, image.get(0, 0).r(), 1e-9);
        assertEquals(0.0, image.get(0, 1).r(), 1e-9);
        assertEquals(-1.0, image.get(0, 2).r(), 1e-9);
        assertEquals(1.0, image.get(2, 0).r(), 1e-9);
        assertEquals(-1.0, image.get(2, 2).r(), 1e-9);
    }

    @Test
    public void testImageArgumentReturnsWrappedImage() {
        final Image image = new Image(2, 2);
        final Argument.ImageArgument argument = new Argument.ImageArgument(image);

        assertSame(image, argument.getValue());
        assertSame(image, argument.toImage(99, 99));
    }
}
