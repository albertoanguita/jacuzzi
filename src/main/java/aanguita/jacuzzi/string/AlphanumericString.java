package aanguita.jacuzzi.string;

import java.util.*;

/**
 * Created by Alberto on 16/03/2016.
 */
public class AlphanumericString {

    public enum CharType {
        NUMERIC('0', '9'),
        LOWERCASE('a', 'z'),
        UPPERCASE('A', 'Z');

        public final char first;

        public final char last;

        CharType(char first, char last) {
            this.first = first;
            this.last = last;
        }

        public boolean isLast(char c) {
            return c == last;
        }

        public static CharType getCharType(char c) {
            int intC = (int) c;
            if (intC >= ZERO && intC <= NINE) {
                return NUMERIC;
            } else if (intC >= LOW_A && intC <= LOW_Z) {
                return LOWERCASE;
            } else if (intC >= UP_A && intC <= UP_Z) {
                return UPPERCASE;
            } else {
                throw new IllegalArgumentException("Illegal character: " + c);
            }
        }
    }

    public static class CharTypeSequence {

        private final Map<CharType, CharType> nextCharType;

        public final char firstChar;

        public CharTypeSequence(CharType... charTypes) {
            this(Arrays.asList(charTypes));
        }

        public CharTypeSequence(List<CharType> charTypes) {
            if (charTypes.isEmpty()) {
                throw new IllegalArgumentException("Need at least one char type");
            }
            if (new HashSet<>(charTypes).size() < charTypes.size()) {
                throw new IllegalArgumentException("Char types cannot be repeated: " + charTypes);
            }
            nextCharType = new HashMap<>();
            for (int i = 0; i < charTypes.size() - 1; i++) {
                nextCharType.put(charTypes.get(i), charTypes.get(i + 1));
            }
            nextCharType.put(charTypes.get(charTypes.size() - 1), charTypes.get(0));
            firstChar = charTypes.get(0).first;
        }

        public boolean hasCharType(CharType charType) {
            return nextCharType.containsKey(charType);
        }

        public CharType nextCharType(CharType charType) {
            return nextCharType.get(charType);
        }

        public String firstString() {
            return "" + firstChar;
        }
    }

    private static final int ZERO = (int) '0';
    private static final int NINE = (int) '9';
    private static final int LOW_A = (int) 'a';
    private static final int LOW_Z = (int) 'z';
    private static final int UP_A = (int) 'A';
    private static final int UP_Z = (int) 'Z';

    public static String nextAlphanumericString(String alphanumericString, List<CharType> charTypes) {
        CharTypeSequence charTypeSequence = new CharTypeSequence(charTypes);
        return nextAlphanumericString(alphanumericString, charTypeSequence);
    }

    public static String nextAlphanumericString(String alphanumericString, CharTypeSequence charTypeSequence) {
        if (alphanumericString.isEmpty()) {
            return charTypeSequence.firstString();
        } else {
            StringBuilder str = new StringBuilder(alphanumericString);
            boolean finished = false;
            int index = alphanumericString.length() - 1;
            while (!finished && index >= 0) {
                char c = alphanumericString.charAt(index);
                CharType charType = CharType.getCharType(c);
                if (!charTypeSequence.hasCharType(charType)) {
                    throw new IllegalArgumentException("Invalid char: " + c);
                } else if (!charType.isLast(c)) {
                    // increase this char in its char type and finish
                    str.setCharAt(index, nextChar(c));
                    finished = true;
                } else {
                    // move this char to the next char type and continue iterating
                    char newChar = charTypeSequence.nextCharType(charType).first;
                    str.setCharAt(index, newChar);
                    finished = newChar != charTypeSequence.firstChar;
                    index--;
                }
            }
            if (!finished) {
                // we must add a new char at the beginning
                str.insert(0, charTypeSequence.firstChar);
            }
            return str.toString();
        }
    }

    public static char nextChar(char c) {
        return (char) ((int) c + 1);
    }
}
