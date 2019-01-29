package io.left.rightmesh.libcbor.parser.callbacks;

import java.util.LinkedList;

import io.left.rightmesh.libcbor.ParserInCallbackApi;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ParsedIntWithTagsCallback {
    void onIntParsed(ParserInCallbackApi parser, LinkedList<Long> tags, long i) throws RxParserException;
}
