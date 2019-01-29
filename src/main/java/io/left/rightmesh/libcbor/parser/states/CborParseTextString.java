package io.left.rightmesh.libcbor.parser.states;

import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.TextStringType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseTextString extends CborParseString {
    public CborParseTextString() {
        super(TextStringType);
    }
}
