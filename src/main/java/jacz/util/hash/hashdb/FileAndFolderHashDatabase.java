package jacz.util.hash.hashdb;

import jacz.util.hash.HashFunction;
import jacz.util.hash.SHA_256;
import jacz.util.io.serialization.UnrecognizedVersionException;
import jacz.util.io.serialization.VersionStack;
import jacz.util.lists.tuple.Duple;
import jacz.util.maps.AutoKeyMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alberto on 13/05/2016.
 */
public class FileAndFolderHashDatabase extends FileHashDatabase {

    protected static class AnnotatedFolder implements Serializable {

        final String path;

        final List<String> fileNames;

        protected AnnotatedFolder(String path, List<String> fileNames, boolean storeAbsolutePaths) {
            this.path = storeAbsolutePaths ? new File(path).getAbsolutePath() : path;
            this.fileNames = fileNames;
        }
    }

    protected static class FolderKeyGenerator implements AutoKeyMap.KeyGenerator<String, AnnotatedFolder, IOException>, Serializable {

        private transient HashFunction hashFunction;

        public FolderKeyGenerator(HashFunction hashFunction) {
            this.hashFunction = hashFunction;
        }

        public void setHashFunction(HashFunction hashFunction) {
            this.hashFunction = hashFunction;
        }

        @Override
        public String generateKey(AnnotatedFolder value) throws IOException {
            if (!new File(value.path).isDirectory()) {
                throw new FileNotFoundException();
            }
            HashFunction totalHash = new SHA_256();
            for (String fileName : value.fileNames) {
                File file = new File(value.path, fileName);
                if (!file.isFile()) {
                    throw new FileNotFoundException();
                }
                String fileHash = hashFunction.digestAsHex(file);
                totalHash.update(fileHash);
            }
            return totalHash.digestAsHex();
        }
    }

    private static final String VERSION_0_1 = "VERSION_0.1";

    private static final String CURRENT_VERSION = VERSION_0_1;

    protected AutoKeyMap<String, AnnotatedFolder, IOException> foldersMap;

    public FileAndFolderHashDatabase(boolean storeAbsolutePaths) {
        this(defaultHashFunction(), storeAbsolutePaths);
    }

    public FileAndFolderHashDatabase(HashFunction hashFunction, boolean storeAbsolutePaths) {
        this(hashFunction, new FileKeyGenerator(hashFunction), new FolderKeyGenerator(hashFunction), storeAbsolutePaths);
    }

//    public FileAndFolderHashDatabase(String path, String... backupPaths) throws VersionedSerializationException, IOException {
//        VersionedObjectSerializer.deserialize(this, path, backupPaths);
//    }

//    protected FileAndFolderHashDatabase(HashFunction hashFunction, FileKeyGenerator fileKeyGenerator, boolean storeAbsolutePaths) {
//        this.hashFunction = hashFunction;
//        filesMap = new AutoKeyMap<>(fileKeyGenerator);
////        foldersMap = new AutoKeyMap<>(folderKeyGenerator);
//        this.storeAbsolutePaths = storeAbsolutePaths;
//    }

    protected FileAndFolderHashDatabase(HashFunction hashFunction, FileKeyGenerator fileKeyGenerator, FolderKeyGenerator folderKeyGenerator, boolean storeAbsolutePaths) {
        super(hashFunction, fileKeyGenerator, storeAbsolutePaths);
        foldersMap = new AutoKeyMap<>(folderKeyGenerator);
    }

    public void clear() {
        super.clear();
        foldersMap.clear();
    }

    public boolean containsKey(String key) {
        return super.containsKey(key) || foldersMap.containsKey(key);
    }

    public boolean containsValue(String folderPath, List<String> fileNames) throws IOException {
        return foldersMap.containsValue(new AnnotatedFolder(folderPath, fileNames, storeAbsolutePaths));
    }

    public Duple<String, List<String>> getFolderPaths(String key) {
        if (foldersMap.containsKey(key)) {
            return new Duple<>(foldersMap.get(key).path, foldersMap.get(key).fileNames);
        } else {
            return null;
        }
    }

    public List<File> getFolderFiles(String key) throws FileNotFoundException {
        Duple<String, List<String>> folder = getFolderPaths(key);
        if (folder == null) {
            throw new FileNotFoundException();
        } else {
            List<File> folderFiles = new ArrayList<>();
            for (String fileName : folder.element2) {
                File file = new File(folder.element1, fileName);
                if (!file.isFile()) {
                    throw new FileNotFoundException();
                } else {
                    folderFiles.add(file);
                }
            }
            return folderFiles;
        }
    }

    public String put(String folderPath, List<String> fileNames) throws IOException {
        return foldersMap.put(new AnnotatedFolder(folderPath, fileNames, storeAbsolutePaths));
    }

    public String remove(String key) {
        String path = super.remove(key);
        if (path == null && foldersMap.containsKey(key)) {
            AnnotatedFolder annotatedFolder = foldersMap.remove(key);
            return annotatedFolder != null ? annotatedFolder.path : null;
        }
        return null;
    }

    public String removeValue(String folderPath, List<String> fileNames) throws IOException {
        return foldersMap.removeValue(new AnnotatedFolder(folderPath, fileNames, storeAbsolutePaths));
    }

    @Override
    public VersionStack getCurrentVersion() {
        return new VersionStack(CURRENT_VERSION, super.getCurrentVersion());
    }

    @Override
    public synchronized Map<String, Serializable> serialize() {
        Map<String, Serializable> map = new HashMap<>(super.serialize());
        map.put("foldersMap", foldersMap);
        return map;
    }

    @Override
    public void deserialize(String version, Map<String, Object> attributes, VersionStack parentVersions) throws UnrecognizedVersionException {
        if (version.equals(CURRENT_VERSION)) {
            super.deserialize(parentVersions.retrieveVersion(), attributes, parentVersions);
            foldersMap = (AutoKeyMap<String, AnnotatedFolder, IOException>) attributes.get("foldersMap");
            ((FolderKeyGenerator) (foldersMap.getKeyGenerator())).setHashFunction(hashFunction);
        } else {
            throw new UnrecognizedVersionException();
        }
    }

}
