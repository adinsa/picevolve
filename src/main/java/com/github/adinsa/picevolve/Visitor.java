package com.github.adinsa.picevolve;

import com.github.adinsa.picevolve.Terminal.ScalarNode;
import com.github.adinsa.picevolve.Terminal.VariableNode;
import com.github.adinsa.picevolve.Terminal.VectorNode;

public interface Visitor {

    void visit(ScalarNode scalarNode);

    void visit(VariableNode variableNode);

    void visit(VectorNode vectorNode);

    void visit(Function function);
}