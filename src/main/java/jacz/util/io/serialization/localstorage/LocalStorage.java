package jacz.util.io.serialization.localstorage;

import jacz.storage.ActiveJDBCController;
import org.javalite.activejdbc.Base;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A local storage implementation backed by SQLite 3 databases. Data access is performed via the ActiveJDBC orm
 * <p/>
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


    private static final String METADATA_TABLE = "metadata";

    private static final String ITEMS_TABLE = "items";

    private static final TableField ID = new TableField("id", "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT");

    private static final TableField VERSION = new TableField("version", "TEXT");

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

    public LocalStorage(String path) throws IOException {
        this.path = path;
        stringItems = new HashMap<>();
        booleanItems = new HashMap<>();
        byteItems = new HashMap<>();
        shortItems = new HashMap<>();
        integerItems = new HashMap<>();
        longItems = new HashMap<>();
        floatItems = new HashMap<>();
        doubleItems = new HashMap<>();
        dateItems = new HashMap<>();
    }

    public static LocalStorage createNew(String path) throws IOException {
        ActiveJDBCController.connect(path);
        Base.exec("DROP TABLE IF EXISTS " + METADATA_TABLE);
        Base.exec("DROP TABLE IF EXISTS " + ITEMS_TABLE);

        StringBuilder create = new StringBuilder("CREATE TABLE ").append(METADATA_TABLE).append("(");
        appendField(create, ID, false);
        appendField(create, VERSION, false);
        appendField(create, CREATION_DATE, true);
        Base.exec(create.toString());

        create = new StringBuilder("CREATE TABLE ").append(ITEMS_TABLE).append("(");
        appendField(create, NAME, false);
        appendField(create, STRING_ITEM, false);
        appendField(create, INTEGER_ITEM, false);
        appendField(create, REAL_ITEM, true);
        Base.exec(create.toString());

        Metadata Metadata = new Metadata();
        Metadata.setString(VERSION.name, CURRENT_VERSION);
        Metadata.setLong(CREATION_DATE.name, new Date().getTime());
        Metadata.saveIt();

        ActiveJDBCController.disconnect(path);
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
        ActiveJDBCController.connect(path);
        try {
            return (Metadata) Metadata.findAll().get(0);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    private Item getItem(String name, boolean create) {
        ActiveJDBCController.connect(path);
        try {
            Item item = Item.findFirst(NAME.name + " = ?", name);
            if (item == null && create) {
                item = new Item();
                item.setString(NAME.name, name);
                item.insert();
            }
            return item;
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public String getVersion() {
        return getMetadata().getString(VERSION.name);
    }

    public Date getCreationDate() {
        Long date = getMetadata().getLong(CREATION_DATE.name);
        return date != null ? new Date(date) : null;
    }

    public int itemCount() {
        ActiveJDBCController.connect(path);
        try {
            return Item.count().intValue();
        } finally {
            ActiveJDBCController.disconnect(path);
        }

    }

    public boolean containsItem(String name) {
        return getItem(name, false) != null;
    }

    public void removeItem(String name) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                item.delete();
            }
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    private <E> E loadCache(Map<String, E> cache, String name, E value) {
        cache.put(name, value);
        return value;
    }

    public String getString(String name) {
        if (stringItems.containsKey(name)) {
            return stringItems.get(name);
        } else {
            ActiveJDBCController.connect(path);
            try {
                Item item = getItem(name, false);
                return loadCache(stringItems, name, item != null ? item.getString(STRING_ITEM.name) : null);
            } finally {
                ActiveJDBCController.disconnect(path);
            }
        }
    }

    public void setString(String name, String value) {
        loadCache(stringItems, name, value);
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setString(STRING_ITEM.name, value);
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Boolean getBoolean(String name) {
        if (booleanItems.containsKey(name)) {
            return booleanItems.get(name);
        } else {
            ActiveJDBCController.connect(path);
            try {
                Item item = getItem(name, false);
                return loadCache(booleanItems, name, item != null ? item.getBoolean(INTEGER_ITEM.name) : null);
            } finally {
                ActiveJDBCController.disconnect(path);
            }
        }
    }

    public void setBoolean(String name, Boolean value) {
        loadCache(booleanItems, name, value);
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setBoolean(INTEGER_ITEM.name, value);
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Byte getByte(String name) {
        if (byteItems.containsKey(name)) {
            return byteItems.get(name);
        } else {
            ActiveJDBCController.connect(path);
            try {
                Item item = getItem(name, false);
                return loadCache(byteItems, name, item != null ? item.getInteger(INTEGER_ITEM.name).byteValue() : null);
            } finally {
                ActiveJDBCController.disconnect(path);
            }
        }
    }

    public void setByte(String name, Byte value) {
        loadCache(byteItems, name, value);
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setInteger(INTEGER_ITEM.name, value);
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Short getShort(String name) {
        if (shortItems.containsKey(name)) {
            return shortItems.get(name);
        } else {
            ActiveJDBCController.connect(path);
            try {
                Item item = getItem(name, false);
                return loadCache(shortItems, name, item != null ? item.getShort(INTEGER_ITEM.name) : null);
            } finally {
                ActiveJDBCController.disconnect(path);
            }
        }
    }

    public void setShort(String name, Short value) {
        loadCache(shortItems, name, value);
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setShort(INTEGER_ITEM.name, value);
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Integer getInteger(String name) {
        if (integerItems.containsKey(name)) {
            return integerItems.get(name);
        } else {
            ActiveJDBCController.connect(path);
            try {
                Item item = getItem(name, false);
                return loadCache(integerItems, name, item != null ? item.getInteger(INTEGER_ITEM.name) : null);
            } finally {
                ActiveJDBCController.disconnect(path);
            }
        }
    }

    public void setInteger(String name, Integer value) {
        loadCache(integerItems, name, value);
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setInteger(INTEGER_ITEM.name, value);
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Long getLong(String name) {
        if (longItems.containsKey(name)) {
            return longItems.get(name);
        } else {
            ActiveJDBCController.connect(path);
            try {
                Item item = getItem(name, false);
                return loadCache(longItems, name, item != null ? item.getLong(INTEGER_ITEM.name) : null);
            } finally {
                ActiveJDBCController.disconnect(path);
            }
        }
    }

    public void setLong(String name, Long value) {
        loadCache(longItems, name, value);
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setLong(INTEGER_ITEM.name, value);
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Float getFloat(String name) {
        if (floatItems.containsKey(name)) {
            return floatItems.get(name);
        } else {
            ActiveJDBCController.connect(path);
            try {
                Item item = getItem(name, false);
                return loadCache(floatItems, name, item != null ? item.getFloat(REAL_ITEM.name) : null);
            } finally {
                ActiveJDBCController.disconnect(path);
            }
        }
    }

    public void setFloat(String name, Float value) {
        loadCache(floatItems, name, value);
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setFloat(REAL_ITEM.name, value);
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Double getDouble(String name) {
        if (doubleItems.containsKey(name)) {
            return doubleItems.get(name);
        } else {
            ActiveJDBCController.connect(path);
            try {
                Item item = getItem(name, false);
                return loadCache(doubleItems, name, item != null ? item.getDouble(REAL_ITEM.name) : null);
            } finally {
                ActiveJDBCController.disconnect(path);
            }
        }
    }

    public void setDouble(String name, Double value) {
        loadCache(doubleItems, name, value);
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setDouble(REAL_ITEM.name, value);
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Date getDate(String name) {
        if (dateItems.containsKey(name)) {
            return dateItems.get(name);
        } else {
            ActiveJDBCController.connect(path);
            try {
                Item item = getItem(name, false);
                if (item != null) {
                    Long date = item.getLong(INTEGER_ITEM.name);
                    return loadCache(dateItems, name, date != null ? new Date(date) : null);
                } else {
                    return null;
                }
            } finally {
                ActiveJDBCController.disconnect(path);
            }
        }
    }

    public void setDate(String name, Date value) {
        loadCache(dateItems, name, value);
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setLong(INTEGER_ITEM.name, value);
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public <E> E getEnum(String name, Class<E> enum_) throws IOException {
        ActiveJDBCController.connect(path);
        try {
            String str = getString(name);
            if (str != null) {
                Method valueOf = enum_.getMethod("valueOf", String.class);
                return (E) valueOf.invoke(null, str);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new IOException("Cannot retrieve enum for " + enum_);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public <E> void setEnum(String name, Class<E> enum_, E value) throws IOException {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            Method getName = enum_.getMethod("name");
            item.setString(STRING_ITEM.name, getName.invoke(value));
            saveItem(item);
        } catch (Exception e) {
            throw new IOException("Cannot set enum for " + enum_);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public List<String> getStringList(String name) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, false);
            return item != null ? deserializeList(item.getString(STRING_ITEM.name)) : null;
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setStringList(String name, List<String> list) {
        setList(name, list);
    }

    public List<Boolean> getBooleanList(String name) {
        ActiveJDBCController.connect(path);
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
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setBooleanList(String name, List<Boolean> list) {
        setList(name, list);
    }

    public List<Byte> getByteList(String name) {
        ActiveJDBCController.connect(path);
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
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setByteList(String name, List<Byte> list) {
        setList(name, list);
    }

    public List<Short> getShortList(String name) {
        ActiveJDBCController.connect(path);
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
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setShortList(String name, List<Short> list) {
        setList(name, list);
    }

    public List<Integer> getIntegerList(String name) {
        ActiveJDBCController.connect(path);
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
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setIntegerList(String name, List<Integer> list) {
        setList(name, list);
    }

    public List<Long> getLongList(String name) {
        ActiveJDBCController.connect(path);
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
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setLongList(String name, List<Long> list) {
        setList(name, list);
    }

    public List<Float> getFloatList(String name) {
        ActiveJDBCController.connect(path);
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
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setFloatList(String name, List<Float> list) {
        setList(name, list);
    }

    public List<Double> getDoubleList(String name) {
        ActiveJDBCController.connect(path);
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
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setDoubleList(String name, List<Double> list) {
        setList(name, list);
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

    public void setDateList(String name, List<Date> list) {
        List<Long> longList = new ArrayList<>();
        for (Date date : list) {
            longList.add(date.getTime());
        }
        setLongList(name, longList);
    }

    public <E> List<E> getEnumList(String name, Class<E> enum_) throws IOException {
        ActiveJDBCController.connect(path);
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
            throw new IOException("Cannot retrieve enum list for " + enum_);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public <E> void setEnumList(String name, Class<E> enum_, List<E> list) throws IOException {
        ActiveJDBCController.connect(path);
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
            throw new IOException("Cannot set enum list for " + enum_);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    private void saveItem(Item item) {
        item.saveIt();
    }

    private void setList(String name, List<?> list) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setString(STRING_ITEM.name, serializeList(list));
            saveItem(item);
        } finally {
            ActiveJDBCController.disconnect(path);
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
}
