package io.left.rightmesh.libcbor;

import io.left.rightmesh.libcbor.encoder.CborEncoder;
import io.left.rightmesh.libcbor.parser.CborParser;

/**
 * @author Lucien Loiseau on 09/09/18.
 */
public class CBOR {

    public static CborEncoderApi encoder() {
        return new CborEncoder();
    }

    public static CborParserApi parser() {
        return CborParser.create();
    }

}
