package org.aanguita.jacuzzi.hash.hashdb;

import org.aanguita.jacuzzi.hash.HashFunction;
import org.aanguita.jacuzzi.io.serialization.localstorage.LocalStorage;
import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A file hash database with embedded and transparent local storage
 *
 * todo check
 */
public class FileHashDatabaseLS {

    private static final String VERSION_0_1 = "VERSION_0.1";

    private static final String CURRENT_VERSION = VERSION_0_1;

    private static final String HASH_FUNCTION_ALGORITHM_KEY = "@@@hashFunctionAlgorithm@@@";

    private static final String HASH_FUNCTION_LENGTH_KEY = "@@@hashFunctionLength@@@";

    private static final String STORE_ABSOLUTE_PATHS_KEY = "@@@absolutePaths@@@";

    private static final String HASH_CATEGORY = "@@@hash@@@";

    private final FileHashDatabase fileHashDatabase;

    private final LocalStorage localStorage;

//    public FileHashDatabaseLS(String localStoragePath) throws IOException {
//        fileHashDatabase = new FileHashDatabase();
//        localStorage = loadLocalStorage(localStoragePath);
//    }
//
//    public FileHashDatabaseLS(String localStoragePath, boolean storeAbsolutePaths) throws IOException {
//        fileHashDatabase = new FileHashDatabase(storeAbsolutePaths);
//        localStorage = loadLocalStorage(localStoragePath);
//    }

    public FileHashDatabaseLS(LocalStorage localStorage, HashFunction hashFunction, boolean storeAbsolutePaths) throws IOException {
        fileHashDatabase = new FileHashDatabase(hashFunction, storeAbsolutePaths);
        this.localStorage = localStorage;
    }

    public FileHashDatabaseLS(LocalStorage localStorage) throws IOException {
        this.localStorage = localStorage;
        HashFunction hashFunction;
        boolean storeAbsolutePaths;
        Map<String, FileHashDatabase.AnnotatedFile> filesMap = new HashMap<>();
        try {
            hashFunction = new HashFunction(localStorage.getString(HASH_FUNCTION_ALGORITHM_KEY), localStorage.getInteger(HASH_FUNCTION_LENGTH_KEY));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid algorithm for hash function: " + localStorage.getString(HASH_FUNCTION_ALGORITHM_KEY));
        }
        storeAbsolutePaths = localStorage.getBoolean(STORE_ABSOLUTE_PATHS_KEY);
        for (String key : localStorage.keys(HASH_CATEGORY)) {
            // this is a file hash -> extract the hash and the path and load
            filesMap.put(key, new FileHashDatabase.AnnotatedFile(localStorage.getString(key, HASH_CATEGORY), storeAbsolutePaths));
        }
        fileHashDatabase = new FileHashDatabase(hashFunction, storeAbsolutePaths);
        fileHashDatabase.addEntries(filesMap);
    }

//    private LocalStorage loadLocalStorage(String localStoragePath) throws IOException {
//        if (Files.exists(Paths.get(localStoragePath))) {
//            LocalStorage localStorage = LocalStorageFactory.openPropertiesLocalStorage(localStoragePath);
//            try {
//                hashFunction = new HashFunction(localStorage.getString(HASH_FUNCTION_ALGORITHM_KEY), localStorage.getInteger(HASH_FUNCTION_LENGTH_KEY));
//            } catch (NoSuchAlgorithmException e) {
//                throw new RuntimeException("Invalid algorithm for hash function: " + localStorage.getString(HASH_FUNCTION_ALGORITHM_KEY));
//            }
//            storeAbsolutePaths = localStorage.getBoolean(STORE_ABSOLUTE_PATHS_KEY);
//            for (String key : localStorage.keys(HASH_CATEGORY)) {
//                // this is a file hash -> extract the hash and the path and load
//                filesMap.put(key, new AnnotatedFile(localStorage.getString(key, HASH_CATEGORY), storeAbsolutePaths));
//            }
//            return localStorage;
//        } else {
//            LocalStorage localStorage = LocalStorageFactory.createPropertiesLocalStorage(localStoragePath, ".", ",", false);
//            localStorage.setString(HASH_FUNCTION_ALGORITHM_KEY, fileHashDatabase.getHashFunction().getAlgorithm());
//            localStorage.setInteger(HASH_FUNCTION_LENGTH_KEY, fileHashDatabase.getHashFunction().getHashLength());
//            localStorage.setBoolean(STORE_ABSOLUTE_PATHS_KEY, fileHashDatabase.isStoreAbsolutePaths());
//            return localStorage;
//        }
//    }

    public LocalStorage getLocalStorage() {
        return localStorage;
    }

    public List<String> getRepairedFiles() {
        return fileHashDatabase.getRepairedFiles();
    }

    public void clear() throws IOException {
        fileHashDatabase.clear();
        String hashAlgorithm = localStorage.getString(HASH_FUNCTION_ALGORITHM_KEY);
        Integer hashLength = localStorage.getInteger(HASH_FUNCTION_LENGTH_KEY);
        boolean storeAbsolutePaths = localStorage.getBoolean(STORE_ABSOLUTE_PATHS_KEY);
        localStorage.clear();
        localStorage.setString(HASH_FUNCTION_ALGORITHM_KEY, hashAlgorithm);
        localStorage.setInteger(HASH_FUNCTION_LENGTH_KEY, hashLength);
        localStorage.setBoolean(STORE_ABSOLUTE_PATHS_KEY, storeAbsolutePaths);
    }

    public int size() {
        return fileHashDatabase.size();
    }

    public boolean containsKey(String key) {
        return fileHashDatabase.containsKey(key);
    }

    public boolean containsPath(String path) throws IOException {
        return fileHashDatabase.containsPath(path);
    }

    public Duple<Boolean, String> containsSimilarFile(String path) throws IOException {
        return fileHashDatabase.containsSimilarFile(path);
    }

    public String getFilePath(String key) {
        return fileHashDatabase.getFilePath(key);
    }

    public File getFile(String key) throws FileNotFoundException {
        return fileHashDatabase.getFile(key);
    }

    public String put(String path) throws IOException {
        String hash = fileHashDatabase.put(path);
        localStorage.setString(hash, fileHashDatabase.getFilePath(hash), HASH_CATEGORY);
        return hash;
    }

    public String remove(String key) throws IOException {
        String path = fileHashDatabase.remove(key);
        localStorage.removeItem(key, HASH_CATEGORY);
        return path;
    }

    public String removeValue(String path) throws IOException {
        String hash = fileHashDatabase.removeValue(path);
        localStorage.removeItem(hash, HASH_CATEGORY);
        return hash;
    }
}
