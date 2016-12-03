package org.aanguita.jacuzzi.concurrency;

import java.util.concurrent.Semaphore;

/**
 * This class provides simple stop/let go functionality to threads. SimpleSemaphore has two methods for opening or
 * closing access. Access threads invoke a method that is non-blocking (returns immediately) if access is open, and
 * is blocking if access is closed (until a controller thread opens access again)
 * <p>
 * The simple semaphore does not maintain the number of available permits or acquired stops. It just acts as an
 * open/close barrier
 */
public class SimpleSemaphore {

    /**
     * Semaphore used to control the execution flow
     */
    private Semaphore semaphore;

    /**
     * Flag for controlling when the element is paused
     */
    private boolean paused;

    public SimpleSemaphore() {
        this(false);
    }

    public SimpleSemaphore(boolean fairness) {
        semaphore = new Semaphore(1, fairness);
        paused = false;
    }

    /**
     * Pauses the element. Further invocations to the access method will be blocked until someone resumes the element.
     * The element can be paused more times, with no effect.
     */
    public void pause() {
        // if the lock isn't currently acquired, then it must be acquired. Otherwise, leave it paused (so this
        // invocation never blocks)
        synchronized (this) {
            if (!paused) {
                semaphore.acquireUninterruptibly();
                paused = true;
            }
        }
    }

    /**
     * Resumes the element. If the element was paused, all accessions will be allowed again. Further invocations to
     * this method will have no effect. This element can be resumed by any thread, even if it did not pause the
     * element previously
     */
    public void resume() {
        // if the lock is currently acquired, then release it. Otherwise leave it unlocked (so this
        // invocation never blocks)
        synchronized (this) {
            if (paused) {
                semaphore.release();
                paused = false;
            }
        }
    }

    /**
     * This method makes the invoking thread access the pausable element. If the element is currently paused, the
     * thread will be blocked until some other thread resumes the element. If the element is not paused, this method
     * will return immediately.
     * <p/>
     * If fairness is used, upon resume, blocked accesses will be executed in order of arrival
     */
    public void access() {
        semaphore.acquireUninterruptibly();
        semaphore.release();
    }
}