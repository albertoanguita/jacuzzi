package jacz.util.lists;

import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskFinalizationIndicator;
import jacz.util.numeric.NumericUtil;

import java.util.*;

/**
 * This class contains utility methods for lists. These methods include efficient ordering with or without threading.
 */
public class Lists {

    /**
     * Sorts the list in n*log(n) time. Applies an efficient sorting algorithm (quick sort). No threading is applied.
     *
     * @param list         list to sort, composed by comparable elements. Natural ordering of the elements is used
     * @param reverseOrder whether the list must be sorted in reverse order (false -> no reverse, true -> reverse)
     */
    public static <T extends Comparable<T>> void sort(List<T> list, boolean reverseOrder) {
        try {
            sort(list, new NaturalComparator<T>(), reverseOrder);
        } catch (IndexOutOfBoundsException e) {
            // this cannot happen
            System.exit(1);
        }
    }

    /**
     * Sorts the list in n*log(n) time. Applies an efficient sorting algorithm (quick sort). No threading is applied.
     *
     * @param list         list to sort
     * @param comparator   comparator to use in the sorting
     * @param reverseOrder whether the list must be sorted in reverse order (false -> no reverse, true -> reverse)
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator, boolean reverseOrder) {
        try {
            sort(list, comparator, reverseOrder, 1);
        } catch (IndexOutOfBoundsException e) {
            // this cannot happen
            System.exit(1);
        }
    }

    /**
     * Sorts the list in n*log(n) time. Applies an efficient sorting algorithm (quick sort). No threading is applied.
     * A list of related lists (their elements are related to the elements of list) are ordered in the same way as list
     *
     * @param list         list to sort, composed by comparable elements. Natural ordering of the elements is used
     * @param reverseOrder whether the list must be sorted in reverse order (false -> no reverse, true -> reverse)
     * @param relatedLists lists that follow the same ordering system as list. These lists will be reordered
     *                     according to the reordering of list. They must be all the same maxSize as list.
     * @throws IndexOutOfBoundsException if any of the related lists is of different maxSize than the main list
     */
    public static <T extends Comparable<T>> void sort(List<T> list, boolean reverseOrder, List<?>... relatedLists) throws IndexOutOfBoundsException {
        sort(list, new NaturalComparator<T>(), reverseOrder, relatedLists);
    }

    /**
     * Sorts the list in n*log(n) time. Applies an efficient sorting algorithm (quick sort). No threading is applied.
     * A list of related lists (their elements are related to the elements of list) are ordered in the same way as list
     *
     * @param list         list to sort
     * @param comparator   comparator to use in the sorting
     * @param reverseOrder whether the list must be sorted in reverse order (false -> no reverse, true -> reverse)
     * @param relatedLists lists that follow the same ordering system as list. These lists will be reordered
     *                     according to the reordering of list. They must be all the same maxSize as list.
     * @throws IndexOutOfBoundsException if any of the related lists is of different maxSize than the main list
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator, boolean reverseOrder, List<?>... relatedLists) throws IndexOutOfBoundsException {
        sort(list, comparator, reverseOrder, 1, relatedLists);
    }

    /**
     * Sorts the list in n*log(n) time. Applies an efficient sorting algorithm (quick sort).
     * The number of threads employed by the algorithm can be specified (or even specify to use all available
     * cores in the machine.
     * A list of related lists (their elements are related to the elements of list) are ordered in the same way as list
     * The basics of the algorithm were extracted from its description in Wikipedia
     *
     * @param list         list to sort, composed by comparable elements. Natural ordering of the elements is used
     * @param reverseOrder whether the list must be sorted in reverse order (false -> no reverse, true -> reverse)
     * @param numThreads   number of threads to be employed in the sorting (must be greater than 0)
     * @param relatedLists lists that follow the same ordering system as list. These lists will be reordered
     *                     according to the reordering of list. They must be all the same maxSize as list. a null
     *                     value for this argument indicates no related lists
     * @throws IndexOutOfBoundsException if any of the related lists is of different maxSize than the main list
     */
    public static <T extends Comparable<T>> void sort(List<T> list, boolean reverseOrder, int numThreads, List<?>... relatedLists)
            throws IndexOutOfBoundsException {
        sort(list, new NaturalComparator<T>(), reverseOrder, numThreads, relatedLists);
    }

    /**
     * Sorts the list in n*log(n) time. Applies an efficient sorting algorithm (quick sort).
     * The number of threads employed by the algorithm can be specified (or even specify to use all available
     * cores in the machine.
     * A list of related lists (their elements are related to the elements of list) are ordered in the same way as list
     * The basics of the algorithm were extracted from its description in Wikipedia
     *
     * @param list         list to sort
     * @param comparator   comparator to use in the sorting
     * @param reverseOrder whether the list must be sorted in reverse order (false -> no reverse, true -> reverse)
     * @param numThreads   number of threads to be employed in the sorting (must be greater than 0)
     * @param relatedLists lists that follow the same ordering system as list. These lists will be reordered
     *                     according to the reordering of list. They must be all the same maxSize as list. a null
     *                     value for this argument indicates no related lists
     * @throws IndexOutOfBoundsException if any of the related lists is of different maxSize than the main list
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator, boolean reverseOrder, int numThreads, List<?>... relatedLists)
            throws IndexOutOfBoundsException {
        List<List<?>> relatedListsAsList = new ArrayList<List<?>>();
        relatedListsAsList.addAll(Arrays.asList(relatedLists));
        sortAux(list, comparator, reverseOrder, numThreads, relatedListsAsList);
    }

    /**
     * Sorts the list in n*log(n) time. Applies an efficient sorting algorithm (quick sort).
     * The number of threads employed by the algorithm can be specified (or even specify to use all available
     * cores in the machine.
     * A list of related lists (their elements are related to the elements of list) are ordered in the same way as list
     * The basics of the algorithm were extracted from its description in Wikipedia
     *
     * @param list         list to sort
     * @param comparator   comparator to use in the sorting
     * @param reverseOrder whether the list must be sorted in reverse order (false -> no reverse, true -> reverse)
     * @param numThreads   number of threads to be employed in the sorting (must be greater than 0)
     * @param relatedLists lists that follow the same ordering system as list. These lists will be reordered
     *                     according to the reordering of list. They must be all the same maxSize as list. a null
     *                     value for this argument indicates no related lists
     * @throws IndexOutOfBoundsException if any of the related lists is of different maxSize than the main list
     * @throws IllegalArgumentException  if numThreads is lesser than 1
     */
    static <T> void sortAux(List<T> list, Comparator<? super T> comparator, boolean reverseOrder, int numThreads, List<List<?>> relatedLists)
            throws IndexOutOfBoundsException, IllegalArgumentException {

        if (numThreads < 1) {
            throw new IllegalArgumentException("Number of threads must be greater than 1");
        }
        if (relatedLists == null) {
            relatedLists = new ArrayList<List<?>>(0);
        }

        // mask used to divide the elements of related lists after the ordering has been carried out
        List<Boolean> leftRightMask = null;
        if (relatedLists.size() > 0) {
            leftRightMask = new ArrayList<Boolean>(list.size());
        }
        for (List<?> anotherList : relatedLists) {
            if (list.size() != anotherList.size()) {
                throw new IndexOutOfBoundsException("Size of related lists must be the same as the main list, main list: " +
                        list.size() + ", a related list: " + anotherList.size());
            }
        }
        if (list.size() > 1) {
            int reverse = reverseOrder ? -1 : 1;

            // can paralelize --> perform recursively
            //noinspection RedundantCast
            if (numThreads > 1) {
                T pivot = list.remove(0);
                List<Object> relatedPivots = new ArrayList<Object>();
                for (List<?> relatedList1 : relatedLists) {
                    relatedPivots.add(relatedList1.remove(0));
                }
                List<T> less = new ArrayList<T>();
                List<T> greater = new ArrayList<T>();

                for (T element : list) {
                    if (reverse * comparator.compare(pivot, element) >= 0) {
                        less.add(element);
                        if (leftRightMask != null) {
                            leftRightMask.add(false);
                        }

                    } else {
                        greater.add(element);
                        if (leftRightMask != null) {
                            leftRightMask.add(true);
                        }
                    }
                }

                // divide the number of threads roughly in proportion to the maxSize of the remaining lists
                int leftThreads = (less.size() * numThreads) / (less.size() + greater.size());
                if (leftThreads < 1) {
                    leftThreads = 1;
                }
                if (leftThreads == numThreads) {
                    leftThreads = numThreads - 1;
                }
                int rightThreads = numThreads - leftThreads;

                // now that the main list has been divided in two pieces, divide also the related lists using the mask
                List<List<?>> relatedLess = new ArrayList<List<?>>();
                List<List<?>> relatedGreater = new ArrayList<List<?>>();
                for (List<?> relatedList : relatedLists) {
                    relatedLess.add(removeLessElements(relatedList, leftRightMask));
                    relatedGreater.add(cloneList(relatedList));
                }


                QuickSortTask<T> quickSortTaskLeft = new QuickSortTask<T>(less, comparator, reverseOrder, leftThreads, relatedLess);
                QuickSortTask<T> quickSortTaskRight = new QuickSortTask<T>(greater, comparator, reverseOrder, rightThreads, relatedGreater);
                TaskFinalizationIndicator tfiLeft = ParallelTaskExecutor.executeTask(quickSortTaskLeft);
                TaskFinalizationIndicator tfiRight = ParallelTaskExecutor.executeTask(quickSortTaskRight);
                tfiLeft.waitForFinalization();
                tfiRight.waitForFinalization();

                // clear all lists before reconstructing them
                list.clear();
                for (List<?> relatedList : relatedLists) {
                    relatedList.clear();
                }

                // reconstruct lists
                list.addAll(less);
                for (int i = 0; i < relatedLists.size(); i++) {
                    ((List<Object>) relatedLists.get(i)).addAll(relatedLess.get(i));
                }
                list.add(pivot);
                for (int i = 0; i < relatedLists.size(); i++) {
                    ((List<Object>) relatedLists.get(i)).add(relatedPivots.get(i));
                }
                list.addAll(greater);
                for (int i = 0; i < relatedLists.size(); i++) {
                    ((List<Object>) relatedLists.get(i)).addAll(relatedGreater.get(i));
                }
            }
            // no more threads available --> use swaps for the rest of the algorithm (do not take more memory)
            else {
                Lists.sortWithSwaps(list, comparator, reverse, 0, list.size() - 1, relatedLists);
            }
        }
    }

    static <T> void sortWithSwaps(List<T> list, Comparator<? super T> comparator, int reverse, int left, int right, List<List<?>> relatedLists) {
        if (right > left) {
            int newPivotIndex = partition(list, comparator, reverse, left, right, right, relatedLists);
            sortWithSwaps(list, comparator, reverse, left, newPivotIndex - 1, relatedLists);
            sortWithSwaps(list, comparator, reverse, newPivotIndex + 1, right, relatedLists);
        }
    }


    private static <T> int partition(List<T> list, Comparator<? super T> comparator, int reverse, int left, int right, int pivotIndex, List<List<?>> relatedLists) {
        try {
            T pivotValue = list.get(pivotIndex);
            swapElements(list, pivotIndex, right, relatedLists);
            int storeIndex = left;
            for (int i = left; i < right; i++) {
                if (reverse * comparator.compare(list.get(i), pivotValue) <= 0) {
                    swapElements(list, i, storeIndex, relatedLists);
                    storeIndex = storeIndex + 1;
                }
            }
            swapElements(list, storeIndex, right, relatedLists);
            return storeIndex;
        } catch (IndexOutOfBoundsException e) {
            // wrong coding of this algorithm!!!
            //noinspection ThrowableInstanceNeverThrown
            new Exception("Internal error in jacz.util.lists.Lists class due to wrong coding!!!").printStackTrace();
            System.exit(1);
        }
        return 0;
    }


    public static <T> void swapElements(List<T> list, int i, int j) throws IndexOutOfBoundsException {
        if (i != j) {
            T tempElement = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tempElement);
        }
    }

    public static <T> void swapElements(List<T> list, int i, int j, List<?>... relatedLists) throws IndexOutOfBoundsException {
        swapElements(list, i, j);
        for (List<?> l : relatedLists) {
            swapElements(l, i, j);
        }
    }

    private static <T> void swapElements(List<T> list, int i, int j, List<List<?>> relatedLists) throws IndexOutOfBoundsException {
        swapElements(list, i, j);
        if (relatedLists != null) {
            for (List<?> anotherList : relatedLists) {
                swapElements(anotherList, i, j);
            }
        }
    }


    private static <T> List<T> removeLessElements(List<T> list, List<Boolean> leftRightMask) {
        List<T> lessList = new ArrayList<T>();
        int listCont = 0;
        int maskCont = 0;
        while (listCont < list.size()) {
            if (!leftRightMask.get(maskCont)) {
                lessList.add(list.remove(listCont));
            } else {
                listCont++;
            }
            maskCont++;
        }
        return lessList;
    }

    public static <T> List<T> cloneList(List<T> list) {
        return new ArrayList<T>(list);
    }

    public static <T extends Filterable> List<Boolean> filterElements(List<T> list, Object filter) {
        return filterElements(list, filter, 1);
    }

    public static <T extends Filterable> List<Boolean> filterElements(List<T> list, Object filter, int threads) {
        if (list.size() == 0) {
            return new ArrayList<Boolean>(0);
        } else if (threads > 1 && list.size() > 1) {

            // divide list for each thread
            List<List<T>> subLists = divideList(list, threads);
            List<FilterElementsTask> filterElementsTaskList = new ArrayList<FilterElementsTask>(threads);
            Set<TaskFinalizationIndicator> tfiSet = new HashSet<TaskFinalizationIndicator>(threads);
            for (List<T> subList : subLists) {
                FilterElementsTask filterElementsTask = new FilterElementsTask(subList, filter);
                filterElementsTaskList.add(filterElementsTask);
                tfiSet.add(ParallelTaskExecutor.executeTask(filterElementsTask));
            }
            for (TaskFinalizationIndicator tfi : tfiSet) {
                tfi.waitForFinalization();
            }

            // reconstruct result
            List<Boolean> mask = new ArrayList<Boolean>(list.size());
            for (FilterElementsTask filterElementsTask : filterElementsTaskList) {
                mask.addAll(filterElementsTask.getMask());
            }
            return mask;
        } else {
            List<Boolean> mask = new ArrayList<Boolean>(list.size());
            for (T element : list) {
                mask.add(element.filter(filter));
            }
            return mask;
        }
    }


    public static <T> List<List<T>> divideList(List<T> list, int numSegments) throws IllegalArgumentException {
        List<List<T>> segments = new ArrayList<List<T>>();
        List<Integer> segmentSizes = NumericUtil.divide(list.size(), numSegments);

        int count = 0;
        for (int i = 0; i < numSegments; i++) {
            segments.add(list.subList(count, count + segmentSizes.get(i)));
            count += segmentSizes.get(i);
        }
        return segments;
    }

    public static void checkNonNullAndEqualInSize(List<?>... lists) throws NullPointerException, IllegalArgumentException {
        if (lists.length != 0) {
            int size = lists[0].size();
            for (int i = 1; i < lists.length; i++) {
                if (lists[i].size() != size) {
                    // collect the sizes
                    String sizesStr = Integer.toString(size);
                    for (int j = 1; j < lists.length; j++) {
                        sizesStr += ", " + lists[j].size();
                    }
                    throw new IllegalArgumentException("The lists are different in maxSize: " + sizesStr);
                }
            }
        }
    }

    public static void normalizeList(List<Float> list, float total) {
        List<Double> doubleList = new ArrayList<Double>(list.size());
        while (!list.isEmpty()) {
            doubleList.add((double) list.remove(0));
        }
        normalizeList(doubleList, total);
        for (double value : doubleList) {
            list.add((float) value);
        }
    }

    public static void normalizeList(List<Double> list, double total) {
        double listSum = 0d;
        for (double value : list) {
            listSum += value;
        }
        double factor = total / listSum;
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i) * factor);
        }
    }
}
