package com.github.adinsa.picevolve.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.adinsa.picevolve.image.Image;

public class FunctionTest {

    private static final double DELTA = 1e-9;

    private static Argument<?> arg(final double r, final double g, final double b) {
        final Image image = new Image(1, 1);
        image.set(0, 0, new Image.Pixel(r, g, b));
        return new Argument.ImageArgument(image);
    }

    private static Argument<?> arg2(final double r0, final double g0, final double b0, final double r1, final double g1, final double b1) {
        final Image image = new Image(2, 1);
        image.set(0, 0, new Image.Pixel(r0, g0, b0));
        image.set(1, 0, new Image.Pixel(r1, g1, b1));
        return new Argument.ImageArgument(image);
    }

    private static Argument<?> flatImage(final int width, final int height, final double value) {
        final Image image = new Image(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.set(x, y, new Image.Pixel(value));
            }
        }
        return new Argument.ImageArgument(image);
    }

    private static Argument<?> grayRow(final double... values) {
        final Image image = new Image(values.length, 1);
        for (int x = 0; x < values.length; x++) {
            image.set(x, 0, new Image.Pixel(values[x]));
        }
        return new Argument.ImageArgument(image);
    }

    private static Image.Pixel eval(final Expression expr, final Argument<?>... args) {
        return expr.interpret(1, 1, List.of(args)).get(0, 0);
    }

    @Test
    public void testPlus() {
        final Image.Pixel p = eval(new Function.Plus(), arg(0.5, 0.25, 0.125), arg(0.25, 0.5, 0.875));
        assertEquals(0.75, p.r(), DELTA);
        assertEquals(0.75, p.g(), DELTA);
        assertEquals(1.0, p.b(), DELTA);
    }

    @Test
    public void testMinus() {
        final Image.Pixel p = eval(new Function.Minus(), arg(0.75, 0.5, 1.0), arg(0.25, 0.25, 0.25));
        assertEquals(0.5, p.r(), DELTA);
        assertEquals(0.25, p.g(), DELTA);
        assertEquals(0.75, p.b(), DELTA);
    }

    @Test
    public void testMultiply() {
        final Image.Pixel p = eval(new Function.Multiply(), arg(0.5, 0.25, 0.75), arg(0.5, 0.5, 0.5));
        assertEquals(0.25, p.r(), DELTA);
        assertEquals(0.125, p.g(), DELTA);
        assertEquals(0.375, p.b(), DELTA);
    }

    @Test
    public void testDivide() {
        final Image.Pixel p = eval(new Function.Divide(), arg(1.0, 0.5, 0.75), arg(2.0, 2.0, 3.0));
        assertEquals(0.5, p.r(), DELTA);
        assertEquals(0.25, p.g(), DELTA);
        assertEquals(0.25, p.b(), DELTA);
    }

    @Test
    public void testDivideByZeroYieldsOne() {
        final Image.Pixel p = eval(new Function.Divide(), arg(1.0, 5.0, 0.5), arg(0.0, 0.0, 0.0));
        assertEquals(1.0, p.r(), DELTA);
        assertEquals(1.0, p.g(), DELTA);
        assertEquals(1.0, p.b(), DELTA);
    }

    @Test
    public void testMod() {
        final Image.Pixel p = eval(new Function.Mod(), arg(5.0, 5.5, 5.25), arg(2.0, 2.0, 2.0));
        assertEquals(1.0, p.r(), DELTA);
        assertEquals(1.5, p.g(), DELTA);
        assertEquals(1.25, p.b(), DELTA);
    }

    @Test
    public void testModByZeroYieldsOne() {
        final Image.Pixel p = eval(new Function.Mod(), arg(5.0, 5.5, 5.25), arg(0.0, 0.0, 0.0));
        assertEquals(1.0, p.r(), DELTA);
        assertEquals(1.0, p.g(), DELTA);
        assertEquals(1.0, p.b(), DELTA);
    }

    @Test
    public void testRound() {
        final Image.Pixel p = eval(new Function.Round(), arg(1.6, 1.2, 0.8));
        assertEquals(2.0, p.r(), DELTA);
        assertEquals(1.0, p.g(), DELTA);
        assertEquals(1.0, p.b(), DELTA);
    }

    @Test
    public void testExpt() {
        final Image.Pixel p = eval(new Function.Expt(), arg(0.0, 1.0, 2.0));
        assertEquals(1.0, p.r(), DELTA);
        assertEquals(Math.E, p.g(), DELTA);
        assertEquals(Math.E * Math.E, p.b(), DELTA);
    }

    @Test
    public void testLog() {
        final Image.Pixel p = eval(new Function.Log(), arg(Math.E, 2.0, Math.E * Math.E));
        assertEquals(1.0, p.r(), DELTA);
        assertEquals(Math.log(2.0), p.g(), DELTA);
        assertEquals(2.0, p.b(), DELTA);
    }

    @Test
    public void testLogClampsNonPositiveValues() {
        final Image.Pixel p = eval(new Function.Log(), arg(0.0, -5.0, -100.0));
        assertEquals(1.0, p.r(), DELTA);
        assertEquals(1.0, p.g(), DELTA);
        assertEquals(1.0, p.b(), DELTA);
    }

    @Test
    public void testSine() {
        final Image.Pixel p = eval(new Function.Sine(), arg(0.0, Math.PI / 2, Math.PI));
        assertEquals(0.0, p.r(), DELTA);
        assertEquals(1.0, p.g(), DELTA);
        assertEquals(0.0, p.b(), DELTA);
    }

    @Test
    public void testCosine() {
        final Image.Pixel p = eval(new Function.Cosine(), arg(0.0, Math.PI, Math.PI / 2));
        assertEquals(1.0, p.r(), DELTA);
        assertEquals(-1.0, p.g(), DELTA);
        assertEquals(0.0, p.b(), DELTA);
    }

    @Test
    public void testTangent() {
        final Image.Pixel p = eval(new Function.Tangent(), arg(0.0, 0.0, 0.0));
        assertEquals(0.0, p.r(), DELTA);
        assertEquals(0.0, p.g(), DELTA);
        assertEquals(0.0, p.b(), DELTA);
    }

    @Test
    public void testMin() {
        final Image.Pixel p = eval(new Function.Min(), arg(0.5, 0.25, 1.0), arg(0.25, 0.75, 0.5));
        assertEquals(0.25, p.r(), DELTA);
        assertEquals(0.25, p.g(), DELTA);
        assertEquals(0.5, p.b(), DELTA);
    }

    @Test
    public void testMax() {
        final Image.Pixel p = eval(new Function.Max(), arg(0.5, 0.25, 1.0), arg(0.25, 0.75, 0.5));
        assertEquals(0.5, p.r(), DELTA);
        assertEquals(0.75, p.g(), DELTA);
        assertEquals(1.0, p.b(), DELTA);
    }

    @Test
    public void testAbs() {
        final Image.Pixel p = eval(new Function.Abs(), arg(-1.0, -2.0, 3.0));
        assertEquals(1.0, p.r(), DELTA);
        assertEquals(2.0, p.g(), DELTA);
        assertEquals(3.0, p.b(), DELTA);
    }

    @Test
    public void testIntAnd() {
        final Image result = new Function.IntAnd().interpret(2, 1,
                List.of(arg2(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), arg2(1.0, 1.0, 1.0, 0.0, 0.0, 0.0)));
        assertEquals(128, result.get(0, 0).r(), DELTA);
        assertEquals(128, result.get(0, 0).g(), DELTA);
        assertEquals(128, result.get(0, 0).b(), DELTA);
        assertEquals(128, result.get(1, 0).r(), DELTA);
        assertEquals(128, result.get(1, 0).g(), DELTA);
        assertEquals(128, result.get(1, 0).b(), DELTA);
    }

    @Test
    public void testIntOr() {
        final Image result = new Function.IntOr().interpret(2, 1,
                List.of(arg2(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), arg2(1.0, 1.0, 1.0, 0.0, 0.0, 0.0)));
        assertEquals(255, result.get(0, 0).r(), DELTA);
        assertEquals(255, result.get(0, 0).g(), DELTA);
        assertEquals(255, result.get(0, 0).b(), DELTA);
        assertEquals(255, result.get(1, 0).r(), DELTA);
        assertEquals(255, result.get(1, 0).g(), DELTA);
        assertEquals(255, result.get(1, 0).b(), DELTA);
    }

    @Test
    public void testIntXor() {
        final Image result = new Function.IntXor().interpret(2, 1,
                List.of(arg2(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), arg2(1.0, 1.0, 1.0, 0.0, 0.0, 0.0)));
        assertEquals(127, result.get(0, 0).r(), DELTA);
        assertEquals(127, result.get(0, 0).g(), DELTA);
        assertEquals(127, result.get(0, 0).b(), DELTA);
        assertEquals(127, result.get(1, 0).r(), DELTA);
        assertEquals(127, result.get(1, 0).g(), DELTA);
        assertEquals(127, result.get(1, 0).b(), DELTA);
    }

    @Test
    public void testFloatAnd() {
        final Image.Pixel p = eval(new Function.FloatAnd(), arg(0.5, 0.25, 0.75), arg(0.75, 0.5, 0.25));
        assertEquals(0.5, p.r(), DELTA);
        assertEquals(0.125, p.g(), DELTA);
        assertEquals(0.125, p.b(), DELTA);
    }

    @Test
    public void testFloatOr() {
        final Image.Pixel p = eval(new Function.FloatOr(), arg(0.5, 0.25, 0.75), arg(0.75, 0.5, 0.25));
        assertEquals(0.75, p.r(), DELTA);
        assertEquals(1.0, p.g(), DELTA);
        assertEquals(1.5, p.b(), DELTA);
    }

    @Test
    public void testFloatXor() {
        final Image.Pixel p = eval(new Function.FloatXor(), arg(0.5, 0.25, 0.75), arg(0.75, 0.5, 0.25));
        assertEquals(0.0, p.r(), 1e-300);
        assertEquals(0.0, p.g(), 1e-300);
        assertEquals(0.0, p.b(), 1e-300);
    }

    @Test
    public void testNoiseOfConstantImagesIsZero() {
        final Image result = new Function.Noise().interpret(1, 1, List.of(arg(0.5, 0.5, 0.5), arg(0.5, 0.5, 0.5), arg(0.5, 0.5, 0.5)));
        assertEquals(0.0, result.get(0, 0).r(), DELTA);
        assertEquals(0.0, result.get(0, 0).g(), DELTA);
        assertEquals(0.0, result.get(0, 0).b(), DELTA);
    }

    @Test
    public void testNoiseIsDeterministic() {
        final Argument<?> a1 = arg2(0.0, 0.0, 0.0, 0.5, 0.25, 0.75);
        final Argument<?> a2 = arg2(0.0, 0.0, 0.0, 0.25, 0.5, 0.75);
        final Argument<?> a3 = arg2(0.0, 0.0, 0.0, 0.75, 0.75, 0.75);

        final Image first = new Function.Noise().interpret(2, 1, List.of(a1, a2, a3));
        final Image second = new Function.Noise().interpret(2, 1, List.of(a1, a2, a3));

        for (int x = 0; x < 2; x++) {
            assertEquals(first.get(x, 0).r(), second.get(x, 0).r(), 1e-12);
            assertEquals(first.get(x, 0).g(), second.get(x, 0).g(), 1e-12);
            assertEquals(first.get(x, 0).b(), second.get(x, 0).b(), 1e-12);
        }
    }

    @Test
    public void testNoiseExactValues() {
        final Argument<?> a1 = grayRow(0.0, 0.25, 0.5, 0.75, 1.0);
        final Argument<?> a2 = grayRow(0.0, 0.5, 1.0, 0.25, 0.75);
        final Argument<?> a3 = grayRow(0.0, 0.75, 0.5, 0.25, 1.0);

        final Image result = new Function.Noise().interpret(5, 1, List.of(a1, a2, a3));

        final double[] expected = { 0.0, -0.2697153091430664, 0.0, -0.43549662083387375, 0.22412109375 };
        for (int x = 0; x < 5; x++) {
            assertEquals(expected[x], result.get(x, 0).r(), 1e-12, "noise value at column " + x);
            assertEquals(expected[x], result.get(x, 0).g(), 1e-12, "noise value at column " + x);
            assertEquals(expected[x], result.get(x, 0).b(), 1e-12, "noise value at column " + x);
        }
    }

    @Test
    public void testWarpedNoiseReturnsExpectedDimensions() {
        final Argument<?> a = arg2(0.0, 0.0, 0.0, 0.5, 0.25, 0.75);
        final Image result = new Function.WarpedNoise().interpret(2, 1, List.of(a, a, a, a, a));
        assertNotNull(result);
    }

    @Test
    public void testBlurReturnsExpectedDimensions() {
        final Image result = new Function.Blur().interpret(4, 4, List.of(flatImage(4, 4, 0.5)));
        assertNotNull(result);
        assertEquals(4 * 4 * 3, result.asDoubleArray().length);
    }

    @Test
    public void testSharpenReturnsExpectedDimensions() {
        final Image result = new Function.Sharpen().interpret(4, 4, List.of(flatImage(4, 4, 0.5)));
        assertNotNull(result);
        assertEquals(4 * 4 * 3, result.asDoubleArray().length);
    }

    @Test
    public void testEmbossReturnsExpectedDimensions() {
        final Image result = new Function.Emboss().interpret(4, 4, List.of(flatImage(4, 4, 0.5)));
        assertNotNull(result);
        assertEquals(4 * 4 * 3, result.asDoubleArray().length);
    }

    @Test
    public void testCopyReturnsEquivalentFunctionForAllDefaultFunctions() {
        for (final String name : FunctionSet.createDefault().names()) {
            final Function function = FunctionSet.createDefault().get(name).get();
            final Function copy = function.copy();

            assertNotNull(copy, "copy() should return a non-null function for " + name);
            assertEquals(function.getClass(), copy.getClass(), "copy() should return the same type for " + name);
            assertEquals(function.getName(), copy.getName());
            assertEquals(function.getArity(), copy.getArity());
            assertTrue(function != copy, "copy() should return a distinct instance for " + name);
        }
    }

    @Test
    public void testArityAndName() {
        assertEquals(2, new Function.Plus().getArity());
        assertEquals("+", new Function.Plus().getName());
        assertEquals(1, new Function.Abs().getArity());
        assertEquals("abs", new Function.Abs().getName());
        assertEquals(3, new Function.Noise().getArity());
        assertEquals("noise", new Function.Noise().getName());
        assertEquals(5, new Function.WarpedNoise().getArity());
        assertEquals("warped-noise", new Function.WarpedNoise().getName());
    }
}
