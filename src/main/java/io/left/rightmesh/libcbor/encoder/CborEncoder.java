package io.left.rightmesh.libcbor.encoder;

import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value16Bit;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value32Bit;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value64Bit;
import static io.left.rightmesh.libcbor.Constants.CborAdditionalInfo.Value8Bit;
import static io.left.rightmesh.libcbor.Constants.CborInternals.BreakByte;
import static io.left.rightmesh.libcbor.Constants.CborInternals.MajorTypeShift;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborArrayWithIndefiniteLength;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborByteStringWithIndefiniteLength;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborDoublePrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborHalfPrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborMapWithIndefiniteLength;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborSimpleValue1ByteFollow;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborSinglePrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborTextStringWithIndefiniteLength;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.NegativeIntegerType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.SimpleTypesType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.UnsignedIntegerType;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.Break;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.FalseValue;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.NullValue;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.TrueValue;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.UndefinedValue;
import static io.left.rightmesh.libcbor.Constants.CborType.CborArrayType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborByteStringType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborMapType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborSimpleType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborTagType;
import static io.left.rightmesh.libcbor.Constants.CborType.CborTextStringType;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

import io.left.rightmesh.libcbor.CBOR;
import io.left.rightmesh.libcbor.CborEncoderApi;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subscribers.DisposableSubscriber;

public class CborEncoder implements CborEncoderApi {

    private class Subscriber extends DisposableSubscriber<ByteBuffer> {
        private ByteBuffer upstream_current;
        private ByteBuffer downstream_next;

        Subscriber(int buffer_size) {
            downstream_next = ByteBuffer.allocate(buffer_size);
        }

        @Override
        protected void onStart() {
            request(1);
        }

        @Override
        public void onNext(ByteBuffer buf) {
            upstream_current = buf;
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
            upstream_current = null;
        }

        public ByteBuffer fill() {
            if (upstream_current == null) {
                request(1);
            }
            downstream_next.clear();

            while (upstream_current != null) {
                while (downstream_next.hasRemaining() && upstream_current.hasRemaining()) {
                    downstream_next.put(upstream_current.get());
                }
                if (!downstream_next.hasRemaining()) {
                    downstream_next.flip();
                    return downstream_next;
                }
                request(1);
            }
            if (downstream_next.position() > 0) {
                downstream_next.flip();
                return downstream_next;
            } else {
                return null;
            }
        }
    }

    private Flowable<ByteBuffer> flow;

    public CborEncoder() {
        flow = Flowable.empty();
    }

    @Override
    public CborEncoderApi merge(CborEncoder o) {
        flow = flow.concatWith(o.flow);
        return this;
    }

    @Override
    public Flowable<ByteBuffer> observe() {
        return flow;
    }

    @Override
    public Flowable<ByteBuffer> observe(int buffer_size) {
        return Flowable.generate(
                () -> {
                    Subscriber s = new Subscriber(buffer_size);
                    flow.concatWith(Flowable.just(ByteBuffer.allocate(0))).subscribe(s);
                    return s;
                },
                (s, emitter) -> {
                    ByteBuffer next = s.fill();
                    if (next == null) {
                        emitter.onComplete();
                    } else {
                        emitter.onNext(next);
                    }
                    return s;
                });
    }

    @Override
    public CborEncoderApi cbor_encode_object(Object o) throws CborEncodingUnknown {
        if (o instanceof Double) {
            cbor_encode_double(((Double) o));
        } else if (o instanceof Float) {
            cbor_encode_float(((Float) o));
        } else if (o instanceof Number) {
            cbor_encode_int(((Number) o).longValue());
        } else if (o instanceof String) {
            cbor_encode_text_string((String) o);
        } else if (o instanceof Boolean) {
            cbor_encode_boolean((Boolean) o);
        } else if (o instanceof Map) {
            cbor_encode_map((Map) o);
        } else if (o instanceof Collection) {
            cbor_encode_collection((Collection) o);
        } else if (o != null) {
            Class<?> type = o.getClass();
            if (type.isArray()) {
                int len = Array.getLength(o);
                cbor_start_array(len);
                for (int i = 0; i < len; i++) {
                    cbor_encode_object(Array.get(o, i));
                }
            } else {
                throw new CborEncodingUnknown();
            }
        } else {
            cbor_encode_null();
        }
        return this;
    }

    @Override
    public CborEncoderApi cbor_encode_boolean(boolean b) {
        return encode_number((byte) (CborSimpleType), b ? TrueValue : FalseValue);
    }

    @Override
    public CborEncoderApi cbor_encode_null() {
        return encode_number((byte) (CborSimpleType), NullValue);
    }

    @Override
    public CborEncoderApi cbor_encode_undefined() {
        return encode_number((byte) (CborSimpleType), UndefinedValue);
    }

    @Override
    public CborEncoderApi cbor_encode_collection(Collection c) throws CborEncodingUnknown {
        cbor_start_array(c.size());
        for (Object o : c) {
            cbor_encode_object(o);
        }
        return this;
    }

    @Override
    public CborEncoderApi cbor_encode_map(Map m) throws CborEncodingUnknown {
        cbor_start_map(m.size());
        for (Object o : m.keySet()) {
            cbor_encode_object(o);
            cbor_encode_object(m.get(o));
        }
        return this;
    }


    @Override
    public CborEncoderApi cbor_start_indefinite_array() {
        return cbor_start_array(-1);
    }

    @Override
    public CborEncoderApi cbor_start_array(long length) {
        if (length < 0) {
            return put((byte) CborArrayWithIndefiniteLength);
        } else {
            return encode_number((byte) CborArrayType, length);
        }
    }

    @Override
    public CborEncoderApi cbor_stop_array() {
        return put((byte) BreakByte);
    }

    @Override
    public CborEncoderApi cbor_start_map(long length) {
        if (length < 0) {
            return put((byte) CborMapWithIndefiniteLength);
        } else {
            return encode_number((byte) CborMapType, length);
        }
    }

    @Override
    public CborEncoderApi cbor_stop_map() {
        return put((byte) BreakByte);
    }

    @Override
    public CborEncoderApi cbor_start_byte_string(long length) {
        if (length < 0) {
            return put((byte) CborByteStringWithIndefiniteLength);
        } else {
            return encode_number((byte) CborByteStringType, length);
        }
    }

    @Override
    public CborEncoderApi cbor_put_byte_string_chunk(byte[] chunk) {
        return cbor_encode_byte_string(chunk);
    }

    @Override
    public CborEncoderApi cbor_stop_byte_string() {
        return put((byte) BreakByte);
    }

    @Override
    public CborEncoderApi cbor_start_text_string(long length) {
        if (length < 0) {
            return put((byte) CborTextStringWithIndefiniteLength);
        } else {
            return encode_number((byte) CborTextStringType, length);
        }
    }

    @Override
    public CborEncoderApi cbor_put_text_string_chunk(String chunk) {
        return cbor_encode_text_string(chunk);
    }

    @Override
    public CborEncoderApi cbor_stop_text_string() {
        return put((byte) BreakByte);
    }

    @Override
    public CborEncoderApi cbor_encode_byte_string(byte[] array) {
        return encode_string((byte) CborByteStringType, array);
    }

    @Override
    public CborEncoderApi cbor_encode_byte_string(ByteBuffer buf) {
        return encode_string((byte) CborByteStringType, buf);
    }

    @Override
    public CborEncoderApi cbor_encode_byte_string(Flowable<ByteBuffer> source, boolean computeSize) {
        if (computeSize) {
            long size = source.map(ByteBuffer::remaining).reduce(
                    (t, s) -> t + s).toSingle().blockingGet();
            cbor_start_byte_string(size);
            add(source);
            return this;
        } else {
            return cbor_encode_byte_string(source);
        }
    }

    @Override
    public CborEncoderApi cbor_encode_byte_string(Flowable<ByteBuffer> source) {
        return cbor_encode_byte_string(-1, source);
    }

    @Override
    public CborEncoderApi cbor_encode_byte_string(long size, Flowable<ByteBuffer> source) {
        if (size < 0) {
            cbor_start_byte_string(-1);
            add(source.concatMap(buffer -> CBOR.encoder().cbor_encode_byte_string(buffer).observe(), 1));
            cbor_stop_byte_string();
        } else {
            cbor_start_byte_string(size);
            add(source);
        }
        return this;
    }

    @Override
    public CborEncoderApi cbor_encode_text_string(String str) {
        return encode_string((byte) CborTextStringType, str.getBytes());
    }

    @Override
    public CborEncoderApi cbor_encode_tag(long tag) {
        return encode_number((byte) CborTagType, tag);
    }

    @Override
    public CborEncoderApi cbor_encode_double(double value) {
        add(Flowable.create(s -> {
            ByteBuffer out = ByteBuffer.allocate(9);
            out.put((byte) CborDoublePrecisionFloat);
            out.putDouble(value);
            out.flip();
            s.onNext(out);
            s.onComplete();
        }, BackpressureStrategy.BUFFER));
        return this;
    }

    @Override
    public CborEncoderApi cbor_encode_float(float value) {
        add(Flowable.create(s -> {
            ByteBuffer out = ByteBuffer.allocate(5);
            out.put((byte) CborSinglePrecisionFloat);
            out.putFloat(value);
            out.flip();
            s.onNext(out);
            s.onComplete();
        }, BackpressureStrategy.BUFFER));
        return this;
    }

    @Override
    public CborEncoderApi cbor_encode_half_float(float value) {
        add(Flowable.create(s -> {
            ByteBuffer out = ByteBuffer.allocate(3);
            out.put((byte) CborHalfPrecisionFloat);
            out.putShort(halfPrecisionToRawIntBits(value));
            out.flip();
            s.onNext(out);
            s.onComplete();
        }, BackpressureStrategy.BUFFER));
        return this;
    }

    @Override
    public CborEncoderApi cbor_encode_simple_value(byte value) {
        if ((value & 0xff) <= Break) {
            return encode_number((byte) (SimpleTypesType << MajorTypeShift), value);
        } else {
            return put((byte) CborSimpleValue1ByteFollow, value);
        }
    }

    @Override
    public CborEncoderApi cbor_encode_int(long value) {
        long ui = value >> 63;
        byte majorType = (byte) (ui & 0x20);
        ui ^= value;
        return encode_number(majorType, ui);
    }

    @Override
    public CborEncoderApi cbor_encode_uint(long ui) {
        return encode_number((byte) (UnsignedIntegerType << MajorTypeShift), ui);
    }

    @Override
    public CborEncoderApi cbor_encode_negative_uint(long absolute_value) {
        return encode_number((byte) (NegativeIntegerType << MajorTypeShift), absolute_value - 1);

    }

    private CborEncoderApi encode_string(byte shifted_mt, byte[] array) {
        int len = (array == null) ? 0 : array.length;
        if (array == null) {
            return encode_number(shifted_mt, len);
        } else {
            encode_number(shifted_mt, len);
            add(Flowable.create(s -> {
                s.onNext(ByteBuffer.wrap(array));
                s.onComplete();
            }, BackpressureStrategy.BUFFER));
            return this;
        }
    }

    private CborEncoderApi encode_string(byte shifted_mt, ByteBuffer buf) {
        int len = (buf == null) ? 0 : buf.remaining();
        if (buf == null) {
            return encode_number(shifted_mt, len);
        } else {
            encode_number(shifted_mt, len);
            add(Flowable.create(s -> {
                ByteBuffer dup = buf.duplicate();
                s.onNext(dup);
                s.onComplete();
            }, BackpressureStrategy.BUFFER));
            return this;
        }
    }

    private CborEncoderApi put(byte b) {
        add(Flowable.create(s -> {
            ByteBuffer out = ByteBuffer.allocate(1);
            out.put(b);
            out.flip();
            s.onNext(out);
            s.onComplete();
        }, BackpressureStrategy.BUFFER));
        return this;
    }

    private CborEncoderApi put(byte b1, byte b2) {
        add(Flowable.create(s -> {
            ByteBuffer out = ByteBuffer.allocate(2);
            out.put(b1);
            out.put(b2);
            out.flip();
            s.onNext(out);
            s.onComplete();
        }, BackpressureStrategy.BUFFER));
        return this;
    }

    private CborEncoderApi encode_number(final byte shifted_mt, final long ui) {
        add(Flowable.create(s -> {
            ByteBuffer out;
            if (ui < Value8Bit) {
                out = ByteBuffer.allocate(1);
                out.put((byte) (shifted_mt | ui & 0xff));
            } else if (ui < 0x100L) {
                out = ByteBuffer.allocate(2);
                out.put((byte) (shifted_mt | Value8Bit));
                out.put((byte) ui);
            } else if (ui < 0x10000L) {
                out = ByteBuffer.allocate(3);
                out.put((byte) (shifted_mt | Value16Bit));
                out.putShort((short) ui);
            } else if (ui < 0x100000000L) {
                out = ByteBuffer.allocate(5);
                out.put((byte) (shifted_mt | Value32Bit));
                out.putInt((int) ui);
            } else {
                out = ByteBuffer.allocate(9);
                out.put((byte) (shifted_mt | Value64Bit));
                out.putLong(ui);
            }
            out.flip();
            s.onNext(out);
            s.onComplete();
        }, BackpressureStrategy.BUFFER));
        return this;
    }

    private short halfPrecisionToRawIntBits(float value) {
        int fbits = Float.floatToIntBits(value);
        int sign = (fbits >>> 16) & 0x8000;
        int val = (fbits & 0x7fffffff) + 0x1000;

        // might be or become NaN/Inf
        if (val >= 0x47800000) {
            if ((fbits & 0x7fffffff) >= 0x47800000) { // is or must become NaN/Inf
                if (val < 0x7f800000) {
                    // was value but too large, make it +/-Inf
                    return (short) (sign | 0x7c00);
                }
                return (short) ((sign | 0x7c00 | (fbits & 0x007fffff) >>> 13)); // keep NaN (and Inf) bits
            }
            return (short) (sign | 0x7bff); // unrounded not quite Inf
        }
        if (val >= 0x38800000) {
            // remains normalized value
            return (short) (sign | val - 0x38000000 >>> 13); // exp - 127 + 15
        }
        if (val < 0x33000000) {
            // too small for subnormal
            return (short) (sign); // becomes +/-0
        }

        val = (fbits & 0x7fffffff) >>> 23;
        // add subnormal bit, round depending on cut off and div by 2^(1-(exp-127+15)) and >> 13 | exp=0
        return (short) (sign | ((fbits & 0x7fffff | 0x800000) + (0x800000 >>> val - 102) >>> 126 - val));
    }


    private void add(Flowable<ByteBuffer> source) {
        flow = flow.concatWith(source);
    }

}