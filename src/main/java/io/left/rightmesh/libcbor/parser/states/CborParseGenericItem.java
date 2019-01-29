package io.left.rightmesh.libcbor.parser.states;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import io.left.rightmesh.libcbor.parser.CborParser;
import io.left.rightmesh.libcbor.parser.items.ArrayItem;
import io.left.rightmesh.libcbor.parser.items.ByteStringItem;
import io.left.rightmesh.libcbor.parser.items.DataItem;
import io.left.rightmesh.libcbor.parser.items.FloatingPointItem;
import io.left.rightmesh.libcbor.parser.items.IntegerItem;
import io.left.rightmesh.libcbor.parser.items.MapItem;
import io.left.rightmesh.libcbor.parser.items.SimpleValueItem;
import io.left.rightmesh.libcbor.parser.items.TagItem;
import io.left.rightmesh.libcbor.parser.items.TextStringItem;
import io.left.rightmesh.libcbor.parser.states.basic.ParserState;
import io.left.rightmesh.libcbor.parser.states.basic.RxParserException;

import static io.left.rightmesh.libcbor.Constants.CborInternals.SmallValueMask;
import static io.left.rightmesh.libcbor.Constants.CborJumpTable.CborBreak;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.ArrayType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.ByteStringType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.MapType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.NegativeIntegerType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.SimpleTypesType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.TagType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.TextStringType;
import static io.left.rightmesh.libcbor.Constants.CborMajorTypes.UnsignedIntegerType;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.DoublePrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.HalfPrecisionFloat;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.SimpleTypeInNextByte;
import static io.left.rightmesh.libcbor.Constants.CborSimpleValues.SinglePrecisionFloat;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public abstract class CborParseGenericItem extends ExtractTagItem {

    LinkedList<Long> tags;
    boolean filter_int = true;
    boolean filter_float = true;
    boolean filter_byte_string = true;
    boolean filter_text_string = true;
    boolean filter_array = true;
    boolean filter_tag = true;
    boolean filter_map = true;
    boolean filter_simple = true;

    public CborParseGenericItem() {
        super(true);
        tags = new LinkedList<>();
    }

    public CborParseGenericItem(EnumSet<CborParser.ExpectedType> types) {
        this();
        filter_int = types.contains(CborParser.ExpectedType.Integer);
        filter_float = types.contains(CborParser.ExpectedType.Float);
        filter_byte_string = types.contains(CborParser.ExpectedType.ByteString);
        filter_text_string = types.contains(CborParser.ExpectedType.TextString);
        filter_array = types.contains(CborParser.ExpectedType.Array);
        filter_map = types.contains(CborParser.ExpectedType.Map);
        filter_tag = types.contains(CborParser.ExpectedType.Tag);
        filter_simple = types.contains(CborParser.ExpectedType.Simple);
    }

    @Override
    public void onTagFound(long tag) {
        tags.add(tag);
    }

    @Override
    public ParserState onItemFound(int mt, byte b) throws RxParserException {
        if (((mt == UnsignedIntegerType) || (mt == NegativeIntegerType)) && (filter_int)) {
            return parse_integer;
        }
        if ((mt == ByteStringType) && (filter_byte_string)) {
            return parse_byte_string;
        }
        if ((mt == TextStringType) && (filter_text_string)) {
            return parse_text_string;
        }

        if ((mt == ArrayType) && (filter_array)) {
            return parse_array_size;
        }
        if ((mt == MapType) && (filter_map)) {
            return parse_map_size;
        }

        if ((mt == TagType) && (filter_tag)) {
            return parse_tag;
        }
        if (mt == SimpleTypesType) {
            switch (b & SmallValueMask) {
                case SimpleTypeInNextByte:
                    if (filter_simple) {
                        return parse_simple_value;
                    }
                case HalfPrecisionFloat:
                    if (filter_float) {
                        return parse_float;
                    }
                case SinglePrecisionFloat:
                    if (filter_float) {
                        return parse_float;
                    }
                case DoublePrecisionFloat:
                    if (filter_float) {
                        return parse_float;
                    }
                default:
                    if (filter_simple) {
                        return parse_simple_value;
                    }
            }
        }
        throw new RxParserException("CborParseGenericItem", "Unknown major type: " + mt);
    }

    CborParseInteger parse_integer = new CborParseInteger() {
        @Override
        public void onTagFound(long tag) {
            // do nothing
        }

        @Override
        public ParserState onSuccess(long obj) throws RxParserException {
            return CborParseGenericItem.this.onSuccess(new IntegerItem(tags, obj));
        }
    };

    CborParseFloat parse_float = new CborParseFloat() {
        @Override
        public void onTagFound(long tag) {
            // do nothing
        }

        @Override
        public ParserState onSuccess(Double obj) throws RxParserException {
            return CborParseGenericItem.this.onSuccess(new FloatingPointItem(tags, obj));
        }
    };

    CborParseByteStringUnsafe parse_byte_string = new CborParseByteStringUnsafe() {
        @Override
        public void onTagFound(long tag) {
            // do nothing
        }

        @Override
        public void onContainerOpen(long size) {
        }

        @Override
        public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
            return CborParseGenericItem.this.onSuccess(new ByteStringItem(tags, obj));
        }
    };

    CborParseTextStringUnsafe parse_text_string = new CborParseTextStringUnsafe() {
        @Override
        public void onTagFound(long tag) {
            // do nothing
        }

        @Override
        public void onContainerOpen(long size) {

        }

        @Override
        public ParserState onSuccessUnsafe(ByteBuffer obj) throws RxParserException {
            String str = StandardCharsets.UTF_8.decode(obj).toString();
            return CborParseGenericItem.this.onSuccess(new TextStringItem(tags, str));
        }
    };

    ExtractContainerSize parse_array_size = new ExtractContainerSize(ArrayType) {
        long size;
        Collection<DataItem> array;

        @Override
        public void onTagFound(long tag) {
            // ignore tags
        }

        @Override
        public ParserState onContainerOpen(long size) throws RxParserException {
            this.size = size;
            array = new LinkedList<>();
            if (size < 0) {
                return checkBreak;
            }
            if (size == 0) {
                return CborParseGenericItem.this.onSuccess(new ArrayItem(tags, array));
            }
            // if size > 0
            return extractNestedItem();
        }

        ParserState checkBreak = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                byte b = CborParser.peek(next);
                if ((b & 0xff) == CborBreak) {
                    next.get();
                    return CborParseGenericItem.this.onSuccess(new ArrayItem(tags, array));
                } else {
                    return extractNestedItem();
                }
            }
        };

        CborParseGenericItem outer = CborParseGenericItem.this;

        CborParseGenericItem extractNestedItem() {
            return new CborParseGenericItem() {
                @Override
                public ParserState onSuccess(DataItem item) throws RxParserException {
                    array.add(item);
                    size--;
                    if (size < 0) {
                        return checkBreak;
                    }
                    if (size == 0) {
                        // it is a win, exit the recursion
                        return outer.onSuccess(new ArrayItem(tags, array));
                    }
                    // size > 0
                    return extractNestedItem();
                }
            };
        }
    };

    ExtractContainerSize parse_map_size = new ExtractContainerSize(MapType) {
        long size;
        Map<DataItem, DataItem> map;
        DataItem currentKey;

        @Override
        public void onTagFound(long tag) {
            // ignore tags
        }

        @Override
        public ParserState onContainerOpen(long size) throws RxParserException {
            this.size = size;
            map = new HashMap<>();
            if (size < 0) {
                return checkBreak;
            }
            if (size == 0) {
                return CborParseGenericItem.this.onSuccess(new MapItem(tags, map));
            }
            // if size > 0
            return extractNextNestedKey();
        }

        ParserState checkBreak = new ParserState() {
            @Override
            public ParserState onNext(ByteBuffer next) throws RxParserException {
                byte b = CborParser.peek(next);
                if ((b & 0xff) == CborBreak) {
                    next.get();
                    return CborParseGenericItem.this.onSuccess(new MapItem(tags, map));
                } else {
                    return extractNextNestedKey();
                }
            }
        };

        CborParseGenericItem extractNextNestedKey() {
            return new CborParseGenericItem() {
                @Override
                public ParserState onSuccess(DataItem item) {
                    currentKey = item;
                    return extractNextNestedValue();
                }
            };
        }

        CborParseGenericItem outer = CborParseGenericItem.this;

        CborParseGenericItem extractNextNestedValue() {
            return new CborParseGenericItem() {
                @Override
                public ParserState onSuccess(DataItem item) throws RxParserException {
                    map.put(currentKey, item);
                    size--;
                    if (size < 0) {
                        return checkBreak;
                    }
                    if (size == 0) {
                        return outer.onSuccess(new MapItem(tags, map));
                    }
                    // size > 0
                    return extractNextNestedKey();
                }
            };
        }
    };

    CborParseTag parse_tag = new CborParseTag() {
        @Override
        public ParserState onSuccess(long tag) throws RxParserException {
            return CborParseGenericItem.this.onSuccess(new TagItem(tag));
        }
    };

    CborParseSimpleValue parse_simple_value = new CborParseSimpleValue() {
        @Override
        public void onTagFound(long tag) {
            // ignore
        }

        @Override
        public ParserState onSimplevalue(int value) throws RxParserException {
            return CborParseGenericItem.this.onSuccess(new SimpleValueItem(value));
        }
    };

    public abstract ParserState onSuccess(DataItem item) throws RxParserException;
}
