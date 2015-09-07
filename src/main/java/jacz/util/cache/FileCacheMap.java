package jacz.util.cache;

import jacz.util.files.FileReaderWriter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 6/06/13
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class FileCacheMap<T, S> extends CacheMap<T, S> {

    public static class FileAnnotatedValue<T, S> extends AnnotatedValue<T, S> {

        private String path;

        private FileAnnotatedValue(S value, long date, T key, String tempDirPath) {
            path = tempDirPath + File.separator + date;
            try {
                FileReaderWriter.writeObject(path, (Serializable) value);
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File(path);
            this.size = file.length();
            this.date = date;
            this.key = key;
        }

        public S getValue() {
            try {
                return (S) FileReaderWriter.readObject(path);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private void remove() {
            File file = new File(path);
            file.delete();
        }
    }

    private final String tempDirPath;

    private boolean mustDeleteTempDir;

    public FileCacheMap(int maxCount, long maxSize, String tempDirPath) {
        super(maxCount, maxSize);
        this.tempDirPath = tempDirPath;
        setupTempDir();
    }

    private void setupTempDir() {
        File tempDir = new File(tempDirPath);
        if (tempDir.isDirectory()) {
            // the directory already exists -> no need to delete it after exit
            mustDeleteTempDir = false;
        } else {
            // we create the directory, and delete it after execution completion
            tempDir.mkdirs();
            mustDeleteTempDir = true;
        }
    }


    @Override
    protected AnnotatedValue<T, S> buildAnnotatedValue(T key, S value, long millis) {
        return new FileAnnotatedValue<T, S>(value, millis, key, tempDirPath);
    }

    @Override
    public S remove(T key) {
        FileAnnotatedValue<T, S> removedValue = (FileAnnotatedValue<T, S>) map.get(key);
        S value = super.remove(key);
        removedValue.remove();
        return value;
    }

    public void clear() {
        // clear all cached files, and then (maybe) delete the directory
        Set<T> keySet = new HashSet<T>(map.keySet());
        for (T key : keySet) {
            remove(key);
        }
        if (mustDeleteTempDir) {
            File tempDir = new File(tempDirPath);
            tempDir.delete();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        clear();
    }
}
