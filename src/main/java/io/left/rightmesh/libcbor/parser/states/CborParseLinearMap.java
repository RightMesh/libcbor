package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.parser.CborParserImpl;
import io.left.rightmesh.libcbor.parser.items.ItemFactory;
import io.left.rightmesh.libcbor.parser.items.ParseableItem;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBreak;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.MapType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseLinearMap<T extends ParseableItem, U extends ParseableItem> extends ExtractContainerSize {

    long size;
    ItemFactory<T> keyFactory;
    ItemFactory<U> valueFactory;
    T currentKey;

    public CborParseLinearMap(ItemFactory<T> keyFactory,
                              ItemFactory<U> valueFactory) {
        super(MapType);
        this.keyFactory = keyFactory;
        this.valueFactory = valueFactory;
    }

    @Override
    public ParserState onContainerOpen(long size) throws RxParserException {
        this.size = size;
        onMapIsOpen(size);
        if (size == 0) {
            return onMapIsClose();
        }
        if (size > 0) {
            return extractKey;
        } else {
            // a negative integer means indefinite size
            return checkBreak;
        }
    }

    ParserState checkBreak = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = CborParserImpl.peek(next);
            if ((b & 0xff) == CborBreak) {
                next.get();
                return onMapIsClose();
            } else {
                return extractKey;
            }
        }
    };

    ParserState extractKey = new ParserState() {
        CborParser parser;

        @Override
        public void onEnter() {
            currentKey = keyFactory.createItem();
            parser = currentKey.getItemParser();
        }

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            if (parser.read(next)) {
                return extractValue;
            }
            return this;
        }
    };

    ParserState extractValue = new ParserState() {
        U value;
        CborParser parser;

        @Override
        public void onEnter() {
            value = valueFactory.createItem();
            parser = value.getItemParser();
        }

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            if (parser.read(next)) {
                onMapEntry(currentKey, value);
                size--;
                if (size < 0) {
                    return checkBreak;
                }
                if (size == 0) {
                    return onMapIsClose();
                }
                if (size > 0) {
                    return extractKey;
                }
            }
            return this;
        }
    };

    public abstract void onMapIsOpen(long l) throws RxParserException;

    public abstract void onMapEntry(T key, U value) throws RxParserException;

    public abstract ParserState onMapIsClose() throws RxParserException;

}
