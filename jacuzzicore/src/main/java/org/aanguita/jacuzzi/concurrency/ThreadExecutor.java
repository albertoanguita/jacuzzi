package org.aanguita.jacuzzi.concurrency;

import org.aanguita.jacuzzi.id.AlphaNumFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * This class provides static methods for launching named threads (runnable and callable implementations),
 * using a cached thread pool in the background
 * <p>
 * It is compatible with the use of SmartLock for fine control for the execution times of the threads
 * todo fix javadocs
 */
public class ThreadExecutor {

    private static class Task {

        private final String threadName;
        
        private final Consumer<Exception> exceptionConsumer;

        Task(String threadName, Consumer<Exception> exceptionConsumer) {
            this.threadName = threadName;
            this.exceptionConsumer = exceptionConsumer;
        }

        protected boolean start() {
            Thread.currentThread().setName(threadName);
            return true;
        }
        
        void consumeRuntimeException(RuntimeException e) {
            if (exceptionConsumer != null) {
                exceptionConsumer.accept(e);
            } else {
                throw e;
            }
        }
    }

    private static class InnerCallable<V> extends Task implements Callable<V> {

        private final Callable<V> task;

        InnerCallable(Callable<V> task, String threadName, Consumer<Exception> exceptionConsumer) {
            super(threadName, exceptionConsumer);
            this.task = task;
        }

        @Override
        public V call() throws Exception {
            start();
            try {
                return task.call();
            } catch (RuntimeException e) {
                consumeRuntimeException(e);
                return null;
            }
        }
    }

    private static class InnerRunnable extends Task implements Runnable {

        private final Runnable task;

        InnerRunnable(Runnable task, String threadName, Consumer<Exception> exceptionConsumer) {
            super(threadName, exceptionConsumer);
            this.task = task;
        }

        @Override
        public void run() {
            start();
            try {
                task.run();
            } catch (RuntimeException e) {
                consumeRuntimeException(e);
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

    /**
     * // TODO: 24/10/2017 this should register the invoking client with its class name. Make an unregister with no arguments
     *
     * @return
     */
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
        return new HashMap<>(registeredClients);
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
     * <p>
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
     * <p>
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
     * <p>
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
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submit(
            Callable<T> task,
            String threadName) {
        return submit(task, threadName, null);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     * <p>
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submitUnregistered(
            Callable<T> task,
            String threadName) {
        return submitUnregistered(task, threadName, null);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized Future<?> submit(
            Runnable task,
            String threadName) {
        return submit(task, threadName, (Consumer<Exception>) null);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     * <p>
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized Future<?> submitUnregistered(
            Runnable task,
            String threadName) {
        return submitUnregistered(task, threadName, (Consumer<Exception>) null);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submit(
            Runnable task,
            T result,
            String threadName) {
        return submit(task, result, threadName, null);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     * <p>
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submitUnregistered(
            Runnable task,
            T result,
            String threadName) {
        return submitUnregistered(task, result, threadName, null);
    }


    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submit(
            Callable<T> task,
            String threadName,
            Consumer<Exception> exceptionConsumer) {
        return executorService.submit(new InnerCallable<>(task, threadName, exceptionConsumer));
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     * <p>
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submitUnregistered(
            Callable<T> task,
            String threadName,
            Consumer<Exception> exceptionConsumer) {
        String id = registerClient();
        Future<T> future = executorService.submit(new InnerCallable<>(task, threadName, exceptionConsumer));
        unregisterClient(id);
        return future;
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized Future<?> submit(
            Runnable task,
            String threadName,
            Consumer<Exception> exceptionConsumer) {
        return executorService.submit(new InnerRunnable(task, threadName, exceptionConsumer));
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     * <p>
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized Future<?> submitUnregistered(
            Runnable task,
            String threadName,
            Consumer<Exception> exceptionConsumer) {
        String id = registerClient();
        Future<?> future = executorService.submit(new InnerRunnable(task, threadName, exceptionConsumer));
        unregisterClient(id);
        return future;
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submit(
            Runnable task,
            T result,
            String threadName,
            Consumer<Exception> exceptionConsumer) {
        return executorService.submit(new InnerRunnable(task, threadName, exceptionConsumer), result);
    }

    /**
     * Executes a task in parallel mode. A dedicated thread is created for executing this new task. The
     * TaskSemaphore object allows to wait for the finalization of the task
     * <p>
     * Previous registration is not required
     *
     * @param task                  task to execute
     * @return a TaskSemaphore that allows invoker to know when the task has been completed
     * (its subsequent use is optional, only necessary if the parent thread must know when the
     * child thread has finished its task. If not necessary it can just be ignored)
     */
    public static synchronized <T> Future<T> submitUnregistered(
            Runnable task,
            T result,
            String threadName,
            Consumer<Exception> exceptionConsumer) {
        String id = registerClient();
        Future<T> future = executorService.submit(new InnerRunnable(task, threadName, exceptionConsumer), result);
        unregisterClient(id);
        return future;
    }

    /**
     * Executes several tasks in parallel, and waits for all of them to finish
     * <p>
     * Previous registration is not required
     *
     * @param tasks tasks to execute
     */
    public static synchronized void submitBlock(Runnable... tasks) throws ExecutionException, InterruptedException {
        try {
            submitBlock(0L, tasks);
        } catch (TimeoutException e) {
            // ignore, cannot happen
        }
    }

    /**
     * Executes several tasks in parallel, and waits for all of them to finish
     * <p>
     * Previous registration is not required
     *
     * @param timeout timeout to wait for each of the tasks
     * @param tasks   tasks to execute
     */
    public static synchronized void submitBlock(long timeout, Runnable... tasks) throws ExecutionException, InterruptedException, TimeoutException {
        submitBlock(timeout, null, tasks);
    }

    /**
     * Executes several tasks in parallel, and waits for all of them to finish
     * <p>
     * Previous registration is not required
     *
     * @param timeout timeout to wait for each of the tasks
     * @param tasks   tasks to execute
     */
    public static synchronized void submitBlock(long timeout, Consumer<Exception> exceptionConsumer, Runnable... tasks) throws ExecutionException, InterruptedException, TimeoutException {
        String id = registerClient();
        Collection<Future<?>> futures = new ArrayList<>();
        for (Runnable task : tasks) {
            futures.add(executorService.submit(new InnerRunnable(task, ThreadUtil.invokerName(1), exceptionConsumer)));
        }
        for (Future<?> future : futures) {
            if (timeout > 0) {
                future.get(timeout, TimeUnit.MILLISECONDS);
            } else {
                future.get();
            }
        }
        unregisterClient(id);
    }

    /**
     * Executes several tasks in parallel, and waits for all of them to finish
     * <p>
     * Previous registration is not required
     *
     * @param tasks tasks to execute
     */
    public static synchronized void submitBlockUnregistered(Runnable... tasks) throws ExecutionException, InterruptedException {
        // first task runs in this thread. Rest of tasks run in separate threads
        String id = registerClient();
        submitBlock(tasks);
        unregisterClient(id);
    }

    /**
     * Executes several tasks in parallel, and waits for all of them to finish
     * <p>
     * Previous registration is not required
     *
     * @param timeout timeout to wait for each of the tasks
     * @param tasks   tasks to execute
     */
    public static synchronized void submitBlockUnregistered(long timeout, Runnable... tasks) throws ExecutionException, InterruptedException, TimeoutException {
        // first task runs in this thread. Rest of tasks run in separate threads
        String id = registerClient();
        submitBlock(timeout, tasks);
        unregisterClient(id);
    }

    /**
     * Executes several tasks in parallel, and waits for all of them to finish
     * <p>
     * Previous registration is not required
     *
     * @param timeout timeout to wait for each of the tasks
     * @param tasks   tasks to execute
     */
    public static synchronized void submitBlockUnregistered(long timeout, Consumer<Exception> exceptionConsumer, Runnable... tasks) throws ExecutionException, InterruptedException, TimeoutException {
        // first task runs in this thread. Rest of tasks run in separate threads
        String id = registerClient();
        submitBlock(timeout, exceptionConsumer, tasks);
        unregisterClient(id);
    }
}
