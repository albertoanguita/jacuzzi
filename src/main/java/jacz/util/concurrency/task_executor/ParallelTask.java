package jacz.util.concurrency.task_executor;

/**
 * This interface provides the skeleton for tasks that can be executed in parallel.
 * The function performTask must be implemented with the parallel task. Results will have to be
 * extracted outside that function, as it does not return anything
 */
public interface ParallelTask {

    /**
     * Task that is to be performed in parallel. Caution must be taken so this function does not have access to
     * shared resources that do not support parallel computing
     */
    public void performTask();
}
