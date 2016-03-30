package jacz.util.AI.evolve;

import jacz.util.concurrency.task_executor.SequentialTaskExecutor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * Created by Alberto on 20/03/2016.
 */
public class StateHooks<S> {

    private S state;

    private final Map<StateCondition<S>, Runnable> registeredEnterStateHooks;

    private Set<StateCondition<S>> activeEnterStateHooks;

    private final Map<StateCondition<S>, Runnable> registeredExitStateHooks;

    private Set<StateCondition<S>> activeExitStateHooks;

    private final boolean executeInParallel;

    private final SequentialTaskExecutor hookExecutor;


    public StateHooks(S state) {
        this(state, true);
    }

    public StateHooks(S state, boolean executeInParallel) {
        this.state = state;
        registeredEnterStateHooks = new HashMap<>();
        activeEnterStateHooks = new HashSet<>();
        registeredExitStateHooks = new HashMap<>();
        activeExitStateHooks = new HashSet<>();
        this.executeInParallel = executeInParallel;
        if (executeInParallel) {
            hookExecutor = new SequentialTaskExecutor();
        } else {
            hookExecutor = null;
        }
    }

    public synchronized void stateHasChanged() {
        checkStateHooks();
    }

    public synchronized void setState(S state) {
        this.state = state;
        checkStateHooks();
    }

    public synchronized void setEnterStateHook(S state, Runnable task) {
        registeredEnterStateHooks.put(new SimpleStateCondition<>(state), task);
    }

    public synchronized void setEnterStateHook(StateCondition<S> stateCondition, Runnable task) {
        registeredEnterStateHooks.put(stateCondition, task);
    }

    public synchronized void removeEnterStateHook(S state) {
        registeredEnterStateHooks.remove(new SimpleStateCondition<>(state));
    }

    public synchronized void removeEnterStateHook(StateCondition<S> stateCondition) {
        registeredEnterStateHooks.remove(stateCondition);
    }

    public synchronized void setExitStateHook(S state, Runnable task) {
        registeredExitStateHooks.put(new SimpleStateCondition<>(state), task);
    }

    public synchronized void setExitStateHook(StateCondition<S> stateCondition, Runnable task) {
        registeredExitStateHooks.put(stateCondition, task);
    }

    public synchronized void removeExitStateHook(S state) {
        registeredExitStateHooks.remove(new SimpleStateCondition<>(state));
    }

    public synchronized void removeExitStateHook(StateCondition<S> stateCondition) {
        registeredExitStateHooks.remove(stateCondition);
    }

    private void checkStateHooks() {
        Set<StateCondition<S>> newActiveExitStateHooks = matchingStateConditions(registeredExitStateHooks.keySet());
        Set<StateCondition<S>> newActiveEnterStateHooks = matchingStateConditions(registeredEnterStateHooks.keySet());
        Collection<StateCondition<S>> exitHooksToInvoke = CollectionUtils.subtract(activeExitStateHooks, newActiveExitStateHooks);
        Collection<StateCondition<S>> enterHooksToInvoke = CollectionUtils.subtract(newActiveEnterStateHooks, activeEnterStateHooks);
        for (StateCondition<S> exitHookToInvoke : exitHooksToInvoke) {
            // stop timer of old state
            runTask(registeredExitStateHooks.get(exitHookToInvoke));
        }
        for (StateCondition<S> enterHookToInvoke : enterHooksToInvoke) {
            // start new state timer
            runTask(registeredEnterStateHooks.get(enterHookToInvoke));
        }
        activeEnterStateHooks = newActiveEnterStateHooks;
        activeExitStateHooks = newActiveExitStateHooks;
    }

    private Set<StateCondition<S>> matchingStateConditions(Set<StateCondition<S>> stateConditions) {
        Set<StateCondition<S>> matchingStateConditions = new HashSet<>();
        for (StateCondition<S> stateCondition : stateConditions) {
            if (stateCondition.isInCondition(state)) {
                matchingStateConditions.add(stateCondition);
            }
        }
        return matchingStateConditions;
    }

    private void runTask(Runnable runnable) {
        if (executeInParallel) {
            hookExecutor.executeTask(runnable);
        } else {
            runnable.run();
        }
    }

    public void stop() {
        if (hookExecutor != null) {
            hookExecutor.stopAndWaitForFinalization();
        }
    }
}
