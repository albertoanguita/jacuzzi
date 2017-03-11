package org.aanguita.jacuzzi.io.serialization.localstorage;

import org.aanguita.jacuzzi.lists.StringListKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Alberto on 05/12/2016.
 */
public abstract class AbstractLocalStorage implements LocalStorage {

    protected static final String METADATA_CATEGORY = "localStorageMetadata";
    private static final String LOCAL_STORAGE_VERSION = "version";
    private static final String CREATION_DATE = "creationDate";
    private static final String LIST_SEPARATOR = "listSeparator";
    private static final String USE_CACHE = "useCache";
    private static final String CURRENT_VERSION = "1.0";


    /**
     * Path to the local database
     */
    protected final String path;

    private final String listSeparator;

    private final boolean useCache;

    private final Map<String, String> cachedEntries;

    protected AbstractLocalStorage(String path) throws FileNotFoundException {
        if (!new File(path).exists()) {
            throw new FileNotFoundException("Cannot find localStorage file in given path: " + path);
        }
        this.path = path;
        this.listSeparator = getString(LIST_SEPARATOR, METADATA_CATEGORY);
        this.useCache = getBoolean(USE_CACHE, METADATA_CATEGORY);
        cachedEntries = Collections.synchronizedMap(new HashMap<>());
    }

    protected AbstractLocalStorage(String path, String listSeparator, boolean useCache, boolean overwrite) throws IOException {
        if (new File(path).exists()) {
            if (!overwrite) {
                throw new FileAlreadyExistsException("File " + path + " already exists. Requested local storage creation with no overwrite");
            } else {
                new File(path).delete();
            }
        }
        this.path = path;
        this.listSeparator = listSeparator;
        this.useCache = useCache;
        cachedEntries = Collections.synchronizedMap(new HashMap<>());
    }

    protected void setMetadata() throws IOException {
        setString(LOCAL_STORAGE_VERSION, CURRENT_VERSION, METADATA_CATEGORY);
        setLong(CREATION_DATE, System.currentTimeMillis(), METADATA_CATEGORY);
        setString(LIST_SEPARATOR, listSeparator, METADATA_CATEGORY);
        setBoolean(USE_CACHE, useCache, METADATA_CATEGORY);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getLocalStorageVersion() {
        return getString(LOCAL_STORAGE_VERSION, METADATA_CATEGORY);
    }

    @Override
    public Date getCreationDate() {
        Long date = getLong(CREATION_DATE, METADATA_CATEGORY);
        return date != null ? new Date(date) : null;
    }

    @Override
    public String getListSeparator() {
        return listSeparator;
    }

    @Override
    public boolean isUseCache() {
        return useCache;
    }

    @Override
    public int itemCount() {
        return itemCountAux() - keys(METADATA_CATEGORY).size();
    }

    protected abstract int itemCountAux();

    public boolean containsItem(String name, String... categories) {
        return useCache ? cachedEntries.containsKey(generateCacheKey(name, categories)) : containsKey(name, categories);
    }

    protected abstract boolean containsKey(String name, String... categories);

    public void removeItem(String name, String... categories) throws IOException {
        String cacheKey = generateCacheKey(name, categories);
        if (useCache && cachedEntries.containsKey(cacheKey)) {
            cachedEntries.remove(cacheKey);
            removeItemAux(name, categories);
        } else {
            removeItemAux(name, categories);
        }
//        setString(name, null, categories);
    }

    protected abstract void removeItemAux(String name, String... categories) throws IOException;

    public final void clear() {
        cachedEntries.clear();
        clearFile();
    }

    protected abstract void clearFile();

    private <E> E loadCache(String name, E value) {
        if (useCache) {
            cachedEntries.put(name, value != null ? value.toString() : null);
        }
        return value;
    }

    protected abstract String getStoredValue(String name, String[] categories);

    protected abstract void writeValue(String name, String value, String... categories) throws IOException;

    private boolean setAux(String name, String value, String... categories) throws IOException {
        String cacheKey = generateCacheKey(name, categories);
        if (!useCache ||
                !cachedEntries.containsKey(cacheKey) ||
                (cachedEntries.containsKey(cacheKey) && !Objects.equals(value, cachedEntries.get(cacheKey)))) {
            writeValue(name, value, categories);
            loadCache(cacheKey, value);
            return true;
        } else {
            return false;
        }
    }

    public String getString(String name, String... categories) {
        String key = generateCacheKey(name, categories);
        if (cachedEntries.containsKey(key)) {
            return cachedEntries.get(key);
        } else {
            return loadCache(key, getStoredValue(name, categories));
        }
    }

    public boolean setString(String name, String value, String... categories) throws IOException {
        return setAux(name, value, categories);
    }

    public Boolean getBoolean(String name, String... categories) {
        String key = generateCacheKey(name, categories);
        if (cachedEntries.containsKey(key)) {
            return Boolean.parseBoolean(cachedEntries.get(key));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(key, value);
            return value != null ? Boolean.parseBoolean(value) : null;
        }
    }

    public boolean setBoolean(String name, Boolean value, String... categories) throws IOException {
        return setAux(name, value != null ? value.toString() : null, categories);
    }

    public Byte getByte(String name, String... categories) {
        String key = generateCacheKey(name, categories);
        if (cachedEntries.containsKey(key)) {
            return Byte.parseByte(cachedEntries.get(key));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(key, value);
            return value != null ? Byte.parseByte(value) : null;
        }
    }

    public boolean setByte(String name, Byte value, String... categories) throws IOException {
        return setAux(name, value != null ? value.toString() : null, categories);
    }

    public Short getShort(String name, String... categories) {
        String key = generateCacheKey(name, categories);
        if (cachedEntries.containsKey(key)) {
            return Short.parseShort(cachedEntries.get(key));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(key, value);
            return value != null ? Short.parseShort(value) : null;
        }
    }

    public boolean setShort(String name, Short value, String... categories) throws IOException {
        return setAux(name, value != null ? value.toString() : null, categories);
    }

    public Integer getInteger(String name, String... categories) {
        String key = generateCacheKey(name, categories);
        if (cachedEntries.containsKey(key)) {
            return Integer.parseInt(cachedEntries.get(key));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(key, value);
            return value != null ? Integer.parseInt(value) : null;
        }
    }

    public boolean setInteger(String name, Integer value, String... categories) throws IOException {
        return setAux(name, value != null ? value.toString() : null, categories);
    }

    public Long getLong(String name, String... categories) {
        String key = generateCacheKey(name, categories);
        if (cachedEntries.containsKey(key)) {
            return Long.parseLong(cachedEntries.get(key));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(key, value);
            return value != null ? Long.parseLong(value) : null;
        }
    }

    public boolean setLong(String name, Long value, String... categories) throws IOException {
        return setAux(name, value != null ? value.toString() : null, categories);
    }

    public Float getFloat(String name, String... categories) {
        String key = generateCacheKey(name, categories);
        if (cachedEntries.containsKey(key)) {
            return Float.parseFloat(cachedEntries.get(key));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(key, value);
            return value != null ? Float.parseFloat(value) : null;
        }
    }

    public boolean setFloat(String name, Float value, String... categories) throws IOException {
        return setAux(name, value != null ? value.toString() : null, categories);
    }

    public Double getDouble(String name, String... categories) {
        String key = generateCacheKey(name, categories);
        if (cachedEntries.containsKey(key)) {
            return Double.parseDouble(cachedEntries.get(key));
        } else {
            String value = getStoredValue(name, categories);
            loadCache(key, value);
            return value != null ? Double.parseDouble(value) : null;
        }
    }

    public boolean setDouble(String name, Double value, String... categories) throws IOException {
        return setAux(name, value != null ? value.toString() : null, categories);
    }

    public Date getDate(String name, String... categories) {
        Long value = getLong(name, categories);
        return value != null ? new Date(value) : null;
    }

    public boolean setDate(String name, Date value, String... categories) throws IOException {
        return setLong(name, value != null ? value.getTime() : null, categories);
    }

    public <E> E getEnum(String name, Class<E> enum_, String... categories) {
        try {
            String str = getString(name, categories);
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
        try {
            Method getName = enum_.getMethod("name");
            return setString(name, (String) getName.invoke(value), categories);
        } catch (Exception e) {
            // cannot happen
            throw new RuntimeException("Fatal error in the implementation of local storage: method 'name' not found in " + enum_.getClass().toString() + ". Cause: " + e.getMessage());
        }
    }

    public List<String> getStringList(String name, String... categories) {
        return deserializeList(getString(name, categories));
    }

    public void setStringList(String name, List<String> list, String... categories) throws IOException {
        setList(name, list, categories);
    }

    public List<Boolean> getBooleanList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Boolean::parseBoolean).collect(Collectors.toList()) : null;
    }

    public void setBooleanList(String name, List<Boolean> list, String... categories) throws IOException {
        setList(name, list, categories);
    }

    public List<Byte> getByteList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Byte::parseByte).collect(Collectors.toList()) : null;
    }

    public void setByteList(String name, List<Byte> list, String... categories) throws IOException {
        setList(name, list, categories);
    }

    public List<Short> getShortList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Short::parseShort).collect(Collectors.toList()) : null;
    }

    public void setShortList(String name, List<Short> list, String... categories) throws IOException {
        setList(name, list, categories);
    }

    public List<Integer> getIntegerList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Integer::parseInt).collect(Collectors.toList()) : null;
    }

    public void setIntegerList(String name, List<Integer> list, String... categories) throws IOException {
        setList(name, list, categories);
    }

    public List<Long> getLongList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Long::parseLong).collect(Collectors.toList()) : null;
    }

    public void setLongList(String name, List<Long> list, String... categories) throws IOException {
        setList(name, list, categories);
    }

    public List<Float> getFloatList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Float::parseFloat).collect(Collectors.toList()) : null;
    }

    public void setFloatList(String name, List<Float> list, String... categories) throws IOException {
        setList(name, list, categories);
    }

    public List<Double> getDoubleList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(Double::parseDouble).collect(Collectors.toList()) : null;
    }

    public void setDoubleList(String name, List<Double> list, String... categories) throws IOException {
        setList(name, list, categories);
    }

    public List<Date> getDateList(String name, String... categories) {
        List<String> stringList = getStringList(name, categories);
        return stringList != null ? stringList.stream().map(s -> new Date(Long.parseLong(s))).collect(Collectors.toList()) : null;
    }

    public void setDateList(String name, List<Date> list, String... categories) throws IOException {
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

    public <E> void setEnumList(String name, Class<E> enum_, List<E> list, String... categories) throws IOException {
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

    private void setList(String name, List<?> list, String... categories) throws IOException {
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

    private String generateCacheKey(String name, String... categories) {
        List<String> keys = new ArrayList<>(Arrays.asList(categories));
        keys.add(name);
        return StringListKey.toString(keys, null);
    }
}
