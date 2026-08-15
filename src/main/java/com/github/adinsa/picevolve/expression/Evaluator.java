package com.github.adinsa.picevolve.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;
import com.github.adinsa.picevolve.image.Image;

/**
 * Evaluates an {@link Expression} into an {@link Image}.
 *
 * @author amar
 *
 */
public class Evaluator implements ExpressionVisitor {

    private final Stack<Image> imageStack;

    private final int width;
    private final int height;

    public Evaluator(final int width, final int height) {
        this.width = width;
        this.height = height;
        imageStack = new Stack<>();
    }

    public Image getImage() {
        if (imageStack.size() != 1) {
            throw new IllegalStateException("Evaluation not complete");
        }
        return imageStack.pop();
    }

    @Override
    public void visit(final ScalarNode scalarNode) {
        imageStack.push(new Argument.ScalarArgument(scalarNode.getValue()).toImage(width, height));
    }

    @Override
    public void visit(final VariableNode variableNode) {
        imageStack.push(new Argument.VariableArgument(variableNode.getValue()).toImage(width, height));
    }

    @Override
    public void visit(final VectorNode vectorNode) {
        imageStack.push(new Argument.VectorArgument(vectorNode.getValue()).toImage(width, height));
    }

    @Override
    public void visit(final Function function) {
        final List<Argument<?>> children = new ArrayList<>(function.getArity());
        for (int i = 0; i < function.getArity(); i++) {
            final Image child = imageStack.pop();
            children.add(0, new Argument.ImageArgument(child));
        }
        imageStack.push(function.interpret(width, height, children));
    }

}
