package io.left.rightmesh.libcbor.parser.callbacks;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.ParserInCallbackApi;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface FilterCallback {
    void onFilter(ParserInCallbackApi parser, ByteBuffer buffer);
}
