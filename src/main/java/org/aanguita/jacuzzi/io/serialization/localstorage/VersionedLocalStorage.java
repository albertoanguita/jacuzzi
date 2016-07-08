package org.aanguita.jacuzzi.io.serialization.localstorage;

import java.io.IOException;

/**
 * A local storage with a built-in function for storing a user-given version of the stored data. An interface is
 * provided for facilitating updates to the stored data
 */
public class VersionedLocalStorage extends LocalStorage {

    public static final String VERSION_KEY = "@@@version@@@";

    public VersionedLocalStorage(String path) throws IOException {
        super(path);
    }

    public VersionedLocalStorage(String path, Updater updater, String currentVersion) {
        super(path);
        String storedVersion = getVersion();
        while (!currentVersion.equals(storedVersion)) {
            storedVersion = updater.update(this, storedVersion);
        }
        updateVersion(currentVersion);
    }

    public static VersionedLocalStorage createNew(String path, String version) throws IOException {
        LocalStorage.createNew(path);
        VersionedLocalStorage vls = new VersionedLocalStorage(path);
        vls.updateVersion(version);
        return vls;
    }

    public String getVersion() {
        return getString(VERSION_KEY);
    }

    public void updateVersion(String version) {
        setString(VERSION_KEY, version);
    }

    @Override
    public void clear() {
        String version = getVersion();
        super.clear();
        // reset the version
        updateVersion(version);
    }
}
