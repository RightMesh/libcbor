package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.states.basic.ByteState;
import io.left.rightmesh.libcbor.parser.states.basic.IntegerState;
import io.left.rightmesh.libcbor.parser.states.basic.LongState;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;
import io.left.rightmesh.libcbor.parser.states.basic.ShortState;

import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.IndefiniteLength;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value16Bit;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value32Bit;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value64Bit;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value8Bit;
import static io.left.rightmesh.libcbor.Constants.CborInternals.SmallValueMask;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class ExtractInteger extends ParserState {
    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        byte b = next.get();
        int adv = (b & SmallValueMask);
        if (adv < Value8Bit) {
            return onSuccess((long) adv);
        }
        if (adv == Value8Bit) {
            return getUInt8;
        }
        if (adv == Value16Bit) {
            return getUInt16;
        }
        if (adv == Value32Bit) {
            return getUInt32;
        }
        if (adv == Value64Bit) {
            return getUInt64;
        }
        if (adv == IndefiniteLength) {
            // indefinite
            return ExtractInteger.this.onSuccess(-1L);
        }
        throw new RxParserException("ExtractInteger", "Wrong additional value: " + adv);
    }

    ByteState getUInt8 = new ByteState() {
        @Override
        public ParserState onSuccess(Byte b) throws RxParserException {
            return ExtractInteger.this.onSuccess((long) (b & 0xff));
        }
    };

    ShortState getUInt16 = new ShortState() {
        @Override
        public ParserState onSuccess(Short s) throws RxParserException {
            return ExtractInteger.this.onSuccess((long) (s & 0xffff));
        }
    };

    IntegerState getUInt32 = new IntegerState() {
        @Override
        public ParserState onSuccess(Integer i) throws RxParserException {
            return ExtractInteger.this.onSuccess((i & 0xffffffffL));
        }
    };

    LongState getUInt64 = new LongState() {
        @Override
        public ParserState onSuccess(Long l) throws RxParserException {
            return ExtractInteger.this.onSuccess(l);
        }
    };

    public abstract ParserState onSuccess(long i) throws RxParserException;
}
