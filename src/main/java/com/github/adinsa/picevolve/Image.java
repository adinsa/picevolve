package com.github.adinsa.picevolve;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.github.adinsa.picevolve.expression.Expression;

/**
 * A 2D image containing pixels with RGB color values. The phenotype of a PicEvolve {@link Expression}.
 *
 * @author amar
 *
 */
public class Image {

    private final Pixel[][] pixels;
    private final int width;
    private final int height;

    public Image(final int width, final int height) {

        this.width = width;
        this.height = height;

        pixels = new Pixel[height][width];
        IntStream.range(0, height).forEach(y -> Arrays.fill(pixels[y], new Pixel(0)));
    }

    public Pixel get(final int x, final int y) {
        return new Pixel(pixels[y][x]);
    }

    public void set(final int x, final int y, final Pixel pixel) {
        pixels[y][x] = pixel;
    }

    /**
     * Returns copy of Image with all (r,g,b) values scaled between 0 and 1.
     *
     * @return
     */
    public Image scaled() {
        return this.scaled(0, 1);
    }

    /**
     * Returns copy of Image with all (r,g,b) values scaled between the minimum and maximum values specified.
     *
     * @param minimum
     * @param maximum
     * @return
     */
    public Image scaled(final double minimum, final double maximum) {

        final double oldMax = asDoubleStream().max().getAsDouble();
        final double oldMin = asDoubleStream().min().getAsDouble();

        final Image scaledImage = new Image(width, height);

        final Function<Double, Double> scaleFunc = component -> oldMax - oldMin == 0 ? minimum
                : (component - oldMin) * (maximum - minimum) / (oldMax - oldMin) + minimum;

        IntStream.range(0, height)
                .forEach(y -> IntStream.range(0, width)
                        .forEach(x -> scaledImage.set(x, y, get(x, y).r(scaleFunc.apply(get(x, y).r()))
                                .g(scaleFunc.apply(get(x, y).g())).b(scaleFunc.apply(get(x, y).b())))));

        return scaledImage;
    }

    /**
     * Writes image to file in the specified format.
     *
     * @param file
     * @param formatName
     */
    public void write(final File file, final String formatName) {
        final BufferedImage buf = asBufferedImage();
        try {
            ImageIO.write(buf, formatName, file);
        } catch (final IOException e) {
            throw new RuntimeException(String.format("Error writing file: '%s'", file.getPath()));
        }
    }

    /**
     * Converts Image to a {@link BufferedImage}.
     *
     * @return
     */
    public BufferedImage asBufferedImage() {
        final BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics graphics = buf.getGraphics();
        IntStream.range(0, pixels.length).forEach(y -> IntStream.range(0, pixels[y].length).forEach(x -> {
            graphics.setColor(new Color((float) get(x, y).r(), (float) get(x, y).g(), (float) get(x, y).b()));
            graphics.drawRect(x, y, 1, 1);
        }));
        return buf;
    }

    /**
     * Creates Image from a {@link BufferedImage}.
     *
     * @param buf
     * @return
     */
    public static Image fromBufferedImage(final BufferedImage buf) {

        final Image image = new Image(buf.getWidth(), buf.getHeight());
        final byte[] pixels = ((DataBufferByte) buf.getRaster().getDataBuffer()).getData();

        for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += 4) {
            int argb = 0;
            argb += (pixels[pixel] & 0xff) << 24; // alpha
            argb += pixels[pixel + 1] & 0xff; // blue
            argb += (pixels[pixel + 2] & 0xff) << 8; // green
            argb += (pixels[pixel + 3] & 0xff) << 16; // red
            final Color color = new Color(argb);
            image.set(col, row, new Pixel(color.getRed(), color.getBlue(), color.getBlue()));
            col++;
            if (col == buf.getWidth()) {
                col = 0;
                row++;
            }
        }

        return image;
    }

    public double[] asDoubleArray() {
        return asDoubleStream().toArray();
    }

    private DoubleStream asDoubleStream() {
        return Arrays.stream(pixels).flatMap(row -> Arrays.stream(row))
                .flatMap(pixel -> Stream.of(pixel.r(), pixel.g(), pixel.b())).mapToDouble(d -> d);
    }

    @Override
    public String toString() {
        return Arrays.stream(pixels).map(row -> Arrays.toString(row)).collect(Collectors.joining("\n"));
    }

    public static class Pixel {

        private double red;
        private double green;
        private double blue;

        public Pixel(final double red, final double green, final double blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public Pixel(final double value) {
            this(value, value, value);
        }

        public Pixel(final Pixel pixel) {
            this(pixel.r(), pixel.g(), pixel.b());
        }

        public Pixel() {
        }

        public Pixel r(final double red) {
            this.red = red;
            return this;
        }

        public Pixel g(final double green) {
            this.green = green;
            return this;
        }

        public Pixel b(final double blue) {
            this.blue = blue;
            return this;
        }

        public double r() {
            return red;
        }

        public double g() {
            return green;
        }

        public double b() {
            return blue;
        }

        @Override
        public String toString() {
            return "(" + red + ", " + green + ", " + blue + ")";
        }
    }
}