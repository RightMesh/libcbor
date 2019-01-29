package io.left.rightmesh.libcbor.parser.items;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParser;

import static io.left.rightmesh.libcbor.Constants.CborType.CborTagType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class TagItem extends DataItem implements ParseableItem {
    public TagItem() {
        super(CborTagType);
    }

    public TagItem(long l) {
        super(CborTagType, l);
    }

    public long value() {
        return (Long) item;
    }

    @Override
    public CborParser getItemParser() {
        return CBOR.parser().cbor_parse_tag(this::setItem);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof TagItem) {
            return ((TagItem) o).item.equals(this.item);
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborTagType;
    }
}
