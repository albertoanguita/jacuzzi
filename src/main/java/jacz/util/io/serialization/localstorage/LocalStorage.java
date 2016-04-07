package jacz.util.io.serialization.localstorage;

import jacz.storage.ActiveJDBCController;
import org.javalite.activejdbc.Base;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A local storage implementation backed by SQLite 3 databases. Data access is performed via the ActiveJDBC orm
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



    public static final String CURRENT_VERSION = "0.1.0";


    /**
     * Path to the local database
     */
    private final String path;

    public LocalStorage(String path) throws IOException {
        this.path = path;
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

        Metadata metadata = (Metadata) Metadata.findAll().get(0);
        metadata.setString(VERSION.name, CURRENT_VERSION);
        metadata.setLong(CREATION_DATE.name, new Date().getTime());

        create = new StringBuilder("CREATE TABLE ").append(ITEMS_TABLE).append("(");
        appendField(create, NAME, false);
        appendField(create, STRING_ITEM, false);
        appendField(create, INTEGER_ITEM, false);
        appendField(create, REAL_ITEM, true);
        Base.exec(create.toString());

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
                Base.openTransaction();
                item = new Item();
                item.setString(NAME.name, name);
                Base.commitTransaction();
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

    public String getString(String name) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, false);
            return item != null ? item.getString(STRING_ITEM.name) : null;
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setString(String name, String value) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setString(STRING_ITEM.name, value);
            clearOtherFieldsAndSave(item, STRING_ITEM);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Short getShort(String name) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, false);
            return item != null ? item.getShort(INTEGER_ITEM.name) : null;
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setShort(String name, Short value) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setShort(INTEGER_ITEM.name, value);
            clearOtherFieldsAndSave(item, INTEGER_ITEM);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Integer getInteger(String name) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, false);
            return item != null ? item.getInteger(INTEGER_ITEM.name) : null;
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setInteger(String name, Integer value) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setInteger(INTEGER_ITEM.name, value);
            clearOtherFieldsAndSave(item, INTEGER_ITEM);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Long getLong(String name) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, false);
            return item != null ? item.getLong(INTEGER_ITEM.name) : null;
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setLong(String name, Long value) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setLong(INTEGER_ITEM.name, value);
            clearOtherFieldsAndSave(item, INTEGER_ITEM);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Float getFloat(String name) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, false);
            return item != null ? item.getFloat(REAL_ITEM.name) : null;
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setFloat(String name, Float value) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setFloat(REAL_ITEM.name, value);
            clearOtherFieldsAndSave(item, REAL_ITEM);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Double getDouble(String name) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, false);
            return item != null ? item.getDouble(REAL_ITEM.name) : null;
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setDouble(String name, Double value) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setDouble(REAL_ITEM.name, value);
            clearOtherFieldsAndSave(item, REAL_ITEM);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public Date getDate(String name) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, false);
            if (item != null) {
                Long date = item.getLong(INTEGER_ITEM.name);
                return date != null ? new Date(date) : null;
            } else {
                return null;
            }
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    public void setDate(String name, Date value) {
        ActiveJDBCController.connect(path);
        try {
            Item item = getItem(name, true);
            item.setLong(INTEGER_ITEM.name, value);
            clearOtherFieldsAndSave(item, INTEGER_ITEM);
        } finally {
            ActiveJDBCController.disconnect(path);
        }
    }

    private void clearOtherFieldsAndSave(Item item, TableField tableField) {
        Set<TableField> otherFields = new HashSet<>();
        if (tableField == STRING_ITEM) {
            otherFields.add(INTEGER_ITEM);
            otherFields.add(REAL_ITEM);
        } else if (tableField == INTEGER_ITEM) {
            otherFields.add(STRING_ITEM);
            otherFields.add(REAL_ITEM);
        } else if (tableField == REAL_ITEM) {
            otherFields.add(INTEGER_ITEM);
            otherFields.add(STRING_ITEM);
        }
        for (TableField otherField : otherFields) {
            item.set(otherField.name, null);
        }
        item.saveIt();
    }
}
