package com.github.adinsa.picevolve.expression;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.adinsa.picevolve.Image;
import com.github.adinsa.picevolve.Image.Pixel;
import com.github.adinsa.picevolve.visitor.Visitor;

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

            return this.pixelOperation(width, height, arguments,
                    p -> new Pixel().r(Math.sin(p.r())).g(Math.sin(p.g()))
                            .b(Math.sin(p.b())));
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

            return this.pixelOperation(width, height, arguments,
                    p -> new Pixel().r(Math.cos(p.r())).g(Math.cos(p.g()))
                            .b(Math.cos(p.b())));
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

            return this.pixelOperation(width, height, arguments,
                    p -> new Pixel().r(Math.tan(p.r())).g(Math.tan(p.g()))
                            .b(Math.tan(p.b())));
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

    public static class IntAnd extends Function {

        public IntAnd() {
            super(2, "int-and");
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
                    .forEach(y -> IntStream.range(0, width)
                            .forEach(x -> newImage.set(x, y,
                                    new Pixel()
                                            .r((int) left.get(x, y).r()
                                                    & (int) right.get(x, y).r())
                                            .g((int) left.get(x, y).g()
                                                    & (int) right.get(x, y).g())
                            .b((int) left.get(x, y).b()
                                    & (int) right.get(x, y).b()))));

            return newImage;
        }

        @Override
        public Function copy() {
            return new IntAnd();
        }
    }

    public static class IntOr extends Function {

        public IntOr() {
            super(2, "int-or");
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
                    .forEach(y -> IntStream.range(0, width)
                            .forEach(x -> newImage.set(x, y,
                                    new Pixel()
                                            .r((int) left.get(x, y).r()
                                                    | (int) right.get(x, y).r())
                                            .g((int) left.get(x, y).g()
                                                    | (int) right.get(x, y).g())
                            .b((int) left.get(x, y).b()
                                    | (int) right.get(x, y).b()))));

            return newImage;
        }

        @Override
        public Function copy() {
            return new IntOr();
        }
    }

    public static class IntXor extends Function {

        public IntXor() {
            super(2, "int-xor");
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
                    .forEach(y -> IntStream.range(0, width)
                            .forEach(x -> newImage.set(x, y,
                                    new Pixel()
                                            .r((int) left.get(x, y).r()
                                                    ^ (int) right.get(x, y).r())
                                            .g((int) left.get(x, y).g()
                                                    ^ (int) right.get(x, y).g())
                            .b((int) left.get(x, y).b()
                                    ^ (int) right.get(x, y).b()))));

            return newImage;
        }

        @Override
        public Function copy() {
            return new IntXor();
        }
    }

    public static class FloatAnd extends Function {

        public FloatAnd() {
            super(2, "float-and");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel()
                            .r(Double.longBitsToDouble(
                                    Double.doubleToLongBits(p1.r())
                                            & Double.doubleToLongBits(p2.r())))
                            .g(Double.longBitsToDouble(
                                    Double.doubleToLongBits(p1.g())
                                            & Double.doubleToLongBits(p2.g())))
                            .b(Double.longBitsToDouble(
                                    Double.doubleToLongBits(p1.b()) & Double
                                            .doubleToLongBits(p2.b()))));
        }

        @Override
        public Function copy() {
            return new FloatAnd();
        }
    }

    public static class FloatOr extends Function {

        public FloatOr() {
            super(2, "float-or");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel()
                            .r(Double.longBitsToDouble(
                                    Double.doubleToLongBits(p1.r())
                                            | Double.doubleToLongBits(p2.r())))
                            .g(Double.longBitsToDouble(
                                    Double.doubleToLongBits(p1.g())
                                            | Double.doubleToLongBits(p2.g())))
                            .b(Double.longBitsToDouble(
                                    Double.doubleToLongBits(p1.b()) | Double
                                            .doubleToLongBits(p2.b()))));
        }

        @Override
        public Function copy() {
            return new FloatOr();
        }
    }

    public static class FloatXor extends Function {

        public FloatXor() {
            super(2, "float-xor");
        }

        @Override
        public Image interpret(final int width, final int height,
                final List<Argument<?>> arguments) {

            return this.pixelOperation(width, height, arguments,
                    (p1, p2) -> new Pixel()
                            .r(Double.longBitsToDouble(
                                    Double.doubleToLongBits(p1.r())
                                            ^ Double.doubleToLongBits(p2.r())))
                            .g(Double.longBitsToDouble(
                                    Double.doubleToLongBits(p1.g())
                                            ^ Double.doubleToLongBits(p2.g())))
                            .b(Double.longBitsToDouble(
                                    Double.doubleToLongBits(p1.b()) ^ Double
                                            .doubleToLongBits(p2.b()))));
        }

        @Override
        public Function copy() {
            return new FloatXor();
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

    /* @formatter:off */
    
    /**
     * @see <a href="http://mrl.nyu.edu/~perlin/noise/">http://mrl.nyu.edu/~perlin/noise/</a>
     */
    // JAVA REFERENCE IMPLEMENTATION OF IMPROVED NOISE - COPYRIGHT 2002 KEN PERLIN.
    private static final class ImprovedNoise {
       static public double noise(double x, double y, double z) {
          int X = (int)Math.floor(x) & 255,                  // FIND UNIT CUBE THAT
              Y = (int)Math.floor(y) & 255,                  // CONTAINS POINT.
              Z = (int)Math.floor(z) & 255;
          x -= Math.floor(x);                                // FIND RELATIVE X,Y,Z
          y -= Math.floor(y);                                // OF POINT IN CUBE.
          z -= Math.floor(z);
          double u = fade(x),                                // COMPUTE FADE CURVES
                 v = fade(y),                                // FOR EACH OF X,Y,Z.
                 w = fade(z);
          int A = p[X  ]+Y, AA = p[A]+Z, AB = p[A+1]+Z,      // HASH COORDINATES OF
              B = p[X+1]+Y, BA = p[B]+Z, BB = p[B+1]+Z;      // THE 8 CUBE CORNERS,
    
          return lerp(w, lerp(v, lerp(u, grad(p[AA  ], x  , y  , z   ),  // AND ADD
                                         grad(p[BA  ], x-1, y  , z   )), // BLENDED
                                 lerp(u, grad(p[AB  ], x  , y-1, z   ),  // RESULTS
                                         grad(p[BB  ], x-1, y-1, z   ))),// FROM  8
                         lerp(v, lerp(u, grad(p[AA+1], x  , y  , z-1 ),  // CORNERS
                                         grad(p[BA+1], x-1, y  , z-1 )), // OF CUBE
                                 lerp(u, grad(p[AB+1], x  , y-1, z-1 ),
                                         grad(p[BB+1], x-1, y-1, z-1 ))));
       }
       static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
       static double lerp(double t, double a, double b) { return a + t * (b - a); }
       static double grad(int hash, double x, double y, double z) {
          int h = hash & 15;                      // CONVERT LO 4 BITS OF HASH CODE
          double u = h<8 ? x : y,                 // INTO 12 GRADIENT DIRECTIONS.
                 v = h<4 ? y : h==12||h==14 ? x : z;
          return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
       }
       static final int p[] = new int[512], permutation[] = { 151,160,137,91,90,15,
       131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
       190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
       88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
       77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
       102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
       135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
       5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
       223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
       129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
       251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
       49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
       138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
       };
       static { for (int i=0; i < 256 ; i++) p[256+i] = p[i] = permutation[i]; }
    }
    
    /* @formatter:on */

}
