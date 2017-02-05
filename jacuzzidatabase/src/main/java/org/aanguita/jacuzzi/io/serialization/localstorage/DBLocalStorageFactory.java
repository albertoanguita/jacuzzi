package org.aanguita.jacuzzi.io.serialization.localstorage;

import java.io.IOException;

/**
 * Created by Alberto on 16/01/2017.
 */
public class DBLocalStorageFactory {

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
}
