package io.left.rightmesh.libcbor.parser.callbacks;

import java.util.LinkedList;
import java.util.Map;

import io.left.rightmesh.libcbor.ParserInCallback;
import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ContainerIsCloseWithMapCallback<T, U> {
    void onContainerIsClose(ParserInCallback parser, LinkedList<Long> tags, Map<T, U> c) throws RxParserException;
}
