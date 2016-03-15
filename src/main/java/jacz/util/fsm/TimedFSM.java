package jacz.util.fsm;

import jacz.util.concurrency.timer.TimerAction;
import jacz.util.concurrency.timer.Timer;

/**
 * FSM with timeout capability
 */
public class TimedFSM<T, Y> extends GenericFSM<T, Y> implements TimerAction {

    private final TimedFSMAction<T, Y> timedFSMAction;

    private final Timer timer;

    private boolean timedOut;

    public TimedFSM(TimedFSMAction<T, Y> timedFSMAction, long timeoutMillis) {
        this("unnamedTimedFSM", timedFSMAction, timeoutMillis);
    }

    public TimedFSM(String name, TimedFSMAction<T, Y> timedFSMAction, long timeoutMillis) {
        super(name, timedFSMAction);
        this.timedFSMAction = timedFSMAction;
        timedOut = false;
        timer = new Timer(timeoutMillis, this, false, name);
    }

    private void stopTimer() {
        timer.stop();
    }

    private void restartTimer() {
        timer.reset();
    }

    @Override
    public synchronized boolean newInput(Y input) {
        if (!timedOut) {
            // we stop the timer during the new input invocation so we don't get timeouts if the method takes long to complete
            stopTimer();
            boolean active = super.newInput(input);
            if (!active) {
                kill();
            } else {
                restartTimer();
            }
            return active;
        } else {
            return false;
        }
    }

    @Override
    public synchronized boolean start() {
        boolean result = super.start();
        if (result) {
            // if it is active after start, initiate the timer. Otherwise, the timer is not initiated and thus it does not need to be killed later
            restartTimer();
        }
        return result;
    }

    public void stop() {
        super.stop();
        kill();
    }

    public void kill() {
        timer.kill();
        synchronized (this) {
            timedOut = true;
        }
    }

    @Override
    public synchronized Long wakeUp(Timer timer) {
        // the timer has jumped -> no activity reached this FSM during the specified time, the FSM finishes
        // we first check that the FSM is still active
        if (active) {
            timedOut = true;
            timedFSMAction.timedOut(currentState);
        }
        // the timer dies
        return 0L;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == TimedFSM.class && id.equals(((GenericFSM) obj).id);
    }
}
