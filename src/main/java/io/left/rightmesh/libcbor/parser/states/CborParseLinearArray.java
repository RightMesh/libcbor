package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import io.left.rightmesh.libcbor.CborParserApi;
import io.left.rightmesh.libcbor.parser.CborParser;
import io.left.rightmesh.libcbor.parser.items.ItemFactory;
import io.left.rightmesh.libcbor.parser.items.ParseableItem;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBreak;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.ArrayType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseLinearArray<T extends ParseableItem> extends ExtractContainerSize {

    long size;
    ItemFactory<T> factory;

    public CborParseLinearArray(ItemFactory<T> factory) {
        super(ArrayType);
        this.factory = factory;
    }

    @Override
    public ParserState onContainerOpen(long size) throws RxParserException {
        this.size = size;
        onArrayIsOpen(size);
        if (size == 0) {
            return onArrayIsClose();
        }
        if (size > 0) {
            return extractOneItem;
        } else {
            // a negative integer means indefinite size
            return checkBreak;
        }
    }

    ParserState checkBreak = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = CborParser.peek(next);
            if ((b & 0xff) == CborBreak) {
                next.get();
                return onArrayIsClose();
            } else {
                return extractOneItem;
            }
        }
    };


    ParserState extractOneItem = new ExtractTagItem(true) {
        T item;
        CborParserApi parser;
        LinkedList<Long> tags;

        @Override
        public void onEnter() throws RxParserException {
            tags = new LinkedList<>();
        }

        @Override
        public void onTagFound(long tag) {
            tags.add(tag);
        }

        @Override
        public ParserState onItemFound(int majorType, byte b) {
            return actualExtract;
        }

        ParserState actualExtract = new ParserState() {
            @Override
            public void onEnter() {
                item = factory.createItem();
                parser = item.getItemParser();
            }

            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                if (parser.read(next)) {
                    onArrayItem(tags, item);
                    size--;
                    if (size < 0) {
                        return checkBreak;
                    }
                    if (size == 0) {
                        return onArrayIsClose();
                    }
                    if (size > 0) {
                        this.onEnter(); // create a new item
                        return this;
                    }
                }
                return this;
            }
        };
    };

    public abstract void onArrayIsOpen(long l) throws RxParserException;

    public abstract void onArrayItem(LinkedList<Long> tags, T item) throws RxParserException;

    public abstract ParserState onArrayIsClose() throws RxParserException;

}
