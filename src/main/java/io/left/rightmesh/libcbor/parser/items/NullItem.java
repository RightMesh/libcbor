package io.left.rightmesh.libcbor.parser.items;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParserApi;

import static io.left.rightmesh.libcbor.Constants.CborType.CborNullType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class NullItem extends DataItem implements ParseableItem {
    public NullItem() {
        super(CborNullType);
    }

    @Override
    public CborParserApi getItemParser() {
        return CBOR.parser().cbor_parse_null();
    }

    public Object value() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof NullItem) {
            return true;
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborNullType;
    }
}
