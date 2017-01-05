package org.aanguita.jacuzzi.lists;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents non-mutable, unique keys from lists of Strings. Such lists can even contain null values
 * <p>
 * The class constructor allows parsing string lists (or arrays) into this class' objects
 */
public class StringListKey {

    private static class IndexedString {

        private final String s;

        private int index;

        public IndexedString(String s) {
            this.s = s;
            index = 0;
        }

        public int readNumber() {
            int separatorIndex = s.indexOf(DEFAULT_SEPARATOR, index);
            int number = Integer.parseInt(s.substring(index, separatorIndex));
            index = separatorIndex + DEFAULT_SEPARATOR.length();
            return number;
        }

        public String readString(int size, String separator) {
            String str = s.substring(index, index + size);
            index += size + separator.length();
            return str;
        }
    }

    public static final String DEFAULT_SEPARATOR = ",";

    public static String toString(List<String> list, String separator) {
        String finalSeparator = getSeparator(separator);
        StringBuilder sb = new StringBuilder();
        sb.append(list.size());
        list.forEach(element -> sb.append(DEFAULT_SEPARATOR).append(element.length()));
        sb.append(DEFAULT_SEPARATOR);
        list.forEach(element -> sb.append(element).append(finalSeparator));
        return sb.toString();
    }

    public static String toString(String[] list, String separator) {
        return toString(Arrays.asList(list), separator);
    }

    public static List<String> parseToList(String s, String separator) {
        String finalSeparator = getSeparator(separator);
        IndexedString indexedString = new IndexedString(s);
        int size = indexedString.readNumber();
        int[] sizes = new int[size];
        for (int i = 0; i < size; i++) {
            sizes[i] = indexedString.readNumber();
        }
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(indexedString.readString(sizes[i], finalSeparator));
        }
        return list;
    }

    public static String[] parseToArray(String s, String separator) {
        return parseToList(s, separator).toArray(new String[0]);
    }

    @NotNull
    private static String getSeparator(String separator) {
        return separator != null ? separator : DEFAULT_SEPARATOR;
    }
}
