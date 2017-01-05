package org.aanguita.jacuzzi.io.serialization.localstorage;

import java.io.IOException;

/**
 * Created by Alberto on 05/12/2016.
 */
public class LocalStorageFactory {

    public static LocalStorage createDBLocalStorage(String path, String categorySeparator, String listSeparator, boolean useCache, boolean overwrite) throws IOException {
        DBLocalStorage localStorage = new DBLocalStorage(path, categorySeparator, listSeparator, useCache, overwrite);
        localStorage.setMetadata();
        return localStorage;
    }

    public static LocalStorage openDBLocalStorage(String path) throws IOException {
        return new DBLocalStorage(path);
    }

    public static ReadOnlyLocalStorage createReadOnlyDBLocalStorage(String path, String categorySeparator, String listSeparator, boolean useCache, boolean overwrite) throws IOException {
        return createDBLocalStorage(path, categorySeparator, listSeparator, useCache, overwrite);
    }

    public static ReadOnlyLocalStorage openReadOnlyDBLocalStorage(String path) throws IOException {
        return openDBLocalStorage(path);
    }

    public static LocalStorage createPropertiesLocalStorage(String path, String categorySeparator, String listSeparator, boolean overwrite) throws IOException {
        PropertiesLocalStorage localStorage = new PropertiesLocalStorage(path, categorySeparator, listSeparator, overwrite);
        localStorage.setMetadata();
        return localStorage;
    }

    public static LocalStorage openPropertiesLocalStorage(String path) throws IOException {
        return new PropertiesLocalStorage(path);
    }

    public static ReadOnlyLocalStorage createReadOnlyPropertiesLocalStorage(String path, String categorySeparator, String listSeparator, boolean overwrite) throws IOException {
        return createPropertiesLocalStorage(path, categorySeparator, listSeparator, overwrite);
    }

    public static ReadOnlyLocalStorage openReadOnlyPropertiesLocalStorage(String path) throws IOException {
        return openPropertiesLocalStorage(path);
    }
}
