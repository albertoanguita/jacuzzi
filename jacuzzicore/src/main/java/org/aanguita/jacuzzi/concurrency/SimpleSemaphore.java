package org.aanguita.jacuzzi.concurrency;

import java.util.concurrent.TimeoutException;

/**
 * @deprecated Use Barrier
 */
public class SimpleSemaphore {

    private final Barrier barrier;

    public SimpleSemaphore() {
        barrier = new Barrier();
    }

    public SimpleSemaphore(boolean fairness) {
        barrier = new Barrier(fairness);
    }

    /**
     * Pauses the element. Further invocations to the access method will be blocked until someone resumes the element.
     * The element can be paused more times, with no effect.
     */
    public void pause() {
        barrier.pause();
    }

    /**
     * Resumes the element. If the element was paused, all accessions will be allowed again. Further invocations to
     * this method will have no effect. This element can be resumed by any thread, even if it did not pause the
     * element previously
     */
    public void resume() {
        barrier.resume();
    }

    /**
     * This method makes the invoking thread access the pausable element. If the element is currently paused, the
     * thread will be blocked until some other thread resumes the element. If the element is not paused, this method
     * will return immediately.
     * <p/>
     * If fairness is used, upon resume, blocked accesses will be executed in order of arrival
     */
    public void access() {
        barrier.access();
    }

    /**
     * This method makes the invoking thread access the pausable element. If the element is currently paused, the
     * thread will be blocked until some other thread resumes the element, or the timeout fires. If the element is not paused, this method
     * will return immediately.
     * <p/>
     * If fairness is used, upon resume, blocked accesses will be executed in order of arrival
     *
     * @param timeout: the time in millis to wait before a timeout exception kicks. If 0 or negative, the timeout
     *                 exception is thrown directly
     * @throws TimeoutException if the pausable element cannot be accessed before the given timeout passes, or if timeout is equals or less than zero
     */
    public void access(long timeout) throws TimeoutException {
        barrier.access(timeout);
    }

    @Override
    public String toString() {
        return barrier.toString();
    }
}
