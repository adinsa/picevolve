package com.github.adinsa.picevolve.visitor;

import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VariableNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;

public interface Visitor {

    void visit(ScalarNode scalarNode);

    void visit(VariableNode variableNode);

    void visit(VectorNode vectorNode);

    void visit(Function function);
}