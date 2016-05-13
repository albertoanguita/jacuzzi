package jacz.util.hash.hashdb;

import jacz.util.hash.HashFunction;
import jacz.util.io.serialization.VersionedSerializationException;
import jacz.util.io.serialization.localstorage.Updater;
import jacz.util.io.serialization.localstorage.VersionedLocalStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A file hash database with embedded and transparent local storage
 */
public class FileHashDatabaseLS extends FileHashDatabase implements Updater {

    private static final String VERSION_0_1 = "VERSION_0.1";

    private static final String CURRENT_VERSION = VERSION_0_1;

    private final VersionedLocalStorage localStorage;

    public FileHashDatabaseLS(String localStoragePath) throws VersionedSerializationException, IOException {
        super();
        localStorage = loadLocalStorage(localStoragePath);
    }

    public FileHashDatabaseLS(String localStoragePath, boolean storeAbsolutePaths) throws VersionedSerializationException, IOException {
        super(storeAbsolutePaths);
        localStorage = loadLocalStorage(localStoragePath);
    }

    public FileHashDatabaseLS(String localStoragePath, HashFunction hashFunction, boolean storeAbsolutePaths) throws VersionedSerializationException, IOException {
        super(hashFunction, storeAbsolutePaths);
        localStorage = loadLocalStorage(localStoragePath);
    }

    private FileHashDatabaseLS(String path, String... more) throws VersionedSerializationException, IOException {
        throw new RuntimeException("Illegal constructor");
    }

    private VersionedLocalStorage loadLocalStorage(String localStoragePath) throws IOException {
        if (Files.exists(Paths.get(localStoragePath))) {
            VersionedLocalStorage localStorage = new VersionedLocalStorage(localStoragePath, this, CURRENT_VERSION);
            for (String key : localStorage.keys()) {
                if (!key.equals(VersionedLocalStorage.VERSION_KEY)) {
                    // this is a file hash
                    filesMap.put(new AnnotatedFile(localStorage.getString(key), storeAbsolutePaths));
                }
            }
            return localStorage;
        } else {
            return VersionedLocalStorage.createNew(localStoragePath, CURRENT_VERSION);
        }
    }

    @Override
    public void clear() {
        super.clear();
        localStorage.clear();
    }

    @Override
    public String put(String path) throws IOException {
        String hash = super.put(path);
        localStorage.setString(hash, getFilePath(hash));
        return hash;
    }


    @Override
    public String remove(String key) {
        String path = super.remove(key);
        localStorage.removeItem(key);
        return path;
    }

    @Override
    public String removeValue(String path) throws IOException {
        String hash = super.removeValue(path);
        localStorage.removeItem(hash);
        return hash;
    }

    @Override
    public String update(VersionedLocalStorage versionedLocalStorage, String storedVersion) {
        throw new RuntimeException();
    }
}
