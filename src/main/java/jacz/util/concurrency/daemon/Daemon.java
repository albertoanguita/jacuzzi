package jacz.util.concurrency.daemon;

import jacz.util.concurrency.execution_control.TrafficControl;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskSemaphore;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class implements a Daemon which is waiting for events and asynchronously performs actions upon such events.
 * <p/>
 * The events modify a "wish state". The daemon always tries to satisfy that wish
 */
public class Daemon {

    private static class DaemonTask implements Runnable {

        private final Daemon daemon;

        private DaemonTask(Daemon daemon) {
            this.daemon = daemon;
        }

        @Override
        public void run() {
            boolean finished = false;
            while (!finished) {
                if (!daemon.alive.get()) {
                    // the daemon was stopped -> ignore action and request finish
                    finished = daemon.requestKillDaemonThread();
                } else {
                    finished = daemon.executeAction();
                    if (finished) {
                        // check if we can really finish (if there are unresolved state changes, we must keep working)
                        finished = daemon.requestKillDaemonThread();
                    }
                }
            }
        }
    }

    /**
     * DaemonAction implementation for resolving state changes
     */
    private final DaemonAction daemonAction;

    /**
     * TaskSemaphore for the currently running thread (or null if no thread is running)
     */
    private TaskSemaphore taskSemaphore;

    /**
     * Flag indicating if the state has changed (true = there is a change that must be solved)
     */
    private final AtomicBoolean stateChangeFlag;

    /**
     * Flag indicating if a daemon thread currently exists
     */
    private final AtomicBoolean daemonThreadFlag;

    /**
     * A block element for client that want to wait until the state is solved
     */
    private final TrafficControl blockUntilStateSolve;

    private final AtomicBoolean alive;


    public Daemon(DaemonAction daemonAction) {
        this.daemonAction = daemonAction;
        taskSemaphore = null;
        stateChangeFlag = new AtomicBoolean(false);
        daemonThreadFlag = new AtomicBoolean(false);
        blockUntilStateSolve = new TrafficControl();
        alive = new AtomicBoolean(true);
    }

    /**
     * Indicates a state change that must be solved by the daemon
     */
    public synchronized void stateChange() {
        stateChangeFlag.set(true);
        blockUntilStateSolve.pause();
        // check if we need to create a new daemon thread
        if (!daemonThreadFlag.get()) {
            daemonThreadFlag.set(true);
            stateChangeFlag.set(false);
            taskSemaphore = ParallelTaskExecutor.executeTask(new DaemonTask(this));
        }
    }

    /**
     * Interrupts the daemon thread (if any thread running)
     */
    public synchronized void interrupt() {
        if (taskSemaphore != null) {
            taskSemaphore.interrupt();
        }
    }

    public synchronized boolean isStateSolved() {
        return !stateChangeFlag.get() && !daemonThreadFlag.get();
    }

    public void blockUntilStateIsSolved() {
        blockUntilStateSolve.access();
    }

    public void stop() {
        alive.set(false);
        interrupt();
    }

    /**
     * Requests to kill the daemon thread. It will check for unresolved state changes
     *
     * @return true if the daemon thread can finish, false otherwise
     */
    private synchronized boolean requestKillDaemonThread() {
        // if there are no state changes active, we allow to kill the thread
        if (!stateChangeFlag.get() || !alive.get()) {
            // there are no registered state changes, the thread can finish ok
            // or the daemon has been stopped
            daemonThreadFlag.set(false);
            taskSemaphore = null;
            blockUntilStateSolve.resume();
            return true;
        } else {
            // there is an unresolved state change, thread must keep working
            stateChangeFlag.set(false);
            return false;
        }
    }

    private boolean executeAction() {
        return daemonAction.solveState();
    }
}
