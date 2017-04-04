package org.aanguita.jacuzzi.io.localstorage;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by Alberto on 05/12/2016.
 */
public interface LocalStorage extends ReadOnlyLocalStorage {

    void removeItem(String name, String... categories) throws IOException;

    void clear() throws IOException;

    boolean setString(String name, String value, String... categories) throws IOException;

    boolean setBoolean(String name, Boolean value, String... categories) throws IOException;

    boolean setByte(String name, Byte value, String... categories) throws IOException;

    boolean setShort(String name, Short value, String... categories) throws IOException;

    boolean setInteger(String name, Integer value, String... categories) throws IOException;

    boolean setLong(String name, Long value, String... categories) throws IOException;

    boolean setFloat(String name, Float value, String... categories) throws IOException;

    boolean setDouble(String name, Double value, String... categories) throws IOException;

    boolean setDate(String name, Date value, String... categories) throws IOException;

    <E> boolean setEnum(String name, Class<E> enum_, E value, String... categories);

    void setStringList(String name, List<String> list, String... categories) throws IOException;

    void setBooleanList(String name, List<Boolean> list, String... categories) throws IOException;

    void setByteList(String name, List<Byte> list, String... categories) throws IOException;

    void setShortList(String name, List<Short> list, String... categories) throws IOException;

    void setIntegerList(String name, List<Integer> list, String... categories) throws IOException;

    void setLongList(String name, List<Long> list, String... categories) throws IOException;

    void setFloatList(String name, List<Float> list, String... categories) throws IOException;

    void setDoubleList(String name, List<Double> list, String... categories) throws IOException;

    void setDateList(String name, List<Date> list, String... categories) throws IOException;

    <E> void setEnumList(String name, Class<E> enum_, List<E> list, String... categories) throws IOException;
}
