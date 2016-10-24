package org.aanguita.jacuzzi.io.serialization;

/**
 * Class for translating between binary and hexadecimal representations
 */
public class Hex {

    public static String asHex(byte[] data) {
        return asHex(data, false);
    }

    public static String asHex(byte[] data, boolean uppercase) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte oneByte : data) {
            stringBuilder.append(asHex(oneByte, uppercase));
        }
        return stringBuilder.toString();
    }

    public static String asHex(byte oneByte, boolean uppercase) {
        int intByte = (int) oneByte;
        intByte = intByte < 0 ? intByte + 0x100 : intByte;
        String strByte = Integer.toBinaryString(intByte + 0x100).substring(1);
        return "" + toHexChar(strByte.substring(0, 4), uppercase) + toHexChar(strByte.substring(4), uppercase);
    }

    public static char toHexChar(String fourBits, boolean uppercase) {
        if (fourBits == null || fourBits.length() != 4) {
            throw new IllegalArgumentException("Invalid argument. Must be non null or of length 4. Received " + fourBits);
        }
        switch (fourBits) {
            case "0000":
                return '0';
            case "0001":
                return '1';
            case "0010":
                return '2';
            case "0011":
                return '3';
            case "0100":
                return '4';
            case "0101":
                return '5';
            case "0110":
                return '6';
            case "0111":
                return '7';
            case "1000":
                return '8';
            case "1001":
                return '9';
            case "1010":
                return uppercase ? 'A' : 'a';
            case "1011":
                return uppercase ? 'B' : 'b';
            case "1100":
                return uppercase ? 'C' : 'c';
            case "1101":
                return uppercase ? 'D' : 'd';
            case "1110":
                return uppercase ? 'E' : 'e';
            case "1111":
                return uppercase ? 'F' : 'f';
            default:
                throw new IllegalArgumentException("Invalid argument. Must be a string of zeros and ones. Received " + fourBits);
        }
    }

    public static String asBinary(String hex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char hexChar : hex.toCharArray()) {
            stringBuilder.append(toFourBits(hexChar));
        }
        return stringBuilder.toString();
    }

    public static String toFourBits(char hexChar) {
        switch (hexChar) {
            case '0':
                return "0000";
            case '1':
                return "0001";
            case '2':
                return "0010";
            case '3':
                return "0011";
            case '4':
                return "0100";
            case '5':
                return "0101";
            case '6':
                return "0110";
            case '7':
                return "0111";
            case '8':
                return "1000";
            case '9':
                return "1001";
            case 'a':
            case 'A':
                return "1010";
            case 'b':
            case 'B':
                return "1011";
            case 'c':
            case 'C':
                return "1100";
            case 'd':
            case 'D':
                return "1101";
            case 'e':
            case 'E':
                return "1110";
            case 'f':
            case 'F':
                return "1111";
            default:
                throw new IllegalArgumentException("Invalid hex char received: " + hexChar);
        }
    }
}
