package com.github.adinsa.picevolve.expression;

import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;

/**
 * Callback interface for traversing the node types of an {@link Expression} tree.
 *
 * @author amar
 *
 */
public interface ExpressionVisitor {

    void visit(ScalarNode scalarNode);

    void visit(VariableNode variableNode);

    void visit(VectorNode vectorNode);

    void visit(Function function);
}
