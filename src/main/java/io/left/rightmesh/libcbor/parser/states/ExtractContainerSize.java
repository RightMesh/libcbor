package io.left.rightmesh.libcbor.parser.states;

import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class ExtractContainerSize extends ExtractTagItem {

    int expectedType;

    public ExtractContainerSize(int type) {
        super(true);
        this.expectedType = type;
    }

    @Override
    public ParserState onItemFound(int majorType, byte b) throws RxParserException {
        if (majorType != expectedType) {
            throw new RxParserException("ExtractContainerSize", "Expected major type: " + expectedType + " but " + majorType + " found");
        }
        return extractContainerSize;
    }

    ExtractInteger extractContainerSize = new ExtractInteger() {
        @Override
        public ParserState onSuccess(long size) throws RxParserException {
            return onContainerOpen(size);
        }
    };

    public abstract ParserState onContainerOpen(long size) throws RxParserException;
}
