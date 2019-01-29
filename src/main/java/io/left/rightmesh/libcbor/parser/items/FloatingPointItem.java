package io.left.rightmesh.libcbor.parser.items;

import java.util.LinkedList;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParserApi;

import static io.left.rightmesh.libcbor.Constants.CborType.CborDoubleType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class FloatingPointItem extends DataItem implements ParseableItem {
    public FloatingPointItem() {
        super(CborDoubleType);
    }

    public FloatingPointItem(double d) {
        super(CborDoubleType, d);
    }

    public FloatingPointItem(LinkedList<Long> tags, double d) {
        super(CborDoubleType, d, tags);
    }

    public double value() {
        return (Double) item;
    }

    @Override
    public CborParserApi getItemParser() {
        return CBOR.parser().cbor_parse_float(this::setTaggedItem);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof FloatingPointItem) {
            return ((FloatingPointItem) o).item.equals(this.item);
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborDoubleType;
    }
}
