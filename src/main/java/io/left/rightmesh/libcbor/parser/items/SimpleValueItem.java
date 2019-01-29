package io.left.rightmesh.libcbor.parser.items;

import java.util.LinkedList;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParserApi;

import static io.left.rightmesh.libcbor.Constants.CborType.CborSimpleType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class SimpleValueItem extends DataItem implements ParseableItem {
    SimpleValueItem() {
        super(CborSimpleType);
    }

    public SimpleValueItem(int value) {
        super(CborSimpleType);
        setItem(null, value);
    }

    SimpleValueItem(LinkedList<Long> tags, int value) {
        super(CborSimpleType, value, tags);
    }

    public long value() {
        return (Long) item;
    }

    @Override
    public CborParserApi getItemParser() {
        return CBOR.parser().cbor_parse_simple_value(this::setItem);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof SimpleValueItem) {
            return this.item.equals(((SimpleValueItem) o).item);
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborSimpleType;
    }
}
