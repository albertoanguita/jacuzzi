package jacz.util.concurrency.task_executor;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.concurrency.concurrency_controller.ConcurrencyController;
import jacz.util.maps.ObjectCount;

import java.util.concurrent.*;

/**
 * This class provides static methods for launching named threads (runnable and callable implementations),
 * using a cached thread pool in the background
 * <p/>
 * It is compatible with the use of SmartLock for fine control for the execution times of the threads
 * todo fix javadocs
 * todo register the stack trace of registers so we can track down who did not unregister during debugging. Give each register an id which maps to their stack trace, and make the id be used in the unregistering
 */
public class ThreadExecutor {

    private static class Task {

        protected final ConcurrencyController concurrencyController;

        protected final String concurrentActivity;

        private final String threadName;

        public Task(ConcurrencyController concurrencyController, String concurrentActivity, String threadName) {
            this.concurrencyController = concurrencyController;
            this.concurrentActivity = concurrentActivity;
            this.threadName = threadName;
        }

        protected void start() {
            Thread.currentThread().setName(threadName);
            if (concurrencyController != null) {
                concurrencyController.beginActivity(concurrentActivity);
            }
        }

        protected void end() {
            if (concurrencyController != null) {
                concurrencyController.endActivity(concurrentActivity);
            }
        }
    }

    private static class InnerCallable<V> extends Task implements Callable<V> {

        private final Callable<V> task;

        public InnerCallable(Callable<V> task, ConcurrencyController concurrencyController, String concurrentActivity, String threadName) {
            super(concurrencyController, concurrentActivity, threadName);
            this.task = task;
        }

        @Override
        public V call() throws Exception {
            try {
                start();
                return task.call();
            } finally {
                end();
            }
        }
    }

    private static class InnerRunnable extends Task implements Runnable {

        private final Runnable task;

        public InnerRunnable(Runnable task, ConcurrencyController concurrencyController, String concurrentActivity, String threadName) {
            super(concurrencyController, concurrentActivity, threadName);
            this.task = task;
        }

        @Override
        public void run() {
            try {
                start();
                task.run();
            } finally {
                end();
            }
        }
    }

    private static ExecutorService executorService;

    private static final ObjectCount<String> registeredClients = new ObjectCount<>();

    public static synchronized void registerClient(String clientName) {
        registeredClients.addObject(clientName);
        if (registeredClients.getTotalCount() == 1) {
            // we must activate the executor service now
            executorService = Executors.newCachedThreadPool();
        }
    }

    public static synchronized void shutdownClient(String clientName) throws IllegalArgumentException {
        try {
            registeredClients.subtractObject(clientName);
            if (registeredClients.getTotalCount() == 0) {
                // no registered clients at this moment -> shutdown the service
                executorService.shutdown();
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("No registered client: " + clientName);
        }
    }

    public static ObjectCount<String> getRegisteredClients() {
        return registeredClients;
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return submit(task, ThreadUtil.invokerName(1));
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static Future<?> submit(Runnable task) {
        return submit(task, ThreadUtil.invokerName(1));
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submit(Runnable task, T result) {
        return submit(task, result, ThreadUtil.invokerName(1));
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submit(Callable<T> task, String threadName) {
        return submit(task, threadName, null, null);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static Future<?> submit(Runnable task, String threadName) {
        return submit(task, threadName, null, null);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submit(Runnable task, T result, String threadName) {
        return submit(task, result, threadName, null, null);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submit(
            Callable<T> task,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        return submit(task, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static Future<?> submit(
            Runnable task,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        return submit(task, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submit(
            Runnable task,
            T result,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        return submit(task, result, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submit(
            Callable<T> task,
            String threadName,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        return executorService.submit(new InnerCallable<>(task, concurrencyController, concurrentActivity, threadName));
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized Future<?> submit(
            Runnable task,
            String threadName,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        return executorService.submit(new InnerRunnable(task, concurrencyController, concurrentActivity, threadName));
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submit(
            Runnable task,
            T result,
            String threadName,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
//        innerThreadFactory.setNextName(threadName);
        return executorService.submit(new InnerRunnable(task, concurrencyController, concurrentActivity, threadName), result);
    }
}
