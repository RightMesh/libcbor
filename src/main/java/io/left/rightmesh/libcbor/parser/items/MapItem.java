package io.left.rightmesh.libcbor.parser.items;

import java.util.LinkedList;
import java.util.Map;

import io.left.rightmesh.libcbor.CborParserApi;

import static io.left.rightmesh.libcbor.Constants.CborType.CborMapType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class MapItem extends DataItem implements ParseableItem {
    public MapItem() {
        super(CborMapType);
    }

    public MapItem(Map m) {
        super(CborMapType, m);
    }

    public MapItem(LinkedList<Long> tags, Map m) {
        super(CborMapType, m, tags);
    }

    public Map value() {
        return (Map) item;
    }

    @Override
    public CborParserApi getItemParser() {
        // todo
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof MapItem) {
            return ((MapItem) o).item.equals(this.item);
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborMapType;
    }
}
