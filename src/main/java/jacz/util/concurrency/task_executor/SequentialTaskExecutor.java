package jacz.util.concurrency.task_executor;

import jacz.util.concurrency.daemon.Daemon;
import jacz.util.concurrency.daemon.DaemonAction;
import jacz.util.concurrency.execution_control.TrafficControl;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Tasks are executed in a separate thread, sequentially
 */
public class SequentialTaskExecutor implements DaemonAction {

    private static class StopTask implements Runnable {

        private final TrafficControl trafficControl;

        private StopTask(TrafficControl trafficControl) {
            this.trafficControl = trafficControl;
        }

        @Override
        public void run() {
            trafficControl.resume();
        }

    }

    private final Daemon daemon;

    private final Queue<Runnable> taskQueue;

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
        Runnable task;
        synchronized (this) {
            if (!taskQueue.isEmpty()) {
                task = taskQueue.remove();
            } else {
                return true;
            }
        }
        task.run();
        return taskQueue.isEmpty();
    }

    public void executeTask(Runnable task) {
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
        TrafficControl trafficControl = new TrafficControl();
        trafficControl.pause();
        executeTask(new StopTask(trafficControl));
        trafficControl.access();
    }
}
