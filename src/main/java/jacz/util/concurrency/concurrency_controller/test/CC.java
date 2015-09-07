package jacz.util.concurrency.concurrency_controller.test;

import jacz.util.concurrency.concurrency_controller.ConcurrencyControllerReadWrite;
import jacz.util.lists.Duple;

/**
 * Class description
 * <p/>
 * User: Admin<br>
 * Date: 09-may-2008<br>
 * Last Modified: 09-may-2008
 */
public class CC extends ConcurrencyControllerReadWrite {

    public CC() {
        super();
    }

    public CC(int threadCount) {
        super(threadCount);
    }

    @Override
    public Duple<Integer, Integer> readWritePriorities() {
        return new Duple<Integer, Integer>(0, 0);
    }


}