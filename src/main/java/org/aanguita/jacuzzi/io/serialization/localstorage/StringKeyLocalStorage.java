package org.aanguita.jacuzzi.io.serialization.localstorage;

/**
 * Created by Alberto on 14/12/2016.
 */
public abstract class StringKeyLocalStorage extends AbstractLocalStorage {

    protected StringKeyLocalStorage(String path, String categorySeparator, String listSeparator, boolean createNew) {
        super(path, categorySeparator, listSeparator, createNew);
    }

    @Override
    protected String getStoredValue(String name, String[] categories) {
        return getStoredValue(generateStringKey(name, categories));
    }

    protected abstract String getStoredValue(String name);
}
