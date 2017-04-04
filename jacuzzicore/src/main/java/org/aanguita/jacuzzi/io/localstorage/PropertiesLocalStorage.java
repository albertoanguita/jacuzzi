package org.aanguita.jacuzzi.io.localstorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Alberto on 17/12/2016.
 */
public class PropertiesLocalStorage extends StringKeyLocalStorage {

    private static final String NULL_VALUE = "###@@@NULL@@@###";

    private final Properties properties;

    public PropertiesLocalStorage(String path) throws IOException {
        super(path);
        properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream(path));
    }

    protected PropertiesLocalStorage(String path, String categorySeparator, String listSeparator, boolean overwrite) throws IOException {
        super(path, categorySeparator, listSeparator, false, overwrite);
        new File(path).createNewFile();
        properties = new Properties();
    }

    @Override
    public int itemCountAux() {
        return properties.size();
    }

    @Override
    public Set<String> keys(String... categories) {
        String preKey = generateStringKey("", categories);
        return properties.stringPropertyNames().stream()
                .filter(name -> name.startsWith(preKey))
                .map(fullKey -> fullKey.substring(preKey.length()))
                .filter(key -> !key.contains(getCategorySeparator()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> categories(String... categories) {
        String preKey = generateStringKey("", categories);
        return properties.stringPropertyNames().stream()
                .filter(name -> name.startsWith(preKey))
                .map(fullKey -> fullKey.substring(preKey.length()))
                .filter(key -> key.contains(getCategorySeparator()))
                .map(key -> key.substring(0, key.indexOf(getCategorySeparator())))
                .collect(Collectors.toSet());
    }

    @Override
    protected boolean containsKey(String key) {
        return properties.getProperty(key) != null;
    }

    @Override
    protected void removeItemAux(String key) throws IOException {
        if (properties.containsKey(key)) {
            properties.remove(key);
            try (OutputStream output = new FileOutputStream(getPath())) {
                properties.store(output, "Properties local storage");
            }
        }
    }

    @Override
    protected String getStoredValue(String key) {
        String value = properties.getProperty(key);
        return value.equals(NULL_VALUE) ? null : value;
    }

    @Override
    protected void writeValue(String key, String value) throws IOException {
        value = value != null ? value : NULL_VALUE;
        if (!properties.containsKey(key) || !Objects.equals(properties.getProperty(key), value)) {
            properties.setProperty(key, value);
            try (OutputStream output = new FileOutputStream(getPath())) {
                properties.store(output, "Properties local storage");
            }
        }
    }
}
