package io.left.rightmesh.libcbor.parser.callbacks;

import java.util.LinkedList;
import java.util.Map;

import io.left.rightmesh.libcbor.ParserInCallbackApi;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ContainerIsCloseWithMapCallback<T, U> {
    void onContainerIsClose(ParserInCallbackApi parser, LinkedList<Long> tags, Map<T, U> c) throws RxParserException;
}
