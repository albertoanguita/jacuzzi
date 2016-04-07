package jacz.util.lists;

import java.util.Comparator;
import java.util.List;

/**
 * This class implements a parallel task for the quick sort algorithm. The actual implementation of the
 * algorithm is located in the Lists class (it is invoked from here)
 */
class QuickSortTask<T> implements Runnable {

    private List<T> list;

    private Comparator<? super T> comparator;

    private boolean reverseOrder;

    private int numThreads;

    private List<List<?>> relatedLists;


    QuickSortTask(List<T> list, Comparator<? super T> comparator, boolean reverseOrder, int numThreads, List<List<?>> relatedLists) {
        this.list = list;
        this.comparator = comparator;
        this.reverseOrder = reverseOrder;
        this.numThreads = numThreads;
        this.relatedLists = relatedLists;
    }

    public void run() {
//        Lists.sortAux(list, comparator, reverseOrder, numThreads, relatedLists);
    }
}
