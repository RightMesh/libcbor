package io.left.rightmesh.libcbor.parser.states.basic;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * @author Lucien Loiseau on 10/09/18.
 */
public abstract class ByteState extends ObjectState<Byte> {
    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        return onSuccess(next.get());
    }
}
