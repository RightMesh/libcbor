package io.left.rightmesh.libcbor.parser.states.basic;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * LongState deserialize a single Long value.
 *
 * @author Lucien Loiseau on 03/09/18.
 */
public abstract class LongState extends ObjectState<Long> {

    byte[] buf = new byte[8];

    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        return buffer;
    }

    BufferState buffer = new BufferState(buf) {
        @Override
        public ParserState onSuccess(ByteBuffer buffer) throws RxParserException {
            return LongState.this.onSuccess(buffer.getLong());
        }
    };

    public abstract ParserState onSuccess(Long s) throws RxParserException;
}
