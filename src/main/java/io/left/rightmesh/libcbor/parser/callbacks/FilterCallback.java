package io.left.rightmesh.libcbor.parser.callbacks;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.ParserInCallback;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface FilterCallback {
    void onFilter(ParserInCallback parser, ByteBuffer buffer);
}
