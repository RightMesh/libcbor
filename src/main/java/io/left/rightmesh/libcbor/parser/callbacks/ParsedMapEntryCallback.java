package io.left.rightmesh.libcbor.parser.callbacks;

import io.left.rightmesh.libcbor.ParserInCallback;
import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ParsedMapEntryCallback<T, U> {
    void onMapEntryParsed(ParserInCallback parser, T key, U value) throws RxParserException;
}
