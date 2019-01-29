package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;

import io.left.rightmesh.libcbor.CborParserApi;
import io.left.rightmesh.libcbor.parser.items.ItemFactory;
import io.left.rightmesh.libcbor.parser.items.ParseableItem;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseCustomItem<T extends ParseableItem> extends ExtractTagItem {
    ItemFactory<T> factory;

    public CborParseCustomItem(ItemFactory<T> factory) {
        super(true);
        this.factory = factory;
    }

    @Override
    public ParserState onItemFound(int majorType, byte b) {
        return extractCustomItem;
    }

    ParserState extractCustomItem = new ParserState() {
        T item;
        CborParserApi itemParser;

        @Override
        public void onEnter() throws RxParserException {
            item = factory.createItem();
            itemParser = item.getItemParser();
        }

        @Override
        public ParserState onNext(ByteBuffer next) throws RxParserException {
            if (itemParser.read(next)) {
                return onSuccess(item);
            }
            return this;
        }
    };

    public abstract ParserState onSuccess(T item) throws RxParserException;
}
