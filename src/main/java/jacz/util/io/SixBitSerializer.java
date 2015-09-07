package jacz.util.io;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides static methods for serializing/de-serializing in String objects where each character represent 6 bits.
 */
public class SixBitSerializer {

    private static final char EXTRA_CHAR_1 = '_';

    private static final char EXTRA_CHAR_2 = '-';

    public static final char FIRST_CHAR = '0';

    public static String serialize(byte[] data) throws IllegalArgumentException {
        List<Byte> copyData = new ArrayList<>(data.length);
        for (byte b : data) {
            copyData.add(b);
        }
        StringBuilder storedBits = new StringBuilder();
        StringBuilder serializedData = new StringBuilder();
        while (isThereDataLeft(copyData, storedBits)) {
            byte b = nextSixBits(copyData, storedBits);
            serializedData.append(serializeSixBits(b));
        }
        return serializedData.toString();
    }

    private static byte nextSixBits(List<Byte> copyData, StringBuilder storedBits) {
        if (storedBits.length() >= 6) {
            byte b = Byte.parseByte(storedBits.substring(0, 6), 2);
            storedBits.delete(0, 6);
            return b;
        } else if (copyData.isEmpty()) {
            byte b = Byte.parseByte(storedBits.toString(), 2);
            storedBits.delete(0, storedBits.length());
            return b;
        } else {
            //storedBits.append(Integer.toBinaryString(copyData.remove(0) + 0x100).substring(1));
            String bits = Integer.toBinaryString(copyData.remove(0) + 0x100);
            while (bits.length() > 8) {
                bits = bits.substring(1);
            }
            storedBits.append(bits);
            return nextSixBits(copyData, storedBits);
        }
    }

    private static boolean isThereDataLeft(List<Byte> data, StringBuilder storedBits) {
        return storedBits.length() > 0 || data.size() > 0;
    }

    public static byte[] deserialize(String s, int length) throws IndexOutOfBoundsException, IllegalArgumentException {
        byte[] data = new byte[length];
        StringBuilder storedBits = new StringBuilder();
        StringBuilder serializedData = new StringBuilder(s);
        for (int i = 0; i < length; i++) {
            data[i] = nextByte(storedBits, serializedData, i == length - 1);
        }
        if (serializedData.length() > 0) {
            // there are unused characters
            throw new IndexOutOfBoundsException("The provided string has too many characters");
        }
        return data;
    }

    private static byte nextByte(StringBuilder storedBits, StringBuilder serializedData, boolean isLastByte) throws IndexOutOfBoundsException {
        if (storedBits.length() >= 8) {
            // lots of deserialized bits -> use 8 from here
            byte b = (byte) Integer.parseInt(storedBits.substring(0, 8), 2);
            storedBits.delete(0, 8);
            return b;
        } else if (serializedData.length() == 0) {
            // few deserialized bits, and we cannot get anymore -> use the few we have
            if (storedBits.length() == 0) {
                // no remaining data to deserialize -> error
                throw new IndexOutOfBoundsException("The provided string cannot produce so many bytes");
            } else {
                byte b = Byte.parseByte(storedBits.toString(), 2);
                storedBits.delete(0, storedBits.length());
                return b;
            }
        } else {
            // few bits but we can get more -> get 8 more
            char c = serializedData.charAt(0);
            serializedData.delete(0, 1);
            byte b = deserializeSixBits(c);
            int i = b + 0x100;
            String s = Integer.toBinaryString(i);
            if (isLastByte && storedBits.length() > 2) {
                // this is the last byte we will return and there is no more data to deserialize -> add only the needed bits to reach 8
                s = s.substring(1 + storedBits.length());
            } else {
                // add 6 more bits to the deserialized series
                s = s.substring(3);
            }
            storedBits.append(s);
            return nextByte(storedBits, serializedData, isLastByte);
        }
    }


    private static char serializeSixBits(byte b) throws IllegalArgumentException {
        if (b >= 0 && b <= 9) {
            return (char) ((byte) '0' + b);
        } else if (b >= 10 && b <= 35) {
            return (char) ((byte) 'a' + b - 10);
        } else if (b >= 36 && b <= 61) {
            return (char) ((byte) 'A' + b - 36);
        } else if (b == 62) {
            return EXTRA_CHAR_1;
        } else if (b == 63) {
            return EXTRA_CHAR_2;
        } else {
            throw new IllegalArgumentException("Byte must be between 0 and 63, found " + b);
        }
    }

    private static byte deserializeSixBits(char c) throws IllegalArgumentException {
        byte byteChar = (byte) c;
        if (byteChar >= (byte) '0' && byteChar <= (byte) '9') {
            return (byte) (byteChar - (byte) '0');
        } else if (byteChar >= (byte) 'a' && byteChar <= (byte) 'z') {
            return (byte) (byteChar - (byte) 'a' + 10);
        } else if (byteChar >= (byte) 'A' && byteChar <= (byte) 'Z') {
            return (byte) (byteChar - (byte) 'A' + 36);
        } else if (byteChar == (byte) EXTRA_CHAR_1) {
            return 62;
        } else if (byteChar == (byte) EXTRA_CHAR_2) {
            return 63;
        } else {
            throw new IllegalArgumentException("Char must be between '0' and '9', or 'a' and 'z', or 'A' and 'Z', or be '-' or '_'. Found " + c);
        }
    }

    public static char getNextChar(char c) {
        byte byteChar = (byte) c;
        if (byteChar >= (byte) '0' && byteChar <= (byte) '8') {
            return (char) (byteChar + 1);
        } else if (c == '9') {
            return 'a';
        } else if (byteChar >= (byte) 'a' && byteChar <= (byte) 'y') {
            return (char) (byteChar + 1);
        } else if (c == 'z') {
            return 'A';
        } else if (byteChar >= (byte) 'A' && byteChar <= (byte) 'Y') {
            return (char) (byteChar + 1);
        } else if (c == 'Z') {
            return EXTRA_CHAR_1;
        } else if (c == EXTRA_CHAR_1) {
            return EXTRA_CHAR_2;
        } else if (c == EXTRA_CHAR_2) {
            return '0';
        } else {
            throw new IllegalArgumentException("Char must be between '0' and '9', or 'a' and 'z', or 'A' and 'Z', or be '-' or '_'. Found " + c);
        }
    }


}
