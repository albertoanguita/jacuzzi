package org.aanguita.jacuzzi.string;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 3/08/11<br>
 * Last Modified: 3/08/11
 */
public class StringBuilderPool {

    private HashSet<StringBuilder> pool;

    private int counter = 0;

    public StringBuilderPool() {
        pool = new HashSet<>();
    }

    public StringBuilder request() {
        if (pool.isEmpty()) {
            counter++;
            return new StringBuilder();
        } else {
            Iterator<StringBuilder> it = pool.iterator();
            StringBuilder stringBuilder = it.next();
            it.remove();
            return stringBuilder;
        }
    }

    public void free(StringBuilder stringBuilder) {
        pool.add(stringBuilder);
    }

    public static void copy(StringBuilder src, StringBuilder dest) {
        dest.setLength(0);
        dest.append(src);
    }

    public static void clear(StringBuilder str) {
        str.setLength(0);
    }

    public static void divide(StringBuilder src, StringBuilder dest1, StringBuilder dest2, int index) {
        // clear the destination strings and copy the src string
        dest1.setLength(0);
        dest2.setLength(0);
        dest1.append(src);
        dest2.append(src);

        // remove the unwanted part
        dest1.delete(index, dest1.length());
        dest2.delete(0, index);
    }

    public static int compareTo(StringBuilder sb, String s) {
        int i = 0;
        boolean equal = true;
        while (i < sb.length() && i < s.length()) {
            if (sb.charAt(i) == s.charAt(i)) {
                i++;
            } else {
                return sb.charAt(i) - s.charAt(i);
            }
        }
        if (i < sb.length()) {
            return 1;
        } else if (i < s.length()) {
            return -1;
        } else {
            return 0;
        }
    }
}
