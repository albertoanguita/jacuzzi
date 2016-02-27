package jacz.util.concurrency.task_executor;

import java.util.Collection;
import java.util.concurrent.Semaphore;

/**
 * This class indicates the finalization state of the execution of a task. It allows other threads
 * to query this state and wait until the task is finalized (not mandatory to use)
 */
public class TaskSemaphore {

    private Semaphore semaphore;

    /**
     * The thread for which this TaskSemaphore is waiting to finish
     */
    private ParallelTaskExecutorThread parallelTaskExecutorThread;

    /**
     * The parallel task executing
     */
    private Task task;

    /**
     * Class constructor
     *
     * @param parallelTaskExecutorThread the parallel task bond to this object
     * @param task               the parallel task related to this finalization indicator
     */
    TaskSemaphore(ParallelTaskExecutorThread parallelTaskExecutorThread, Task task) {
        this.parallelTaskExecutorThread = parallelTaskExecutorThread;
        this.task = task;
        semaphore = new Semaphore(0);
    }

    /**
     * Indicates that the task has been finalized
     */
    void finaliseTask() {
        semaphore.release();
    }

    /**
     * Stops the execution until the task has been finalized
     */
    public void waitForFinalization() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            // ignore this interruption
        }
    }

    /**
     * Waits for a set of task finalization indicators to finish
     *
     * @param tfiCollection collection of task finalization indicators
     */
    public static void waitForFinalization(Collection<TaskSemaphore> tfiCollection) {
        for (TaskSemaphore tfi : tfiCollection) {
            tfi.waitForFinalization();
        }
    }

    public void interrupt() {
        parallelTaskExecutorThread.interrupt();
    }

    public boolean isInterrupted() {
        return parallelTaskExecutorThread.isInterrupted();
    }

    public Task getTask() {
        return task;
    }
}
