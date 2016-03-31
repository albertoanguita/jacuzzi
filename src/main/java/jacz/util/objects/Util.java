package jacz.util.objects;

/**
 * Created by Alberto on 31/03/2016.
 */
public class Util {

    /**
     * Compares two objects using the first object's equal method. Before comparing, it checks for null values
     * @param o1
     * @param o2
     * @return
     */
    public static boolean compare(Object o1, Object o2) {
        return o1 == null && o2 == null || !(o1 == null || o2 == null) && o1.equals(o2);
    }
}
