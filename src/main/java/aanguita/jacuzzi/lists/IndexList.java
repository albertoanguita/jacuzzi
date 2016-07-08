package aanguita.jacuzzi.lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 13-mar-2010<br>
 * Last Modified: 13-mar-2010
 */
public class IndexList<T> {

    private List<T> baseList;

    private List<Integer> indexList;

    public IndexList(List<T> baseList) {
        this.baseList = new ArrayList<>(baseList);
        buildIndexList();
    }

    public IndexList(List<T> baseList, Comparator<T> comparator, boolean reverseOrder, int numThreads) {
        this.baseList = new ArrayList<>(baseList);
        buildIndexList();
        sortIndexes(comparator, reverseOrder, numThreads);
    }

    private void buildIndexList() {
        indexList = new ArrayList<>(baseList.size());
        for (int i = 0; i < baseList.size(); i++) {
            indexList.add(i);
        }
    }

    public List<Integer> getIndexList() {
        return indexList;
    }

    public void sortIndexes(Comparator<T> comparator, boolean reverseOrder, int numThreads) {
//        Lists.sort(baseList, comparator, reverseOrder, numThreads, indexList);
        // todo
        throw new RuntimeException();
    }
}
