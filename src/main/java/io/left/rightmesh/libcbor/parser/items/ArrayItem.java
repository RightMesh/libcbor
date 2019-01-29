package io.left.rightmesh.libcbor.parser.items;

import java.util.Collection;
import java.util.LinkedList;

import io.left.rightmesh.libcbor.CborParser;

import static io.left.rightmesh.libcbor.Constants.CborType.CborArrayType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class ArrayItem extends DataItem implements ParseableItem {
    public ArrayItem() {
        super(CborArrayType);
    }

    public ArrayItem(Collection c) {
        super(CborArrayType, c);
    }

    public ArrayItem(LinkedList<Long> tags, Collection c) {
        super(CborArrayType, c, tags);
    }

    public Collection value() {
        return (Collection) item;
    }

    @Override
    public CborParser getItemParser() {
        // todo
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof ArrayItem) {
            return ((ArrayItem) o).item.equals(this.item);
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborArrayType;
    }
}
