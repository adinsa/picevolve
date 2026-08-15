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
    private int height = 1;

    public final Expression getParent() {
        return parent;
    }

    /**
     * Returns the height (number of levels) of the subtree rooted at this node. Defaults to 1 (a leaf) and is populated during mutation.
     *
     * @return
     */
    public final int getHeight() {
        return height;
    }

    public final void setHeight(final int height) {
        this.height = height;
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

    /**
     * Replaces this node with the provided node in its parent's list of children.
     *
     * @param replacement
     */
    public final void replaceWith(final Expression replacement) {
        if (parent == null) {
            throw new IllegalStateException("Root node cannot be replaced");
        }
        final List<Expression> siblings = parent.getChildren();
        siblings.set(siblings.indexOf(this), replacement);
        parent.setChildren(siblings);
    }

    public abstract Image interpret(final int width, final int height, final List<Argument<?>> arguments);

    public abstract void accept(Visitor visitor);
}