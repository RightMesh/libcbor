package io.left.rightmesh.libcbor.parser.items;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborParserApi;

import static io.left.rightmesh.libcbor.Constants.CborType.CborByteStringType;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public class ByteStringItem extends DataItem implements ParseableItem {
    public ByteStringItem() {
        super(CborByteStringType);
    }

    public ByteStringItem(ByteBuffer b) {
        super(CborByteStringType, b);
    }

    public ByteStringItem(LinkedList<Long> tags, ByteBuffer b) {
        super(CborByteStringType, b, tags);
    }

    public ByteBuffer value() {
        return (ByteBuffer) item;
    }

    @Override
    public CborParserApi getItemParser() {
        return CBOR.parser().cbor_parse_byte_string_unsafe(this::setTaggedItem);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof ByteStringItem) {
            return ((ByteStringItem) o).item.equals(this.item);
        }
        return false;
    }

    public static boolean ofType(DataItem item) {
        return item.cborType == CborByteStringType;
    }
}
