package io.left.rightmesh.libcbor.parser.callbacks;

import io.left.rightmesh.libcbor.ParserInCallback;
import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ParsedBoolean {
    void onBooleanParsed(ParserInCallback parser, boolean b) throws RxParserException;
}
