package io.left.rightmesh.libcbor.parser.callbacks;

import io.left.rightmesh.libcbor.ParserInCallbackApi;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ParsedMapEntryCallback<T, U> {
    void onMapEntryParsed(ParserInCallbackApi parser, T key, U value) throws RxParserException;
}
