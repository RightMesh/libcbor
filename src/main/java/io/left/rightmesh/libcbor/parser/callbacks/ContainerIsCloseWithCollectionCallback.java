package io.left.rightmesh.libcbor.parser.callbacks;

import java.util.Collection;
import java.util.LinkedList;

import io.left.rightmesh.libcbor.ParserInCallback;
import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ContainerIsCloseWithCollectionCallback<T> {
    void onContainerIsClose(ParserInCallback parser, LinkedList<Long> tags, Collection<T> c) throws RxParserException;
}
