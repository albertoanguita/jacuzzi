package jacz.util.AI.evolve;

import jacz.util.bool.SynchedBoolean;
import jacz.util.concurrency.daemon.Daemon;
import jacz.util.concurrency.daemon.DaemonAction;
import jacz.util.concurrency.task_executor.SequentialTaskExecutor;
import jacz.util.concurrency.timer.SimpleTimerAction;
import jacz.util.concurrency.timer.Timer;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * State and goal must be mutable objects that are never re-assigned
 */
public abstract class EvolvingState<S, G, P> implements DaemonAction, SimpleTimerAction, EvolvingStateController<S, G> {

    public interface Transitions<S, G> {

        /**
         * @param state current state
         * @param goal  current goal
         */
        void runTransition(S state, G goal, EvolvingStateController<S, G> controller);

        boolean hasReachedGoal(S state, G goal);
    }

    private static class TimerSource<P> {

        public boolean generalTimer;

        public P portionTimer;

        private TimerSource<P> stateCopy;

        public TimerSource() {
            init(false, null);
        }

        private TimerSource(boolean generalTimer, P portionTimer) {
            init(generalTimer, portionTimer);
        }

        private void init(boolean generalTimer, P portionTimer) {
            this.generalTimer = generalTimer;
            this.portionTimer = portionTimer;
        }

        public void sourceIsGeneralTimer() {
            generalTimer = true;
            portionTimer = null;
        }

        public void sourceIsPortionTimer(P portion) {
            generalTimer = false;
            portionTimer = portion;
        }

        public void saveState() {
            stateCopy = new TimerSource<>(generalTimer, portionTimer);
        }

        public void restoreState() {
            init(stateCopy.generalTimer, stateCopy.portionTimer);
        }
    }

    /**
     * The action of a timer can be simply invoke the evolve method, run a specific transition, or run a generic runnable
     */
    private static class TimerAction {

        private final long millis;

        private final boolean evolve;

        private final Runnable runnable;

        private TimerAction(long millis, boolean evolve, Runnable runnable) {
            this.millis = millis;
            this.evolve = evolve;
            this.runnable = runnable;
        }

        public static TimerAction evolve(long millis) {
            return new TimerAction(millis, true, null);
        }

        public static TimerAction runnable(long millis, Runnable runnable) {
            return new TimerAction(millis, false, runnable);
        }
    }

    protected S state;

    private G goal;

    private final Transitions<S, G> transitions;

    protected final Daemon daemon;

    private final Map<P, TimerAction> registeredStateTimers;

    private TimerAction generalTimer;

    private final Timer stateTimer;

    private final TimerSource<P> timerSource;

    private final Map<P, Runnable> registeredEnterStateHooks;

    private Set<P> activeEnterStateHooks;

    private final Map<P, Runnable> registeredExitStateHooks;

    private Set<P> activeExitStateHooks;


    protected final SequentialTaskExecutor hookExecutor;

    private final SynchedBoolean alive;
//    private final AtomicBoolean alive2;

    public EvolvingState(S state, G initialGoal, Transitions<S, G> transitions) {
        this.state = state;
        this.goal = initialGoal;
        this.transitions = transitions;
        daemon = new Daemon(this);
        registeredStateTimers = new HashMap<>();
        generalTimer = null;
        stateTimer = new Timer(0, this, false, this.getClass().toString());
        timerSource = new TimerSource<>();
        registeredEnterStateHooks = new HashMap<>();
        activeEnterStateHooks = new HashSet<>();
        registeredExitStateHooks = new HashMap<>();
        activeExitStateHooks = new HashSet<>();
        hookExecutor = new SequentialTaskExecutor();
        alive = new SynchedBoolean(true);
    }

    public void evolve() {
        daemon.stateChange();
    }

    public void setGeneralTimer(long millis) {
        generalTimer = TimerAction.evolve(millis);
    }

    public void setGeneralTimer(long millis, Runnable runnable) {
        generalTimer = TimerAction.runnable(millis, runnable);
    }

    public void stopGeneralTimer() {
        generalTimer = null;
    }

    public synchronized S state() {
        return state;
    }

    public synchronized void stateHasChanged() {
        checkStateTimers();
        checkStateHooks();
    }

    private void checkStateTimers() {
        Long timerRemainingTime = null;
        if (stateTimer.isRunning()) {
            // store the remaining time in case we need it. If the portion (or general timer) that caused the current
            // timer to be running is still active after the following check, we will use the remaining time in the
            // min time computation. Otherwise, it will be discarded
            timerRemainingTime = stateTimer.remainingTime();
            // also, save the state of the timer source. If we finally restore the remaining time, we also have to
            // make sure that the timer source state remains the same
            timerSource.saveState();
        }
        stateTimer.stop();
        boolean timerSourceIsActive = false;
        Long minTime = null;
        Set<P> activeStateTimers = matchingPortions(state, registeredStateTimers.keySet());
        // find the shortest registered timer and start it
        for (P portion : activeStateTimers) {
            if (portion.equals(timerSource.portionTimer)) {
                // timer source found here
                timerSourceIsActive = true;
            }
            if (minTime == null || registeredStateTimers.get(portion).millis < minTime) {
                minTime = registeredStateTimers.get(portion).millis;
                timerSource.sourceIsPortionTimer(portion);
            }
        }
        // check also the general timer
        if (generalTimer != null) {
            if (timerSource.generalTimer) {
                // timer source found in general timer
                timerSourceIsActive = true;
            }
            if (minTime == null || generalTimer.millis < minTime) {
                minTime = generalTimer.millis;
                timerSource.sourceIsGeneralTimer();
            }
        }
        // check the remaining time
        if (timerRemainingTime != null && timerSourceIsActive) {
            // the timer source is active --> consider the remaining time as possible min time
            if (timerRemainingTime < minTime) {
                minTime = timerRemainingTime;
                // set the timer source as the original one
                timerSource.restoreState();
            }
        }
        if (minTime != null) {
            // the timer must be set
            stateTimer.reset(minTime);
        }
    }

    private void checkStateHooks() {
        Set<P> newActiveExitStateHooks = matchingPortions(state, registeredExitStateHooks.keySet());
        Set<P> newActiveEnterStateHooks = matchingPortions(state, registeredEnterStateHooks.keySet());
        Collection<P> exitHooksToInvoke = CollectionUtils.subtract(activeExitStateHooks, newActiveExitStateHooks);
        Collection<P> enterHooksToInvoke = CollectionUtils.subtract(newActiveEnterStateHooks, activeEnterStateHooks);
        for (P exitHookToInvoke : exitHooksToInvoke) {
            // stop timer of old state
            registeredExitStateHooks.get(exitHookToInvoke).run();
        }
        for (P enterHookToInvoke : enterHooksToInvoke) {
            // start new state timer
            registeredEnterStateHooks.get(enterHookToInvoke).run();
        }
        activeEnterStateHooks = newActiveEnterStateHooks;
        activeExitStateHooks = newActiveExitStateHooks;
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

    protected abstract boolean stateIsInPortion(S state, P portion);

    protected abstract Set<P> matchingPortions(S state, Set<P> portions);

    protected synchronized void setStateTimer(P portion, long millis) {
        registeredStateTimers.put(portion, TimerAction.evolve(millis));
        checkStateTimers();
    }

    protected synchronized void setStateTimer(P portion, long millis, Runnable runnable) {
        registeredStateTimers.put(portion, TimerAction.runnable(millis, runnable));
        checkStateTimers();
    }

    protected synchronized void removeStateTimer(P portion) {
        registeredStateTimers.remove(portion);
        checkStateTimers();
    }

    public synchronized void setEnterStateHook(P portion, Runnable task) {
        registeredEnterStateHooks.put(portion, task);
    }

    public synchronized void removeEnterStateHook(P portion) {
        registeredEnterStateHooks.remove(portion);
    }

    public synchronized void setExitStateHook(P portion, Runnable task) {
        registeredExitStateHooks.put(portion, task);
    }

    public synchronized void removeExitStateHook(P portion) {
        registeredExitStateHooks.remove(portion);
    }


    public synchronized boolean hasReachedGoal() {
        return transitions.hasReachedGoal(state(), goal());
    }

    @Override
    public synchronized boolean solveState() {
        if (alive.isValue()) {
            transitions.runTransition(state(), goal(), this);
            return true;
        } else {
            // stopped
            return true;
        }
    }

    @Override
    public Long wakeUp(Timer timer) {
        // some reminder timer woke up
        // find the source and then the action to invoke
        if (timerSource.generalTimer) {
            performTimerAction(generalTimer);
        } else if (timerSource.portionTimer != null) {
            performTimerAction(registeredStateTimers.get(timerSource.portionTimer));
        }
        return null;
    }

    private synchronized void performTimerAction(TimerAction timerAction) {
        if (timerAction.evolve) {
            daemon.stateChange();
        } else {
            timerAction.runnable.run();
        }
    }

    public void blockUntilStateIsSolved() {
        daemon.blockUntilStateIsSolved();
    }

    public synchronized void stop() {
        stateTimer.kill();
        daemon.stop();
        hookExecutor.stopAndWaitForFinalization();
    }


}
