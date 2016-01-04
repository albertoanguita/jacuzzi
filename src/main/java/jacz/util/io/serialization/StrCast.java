package jacz.util.io.serialization;

/**
 * Methods for casting strings (included null values) to different basic types
 */
public class StrCast {

    public static Byte asByte(String value) throws NumberFormatException {
        return  (value != null) ? Byte.parseByte(value) : null;
    }

    public static Short asShort(String value) throws NumberFormatException {
        return  (value != null) ? Short.parseShort(value) : null;
    }

    public static Integer asInteger(String value) throws NumberFormatException {
        return  (value != null) ? Integer.parseInt(value) : null;
    }

    public static Long asLong(String value) throws NumberFormatException {
        return  (value != null) ? Long.parseLong(value) : null;
    }

    public static Float asFloat(String value) throws NumberFormatException {
        return  (value != null) ? Float.parseFloat(value) : null;
    }

    public static Double asDouble(String value) throws NumberFormatException {
        return  (value != null) ? Double.parseDouble(value) : null;
    }

    public static Boolean asBoolean(String value) throws NumberFormatException {
        return  (value != null) ? Boolean.parseBoolean(value) : null;
    }
}
