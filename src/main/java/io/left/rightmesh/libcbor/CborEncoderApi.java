package io.left.rightmesh.libcbor;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

import io.left.rightmesh.libcbor.encoder.CborEncoder;
import io.left.rightmesh.libcbor.encoder.CborEncodingUnknown;
import io.reactivex.Flowable;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface CborEncoderApi {

    CborEncoderApi merge(CborEncoder o);

    Flowable<ByteBuffer> observe();

    Flowable<ByteBuffer> observe(int buffer_size);

    /**
     * cbor_encode_object will try to encode the object given as a parameter. The Object must be an
     * instance of one of the following class:
     * <lu>
     * <li>Double</li>
     * <li>Float</li>
     * <li>Long</li>
     * <li>Integer</li>
     * <li>Short</li>
     * <li>Byte</li>
     * <li>Boolean</li>
     * <li>String</li>
     * <li>Map</li>
     * <li>Collection</li>
     * <li>Object[]</li>
     * </lu>
     * <p>
     * <p>For Map, Collection and array, the encapsulated data must also be one of the listed type.
     *
     * @param o object to be encoded
     * @return this encoder
     * @throws BufferOverflowException if the buffer is full
     * @throws CborEncodingUnknown     if object is not accepted type
     */
    CborEncoderApi cbor_encode_object(Object o) throws CborEncodingUnknown;

    CborEncoderApi cbor_encode_boolean(boolean b);

    CborEncoderApi cbor_encode_null();

    CborEncoderApi cbor_encode_undefined();

    /**
     * cbor_encode_collection will try to encode the collection given as a paremeter. The item
     * embedded in this collection must be encodable with cbor_encode_object.
     *
     * @param c collection to encode
     * @return this encoder
     * @throws BufferOverflowException if the buffer is full
     * @throws CborEncodingUnknown     if object is not accepted type
     */
    CborEncoderApi cbor_encode_collection(Collection c) throws CborEncodingUnknown;

    /**
     * cbor_encode_map will try to encode the map given as a paremeter. The keys and items
     * embedded in this map must be encodable with cbor_encode_object.
     *
     * @param m Map to encode
     * @return this encoder
     * @throws BufferOverflowException if the buffer is full
     * @throws CborEncodingUnknown     if object is not accepted type
     */
    CborEncoderApi cbor_encode_map(Map m) throws CborEncodingUnknown;

    /**
     * Starts an indefinite array. This encoder makes no check if a break ever appear later
     * so a later call to cbor_stop_array must be done to ensure cbor-validity.
     *
     * @return this encoder
     */
    CborEncoderApi cbor_start_indefinite_array();

    /**
     * Starts an array of length given. if length is negative, the array is assumed to be of size
     * indefinite. This encoder makes no check if a break ever appear later so a later call to
     * cbor_stop_array must be done to ensure cbor-validity.
     *
     * @param length of the array
     * @return this encoder
     */
    CborEncoderApi cbor_start_array(long length);

    /**
     * Close an opened array. This encoder makes no check wether a container was opened earlier.
     *
     * @return this encoder
     */
    CborEncoderApi cbor_stop_array();

    /**
     * Starts a map of length given. if length is negative, the map is assumed to be of size
     * indefinite. This encoder makes no check if a break ever appear later so it must be added
     * manually to ensure cbor-validity.
     *
     * @param length of the map
     * @return this encoder
     */
    CborEncoderApi cbor_start_map(long length);

    /**
     * Close an opened map. This encoder makes no check wether a container was opened earlier.
     *
     * @return this encoder
     */
    CborEncoderApi cbor_stop_map();

    /**
     * Starts a byte string of length given. if length is negative, the string is assumed to be of
     * size indefinite. While this byte string is open, chunks must be added with
     * {@see cbor_put_byte_string_chunk}. This encoder makes no check if a break ever appear later
     * so it must be added manually to ensure cbor-validity.
     *
     * @param length of the string
     * @return this encoder
     */
    CborEncoderApi cbor_start_byte_string(long length);

    /**
     * Add a fixed length byte string.
     *
     * @param chunk to add
     * @return this encoder
     */
    CborEncoderApi cbor_put_byte_string_chunk(byte[] chunk);

    /**
     * Close an opened byte string. This encoder makes no check wether a container was opened
     * earlier.
     *
     * @return this encoder
     */
    CborEncoderApi cbor_stop_byte_string();

    /**
     * Starts a text string of length given. if length is negative, the string is assumed to be of
     * size indefinite. While this text string is open, chunks must be added with
     * {@see cbor_put_text_string_chunk}. This encoder makes no check if a break ever appear later
     * so it must be added manually to ensure cbor-validity.
     *
     * @param length of the string
     * @return this encoder
     */
    CborEncoderApi cbor_start_text_string(long length);

    /**
     * Add a fixed length text string.
     *
     * @param chunk to add
     * @return this encoder
     */
    CborEncoderApi cbor_put_text_string_chunk(String chunk);

    /**
     * Close an opened text string. This encoder makes no check wether a container was opened
     * earlier.
     *
     * @return this encoder
     */
    CborEncoderApi cbor_stop_text_string();

    /**
     * Add a fixed length byte string.
     *
     * @param array to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_byte_string(byte[] array);

    /**
     * Add a fixed length byte string.
     *
     * @param buf to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_byte_string(ByteBuffer buf);

    /**
     * Add a byte string from a Flowable and create an indefinite size cbor byte string.
     *
     * @param source      to encode
     * @return this encoder
     */
    CborEncoderApi cbor_encode_byte_string(Flowable<ByteBuffer> source);

    /**
     * Add a byte string from a Flowable. If computeSize is set to true, it will create a
     * definite size cbor byte string by doing a first pass on the source. otherwise it will
     * create an indefinite size cbor byte string.
     *
     * @param source      to encode
     * @param computeSize set whether or not to do a first pass to compute total length
     * @return this encoder
     */
    CborEncoderApi cbor_encode_byte_string(Flowable<ByteBuffer> source, boolean computeSize);

    /**
     * Add a byte string from a Flowable with a definite size. It will throw an exception if
     * the flowable does not match the size given as a parameter.
     *
     * @param size   total number of bytes in the byte string
     * @param source to encode
     * @return this encoder
     */
    CborEncoderApi cbor_encode_byte_string(long size, Flowable<ByteBuffer> source);

    /**
     * Add a fixed length text string. This encoder makes no check that the str supplied is
     * a UTF-8 text string.
     *
     * @param str to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_text_string(String str);

    /**
     * add a tag to the CBOR stream.
     *
     * @param tag to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_tag(long tag);

    /**
     * encode a double floating point number.
     *
     * @param value to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_double(double value);

    /**
     * encode a single floating point number.
     *
     * @param value to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_float(float value);

    /**
     * encode a half floating point number.
     *
     * @param value to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_half_float(float value);

    /**
     * add a simple value {@see Constants.CborSimpleValues}.
     *
     * @param value to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_simple_value(byte value);

    /**
     * encode a positive or negative byte/short/integer/long number.
     *
     * @param value to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_int(long value);

    /**
     * encode an unsigned positive byte/short/integer/long number.
     *
     * @param ui to add
     * @return this encoder
     */
    CborEncoderApi cbor_encode_uint(long ui);

    /**
     * encode a negative byte/short/integer/long number.
     *
     * @param absolute_value to add, value must be absolute
     * @return this encoder
     */
    CborEncoderApi cbor_encode_negative_uint(long absolute_value);
}
