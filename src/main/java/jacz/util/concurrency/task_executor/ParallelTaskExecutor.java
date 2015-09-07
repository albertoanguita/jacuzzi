package jacz.util.concurrency.task_executor;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.concurrency.concurrency_controller.ConcurrencyController;

/**
 * This class allows to launch tasks in parallel. It receives implementations of the ParallelTask interface, and
 * creates threads for executing those tasks in parallel
 */
public class ParallelTaskExecutor {

    /**
     * No objects of this class must be created
     */
    private ParallelTaskExecutor() {
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskFinalizationIndicator object allows to wait for the finalization of the task
     *
     * @param parallelTask task to execute
     * @return a TaskFinalizationIndicator that allows invoker to know when the task has been completed
     *         (its subsequent use is optional, only necessary if the parent thread must know when the
     *         child thread has finished its task. If not necessary it can just be ignored)
     */
    public static TaskFinalizationIndicator executeTask(ParallelTask parallelTask) {
        return executeTask(parallelTask, ThreadUtil.invokerName(1));
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskFinalizationIndicator object allows to wait for the finalization of the task
     *
     * @param parallelTask task to execute
     * @return a TaskFinalizationIndicator that allows invoker to know when the task has been completed
     *         (its subsequent use is optional, only necessary if the parent thread must know when the
     *         child thread has finished its task. If not necessary it can just be ignored)
     */
    public static TaskFinalizationIndicator executeTask(ParallelTask parallelTask, String threadName) {
        // create a parallel task executor thread for this task
        ParallelTaskExecutorThread parallelTaskExecutorThread = new ParallelTaskExecutorThread(parallelTask, threadName);
        return runParallelTask(parallelTaskExecutorThread);
    }


    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskFinalizationIndicator object allows to wait for the finalization of the task
     *
     * @param parallelTask          task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskFinalizationIndicator that allows invoker to know when the task has been completed
     *         (its subsequent use is optional, only necessary if the parent thread must know when the
     *         child thread has finished its task. If not necessary it can just be ignored)
     */
    public static TaskFinalizationIndicator executeTask(
            ParallelTask parallelTask,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        return executeTask(parallelTask, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskFinalizationIndicator object allows to wait for the finalization of the task
     *
     * @param parallelTask          task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskFinalizationIndicator that allows invoker to know when the task has been completed
     *         (its subsequent use is optional, only necessary if the parent thread must know when the
     *         child thread has finished its task. If not necessary it can just be ignored)
     */
    public static TaskFinalizationIndicator executeTask(
            ParallelTask parallelTask,
            String threadName,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        return executeTask(parallelTask, threadName, concurrencyController, concurrentActivity, false);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskFinalizationIndicator object allows to wait for the finalization of the task
     *
     * @param parallelTask          task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskFinalizationIndicator that allows invoker to know when the task has been completed
     *         (its subsequent use is optional, only necessary if the parent thread must know when the
     *         child thread has finished its task. If not necessary it can just be ignored)
     */
    public static TaskFinalizationIndicator executeTask(
            ParallelTask parallelTask,
            ConcurrencyController concurrencyController,
            String concurrentActivity,
            boolean sequentialActivityRegistration) {
        return executeTask(parallelTask, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity, sequentialActivityRegistration);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskFinalizationIndicator object allows to wait for the finalization of the task
     *
     * @param parallelTask          task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskFinalizationIndicator that allows invoker to know when the task has been completed
     *         (its subsequent use is optional, only necessary if the parent thread must know when the
     *         child thread has finished its task. If not necessary it can just be ignored)
     */
    public static TaskFinalizationIndicator executeTask(
            ParallelTask parallelTask,
            String threadName,
            ConcurrencyController concurrencyController,
            String concurrentActivity,
            boolean sequentialActivityRegistration) {

        ConcurrencyController.QueueElement queueElement = null;
        if (sequentialActivityRegistration) {
            queueElement = concurrencyController.registerActivity(concurrentActivity);
        }
        // create a parallel task executor thread for this task
        ParallelTaskExecutorThread parallelTaskExecutorThread =
                new ParallelTaskExecutorThread(parallelTask, threadName, concurrencyController, queueElement, concurrentActivity);
        return runParallelTask(parallelTaskExecutorThread);
    }

    /**
     * This method runs an already created ParallelTaskExecutorThread
     *
     * @param parallelTaskExecutorThread the thread to run
     * @return the TaskFinalizationIndicator given by this thread
     */
    private static TaskFinalizationIndicator runParallelTask(ParallelTaskExecutorThread parallelTaskExecutorThread) {
        // acquire the TaskFinalizationIndicator of the newly created thread, for returning it later
        TaskFinalizationIndicator tfi = parallelTaskExecutorThread.getTaskFinalizationIndicator();

        // execute the task in parallel mode
        parallelTaskExecutorThread.start();

        // return the TaskFinalizationIndicator
        return tfi;
    }
}
