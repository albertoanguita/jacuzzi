package aanguita.jacuzzi.hash.hashdb;

import aanguita.jacuzzi.hash.HashFunction;
import aanguita.jacuzzi.io.serialization.localstorage.LocalStorage;
import aanguita.jacuzzi.io.serialization.localstorage.Updater;
import aanguita.jacuzzi.io.serialization.localstorage.VersionedLocalStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

/**
 * A file hash database with embedded and transparent local storage
 */
public class FileHashDatabaseLS extends FileHashDatabase implements Updater {

    private static final String VERSION_0_1 = "VERSION_0.1";

    private static final String CURRENT_VERSION = VERSION_0_1;

    private static final String HASH_FUNCTION_ALGORITHM_KEY = "@@@hashFunctionAlgorithm@@@";

    private static final String HASH_FUNCTION_LENGTH_KEY = "@@@hashFunctionLength@@@";

    private static final String STORE_ABSOLUTE_PATHS_KEY = "@@@absolutePaths@@@";

    private static final String HASH_CATEGORY = "@@@hash@@@";

    private final VersionedLocalStorage localStorage;

    public FileHashDatabaseLS(String localStoragePath) throws IOException {
        super();
        localStorage = loadLocalStorage(localStoragePath);
    }

    public FileHashDatabaseLS(String localStoragePath, boolean storeAbsolutePaths) throws IOException {
        super(storeAbsolutePaths);
        localStorage = loadLocalStorage(localStoragePath);
    }

    public FileHashDatabaseLS(String localStoragePath, HashFunction hashFunction, boolean storeAbsolutePaths) throws IOException {
        super(hashFunction, storeAbsolutePaths);
        localStorage = loadLocalStorage(localStoragePath);
    }

    private VersionedLocalStorage loadLocalStorage(String localStoragePath) throws IOException {
        if (Files.exists(Paths.get(localStoragePath))) {
            VersionedLocalStorage localStorage = new VersionedLocalStorage(localStoragePath, this, CURRENT_VERSION);
            try {
                hashFunction = new HashFunction(localStorage.getString(HASH_FUNCTION_ALGORITHM_KEY), localStorage.getInteger(HASH_FUNCTION_LENGTH_KEY));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Invalid algorithm for hash function: " + localStorage.getString(HASH_FUNCTION_ALGORITHM_KEY));
            }
            storeAbsolutePaths = localStorage.getBoolean(STORE_ABSOLUTE_PATHS_KEY);
            for (String key : localStorage.keys(HASH_CATEGORY)) {
                // this is a file hash -> extract the hash and the path and load
                filesMap.put(key, new AnnotatedFile(localStorage.getString(HASH_CATEGORY, key), storeAbsolutePaths));
            }
            return localStorage;
        } else {
            VersionedLocalStorage localStorage = VersionedLocalStorage.createNew(localStoragePath, CURRENT_VERSION);
            localStorage.setString(HASH_FUNCTION_ALGORITHM_KEY, hashFunction.getAlgorithm());
            localStorage.setInteger(HASH_FUNCTION_LENGTH_KEY, hashFunction.getHashLength());
            localStorage.setBoolean(STORE_ABSOLUTE_PATHS_KEY, storeAbsolutePaths);
            return localStorage;
        }
    }

    public LocalStorage getLocalStorage() {
        return localStorage;
    }

    @Override
    public void clear() {
        super.clear();
        String hashAlgorithm = localStorage.getString(HASH_FUNCTION_ALGORITHM_KEY);
        Integer hashLength = localStorage.getInteger(HASH_FUNCTION_LENGTH_KEY);
        boolean storeAbsolutePaths = localStorage.getBoolean(STORE_ABSOLUTE_PATHS_KEY);
        localStorage.clear();
        localStorage.setString(HASH_FUNCTION_ALGORITHM_KEY, hashAlgorithm);
        localStorage.setInteger(HASH_FUNCTION_LENGTH_KEY, hashLength);
        localStorage.setBoolean(STORE_ABSOLUTE_PATHS_KEY, storeAbsolutePaths);
    }

    @Override
    public String put(String path) throws IOException {
        String hash = super.put(path);
        localStorage.setString(HASH_CATEGORY, hash, getFilePath(hash));
        return hash;
    }


    @Override
    public String remove(String key) {
        String path = super.remove(key);
        localStorage.removeItem(HASH_CATEGORY, key);
        return path;
    }

    @Override
    public String removeValue(String path) throws IOException {
        String hash = super.removeValue(path);
        localStorage.removeItem(HASH_CATEGORY, hash);
        return hash;
    }

    @Override
    public String update(VersionedLocalStorage versionedLocalStorage, String storedVersion) {
        throw new RuntimeException();
    }
}
