package jacz.util.numeric.logical;

/**
 * Class with static methods implementing several logical operations
 */
public class LogicalOperators {

    public static Long and(Long l1, Long l2) {
        return Long.parseLong(and(Long.toBinaryString(l1), Long.toBinaryString(l2)), 2);
    }

    public static String and(String n1, String n2) throws NumberFormatException {
        checkCorrectBinaryString(n1);
        checkCorrectBinaryString(n2);
        char[] n1char = n1.toCharArray();
        char[] n2char = n2.toCharArray();
        char[] resultChar = new char[Math.max(n1char.length, n2char.length)];

        if (n1char.length > n2char.length) {
            System.arraycopy(n1char, 0, resultChar, 0, n1char.length - n2char.length);
        } else {
            System.arraycopy(n2char, 0, resultChar, 0, n2char.length - n1char.length);
        }
        for (int i = 0; i < Math.min(n1char.length, n2char.length); i++) {
            resultChar[resultChar.length - 1] = and(n1char[n1char.length - i], n2char[n2char.length - i]);
        }
        return new String(resultChar);
    }

    private static char and(char c1, char c2) {
        if (c1 == '0') {
            return '0';
        } else {
            return c2;
        }
    }

    /*public static String or(String n1, String n2) {

    }

    public static String not(String n) {

    }

    public static String xor(String n1, String n2) {

    }*/


    public static String shiftRightZeros(String n, int shifts) throws NumberFormatException {
        checkCorrectBinaryString(n);
        if (n.length() > shifts) {
            return n.substring(0, n.length() - shifts);
        } else {
            return "0";
        }
    }

    /*public static String shiftRightOnes(String n, int shifts) {

    }*/

    private static void checkCorrectBinaryString(String n) throws NumberFormatException {
        for (Character c : n.toCharArray()) {
            if (!c.equals('0') && !c.equals('1')) {
                throw new NumberFormatException("Invalid String (non binary chars): " + n);
            }
        }
    }
}
