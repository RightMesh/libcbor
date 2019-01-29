package io.left.rightmesh.libcbor.parser.items;

import java.util.LinkedList;

import io.left.rightmesh.libcbor.ParserInCallbackApi;

/**
 * A decoded Cbor data item.
 */
public class DataItem {
    public int cborType;
    public Object item;
    public LinkedList<Long> tags;

    public DataItem(int cborType) {
        this.cborType = cborType;
        tags = new LinkedList<>();
    }

    public DataItem(int cborType, Object item) {
        this.cborType = cborType;
        setItem(null, item);
    }

    public DataItem(int cborType, Object item, LinkedList<Long> tags) {
        this.cborType = cborType;
        setTaggedItem(null, tags, item);
    }

    void addTags(LinkedList<Long> tags) {
        this.tags = tags;
    }

    void setItem(ParserInCallbackApi parser, Object item) {
        this.item = item;
    }

    void setTaggedItem(ParserInCallbackApi parser, LinkedList<Long> tags, Object item) {
        addTags(tags);
        setItem(null, item);
    }
}
