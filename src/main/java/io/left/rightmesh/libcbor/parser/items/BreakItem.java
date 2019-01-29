package io.left.rightmesh.libcbor.parser.items;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParserApi;

import static io.left.rightmesh.libcbor.Constants.CborType.CborBreakType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class BreakItem extends DataItem implements ParseableItem {
    public BreakItem() {
        super(CborBreakType);
    }

    @Override
    public CborParserApi getItemParser() {
        return CBOR.parser().cbor_parse_break();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof BreakItem) {
            return true;
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborBreakType;
    }
}
