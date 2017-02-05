package org.aanguita.jacuzzi.io.serialization.localstorage;

import org.aanguita.jacuzzi.io.activejdbcsupport.ActiveJDBCController;
import org.aanguita.jacuzzi.objects.ObjectMapPool;
import org.javalite.activejdbc.DB;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * A local storage implementation backed by SQLite 3 databases. Data access is performed via the ActiveJDBC orm
 * <p>
 * A write-through cache is maintained for all written data, so accessions do not go to the database.
 */
public class DBLocalStorage extends StringKeyLocalStorage {

    private static class TableField {

        final String name;

        final String type;

        public TableField(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    static final String DATABASE = "jacuzzi_localStorage";

    static final String ITEMS_TABLE = DATABASE + "_items";

    private static final TableField NAME = new TableField("id", "TEXT NOT NULL PRIMARY KEY");

    private static final TableField STRING_ITEM = new TableField("string_item", "TEXT");

    private static final ObjectMapPool<String, Lock> locks = new ObjectMapPool<>(s -> new ReentrantLock());

    DBLocalStorage(String path) throws FileNotFoundException {
        super(path);
    }

    DBLocalStorage(String path, String categorySeparator, String listSeparator, boolean useCache, boolean overwrite) throws IOException {
        super(path, categorySeparator, listSeparator, useCache, overwrite);
        DB db = ActiveJDBCController.connect(DATABASE, path);
        db.exec("DROP TABLE IF EXISTS " + ITEMS_TABLE);

        StringBuilder create = new StringBuilder("CREATE TABLE ").append(ITEMS_TABLE).append("(");
        // todo add index to name
        appendField(create, NAME, false);
        appendField(create, STRING_ITEM, true);
        db.exec(create.toString());

        ActiveJDBCController.disconnect();
    }

    private static void appendField(StringBuilder create, TableField field, boolean isFinal) {
        create.append(field.name).append(" ").append(field.type);
        if (isFinal) {
            create.append(")");
        } else {
            create.append(",");
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

    @Override
    public int itemCountAux() {
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return Item.count().intValue();
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    public Set<String> keys(String... categories) {
        String preKey = generateStringKey("", categories);
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return Item.where(NAME.name + " LIKE ?", preKey + "%").stream()
                    .map(model -> model.getString(NAME.name))
                    .map(fullKey -> fullKey.substring(preKey.length()))
                    .filter(key -> !key.contains(getCategorySeparator()))
                    .collect(Collectors.toSet());
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    public Set<String> categories(String... categories) {
        String preKey = generateStringKey("", categories);
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return Item.where(NAME.name + " LIKE ?", preKey + "%").stream()
                    .map(model -> model.getString(NAME.name))
                    .map(fullKey -> fullKey.substring(preKey.length()))
                    .filter(key -> key.contains(getCategorySeparator()))
                    .map(key -> key.substring(0, key.indexOf(getCategorySeparator())))
                    .collect(Collectors.toSet());
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    @Override
    public void clearFile() {
        ActiveJDBCController.connect(DATABASE, path);
        try {
            // todo keep metadata
            Item.deleteAll();
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    private void connect(String key) {
        ActiveJDBCController.connect(DATABASE, path);
        getLock(key).lock();
    }

    private void disconnect(String key) {
        getLock(key).unlock();
        ActiveJDBCController.disconnect();
    }

    private Lock getLock(String name) {
        return locks.getObject(path + name);
    }

    @Override
    protected boolean containsKey(String key) {
        try {
            connect(key);
            return getItem(key, false) != null;
        } finally {
            disconnect(key);
        }
    }

    @Override
    protected void removeItemAux(String key) {
        try {
            connect(key);
            Item item = getItem(key, false);
            if (item != null) {
                item.delete();
            }
        } finally {
            disconnect(key);
        }
    }

    @Override
    protected String getStoredValue(String key) {
        try {
            connect(key);
            Item item = getItem(key, false);
            return item != null ? item.getString(STRING_ITEM.name) : null;
        } finally {
            disconnect(key);
        }
    }

    @Override
    protected void writeValue(String key, String value) {
        try {
            connect(key);
            Item item = getItem(key, true);
            item.setString(STRING_ITEM.name, value);
            item.saveIt();
        } finally {
            disconnect(key);
        }
    }
}
