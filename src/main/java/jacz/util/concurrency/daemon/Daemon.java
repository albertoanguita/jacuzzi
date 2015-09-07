package jacz.util.concurrency.daemon;

import jacz.util.bool.MutableBoolean;
import jacz.util.concurrency.task_executor.ParallelTask;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskFinalizationIndicator;

/**
 * This class implements a Daemon which is waiting for events and asynchronously performs actions upon such events.
 * <p/>
 * The events modify a "wish state". The daemon always tries to satisfy that wish
 */
public class Daemon {

    private static class DaemonTask implements ParallelTask {

        private final Daemon daemon;

        private DaemonTask(Daemon daemon) {
            this.daemon = daemon;
        }

        @Override
        public void performTask() {
            boolean finished = false;
            while (!finished) {
                finished = daemon.executeAction();
                if (finished) {
                    // check if we can really finish (if there are unresolved state changes, we must keep working)
                    finished = daemon.requestKillDaemonThread();
                }
            }
        }
    }

    /**
     * DaemonAction implementation for resolving state changes
     */
    private final DaemonAction daemonAction;

    /**
     * TaskFinalizationIndicator for the currently running thread (or null if no thread is running)
     */
    private TaskFinalizationIndicator taskFinalizationIndicator;

    /**
     * Flag indicating if the state has changed (true = there is a change that must be solved)
     */
    private final MutableBoolean stateChangeFlag;

    /**
     * Flag indicating if a daemon thread currently exists
     */
    private final MutableBoolean daemonThreadFlag;


    public Daemon(DaemonAction daemonAction) {
        this.daemonAction = daemonAction;
        taskFinalizationIndicator = null;
        stateChangeFlag = new MutableBoolean(false);
        daemonThreadFlag = new MutableBoolean(false);
    }

    /**
     * Indicates a state change that must be solved by the daemon
     */
    public synchronized void stateChange() {
        stateChangeFlag.setValue(true);
        // check if we need to create a new daemon thread
        if (!daemonThreadFlag.isValue()) {
            daemonThreadFlag.setValue(true);
            stateChangeFlag.setValue(false);
            taskFinalizationIndicator = ParallelTaskExecutor.executeTask(new DaemonTask(this));
        }
    }

    /**
     * Interrupts the daemon thread (if any thread running)
     */
    public synchronized void interrupt() {
        if (taskFinalizationIndicator != null) {
            taskFinalizationIndicator.interrupt();
        }
    }

    /**
     * Requests to kill the daemon thread. It will check for unresolved state changes
     *
     * @return true if the daemon thread can finish, false otherwise
     */
    private synchronized boolean requestKillDaemonThread() {
        // if there are no state changes active, we allow to kill the thread
        if (!stateChangeFlag.isValue()) {
            // there are no registered state changes, the thread can finish ok
            daemonThreadFlag.setValue(false);
            taskFinalizationIndicator = null;
            return true;
        } else {
            // there is an unresolved state change, thread must keep working
            stateChangeFlag.setValue(false);
            return false;
        }
    }

    private boolean executeAction() {
        return daemonAction.solveState();
    }
}
