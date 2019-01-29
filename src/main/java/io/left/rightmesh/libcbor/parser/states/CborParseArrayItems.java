package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.parser.CborParserImpl;
import io.left.rightmesh.libcbor.parser.items.ItemFactory;
import io.left.rightmesh.libcbor.parser.items.ParseableItem;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBreak;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseArrayItems<T extends ParseableItem> extends ParserState {

    ItemFactory<T> factory;

    public CborParseArrayItems(ItemFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public ParserState onNext(ByteBuffer next) throws RxParserException {
        return checkBreak;
    }

    ParserState checkBreak = new ParserState() {
        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            byte b = CborParserImpl.peek(next);
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
        CborParser parser;
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
                    return checkBreak;
                }
                return this;
            }
        };
    };

    public abstract void onArrayItem(LinkedList<Long> tags, T item) throws RxParserException;

    public abstract ParserState onArrayIsClose() throws RxParserException;

}
