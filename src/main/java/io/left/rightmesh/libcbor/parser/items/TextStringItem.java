package io.left.rightmesh.libcbor.parser.items;

import java.util.LinkedList;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParserApi;

import static io.left.rightmesh.libcbor.Constants.CborType.CborTextStringType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class TextStringItem extends DataItem implements ParseableItem {
    public TextStringItem() {
        super(CborTextStringType);
    }

    public TextStringItem(String str) {
        super(CborTextStringType, str);
    }

    public TextStringItem(LinkedList<Long> tags, String str) {
        super(CborTextStringType, str, tags);
    }

    public String value() {
        return (String) item;
    }

    @Override
    public CborParserApi getItemParser() {
        return CBOR.parser().cbor_parse_text_string_unsafe(this::setTaggedItem);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof String) {
            return item.equals(o);
        }
        if (o instanceof TextStringItem) {
            return ((TextStringItem) o).item.equals(this.item);
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborTextStringType;
    }
}
