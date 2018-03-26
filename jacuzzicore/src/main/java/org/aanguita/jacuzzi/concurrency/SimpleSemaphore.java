package org.aanguita.jacuzzi.concurrency;

/**
 * @deprecated Use Barrier
 */
public class SimpleSemaphore extends Barrier {
    public SimpleSemaphore() {
    }

    public SimpleSemaphore(boolean fairness) {
        super(fairness);
    }
}
