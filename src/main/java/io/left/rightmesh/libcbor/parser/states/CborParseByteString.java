package io.left.rightmesh.libcbor.parser.states;

import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.ByteStringType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseByteString extends CborParseString {
    public CborParseByteString() {
        super(ByteStringType);
    }
}
