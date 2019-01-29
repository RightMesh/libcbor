package io.left.rightmesh.libcbor.parser.states;

import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.TextStringType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseTextStringUnsafe extends CborParseStringUnsafe {
    public CborParseTextStringUnsafe() {
        super(TextStringType);
    }
}
