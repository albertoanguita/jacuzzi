package org.aanguita.jacuzzi.io.localstorage;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Alberto on 17/12/2016.
 */
public class PropertiesLocalStorage extends StringKeyLocalStorage {

    private static final String NULL_VALUE = "###@@@NULL@@@###";

    private Properties properties;

    public PropertiesLocalStorage(String path) throws IOException {
        super(path);
//        getProperties().load(getClass().getClassLoader().getResourceAsStream(path));
        getProperties().load(new FileInputStream(path));
    }

    protected PropertiesLocalStorage(String path, String categorySeparator, String listSeparator, boolean overwrite) throws IOException {
        super(path, categorySeparator, listSeparator, false, overwrite);
        new File(path).createNewFile();
    }

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    @Override
    public int itemCountAux() {
        return getProperties().size();
    }

    @Override
    public Set<String> keys(String... categories) {
        String preKey = generateStringKey("", categories);
        return getProperties().stringPropertyNames().stream()
                .filter(name -> name.startsWith(preKey))
                .map(fullKey -> fullKey.substring(preKey.length()))
                .filter(key -> !key.contains(getCategorySeparator()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> categories(String... categories) {
        String preKey = generateStringKey("", categories);
        return getProperties().stringPropertyNames().stream()
                .filter(name -> name.startsWith(preKey))
                .map(fullKey -> fullKey.substring(preKey.length()))
                .filter(key -> key.contains(getCategorySeparator()))
                .map(key -> key.substring(0, key.indexOf(getCategorySeparator())))
                .collect(Collectors.toSet());
    }

    @Override
    protected boolean containsKey(String key) {
        return getProperties().getProperty(key) != null;
    }

    @Override
    protected void removeItemAux(String key) throws IOException {
        if (getProperties().containsKey(key)) {
            getProperties().remove(key);
            try (OutputStream output = new FileOutputStream(getPath())) {
                properties.store(output, "Properties local storage");
            }
        }
    }

    @Override
    protected String getStoredValue(String key) {
        String value = getProperties().getProperty(key);
        return value == null || value.equals(NULL_VALUE) ? null : value;
    }

    @Override
    protected void writeValue(String key, String value) throws IOException {
        value = value != null ? value : NULL_VALUE;
        if (!getProperties().containsKey(key) || !Objects.equals(getProperties().getProperty(key), value)) {
            getProperties().setProperty(key, value);
            try (OutputStream output = new FileOutputStream(getPath())) {
                getProperties().store(output, "Properties local storage");
            }
        }
    }
}
