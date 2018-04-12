package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.monitor.Monitor;
import org.aanguita.jacuzzi.concurrency.monitor.StateSolver;
import org.aanguita.jacuzzi.concurrency.timer.Timer;
import org.aanguita.jacuzzi.concurrency.timer.TimerAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by Alberto on 30/10/2016.
 */
public class SimpleGoalExecutor<S> extends AbstractGoalExecutor<S> implements StateSolver, TimerAction {

    public interface Transitions<S> {

        /**
         * @param state current state
         * @param goal  current goal
         * @return the new state (null if state did not change)
         */
        S runTransition(S state, S goal);
    }

    private final Transitions<S> transitions;

    protected final Monitor monitor;

    private final Timer transitionTimer;

    private Long globalBehavior;

    private final Map<S, Long> stateBehavior;

    private final Map<S, Map<S, Long>> stateGoalBehavior;

    private final AtomicBoolean alive;

    public SimpleGoalExecutor(S initialState, Transitions<S> transitions) {
        this(initialState, transitions, ThreadUtil.invokerName(1));
    }

    public SimpleGoalExecutor(S initialState, Transitions<S> transitions, String threadName) {
        this(initialState, transitions, threadName, null);
    }

    public SimpleGoalExecutor(S initialState, Transitions<S> transitions, String threadName, Consumer<Exception> exceptionConsumer) {
        super(initialState, threadName, exceptionConsumer);
        this.transitions = transitions;
        monitor = new Monitor(this, threadName + ".GoalExecutor", exceptionConsumer);
        transitionTimer = new Timer(0, this, false, threadName + ".GoalExecutor.TransitionTimer", exceptionConsumer);
        globalBehavior = null;
        stateBehavior = new HashMap<>();
        stateGoalBehavior = new HashMap<>();
        alive = new AtomicBoolean(true);
    }

    @Override
    public synchronized void setState(S newState) {
        if (!state.equals(newState)) {
            super.setState(newState);
            // check the behavior for the new state
            Long behavior = getCurrentBehavior();
            if (behavior != null && behavior <= 0) {
                // transit right now
                evolve();
            } else if (behavior != null && behavior > 0) {
                // wait some time before next transition
                transitionTimer.reset(behavior);
            }
        }
    }

    @Override
    public synchronized void setGoal(S newGoal) {
        super.setGoal(newGoal);
        evolve();
    }

    public void evolve() {
        monitor.stateChange();
    }

    public synchronized void setGlobalBehavior(long millis) {
        globalBehavior = millis;
    }

    public synchronized void setBehavior(S state, long millis) {
        stateBehavior.put(state, millis);
    }

    public synchronized void setBehavior(S state, S goal, long millis) {
        if (stateGoalBehavior.containsKey(state)) {
            stateGoalBehavior.put(state, new HashMap<>());
        }
        stateGoalBehavior.get(state).put(goal, millis);
    }

    public synchronized void removeGlobalBehavior() {
        globalBehavior = null;
    }

    public synchronized void removeBehavior(S state) {
        stateBehavior.remove(state);
    }

    public synchronized void removeBehavior(S state, S goal) {
        if (stateGoalBehavior.containsKey(state)) {
            stateGoalBehavior.get(state).remove(goal);
        }
    }

    @Override
    public synchronized boolean solveState() {
        if (!alive.get()) {
            return true;
        }
        transitionTimer.stop();
        S newState = transitions.runTransition(getState(), getGoal());
        if (newState != null) {
            // use this value to replace the old state
            setState(newState);
        }
        // check if we have reached the desired state
        if (state.equals(goal)) {
            return true;
        }
        // check the behavior for the current state
        Long behavior = getCurrentBehavior();
        if (behavior == null) {
            // do nothing else
            return true;
        } else if (behavior <= 0) {
            // transit right now
            return false;
        } else {
            // wait some time before next transition
            transitionTimer.reset(behavior);
            return true;
        }
    }

    private Long getCurrentBehavior() {
        Long currentBehavior = globalBehavior;
        if (stateBehavior.containsKey(state)) {
            currentBehavior = stateBehavior.get(state);
        }
        if (stateGoalBehavior.containsKey(state) && stateGoalBehavior.get(state).containsKey(goal)) {
            currentBehavior = stateGoalBehavior.get(state).get(goal);
        }
        return currentBehavior;
    }

    @Override
    public synchronized Long wakeUp(Timer timer) {
        evolve();
        return 0L;
    }

    @Override
    public void stop() {
        super.stop();
        monitor.stop();
        transitionTimer.stop();
        alive.set(false);
    }
}
