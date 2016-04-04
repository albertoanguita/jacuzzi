package jacz.util.hash.hashdb;

import jacz.util.files.FileGenerator;
import jacz.util.hash.SHA_256;
import jacz.util.io.serialization.Serializer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Alberto
 * Date: 19/08/14
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public class FileHashDatabaseWithMaxSize {

    private static class AnnotatedPath implements Comparable<AnnotatedPath>, Serializable {

        final String path;

        final long date;

        final String key;

        /**
         * Size in bytes of the file
         */
        final long size;

        private AnnotatedPath(String path, long date, String key, long size) {
            this.path = path;
            this.date = date;
            this.key = key;
            this.size = size;
        }

        @Override
        public int compareTo(AnnotatedPath o) {
            if (date < o.date) {
                return -1;
            } else if (date > o.date) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AnnotatedPath)) return false;

            AnnotatedPath that = (AnnotatedPath) o;

            return key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        byte[] serialize() {
            return Serializer.addArrays(Serializer.serialize(key), Serializer.serialize(path), Serializer.serialize(date), Serializer.serialize(size));
        }
    }

    /**
     * Table linking hashes of files with paths to the files
     */
    private Map<String, AnnotatedPath> data;

    /**
     * Ordered queue of paths, that allows finding the oldest path when size exceeds the max allowed
     */
    private PriorityQueue<AnnotatedPath> orderedPaths;

    /**
     * Max size in bytes that we want the files of this FileHashDatabase to occupy. The code will try to maintain the total size
     * under this value (null if not used)
     */
    private final Long maxSize;

    /**
     * The current size occupied by all files included in this FileHashDatabase. Allows evaluating if we exceeded the max size
     */
    private long currentSize;

    public FileHashDatabaseWithMaxSize() {
        this(null);
    }


    public FileHashDatabaseWithMaxSize(Long maxSize) {
        data = new HashMap<>();
        orderedPaths = new PriorityQueue<>();
        this.maxSize = maxSize;
        currentSize = 0;
    }

    private static String getHash(File file) throws IOException {
        return new SHA_256().digestAsHex(file);
    }

    public Long getMaxSize() {
        return maxSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public Map<String, String> swallowAnalysis() {
        return performFileAnalysis(false);
    }

    public Map<String, String> deepAnalysis() {
        return performFileAnalysis(true);
    }

    private Map<String, String> performFileAnalysis(boolean deep) {
        Map<String, String> wrongEntries = new HashMap<>();
        for (AnnotatedPath annotatedPath : data.values()) {
            // check that the file exists in the specified path (in all cases)
            if (!new File(annotatedPath.path).isFile()) {
                wrongEntries.put(annotatedPath.key, annotatedPath.path);
                continue;
            }
            if (deep) {
                // also check that the file exists, and that the hash is correct
                File file = new File(annotatedPath.path);
                try {
                    if (!annotatedPath.key.equals(getHash(file))) {
                        wrongEntries.put(annotatedPath.key, annotatedPath.path);
                    }
                } catch (IOException e) {
                    wrongEntries.put(annotatedPath.key, annotatedPath.path);
                }
            }
        }
        return wrongEntries;
    }

    public boolean containsValue(String path) throws IOException {
        if (!new File(path).isFile()) {
            throw new FileNotFoundException();
        }
        File file = new File(path);
        String key = getHash(file);
        return containsKey(key);
    }

    public String getPath(String key) {
        if (data.containsKey(key)) {
            return data.get(key).path;
        } else {
            return null;
        }
    }

    public File getFile(String key) throws FileNotFoundException {
        String path = getPath(key);
        if (path == null) {
            return null;
        } else {
            if (!new File(path).isFile()) {
                throw new FileNotFoundException();
            }
            return new File(path);
        }
    }

    public String put(String path) throws IOException {
        return put(path, false);
    }

    public String putDeleteIfExists(String path) throws IOException {
        return put(path, true);
    }

    private String put(String path, boolean deleteIfExists) throws IOException {
        if (!new File(path).isFile()) {
            throw new FileNotFoundException();
        }
        File file = new File(path);
        String key = getHash(file);
        if (!containsKey(key)) {
            long size = new File(path).length();
            AnnotatedPath annotatedPath = new AnnotatedPath(path, System.currentTimeMillis(), key, size);
            data.put(key, annotatedPath);
            orderedPaths.add(annotatedPath);
            currentSize += size;
        } else if (deleteIfExists) {
            FileUtils.forceDelete(new File(path));
        }
        adjustTotalSize();
        return key;
    }

    public void remove(String path) throws IOException {
        if (!new File(path).isFile()) {
            throw new FileNotFoundException();
        }
        File file = new File(path);
        String key = getHash(file);
        if (data.containsKey(key)) {
            AnnotatedPath annotatedPathToRemove = data.get(key);
            data.remove(key);
            orderedPaths.remove(annotatedPathToRemove);
            currentSize -= annotatedPathToRemove.size;
        }
    }

    private void adjustTotalSize() {
        // deletes files if the current size exceeds the max allowed size
        if (maxSize != null) {
            while (currentSize > maxSize && !data.isEmpty()) {
                // delete the oldest file
                AnnotatedPath oldestPath = orderedPaths.poll();
                data.remove(oldestPath.key);
                currentSize -= oldestPath.size;
            }
        }
    }
}
