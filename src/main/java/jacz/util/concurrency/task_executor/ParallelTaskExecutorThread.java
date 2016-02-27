package jacz.util.concurrency.task_executor;

import jacz.util.concurrency.concurrency_controller.ConcurrencyController;

/**
 * This class implements threads that can execute tasks implementing the Task interface
 */
class ParallelTaskExecutorThread extends Thread {

    /**
     * Task that is executed
     */
    private Runnable task;

    /**
     * Concurrency controller employed to monitor this task (null if not to be used)
     */
    private ConcurrencyController concurrencyController;

    /**
     * If the activity registration was performed sequentially, we have a QueueElement for beginning the activity
     */
    private final ConcurrencyController.QueueElement queueElement;

    /**
     * The concurrent activity to execute under the monitoring of the concurrency controller
     */
    private String concurrentActivity;

    /**
     * Indicator of finalization of the task execution
     */
    private TaskSemaphore taskSemaphore;

    /**
     * Constructor of class
     *
     * @param task       task that must be executed
     * @param threadName name of the thread to create
     */
    ParallelTaskExecutorThread(Runnable task, String threadName) {
        this(task, threadName, null, null, null);
    }

    /**
     * Constructor of class
     *
     * @param task                  task that must be executed
     * @param threadName            name of the thread to create
     * @param concurrencyController concurrency controller employed to monitor this task (null if not used)
     * @param concurrentActivity    the concurrent activity to execute under the monitoring of the concurrency controller
     */
    ParallelTaskExecutorThread(
            Runnable task,
            String threadName,
            ConcurrencyController concurrencyController,
            ConcurrencyController.QueueElement queueElement,
            String concurrentActivity) {
        super(threadName + "/ParallelTaskExecutor");
        this.task = task;
        this.concurrencyController = concurrencyController;
        this.queueElement = queueElement;
        this.concurrentActivity = concurrentActivity;
        // creates a new indicator
        taskSemaphore = new TaskSemaphore(this, task);
    }

    /**
     * Retrieves the indicator of finalization of the task of this thread
     *
     * @return the task finalization indicator of this thread
     */
    public TaskSemaphore getTaskSemaphore() {
        return taskSemaphore;
    }

    /**
     * Runs the thread
     */
    public void run() {
        if (concurrencyController != null) {
            // indicate the execution start to the given concurrency controller
            if (queueElement != null) {
                // there is also a queue element
                concurrencyController.beginRegisteredActivity(queueElement);
            } else {
                concurrencyController.beginActivity(concurrentActivity);
            }
        }
        // perform the task
        task.run();

        if (concurrencyController != null) {
            // indicate the execution start to the given concurrency controller
            concurrencyController.endActivity(concurrentActivity);
        }
        // indicate that the task has been finalised
        taskSemaphore.finaliseTask();
    }
}
