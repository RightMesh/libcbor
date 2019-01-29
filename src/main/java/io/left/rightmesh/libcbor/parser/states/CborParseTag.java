package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.CborParser;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborInternals.MajorTypeMask;
import static io.left.rightmesh.libcbor.Constants.CborInternals.MajorTypeShift;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.TagType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseTag extends ParserState {
    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        byte b = CborParser.peek(next);
        int mt = (((b & MajorTypeMask) & 0xff) >>> MajorTypeShift);
        if (mt == TagType) {
            return extractInteger;
        }
        throw new RxParserException("CborParseTag", "Unexpected major type: " + mt + " expected " + TagType);
    }

    ExtractInteger extractInteger = new ExtractInteger() {
        @Override
        public ParserState onSuccess(long l) throws RxParserException {
            if (l < 0) {
                throw new RxParserException("CborParseTag", "negative value can not be a tag");
            }
            return CborParseTag.this.onSuccess(l);
        }
    };

    public abstract ParserState onSuccess(long tag) throws RxParserException;
}
