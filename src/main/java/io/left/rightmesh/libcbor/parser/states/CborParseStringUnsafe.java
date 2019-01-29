package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseStringUnsafe extends CborParseString {
    ByteBuffer output;
    private static final int increasingFactor = 2;

    CborParseStringUnsafe(int expectedType) {
        super(expectedType);
    }

    @Override
    public void onEnter() {
        output = ByteBuffer.allocate(1024);
    }

    private void reallocate() {
        ByteBuffer newbuf = ByteBuffer.allocate(output.capacity() * increasingFactor);
        output.limit(output.position());
        output.rewind();
        newbuf.put(output);
        output.clear();
        output = newbuf;
    }

    @Override
    public void onNextChunk(ByteBuffer buffer) {
        while (output.remaining() < buffer.remaining()) {
            reallocate();
        }
        output.put(buffer);
    }

    @Override
    public ParserState onSuccess() throws RxParserException {
        output.flip();
        return onSuccessUnsafe(output);
    }

    public abstract ParserState onSuccessUnsafe(ByteBuffer buffer) throws RxParserException;
}
