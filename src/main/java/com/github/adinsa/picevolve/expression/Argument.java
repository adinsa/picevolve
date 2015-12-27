package com.github.adinsa.picevolve.expression;

import java.util.List;
import java.util.stream.IntStream;

import com.github.adinsa.picevolve.Image;
import com.github.adinsa.picevolve.Image.Pixel;

/**
 * An argument to an {@link Expression}
 *
 * @author amar
 *
 * @param <T>
 */
public abstract class Argument<T> {

    private final T value;

    public Argument(final T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public abstract Image toImage(int width, int height);

    public static class ScalarArgument extends Argument<Double> {

        public ScalarArgument(final Double value) {
            super(value);
        }

        @Override
        public Image toImage(final int width, final int height) {
            final Image image = new Image(width, height);
            IntStream.range(0, height).forEach(y -> IntStream.range(0, width)
                    .forEach(x -> image.set(x, y, new Pixel(this.getValue()))));
            return image;
        }
    }

    public static class VariableArgument extends Argument<Variable> {

        public VariableArgument(final Variable variable) {
            super(variable);
        }

        @Override
        public Image toImage(final int width, final int height) {
            final Image image = new Image(width, height);
            double xCur = -width / 2;
            double yCur = height / 2;
            for (int y = 0; y < height; y++) {
                xCur = -width / 2;
                for (int x = 0; x < width; x++) {
                    image.set(x, y, new Pixel(this.getValue().equals(Variable.X)
                            ? xCur++ : yCur));
                }
                yCur--;
            }
            return image.scaled(-1, 1);
        }
    }

    public static class VectorArgument extends Argument<List<Double>> {

        public VectorArgument(final List<Double> vector) {
            super(vector);
        }

        @Override
        public Image toImage(final int width, final int height) {
            final Image image = new Image(width, height);
            IntStream.range(0, height)
                    .forEach(y -> IntStream.range(0, width)
                            .forEach(x -> image.set(x, y,
                                    new Pixel(this.getValue().get(0),
                                            this.getValue().get(1),
                                            this.getValue().get(2)))));
            return image;
        }
    }

    public static class ImageArgument extends Argument<Image> {

        public ImageArgument(final Image image) {
            super(image);
        }

        @Override
        public Image toImage(final int width, final int height) {
            return this.getValue();
        }
    }
}
