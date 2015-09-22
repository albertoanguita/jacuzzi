package jacz.util.io.object_serialization;

/**
 * Methods for casting strings (included null values) to different basic types
 */
public class StrCast {

    public static Byte asByte(String value) {
        return  (value != null) ? Byte.parseByte(value) : null;
    }

    public static Short asShort(String value) {
        return  (value != null) ? Short.parseShort(value) : null;
    }

    public static Integer asInteger(String value) {
        return  (value != null) ? Integer.parseInt(value) : null;
    }

    public static Long asLong(String value) {
        return  (value != null) ? Long.parseLong(value) : null;
    }

    public static Float asFloat(String value) {
        return  (value != null) ? Float.parseFloat(value) : null;
    }

    public static Double asDouble(String value) {
        return  (value != null) ? Double.parseDouble(value) : null;
    }

    public static Boolean asBoolean(String value) {
        return  (value != null) ? Boolean.parseBoolean(value) : null;
    }
}
