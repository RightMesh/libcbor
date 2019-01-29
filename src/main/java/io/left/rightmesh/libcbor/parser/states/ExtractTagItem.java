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
public abstract class ExtractTagItem extends ParserState {

    boolean must_peek;

    ExtractTagItem(boolean peek) {
        this.must_peek = peek;
    }

    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        byte b = must_peek ? CborParser.peek(next) : next.get();
        int mt = (((b & MajorTypeMask) & 0xff) >>> MajorTypeShift);
        if (mt == TagType) {
            return extractTag;
        } else {
            return onItemFound(mt, b);
        }
    }

    ExtractInteger extractTag = new ExtractInteger() {
        @Override
        public ParserState onSuccess(long tag) {
            onTagFound(tag);
            return ExtractTagItem.this;
        }
    };

    public abstract void onTagFound(long tag);

    public abstract ParserState onItemFound(int majorType, byte b) throws RxParserException;

}
