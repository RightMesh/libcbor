package io.left.rightmesh.libcbor.parser.states;

import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.Break;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseBreak extends CborParseSimpleValue {
    @Override
    public ParserState onSimplevalue(int value) throws RxParserException {
        if (value != Break) {
            throw new RxParserException("CborParseBreak", "Not a Break Value");
        } else {
            return onBreak();
        }
    }

    public abstract ParserState onBreak() throws RxParserException;
}
