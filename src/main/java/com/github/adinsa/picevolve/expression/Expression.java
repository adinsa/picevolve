package com.github.adinsa.picevolve.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.adinsa.picevolve.Image;
import com.github.adinsa.picevolve.visitor.Visitor;

/**
 * Representation of a symbolic expression that serves as the genotype of a PicEvolve image
 *
 * @author amar
 *
 */
public abstract class Expression {

    private Expression parent;
    private List<Expression> children;

    public final Expression getParent() {
        return parent;
    }

    public final void setChildren(final List<Expression> children) {
        for (final Expression child : children) {
            child.parent = this;
        }
        this.children = children;
    }

    public final List<Expression> getChildren() {
        return Optional.ofNullable(children).orElse(new ArrayList<Expression>());
    }

    public abstract Image interpret(final int width, final int height, final List<Argument<?>> arguments);

    public abstract void accept(Visitor visitor);
}