package io.left.rightmesh.libcbor.parser;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import io.left.rightmesh.libcbor.CborParser;
import io.left.rightmesh.libcbor.ParserInCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ChunkCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ConditionCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ContainerIsCloseCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ContainerIsCloseWithCollectionCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ContainerIsCloseWithMapCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ContainerIsOpenCallback;
import io.left.rightmesh.libcbor.parser.callbacks.FilterCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ParsedBoolean;
import io.left.rightmesh.libcbor.parser.callbacks.ParsedFloatWithTagsCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ParsedIntWithTagsCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ParsedItemCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ParsedItemWithTagsCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ParsedMapEntryCallback;
import io.left.rightmesh.libcbor.parser.callbacks.ParsingDoneCallback;
import io.left.rightmesh.libcbor.parser.items.DataItem;

import io.left.rightmesh.libcbor.parser.items.ItemFactory;
import io.left.rightmesh.libcbor.parser.items.ParseableItem;
import io.left.rightmesh.libcbor.parser.states.CborOr;
import io.left.rightmesh.libcbor.parser.states.CborParseArrayItems;
import io.left.rightmesh.libcbor.parser.states.CborParseBoolean;
import io.left.rightmesh.libcbor.parser.states.CborParseBreak;
import io.left.rightmesh.libcbor.parser.states.CborParseByteString;
import io.left.rightmesh.libcbor.parser.states.CborParseByteStringUnsafe;
import io.left.rightmesh.libcbor.parser.states.CborParseCustomItem;
import io.left.rightmesh.libcbor.parser.states.CborParseFloat;
import io.left.rightmesh.libcbor.parser.states.CborParseGenericItem;
import io.left.rightmesh.libcbor.parser.states.CborParseInteger;
import io.left.rightmesh.libcbor.parser.states.CborParseLinearArray;
import io.left.rightmesh.libcbor.parser.states.CborParseLinearMap;
import io.left.rightmesh.libcbor.parser.states.CborParseNull;
import io.left.rightmesh.libcbor.parser.states.CborParseSimpleValue;
import io.left.rightmesh.libcbor.parser.states.CborParseTag;
import io.left.rightmesh.libcbor.parser.states.CborParseTextString;
import io.left.rightmesh.libcbor.parser.states.CborParseTextStringUnsafe;
import io.left.rightmesh.libcbor.parser.states.CborParseUndefined;
import io.left.rightmesh.libcbor.parser.states.ExtractContainerSize;
import io.left.rightmesh.libcbor.parser.states.basic.DoState;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;

import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.ArrayType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.MapType;

public class CborParserImpl implements CborParser {

    public class ParserInCallbackImpl implements ParserInCallback {
        @Override
        public ParserInCallback do_for_each_now(String key, FilterCallback cb) {
            if ((key != null) && (cb != null)) {
                add_filter(key, cb);
            }
            return this;
        }

        @Override
        public ParserInCallback undo_for_each_now(String key) {
            if (key != null) {
                remove_filter(key);
            }
            return this;
        }

        @Override
        public ParserInCallback insert_now(CborParserImpl parser) {
            if (parser != null) {
                insert(parser);
            }
            return this;
        }

        @Override
        public ParserInCallback set(String key, Object object) {
            items.put(key, object);
            return this;
        }

        @Override
        public <T> T get(String key) {
            return (T)items.get(key);
        }

        @Override
        public ParserInCallback remove(String key) {
            items.remove(key);
            return this;
        }

        @Override
        public ParserInCallback setReg(int pos, Object object) {
            if(pos > register.length) {
                return this;
            }

            register[pos] = object;
            return this;
        }

        @Override
        public <T> T getReg(int pos) {
            if(pos >= register.length) {
                return null;
            }
            return (T)register[pos];
        }
    }

    private ParserInCallback thisInCallback;
    private LinkedList<ParserState> resetQueue = new LinkedList<>();
    private LinkedList<ParserState> doneQueue = new LinkedList<>();
    private LinkedList<ParserState> parserQueue = new LinkedList<>();
    private Map<String, FilterCallback> filters = new HashMap<>();
    private Map<String, Object> items = new HashMap<>();
    private ParserState state = null;
    private Object[] register = new Object[10];

    private boolean dequeue() {
        if (parserQueue.isEmpty()) {
            return false;
        } else {
            state = parserQueue.poll();
            doneQueue.add(state);
            return true;
        }
    }

    public CborParserImpl() {
        thisInCallback = new ParserInCallbackImpl();
    }


    @Override
    public boolean isDone() {
        return (state == null) && (parserQueue.size() == 0);
    }

    /**
     * add a callback for every buffer successfully processed by this parser
     *
     * @param key to identify the callback
     * @param cb the callback
     */
    private void add_filter(String key, FilterCallback cb) {
        if (key != null && cb != null) {
            filters.put(key, cb);
        }
    }

    /**
     * remove a filter callback
     *
     * @param key to the callback
     */
    private void remove_filter(String key) {
        if (key != null) {
            filters.remove(key);
        }
    }

    @Override
    public CborParser set(String key, Object object) {
        items.put(key, object);
        return this;
    }

    @Override
    public <T> T get(String key) {
        return (T)items.get(key);
    }

    @Override
    public CborParser remove(String key) {
        items.remove(key);
        return this;
    }

    @Override
    public CborParser setReg(int pos, Object object) {
        if(pos >= register.length) {
            return this;
        }

        register[pos] = object;
        return this;
    }

    @Override
    public <T> T getReg(int pos) {
        if(pos > register.length) {
            return null;
        }
        return (T)register[pos];
    }

    /* parser method */

    @Override
    public void reset() {
        doneQueue.clear();
        parserQueue.clear();
        parserQueue.addAll(resetQueue);
        resetQueue.clear();
        filters.clear();
        items.clear();
        for(int i = 0; i < register.length; i++) {
            register[i] = null;
        }
    }


    @Override
    public boolean read(ByteBuffer buffer) throws RxParserException {
        if (state == null) {
            resetQueue.addAll(parserQueue);
        }

        ParserState next = null;
        ByteBuffer dup = buffer.duplicate();

        while (true) {
            if (state == null) {
                if (!dequeue()) {
                    return true;
                } else {
                    state.onEnter();
                    state.parser_ref = thisInCallback;
                }
            }

            // break condition
            if (!buffer.hasRemaining() && !(state instanceof DoState)) {
                break;
            }

            // parse current state
            dup.position(buffer.position());
            dup.mark();
            int remaining = buffer.remaining();
            next = state.onNext(buffer);
            dup.limit(buffer.position());

            // run filters if any
            for (String key : filters.keySet()) {
                dup.reset();
                filters.get(key).onFilter(thisInCallback, dup.slice());
            }

            // watchdog
            if (next == state) {
                if (buffer.remaining() == remaining) {
                    throw new RxParserException("CborParserImpl", "Failsafe! parser is not consuming buffer: " + next.getClass().getName());
                }
            } else {
                state.onExit();
                state = next;
                if (state != null) {
                    state.onEnter();
                    state.parser_ref = thisInCallback;
                }
            }
        }

        if (state == null && parserQueue.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public CborParser merge(CborParser parser) {
        if (parser != null) {
            parserQueue.addAll(((CborParserImpl)parser).parserQueue);
        }
        return this;
    }

    @Override
    public CborParser insert(CborParser parser) {
        if (parser != null) {
            Deque<ParserState> d = new ArrayDeque<>();
            for (ParserState s : ((CborParserImpl)parser).parserQueue) {
                d.push(s);
            }
            for (ParserState s : d) {
                parserQueue.addFirst(s);
            }
        }
        return this;
    }

    /* parser API */

    @Override
    public CborParser do_for_each(String key, FilterCallback cb) {
        do_here((p) -> p.do_for_each_now(key, cb));
        return this;
    }

    @Override
    public CborParser undo_for_each(String key) {
        do_here((p) -> p.undo_for_each_now(key));
        return this;
    }

    @Override
    public CborParser undo_for_each(String key, ParsingDoneCallback cb) {
        do_here((p) -> {
            p.undo_for_each_now(key);
            cb.onParsingDone(p);
        });
        return this;
    }

    @Override
    public CborParser do_here(ParsingDoneCallback cb) {
        parserQueue.add(new DoState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                cb.onParsingDone((ParserInCallback) parser_ref);
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser do_insert_if(ConditionCallback ccb, CborParser parser) {
        do_here((p) -> {
            if (ccb.condition(p)) {
                p.insert_now((CborParserImpl)parser);
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_generic(ParsedItemCallback<DataItem> cb) {
        parserQueue.add(new CborParseGenericItem() {
            @Override
            public ParserState onSuccess(DataItem item) throws RxParserException {
                cb.onItemParsed((ParserInCallback) parser_ref, item);
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_generic(EnumSet<ExpectedType> types, ParsedItemCallback<DataItem> cb) {
        parserQueue.add(new CborParseGenericItem(types) {
            @Override
            public ParserState onSuccess(DataItem item) throws RxParserException {
                cb.onItemParsed((ParserInCallback) parser_ref, item);
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_or(CborParser p1, CborParser p2) {
        CborParserImpl[] contender = {(CborParserImpl)p1, (CborParserImpl)p2};
        parserQueue.add(new CborOr(contender) {
            @Override
            public ParserState onSuccess(CborParserImpl p) {
                if (!p.isDone()) {
                    insert(p);
                    return p.state;
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public <T extends ParseableItem> CborParserImpl cbor_parse_custom_item(ItemFactory<T> factory, ParsedItemWithTagsCallback<T> cb) {
        parserQueue.add(new CborParseCustomItem<T>(factory) {
            LinkedList<Long> tags;

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onSuccess(T item) throws RxParserException {
                cb.onItemParsed((ParserInCallback) parser_ref, tags, item);
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_boolean(ParsedBoolean cb) {
        parserQueue.add(new CborParseBoolean() {
            @Override
            public void onTagFound(long tag) {
                // a boolean should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onSuccess(boolean b) throws RxParserException {
                cb.onBooleanParsed((ParserInCallback) parser_ref, b);
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_break() {
        return cbor_parse_break(null);
    }

    @Override
    public CborParser cbor_parse_break(ParsingDoneCallback cb) {
        parserQueue.add(new CborParseBreak() {
            @Override
            public void onTagFound(long tag) {
                // a break should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onBreak() throws RxParserException {
                if (cb != null) {
                    cb.onParsingDone((ParserInCallback) parser_ref);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_undefined() {
        return cbor_parse_undefined(null);
    }

    @Override
    public CborParser cbor_parse_undefined(ParsingDoneCallback cb) {
        parserQueue.add(new CborParseUndefined() {
            @Override
            public void onTagFound(long tag) {
                // an undefined value should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onUndefined() throws RxParserException {
                if (cb != null) {
                    cb.onParsingDone((ParserInCallback) parser_ref);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_null() {
        return cbor_parse_null(null);
    }

    @Override
    public CborParser cbor_parse_null(ParsingDoneCallback cb) {
        parserQueue.add(new CborParseNull() {
            @Override
            public void onTagFound(long tag) {
                // a null value should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onNull() throws RxParserException {
                if (cb != null) {
                    cb.onParsingDone((ParserInCallback) parser_ref);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_simple_value(ParsedItemCallback cb) {
        parserQueue.add(new CborParseSimpleValue() {
            @Override
            public void onTagFound(long tag) {
                // a null value should not have a tag attached
                // should we raise an error ?
            }

            @Override
            public ParserState onSimplevalue(int value) throws RxParserException {
                if (cb != null) {
                    cb.onItemParsed((ParserInCallback) parser_ref, value);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParserImpl cbor_parse_int(ParsedIntWithTagsCallback cb) {
        parserQueue.add(new CborParseInteger() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onSuccess(long l) throws RxParserException {
                cb.onIntParsed((ParserInCallback) parser_ref, tags, l);
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParserImpl cbor_parse_float(ParsedFloatWithTagsCallback cb) {
        parserQueue.add(new CborParseFloat() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onSuccess(Double obj) throws RxParserException {
                cb.onFloatParsed((ParserInCallback) parser_ref, tags, obj);
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_byte_string(ChunkCallback<ByteBuffer> cb) {
        return cbor_parse_byte_string(null, cb, null);
    }

    @Override
    public CborParser cbor_parse_byte_string(ContainerIsOpenCallback cb1, ChunkCallback<ByteBuffer> cb2) {
        return cbor_parse_byte_string(cb1, cb2, null);
    }

    @Override
    public CborParser cbor_parse_byte_string(ContainerIsOpenCallback cb1,
                                             ChunkCallback<ByteBuffer> cb2,
                                             ContainerIsCloseCallback cb3) {
        parserQueue.add(new CborParseByteString() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onContainerOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen((ParserInCallback) parser_ref, tags, size);
                }
            }

            @Override
            public void onNextChunk(ByteBuffer next) throws RxParserException {
                if (cb2 != null) {
                    cb2.onChunk((ParserInCallback) parser_ref, next);
                }
            }

            @Override
            public ParserState onSuccess() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose((ParserInCallback) parser_ref);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_byte_string_unsafe(ParsedItemWithTagsCallback<ByteBuffer> cb) {
        parserQueue.add(new CborParseByteStringUnsafe() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onContainerOpen(long size) {
                // ignore because unsafe
            }

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
                cb.onItemParsed((ParserInCallback) parser_ref, tags, obj);
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_text_string(ChunkCallback<String> cb) {
        return cbor_parse_text_string(null, cb, null);
    }

    @Override
    public CborParser cbor_parse_text_string(ContainerIsOpenCallback cb1, ChunkCallback<String> cb2) {
        return cbor_parse_text_string(cb1, null, null);
    }

    @Override
    public CborParser cbor_parse_text_string(ContainerIsOpenCallback cb1,
                                             ChunkCallback<String> cb2,
                                             ContainerIsCloseCallback cb3) {
        parserQueue.add(new CborParseTextString() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onContainerOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen((ParserInCallback) parser_ref, tags, size);
                }
            }

            @Override
            public void onNextChunk(ByteBuffer next) throws RxParserException {
                if (cb2 != null) {
                    cb2.onChunk((ParserInCallback) parser_ref, StandardCharsets.UTF_8.decode(next).toString());
                }
            }

            @Override
            public ParserState onSuccess() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose((ParserInCallback) parser_ref);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_text_string_full(ParsedItemCallback<String> cb) {
        return cbor_parse_text_string_full(null, cb);
    }

    @Override
    public CborParser cbor_parse_text_string_full(ContainerIsOpenCallback cb1, ParsedItemCallback<String> cb2) {
        parserQueue.add(new CborParseTextStringUnsafe() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onContainerOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen((ParserInCallback) parser_ref, tags, size);
                }
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer next) throws RxParserException {
                cb2.onItemParsed((ParserInCallback) parser_ref, StandardCharsets.UTF_8.decode(next).toString());
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParserImpl cbor_parse_text_string_unsafe(ParsedItemWithTagsCallback<String> cb) {
        parserQueue.add(new CborParseTextStringUnsafe() {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onContainerOpen(long size) {
                // ignore because unsafe
            }

            @Override
            public ParserState onSuccessUnsafe(ByteBuffer next) throws RxParserException {
                cb.onItemParsed((ParserInCallback) parser_ref, tags, StandardCharsets.UTF_8.decode(next).toString());
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_parse_tag(ParsedItemCallback<Long> cb) {
        parserQueue.add(new CborParseTag() {
            @Override
            public ParserState onSuccess(long tag) throws RxParserException {
                cb.onItemParsed((ParserInCallback) parser_ref, tag);
                return null;
            }
        });
        return this;
    }

    @Override
    public <T extends ParseableItem> CborParser cbor_parse_linear_array(
            ItemFactory<T> factory,
            ContainerIsCloseWithCollectionCallback<T> cb) {
        return cbor_parse_linear_array(factory, null, null, cb);
    }

    @Override
    public <T extends ParseableItem> CborParser cbor_parse_linear_array(
            ItemFactory<T> factory,
            ContainerIsOpenCallback cb1,
            ParsedItemWithTagsCallback<T> cb2,
            ContainerIsCloseWithCollectionCallback<T> cb3) {
        parserQueue.add(new CborParseLinearArray<T>(factory) {

            Collection<T> c;
            LinkedList<Long> tags;

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onArrayIsOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen((ParserInCallback) parser_ref, tags, size);
                }
                c = new LinkedList<>();
            }

            @Override
            public void onArrayItem(LinkedList<Long> tags, T item) throws RxParserException {
                if (cb2 != null) {
                    cb2.onItemParsed((ParserInCallback) parser_ref, tags, item);
                }
                c.add(item);
            }

            @Override
            public ParserState onArrayIsClose() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose((ParserInCallback) parser_ref, tags, c);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public <T extends ParseableItem> CborParser cbor_parse_linear_array_stream(
            ItemFactory<T> factory,
            ParsedItemWithTagsCallback<T> cb) {
        return cbor_parse_linear_array_stream(factory, null, cb, null);
    }

    @Override
    public <T extends ParseableItem> CborParser cbor_parse_linear_array_stream(
            ItemFactory<T> factory,
            ContainerIsOpenCallback cb1,
            ParsedItemWithTagsCallback<T> cb2,
            ContainerIsCloseCallback cb3) {
        parserQueue.add(new CborParseLinearArray<T>(factory) {
            LinkedList<Long> tags;

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onArrayIsOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen((ParserInCallback) parser_ref, tags, size);
                }
            }

            @Override
            public void onArrayItem(LinkedList<Long> tags, T item) throws RxParserException {
                if (cb2 != null) {
                    cb2.onItemParsed((ParserInCallback) parser_ref, tags, item);
                }
            }

            @Override
            public ParserState onArrayIsClose() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose((ParserInCallback) parser_ref);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public <T extends ParseableItem, U extends ParseableItem> CborParser cbor_parse_linear_map(
            ItemFactory<T> keyFactory,
            ItemFactory<U> valueFactory,
            ContainerIsCloseWithMapCallback<T, U> cb) {
        return cbor_parse_linear_map(keyFactory, valueFactory, null, null, cb);
    }

    @Override
    public <T extends ParseableItem, U extends ParseableItem> CborParser cbor_parse_linear_map(
            ItemFactory<T> keyFactory,
            ItemFactory<U> valueFactory,
            ContainerIsOpenCallback cb1,
            ParsedMapEntryCallback<T, U> cb2,
            ContainerIsCloseWithMapCallback<T, U> cb3) {
        parserQueue.add(new CborParseLinearMap<T, U>(keyFactory, valueFactory) {

            Map<T, U> m;
            LinkedList<Long> tags;

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public void onMapIsOpen(long size) throws RxParserException {
                if (cb1 != null) {
                    cb1.onContainerIsOpen((ParserInCallback) parser_ref, tags, size);
                }
                m = new HashMap<>();
            }

            @Override
            public void onMapEntry(T keyItem, U valueItem) throws RxParserException {
                if (cb2 != null) {
                    cb2.onMapEntryParsed((ParserInCallback) parser_ref, keyItem, valueItem);
                }
                m.put(keyItem, valueItem);
            }

            @Override
            public ParserState onMapIsClose() throws RxParserException {
                if (cb3 != null) {
                    cb3.onContainerIsClose((ParserInCallback) parser_ref, tags, m);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_open_map(ContainerIsOpenCallback cb) {
        return cbor_open_container(cb, MapType);
    }

    @Override
    public CborParser cbor_close_map() {
        return cbor_close_container(null);
    }

    @Override
    public CborParser cbor_close_map(ContainerIsCloseCallback cb) {
        return cbor_close_container(cb);
    }

    @Override
    public CborParser cbor_open_array(ContainerIsOpenCallback cb) {
        return cbor_open_container(cb, ArrayType);
    }

    @Override
    public CborParser cbor_open_array(int expectedSize) {
        return cbor_open_container_expected_size(expectedSize, ArrayType);
    }

    @Override
    public CborParser cbor_open_container(ContainerIsOpenCallback cb,
                                          int majorType) {
        parserQueue.add(new ExtractContainerSize(majorType) {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onContainerOpen(long size) throws RxParserException {
                if (cb != null) {
                    cb.onContainerIsOpen((ParserInCallback) parser_ref, tags, size);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_open_container_expected_size(int expectedSize, int majorType) {
        parserQueue.add(new ExtractContainerSize(majorType) {
            LinkedList<Long> tags = new LinkedList<>();

            @Override
            public void onTagFound(long tag) {
                tags.add(tag);
            }

            @Override
            public ParserState onContainerOpen(long size) throws RxParserException {
                if (size != expectedSize) {
                    throw new RxParserException("cbor_open_container_expected_size", "wrong array length, expected: " + expectedSize + " got: " + size);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public <T extends ParseableItem> CborParser cbor_parse_array_items(
            ItemFactory<T> factory,
            ParsedItemWithTagsCallback<T> cb) {
        return cbor_parse_array_items(factory, cb, null);
    }

    @Override
    public <T extends ParseableItem> CborParser cbor_parse_array_items(
            ItemFactory<T> factory,
            ParsedItemWithTagsCallback<T> cb1,
            ContainerIsCloseCallback cb2) {
        parserQueue.add(new CborParseArrayItems<T>(factory) {
            @Override
            public void onArrayItem(LinkedList<Long> tags, T item) throws RxParserException {
                if (cb1 != null) {
                    cb1.onItemParsed((ParserInCallback) parser_ref, tags, item);
                }
            }

            @Override
            public ParserState onArrayIsClose() throws RxParserException {
                if (cb2 != null) {
                    cb2.onContainerIsClose((ParserInCallback) parser_ref);
                }
                return null;
            }
        });
        return this;
    }

    @Override
    public CborParser cbor_close_array() {
        return cbor_close_container(null);
    }

    @Override
    public CborParser cbor_close_array(ContainerIsCloseCallback cb) {
        return cbor_close_container(cb);
    }

    @Override
    public CborParser cbor_close_container(ContainerIsCloseCallback cb) {
        parserQueue.add(new CborParseBreak() {
            @Override
            public void onTagFound(long tag) {
                // do nothing but it is probably an error
            }

            @Override
            public ParserState onBreak() throws RxParserException {
                if (cb != null) {
                    cb.onContainerIsClose((ParserInCallback) parser_ref);
                }
                return null;
            }
        });
        return this;
    }

    public static byte peek(ByteBuffer buffer) {
        return buffer.get(buffer.position());
    }
}