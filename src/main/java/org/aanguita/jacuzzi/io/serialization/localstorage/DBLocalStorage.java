package org.aanguita.jacuzzi.io.serialization.localstorage;

import org.aanguita.jacuzzi.io.serialization.activejdbcsupport.ActiveJDBCController;
import org.aanguita.jacuzzi.objects.ObjectMapPool;
import org.javalite.activejdbc.DB;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A local storage implementation backed by SQLite 3 databases. Data access is performed via the ActiveJDBC orm
 * <p>
 * A write-through cache is maintained for all written data, so accessions do not go to the database.
 *
 * todo put IOException upon loading not existing path?
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

    private static final String CATEGORY_SEPARATOR = "@/-CAT-/@";

    private static final String LIST_SEPARATOR = "\n";


    public static final String CURRENT_VERSION = "0.1.0";


    private static final ObjectMapPool<String, Lock> locks = new ObjectMapPool<>(s -> new ReentrantLock());

    public DBLocalStorage(String path) {
        super(path, CATEGORY_SEPARATOR, LIST_SEPARATOR);
    }

    public static DBLocalStorage createNew(String path) throws IOException {
        DB db = ActiveJDBCController.connect(DATABASE, path);
        db.exec("DROP TABLE IF EXISTS " + ITEMS_TABLE);

        StringBuilder create = new StringBuilder("CREATE TABLE ").append(ITEMS_TABLE).append("(");
        // todo add index to name
        appendField(create, NAME, false);
        appendField(create, STRING_ITEM, true);
        db.exec(create.toString());

        ActiveJDBCController.disconnect();
        return new DBLocalStorage(path);
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

    public int itemCount() {
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return Item.count().intValue();
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    public List<String> keys(String... categories) {
        String preKey = generateName("", categories);
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return Item.where(NAME.name + " LIKE ?", preKey + "%").stream()
                    .map(model -> model.getString(NAME.name))
                    .map(fullKey -> fullKey.substring(preKey.length()))
                    .filter(key -> !key.contains(CATEGORY_SEPARATOR))
                    .collect(Collectors.toList());
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    public Set<String> categories(String... categories) {
        String preKey = generateName("", categories);
        ActiveJDBCController.connect(DATABASE, path);
        try {
            return Item.where(NAME.name + " LIKE ?", preKey + "%").stream()
                    .map(model -> model.getString(NAME.name))
                    .map(fullKey -> fullKey.substring(preKey.length()))
                    .filter(key -> key.contains(CATEGORY_SEPARATOR))
                    .map(key -> key.substring(0, key.indexOf(CATEGORY_SEPARATOR)))
                    .collect(Collectors.toSet());
        } finally {
            ActiveJDBCController.disconnect();
        }
    }

    public boolean containsItem(String name, String... categories) {
        name = generateName(name, categories);
        connect(name);
        try {
            return getItem(name, false) != null;
        } finally {
            disconnect(name);
        }
    }

    public void removeItem(String name, String... categories) {
        name = generateName(name, categories);
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

    public void clear() {
        ActiveJDBCController.connect(DATABASE, path);
        try {
            Item.deleteAll();
        } finally {
            ActiveJDBCController.disconnect();
        }
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
        return locks.getObject(path + name);
    }

    @Override
    protected String getStoredValue(String name) {
        try {
            connect(name);
            Item item = getItem(name, false);
            return item.getString(STRING_ITEM.name);
        } finally {
            disconnect(name);
        }
    }

    @Override
    protected void writeValue(String name, Object value) {
        try {
            connect(name);
            Item item = getItem(name, true);
            item.setString(STRING_ITEM.name, value.toString());
            saveItem(item);
        } finally {
            disconnect(name);
        }
    }


    private void saveItem(Item item) {
        item.saveIt();
    }

    private static String generateName(String name, String... categories) {
        StringBuilder catBuilder = new StringBuilder();
        Stream.of(categories).forEach(cat -> catBuilder.append(generateCategory(cat)));
        return catBuilder.append(name).toString();
    }

    private static String generateCategory(String category) {
        return category + CATEGORY_SEPARATOR;
    }
}
