package jacz.util.io.serialization.localstorage;

import jacz.util.concurrency.LockMap;
import jacz.util.io.serialization.activejdbcsupport.ActiveJDBCController;
import jacz.util.objects.Util;
import org.javalite.activejdbc.DB;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * A local storage implementation backed by SQLite 3 databases. Data access is performed via the ActiveJDBC orm
 * <p>
 * A write-through cache is maintained for all written data, so accessions do not go to the database.
 */
public class LocalStorage {

    private static class TableField {

        final String name;

        final String type;

        public TableField(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    // todo update name to match api name
    static final String DATABASE = "jacuzzi_localStorage";

    static final String METADATA_TABLE = DATABASE + "_metadata";

    static final String ITEMS_TABLE = DATABASE + "_items";

    private static final TableField ID = new TableField("id", "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT");

    private static final TableField LS_VERSION = new TableField("ls_version", "TEXT");

    private static final TableField CREATION_DATE = new TableField("creation_date", "INTEGER");

    private static final TableField NAME = new TableField("id", "TEXT NOT NULL PRIMARY KEY");

    private static final TableField STRING_ITEM = new TableField("string_item", "TEXT");

    private static final TableField INTEGER_ITEM = new TableField("integer_item", "INTEGER");

    private static final TableField REAL_ITEM = new TableField("real_item", "REAL");

    private static final String LIST_SEPARATOR = "\n";


    public static final String CURRENT_VERSION = "0.1.0";


    /**
     * Path to the local database
     */
    private final String path;

    private final Map<String, String> stringItems;

    private final Map<String, Boolean> booleanItems;

    private final Map<String, Byte> byteItems;

    private final Map<String, Short> shortItems;

    private final Map<String, Integer> integerItems;

    private final Map<String, Long> longItems;

    private final Map<String, Float> floatItems;

    private final Map<String, Double> doubleItems;

    private final Map<String, Date> dateItems;

    private static final LockMap<String> locks = new LockMap<>();

    public LocalStorage(String path) {
        this.path = path;
        stringItems = Collections.synchronizedMap(new HashMap<>());
        booleanItems = Collections.synchronizedMap(new HashMap<>());
        byteItems = Collections.synchronizedMap(new HashMap<>());
        shortItems = Collections.synchronizedMap(new HashMap<>());
        integerItems = Collections.synchronizedMap(new HashMap<>());
        longItems = Collections.synchronizedMap(new HashMap<>());
        floatItems = Collections.synchronizedMap(new HashMap<>());
        doubleItems = Collections.synchronizedMap(new HashMap<>());
        dateItems = Collections.synchronizedMap(new HashMap<>());
    }

    public static LocalStorage createNew(String path) throws IOException {
        DB db = ActiveJDBCController.connect(DATABASE, path);
        db.exec("DROP TABLE IF EXISTS " + METADATA_TABLE);
        db.exec("DROP TABLE IF EXISTS " + ITEMS_TABLE);

        StringBuilder create = new StringBuilder("CREATE TABLE ").append(METADATA_TABLE).append("(");
        appendField(create, ID, false);
        appendField(create, LS_VERSION, false);
        appendField(create, CREATION_DATE, true);
        db.exec(create.toString());

        create = new StringBuilder("CREATE TABLE ").append(ITEMS_TABLE).append("(");
        appendField(create, NAME, false);
        appendField(create, STRING_ITEM, false);
        appendField(create, INTEGER_ITEM, false);
        appendField(create, REAL_ITEM, true);
        db.exec(create.toString());

        Metadata Metadata = new Metadata();
        Metadata.setString(LS_VERSION.name, CURRENT_VERSION);
        Metadata.setLong(CREATION_DATE.name, new Date().getTime());
        Metadata.saveIt();

        ActiveJDBCController.disconnect();
        return new LocalStorage(path);
    }

    private static void appendField(StringBuilder create, TableField field, boolean isFinal) {
        create.append(field.name).append(" ").append(field.type);
        if (isFinal) {
            create.append(")");
        } else {
            create.append(",");
        }
    }

    private Metadata getMetadata() {
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return (Metadata) Metadata.findAll().get(0);
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    private Item getItem(String name, boolean create) {
        // must be previously connected
        Item item = Item.findFirst(NAME.name + " = ?", name);
        if (item == null && create) {
            item = new Item();
            item.setString(NAME.name, name);
            item.insert();
        }
        return item;
    }

    public String getPath() {
        return path;
    }

    public String getLocalStorageVersion() {
        return getMetadata().getString(LS_VERSION.name);
    }

    public Date getCreationDate() {
        Long date = getMetadata().getLong(CREATION_DATE.name);
        return date != null ? new Date(date) : null;
    }

    public int itemCount() {
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return Item.count().intValue();
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    public List<String> keys() {
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return Item.findAll().stream().map(model -> model.getString(NAME.name)).collect(Collectors.toList());
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    public List<String> keys(String category) {
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return Item.where(ID.name + " LIKE ?", category + "%").stream().map(model -> model.getString(NAME.name)).map(categoryAndName -> extractName(category, categoryAndName)).collect(Collectors.toList());
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    public boolean containsItem(String name) {
        connect(name);
        try {
            return getItem(name, false) != null;
        } finally {
            disconnect(name);
        }
    }

    public boolean containsItem(String category, String name) {
        return containsItem(generateName(category, name));
    }

    public void removeItem(String name) {
        connect(name);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                item.delete();
            }
        } finally {
            disconnect(name);
        }
    }

    public void removeItem(String category, String name) {
        removeItem(generateName(category, name));
    }

    public void clear() {
        ActiveJDBCController.connect(DATABASE, path);
        try {
            Item.deleteAll();
            stringItems.clear();
            booleanItems.clear();
            byteItems.clear();
            shortItems.clear();
            integerItems.clear();
            longItems.clear();
            floatItems.clear();
            doubleItems.clear();
            dateItems.clear();
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    private <E> E loadCache(Map<String, E> cache, String name, E value) {
        cache.put(name, value);
        return value;
    }

    private void connect(String name) {
        ActiveJDBCController.connect(DATABASE, path);
        getLock(name).lock();
    }

    private void disconnect(String name) {
        getLock(name).unlock();
        ActiveJDBCController.disconnect();
    }

    private Lock getLock(String name) {
        return locks.getLock(path + name);
    }

    public String getString(String name) {
        if (stringItems.containsKey(name)) {
            return stringItems.get(name);
        } else {
            connect(name);
            try {
                Item item = getItem(name, false);
                return loadCache(stringItems, name, item != null ? item.getString(STRING_ITEM.name) : null);
            } finally {
                disconnect(name);
            }
        }
    }

    public String getString(String category, String name) {
        return getString(generateName(category, name));
    }

    public boolean setString(String name, String value) {
        String storedValue = getString(name);
        if (value == null || !Util.equals(value, storedValue)) {
            loadCache(stringItems, name, value);
            connect(name);
            try {
                Item item = getItem(name, true);
                item.setString(STRING_ITEM.name, value);
                saveItem(item);
                return true;
            } finally {
                disconnect(name);
            }
        } else {
            return false;
        }
    }

    public boolean setString(String category, String name, String value) {
        return setString(generateName(category, name), value);
    }

    public Boolean getBoolean(String name) {
        if (booleanItems.containsKey(name)) {
            return booleanItems.get(name);
        } else {
            connect(name);
            try {
                Item item = getItem(name, false);
                return loadCache(booleanItems, name, item != null ? item.getBoolean(INTEGER_ITEM.name) : null);
            } finally {
                disconnect(name);
            }
        }
    }

    public Boolean getBoolean(String category, String name) {
        return getBoolean(generateName(category, name));
    }

    public boolean setBoolean(String name, Boolean value) {
        Boolean storedValue = getBoolean(name);
        if (value == null || !Util.equals(value, storedValue)) {
            loadCache(booleanItems, name, value);
            connect(name);
            try {
                Item item = getItem(name, true);
                item.setBoolean(INTEGER_ITEM.name, value);
                saveItem(item);
                return true;
            } finally {
                disconnect(name);
            }
        } else {
            return false;
        }
    }

    public boolean setBoolean(String category, String name, Boolean value) {
        return setBoolean(generateName(category, name), value);
    }

    public Byte getByte(String name) {
        if (byteItems.containsKey(name)) {
            return byteItems.get(name);
        } else {
            connect(name);
            try {
                Item item = getItem(name, false);
                return loadCache(byteItems, name, item != null ? item.getInteger(INTEGER_ITEM.name).byteValue() : null);
            } finally {
                disconnect(name);
            }
        }
    }

    public Byte getByte(String category, String name) {
        return getByte(generateName(category, name));
    }

    public boolean setByte(String name, Byte value) {
        Byte storedValue = getByte(name);
        if (value == null || !Util.equals(value, storedValue)) {
            loadCache(byteItems, name, value);
            connect(name);
            try {
                Item item = getItem(name, true);
                item.setInteger(INTEGER_ITEM.name, value);
                saveItem(item);
                return true;
            } finally {
                disconnect(name);
            }
        } else {
            return false;
        }
    }

    public boolean setByte(String category, String name, Byte value) {
        return setByte(generateName(category, name), value);
    }

    public Short getShort(String name) {
        if (shortItems.containsKey(name)) {
            return shortItems.get(name);
        } else {
            connect(name);
            try {
                Item item = getItem(name, false);
                return loadCache(shortItems, name, item != null ? item.getShort(INTEGER_ITEM.name) : null);
            } finally {
                disconnect(name);
            }
        }
    }

    public Short getShort(String category, String name) {
        return getShort(generateName(category, name));
    }

    public boolean setShort(String name, Short value) {
        Short storedValue = getShort(name);
        if (value == null || !Util.equals(value, storedValue)) {
            loadCache(shortItems, name, value);
            connect(name);
            try {
                Item item = getItem(name, true);
                item.setShort(INTEGER_ITEM.name, value);
                saveItem(item);
                return true;
            } finally {
                disconnect(name);
            }
        } else {
            return false;
        }
    }

    public boolean setShort(String category, String name, Short value) {
        return setShort(generateName(category, name), value);
    }

    public Integer getInteger(String name) {
        if (integerItems.containsKey(name)) {
            return integerItems.get(name);
        } else {
            connect(name);
            try {
                Item item = getItem(name, false);
                return loadCache(integerItems, name, item != null ? item.getInteger(INTEGER_ITEM.name) : null);
            } finally {
                disconnect(name);
            }
        }
    }

    public Integer getInteger(String category, String name) {
        return getInteger(generateName(category, name));
    }

    public boolean setInteger(String name, Integer value) {
        Integer storedValue = getInteger(name);
        if (value == null || !Util.equals(value, storedValue)) {
            loadCache(integerItems, name, value);
            connect(name);
            try {
                Item item = getItem(name, true);
                item.setInteger(INTEGER_ITEM.name, value);
                saveItem(item);
                return true;
            } finally {
                disconnect(name);
            }
        } else {
            return false;
        }
    }

    public boolean setInteger(String category, String name, Integer value) {
        return setInteger(generateName(category, name), value);
    }

    public Long getLong(String name) {
        if (longItems.containsKey(name)) {
            return longItems.get(name);
        } else {
            connect(name);
            try {
                Item item = getItem(name, false);
                return loadCache(longItems, name, item != null ? item.getLong(INTEGER_ITEM.name) : null);
            } finally {
                disconnect(name);
            }
        }
    }

    public Long getLong(String category, String name) {
        return getLong(generateName(category, name));
    }

    public boolean setLong(String name, Long value) {
        Long storedValue = getLong(name);
        if (value == null || !Util.equals(value, storedValue)) {
            loadCache(longItems, name, value);
            connect(name);
            try {
                Item item = getItem(name, true);
                item.setLong(INTEGER_ITEM.name, value);
                saveItem(item);
                return true;
            } finally {
                disconnect(name);
            }
        } else {
            return false;
        }
    }

    public boolean setLong(String category, String name, Long value) {
        return setLong(generateName(category, name), value);
    }

    public Float getFloat(String name) {
        if (floatItems.containsKey(name)) {
            return floatItems.get(name);
        } else {
            connect(name);
            try {
                Item item = getItem(name, false);
                return loadCache(floatItems, name, item != null ? item.getFloat(REAL_ITEM.name) : null);
            } finally {
                disconnect(name);
            }
        }
    }

    public Float getFloat(String category, String name) {
        return getFloat(generateName(category, name));
    }

    public boolean setFloat(String name, Float value) {
        Float storedValue = getFloat(name);
        if (value == null || !Util.equals(value, storedValue)) {
            loadCache(floatItems, name, value);
            connect(name);
            try {
                Item item = getItem(name, true);
                item.setFloat(REAL_ITEM.name, value);
                saveItem(item);
                return true;
            } finally {
                disconnect(name);
            }
        } else {
            return false;
        }
    }

    public boolean setFloat(String category, String name, Float value) {
        return setFloat(generateName(category, name), value);
    }

    public Double getDouble(String name) {
        if (doubleItems.containsKey(name)) {
            return doubleItems.get(name);
        } else {
            connect(name);
            try {
                Item item = getItem(name, false);
                return loadCache(doubleItems, name, item != null ? item.getDouble(REAL_ITEM.name) : null);
            } finally {
                disconnect(name);
            }
        }
    }

    public Double getDouble(String category, String name) {
        return getDouble(generateName(category, name));
    }

    public boolean setDouble(String name, Double value) {
        Double storedValue = getDouble(name);
        if (value == null || !Util.equals(value, storedValue)) {
            loadCache(doubleItems, name, value);
            connect(name);
            try {
                Item item = getItem(name, true);
                item.setDouble(REAL_ITEM.name, value);
                saveItem(item);
                return true;
            } finally {
                disconnect(name);
            }
        } else {
            return false;
        }
    }

    public boolean setDouble(String category, String name, String value) {
        return setString(generateName(category, name), value);
    }

    public Date getDate(String name) {
        if (dateItems.containsKey(name)) {
            return dateItems.get(name);
        } else {
            connect(name);
            try {
                Item item = getItem(name, false);
                if (item != null) {
                    Long date = item.getLong(INTEGER_ITEM.name);
                    return loadCache(dateItems, name, date != null ? new Date(date) : null);
                } else {
                    return null;
                }
            } finally {
                disconnect(name);
            }
        }
    }

    public Date getDate(String category, String name) {
        return getDate(generateName(category, name));
    }

    public boolean setDate(String name, Date value) {
        Date storedValue = getDate(name);
        if (value == null || !Util.equals(value, storedValue)) {
            loadCache(dateItems, name, value);
            connect(name);
            try {
                Item item = getItem(name, true);
                item.setLong(INTEGER_ITEM.name, value);
                saveItem(item);
                return true;
            } finally {
                disconnect(name);
            }
        } else {
            return false;
        }
    }

    public boolean setDate(String category, String name, Date value) {
        return setDate(generateName(category, name), value);
    }

    public <E> E getEnum(String name, Class<E> enum_) {
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
            // todo fatal error
            return null;
        }
    }

    public <E> E getEnum(String category, String name, Class<E> enum_) {
        return getEnum(generateName(category, name), enum_);
    }

    public <E> boolean setEnum(String name, Class<E> enum_, E value) {
        try {
            Method getName = enum_.getMethod("name");
            return setString(name, (String) getName.invoke(value));
        } catch (Exception e) {
            // cannot happen
            // todo fatal error
            return false;
        }
    }

    public <E> boolean setEnum(String category, String name, Class<E> enum_, E value) {
        return setEnum(generateName(category, name), enum_, value);
    }

    public List<String> getStringList(String name) {
        connect(name);
        try {
            Item item = getItem(name, false);
            return item != null ? deserializeList(item.getString(STRING_ITEM.name)) : null;
        } finally {
            disconnect(name);
        }
    }

    public List<String> getStringList(String category, String name) {
        return getStringList(generateName(category, name));
    }

    public void setStringList(String name, List<String> list) {
        setList(name, list);
    }

    public void setStringList(String category, String name, List<String> value) {
        setStringList(generateName(category, name), value);
    }

    public List<Boolean> getBooleanList(String name) {
        connect(name);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                List<Boolean> values = new ArrayList<>();
                for (String str : getStringList(name)) {
                    values.add(Boolean.parseBoolean(str));
                }
                return values;
            } else {
                return null;
            }
        } finally {
            disconnect(name);
        }
    }

    public List<Boolean> getBooleanList(String category, String name) {
        return getBooleanList(generateName(category, name));
    }

    public void setBooleanList(String name, List<Boolean> list) {
        setList(name, list);
    }

    public void setBooleanList(String category, String name, List<Boolean> value) {
        setBooleanList(generateName(category, name), value);
    }

    public List<Byte> getByteList(String name) {
        connect(name);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                List<Byte> values = new ArrayList<>();
                for (String str : getStringList(name)) {
                    values.add(Byte.parseByte(str));
                }
                return values;
            } else {
                return null;
            }
        } finally {
            disconnect(name);
        }
    }

    public List<Byte> getByteList(String category, String name) {
        return getByteList(generateName(category, name));
    }

    public void setByteList(String name, List<Byte> list) {
        setList(name, list);
    }

    public void setByteList(String category, String name, List<Byte> value) {
        setByteList(generateName(category, name), value);
    }

    public List<Short> getShortList(String name) {
        connect(name);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                List<Short> values = new ArrayList<>();
                for (String str : getStringList(name)) {
                    values.add(Short.parseShort(str));
                }
                return values;
            } else {
                return null;
            }
        } finally {
            disconnect(name);
        }
    }

    public List<Short> getShortList(String category, String name) {
        return getShortList(generateName(category, name));
    }

    public void setShortList(String name, List<Short> list) {
        setList(name, list);
    }

    public void setShortList(String category, String name, List<Short> value) {
        setShortList(generateName(category, name), value);
    }

    public List<Integer> getIntegerList(String name) {
        connect(name);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                List<Integer> values = new ArrayList<>();
                for (String str : getStringList(name)) {
                    values.add(Integer.parseInt(str));
                }
                return values;
            } else {
                return null;
            }
        } finally {
            disconnect(name);
        }
    }

    public List<Integer> getIntegerList(String category, String name) {
        return getIntegerList(generateName(category, name));
    }

    public void setIntegerList(String name, List<Integer> list) {
        setList(name, list);
    }

    public void setIntegerList(String category, String name, List<Integer> value) {
        setIntegerList(generateName(category, name), value);
    }

    public List<Long> getLongList(String name) {
        connect(name);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                List<Long> values = new ArrayList<>();
                for (String str : getStringList(name)) {
                    values.add(Long.parseLong(str));
                }
                return values;
            } else {
                return null;
            }
        } finally {
            disconnect(name);
        }
    }

    public List<Long> getLongList(String category, String name) {
        return getLongList(generateName(category, name));
    }

    public void setLongList(String name, List<Long> list) {
        setList(name, list);
    }

    public void setLongList(String category, String name, List<Long> value) {
        setLongList(generateName(category, name), value);
    }

    public List<Float> getFloatList(String name) {
        connect(name);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                List<Float> values = new ArrayList<>();
                for (String str : getStringList(name)) {
                    values.add(Float.parseFloat(str));
                }
                return values;
            } else {
                return null;
            }
        } finally {
            disconnect(name);
        }
    }

    public List<Float> getFloatList(String category, String name) {
        return getFloatList(generateName(category, name));
    }

    public void setFloatList(String name, List<Float> list) {
        setList(name, list);
    }

    public void setFloatList(String category, String name, List<Float> value) {
        setFloatList(generateName(category, name), value);
    }

    public List<Double> getDoubleList(String name) {
        connect(name);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                List<Double> values = new ArrayList<>();
                for (String str : getStringList(name)) {
                    values.add(Double.parseDouble(str));
                }
                return values;
            } else {
                return null;
            }
        } finally {
            disconnect(name);
        }
    }

    public List<Double> getDoubleList(String category, String name) {
        return getDoubleList(generateName(category, name));
    }

    public void setDoubleList(String name, List<Double> list) {
        setList(name, list);
    }

    public void setDoubleList(String category, String name, List<Double> value) {
        setDoubleList(generateName(category, name), value);
    }

    public List<Date> getDateList(String name) {
        List<Long> longList = getLongList(name);
        if (longList != null) {
            List<Date> list = new ArrayList<>();
            for (long value : longList) {
                list.add(new Date(value));
            }
            return list;
        } else {
            return null;
        }
    }

    public List<Date> getDateList(String category, String name) {
        return getDateList(generateName(category, name));
    }

    public void setDateList(String name, List<Date> list) {
        List<Long> longList = new ArrayList<>();
        for (Date date : list) {
            longList.add(date.getTime());
        }
        setLongList(name, longList);
    }

    public void setDateList(String category, String name, List<Date> value) {
        setDateList(generateName(category, name), value);
    }

    public <E> List<E> getEnumList(String name, Class<E> enum_) {
        connect(name);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                Method valueOf = enum_.getMethod("valueOf", String.class);
                List<E> enumValues = new ArrayList<>();
                for (String str : getStringList(name)) {
                    enumValues.add((E) valueOf.invoke(null, str));
                }
                return enumValues;
            } else {
                return null;
            }
        } catch (Exception e) {
            // cannot happen
            // todo fatal error
            return null;
        } finally {
            disconnect(name);
        }
    }

    public <E> List<E> getEnumList(String category, String name, Class<E> enum_) {
        return getEnumList(generateName(category, name), enum_);
    }

    public <E> void setEnumList(String name, Class<E> enum_, List<E> list) {
        connect(name);
        try {
            Item item = getItem(name, true);
            Method getName = enum_.getMethod("name");
            List<String> strList = new ArrayList<>();
            for (E value : list) {
                strList.add((String) getName.invoke(value));
            }
            setString(name, serializeList(strList));
            saveItem(item);
        } catch (Exception e) {
            // cannot happen
            // todo fatal error
        } finally {
            disconnect(name);
        }
    }

    public <E> void setEnumList(String category, String name, Class<E> enum_, List<E> value) {
        setEnumList(generateName(category, name), enum_, value);
    }

    private void saveItem(Item item) {
        item.saveIt();
    }

    private void setList(String name, List<?> list) {
        connect(name);
        try {
            Item item = getItem(name, true);
            item.setString(STRING_ITEM.name, serializeList(list));
            saveItem(item);
        } finally {
            disconnect(name);
        }
    }

    private String serializeList(List<?> list) {
        if (list.isEmpty()) {
            return "";
        } else {
            StringBuilder serList = new StringBuilder(LIST_SEPARATOR);
            for (Object item : list) {
                serList.append(item.toString()).append(LIST_SEPARATOR);
            }
            return serList.toString();
        }
    }

    private List<String> deserializeList(String value) {
        value = value == null ? "" : value;
        StringTokenizer tokenizer = new StringTokenizer(value, LIST_SEPARATOR);
        List<String> list = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        return list;
    }

    private static String generateName(String category, String name) {
        return category + name;
    }

    private static String extractName(String category, String categoryAndName) {
        return categoryAndName.substring(category.length());
    }
}
