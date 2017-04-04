package org.aanguita.jacuzzi.io.localstorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * Created by Alberto on 14/12/2016.
 */
public abstract class StringKeyLocalStorage extends AbstractLocalStorage {

    private static final String CATEGORY_SEPARATOR = "categorySeparator";

    private static final String DEFAULT_CATEGORY_SEPARATOR = ".";

    private final String categorySeparator;

    public StringKeyLocalStorage(String path) throws FileNotFoundException {
        super(path);
        this.categorySeparator = getString(CATEGORY_SEPARATOR, METADATA_CATEGORY) != null ? getString(CATEGORY_SEPARATOR, METADATA_CATEGORY) : DEFAULT_CATEGORY_SEPARATOR;
    }

    protected StringKeyLocalStorage(String path, String categorySeparator, String listSeparator, boolean useCache, boolean overwrite) throws IOException {
        super(path, listSeparator, useCache, overwrite);
        this.categorySeparator = categorySeparator;
    }

    @Override
    protected void setMetadata() throws IOException {
        super.setMetadata();
        setString(CATEGORY_SEPARATOR, categorySeparator, METADATA_CATEGORY);
    }

    public String getCategorySeparator() {
        return categorySeparator;
    }

    @Override
    protected boolean containsKey(String name, String... categories) {
        return containsKey(generateStringKey(name, categories));
    }

    protected abstract boolean containsKey(String key);

    @Override
    protected void removeItemAux(String name, String... categories) throws IOException {
        removeItemAux(generateStringKey(name, categories));
    }

    protected abstract void removeItemAux(String key) throws IOException;

    @Override
    protected String getStoredValue(String name, String[] categories) {
        return getStoredValue(generateStringKey(name, categories));
    }

    protected abstract String getStoredValue(String key);

    protected void writeValue(String name, String value, String... categories) throws IOException {
        writeValue(generateStringKey(name, categories), value);
    }

    protected abstract void writeValue(String key, String value) throws IOException;

    protected String generateStringKey(String name, String... categories) {
        StringBuilder catBuilder = new StringBuilder();
        Stream.of(categories).forEach(cat -> catBuilder.append(cat).append(categorySeparator));
        return catBuilder.append(name).toString();
    }
}
