package io.left.rightmesh.libcbor.parser.states;

import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.UndefinedValue;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseUndefined extends CborParseSimpleValue {
    @Override
    public ParserState onSimplevalue(int value) throws RxParserException {
        if (value != UndefinedValue) {
            throw new RxParserException("CborParseUndefined", "Not an Undefined Value");
        } else {
            return onUndefined();
        }
    }

    public abstract ParserState onUndefined() throws RxParserException;
}
