package io.left.rightmesh.libcbor.parser.items;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;

import static io.left.rightmesh.libcbor.Constants.CborType.CborBooleanType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class BooleanItem extends DataItem implements ParseableItem {
    public BooleanItem() {
        super(CborBooleanType);
    }

    public BooleanItem(boolean b) {
        super(CborBooleanType, b);
    }

    public boolean value() {
        return (Boolean) item;
    }

    @Override
    public CborParser getItemParser() {
        return CBOR.parser().cbor_parse_boolean(this::setItem);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return item.equals(o);
        }
        if (o instanceof BooleanItem) {
            return ((BooleanItem) o).item.equals(this.item);
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborBooleanType;
    }
}
