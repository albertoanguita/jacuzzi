package jacz.util.AI.evolve;

import jacz.util.concurrency.timer.Timer;
import jacz.util.concurrency.timer.TimerAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Alberto on 20/03/2016.
 */
public class StateTimers<S> implements TimerAction {

    /**
     * The action of a timer can be simply invoke the evolve method, run a specific transition, or run a generic runnable
     */
    private static class TimerAction {

        private final long millis;

        private final Runnable runnable;

        private TimerAction(long millis, Runnable runnable) {
            this.millis = millis;
            this.runnable = runnable;
        }
    }

    private S state;

    private final Map<StateCondition<S>, TimerAction> registeredStateTimers;

    private final Timer timer;

    private StateCondition<S> timerSource;

    private final AtomicBoolean alive;

    public StateTimers(S state) {
        this.state = state;
        registeredStateTimers = new HashMap<>();
        timer = new Timer(0, this, false, this.getClass().toString());
        timerSource = null;
        alive = new AtomicBoolean(true);
    }

    public synchronized void stateHasChanged() {
        checkStateTimers();
    }

    public synchronized void setState(S newState) {
        state = newState;
        checkStateTimers();
    }

    public synchronized void setStateTimer(S state, long millis, Runnable runnable) {
        // remove any previous clashing simple state conditions
        removeStateTimer(state);
        registeredStateTimers.put(new SimpleStateCondition<>(state), new TimerAction(millis, runnable));
        checkStateTimers();
    }

    public synchronized void setStateTimer(StateCondition<S> stateCondition, long millis, Runnable runnable) {
        registeredStateTimers.put(stateCondition, new TimerAction(millis, runnable));
        checkStateTimers();
    }

    public synchronized void removeStateTimer(S state) {
        registeredStateTimers.remove(new SimpleStateCondition<>(state));
        checkStateTimers();
    }

    public synchronized void removeStateTimer(StateCondition<S> stateCondition) {
        registeredStateTimers.remove(stateCondition);
        checkStateTimers();
    }

    /**
     * This method is always invoked after the state has evolved (either due to a timer going off, or the user manually
     * requesting an evolve action). In the former case, the timer is paused. In the latter, it is stopped.
     * This method will have to determine if it must be restarted again.
     */
    private void checkStateTimers() {
        timer.stop();
        Long minTime = null;
        if (timerSource != null && timerSource.isInCondition(state)) {
            // there is an active timer and it is still valid, put it as candidate with its remaining time
            minTime = timer.remainingTime();
        }
        for (Map.Entry<StateCondition<S>, TimerAction> timers : registeredStateTimers.entrySet()) {
            if (timers.getKey().isInCondition(state)) {
                // valid state condition
                if (minTime == null || timers.getValue().millis < minTime) {
                    timerSource = timers.getKey();
                    minTime = timers.getValue().millis;
                }
            }
        }
        // we found a compatible timer -> start it
        if (minTime != null) {
            // the timer must be set
            timer.reset(minTime);
        } else {
            // no compatible timers
            timerSource = null;
        }
    }

    @Override
    public synchronized Long wakeUp(Timer timer) {
        if (alive.get() && timerSource != null) {
            registeredStateTimers.get(timerSource).runnable.run();
        }
        return null;
    }

    public synchronized void stop() {
        alive.set(false);
        timer.kill();
    }
}
