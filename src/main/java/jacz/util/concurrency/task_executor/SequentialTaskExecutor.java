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

    private static class StopTask implements ParallelTask {

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

    private final Queue<ParallelTask> taskQueue;

    private boolean alive;

    public SequentialTaskExecutor() {
        daemon = new Daemon(this);
        taskQueue = new ArrayDeque<>();
        alive = true;
    }

    @Override
    public boolean solveState() {
        ParallelTask parallelTask;
        synchronized (this) {
            if (!taskQueue.isEmpty()) {
                parallelTask = taskQueue.remove();
            } else {
                return true;
            }
        }
        parallelTask.performTask();
        return taskQueue.isEmpty();
    }

    public void executeTask(ParallelTask parallelTask) {
        synchronized (this) {
            if (alive || parallelTask instanceof StopTask) {
                taskQueue.add(parallelTask);
            } else {
                throw new IllegalStateException();
            }
        }
        daemon.stateChange();
    }

    public void stopAndWaitForFinalization() {
        synchronized (this) {
            alive = false;
        }
        PausableElement pausableElement = new PausableElement();
        pausableElement.pause();
        executeTask(new StopTask(pausableElement));
        pausableElement.access();
    }
}
