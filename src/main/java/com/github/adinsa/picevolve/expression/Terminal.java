package com.github.adinsa.picevolve.expression;

import java.util.List;

import com.github.adinsa.picevolve.Image;
import com.github.adinsa.picevolve.visitor.Visitor;

/**
 * A terminal {@link Expression} node
 *
 * @author amar
 *
 * @param <T>
 */
public abstract class Terminal<T> extends Expression {

    private T value;

    public Terminal(final T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    public static class ScalarNode extends Terminal<Double> {

        public ScalarNode(final Double value) {
            super(value);
        }

        @Override
        public Image interpret(final int width, final int height, final List<Argument<?>> arguments) {
            return new Argument.ScalarArgument(getValue()).toImage(width, height);
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return String.valueOf(getValue());
        }
    }

    public static class VectorNode extends Terminal<List<Double>> {

        public VectorNode(final List<Double> vector) {
            super(vector);
        }

        @Override
        public Image interpret(final int width, final int height, final List<Argument<?>> arguments) {
            return new Argument.VectorArgument(getValue()).toImage(width, height);
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return String.format("#%f,%f,%f", getValue().get(0), getValue().get(1), getValue().get(2));
        }
    }

    public static class VariableNode extends Terminal<Variable> {

        public VariableNode(final Variable var) {
            super(var);
        }

        @Override
        public Image interpret(final int width, final int height, final List<Argument<?>> arguments) {
            return new Argument.VariableArgument(getValue()).toImage(width, height);
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return getValue().name();
        }
    }
}