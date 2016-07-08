package org.aanguita.jacuzzi.io.serialization;

import java.io.*;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains utility methods for transforming different types of objects into byte arrays, and vice-versa
 */
public class Serializer {

    private static final String UTF8 = "UTF-8";

    private static final String LIST_SERIALIZER_SEPARATOR = "/";

    /**
     * Adds several byte arrays into a single byte array (admits null values)
     *
     * @return a byte array result of the concatenation of the argument byte arrays
     */
    public static byte[] addArrays(byte[]... arrays) {
        int totalLength = 0;
        int partialLength = 0;
        for (byte[] oneArray : arrays) {
            if (oneArray != null) {
                totalLength += oneArray.length;
            }
        }
        byte[] result = new byte[totalLength];
        for (byte[] oneArray : arrays) {
            if (oneArray != null) {
                System.arraycopy(oneArray, 0, result, partialLength, oneArray.length);
                partialLength += oneArray.length;
            }
        }
        return result;
    }

    /**
     * Serializes an object into a byte array
     *
     * @param o Object to serialize
     * @return byte array containing the object
     */
    public static byte[] serializeObject(Serializable o) throws NotSerializableException {
        byte[] objectData = serializeObjectWithoutLengthHeader(o);
        return addArrays(Serializer.serialize(objectData.length), objectData);
    }


    public static byte[] serializeObjectWithoutLengthHeader(Serializable o) throws NotSerializableException {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(o);
            so.flush();
            so.close();
            bo.close();
            return bo.toByteArray();
        } catch (IOException e) {
            // the object is not serializable
            throw new NotSerializableException(o.getClass().toString());
        }
    }


    /**
     * Serializes an object into a String
     *
     * @param o Object to serialize
     * @return String containing the object
     */
    public static String serializeObjectToString(Serializable o) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream so = new ObjectOutputStream(bo);
        so.writeObject(o);
        so.flush();
        // This encoding induces a bijection between byte[] and String (unlike UTF-8)
        return bo.toString("ISO-8859-1");
    }

    /**
     * Serializes a list of objects into a String without using the native Java serialization API. The process builds a string as follows:
     * First, the number of elements and a separator are written. Second, for each element, their length and a separator are written, followed by
     * the element itself
     * <p/>
     * This is not an efficient serialization, but is readable and avoids unexpected characters to appear
     *
     * @param list Object list to serialize
     * @return String containing the object
     */
    public static String serializeListToReadableString(Object... elements) {
        return serializeListToReadableString(Arrays.asList(elements));
    }

    /**
     * Serializes a list of objects into a String without using the native Java serialization API. The process builds a string as follows:
     * First, the number of elements and a separator are written. Second, for each element, their length and a separator are written, followed by
     * the element itself
     * <p/>
     * This is not an efficient serialization, but is readable and avoids unexpected characters to appear
     *
     * @param list Object list to serialize
     * @return String containing the object
     */
    public static String serializeListToReadableString(List<?> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(list.size()).append(LIST_SERIALIZER_SEPARATOR);
        for (Object o : list) {
            if (o != null) {
                stringBuilder.append(o.toString().length()).append(LIST_SERIALIZER_SEPARATOR).append(o.toString()).append(LIST_SERIALIZER_SEPARATOR);
            } else {
                stringBuilder.append(-1).append(LIST_SERIALIZER_SEPARATOR).append(LIST_SERIALIZER_SEPARATOR);
            }
        }
        return stringBuilder.toString();
    }

    public static byte[] serializeListToByteArray(List<?> list) {
        return serialize(serializeListToReadableString(list));
    }

    /**
     * Serializes a string object into a byte array with UTF-8. The byte array contains 4 bytes for indicating the length of the string, and the necessary
     * bytes for storing the characters of the string
     *
     * @param str String to serialize
     * @return byte array containing the string object
     */
    public static byte[] serialize(String str) {
        try {
            return serialize(str, UTF8);
        } catch (UnsupportedEncodingException e) {
            // ignore, cannot happen
            return new byte[0];
        }
    }

    /**
     * Serializes a string object into a byte array. The byte array contains 4 bytes for indicating the length of the string, and the necessary
     * bytes for storing the characters of the string
     *
     * @param str String to serialize
     * @return byte array containing the string object
     */
    public static byte[] serialize(String str, String encoding) throws UnsupportedEncodingException {
        if (str == null) {
            // null values are serialized with a 4-byte array containing a -1
            return Serializer.serialize(-1);
        } else {
            byte[] strBytes = str.getBytes(encoding);
            return addArrays(serialize(strBytes.length), strBytes);
        }
    }

    /**
     * Serializes a Boolean object in a 1 byte array
     *
     * @param b the Boolean object to serialize
     * @return a byte array of size 1 with the value of the given Boolean
     */
    public static byte[] serialize(Boolean b) {
        if (b == null) {
            // null values are serialized with 1 byte containing a -1
            return serialize((byte) -1);
        } else if (b) {
            return serialize((byte) 1);
        } else {
            return serialize((byte) 0);
        }
    }

    /**
     * Serializes a boolean value in a 1 byte array
     *
     * @param b the boolean value to serialize
     * @return a byte array of size 1 with the value of the given boolean
     */
    public static byte[] serialize(boolean b) {
        return serialize(Boolean.valueOf(b));
    }

    /**
     * Serializes a Byte object in a 1 or 2 byte array
     *
     * @param b the Byte object to serialize
     * @return a byte array of size 1 or 2 with the value of the given byte
     */
    public static byte[] serialize(Byte b) {
        if (b != null) {
            return addArrays(serialize(true), serialize(b.byteValue()));
        } else {
            // b is null -> just serialize a false
            return addArrays(serialize(false));
        }
    }

    /**
     * Serializes a byte value in a 1 byte array
     *
     * @param b the byte value to serialize
     * @return a byte array of size 1 with the value of the given byte
     */
    public static byte[] serialize(byte b) {
        return serializeNumber((long) b, 1);
    }

    /**
     * Serializes a Short object in a 2 or 3 byte array
     *
     * @param s the Byte object to serialize
     * @return a byte array of size 2 or 3 with the value of the given short
     */
    public static byte[] serialize(Short s) {
        if (s != null) {
            return addArrays(serialize(true), serialize(s.shortValue()));
        } else {
            // s is null -> just serialize a false
            return addArrays(serialize(false));
        }
    }

    /**
     * Serializes a short value in a 2 byte array
     *
     * @param s the short value to serialize
     * @return a byte array of size 2 with the value of the given short
     */
    public static byte[] serialize(short s) {
        return serializeNumber((long) s, 2);
    }

    /**
     * Serializes an Integer object in a 4 or 5 byte array
     *
     * @param i the Integer object to serialize
     * @return a byte array of size 4 or 5 with the value of the given integer
     */
    public static byte[] serialize(Integer i) {
        if (i != null) {
            return addArrays(serialize(true), serialize(i.intValue()));
        } else {
            // i is null -> just serialize a false
            return addArrays(serialize(false));
        }
    }

    /**
     * Serializes an int value in a 4 byte array
     *
     * @param i the int value to serialize
     * @return a byte array of size 4 with the value of the given integer
     */
    public static byte[] serialize(int i) {
        return serializeNumber((long) i, 4);
    }

    /**
     * Serializes a Long object in an 8 or 9 byte array
     *
     * @param l the Long object to serialize
     * @return a byte array of size 8 or 9 with the value of the given long
     */
    public static byte[] serialize(Long l) {
        if (l != null) {
            return addArrays(serialize(true), serialize(l.longValue()));
        } else {
            // l is null -> just serialize a false
            return addArrays(serialize(false));
        }
    }

    /**
     * Serializes a long value in an 8 byte array
     *
     * @param l the long value to serialize
     * @return a byte array of size 8 with the value of the given long
     */
    public static byte[] serialize(long l) {
        return serializeNumber(l, 8);
    }

    /**
     * Serializes a Float object in a 4 or 5 byte array
     *
     * @param f the Float object to serialize
     * @return a byte array of size 4 or 5 with the value of the given long
     */
    public static byte[] serialize(Float f) {
        if (f == null) {
            return serialize((Integer) null);
        } else {
            return serialize(new Integer(Float.floatToIntBits(f)));
        }
    }

    /**
     * Serializes a float value in a 4 byte array
     *
     * @param f the float object to serialize
     * @return a byte array of size 4 with the value of the given long
     */
    public static byte[] serialize(float f) {
        return serialize(Float.floatToIntBits(f));
    }

    /**
     * Serializes a Double object in an 8 or 9 byte array
     *
     * @param d the Double object to serialize
     * @return a byte array of size 8 or 9 with the value of the given long
     */
    public static byte[] serialize(Double d) {
        if (d == null) {
            return serialize((Long) null);
        } else {
            return serialize(new Long(Double.doubleToLongBits(d)));
        }
    }

    /**
     * Serializes a double value in an 8 byte array
     *
     * @param d the double value to serialize
     * @return a byte array of size 8 with the value of the given long
     */
    public static byte[] serialize(double d) {
        return serialize(Double.doubleToLongBits(d));
    }

    public static <T extends Enum<T>> byte[] serialize(Enum<T> e) {
        return Serializer.serialize(e.ordinal());
    }

    private static byte[] serializeNumber(long number, int byteCount) {
        String hex = Long.toHexString(number);
        hex = keepHexDigits(hex, byteCount * 2);
        byte[] data = new byte[byteCount];
        for (int j = 0; j < byteCount; j++) {
            data[j] = (byte) Short.parseShort(hex.substring(2 * j, 2 * j + 2), 16);
        }
        return data;
    }

    public static byte[] serialize(byte[] bytes) {
        if (bytes != null) {
            return addArrays(serialize(new Integer(bytes.length)), bytes);
        } else {
            return serialize((Integer) null);
        }
    }

    public static byte[] deserializeRest(byte[] data, MutableOffset offset) {
        byte[] rest = new byte[data.length - offset.value()];
        System.arraycopy(data, offset.value(), rest, 0, rest.length);
        offset.add(rest.length);
        return rest;
    }

    public static Object deserializeObject(byte[] data, MutableOffset offset) throws ClassNotFoundException {
        int objectLength = deserializeIntValue(data, offset);
        byte[] objectData = Arrays.copyOfRange(data, offset.value(), offset.value() + objectLength);
        Object o = deserializeObjectWithoutLengthHeader(objectData);
        offset.add(objectLength);
        return o;
    }

    public static Object deserializeObjectWithoutLengthHeader(byte[] data) throws ClassNotFoundException {
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(data);
            ObjectInputStream si = new ObjectInputStream(bi);
            return si.readObject();
        } catch (IOException e) {
            // this exception cannot happen, since the input stream is in memory, not in the file system. Nevertheless, throw the exception
            e.printStackTrace();
            return null;
        }
    }

    public static Object deserializeObject(String s) throws IOException, ClassNotFoundException {
        // This encoding induces a bijection between byte[] and String (unlike UTF-8)
        byte b[] = s.getBytes("ISO-8859-1");
        ByteArrayInputStream bi = new ByteArrayInputStream(b);
        ObjectInputStream si = new ObjectInputStream(bi);
        return si.readObject();
    }

    public static List<String> deserializeListFromReadableString(String s) throws ParseException {
        int offset = 0;
        int index = s.indexOf(LIST_SERIALIZER_SEPARATOR);
        try {
            int elementCount = Integer.parseInt(s.substring(0, index));
            index += LIST_SERIALIZER_SEPARATOR.length();
            List<String> list = new ArrayList<>();
            while (list.size() < elementCount) {
                offset = index;
                index = s.indexOf(LIST_SERIALIZER_SEPARATOR, offset);
                int elementLength = Integer.parseInt(s.substring(offset, index));
                if (elementLength >= 0) {
                    String element = s.substring(index + LIST_SERIALIZER_SEPARATOR.length(), index + LIST_SERIALIZER_SEPARATOR.length() + elementLength);
                    list.add(element);
                    index += LIST_SERIALIZER_SEPARATOR.length() + elementLength + LIST_SERIALIZER_SEPARATOR.length();
                } else {
                    // element is null
                    list.add(null);
                    index += LIST_SERIALIZER_SEPARATOR.length() + LIST_SERIALIZER_SEPARATOR.length();
                }
            }
            return list;
        } catch (Exception e) {
            throw new ParseException("Error parsing the string", offset);
        }
    }

    public static List<String> deserializeListFromByteArray(byte[] data, MutableOffset offset) throws ParseException {
        return deserializeListFromReadableString(deserializeString(data, offset));
    }

    public static String deserializeString(byte[] data, MutableOffset offset) {
        try {
            return deserializeString(data, UTF8, offset);
        } catch (UnsupportedEncodingException e) {
            // ignore, cannot happen
            return "";
        }
    }

    public static String deserializeString(byte[] data, String encoding, MutableOffset offset) throws UnsupportedEncodingException {
        int strLen = deserializeIntValue(data, offset);
        if (strLen < 0) {
            return null;
        } else {
            byte[] strBytes = new byte[strLen];
            System.arraycopy(data, offset.value(), strBytes, 0, strLen);
            offset.add(strLen);
            return new String(strBytes, encoding);
        }
    }

    public static Boolean deserializeBoolean(byte[] data, MutableOffset offset) {
        byte b = deserializeByteValue(data, offset);
        if (b == -1) {
            return null;
        } else {
            return b != 0;
        }
    }

    public static boolean deserializeBooleanValue(byte[] data, MutableOffset offset) {
        return deserializeByteValue(data, offset) != 0;
    }

    public static Byte deserializeByte(byte[] data, MutableOffset offset) {
        boolean notNull = deserializeBooleanValue(data, offset);
        if (notNull) {
            return deserializeByteValue(data, offset);
        } else {
            return null;
        }
    }

    public static byte deserializeByteValue(byte[] data, MutableOffset offset) {
        return deserializeNumber(data, 1, offset).byteValue();
    }

    public static Short deserializeShort(byte[] data, MutableOffset offset) {
        boolean notNull = deserializeBooleanValue(data, offset);
        if (notNull) {
            return deserializeShortValue(data, offset);
        } else {
            return null;
        }
    }

    public static short deserializeShortValue(byte[] data, MutableOffset offset) {
        return deserializeNumber(data, 2, offset).shortValue();
    }

    public static Integer deserializeInt(byte[] data, MutableOffset offset) {
        boolean notNull = deserializeBooleanValue(data, offset);
        if (notNull) {
            return deserializeIntValue(data, offset);
        } else {
            return null;
        }
    }

    public static int deserializeIntValue(byte[] data, MutableOffset offset) {
        return deserializeNumber(data, 4, offset).intValue();
    }

    public static Long deserializeLong(byte[] data, MutableOffset offset) {
        boolean notNull = deserializeBooleanValue(data, offset);
        if (notNull) {
            return deserializeLongValue(data, offset);
        } else {
            return null;
        }
    }

    public static long deserializeLongValue(byte[] data, MutableOffset offset) {
        return deserializeNumber(data, 8, offset);
    }

    public static Float deserializeFloat(byte[] data, MutableOffset offset) {
        Integer floatBits = deserializeInt(data, offset);
        if (floatBits != null) {
            return Float.intBitsToFloat(floatBits);
        } else {
            return null;
        }
    }

    public static float deserializeFloatValue(byte[] data, MutableOffset offset) {
        return Float.intBitsToFloat(deserializeIntValue(data, offset));
    }

    public static Double deserializeDouble(byte[] data, MutableOffset offset) {
        Long doubleBits = deserializeLong(data, offset);
        if (doubleBits != null) {
            return Double.longBitsToDouble(doubleBits);
        } else {
            return null;
        }
    }

    public static double deserializeDoubleValue(byte[] data, MutableOffset offset) {
        return Double.longBitsToDouble(deserializeLongValue(data, offset));
    }

    public static <E extends Enum<E>> E deserializeEnum(Class<E> enumType, byte[] data, MutableOffset offset) {
        int ordinal = Serializer.deserializeIntValue(data, offset);
        for (Enum<E> value : enumType.getEnumConstants()) {
            if (value.ordinal() == ordinal) {
                //noinspection unchecked
                return (E) value;
            }
        }
        return null;
    }

    private static Long deserializeNumber(byte[] data, int byteCount, MutableOffset offset) {
        String hex = "";
        for (int i = 0; i < byteCount; i++) {
            hex = hex + addZerosLeft(byteToHex(data[i + offset.value()]), 2);
        }
        offset.add(byteCount);
        return new BigInteger(hex, 16).longValue();
    }

    public static byte[] deserializeBytes(byte[] data, MutableOffset offset) {
        Integer bytesLen = deserializeInt(data, offset);
        if (bytesLen != null) {
            byte[] bytes = new byte[bytesLen];
            System.arraycopy(data, offset.value(), bytes, 0, bytesLen);
            offset.add(bytesLen);
            return bytes;
        } else {
            return null;
        }
    }

    private static String byteToHex(byte b) {
        String intHex = Integer.toHexString((int) b);
        if (intHex.length() > 2) {
            intHex = intHex.substring(intHex.length() - 2, intHex.length());
        }
        return intHex;
    }

    private static String keepHexDigits(String hex, int digitCount) {
        if (hex.length() > digitCount) {
            return hex.substring(hex.length() - digitCount, hex.length());
        } else {
            return addZerosLeft(hex, digitCount);
        }
    }

    private static String addZerosLeft(String str, int expectedLength) {
        while (str.length() < expectedLength) {
            str = "0" + str;
        }
        return str;
    }
}
