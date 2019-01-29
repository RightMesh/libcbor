package io.left.rightmesh.libcbor.parser.states;

import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBooleanFalse;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBooleanTrue;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseBoolean extends ExtractTagItem {

    public CborParseBoolean() {
        super(false);
    }

    @Override
    public ParserState onItemFound(int majorType, byte b) throws RxParserException {
        if ((b & 0xff) == CborBooleanFalse) {
            return onSuccess(false);
        }
        if ((b & 0xff) == CborBooleanTrue) {
            return onSuccess(true);
        }
        throw new RxParserException("CborParseBooleans", "Non boolean type: " + b);
    }

    public abstract ParserState onSuccess(boolean tag) throws RxParserException;
}
