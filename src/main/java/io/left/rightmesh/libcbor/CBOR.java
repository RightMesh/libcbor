package io.left.rightmesh.libcbor;

import io.left.rightmesh.libcbor.encoder.CborEncoderImpl;
import io.left.rightmesh.libcbor.parser.CborParserImpl;

/**
 * @author Lucien Loiseau on 09/09/18.
 */
public class CBOR {

    public static CborEncoder encoder() {
        return new CborEncoderImpl();
    }

    public static CborParser parser() {
        return new CborParserImpl();
    }

}
