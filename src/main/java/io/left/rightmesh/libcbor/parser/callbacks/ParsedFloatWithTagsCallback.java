package io.left.rightmesh.libcbor.parser.callbacks;

import java.util.LinkedList;

import io.left.rightmesh.libcbor.ParserInCallbackApi;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ParsedFloatWithTagsCallback {
    void onFloatParsed(ParserInCallbackApi parser, LinkedList<Long> tags, double d)
            throws RxParserException;
}
