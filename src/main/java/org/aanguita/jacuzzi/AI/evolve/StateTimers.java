package org.aanguita.jacuzzi.AI.evolve;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.timer.Timer;
import org.aanguita.jacuzzi.concurrency.timer.TimerAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A pool of registered timers organized in a set of states.
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
        this(state, ThreadUtil.invokerName(1));
    }

    public StateTimers(S state, String threadName) {
        this.state = state;
        registeredStateTimers = new HashMap<>();
        timer = new Timer(0, this, false, threadName);
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
        setStateTimer(new SimpleStateCondition<>(state), millis, runnable);
    }

    public synchronized void setStateTimer(StateCondition<S> stateCondition, long millis, Runnable runnable) {
        // remove any previous clashing simple state conditions
        removeStateTimer(stateCondition);
        registeredStateTimers.put(stateCondition, new TimerAction(millis, runnable));
        checkStateTimers();
    }

    public synchronized void removeStateTimer(S state) {
        removeStateTimer(new SimpleStateCondition<>(state));
    }

    public synchronized void removeStateTimer(StateCondition<S> stateCondition) {
        if (stateCondition.equals(timerSource)) {
            // we are removing the currently running timer
            timerSource = null;
        }
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
        boolean minTimeIsForCurrentTimer = false;
        if (timerSource != null && timerSource.isInCondition(state)) {
            // there is an active timer and it is still valid, put it as candidate with its remaining time
            minTime = timer.remainingTime();
            minTimeIsForCurrentTimer = true;
        }
        for (Map.Entry<StateCondition<S>, TimerAction> timers : registeredStateTimers.entrySet()) {
            if (timers.getKey().isInCondition(state)) {
                // valid state condition
                if (minTime == null || timers.getValue().millis < minTime) {
                    timerSource = timers.getKey();
                    minTime = timers.getValue().millis;
                    minTimeIsForCurrentTimer = false;
                }
            }
        }
        // we found a compatible timer -> start it
        if (minTime != null) {
            // the timer must be set
            if (minTimeIsForCurrentTimer) {
                timer.resume();
            } else {
                timer.reset(minTime);
            }
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
