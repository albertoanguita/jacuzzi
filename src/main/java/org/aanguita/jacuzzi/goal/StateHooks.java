package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.timer.Timer;
import org.aanguita.jacuzzi.event.hub.EventHub;
import org.aanguita.jacuzzi.event.hub.EventHubFactory;
import org.aanguita.jacuzzi.event.hub.EventHubSubscriber;
import org.aanguita.jacuzzi.id.AlphaNumFactory;
import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * S must correspond to an immutable class, as its values will serve as keys in map structures
 */
public class StateHooks<S> {

    private static class HookSubscriber extends StringIdClass implements EventHubSubscriber {

        private final Runnable hook;

        private HookSubscriber(Runnable hook) {
            super();
            this.hook = hook;
        }

        @Override
        public void event(String channel, Object... messages) {
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
        eventHub = EventHubFactory.createEventHub(threadName + AlphaNumFactory.getStaticId(), EventHubFactory.Type.MIXED);
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
        return registeredEnterStateHooks.get(state).channel + "-exit";
    }

    public synchronized void addEnterStateHook(S state, Runnable task, boolean useOwnThread) {
        HookSubscriber hookSubscriber = addStateHook(state, task, registeredEnterStateHooks);
        eventHub.subscribe(hookSubscriber.getId(), hookSubscriber, useOwnThread, getEnterChannel(state));
    }

    public synchronized void removeEnterStateHook(S state, Runnable task) {
        removeStateHook(state, task, registeredEnterStateHooks);
    }

    public synchronized void setPeriodicStateHook(S state, Runnable task, long delay) {
        removePeriodicStateHook(state);
        registeredPeriodicHooks.put(state, new PeriodicHook(task, delay));
        checkPeriodicHook();
    }

    public synchronized void removePeriodicStateHook(S state) {
        if (registeredPeriodicHooks.containsKey(state)) {
            registeredPeriodicHooks.remove(state);
            periodicHookTimer.stop();
        }
    }

    public synchronized void addExitStateHook(S state, Runnable task, boolean useOwnThread) {
        HookSubscriber hookSubscriber = addStateHook(state, task, registeredExitStateHooks);
        eventHub.subscribe(hookSubscriber.getId(), hookSubscriber, useOwnThread, getExitChannel(state));
    }

    public synchronized void removeExitStateHook(S state, Runnable task) {
        removeStateHook(state, task, registeredExitStateHooks);
    }

    private HookSubscriber addStateHook(S state, Runnable task, Map<S, ChannelTaskSet> registeredStates) {
        if (!registeredStates.containsKey(state)) {
            registeredStates.put(state, new ChannelTaskSet());
        }
        HookSubscriber hookSubscriber = new HookSubscriber(task);
        registeredStates.get(state).hooks.add(hookSubscriber);
        return hookSubscriber;
    }

    private void removeStateHook(S state, Runnable task, Map<S, ChannelTaskSet> registeredStates) {
        if (registeredStates.containsKey(state)) {
            registeredStates.get(state).hooks.remove(new HookSubscriber(task));
        }
    }

    private void checkStateHooks() {
        if (!state.equals(oldState)) {
            // we have moved to a new state -> invoke hooks
            stopTimer();
            eventHub.publish(getExitChannel(oldState), true);
            checkPeriodicHook();
            eventHub.publish(getEnterChannel(state), true);

            oldState = state;
        }
    }

    private void stopTimer() {
        if (periodicHookTimer != null) {
            periodicHookTimer.stop();
        }
    }

    private void checkPeriodicHook() {
        if (registeredPeriodicHooks.containsKey(state)) {
            periodicHookTimer = new Timer(
                    registeredPeriodicHooks.get(state).delay,
                    timer -> {
                        registeredPeriodicHooks.get(state).task.run();
                        return null;
                    }, threadName);
        }
    }

    public void stop() {
        stopTimer();
        eventHub.close();
    }
}
