package org.aanguita.jacuzzi.lists;

import org.aanguita.jacuzzi.numeric.range.IntegerRange;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An array which is internally stored by a list of arrays, to avoid copying each time it is extended. When its values
 * are requested, a copy of the desired range is created. This is useful only when we want to use an array that we
 * believe will grow in maxSize frequently
 */
public class FragmentedArray<T> {

    /**
     * The fragmented arrays themselves. Together, they form the "virtual array"
     */
    private ArrayList<T[]> arrays;

    /**
     * Positions in the "virtual array" that each independent array stores. Can contain negative indexes, just meaning
     * that some time it was stored to the left, although in the "virtual array" indexes will always be >= 0
     */
    private ArrayList<IntegerRange> indexes;

    public FragmentedArray(T[] initialArray) {
        arrays = new ArrayList<>(0);
        indexes = new ArrayList<>(0);
        add(initialArray);
    }

    public void add(T[] array) {
        arrays.add(array);
        Integer oldMax;
        if (size() > 0) {
            oldMax = indexes.get(indexes.size() - 1).getMax();
        } else {
            oldMax = -1;
        }
        indexes.add(new IntegerRange(oldMax + 1, oldMax + array.length));
    }

    @SafeVarargs
    public final void addArray(T... array) {
        add(array);
    }

    public void addLeft(T[] array) {
        arrays.add(0, array);
        Integer oldMin;
        if (size() > 0) {
            oldMin = indexes.get(0).getMin();
        } else {
            oldMin = 0;
        }
        indexes.add(0, new IntegerRange(oldMin - array.length, oldMin - 1));
    }

    @SafeVarargs
    public final void addArrayLeft(T... array) {
        addLeft(array);
    }

    public int size() {
        if (indexes.size() == 0) {
            return 0;
        } else {
            return indexes.get(indexes.size() - 1).getMax() - indexes.get(0).getMin() + 1;
        }
    }

    public T[] getArray() {
        return getArray(0, size());
    }

    public T[] getArray(int offset, int length) throws ArrayIndexOutOfBoundsException {
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("offset cannot be lesser than 0, found " + offset);
        }
        if (length < 0) {
            throw new ArrayIndexOutOfBoundsException("length cannot be lesser than 0, found " + offset);
        }
        if (offset + length > size()) {
            throw new ArrayIndexOutOfBoundsException("array limit exceeded, offset " + offset + ", length " + length + ", array length: " + size());
        }
        if (length == 0) {
            return Arrays.copyOf(arrays.get(0), 0);
        }
//        Arrays.copyOf(baseArray, 0);
        T[] result;
        // we have at least one array (maxSize != 0) and length is not 0
        // move the offset to match out internal indexes
        // from, inclusive
        int from = indexes.get(0).getMin() + offset;
        // to, exclusive
        int to = from + length;
        // find the first array to copy
        int i = 0;
        // search first array to use
        while (indexes.get(i).getMax() < from) {
            i++;
        }
        // if we only need to copy the one array, use copyOfRange method
        if (to <= indexes.get(i).getMax() + 1) {
            return Arrays.copyOfRange(arrays.get(i), from - indexes.get(i).getMin(), to - indexes.get(i).getMin());
        } else {
            // otherwise, we create an array of the needed length, using baseArray
            result = Arrays.copyOf(arrays.get(0), length);
            // copy the first array
            System.arraycopy(arrays.get(i), from - indexes.get(i).getMin(), result, 0, arrays.get(i).length - (from - indexes.get(i).getMin()));
            //result = Arrays.copyOfRange(arrays.get(i), from - indexes.get(i).getMin(), arrays.get(i).length - 1);
            int sizeCopied = arrays.get(i).length - (from - indexes.get(i).getMin());
            // copy middle arrays
            i++;
            while (to > indexes.get(i).getMax() + 1) {
                System.arraycopy(arrays.get(i), 0, result, sizeCopied, arrays.get(i).length);
                sizeCopied += arrays.get(i).length;
                i++;
            }
            // copy final array
            System.arraycopy(arrays.get(i), 0, result, sizeCopied, to - indexes.get(i).getMin());

            return result;
        }
    }
}
