package com.github.adinsa.picevolve.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.adinsa.picevolve.Image;
import com.github.adinsa.picevolve.expression.Argument;
import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;

/**
 * {@link Visitor} implementation that evaluates an {@link Expression} into an {@link Image}
 *
 * @author amar
 *
 */
public class EvaluatorVisitor implements Visitor {

    private final Stack<Image> imageStack;

    private final int width;
    private final int height;

    public EvaluatorVisitor(final int width, final int height) {
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
            children.add(new Argument.ImageArgument(child));
        }
        imageStack.push(function.interpret(width, height, children));
    }

}
