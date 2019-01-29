package io.left.rightmesh.libcbor.parser.callbacks;

import java.util.LinkedList;

import io.left.rightmesh.libcbor.ParserInCallback;
import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ParsedItemWithTagsCallback<T> {
    void onItemParsed(ParserInCallback parser, LinkedList<Long> tags, T obj) throws RxParserException;
}
