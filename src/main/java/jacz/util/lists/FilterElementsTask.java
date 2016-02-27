package jacz.util.lists;

import java.util.List;

import static jacz.util.lists.Lists.filterElements;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 13-mar-2010<br>
 * Last Modified: 13-mar-2010
 */
class FilterElementsTask implements Runnable {

    private List<? extends Filterable> list;

    private Object filter;

    private List<Boolean> mask;

    public <T extends Filterable> FilterElementsTask(List<T> list, Object filter) {
        this.list = list;
        this.filter = filter;
    }

    public List<Boolean> getMask() {
        return mask;
    }

    @Override
    public void run() {
        mask = filterElements(list, filter, 1);
    }
}
