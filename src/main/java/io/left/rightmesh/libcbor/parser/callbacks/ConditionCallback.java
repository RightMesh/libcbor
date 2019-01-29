package io.left.rightmesh.libcbor.parser.callbacks;

import io.left.rightmesh.libcbor.ParserInCallback;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ConditionCallback {
    boolean condition(ParserInCallback p);
}
