package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.states.basic.IntegerState;
import io.left.rightmesh.libcbor.parser.states.basic.LongState;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;
import io.left.rightmesh.libcbor.parser.states.basic.ShortState;

import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborDoublePrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborHalfPrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborSinglePrecisionFloat;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseFloat extends ExtractTagItem {

    public CborParseFloat() {
        super(true);
    }

    @Override
    public ParserState onItemFound(int majorType, byte b) {
        return extractFloatType;
    }

    ParserState extractFloatType = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = next.get();
            if ((b & 0xff) == CborHalfPrecisionFloat) {
                return getUInt16;
            }
            if ((b & 0xff) == CborSinglePrecisionFloat) {
                return getUInt32;
            }
            if ((b & 0xff) == CborDoublePrecisionFloat) {
                return getUInt64;
            }
            throw new RxParserException("CborParseFloat", "Expected Float-Family major type but got: " + (b & 0xff));
        }
    };

    ShortState getUInt16 = new ShortState() {
        @Override
        public ParserState onSuccess(Short s) throws RxParserException {
            int exp = (s >> 10) & 0x1f;
            int mant = s & 0x3ff;

            double val;
            if (exp == 0) {
                val = mant * Math.pow(2, -24);
            } else if (exp != 31) {
                val = (mant + 1024) * Math.pow(2, exp - 25);
            } else if (mant != 0) {
                val = Double.NaN;
            } else {
                val = Double.POSITIVE_INFINITY;
            }

            return CborParseFloat.this.onSuccess(((s & 0x8000) == 0) ? val : -val);
        }
    };

    IntegerState getUInt32 = new IntegerState() {
        @Override
        public ParserState onSuccess(Integer i) throws RxParserException {
            return CborParseFloat.this.onSuccess((double) Float.intBitsToFloat(i));
        }
    };

    LongState getUInt64 = new LongState() {
        @Override
        public ParserState onSuccess(Long l) throws RxParserException {
            return CborParseFloat.this.onSuccess(Double.longBitsToDouble(l));
        }
    };

    public abstract ParserState onSuccess(Double d) throws RxParserException;
}
