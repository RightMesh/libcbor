package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.CborParser;
import io.left.rightmesh.libcbor.parser.states.basic.BufferState;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBreak;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseString extends ExtractTagItem {

    int expectedType;
    long bytesExpected;
    int max_chunk_size = 2048;
    ByteBuffer chunk = null;

    CborParseString(int expectedType) {
        super(true);
        this.expectedType = expectedType;
    }

    @Override
    public ParserState onItemFound(int majorType, byte b) throws RxParserException {
        if (majorType != expectedType) {
            throw new RxParserException("CborParseString", "Expected major type: " + expectedType + " but " + majorType + " found");
        }
        return extractStringSize;
    }

    ExtractInteger extractStringSize = new ExtractInteger() {
        @Override
        public ParserState onSuccess(long stringSize) throws RxParserException {
            bytesExpected = stringSize;
            onContainerOpen(stringSize);
            if (bytesExpected == 0) {
                return CborParseString.this.onSuccess();
            }
            if (bytesExpected > 0) {
                extractDefiniteLengthString.realloc(Math.min((int) bytesExpected, max_chunk_size));
                return extractDefiniteLengthString;
            } else {
                // a negative integer means indefinite size
                return checkBreak;
            }
        }
    };

    BufferState extractDefiniteLengthString = new BufferState() {
        @Override
        public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
            bytesExpected -= buffer.remaining();
            if (bytesExpected == 0) {
                CborParseString.this.onNextChunk(buffer);
                return CborParseString.this.onSuccess();
            } else {
                CborParseString.this.onNextChunk(buffer);
                realloc(Math.min((int) bytesExpected, max_chunk_size));
                onEnter();
                return this;
            }
        }
    };

    ParserState checkBreak = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = CborParser.peek(next);
            if ((b & 0xff) == CborBreak) {
                next.get();
                return CborParseString.this.onSuccess();
            } else {
                return extractChunkSize;
            }
        }
    };

    ExtractInteger extractChunkSize = new ExtractInteger() {
        @Override
        public ParserState onSuccess(long size) throws RxParserException {
            bytesExpected = size;
            if (size == 0) {
                CborParseString.this.onNextChunk(ByteBuffer.allocate(0));
                return checkBreak;
            }
            if (size > 0) {
                extractChunk.realloc(Math.min((int) bytesExpected, max_chunk_size));
                return extractChunk;
            }
            throw new RxParserException("CborParseString", "Byte string chunk must be definite-length");
        }
    };

    BufferState extractChunk = new BufferState() {
        @Override
        public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
            bytesExpected -= buffer.remaining();
            CborParseString.this.onNextChunk(buffer);
            if (bytesExpected == 0) {
                return checkBreak;
            } else {
                realloc(Math.min((int) bytesExpected, max_chunk_size));
                return this;
            }
        }
    };

    public abstract void onContainerOpen(long size) throws RxParserException;

    public abstract void onNextChunk(ByteBuffer buffer) throws RxParserException;

    public abstract ParserState onSuccess() throws RxParserException;
}
