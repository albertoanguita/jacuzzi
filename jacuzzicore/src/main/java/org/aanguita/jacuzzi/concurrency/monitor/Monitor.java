package org.aanguita.jacuzzi.concurrency.monitor;

import org.aanguita.jacuzzi.concurrency.Barrier;
import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * This class implements a Monitor which is waiting for events and asynchronously performs actions upon such events.
 * <p/>
 * The events modify a "wish state". The monitor always tries to satisfy that wish
 */
public class Monitor {

    private static class MonitorTask implements Runnable {

        private final Monitor monitor;

        private MonitorTask(Monitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void run() {
            boolean finished = false;
            while (!finished) {
                if (!monitor.alive.get()) {
                    // the monitor was stopped -> ignore action and request finish
                    finished = monitor.requestKillDaemonThread();
                } else {
                    finished = monitor.executeAction();
                    if (finished) {
                        // check if we can really finish (if there are unresolved state changes, we must keep working)
                        finished = monitor.requestKillDaemonThread();
                    }
                }
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);

    /**
     * StateSolver implementation for resolving state changes
     */
    private final StateSolver stateSolver;

    /**
     * Future object for the currently running thread (or null if no thread is running)
     */
    private Future future;

    /**
     * Flag indicating if the state has changed (true = there is a change that must be solved)
     */
    private final AtomicBoolean stateChangeFlag;

    /**
     * Flag indicating if a monitor thread currently exists
     */
    private final AtomicBoolean daemonThreadFlag;

    /**
     * A block element for client that want to wait until the state is solved
     */
    private final Barrier blockUntilStateSolve;

    private final AtomicBoolean alive;

    private final String threadName;

    private final Consumer<Exception> exceptionConsumer;

    private final String threadExecutorClientId;


    public Monitor(StateSolver stateSolver) {
        this(stateSolver, ThreadUtil.invokerName(1));
    }

    public Monitor(StateSolver stateSolver, String threadName) {
        this(stateSolver, threadName, null);
    }

    public Monitor(StateSolver stateSolver, String threadName, Consumer<Exception> exceptionConsumer) {
        this.stateSolver = stateSolver;
        future = null;
        stateChangeFlag = new AtomicBoolean(false);
        daemonThreadFlag = new AtomicBoolean(false);
        blockUntilStateSolve = new Barrier();
        alive = new AtomicBoolean(true);
        this.threadName = threadName;
        threadExecutorClientId = ThreadExecutor.registerClient(this.getClass().getName() + "(" + threadName + ")");
        this.exceptionConsumer = exceptionConsumer;
    }

    /**
     * Indicates a state change that must be solved by the monitor
     */
    public synchronized void stateChange() {
        if (alive.get()) {
            stateChangeFlag.set(true);
            blockUntilStateSolve.pause();
            // check if we need to create a new monitor thread
            if (!daemonThreadFlag.get()) {
                daemonThreadFlag.set(true);
                stateChangeFlag.set(false);
                future = ThreadExecutor.submit(new MonitorTask(this), threadName + ".Monitor");
            }
        }
    }

    public synchronized boolean isStateSolved() {
        return !stateChangeFlag.get() && !daemonThreadFlag.get();
    }

    public void blockUntilStateIsSolved() {
        blockUntilStateSolve.access();
    }

    public void blockUntilStateIsSolved(long timeout) throws TimeoutException {
        blockUntilStateSolve.access(timeout);
    }

    public void stop() {
        if (alive.getAndSet(false)) {
            if (future != null) {
                try {
                    future.get();
                } catch (Exception e) {
                    // ignore exceptions
                }
            }
            ThreadExecutor.unregisterClient(threadExecutorClientId);
        }
    }

    /**
     * Requests to kill the monitor thread. It will check for unresolved state changes
     *
     * @return true if the monitor thread can finish, false otherwise
     */
    private synchronized boolean requestKillDaemonThread() {
        // if there are no state changes active, we allow to kill the thread
        if (!stateChangeFlag.get() || !alive.get()) {
            // there are no registered state changes, the thread can finish ok
            // or the monitor has been stopped
            daemonThreadFlag.set(false);
            future = null;
            blockUntilStateSolve.resume();
            return true;
        } else {
            // there is an unresolved state change, thread must keep working
            stateChangeFlag.set(false);
            return false;
        }
    }

    private boolean executeAction() {
        try {
            return stateSolver.solveState();
        } catch (Exception e) {
            //unexpected exception obtained. Print error and terminate
            if (logger.isErrorEnabled()) {
                logger.error("UNEXPECTED EXCEPTION THROWN BY MONITOR IMPLEMENTATION. PLEASE CORRECT THE CODE SO NO EXCEPTIONS ARE THROWN AT THIS LEVEL", e);
            }
            if (exceptionConsumer != null) {
                exceptionConsumer.accept(e);
            }
            return true;
        }
    }
}
