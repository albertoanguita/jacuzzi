package jacz.util.AI.evolve;

import jacz.util.bool.SynchedBoolean;
import jacz.util.concurrency.ThreadUtil;
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
public abstract class DynamicState<S, G, P> implements DaemonAction, SimpleTimerAction {

    public interface Transitions<S, G> {

        /**
         * @param state current state
         * @param goal  current goal
         * @return null if no transition required
         */
        Transition<S, G> getTransition(S state, G goal);
    }

    public interface Transition<S, G> {

        /**
         * @return null if synchronous, no active wait required. Non-null and positive if asynchronous and active
         * wait required, or unexpected result that requires waiting for a subsequent try
         */
        Long actOnState(DynamicState<S, G, ?> dynamicState);
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

    protected S state;

    private G goal;

    private final Transitions<S, G> transitions;

    protected final Daemon daemon;

    private final Map<P, Long> registeredStateTimers;

    private Long generalTimer;

    private final Timer stateTimer;

    private final TimerSource<P> timerSource;

    private final Map<P, Runnable> registeredEnterStateHooks;

    private Set<P> activeEnterStateHooks;

    private final Map<P, Runnable> registeredExitStateHooks;

    private Set<P> activeExitStateHooks;


    protected final SequentialTaskExecutor hookExecutor;

    private final SynchedBoolean alive;

    public DynamicState(S state, G initialGoal, Transitions<S, G> transitions) {
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

    public void setGeneralTimer(long millis) {
        generalTimer = millis;
    }

    public void stopGeneralTimer() {
        generalTimer = null;
    }

    public synchronized S state() {
        return state;
    }

    protected synchronized void stateHasChanged() {
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
            if (minTime == null || registeredStateTimers.get(portion) < minTime) {
                minTime = registeredStateTimers.get(portion);
                timerSource.sourceIsPortionTimer(portion);
            }
        }
        // check also the general timer
        if (generalTimer != null) {
            if (timerSource.generalTimer) {
                // timer source found in general timer
                timerSourceIsActive = true;
            }
            if (minTime == null || generalTimer < minTime) {
                minTime = generalTimer;
                timerSource.sourceIsGeneralTimer();
            }
        }
        // check the remaining time
        if (timerRemainingTime != null && timerSourceIsActive) {
            // the timer source is active --> consider the remaining time as possible min time
            if (minTime == null || timerRemainingTime < minTime) {
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
        if (!goal.equals(newGoal)) {
            goal = newGoal;
            daemon.stateChange();
        }
    }

    protected abstract boolean stateIsInPortion(S state, P portion);

    protected abstract Set<P> matchingPortions(S state, Set<P> portions);

    public synchronized void setStateTimer(P portion, long millis) {
        registeredStateTimers.put(portion, millis);
        checkStateTimers();
    }

    public synchronized void removeStateTimer(P portion) {
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
        return transitions.getTransition(state(), goal()) != null;
    }

    @Override
    public boolean solveState() {
        if (alive.isValue()) {
            Transition<S, G> transition = transitions.getTransition(state(), goal());
            if (transition != null) {
                Long wait = transition.actOnState(this);
                if (wait != null) {
                    ThreadUtil.safeSleep(wait);
                }
                return false;
            } else {
                // goal reached
                return true;
            }
        } else {
            // stopped
            return true;
        }
    }

    @Override
    public Long wakeUp(Timer timer) {
        // some reminder timer woke up
        daemon.stateChange();
        return null;
    }

    public void blockUntilStateIsSolved() {
        daemon.blockUntilStateIsSolved();
    }

    public synchronized void stop() {
        daemon.stop();
        hookExecutor.stopAndWaitForFinalization();
    }


}
