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
 */
public class ThreadExecutor {

    private static class Task {

        protected final ConcurrencyController concurrencyController;

        protected final String concurrentActivity;

        public Task(ConcurrencyController concurrencyController, String concurrentActivity) {
            this.concurrencyController = concurrencyController;
            this.concurrentActivity = concurrentActivity;
        }

        protected void start() {
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

        public InnerCallable(Callable<V> task, ConcurrencyController concurrencyController, String concurrentActivity) {
            super(concurrencyController, concurrentActivity);
            this.task = task;
        }

        @Override
        public V call() throws Exception {
            start();
            V result = task.call();
            end();
            return result;
        }
    }

    private static class InnerRunnable extends Task implements Runnable {

        private final Runnable task;

        public InnerRunnable(Runnable task, ConcurrencyController concurrencyController, String concurrentActivity) {
            super(concurrencyController, concurrentActivity);
            this.task = task;
        }

        @Override
        public void run() {
            start();
            task.run();
            end();
        }
    }


    /**
     * The default thread factory
     */
    private static class InnerThreadFactory implements ThreadFactory {

        private final ThreadGroup group;

        private String nextName;

        InnerThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            nextName = "unknown";
        }

        private void setNextName(String name) {
            nextName = name;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(
                    group,
                    r,
                    nextName,
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    private static final InnerThreadFactory innerThreadFactory = new InnerThreadFactory();

    private static final ExecutorService executorService = Executors.newCachedThreadPool(innerThreadFactory);

    private static final ObjectCount<String> registeredClients = new ObjectCount<>();

    public static void registerClient(String clientName) {
        registeredClients.addObject(clientName);
    }

    public static void shutdownClient(String clientName) throws IllegalArgumentException {
        try {
            registeredClients.subtractObject(clientName);
            if (registeredClients.getTotalCount() == 0) {
                executorService.shutdown();
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("No registered client: " + clientName);
        }
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
        innerThreadFactory.setNextName(threadName);
        return executorService.submit(new InnerCallable<>(task, concurrencyController, concurrentActivity));
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
        innerThreadFactory.setNextName(threadName);
        return executorService.submit(new InnerRunnable(task, concurrencyController, concurrentActivity));
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
        innerThreadFactory.setNextName(threadName);
        return executorService.submit(new InnerRunnable(task, concurrencyController, concurrentActivity), result);
    }
}
