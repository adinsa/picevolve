package com.github.adinsa.picevolve;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.adinsa.picevolve.Image.Pixel;

/**
 * Non-terminal {@link Expression} node
 *
 * @author amar
 *
 */
public abstract class Function extends Expression {

    private final int arity;
    private final String name;

    public Function(final int arity, final String name) {
        this.arity = arity;
        this.name = name;
    }

    public int getArity() {
        return this.arity;
    }

    public String getName() {
        return this.name;
    }

    public abstract Function copy();

    @Override
    public void accept(final Visitor visitor) {
        for (final Expression child : this.getChildren()) {
            child.accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "(" + this.getName() + " " + this.getChildren().stream()
                .map(child -> child.toString()).collect(Collectors.joining(" "))
                + ")";
    }

    protected Image pixelOperation(final int width, final int height,
            final List<Argument<?>> arguments,
            final java.util.function.Function<Pixel, Pixel> pixelOp) {

        final Image arg = arguments.get(0).toImage(width, height);
        final Image newImage = new Image(width, height);
        IntStream.range(0, height)
                .forEach(y -> IntStream.range(0, width).forEach(
                        x -> newImage.set(x, y, pixelOp.apply(arg.get(x, y)))));

        return newImage;
    }

    protected Image pixelOperation(final int width, final int height,
            final List<Argument<?>> arguments,
            final BiFunction<Pixel, Pixel, Pixel> pixelOp) {

        final Image left = arguments.get(0).toImage(width, height);
        final Image right = arguments.get(1).toImage(width, height);
        final Image newImage = new Image(width, height);
        IntStream.range(0, height).forEach(
                y -> IntStream.range(0, width).forEach(x -> newImage.set(x, y,
                        pixelOp.apply(left.get(x, y), right.get(x, y)))));

        return newImage;
    }

    public static class Plus extends Function {

        public Plus() {
            super(2, "+");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel().r(p1.r() + p2.r())
                            .g(p1.g() + p2.g()).b(p1.b() + p2.b()));

        }

        @Override
        public Function copy() {
            return new Plus();
        }
    }

    public static class Minus extends Function {

        public Minus() {
            super(2, "-");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel().r(p1.r() - p2.r())
                            .g(p1.g() - p2.g()).b(p1.b() - p2.b()));
        }

        @Override
        public Function copy() {
            return new Minus();
        }
    }

    public static class Multiply extends Function {

        public Multiply() {
            super(2, "*");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel().r(p1.r() * p2.r())
                            .g(p1.g() * p2.g()).b(p1.b() * p2.b()));
        }

        @Override
        public Function copy() {
            return new Multiply();
        }
    }

    public static class Divide extends Function {

        public Divide() {
            super(2, "/");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel().r(p2.r() == 0 ? 1 : p1.r() / p2.r())
                            .g(p2.g() == 0 ? 1 : p1.g() / p2.g())
                            .b(p2.b() == 0 ? 1 : p1.b() / p2.b()));
        }

        @Override
        public Function copy() {
            return new Divide();
        }
    }

    public static class Expt extends Function {

        public Expt() {
            super(1, "expt");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p) -> new Pixel().r(Math.exp(p.r())).g(Math.exp(p.g()))
                            .b(Math.exp(p.b())));
        }

        @Override
        public Function copy() {
            return new Expt();
        }
    }

    public static class Log extends Function {

        public Log() {
            super(1, "log");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p) -> new Pixel().r(Math.log(this.clamp(p.r())))
                            .g(Math.log(this.clamp(p.g())))
                            .b(Math.log(this.clamp(p.b()))));
        }

        @Override
        public Function copy() {
            return new Log();
        }

        private double clamp(final double component) {
            return component <= 0 ? Math.E : component;
        }
    }

    public static class Round extends Function {

        public Round() {
            super(1, "round");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p) -> new Pixel().r(Math.round((float) p.r()))
                            .g(Math.round((float) p.g()))
                            .b(Math.round((float) p.b())));
        }

        @Override
        public Function copy() {
            return new Round();
        }
    }

    public static class Sine extends Function {

        public Sine() {
            super(1, "sin");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this
                    .pixelOperation(width, height, arguments,
                            p -> new Pixel().r(Math.sin(p.r()))
                                    .g(Math.sin(p.g())).b(Math.sin(p.b())))
                    .scaled();
        }

        @Override
        public Function copy() {
            return new Sine();
        }
    }

    public static class Cosine extends Function {

        public Cosine() {
            super(1, "cos");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this
                    .pixelOperation(width, height, arguments,
                            p -> new Pixel().r(Math.cos(p.r()))
                                    .g(Math.cos(p.g())).b(Math.cos(p.b())))
                    .scaled();
        }

        @Override
        public Function copy() {
            return new Cosine();
        }
    }

    public static class Tangent extends Function {

        public Tangent() {
            super(1, "tan");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this
                    .pixelOperation(width, height, arguments,
                            p -> new Pixel().r(Math.tan(p.r()))
                                    .g(Math.tan(p.g())).b(Math.tan(p.b())))
                    .scaled();
        }

        @Override
        public Function copy() {
            return new Tangent();
        }
    }

    public static class Min extends Function {

        public Min() {
            super(2, "min");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel().r(Math.min(p1.r(), p2.r()))
                            .g(Math.min(p1.g(), p2.g()))
                            .b(Math.min(p1.b(), p2.b())));
        }

        @Override
        public Function copy() {
            return new Min();
        }
    }

    public static class Max extends Function {

        public Max() {
            super(2, "max");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel().r(Math.max(p1.r(), p2.r()))
                            .g(Math.max(p1.g(), p2.g()))
                            .b(Math.max(p1.b(), p2.b())));
        }

        @Override
        public Function copy() {
            return new Max();
        }
    }

    public static class Abs extends Function {

        public Abs() {
            super(1, "abs");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p) -> new Pixel().r(Math.abs(p.r())).g(Math.abs(p.g()))
                            .b(Math.abs(p.b())));
        }

        @Override
        public Function copy() {
            return new Abs();
        }
    }

    public static class Mod extends Function {

        public Mod() {
            super(2, "mod");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel().r(p2.r() == 0 ? 1 : p1.r() % p2.r())
                            .g(p2.g() == 0 ? 1 : p1.g() % p2.g())
                            .b(p2.b() == 0 ? 1 : p1.b() % p2.b()));
        }

        @Override
        public Function copy() {
            return new Mod();
        }
    }

    public static class And extends Function {

        public And() {
            super(2, "and");
        }

        private int and(final double component1, final double component2) {
            final String bitString1 = Integer.toBinaryString((int) component1);
            final String bitString2 = Integer.toBinaryString((int) component2);

            final StringBuilder sb = new StringBuilder();

            IntStream.range(0, bitString1.length()).forEach(i -> sb.append(
                    bitString1.charAt(i) == '1' && bitString2.charAt(i) == '1'
                            ? '1' : '0'));
            return Integer.parseInt(sb.toString(), 2);
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            final Image left = arguments.get(0).toImage(width, height)
                    .scaled(128, 255);
            final Image right = arguments.get(1).toImage(width, height)
                    .scaled(128, 255);
            final Image newImage = new Image(width, height);
            IntStream.range(0, height)
                    .forEach(
                            y -> IntStream.range(0, width)
                                    .forEach(x -> newImage.set(x, y,
                                            new Pixel()
                                                    .r(this.and(
                                                            left.get(x, y).r(),
                                                            right.get(x, y)
                                                                    .r()))
                                            .g(this.and(left.get(x, y).g(),
                                                    right.get(x, y).g()))
                            .b(this.and(left.get(x, y).b(),
                                    right.get(x, y).b())))));
            return newImage;
        }

        @Override
        public Function copy() {
            return new And();
        }
    }

    public static class Or extends Function {

        public Or() {
            super(2, "or");
        }

        private int or(final double component1, final double component2) {

            final String bitString1 = Integer.toBinaryString((int) component1);
            final String bitString2 = Integer.toBinaryString((int) component2);

            final StringBuilder sb = new StringBuilder();

            IntStream.range(0, bitString1.length()).forEach(i -> sb.append(
                    bitString1.charAt(i) == '1' || bitString2.charAt(i) == '1'
                            ? '1' : '0'));
            return Integer.parseInt(sb.toString(), 2);
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            final Image left = arguments.get(0).toImage(width, height)
                    .scaled(128, 255);
            final Image right = arguments.get(1).toImage(width, height)
                    .scaled(128, 255);
            final Image newImage = new Image(width, height);
            IntStream.range(0, height)
                    .forEach(
                            y -> IntStream.range(0, width)
                                    .forEach(x -> newImage.set(x, y,
                                            new Pixel()
                                                    .r(this.or(
                                                            left.get(x, y).r(),
                                                            right.get(x, y)
                                                                    .r()))
                                            .g(this.or(left.get(x, y).g(),
                                                    right.get(x, y).g()))
                            .b(this.or(left.get(x, y).b(),
                                    right.get(x, y).b())))));
            return newImage;
        }

        @Override
        public Function copy() {
            return new Or();
        }
    }

    public static class Noise extends Function {

        public Noise() {
            super(3, "noise");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            final Image arg1 = arguments.get(0).toImage(width, height).scaled();
            final Image arg2 = arguments.get(1).toImage(width, height).scaled();
            final Image arg3 = arguments.get(2).toImage(width, height).scaled();
            final Image newImage = new Image(width, height);

            IntStream.range(0, height)
                    .forEach(y -> IntStream.range(0, width).forEach(x -> {
                        newImage.set(x, y, new Pixel()
                                .r(ImprovedNoise.noise(arg1.get(x, y).r(),
                                        arg2.get(x, y).r(), arg3.get(x, y).r()))
                                .g(ImprovedNoise.noise(arg1.get(x, y).g(),
                                        arg2.get(x, y).g(), arg3.get(x, y).g()))
                                .b(ImprovedNoise.noise(arg1.get(x, y).b(),
                                        arg2.get(x, y).b(),
                                        arg3.get(x, y).b())));
                    }));
            return newImage;
        }

        @Override
        public Function copy() {
            return new Noise();
        }
    }

    public static class WarpedNoise extends Function {

        public WarpedNoise() {
            super(5, "warped-noise");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            final Image arg1 = arguments.get(0).toImage(width, height).scaled();
            final Image arg2 = arguments.get(1).toImage(width, height).scaled();
            final Image arg3 = arguments.get(2).toImage(width, height).scaled();
            final Image arg4 = arguments.get(3).toImage(width, height).scaled(0,
                    width - 1);
            final Image arg5 = arguments.get(4).toImage(width, height).scaled(0,
                    height - 1);
            final Image newImage = new Image(width, height);

            IntStream.range(0, height)
                    .forEach(y -> IntStream.range(0, width).forEach(x -> {
                        final int newX = (int) Math.floor(arg4.get(x, y).r());
                        final int newY = (int) Math.floor(arg5.get(x, y).g());
                        newImage.set(newX, newY, new Pixel()
                                .r(ImprovedNoise.noise(arg1.get(x, y).r(),
                                        arg2.get(x, y).r(), arg3.get(x, y).r()))
                                .g(ImprovedNoise.noise(arg1.get(x, y).g(),
                                        arg2.get(x, y).g(), arg3.get(x, y).g()))
                                .b(ImprovedNoise.noise(arg1.get(x, y).b(),
                                        arg2.get(x, y).b(),
                                        arg3.get(x, y).b())));
                    }));
            return newImage;
        }

        @Override
        public Function copy() {
            return new WarpedNoise();
        }
    }

    public static class Blur extends Function {

        public Blur() {
            super(1, "blur");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            final int kernelSize = 400;

            final float[] kernelData = new float[kernelSize];
            for (int i = 0; i < kernelSize; i++) {
                kernelData[i] = 1.0f / kernelSize;
            }

            final Image arg1 = arguments.get(0).toImage(width, height).scaled();
            final BufferedImage sourceImage = arg1.asBufferedImage();

            final BufferedImage newSource = new BufferedImage(
                    sourceImage.getWidth() + (int) Math.sqrt(kernelSize),
                    sourceImage.getHeight() + (int) Math.sqrt(kernelSize),
                    BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics2D g2 = newSource.createGraphics();
            g2.drawImage(sourceImage, ((int) Math.sqrt(kernelSize)) / 2,
                    ((int) Math.sqrt(kernelSize)) / 2, null);
            g2.dispose();

            final BufferedImage destImage = null;
            final ConvolveOp op = new ConvolveOp(
                    new Kernel((int) Math.sqrt(kernelSize),
                            (int) Math.sqrt(kernelSize), kernelData),
                    ConvolveOp.EDGE_NO_OP, null);
            final BufferedImage blurredImage = op.filter(newSource, destImage);

            final BufferedImage blurredCropped = new BufferedImage(width,
                    height, BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics g = blurredCropped.getGraphics();
            g.drawImage(
                    blurredImage.getSubimage(((int) Math.sqrt(kernelSize)) / 2,
                            ((int) Math.sqrt(kernelSize)) / 2, width, height),
                    0, 0, null);
            g.dispose();

            return Image.fromBufferedImage(blurredCropped);
        }

        @Override
        public Function copy() {
            return new Blur();
        }
    }

    public static class Sharpen extends Function {

        public Sharpen() {
            super(1, "sharpen");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            final float[] matrix = { 1, 1, 1, 1, -7, 1, 1, 1, 1 };

            final Image arg1 = arguments.get(0).toImage(width, height).scaled();
            final BufferedImage sourceImage = arg1.asBufferedImage();

            final BufferedImage destImage = null;
            final ConvolveOp op = new ConvolveOp(new Kernel(3, 3, matrix),
                    ConvolveOp.EDGE_NO_OP, null);
            final BufferedImage sharpenedImage = op.filter(sourceImage,
                    destImage);

            return Image.fromBufferedImage(sharpenedImage);
        }

        @Override
        public Function copy() {
            return new Sharpen();
        }
    }

    public static class Emboss extends Function {

        public Emboss() {
            super(1, "emboss");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            final float[] matrix = { -1, -1, -1, -1, 0, -1, -1, -1, 0, 1, -1,
                    -1, 0, 1, 1, -1, 0, 1, 1, 1, 0, 1, 1, 1, 1 };

            final Image arg1 = arguments.get(0).toImage(width, height).scaled();
            final BufferedImage sourceImage = arg1.asBufferedImage();

            final BufferedImage destImage = null;
            final ConvolveOp op = new ConvolveOp(new Kernel(5, 5, matrix),
                    ConvolveOp.EDGE_NO_OP, null);
            final BufferedImage embossedImage = op.filter(sourceImage,
                    destImage);

            return Image.fromBufferedImage(embossedImage);
        }

        @Override
        public Function copy() {
            return new Emboss();
        }
    }
}
