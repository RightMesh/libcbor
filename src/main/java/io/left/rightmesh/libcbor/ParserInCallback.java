package io.left.rightmesh.libcbor;

import io.left.rightmesh.libcbor.parser.CborParserImpl;
import io.left.rightmesh.libcbor.parser.callbacks.FilterCallback;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ParserInCallback {
    /**
     * Add a filter for the actual parser (runner) that will run for every parsed buffer
     * until undo_for_each is called.
     *
     * @param key the key for this filter
     * @param cb  te filter
     * @return this ParserInCallbackImpl object
     */
    ParserInCallback do_for_each_now(String key, FilterCallback cb);

    /**
     * Remove a filter from this actual parser (runner).
     *
     * @param key
     * @return this parser
     */
    ParserInCallback undo_for_each_now(String key);

    /**
     * Add a parsing sequence after the current
     * parsing state that called the callback.
     *
     * @param parser to add at the front of the sequence
     * @return this parser
     */
    ParserInCallback insert_now(CborParserImpl parser);

    /**
     * set an object in a map so it is accessible by any other callback.
     * It overwrites any object that was already saved with the same key.
     *
     * @param key    to retrieve the object
     * @param object to be saved
     * @return this parser
     */
    ParserInCallback set(String key, Object object);

    /**
     * Returns a previously saved item from the map.
     *
     * @param key to retrieve the object
     * @param <T> type of the object
     * @return the saved object
     */
    <T> T get(String key);

    /**
     * remove a previously saved item.
     *
     * @param key
     * @return this parser
     */
    ParserInCallback remove(String key);

    /**
     * set an object in a register so it is accessible by any other callback.
     * It overwrites any object that was already saved with the same key.
     *
     * @param pos    position of the object
     * @param object to be saved
     * @return ParserInCallbackImpl
     */
    ParserInCallback setReg(int pos, Object object);

    /**
     * Returns a previously saved item from the register.
     *
     * @param pos to retrieve the object
     * @param <T> type of the object
     * @return the saved object
     */
    <T> T getReg(int pos);
}
