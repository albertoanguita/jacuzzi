package org.aanguita.jacuzzi.io.serialization.localstorage;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Alberto on 05/12/2016.
 */
public interface LocalStorage {

    String getLocalStorageVersion();

    Date getCreationDate();

    int itemCount();

    List<String> keys(String... categories);

    Set<String> categories(String... categories);

    boolean containsItem(String name, String... categories);

    void removeItem(String name, String... categories);

    void clear();

    String getString(String name, String... categories);

    boolean setString(String name, String value, String... categories);

    Boolean getBoolean(String name, String... categories);

    boolean setBoolean(String name, Boolean value, String... categories);

    Byte getByte(String name, String... categories);

    boolean setByte(String name, Byte value, String... categories);

    Short getShort(String name, String... categories);

    boolean setShort(String name, Short value, String... categories);

    Integer getInteger(String name, String... categories);

    boolean setInteger(String name, Integer value, String... categories);

    Long getLong(String name, String... categories);

    boolean setLong(String name, Long value, String... categories);

    Float getFloat(String name, String... categories);

    boolean setFloat(String name, Float value, String... categories);

    Double getDouble(String name, String... categories);

    boolean setDouble(String name, Double value, String... categories);

    Date getDate(String name, String... categories);

    boolean setDate(String name, Date value, String... categories);

    <E> E getEnum(String name, Class<E> enum_, String... categories);

    <E> boolean setEnum(String name, Class<E> enum_, E value, String... categories);

    List<String> getStringList(String name, String... categories);

    void setStringList(String name, List<String> list, String... categories);

    List<Boolean> getBooleanList(String name, String... categories);

    void setBooleanList(String name, List<Boolean> list, String... categories);

    List<Byte> getByteList(String name, String... categories);

    void setByteList(String name, List<Byte> list, String... categories);

    List<Short> getShortList(String name, String... categories);

    void setShortList(String name, List<Short> list, String... categories);

    List<Integer> getIntegerList(String name, String... categories);

    void setIntegerList(String name, List<Integer> list, String... categories);

    List<Long> getLongList(String name, String... categories);

    void setLongList(String name, List<Long> list, String... categories);

    List<Float> getFloatList(String name, String... categories);

    void setFloatList(String name, List<Float> list, String... categories);

    List<Double> getDoubleList(String name, String... categories);

    void setDoubleList(String name, List<Double> list, String... categories);

    List<Date> getDateList(String name, String... categories);

    void setDateList(String name, List<Date> list, String... categories);

    <E> List<E> getEnumList(String name, Class<E> enum_, String... categories);

    <E> void setEnumList(String name, Class<E> enum_, List<E> list, String... categories);

}
