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
public class GoalRunner<S> implements StateSolver, TimerAction {

    public interface Transitions<S> {

        /**
         * @param state current state
         * @param goal  current goal
         * @return true if state is stable now. False otherwise, so more transitions must be immediately run
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

    private final StateTimers<S> stateTimers;

    private final StateHooks<S> stateHooks;

    private final SimpleSemaphore atDesiredState;

    private final AtomicBoolean alive;

    public GoalRunner(S initialState, Transitions<S> transitions) {
        this(initialState, transitions, ThreadUtil.invokerName(1));
    }

    public GoalRunner(S initialState, Transitions<S> transitions, String threadName) {
        this.state = initialState;
        this.goal = initialState;
        this.transitions = transitions;
        monitor = new Monitor(this, threadName + "/EvolvingState");
        transitionTimer = new Timer(0, this, threadName + "/TransitionTimer");
        stateHooks = new StateHooks<>(state, threadName + "/EvolvingState/StateHooks");
        atDesiredState = new SimpleSemaphore();
        setAtDesiredState();
        alive = new AtomicBoolean(true);
    }

    public void evolve() {
        monitor.stateChange();
    }

    public synchronized S state() {
        return state;
    }

    public synchronized void setState(S state) {
        this.state = state;
        stateHooks.setState(state);
    }

    public synchronized S goal() {
        return goal;
    }

    public synchronized void setGoal(S newGoal) {
        setGoal(newGoal, true);
    }

    public synchronized void setGoal(S newGoal, boolean evolve) {
        if (!goal.equals(newGoal)) {
            goal = newGoal;
            setAtDesiredState();
            if (evolve) {
                evolve();
            }
        }
    }

    private void setAtDesiredState() {
        if (state.equals(goal)) {
            atDesiredState.resume();
        } else {
            atDesiredState.pause();
        }
    }

    public synchronized void addEnterStateHook(S state, Runnable task, boolean useOwnThread) {
        stateHooks.addEnterStateHook(state, task, useOwnThread);
    }

    public synchronized void removeEnterStateHook(S state, Runnable task) {
        stateHooks.removeEnterStateHook(state, task);
    }

    public synchronized void setPeriodicStateHook(S state, Runnable task, long delay) {
        stateHooks.setPeriodicStateHook(state, task, delay);
    }

    public synchronized void removePeriodicStateHook(S state) {
        stateHooks.removePeriodicStateHook(state);
    }

    public synchronized void addExitStateHook(S state, Runnable task, boolean useOwnThread) {
        stateHooks.addExitStateHook(state, task, useOwnThread);
    }

    public synchronized void removeExitStateHook(S state, Runnable task) {
        stateHooks.removeExitStateHook(state, task);
    }

    @Override
    public synchronized boolean hasReachedGoal() {
        return state.equals(goal);
    }

    @Override
    public synchronized boolean solveState() {
        if (!alive.get()) {
            return true;
        }
        transitionTimer.stop();
        S newState = transitions.runTransition(state(), goal());
        if (newState != null) {
            // use this value to replace the old state
            state = newState;
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
        stateTimers.stop();
        stateHooks.stop();
        monitor.stop();
        transitionTimer.stop();
        alive.set(false);
    }
}
