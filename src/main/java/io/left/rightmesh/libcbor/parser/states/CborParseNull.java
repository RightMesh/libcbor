package io.left.rightmesh.libcbor.parser.states;

import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.NullValue;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseNull extends CborParseSimpleValue {
    @Override
    public ParserState onSimplevalue(int value) throws RxParserException {
        if (value != NullValue) {
            throw new RxParserException("CborParseNull", "Not a Null Value");
        } else {
            return onNull();
        }
    }

    public abstract ParserState onNull() throws RxParserException;
}
