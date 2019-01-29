package io.left.rightmesh.libcbor.parser.states;

import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.NegativeIntegerType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.UnsignedIntegerType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseInteger extends ExtractTagItem {

    int mt;

    public CborParseInteger() {
        super(true);
    }

    @Override
    public ParserState onItemFound(int majorType, byte b) throws RxParserException {
        if ((majorType == UnsignedIntegerType) || (majorType == NegativeIntegerType)) {
            this.mt = majorType;
            return extractInteger;
        }
        throw new RxParserException("Unexpected major type: " + mt);
    }

    ExtractInteger extractInteger = new ExtractInteger() {
        @Override
        public ParserState onSuccess(long l) throws RxParserException {
            if (l < 0) {
                throw new RxParserException("CborParseInteger", "The extracted integer should be absolute");
            }
            return CborParseInteger.this.onSuccess(l ^ -mt);
        }
    };

    public abstract ParserState onSuccess(long d) throws RxParserException;
}
