package org.aanguita.jacuzzi.io.localstorage;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Alberto on 17/12/2016.
 */
public interface ReadOnlyLocalStorage {

    String getLocalStorageVersion();

    Date getCreationDate();

    String getListSeparator();

    boolean isUseCache();

    int itemCount();

    Set<String> keys(String... categories);

    Set<String> categories(String... categories);

    boolean containsItem(String name, String... categories);

    String getString(String name, String... categories);

    Boolean getBoolean(String name, String... categories);

    Byte getByte(String name, String... categories);

    Short getShort(String name, String... categories);

    Integer getInteger(String name, String... categories);

    Long getLong(String name, String... categories);

    Float getFloat(String name, String... categories);

    Double getDouble(String name, String... categories);

    Date getDate(String name, String... categories);

    <E> E getEnum(String name, Class<E> enum_, String... categories);

    List<String> getStringList(String name, String... categories);

    List<Boolean> getBooleanList(String name, String... categories);

    List<Byte> getByteList(String name, String... categories);

    List<Short> getShortList(String name, String... categories);

    List<Integer> getIntegerList(String name, String... categories);

    List<Long> getLongList(String name, String... categories);

    List<Float> getFloatList(String name, String... categories);

    List<Double> getDoubleList(String name, String... categories);

    List<Date> getDateList(String name, String... categories);

    <E> List<E> getEnumList(String name, Class<E> enum_, String... categories) throws ClassCastException;
}
