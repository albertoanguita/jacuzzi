package jacz.util.cache;

import jacz.util.files.FileReaderWriter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * A map-based cache which keeps track of the count of added elements and automatically erases the oldest ones (accessed longer ago) when
 * the max allowed number is exceeded.
 */
public class CacheMap<T, S> {


    protected static class AnnotatedValue<T, S> implements Comparable<AnnotatedValue<T, S>> {

        S value;

        long date;

        T key;

        long size;

        protected AnnotatedValue() {
        }

        public AnnotatedValue(S value, long date, T key) {
            this.value = value;
            this.date = date;
            this.key = key;
            long size;
            try {
                size = FileReaderWriter.sizeOfObject((Serializable) value, "." + File.separator + "EutilsWrapper" + File.separator + "temp" + date + ".tmp");
            } catch (IOException e) {
                // ignore
                size = 1000000;
            }
            this.size = size;
        }

        public S getValue() {
            return value;
        }

        public int compareTo(AnnotatedValue<T, S> o) {
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
            if (o == null || getClass() != o.getClass()) return false;

            AnnotatedValue that = (AnnotatedValue) o;

            if (key != null ? !key.equals(that.key) : that.key != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }
    }

    /**
     * Table with the stored elements
     */
    //private final Map<T, AnnotatedValue<T, S>> map;

    protected final Map<T, AnnotatedValue<T, S>> map;

    /**
     * Ordered queue of paths, that allows finding the oldest path when size exceeds the max allowed
     */
    private final PriorityQueue<AnnotatedValue<T, S>> orderedValues;

    /**
     * Max allowed element count
     */
    private final int maxCount;

    /**
     * Max allowed memory size
     */
    private final long maxSize;

    /**
     * Current size in memory of stored objects
     */
    private long size;

    //private Class A;


    public CacheMap(int maxCount, long maxSize) {
        map = new HashMap<T, AnnotatedValue<T, S>>();
        this.maxCount = maxCount;
        this.maxSize = maxSize;
        orderedValues = new PriorityQueue<AnnotatedValue<T, S>>();
        //this(maxCount, maxSize, AnnotatedValue.class);
    }

    /*public <A extends AnnotatedValue<T, S>> CacheMap(int maxCount, long maxSize, Class A) {
        this.A = A;
        A annotatedValue;
        map = new HashMap<T, AnnotatedValue<T, S>>();
        orderedValues = new PriorityQueue<AnnotatedValue<T, S>>();
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }*/

    public void put(T key, S value) {
        /*try {
            A.getConstructors()[0].newInstance(value, System.currentTimeMillis(), key);
        } catch (Exception e) {
            // ignore, cannot happen
        }
        AnnotatedValue<T, S> tsAnnotatedValue = new AnnotatedValue<T, S>(value, System.currentTimeMillis(), key);*/
        /*try {
            map.put(key, A.cast(A.getConstructors()[0].newInstance(value, System.currentTimeMillis(), key)));
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
        AnnotatedValue<T, S> tsAnnotatedValue = buildAnnotatedValue(key, value, System.currentTimeMillis());
        map.put(key, tsAnnotatedValue);
        orderedValues.add(tsAnnotatedValue);
        size += tsAnnotatedValue.size;
        printSize();
        adjustSize();
    }

    protected AnnotatedValue<T, S> buildAnnotatedValue(T key, S value, long millis) {
        return new AnnotatedValue<T, S>(value, millis, key);
    }

    private void adjustSize() {
        while (map.size() > maxCount || size() > maxSize) {
            //System.out.println("One cached element removed");
            AnnotatedValue<T, S> oldestValue = orderedValues.poll();
            remove(oldestValue.key);
            printSize();
        }
        System.gc();
    }

    public S get(T key) {
        AnnotatedValue<T, S> value = map.get(key);
        return value != null ? value.getValue() : null;
    }

    public boolean contains(T key) {
        return map.containsKey(key);
    }

    public S remove(T key) {
        AnnotatedValue<T, S> removedValue = map.remove(key);
        if (removedValue != null) {
            orderedValues.remove(removedValue);
            size -= removedValue.size;
            return removedValue.getValue();
        } else {
            return null;
        }
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int count() {
        return map.size();
    }

    public long size() {
        return size;
    }

    public Set<T> keySet() {
        return map.keySet();
    }

    public Collection<S> values() {
        Collection<S> values = new HashSet<S>();
        for (AnnotatedValue<T, S> annotatedValues : map.values()) {
            values.add(annotatedValues.value);
        }
        return values;
    }

    private void printSize() {
        //System.out.println("Cache size is: " + map.size() + "/" + (size() / (1024*1024)) + "MB");
    }
}
