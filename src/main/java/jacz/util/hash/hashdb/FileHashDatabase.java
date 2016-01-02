package jacz.util.hash.hashdb;

import jacz.util.files.FileUtil;
import jacz.util.hash.HashFunction;
import jacz.util.hash.SHA_256;
import jacz.util.io.object_serialization.*;
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
 * A map store for files, where each file is indexed by its hash code. This store allows managing a set of files (either in the same directory
 * or in different directories), by its hash value. Files can be added, accessed or removed (as a normal map interface).
 * <p/>
 * The FileHashDatabase can be configured with a default directory. This way, we can add the content of files as byte arrays, and
 * the code handles its serialization into a file in the default directory.
 * <p/>
 * The class can also be configured with a maximum size. This way, oldest accessed files are erased if the total size of the managed files
 * exceeds a certain value.
 * <p/>
 */
public class FileHashDatabase implements VersionedObject {

    protected static class AnnotatedFile implements Serializable {

        final String path;

        protected AnnotatedFile(String path) {
            this.path = path;
        }
    }

    protected static class AnnotatedFolder implements Serializable {

        final String path;

        final List<String> fileNames;

        protected AnnotatedFolder(String path, List<String> fileNames) {
            this.path = path;
            this.fileNames = fileNames;
        }
    }

    protected static class FileKeyGenerator implements AutoKeyMap.KeyGenerator<String, AnnotatedFile, IOException>, Serializable {

        @Override
        public String generateKey(AnnotatedFile value) throws IOException {
            if (!FileUtil.isFile(value.path)) {
                throw new FileNotFoundException();
            }
            File file = new File(value.path);
            return getHash(file);
        }
    }

    protected static class FolderKeyGenerator implements AutoKeyMap.KeyGenerator<String, AnnotatedFolder, IOException>, Serializable {

        @Override
        public String generateKey(AnnotatedFolder value) throws IOException {
            if (!FileUtil.isDirectory(value.path)) {
                throw new FileNotFoundException();
            }
            HashFunction totalHash = new SHA_256();
            for (String fileName : value.fileNames) {
                File file = new File(value.path, fileName);
                if (!file.isFile()) {
                    throw new FileNotFoundException();
                }
                String fileHash = getHash(file);
                totalHash.update(fileHash);
            }
            return totalHash.digestAsHex();
        }
    }

    private static final String VERSION_0_1 = "VERSION_0.1";

    private static final String CURRENT_VERSION = VERSION_0_1;

    protected AutoKeyMap<String, AnnotatedFile, IOException> filesMap;

    protected AutoKeyMap<String, AnnotatedFolder, IOException> foldersMap;

    public FileHashDatabase() {
        this(new FileKeyGenerator(), new FolderKeyGenerator());
    }

    public FileHashDatabase(String path, String... backupPaths) throws VersionedSerializationException, IOException {
        VersionedObjectSerializer.deserialize(this, path, backupPaths);
    }

    protected FileHashDatabase(FileKeyGenerator fileKeyGenerator, FolderKeyGenerator folderKeyGenerator) {
        filesMap = new AutoKeyMap<>(fileKeyGenerator);
        foldersMap = new AutoKeyMap<>(folderKeyGenerator);
    }

    public static String getHash(File file) throws IOException {
        return new SHA_256().digestAsHex(file);
    }

    public boolean containsKey(String key) {
        return filesMap.containsKey(key) || foldersMap.containsKey(key);
    }

    public boolean containsValue(String path) throws IOException {
        return filesMap.containsValue(new AnnotatedFile(path));
    }

    public boolean containsValue(String folderPath, List<String> fileNames) throws IOException {
        return foldersMap.containsValue(new AnnotatedFolder(folderPath, fileNames));
    }

    public Map<String, String> performFileAnalysis(boolean deep) {
        Map<String, String> wrongEntries = new HashMap<>();
        for (Map.Entry<String, AnnotatedFile> entry : filesMap.entrySet()) {
            // check that the file exists in the specified path (in all cases)
            if (!FileUtil.isFile(entry.getValue().path)) {
                wrongEntries.put(entry.getKey(), entry.getValue().path);
                continue;
            }
            if (deep) {
                // also check that the file exists, and that the hash is correct
                File file = new File(entry.getValue().path);
                try {
                    if (!entry.getKey().equals(getHash(file))) {
                        wrongEntries.put(entry.getKey(), entry.getValue().path);
                    }
                } catch (IOException e) {
                    wrongEntries.put(entry.getKey(), entry.getValue().path);
                }
            }
        }
        return wrongEntries;
    }

    public String getFilePath(String key) {
        if (filesMap.containsKey(key)) {
            return filesMap.get(key).path;
        } else {
            return null;
        }
    }

    public File getFile(String key) throws FileNotFoundException {
        String path = getFilePath(key);
        if (path == null || !FileUtil.isFile(path)) {
            throw new FileNotFoundException();
        } else {
            return new File(path);
        }
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

    public String put(String path) throws IOException {
        return filesMap.put(new AnnotatedFile(path));
    }

    public String put(String folderPath, List<String> fileNames) throws IOException {
        return foldersMap.put(new AnnotatedFolder(folderPath, fileNames));
    }

    public String remove(String key) {
        if (filesMap.containsKey(key)) {
            AnnotatedFile annotatedFile = filesMap.remove(key);
            return annotatedFile != null ? annotatedFile.path : null;
        }
        if (foldersMap.containsKey(key)) {
            AnnotatedFolder annotatedFolder = foldersMap.remove(key);
            return annotatedFolder != null ? annotatedFolder.path : null;
        }
        return null;
    }

    public String removeValue(String path) throws IOException {
        return filesMap.removeValue(new AnnotatedFile(path));
    }

    public String removeValue(String folderPath, List<String> fileNames) throws IOException {
        return foldersMap.removeValue(new AnnotatedFolder(folderPath, fileNames));
    }

    @Override
    public VersionStack getCurrentVersion() {
        return new VersionStack(CURRENT_VERSION);
    }

    @Override
    public Map<String, Serializable> serialize() {
        Map<String, Serializable> map = new HashMap<>();
        map.put("filesMap", filesMap);
        map.put("foldersMap", foldersMap);
        return map;
    }

    @Override
    public void deserialize(String version, Map<String, Object> attributes, VersionStack parentVersions) throws UnrecognizedVersionException {
        if (version.equals(CURRENT_VERSION)) {
            filesMap = (AutoKeyMap<String, AnnotatedFile, IOException>) attributes.get("filesMap");
            foldersMap = (AutoKeyMap<String, AnnotatedFolder, IOException>) attributes.get("foldersMap");
        } else {
            throw new UnrecognizedVersionException();
        }
    }
}
