package com.github.adinsa.picevolve.random;

import com.github.adinsa.picevolve.PicEvolve;
import com.github.adinsa.picevolve.expression.Expression;
import com.github.adinsa.picevolve.expression.Function;
import com.github.adinsa.picevolve.expression.Terminal;
import com.github.adinsa.picevolve.expression.Terminal.ScalarNode;
import com.github.adinsa.picevolve.expression.Terminal.VectorNode;
import com.github.adinsa.picevolve.mutation.Mutation;
import com.github.adinsa.picevolve.mutation.MutationFactory;

/**
 * Wraps all non-deterministic behavior involved in performing genetic operations on {@link Expression} trees.
 *
 * @author amar
 *
 */
public interface Random {

    /**
     * Returns a randomly-generated {@link ScalarNode} with a value between 0 and 1.
     *
     * @return
     */
    public ScalarNode nextScalar();

    /**
     * Returns a randomly-generated {@link VectorNode} with element values between 0 and 1.
     *
     * @return
     */
    public VectorNode nextVector();

    /**
     * Returns a random {@link Terminal} node (scalar, vector, or variable).
     *
     * @return
     */
    public Expression nextTerminal();

    /**
     * Returns a random {@link Function} from {@link PicEvolve}'s function set.
     *
     * @return
     */
    public Function nextFunction();

    /**
     * Returns a randomly-generated {@link Expression}.
     *
     * @return
     */
    public Expression nextExpression();

    /**
     * Returns a random node from the provided {@link Expression} tree.
     *
     * @param root
     * @return
     */
    public Expression nextNode(final Expression root);

    /**
     * Returns a random node from the provided {@link Expression} node's immediate children.
     *
     * @param node
     * @return
     */
    public Expression nextChild(final Expression node);

    /**
     * Returns a random {@link Mutation} that may be applied to the given type of {@link Expression} node (based on the
     * types and relative frequencies defined in {@link MutationFactory}).
     *
     * @param nodeType
     * @return
     */
    public Mutation nextMutation(final Class<? extends Expression> nodeType);

    /**
     * Should a mutation be performed while visiting the given {@link Expression} node?
     *
     * @param node
     * @param globalMutationFrequency
     * @return
     */
    public boolean shouldMutate(final Expression node, final double globalMutationFrequency);
}