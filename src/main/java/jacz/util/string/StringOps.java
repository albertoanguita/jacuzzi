package jacz.util.string;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 13-oct-2007<br>
 * Last Modified: 13-oct-2007
 */
public class StringOps {


    /**
     * Transforms a given string route using a mapping of strings
     *
     * @param string   the string to modify
     * @param mappings the mappings of string
     * @return the new string
     */
    public static String transformStringWithMappings(String string, Map<String, String> mappings) {

        StringBuilder stringBuilder = new StringBuilder(string);
        for (Map.Entry<String, String> stringEntry : mappings.entrySet()) {
            transformStringWithOneMapping(stringBuilder, stringEntry.getKey(), stringEntry.getValue());
        }
        return stringBuilder.toString();
    }


    /**
     * Transforms a given string route using a given pair for the mapping
     *
     * @param string the string to modify
     * @param key    key to substitute
     * @param value  value for replacement
     */
    private static void transformStringWithOneMapping(StringBuilder string, String key, String value) {
        transformStringWithOneMapping(string, key, value, 0);
    }

    /**
     * Transforms a given string route using a given pair for the mapping
     *
     * @param string the string to modify
     * @param key    key to substitute
     * @param value  value for replacement
     */
    private static void transformStringWithOneMapping(StringBuilder string, String key, String value, int fromIndex) {
        // the method is recursive. It transforms or copies a directory at each step
        // find index of first occurrence of key in string
        int index = string.indexOf(key, fromIndex);
        if (index >= 0) {
            string.replace(index, index + key.length(), value);
            transformStringWithOneMapping(string, key, value, index + value.length());
        }
    }

    /**
     * Separates a string in a list of tokens. It takes into account empty tokens, unlike the String.split of the StringTokenizer
     *
     * @param str                the string to separate
     * @param separator          separator sequence
     * @param requireNonEmpty    whether all values should be non-empty
     * @param nullValue          if null values are admitted, this represents the null values (if not null)
     * @param expectedTokenCount if there is an expected token count, this parameter indicates it (if >= 0)
     * @return the list of parsed tokens
     * @throws ParseException if there is an error parsing the string
     */
    public static List<String> separateTokens(String str, String separator, boolean requireNonEmpty, String nullValue, int expectedTokenCount) throws ParseException {
        List<String> tokens = new ArrayList<>();
        String originalLine = str;
        int offset = 0;
        while (str.contains(separator)) {
            int index = str.indexOf(separator);
            String token = str.substring(0, index);
            if (nullValue != null && token.equals(nullValue)) {
                token = null;
            }
            if (requireNonEmpty && (token == null || token.isEmpty())) {
                throw new ParseException("Empty or null token found at: " + originalLine, offset);
            }
            tokens.add(token);
            str = str.substring(index + separator.length());
            offset += index + separator.length();
        }
        // add the last token
        if (nullValue != null && str.equals(nullValue)) {
            str = null;
        }
        if (requireNonEmpty && (str == null || str.isEmpty())) {
            throw new ParseException("Empty token found at: " + str, offset);
        }
        tokens.add(str);
        if (expectedTokenCount >= 0 && tokens.size() != expectedTokenCount) {
            throw new ParseException("Wrong token size: " + tokens.size() + ". Expected: " + expectedTokenCount + ". Line: " + originalLine, offset);
        }
        return tokens;
    }

}
