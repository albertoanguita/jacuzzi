package org.aanguita.jacuzzi.fsm;

import org.aanguita.jacuzzi.concurrency.timer.Timer;
import org.aanguita.jacuzzi.concurrency.timer.TimerAction;
import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An FSM with timeout capability. If, after being started, the timed fsm does not receive any input
 * in a given amount of time, the timer dies and the user-provided interface is duly notified
 */
public class TimedFSM<T, Y> extends StringIdClass implements TimerAction {

    private FSM<T, Y> fsm;

    private final TimedFSMAction<T, Y> timedFSMAction;

    private final Timer timer;

    private final AtomicBoolean started;

    private final AtomicBoolean alive;

    public TimedFSM(TimedFSMAction<T, Y> timedFSMAction, long timeoutMillis) {
        this("unnamedTimedFSM", timedFSMAction, timeoutMillis);
    }

    public TimedFSM(String threadName, TimedFSMAction<T, Y> timedFSMAction, long timeoutMillis) {
        this(threadName, timedFSMAction, timeoutMillis, true);
    }

    public TimedFSM(String threadName, TimedFSMAction<T, Y> timedFSMAction, long timeoutMillis, boolean start) {
        this.timedFSMAction = timedFSMAction;
        timer = new Timer(timeoutMillis, this, start, threadName);
        started = new AtomicBoolean(start);
        alive = new AtomicBoolean(true);
        if (start) {
            start();
        }
    }

    public TimedFSM<T, Y> start() {
        if (alive.get() && !started.getAndSet(true)) {
            fsm = new FSM<>(timedFSMAction);
            if (fsm.isActive()) {
                // if it is active after start, initiate the timer. Otherwise, the timer
                // is not initiated and thus it does not need to be killed later
                timer.reset();
            } else {
                timer.stop();
            }
        }
        return this;
    }

    /**
     * Returns the current state of the fsm
     *
     * @return the current state of the fsm
     */
    public T getCurrentState() {
        return fsm != null ? fsm.getCurrentState() : null;
    }

    /**
     * Indicates if this fsm is still active
     *
     * @return true if the fsm is active. False otherwise
     */
    public boolean isActive() {
        return started.get() && fsm.isActive();
    }

    /**
     * Indicates if this fsm is still active
     *
     * @return true if the fsm is active. False otherwise
     */
    public boolean isAlive() {
        return alive.get();
    }


    public boolean newInput(Y input) {
        if (alive.get()) {
            timer.stop();
            start();
            if (fsm.newInput(input)) {
                timer.reset();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void stop() {
        if (alive.getAndSet(false)) {
            timer.stop();
            timedFSMAction.stopped();
        }
    }

    @Override
    public Long wakeUp(Timer timer) {
        // the timer has gone off -> no activity reached this FSM during the specified time, the FSM finishes
        // we first check that the FSM is still active
        if (alive.getAndSet(false)) {
            timedFSMAction.timedOut(fsm.getCurrentState());
        }
        // the timer dies
        return 0L;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == TimedFSM.class && getId().equals(((TimedFSM) obj).getId());
    }


    @Override
    public String toString() {
        return "TimedFSM (id: " + getId() + "): state=" + (getCurrentState() != null ? getCurrentState().toString() : "null");
    }
}
