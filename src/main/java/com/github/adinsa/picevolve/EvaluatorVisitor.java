package com.github.adinsa.picevolve;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.adinsa.picevolve.Terminal.ScalarNode;
import com.github.adinsa.picevolve.Terminal.VariableNode;
import com.github.adinsa.picevolve.Terminal.VectorNode;

/**
 * {@link Visitor} implementation that evaluates an {@link Expression} into an
 * {@link Image}
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
        this.imageStack = new Stack<>();
    }

    public Image getImage() {
        if (this.imageStack.size() != 1) {
            throw new IllegalStateException("Evaluation not complete");
        }
        return this.imageStack.pop();
    }

    @Override
    public void visit(final ScalarNode scalarNode) {
        this.imageStack.push(new Argument.ScalarArgument(scalarNode.getValue())
                .toImage(this.width, this.height));
    }

    @Override
    public void visit(final VariableNode variableNode) {
        this.imageStack
                .push(new Argument.VariableArgument(variableNode.getValue())
                        .toImage(this.width, this.height));
    }

    @Override
    public void visit(final VectorNode vectorNode) {
        this.imageStack.push(new Argument.VectorArgument(vectorNode.getValue())
                .toImage(this.width, this.height));
    }

    @Override
    public void visit(final Function function) {
        final List<Argument<?>> children = new ArrayList<>(function.getArity());
        for (int i = 0; i < function.getArity(); i++) {
            final Image child = this.imageStack.pop();
            children.add(new Argument.ImageArgument(child));
        }
        this.imageStack
                .push(function.interpret(this.width, this.height, children));
    }

}
