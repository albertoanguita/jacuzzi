package org.aanguita.jacuzzi.AI.evolve;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.monitor.Monitor;
import org.aanguita.jacuzzi.concurrency.monitor.StateSolver;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * State and goal must be mutable objects that are never re-assigned
 */
public class EvolvingState<S, G> implements StateSolver, EvolvingStateController<S, G> {

    public interface Transitions<S, G> {

        /**
         * @param state current state
         * @param goal  current goal
         * @return true if state is stable now. False otherwise, so more transitions must be immediately run
         */
        boolean runTransition(S state, G goal, EvolvingStateController<S, G> controller);

        boolean hasReachedGoal(S state, G goal);
    }

    protected S state;

    private G goal;

    private final Transitions<S, G> transitions;

    protected final Monitor monitor;

    private final StateTimers<S> runnableStateTimers;

    private final StateTimers<S> evolveStateTimers;

    private final Runnable evolveTask;

    private final StateHooks<S> stateHooks;

    private final AtomicBoolean alive;

    public EvolvingState(S state, G initialGoal, Transitions<S, G> transitions) {
        this(state, initialGoal, transitions, ThreadUtil.invokerName(1));
    }

    public EvolvingState(S state, G initialGoal, Transitions<S, G> transitions, String threadName) {
        this.state = state;
        this.goal = initialGoal;
        this.transitions = transitions;
        monitor = new Monitor(this, threadName + ".EvolvingState");
        runnableStateTimers = new StateTimers<>(state, threadName + ".EvolvingState/RunnableStateTimers");
        evolveStateTimers = new StateTimers<>(state, threadName + ".EvolvingState/EvolveStateTimers");
        evolveTask = this::evolve;
        stateHooks = new StateHooks<>(state, threadName + ".EvolvingState/StateHooks");
        alive = new AtomicBoolean(true);
    }

    public void evolve() {
        monitor.stateChange();
    }

    public synchronized S state() {
        return state;
    }

    public synchronized void stateHasChanged() {
        evolveStateTimers.stateHasChanged();
        runnableStateTimers.stateHasChanged();
        stateHooks.stateHasChanged();
    }

    public synchronized void setState(S state) {
        this.state = state;
        evolveStateTimers.setState(state);
        runnableStateTimers.setState(state);
        stateHooks.setState(state);
    }

    public synchronized G goal() {
        return goal;
    }

    public synchronized void setGoal(G newGoal) {
        setGoal(newGoal, true);
    }

    public synchronized void setGoal(G newGoal, boolean evolve) {
        if (!goal.equals(newGoal)) {
            goal = newGoal;
            if (evolve) {
                evolve();
            }
        }
    }

    public synchronized void setEvolveStateTimer(S state, long millis) {
        evolveStateTimers.setStateTimer(state, millis, evolveTask);
    }

    public synchronized void setEvolveStateTimer(Predicate<S> stateCondition, long millis) {
        evolveStateTimers.setStateTimer(stateCondition, millis, evolveTask);
    }

    public synchronized void setRunnableStateTimer(S state, long millis, Runnable runnable) {
        runnableStateTimers.setStateTimer(state, millis, runnable);
    }

    public synchronized void setRunnableStateTimer(Predicate<S> stateCondition, long millis, Runnable runnable) {
        runnableStateTimers.setStateTimer(stateCondition, millis, runnable);
    }

    public synchronized void removeEvolveStateTimer(S state) {
        evolveStateTimers.removeStateTimer(state);
    }

    public synchronized void removeEvolveStateTimer(Predicate<S> stateCondition) {
        evolveStateTimers.removeStateTimer(stateCondition);
    }

    public synchronized void removeRunnableStateTimer(S state) {
        runnableStateTimers.removeStateTimer(state);
    }

    public synchronized void removeRunnableStateTimer(Predicate<S> stateCondition) {
        runnableStateTimers.removeStateTimer(stateCondition);
    }

    public synchronized void setEnterStateHook(S state, Runnable task) {
        stateHooks.setEnterStateHook(state, task);
    }

    public synchronized void setEnterStateHook(Predicate<S> stateCondition, Runnable task) {
        stateHooks.setEnterStateHook(stateCondition, task);
    }

    public synchronized void removeEnterStateHook(S state) {
        stateHooks.removeEnterStateHook(state);
    }

    public synchronized void removeEnterStateHook(Predicate<S> stateCondition) {
        stateHooks.removeEnterStateHook(stateCondition);
    }

    public synchronized void setExitStateHook(S state, Runnable task) {
        stateHooks.setExitStateHook(state, task);
    }

    public synchronized void setExitStateHook(Predicate<S> stateCondition, Runnable task) {
        stateHooks.setExitStateHook(stateCondition, task);
    }

    public synchronized void removeExitStateHook(S state) {
        stateHooks.removeExitStateHook(state);
    }

    public synchronized void removeExitStateHook(Predicate<S> stateCondition) {
        stateHooks.removeExitStateHook(stateCondition);
    }


    public synchronized boolean hasReachedGoal() {
        return transitions.hasReachedGoal(state(), goal());
    }

    @Override
    public synchronized boolean solveState() {
        return !alive.get() || transitions.runTransition(state(), goal(), this);
    }

    public void blockUntilGoalReached(long timeToRecheck) {
        monitor.blockUntilStateIsSolved();
        while (!transitions.hasReachedGoal(state(), goal())) {
            ThreadUtil.safeSleep(timeToRecheck);
            monitor.blockUntilStateIsSolved();
        }
    }

    public void stop() {
        evolveStateTimers.stop();
        runnableStateTimers.stop();
        stateHooks.stop();
        monitor.blockUntilStateIsSolved();
        monitor.stop();
        alive.set(false);
    }
}
