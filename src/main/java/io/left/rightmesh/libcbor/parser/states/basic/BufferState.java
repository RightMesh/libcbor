package io.left.rightmesh.libcbor.parser.states.basic;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * BufferState is a RxState that buffers data and returns it.
 *
 * @author Lucien Loiseau on 03/09/18.
 */

public abstract class BufferState extends ObjectState<ByteBuffer> {
    private ByteBuffer buffer;

    public BufferState realloc(int size) {
        if(size == buffer.capacity()) {
            buffer.clear();
        } else {
            buffer = ByteBuffer.allocate(size);
        }
        return this;
    }

    public BufferState(int size) {
        buffer = ByteBuffer.allocate(size);
    }

    public BufferState() {
        buffer = ByteBuffer.allocate(0);
    }

    public BufferState(byte[] array) {
        buffer = ByteBuffer.wrap(array);
    }

    @Override
    public void onEnter() {
        buffer.clear();
    }

    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        if (buffer.remaining() >= next.remaining()) {
            buffer.put(next);
        } else {
            while (buffer.hasRemaining()) {
                buffer.put(next.get());
            }
        }

        if (!buffer.hasRemaining()) {
            buffer.flip();
            return onSuccess(buffer);
        }
        return this;
    }

    /**
     * onSuccess is called whenever the buffer is filled.
     *
     * @param buffer of data
     * @throws RxParserException if an exception happened during deserialization.
     */
    public abstract ParserState onSuccess(ByteBuffer buffer) throws RxParserException;
}
