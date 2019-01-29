package io.left.rightmesh.libcbor.parser.items;

import io.left.rightmesh.libcbor.CborParserApi;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ParseableItem {
    CborParserApi getItemParser();
}
