package io.left.rightmesh.libcbor.parser.items;

import java.util.LinkedList;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParserApi;

import static io.left.rightmesh.libcbor.Constants.CborType.CborIntegerType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class IntegerItem extends DataItem implements ParseableItem {
    public IntegerItem() {
        super(CborIntegerType);
    }

    public IntegerItem(long l) {
        super(CborIntegerType, l);
    }

    public IntegerItem(LinkedList<Long> tags, long l) {
        super(CborIntegerType, l, tags);
    }

    public long value() {
        return (Long) item;
    }

    @Override
    public CborParserApi getItemParser() {
        return CBOR.parser().cbor_parse_int(this::setTaggedItem);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof IntegerItem) {
            return ((IntegerItem) o).item.equals(this.item);
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborIntegerType;
    }
}
