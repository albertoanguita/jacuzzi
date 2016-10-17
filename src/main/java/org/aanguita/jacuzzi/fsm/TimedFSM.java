package org.aanguita.jacuzzi.fsm;

import org.aanguita.jacuzzi.concurrency.timer.Timer;
import org.aanguita.jacuzzi.concurrency.timer.TimerAction;

/**
 * FSM with timeout capability
 */
public class TimedFSM<T, Y> extends GenericFSM<T, Y> implements TimerAction {

    private final TimedFSMAction<T, Y> timedFSMAction;

    private final Timer timer;

    public TimedFSM(TimedFSMAction<T, Y> timedFSMAction, long timeoutMillis) {
        this("unnamedTimedFSM", timedFSMAction, timeoutMillis);
    }

    public TimedFSM(String name, TimedFSMAction<T, Y> timedFSMAction, long timeoutMillis) {
        super(name, timedFSMAction);
        this.timedFSMAction = timedFSMAction;
        timer = new Timer(timeoutMillis, this, false, name);
    }

    @Override
    public boolean start() {
        if (super.start()) {
            // if it is active after start, initiate the timer. Otherwise, the timer is not initiated and thus it does not need to be killed later
            timer.reset();
            return true;
        } else {
            timer.stop();
            return false;
        }
    }

    @Override
    public boolean newInput(Y input) {
        timer.stop();
        if (super.newInput(input)) {
            timer.reset();
            return true;
        } else {
            timer.stop();
            return false;
        }
    }

    @Override
    public void stop() {
        super.stop();
        timer.stop();
    }

    @Override
    public Long wakeUp(Timer timer) {
        // the timer has jumped -> no activity reached this FSM during the specified time, the FSM finishes
        // we first check that the FSM is still active
        if (active.getAndSet(false)) {
            active.set(false);
            //timedOut.set(true);
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
