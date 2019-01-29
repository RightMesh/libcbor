package io.left.rightmesh.libcbor;

import java.nio.ByteBuffer;
import java.util.EnumSet;

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
import io.left.rightmesh.libcbor.parser.RxParserException;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface CborParser {

    enum ExpectedType {
        Integer, Float, ByteString, TextString, Array, Map, Tag, Simple
    }

    /**
     * check wether the parser has done parsing all its sequence
     *
     * @return true if parser is done, false otherwise
     */
    boolean isDone();

    /**
     * set an object so it is accessible by any other callback.
     * It overwrites any object that was already saved with the same key.
     *
     * @param key    to retrieve the object
     * @param object to be saved
     * @return this parser
     */
    CborParser set(String key, Object object);

    /**
     * Returns a previously saved item.
     *
     * @param key to retrieve the object
     * @param <T> type of the object
     * @return the saved object
     */
    <T> T get(String key);

    /**
     * remove an object from the map
     *
     * @param key to the object
     * @return this parser
     */
    CborParser remove(String key);

    /**
     * set an object in a register so it is accessible by any other callback.
     * It overwrites any object that was already saved with the same key.
     *
     * @param pos    position of the object
     * @param object to be saved
     * @return this parser
     */
    CborParser setReg(int pos, Object object);

    /**
     * Returns a previously saved item from the register.
     *
     * @param pos to retrieve the object
     * @param <T> type of the object
     * @return the saved object
     */
    <T> T getReg(int pos);

    /**
     * reset this parsing sequence to its initial state.
     */
    void reset();

    /**
     * Parse the buffer given as parameter. If the parser has finished parsing before the end
     * of this buffer, it returns true and the buffer position is left to where it was last
     * consumed and can be reuse for another parsing job. If the buffer was entirely consummed
     * but the current parser hasn't finished, it returns false and further read may be needed
     * to finish the parsing job.
     * <p>
     * Note that it is safe to call merge() or insert() from within one of the parsing callback.
     *
     * @param buffer
     * @return true if the object was successfully parsed, false otherwise.
     * @throws RxParserException if an error occured during parsing
     */
    boolean read(ByteBuffer buffer) throws RxParserException;

    /**
     * Add a parsing sequence at the end of this parser sequence.
     *
     * @param parser sequence to add at the end.
     */
    CborParser merge(CborParser parser);

    /**
     * Add a parsing sequence after the current
     * parsing state that called the callback.
     *
     * @param parser to add at the front of the sequence
     */
    CborParser insert(CborParser parser);

    CborParser do_for_each(String key, FilterCallback cb);

    CborParser undo_for_each(String key);

    CborParser undo_for_each(String key, ParsingDoneCallback cb);

    CborParser do_here(ParsingDoneCallback cb);

    CborParser do_insert_if(ConditionCallback ccb, CborParser parser);

    CborParser cbor_parse_generic(ParsedItemCallback<DataItem> cb);

    CborParser cbor_parse_generic(EnumSet<CborParser.ExpectedType> types, ParsedItemCallback<DataItem> cb);

    CborParser cbor_or(CborParser p1, CborParser p2);

    <T extends ParseableItem> CborParser cbor_parse_custom_item(ItemFactory<T> factory, ParsedItemWithTagsCallback<T> cb);

    CborParser cbor_parse_boolean(ParsedBoolean cb);

    CborParser cbor_parse_break();

    CborParser cbor_parse_break(ParsingDoneCallback cb);

    CborParser cbor_parse_undefined();

    CborParser cbor_parse_undefined(ParsingDoneCallback cb);

    CborParser cbor_parse_null();

    CborParser cbor_parse_null(ParsingDoneCallback cb);

    CborParser cbor_parse_simple_value(ParsedItemCallback cb);

    CborParser cbor_parse_int(ParsedIntWithTagsCallback cb);

    CborParser cbor_parse_float(ParsedFloatWithTagsCallback cb);

    CborParser cbor_parse_byte_string(ChunkCallback<ByteBuffer> cb);

    CborParser cbor_parse_byte_string(ContainerIsOpenCallback cb1, ChunkCallback<ByteBuffer> cb2);

    CborParser cbor_parse_byte_string(ContainerIsOpenCallback cb1,
                                      ChunkCallback<ByteBuffer> cb2,
                                      ContainerIsCloseCallback cb3);

    CborParser cbor_parse_byte_string_unsafe(ParsedItemWithTagsCallback<ByteBuffer> cb);

    CborParser cbor_parse_text_string(ChunkCallback<String> cb);

    CborParser cbor_parse_text_string(ContainerIsOpenCallback cb1, ChunkCallback<String> cb2);

    CborParser cbor_parse_text_string(ContainerIsOpenCallback cb1,
                                      ChunkCallback<String> cb2,
                                      ContainerIsCloseCallback cb3);

    CborParser cbor_parse_text_string_full(ParsedItemCallback<String> cb);

    CborParser cbor_parse_text_string_full(ContainerIsOpenCallback cb1, ParsedItemCallback<String> cb2);

    CborParser cbor_parse_text_string_unsafe(ParsedItemWithTagsCallback<String> cb);

    CborParser cbor_parse_tag(ParsedItemCallback<Long> cb);

    <T extends ParseableItem> CborParser cbor_parse_linear_array(
            ItemFactory<T> factory,
            ContainerIsCloseWithCollectionCallback<T> cb);

    <T extends ParseableItem> CborParser cbor_parse_linear_array(
            ItemFactory<T> factory,
            ContainerIsOpenCallback cb1,
            ParsedItemWithTagsCallback<T> cb2,
            ContainerIsCloseWithCollectionCallback<T> cb3);

    <T extends ParseableItem> CborParser cbor_parse_linear_array_stream(
            ItemFactory<T> factory,
            ParsedItemWithTagsCallback<T> cb);

    <T extends ParseableItem> CborParser cbor_parse_linear_array_stream(
            ItemFactory<T> factory,
            ContainerIsOpenCallback cb1,
            ParsedItemWithTagsCallback<T> cb2,
            ContainerIsCloseCallback cb3);

    <T extends ParseableItem, U extends ParseableItem> CborParser cbor_parse_linear_map(
            ItemFactory<T> keyFactory,
            ItemFactory<U> valueFactory,
            ContainerIsCloseWithMapCallback<T, U> cb);

    <T extends ParseableItem, U extends ParseableItem> CborParser cbor_parse_linear_map(
            ItemFactory<T> keyFactory,
            ItemFactory<U> valueFactory,
            ContainerIsOpenCallback cb1,
            ParsedMapEntryCallback<T, U> cb2,
            ContainerIsCloseWithMapCallback<T, U> cb3);

    CborParser cbor_open_map(ContainerIsOpenCallback cb);

    CborParser cbor_close_map();

    CborParser cbor_close_map(ContainerIsCloseCallback cb);

    CborParser cbor_open_array(ContainerIsOpenCallback cb);

    CborParser cbor_open_array(int expectedSize);

    CborParser cbor_open_container(ContainerIsOpenCallback cb,
                                   int majorType);

    CborParser cbor_open_container_expected_size(int expectedSize, int majorType);

    <T extends ParseableItem> CborParser cbor_parse_array_items(
            ItemFactory<T> factory,
            ParsedItemWithTagsCallback<T> cb);

    <T extends ParseableItem> CborParser cbor_parse_array_items(
            ItemFactory<T> factory,
            ParsedItemWithTagsCallback<T> cb1,
            ContainerIsCloseCallback cb2);

    CborParser cbor_close_array();

    CborParser cbor_close_array(ContainerIsCloseCallback cb);

    CborParser cbor_close_container(ContainerIsCloseCallback cb);
}
