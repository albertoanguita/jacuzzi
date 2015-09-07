package jacz.util.concurrency.concurrency_controller;

import jacz.util.lists.Duple;

/**
 * A basic concurrency controller with readers and writers, and similar priority for both activities
 */
public class ConcurrencyControllerReadWriteBasic extends ConcurrencyControllerReadWrite {

    public ConcurrencyControllerReadWriteBasic() {
        super();
    }

    public ConcurrencyControllerReadWriteBasic(int maxNumberOfExecutionsAllowed) {
        super(maxNumberOfExecutionsAllowed);
    }

    @Override
    public Duple<Integer, Integer> readWritePriorities() {
        return new Duple<>(0, 0);
    }
}
