package jacz.util.lists;

import jacz.util.concurrency.task_executor.Task;

import java.util.List;
import java.util.Comparator;

/**
 * This class implements a parallel task for the quick sort algorithm. The actual implementation of the
 * algorithm is located in the Lists class (it is invoked from here)
 */
class QuickSortTask<T> implements Task {

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

    public void performTask() {
        Lists.sortAux(list, comparator, reverseOrder, numThreads, relatedLists);
    }
}
