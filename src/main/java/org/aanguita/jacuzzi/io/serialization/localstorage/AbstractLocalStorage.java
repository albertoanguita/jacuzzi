package org.aanguita.jacuzzi.io.serialization.localstorage;

import org.aanguita.jacuzzi.objects.ObjectMapPool;
import org.aanguita.jacuzzi.objects.Util;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Alberto on 05/12/2016.
 */
public abstract class AbstractLocalStorage implements LocalStorage {

    private static final String METADATA_CATEGORY = "localStorageMetadata";
    private static final String METADATA_LOCAL_STORAGE_VERSION = "version";
    private static final String METADATA_CREATION_DATE = "creationDate";
    private static final String CURRENT_VERSION = "1.0";



    /**
     * Path to the local database
     */
    protected final String path;

    private final String categorySeparator;

    private final String listSeparator;

    private final Map<String, String> cachedEntries;

    private static final ObjectMapPool<String, Lock> locks = new ObjectMapPool<>(s -> new ReentrantLock());

    protected AbstractLocalStorage(String path, String categorySeparator, String listSeparator, boolean createNew) {
        this.path = path;
        this.categorySeparator = categorySeparator;
        this.listSeparator = listSeparator;
        cachedEntries = Collections.synchronizedMap(new HashMap<>());
        if (createNew) {
            setMetadata();
        }
    }

    private void setMetadata() {
        setString(METADATA_LOCAL_STORAGE_VERSION, CURRENT_VERSION, METADATA_CATEGORY);
        setLong(METADATA_CREATION_DATE, System.currentTimeMillis(), METADATA_CATEGORY);
    }

    public String getPath() {
        return path;
    }

    public String getLocalStorageVersion() {
        return getString(METADATA_LOCAL_STORAGE_VERSION, METADATA_CATEGORY);
    }

    public Date getCreationDate() {
        Long date = getLong(METADATA_CREATION_DATE, METADATA_CATEGORY);
        return date != null ? new Date(date) : null;
    }

    public boolean containsItem(String name, String... categories) {
        return getString(name, categories) != null;
    }

    public void removeItem(String name, String... categories) {
        setString(name, null, categories);
    }

    public void clear() {
        cachedEntries.clear();
    }

    private <E> E loadCache(String name, E value) {
        cachedEntries.put(name, value != null ? value.toString() : null);
        return value;
    }

    protected abstract String getStoredValue(String name, String[] categories);

    protected abstract void writeValue(String name, Object value);

    private boolean setAux(String name, Object value, Object storedValue) {
        if (value == null || !Util.equals(value, storedValue)) {
            writeValue(name, loadCache(name, value));
            return true;
        } else {
            return false;
        }
    }

    public String getString(String name, String... categories) {
        name = generateStringKey(name, categories);
        if (cachedEntries.containsKey(name)) {
            return cachedEntries.get(name);
        } else {
            return loadCache(name, getStoredValue(name, categories));
        }
    }

    public boolean setString(String name, String value, String... categories) {
        name = generateStringKey(name, categories);
        String storedValue = getString(name);
        return setAux(name, value, storedValue);
    }

    public Boolean getBoolean(String name, String... categories) {
        name = generateStringKey(name, categories);
        if (cachedEntries.containsKey(name)) {
            return Boolean.parseBoolean(cachedEntries.get(name));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(name, value);
            return value != null ? Boolean.parseBoolean(value) : null;
        }
    }

    public boolean setBoolean(String name, Boolean value, String... categories) {
        name = generateStringKey(name, categories);
        Boolean storedValue = getBoolean(name);
        return setAux(name, value, storedValue);
    }

    public Byte getByte(String name, String... categories) {
        name = generateStringKey(name, categories);
        if (cachedEntries.containsKey(name)) {
            return Byte.parseByte(cachedEntries.get(name));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(name, value);
            return value != null ? Byte.parseByte(value) : null;
        }
    }

    public boolean setByte(String name, Byte value, String... categories) {
        name = generateStringKey(name, categories);
        Byte storedValue = getByte(name);
        return setAux(name, value, storedValue);
    }

    public Short getShort(String name, String... categories) {
        name = generateStringKey(name, categories);
        if (cachedEntries.containsKey(name)) {
            return Short.parseShort(cachedEntries.get(name));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(name, value);
            return value != null ? Short.parseShort(value) : null;
        }
    }

    public boolean setShort(String name, Short value, String... categories) {
        name = generateStringKey(name, categories);
        Short storedValue = getShort(name);
        return setAux(name, value, storedValue);
    }

    public Integer getInteger(String name, String... categories) {
        name = generateStringKey(name, categories);
        if (cachedEntries.containsKey(name)) {
            return Integer.parseInt(cachedEntries.get(name));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(name, value);
            return value != null ? Integer.parseInt(value) : null;
        }
    }

    public boolean setInteger(String name, Integer value, String... categories) {
        name = generateStringKey(name, categories);
        Integer storedValue = getInteger(name);
        return setAux(name, value, storedValue);
    }

    public Long getLong(String name, String... categories) {
        name = generateStringKey(name, categories);
        if (cachedEntries.containsKey(name)) {
            return Long.parseLong(cachedEntries.get(name));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(name, value);
            return value != null ? Long.parseLong(value) : null;
        }
    }

    public boolean setLong(String name, Long value, String... categories) {
        name = generateStringKey(name, categories);
        Long storedValue = getLong(name);
        return setAux(name, value, storedValue);
    }

    public Float getFloat(String name, String... categories) {
        name = generateStringKey(name, categories);
        if (cachedEntries.containsKey(name)) {
            return Float.parseFloat(cachedEntries.get(name));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(name, value);
            return value != null ? Float.parseFloat(value) : null;
        }
    }

    public boolean setFloat(String name, Float value, String... categories) {
        name = generateStringKey(name, categories);
        Float storedValue = getFloat(name);
        return setAux(name, value, storedValue);
    }

    public Double getDouble(String name, String... categories) {
        name = generateStringKey(name, categories);
        if (cachedEntries.containsKey(name)) {
            return Double.parseDouble(cachedEntries.get(name));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(name, value);
            return value != null ? Double.parseDouble(value) : null;
        }
    }

    public boolean setDouble(String name, Double value, String... categories) {
        name = generateStringKey(name, categories);
        Double storedValue = getDouble(name);
        return setAux(name, value, storedValue);
    }

    public Date getDate(String name, String... categories) {
        name = generateStringKey(name, categories);
        if (cachedEntries.containsKey(name)) {
            return new Date(Long.parseLong(cachedEntries.get(name)));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(name, value);
            return value != null ? new Date(Long.parseLong(value)) : null;
        }
    }

    public boolean setDate(String name, Date value, String... categories) {
        name = generateStringKey(name, categories);
        Date storedValue = getDate(name);
        return setAux(name, value, storedValue);
    }

    public <E> E getEnum(String name, Class<E> enum_, String... categories) {
        name = generateStringKey(name, categories);
        try {
            String str = getString(name);
            if (str != null) {
                Method valueOf = enum_.getMethod("valueOf", String.class);
                return (E) valueOf.invoke(null, str);
            } else {
                return null;
            }
        } catch (Exception e) {
            // cannot happen
            throw new RuntimeException("Fatal error in the implementation of local storage: method 'valueOf' not found in " + enum_.getClass().toString() + ". Cause: " + e.getMessage());
        }
    }

    public <E> boolean setEnum(String name, Class<E> enum_, E value, String... categories) {
        name = generateStringKey(name, categories);
        try {
            Method getName = enum_.getMethod("name");
            return setString(name, (String) getName.invoke(value));
        } catch (Exception e) {
            // cannot happen
            throw new RuntimeException("Fatal error in the implementation of local storage: method 'name' not found in " + enum_.getClass().toString() + ". Cause: " + e.getMessage());
        }
    }

    public List<String> getStringList(String name, String... categories) {
        return deserializeList(getString(name, categories));
    }

    public void setStringList(String name, List<String> list, String... categories) {
        setList(name, list, categories);
    }

    public List<Boolean> getBooleanList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Boolean::parseBoolean).collect(Collectors.toList()) : null;
    }

    public void setBooleanList(String name, List<Boolean> list, String... categories) {
        setList(name, list, categories);
    }

    public List<Byte> getByteList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Byte::parseByte).collect(Collectors.toList()) : null;
    }

    public void setByteList(String name, List<Byte> list, String... categories) {
        setList(name, list, categories);
    }

    public List<Short> getShortList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Short::parseShort).collect(Collectors.toList()) : null;
    }

    public void setShortList(String name, List<Short> list, String... categories) {
        setList(name, list, categories);
    }

    public List<Integer> getIntegerList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Integer::parseInt).collect(Collectors.toList()) : null;
    }

    public void setIntegerList(String name, List<Integer> list, String... categories) {
        setList(name, list, categories);
    }

    public List<Long> getLongList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Long::parseLong).collect(Collectors.toList()) : null;
    }

    public void setLongList(String name, List<Long> list, String... categories) {
        setList(name, list, categories);
    }

    public List<Float> getFloatList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Float::parseFloat).collect(Collectors.toList()) : null;
    }

    public void setFloatList(String name, List<Float> list, String... categories) {
        setList(name, list, categories);
    }

    public List<Double> getDoubleList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Double::parseDouble).collect(Collectors.toList()) : null;
    }

    public void setDoubleList(String name, List<Double> list, String... categories) {
        setList(name, list, categories);
    }

    public List<Date> getDateList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(s -> new Date(Long.parseLong(s))).collect(Collectors.toList()) : null;
    }

    public void setDateList(String name, List<Date> list, String... categories) {
        setLongList(name, list.stream().map(Date::getTime).collect(Collectors.toList()), categories);
    }

    public <E> List<E> getEnumList(String name, Class<E> enum_, String... categories) {
        List<String> stringList = getStringList(name, categories);
        try {
            Method valueOf = enum_.getMethod("valueOf", String.class);
            return stringList != null ? stringList.stream().map(s -> {
                try {
                    return (E) valueOf.invoke(null, s);
                } catch (Exception e) {
                    return null;
                }
            }).collect(Collectors.toList()) : null;
        } catch (Exception e) {
            // cannot happen
            throw new RuntimeException("Fatal error in the implementation of local storage: method 'valueOf' not found in " + enum_.getClass().toString() + ". Cause: " + e.getMessage());
        }
    }

    public <E> void setEnumList(String name, Class<E> enum_, List<E> list, String... categories) {
        try {
            Method getName = enum_.getMethod("name");
            List<String> stringList = list.stream().map(v -> {
                try {
                    return (String) getName.invoke(v);
                } catch (Exception e) {
                    return null;
                }
            }).collect(Collectors.toList());
            setString(name, serializeList(stringList), categories);
        } catch (NoSuchMethodException e) {
            // cannot happen
            throw new RuntimeException("Fatal error in the implementation of local storage: method 'name' not found in " + enum_.getClass().toString() + ". Cause: " + e.getMessage());
        }
    }

    private void setList(String name, List<?> list, String... categories) {
        setString(name, serializeList(list), categories);
    }

    private String serializeList(List<?> list) {
        if (list == null) {
            return null;
        } else if (list.isEmpty()) {
            return "";
        } else {
            StringBuilder serList = new StringBuilder();
            for (Object item : list) {
                serList.append(item.toString()).append(listSeparator);
            }
            return serList.toString();
        }
    }

    private List<String> deserializeList(String value) {
        if (value == null) {
            return null;
        } else {
            StringTokenizer tokenizer = new StringTokenizer(value, listSeparator);
            List<String> list = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                list.add(tokenizer.nextToken());
            }
            return list;
        }
    }

    protected String generateStringKey(String name, String... categories) {
        StringBuilder catBuilder = new StringBuilder();
        Stream.of(categories).forEach(cat -> catBuilder.append(cat).append(categorySeparator));
        return catBuilder.append(name).toString();
    }
}
