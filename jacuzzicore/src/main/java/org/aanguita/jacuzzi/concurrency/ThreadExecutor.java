package org.aanguita.jacuzzi.concurrency;

import org.aanguita.jacuzzi.concurrency.controller.ConcurrencyController;
import org.aanguita.jacuzzi.id.AlphaNumFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class provides static methods for launching named threads (runnable and callable implementations),
 * using a cached thread pool in the background
 * <p>
 * It is compatible with the use of SmartLock for fine control for the execution times of the threads
 * todo fix javadocs
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

        protected boolean start() {
            Thread.currentThread().setName(threadName);
            return concurrencyController == null || concurrencyController.beginActivity(concurrentActivity);
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
                return start() ? task.call() : null;
            } catch (Throwable e) {
                if (logger.isErrorEnabled()) {
                    logger.error("UNEXPECTED EXCEPTION THROWN BY CALLABLE IMPLEMENTATION. PLEASE CORRECT THE CODE SO NO THROWABLES ARE THROWN AT THIS LEVEL", e);
                }
                return null;
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
                if (start()) {
                    task.run();
                }
            } catch (Throwable e) {
                if (logger.isErrorEnabled()) {
                    logger.error("UNEXPECTED EXCEPTION THROWN BY RUNNABLE IMPLEMENTATION. PLEASE CORRECT THE CODE SO NO THROWABLES ARE THROWN AT THIS LEVEL", e);
                }
            } finally {
                end();
            }
        }
    }

    public static class ClientData {

        public final String clientName;

        public final StackTraceElement[] threadStack;

        public ClientData(String clientName, StackTraceElement[] threadStack) {
            this.clientName = clientName;
            this.threadStack = threadStack;
        }

        @Override
        public String toString() {
            return "ClientData{" +
                    "clientName='" + clientName + '\'' +
                    ", threadStack=" + Arrays.toString(threadStack) +
                    '}';
        }
    }

    private static final String UNNAMED_CLIENT = "unnamed_client";

    private static final Logger logger = LoggerFactory.getLogger(ThreadExecutor.class);

    private static ExecutorService executorService;

    private static final Map<String, ClientData> registeredClients = new HashMap<>();

    public static synchronized String registerClient() {
        return registerClient(UNNAMED_CLIENT);
    }

    public static synchronized String registerClient(String clientName) {
        String clientId = AlphaNumFactory.getStaticId();
        registeredClients.put(clientId, new ClientData(clientName, Thread.currentThread().getStackTrace()));
        if (registeredClients.size() == 1) {
            // we must activate the executor service now
            executorService = Executors.newCachedThreadPool();
        }
        return clientId;
    }

    public static synchronized void unregisterClient(String clientId) throws IllegalArgumentException {
        if (registeredClients.containsKey(clientId)) {
            registeredClients.remove(clientId);
        } else {
            throw new IllegalArgumentException("No registered client: " + clientId);
        }
        if (registeredClients.isEmpty()) {
            // no registered clients at this moment -> shutdown the service
            executorService.shutdown();
        }
    }

    public static synchronized Map<String, ClientData> getRegisteredClients() {
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
     * Previous registration is not required
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submitUnregistered(Callable<T> task) {
        String id = registerClient();
        Future<T> future = submit(task, ThreadUtil.invokerName(1));
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static Future<?> submitUnregistered(Runnable task) {
        String id = registerClient();
        Future<?> future = submit(task, ThreadUtil.invokerName(1));
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submitUnregistered(Runnable task, T result) {
        String id = registerClient();
        Future<T> future = submit(task, result, ThreadUtil.invokerName(1));
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submitUnregistered(Callable<T> task, String threadName) {
        String id = registerClient();
        Future<T> future = submit(task, threadName, null, null);
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static Future<?> submitUnregistered(Runnable task, String threadName) {
        String id = registerClient();
        Future<?> future = submit(task, threadName, null, null);
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submitUnregistered(Runnable task, T result, String threadName) {
        String id = registerClient();
        Future<T> future = submit(task, result, threadName, null, null);
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submitUnregistered(
            Callable<T> task,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        String id = registerClient();
        Future<T> future = submit(task, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity);
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static Future<?> submitUnregistered(
            Runnable task,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        String id = registerClient();
        Future<?> future = submit(task, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity);
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static <T> Future<T> submitUnregistered(
            Runnable task,
            T result,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        String id = registerClient();
        Future<T> future = submit(task, result, ThreadUtil.invokerName(1), concurrencyController, concurrentActivity);
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submitUnregistered(
            Callable<T> task,
            String threadName,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        String id = registerClient();
        Future<T> future = executorService.submit(new InnerCallable<>(task, concurrencyController, concurrentActivity, threadName));
        unregisterClient(id);
        return future;
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
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized Future<?> submitUnregistered(
            Runnable task,
            String threadName,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        String id = registerClient();
        Future<?> future = executorService.submit(new InnerRunnable(task, concurrencyController, concurrentActivity, threadName));
        unregisterClient(id);
        return future;
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
        return executorService.submit(new InnerRunnable(task, concurrencyController, concurrentActivity, threadName), result);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @param concurrencyController the concurrency controller that will monitor this task
     * @param concurrentActivity    the activity name that this parallel task is going to execute in the
     *                              given concurrency controller
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submitUnregistered(
            Runnable task,
            T result,
            String threadName,
            ConcurrencyController concurrencyController,
            String concurrentActivity) {
        String id = registerClient();
        Future<T> future = executorService.submit(new InnerRunnable(task, concurrencyController, concurrentActivity, threadName), result);
        unregisterClient(id);
        return future;
    }
}
