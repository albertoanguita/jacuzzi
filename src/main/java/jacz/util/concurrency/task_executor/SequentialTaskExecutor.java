package jacz.util.concurrency.task_executor;

import jacz.util.concurrency.daemon.Daemon;
import jacz.util.concurrency.daemon.DaemonAction;
import jacz.util.concurrency.execution_control.PausableElement;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Tasks are executed in a separate thread, sequentially
 */
public class SequentialTaskExecutor implements DaemonAction {

    private static class StopTask implements Task {

        private final PausableElement pausableElement;

        private StopTask(PausableElement pausableElement) {
            this.pausableElement = pausableElement;
        }

        @Override
        public void performTask() {
            pausableElement.resume();
        }

    }

    private final Daemon daemon;

    private final Queue<Task> taskQueue;

    private boolean alive;

    private boolean ignoreFutureTasks;

    public SequentialTaskExecutor() {
        daemon = new Daemon(this);
        taskQueue = new ArrayDeque<>();
        alive = true;
        ignoreFutureTasks = false;
    }

    @Override
    public boolean solveState() {
        Task task;
        synchronized (this) {
            if (!taskQueue.isEmpty()) {
                task = taskQueue.remove();
            } else {
                return true;
            }
        }
        task.performTask();
        return taskQueue.isEmpty();
    }

    public void executeTask(Task task) {
        synchronized (this) {
            if (alive || task instanceof StopTask || ignoreFutureTasks) {
                taskQueue.add(task);
            } else {
                throw new IllegalStateException();
            }
        }
        daemon.stateChange();
    }

    public void stopAndWaitForFinalization() {
        stopAndWaitForFinalization(true);
    }

    public void stopAndWaitForFinalization(boolean ignoreFutureTasks) {
        synchronized (this) {
            alive = false;
            this.ignoreFutureTasks = ignoreFutureTasks;
        }
        PausableElement pausableElement = new PausableElement();
        pausableElement.pause();
        executeTask(new StopTask(pausableElement));
        pausableElement.access();
    }
}
