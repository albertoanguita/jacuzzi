package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.SimpleSemaphore;
import org.aanguita.jacuzzi.concurrency.monitor.Monitor;
import org.aanguita.jacuzzi.concurrency.monitor.StateSolver;
import org.aanguita.jacuzzi.concurrency.timer.Timer;
import org.aanguita.jacuzzi.concurrency.timer.TimerAction;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Alberto on 30/10/2016.
 */
public class SimpleGoalExecutor<S> implements StateSolver, TimerAction, GoalExecutor<S> {

    public interface Transitions<S> {

        /**
         * @param state current state
         * @param goal  current goal
         * @return the new state (null if state did not change)
         */
        S runTransition(S state, S goal);

        /**
         * Describes the goal runner behavior in each state, with a specific goal
         *
         * @param state the current state
         * @param goal  the desired state
         * @return - a negative value if the runner should immediately try to transition to the desired goal
         * - null if the runner should not perform any transitions, and rather wait for external input
         * - a positive value if the runner should wait some time (ms) until transitioning again
         * In any case, if the current state equals the goal, this method will not even be invoked
         */
        Long behaviorInState(S state, S goal);
    }

    protected S state;

    private S goal;

    private final Transitions<S> transitions;

    protected final Monitor monitor;

    private final Timer transitionTimer;

    private final StateHooks<S> stateHooks;

    private final SimpleSemaphore atDesiredState;

    private final AtomicBoolean alive;

    public SimpleGoalExecutor(S initialState, Transitions<S> transitions) {
        this(initialState, transitions, ThreadUtil.invokerName(1));
    }

    public SimpleGoalExecutor(S initialState, Transitions<S> transitions, String threadName) {
        this.state = initialState;
        this.goal = initialState;
        this.transitions = transitions;
        monitor = new Monitor(this, threadName + ".GoalExecutor");
        transitionTimer = new Timer(0, this, threadName + ".GoalExecutor.TransitionTimer");
        stateHooks = new StateHooks<>(state, threadName + ".GoalExecutor.StateHooks");
        atDesiredState = new SimpleSemaphore();
        setAtDesiredState();
        alive = new AtomicBoolean(true);
    }


    public synchronized S getState() {
        return state;
    }

    public synchronized S getGoal() {
        return goal;
    }

    @Override
    public synchronized void setGoal(S newGoal) {
        if (!goal.equals(newGoal)) {
            goal = newGoal;
            setAtDesiredState();
        }
    }

    @Override
    public synchronized boolean hasReachedGoal() {
        return state.equals(goal);
    }

    public void evolve() {
        monitor.stateChange();
    }

    private void setAtDesiredState() {
        if (state.equals(goal)) {
            atDesiredState.resume();
        } else {
            atDesiredState.pause();
        }
    }

    @Override
    public synchronized void addEnterStateHook(S state, Runnable task) {
        stateHooks.addEnterStateHook(state, task);
    }

    @Override
    public synchronized void removeEnterStateHook(S state, Runnable task) {
        stateHooks.removeEnterStateHook(state, task);
    }

    @Override
    public synchronized void setPeriodicStateHook(S state, Runnable task, long delay) {
        stateHooks.setPeriodicStateHook(state, task, delay);
    }

    @Override
    public synchronized void removePeriodicStateHook(S state) {
        stateHooks.removePeriodicStateHook(state);
    }

    @Override
    public synchronized void addExitStateHook(S state, Runnable task) {
        stateHooks.addExitStateHook(state, task);
    }

    @Override
    public synchronized void removeExitStateHook(S state, Runnable task) {
        stateHooks.removeExitStateHook(state, task);
    }

    @Override
    public synchronized boolean solveState() {
        if (!alive.get()) {
            return true;
        }
        transitionTimer.stop();
        S newState = transitions.runTransition(getState(), getGoal());
        if (newState != null && newState != state) {
            // use this value to replace the old state
            state = newState;
            stateHooks.setState(newState);
        }
        setAtDesiredState();
        // check if we have reached the desired state
        if (state.equals(goal)) {
            return true;
        }
        // check the behavior for the current state
        Long behavior = transitions.behaviorInState(state, goal);
        if (behavior == null) {
            // do nothing else
            return true;
        } else if (behavior < 0) {
            // transit right now
            return false;
        } else {
            // wait some time before next transition
            transitionTimer.reset(behavior);
            return true;
        }
    }

    @Override
    public synchronized Long wakeUp(Timer timer) {
        evolve();
        return 0L;
    }

    public void blockUntilGoalReached() {
        atDesiredState.access();
    }

    public void stop() {
        stateHooks.stop();
        monitor.stop();
        transitionTimer.stop();
        alive.set(false);
    }
}
