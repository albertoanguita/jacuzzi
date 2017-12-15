package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.timer.Timer;
import org.aanguita.jacuzzi.event.hub.*;
import org.aanguita.jacuzzi.id.AlphaNumFactory;
import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.*;

/**
 * This class allows to register hook methods that must be invoked when certain state among a set of possible states
 * is reached/left. The states are user defined.
 *
 * There are different types of hooks: upon entering a state, upon leaving a state, or periodical (while staying in
 * a state). All hooks are invoked asynchronously.
 *
 * S must correspond to an immutable class, as its values will serve as keys in map structures
 *
 * todo make type of event hub configurable, might want all synchronous (in goal executor e.g.)
 */
public class StateHooks<S> {

    private static class HookSubscriber extends EventHubSubscriberRandomId {

        private final Runnable hook;

        private HookSubscriber(Runnable hook) {
            super();
            this.hook = hook;
        }

        @Override
        public void event(Publication publication) {
            hook.run();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HookSubscriber that = (HookSubscriber) o;

            return hook.equals(that.hook);
        }

        @Override
        public int hashCode() {
            return hook.hashCode();
        }
    }

    private static class ChannelTaskSet {

        private final String channel;

        private final Set<HookSubscriber> hooks;

        private ChannelTaskSet() {
            this.channel = AlphaNumFactory.getStaticId();
            this.hooks = new HashSet<>();
        }
    }

    private static class PeriodicHook {

        private final Runnable task;

        private final long delay;

        private PeriodicHook(Runnable task, long delay) {
            this.task = task;
            this.delay = delay;
        }
    }

    private S state;

    private S oldState;

    private final Map<S, ChannelTaskSet> registeredEnterStateHooks;

    private final Map<S, PeriodicHook> registeredPeriodicHooks;

    private final Map<S, ChannelTaskSet> registeredExitStateHooks;

    private final EventHub eventHub;

    private Timer periodicHookTimer;

    private final String threadName;

    public StateHooks(S state) {
        this(state, ThreadUtil.invokerName(1));
    }

    public StateHooks(S state, String threadName) {
        this.state = state;
        oldState = state;
        registeredEnterStateHooks = new HashMap<>();
        registeredPeriodicHooks = new HashMap<>();
        registeredExitStateHooks = new HashMap<>();
        eventHub = EventHubFactory.createEventHub(threadName + AlphaNumFactory.getStaticId(), EventHubFactory.Type.SYNCHRONOUS);
        eventHub.start();
        this.threadName = threadName;
    }

    public synchronized void setState(S state) {
        this.state = state;
        checkStateHooks();
    }

    private String getEnterChannel(S state) {
        return registeredEnterStateHooks.get(state).channel + "-enter";
    }

    private String getExitChannel(S state) {
        return registeredExitStateHooks.get(state).channel + "-exit";
    }

    public synchronized void addEnterStateHook(S state, Runnable task) {
        HookSubscriber hookSubscriber = addStateHook(state, task, registeredEnterStateHooks);
        eventHub.registerSubscriber(hookSubscriber, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD);
        eventHub.subscribe(hookSubscriber, 0, getEnterChannel(state));
    }

    public synchronized void removeEnterStateHook(S state, Runnable task) {
        HookSubscriber hookSubscriber = removeStateHook(state, task, registeredEnterStateHooks);
        unsubscribeHook(hookSubscriber);
    }

    public synchronized void setPeriodicStateHook(S state, Runnable task, long delay) {
        removePeriodicStateHook(state);
        registeredPeriodicHooks.put(state, new PeriodicHook(task, delay));
        checkPeriodicHook();
    }

    public synchronized void removePeriodicStateHook(S state) {
        if (registeredPeriodicHooks.containsKey(state)) {
            registeredPeriodicHooks.remove(state);
            checkPeriodicHook();
        }
    }

    public synchronized void addExitStateHook(S state, Runnable task) {
        HookSubscriber hookSubscriber = addStateHook(state, task, registeredExitStateHooks);
        eventHub.registerSubscriber(hookSubscriber, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD);
        eventHub.subscribe(hookSubscriber, 0, getExitChannel(state));
    }

    public synchronized void removeExitStateHook(S state, Runnable task) {
        HookSubscriber hookSubscriber = removeStateHook(state, task, registeredExitStateHooks);
        unsubscribeHook(hookSubscriber);
    }

    private HookSubscriber addStateHook(S state, Runnable task, Map<S, ChannelTaskSet> registeredStates) {
        if (!registeredStates.containsKey(state)) {
            registeredStates.put(state, new ChannelTaskSet());
        }
        HookSubscriber hookSubscriber = new HookSubscriber(task);
        registeredStates.get(state).hooks.add(hookSubscriber);
        return hookSubscriber;
    }

    private HookSubscriber removeStateHook(S state, Runnable task, Map<S, ChannelTaskSet> registeredStates) {
        if (registeredStates.containsKey(state)) {
            for (Iterator<HookSubscriber> iterator = registeredStates.get(state).hooks.iterator(); iterator.hasNext(); ) {
                HookSubscriber hookSubscriber = iterator.next();
                if (hookSubscriber.hook.equals(task)) {
                    iterator.remove();
                    return hookSubscriber;
                }
            }
            // task not found
            return null;
        } else {
            return null;
        }
    }

    private void unsubscribeHook(HookSubscriber hookSubscriber) {
        if (hookSubscriber != null) {
            eventHub.unregisterSubscriber(hookSubscriber);
        }
    }

    private void checkStateHooks() {
        if (!state.equals(oldState)) {
            // we have moved to a new state -> invoke hooks
            stopTimer();
            if (registeredExitStateHooks.containsKey(oldState)) {
                eventHub.publish(getExitChannel(oldState), true);
            }
            checkPeriodicHook();
            if (registeredEnterStateHooks.containsKey(state)) {
                eventHub.publish(getEnterChannel(state), true);
            }

            oldState = state;
        }
    }

    private void stopTimer() {
        if (periodicHookTimer != null) {
            periodicHookTimer.stop();
        }
    }

    private void checkPeriodicHook() {
        if (registeredPeriodicHooks.containsKey(state) && (periodicHookTimer == null || periodicHookTimer.isStopped())) {
            periodicHookTimer = new Timer(
                    registeredPeriodicHooks.get(state).delay,
                    timer -> {
                        Runnable task;
                        synchronized (this) {
                            if (registeredPeriodicHooks.containsKey(state)) {
                                task = registeredPeriodicHooks.get(state).task;
                            } else {
                                task = null;
                            }
                        }
                        // the task is run outside the synchronized block, to avoid blocking the state hook while running it
                        if (task != null) {
                            task.run();
                            return null;
                        } else {
                            return 0L;
                        }
                    }, threadName);
        } else if (!registeredPeriodicHooks.containsKey(state) && periodicHookTimer != null) {
            periodicHookTimer.stop();
        }
    }

    public void stop() {
        stopTimer();
        eventHub.close();
    }
}
