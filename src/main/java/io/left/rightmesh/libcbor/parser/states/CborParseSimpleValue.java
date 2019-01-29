package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborInternals.SmallValueMask;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborSimpleValue1ByteFollow;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.SimpleTypesType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseSimpleValue extends ExtractTagItem {

    public CborParseSimpleValue() {
        super(false);
    }

    @Override
    public ParserState onItemFound(int majorType, byte b) throws RxParserException {
        if (majorType != SimpleTypesType) {
            throw new RxParserException("CborParseSimpleValue", "Unexpected major type: " + majorType);
        }
        if ((b & 0xff) == CborSimpleValue1ByteFollow) {
            return extractNextByte;
        }
        return onSimplevalue(b & SmallValueMask);
    }

    ParserState extractNextByte = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = next.get();
            return onSimplevalue(b & 0xff);
        }
    };

    public abstract ParserState onSimplevalue(int value) throws RxParserException;
}
