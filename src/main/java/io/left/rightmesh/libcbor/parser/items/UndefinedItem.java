package io.left.rightmesh.libcbor.parser.items;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;

import static io.left.rightmesh.libcbor.Constants.CborType.CborUndefinedType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class UndefinedItem extends DataItem implements ParseableItem {
    public UndefinedItem() {
        super(CborUndefinedType);
    }

    @Override
    public CborParser getItemParser() {
        return CBOR.parser().cbor_parse_undefined();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof UndefinedItem) {
            return true;
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborUndefinedType;
    }
}
