package jacz.util.hash.hashdb;

import jacz.util.files.FileUtil;
import jacz.util.hash.HashFunction;
import jacz.util.hash.SHA_256;
import jacz.util.maps.AutoKeyMap;
import jacz.util.maps.ComparableIndexMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * A FileHashDatabase that stores timestamps on added files (does not work with folders). We can retrieve the keys
 * of the newest files.
 * <p/>
 * Does not seem to be finished. DO NOT USE.
 */
public class FileHashDatabaseWithTimestamp extends FileHashDatabase {

    protected static class AnnotatedFileWithTimeStamp extends AnnotatedFile {

        final long timeStamp;

        private AnnotatedFileWithTimeStamp(String path, long timeStamp) {
            super(path);
            this.timeStamp = timeStamp;
        }
    }

    protected static class AnnotatedFolder implements Serializable {

        final String path;

        final List<String> fileNames;

        private AnnotatedFolder(String path, List<String> fileNames) {
            this.path = path;
            this.fileNames = fileNames;
        }
    }

    protected static class FileKeyGeneratorWithTimeStamp extends FileKeyGenerator {

        private ComparableIndexMap<Long, String> timeToKeyFiles;

        public void setTimeToKeyFiles(ComparableIndexMap<Long, String> timeToKeyFiles) {
            this.timeToKeyFiles = timeToKeyFiles;
        }

        @Override
        public String generateKey(AnnotatedFile value) throws IOException {
            String key = super.generateKey(value);
            AnnotatedFileWithTimeStamp annotatedFileWithTimeStamp = (AnnotatedFileWithTimeStamp) value;
            timeToKeyFiles.put(annotatedFileWithTimeStamp.timeStamp, key);
            return key;
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

    private ComparableIndexMap<Long, String> timeToKeyFiles;

    private long nextTimeStamp;

    public FileHashDatabaseWithTimestamp() {
//        FileKeyGeneratorWithTimeStamp fileKeyGeneratorWithTimeStamp = new FileKeyGeneratorWithTimeStamp();
        super(new FileKeyGeneratorWithTimeStamp(), null);
        timeToKeyFiles = new ComparableIndexMap<>();
//        fileKeyGeneratorWithTimeStamp.setTimeToKeyFiles(timeToKeyFiles);
        nextTimeStamp = 0L;
    }

    private long getNextTimeStamp() {
        return nextTimeStamp++;
    }

    public Long newestTimeStamp() {
        return timeToKeyFiles.getKey(timeToKeyFiles.size() - 1);
    }

    public Collection<String> hashesNewerThan(Long timeStamp) {
        return timeToKeyFiles.valuesGreaterThan(timeStamp);
    }

    @Override
    public String put(String path) throws IOException {
        return filesMap.put(new AnnotatedFileWithTimeStamp(path, getNextTimeStamp()));
    }

    @Override
    public void remove(String key) throws IOException {
        if (filesMap.containsKey(key)) {
            AnnotatedFileWithTimeStamp annotatedFileWithTimeStamp = (AnnotatedFileWithTimeStamp) filesMap.get(key);
            timeToKeyFiles.remove(annotatedFileWithTimeStamp.timeStamp);
            super.remove(key);
        }
    }
}
