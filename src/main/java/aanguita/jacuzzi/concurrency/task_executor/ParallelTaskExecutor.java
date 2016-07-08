package aanguita.jacuzzi.concurrency.task_executor;

/**
 * This class allows to launch tasks in parallel. It receives implementations of the Runnable interface, and
 * creates threads for executing those tasks in parallel
 * todo remove
 */
public class ParallelTaskExecutor {
//
//    /**
//     * No objects of this class must be created
//     */
//    private ParallelTaskExecutor() {
//    }
//
//    /**
//     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
//     * TaskSemaphore object allows to wait for the finalization of the task
//     *
//     * @param task task to execute
//     * @return a TaskSemaphore that allows invoker to know when the task has been completed
//     *         (its subsequent use is optional, only necessary if the parent thread must know when the
//     *         child thread has finished its task. If not necessary it can just be ignored)
//     */
//    public static TaskSemaphore executeTask(Runnable task) {
//        return executeTask(task, ThreadUtil.invokerName(1));
//    }
//
//    /**
//     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
//     * TaskSemaphore object allows to wait for the finalization of the task
//     *
//     * @param task task to execute
//     * @return a TaskSemaphore that allows invoker to know when the task has been completed
//     *         (its subsequent use is optional, only necessary if the parent thread must know when the
//     *         child thread has finished its task. If not necessary it can just be ignored)
//     */
//    public static TaskSemaphore executeTask(Runnable task, String threadName) {
//        // create a parallel task executor thread for this task
//        ParallelTaskExecutorThread parallelTaskExecutorThread = new ParallelTaskExecutorThread(task, threadName);
//        return runParallelTask(parallelTaskExecutorThread);
//    }
//
//
//    /**
//     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
//     * TaskSemaphore object allows to wait for the finalization of the task
//     *
//     * @param task          task to execute
//     * @param concurrencyController the concurrency controller that will monitor this task
//     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
//     *                              given concurrency controller
//     * @return a TaskSemaphore that allows invoker to know when the task has been completed
//     *         (its subsequent use is optional, only necessary if the parent thread must know when the
//     *         child thread has finished its task. If not necessary it can just be ignored)
//     */
//    public static TaskSemaphore executeTask(
//            Runnable task,
//            ConcurrencyController concurrencyController,
//            String concurrentActivity) {
//        return executeTask(task, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity);
//    }
//
//    /**
//     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
//     * TaskSemaphore object allows to wait for the finalization of the task
//     *
//     * @param task          task to execute
//     * @param concurrencyController the concurrency controller that will monitor this task
//     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
//     *                              given concurrency controller
//     * @return a TaskSemaphore that allows invoker to know when the task has been completed
//     *         (its subsequent use is optional, only necessary if the parent thread must know when the
//     *         child thread has finished its task. If not necessary it can just be ignored)
//     */
//    public static TaskSemaphore executeTask(
//            Runnable task,
//            String threadName,
//            ConcurrencyController concurrencyController,
//            String concurrentActivity) {
//        return executeTask(task, threadName, concurrencyController, concurrentActivity, false);
//    }
//
//    /**
//     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
//     * TaskSemaphore object allows to wait for the finalization of the task
//     *
//     * @param task          task to execute
//     * @param concurrencyController the concurrency controller that will monitor this task
//     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
//     *                              given concurrency controller
//     * @return a TaskSemaphore that allows invoker to know when the task has been completed
//     *         (its subsequent use is optional, only necessary if the parent thread must know when the
//     *         child thread has finished its task. If not necessary it can just be ignored)
//     */
//    public static TaskSemaphore executeTask(
//            Runnable task,
//            ConcurrencyController concurrencyController,
//            String concurrentActivity,
//            boolean sequentialActivityRegistration) {
//        return executeTask(task, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity, sequentialActivityRegistration);
//    }
//
//    /**
//     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
//     * TaskSemaphore object allows to wait for the finalization of the task
//     *
//     * @param task          task to execute
//     * @param concurrencyController the concurrency controller that will monitor this task
//     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
//     *                              given concurrency controller
//     * @return a TaskSemaphore that allows invoker to know when the task has been completed
//     *         (its subsequent use is optional, only necessary if the parent thread must know when the
//     *         child thread has finished its task. If not necessary it can just be ignored)
//     */
//    public static TaskSemaphore executeTask(
//            Runnable task,
//            String threadName,
//            ConcurrencyController concurrencyController,
//            String concurrentActivity,
//            boolean sequentialActivityRegistration) {
//
//        ConcurrencyController.QueueElement queueElement = null;
//        if (sequentialActivityRegistration) {
//            // todo remove this, no sense, no need
//            queueElement = concurrencyController.registerActivity(concurrentActivity);
//        }
//        // create a parallel task executor thread for this task
//        ParallelTaskExecutorThread parallelTaskExecutorThread =
//                new ParallelTaskExecutorThread(task, threadName, concurrencyController, queueElement, concurrentActivity);
//        return runParallelTask(parallelTaskExecutorThread);
//    }
//
//    /**
//     * This method runs an already created ParallelTaskExecutorThread
//     *
//     * @param parallelTaskExecutorThread the thread to run
//     * @return the TaskSemaphore given by this thread
//     */
//    private static TaskSemaphore runParallelTask(ParallelTaskExecutorThread parallelTaskExecutorThread) {
////        Executors
//
//
//        // acquire the TaskSemaphore of the newly created thread, for returning it later
//        TaskSemaphore tfi = parallelTaskExecutorThread.getTaskSemaphore();
//
//        // execute the task in parallel mode
//        parallelTaskExecutorThread.start();
//
//        // return the TaskSemaphore
//        return tfi;
//    }
}
