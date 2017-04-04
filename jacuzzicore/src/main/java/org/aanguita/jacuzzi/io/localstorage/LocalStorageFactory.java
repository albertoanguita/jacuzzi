package org.aanguita.jacuzzi.io.localstorage;

import java.io.IOException;

/**
 * Created by Alberto on 05/12/2016.
 */
public class LocalStorageFactory {

    public static LocalStorage createPropertiesLocalStorage(String path, boolean overwrite) throws IOException {
        return createPropertiesLocalStorage(path, null, null, overwrite);
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
