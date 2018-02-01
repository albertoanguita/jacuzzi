package org.aanguita.jacuzzi.concurrency;

import java.util.concurrent.TimeoutException;

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
