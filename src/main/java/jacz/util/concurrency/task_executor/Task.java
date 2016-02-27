package jacz.util.concurrency.task_executor;

/**
 * This interface provides the skeleton for tasks that execute some custom code.
 * Results will have to be extracted outside the performTask function, as it does not return anything
 */
public interface Task {

    /**
     * Task that is to be performed in parallel. Caution must be taken so this function does not have access to
     * shared resources that do not support parallel computing
     */
    void performTask();
}
