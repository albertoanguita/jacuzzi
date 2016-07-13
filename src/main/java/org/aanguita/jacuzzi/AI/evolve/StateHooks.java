package org.aanguita.jacuzzi.AI.evolve;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 * Created by Alberto on 20/03/2016.
 */
public class StateHooks<S> {

    private S state;

    private final Map<Predicate<S>, Runnable> registeredEnterStateHooks;

    private Set<Predicate<S>> activeEnterStateHooks;

    private final Map<Predicate<S>, Runnable> registeredExitStateHooks;

    private Set<Predicate<S>> activeExitStateHooks;

    private final boolean executeInParallel;

    private final ExecutorService hookExecutor;

    private final String threadName;


    public StateHooks(S state) {
        this(state, true);
    }

    public StateHooks(S state, String threadName) {
        this(state, true, threadName);
    }

    public StateHooks(S state, boolean executeInParallel) {
        this(state, executeInParallel, ThreadUtil.invokerName(1));
    }

    public StateHooks(S state, boolean executeInParallel, String threadName) {
        this.state = state;
        registeredEnterStateHooks = new HashMap<>();
        activeEnterStateHooks = new HashSet<>();
        registeredExitStateHooks = new HashMap<>();
        activeExitStateHooks = new HashSet<>();
        this.executeInParallel = executeInParallel;
        if (executeInParallel) {
            hookExecutor = Executors.newSingleThreadExecutor();
        } else {
            hookExecutor = null;
        }
        this.threadName = threadName;
    }

    public synchronized void stateHasChanged() {
        checkStateHooks();
    }

    public synchronized void setState(S state) {
        this.state = state;
        checkStateHooks();
    }

    public synchronized void setEnterStateHook(S state, Runnable task) {
        registeredEnterStateHooks.put(new CompareStatePredicate<>(state), task);
    }

    public synchronized void setEnterStateHook(Predicate<S> stateCondition, Runnable task) {
        registeredEnterStateHooks.put(stateCondition, task);
    }

    public synchronized void removeEnterStateHook(S state) {
        registeredEnterStateHooks.remove(new CompareStatePredicate<>(state));
    }

    public synchronized void removeEnterStateHook(Predicate<S> stateCondition) {
        registeredEnterStateHooks.remove(stateCondition);
    }

    public synchronized void setExitStateHook(S state, Runnable task) {
        registeredExitStateHooks.put(new CompareStatePredicate<>(state), task);
    }

    public synchronized void setExitStateHook(Predicate<S> stateCondition, Runnable task) {
        registeredExitStateHooks.put(stateCondition, task);
    }

    public synchronized void removeExitStateHook(S state) {
        registeredExitStateHooks.remove(new CompareStatePredicate<>(state));
    }

    public synchronized void removeExitStateHook(Predicate<S> stateCondition) {
        registeredExitStateHooks.remove(stateCondition);
    }

    private void checkStateHooks() {
        Set<Predicate<S>> newActiveExitStateHooks = matchingStateConditions(registeredExitStateHooks.keySet());
        Set<Predicate<S>> newActiveEnterStateHooks = matchingStateConditions(registeredEnterStateHooks.keySet());
        Collection<Predicate<S>> exitHooksToInvoke = CollectionUtils.subtract(activeExitStateHooks, newActiveExitStateHooks);
        Collection<Predicate<S>> enterHooksToInvoke = CollectionUtils.subtract(newActiveEnterStateHooks, activeEnterStateHooks);
        for (Predicate<S> exitHookToInvoke : exitHooksToInvoke) {
            // stop timer of old state
            runTask(registeredExitStateHooks.get(exitHookToInvoke));
        }
        for (Predicate<S> enterHookToInvoke : enterHooksToInvoke) {
            // start new state timer
            runTask(registeredEnterStateHooks.get(enterHookToInvoke));
        }
        activeEnterStateHooks = newActiveEnterStateHooks;
        activeExitStateHooks = newActiveExitStateHooks;
    }

    private Set<Predicate<S>> matchingStateConditions(Set<Predicate<S>> stateConditions) {
        Set<Predicate<S>> matchingStateConditions = new HashSet<>();
        for (Predicate<S> stateCondition : stateConditions) {
            if (stateCondition.test(state)) {
                matchingStateConditions.add(stateCondition);
            }
        }
        return matchingStateConditions;
    }

    private void runTask(Runnable runnable) {
        if (executeInParallel) {
            hookExecutor.submit(runnable, threadName);
        } else {
            runnable.run();
        }
    }

    public void stop() {
        if (hookExecutor != null) {
            hookExecutor.shutdown();
        }
    }
}
