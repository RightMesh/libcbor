package io.left.rightmesh.libcbor.parser.callbacks;

import io.left.rightmesh.libcbor.ParserInCallbackApi;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ParsedBoolean {
    void onBooleanParsed(ParserInCallbackApi parser, boolean b) throws RxParserException;
}
