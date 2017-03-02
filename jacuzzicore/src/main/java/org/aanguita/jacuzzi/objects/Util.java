package org.aanguita.jacuzzi.objects;

/**
 * Utility methods for objects
 *
 * @deprecated use Objects class from SDK
 */
public class Util {

    /**
     * Compares two objects using the first object's equal method. Before comparing, it checks for null values
     * @param o1 the first object to compare
     * @param o2 the second object to compare
     * @return true if a) both objects are null, or b) both objects are not null and are equal. False otherwise
     * todo refactor the if so it is simpler. It is very hard to read...
     */
    public static boolean equals(Object o1, Object o2) {
        return o1 == null && o2 == null || !(o1 == null || o2 == null) && o1.equals(o2);
    }
}
