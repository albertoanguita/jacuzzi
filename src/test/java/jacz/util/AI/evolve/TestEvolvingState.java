package jacz.util.AI.evolve;

import jacz.util.concurrency.ThreadUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Evolving state tests
 */
public class TestEvolvingState {

    private static final long BLOCK = 2000L;
    private static final long FAST_BLOCK = 100L;

    private class HookEnd {

        private static final long RECENT = 4000;

        private Map<String, Long> latestMessages;

        public HookEnd() {
            latestMessages = new HashMap<>();
        }

        public void submitMessage(String message) {
            latestMessages.put(message, System.currentTimeMillis());
        }

        public boolean checkRecentMessage(String message) {
            return latestMessages.containsKey(message) && System.currentTimeMillis() - RECENT < latestMessages.get(message);
        }
    }

    private class Hook implements Runnable {

        private final HookEnd hookEnd;

        private final String message;

        public Hook(HookEnd hookEnd, String message) {
            this.hookEnd = hookEnd;
            this.message = message;
        }

        @Override
        public void run() {
            System.out.println(message);
            hookEnd.submitMessage(message);
        }
    }

    private enum DiscreteState {
        A,
        B,
        C,
        D,
        E
    }

    @Test
    public void test() {

        EvolvingState.Transitions<DiscreteState, Boolean> transitions = new EvolvingState.Transitions<DiscreteState, Boolean>() {
            @Override
            public boolean runTransition(DiscreteState state, Boolean goal, EvolvingStateController<DiscreteState, Boolean> controller) {
                if (goal) {
                    switch (state) {

                        case A:
                            System.out.println("moving state from A to B");
                            controller.setState(DiscreteState.B);
                            return true;

                        case B:
                            System.out.println("moving state from B to C");
                            controller.setState(DiscreteState.C);
                            return true;

                        case C:
                            System.out.println("moving state from C to D");
                            controller.setState(DiscreteState.D);
                            return true;

                        case D:
                            System.out.println("moving state from D to E");
                            controller.setState(DiscreteState.E);
                            return true;

                    }
                } else {
                    switch (state) {

                        case D:
                            System.out.println("moving state from D to C");
                            controller.setState(DiscreteState.C);
                            return true;

                        case E:
                            System.out.println("moving state from E to D");
                            controller.setState(DiscreteState.D);
                            return true;

                    }
                }
                return true;
            }

            @Override
            public boolean hasReachedGoal(DiscreteState state, Boolean goal) {
                return true;
            }
        };
        EvolvingState<DiscreteState, Boolean> evolvingState = new EvolvingState<>(DiscreteState.A, true, transitions);
        evolvingState.setEvolveStateTimer(DiscreteState.B, BLOCK);
        evolvingState.setEvolveStateTimer(DiscreteState.C, BLOCK * 2);
        evolvingState.setEvolveStateTimer(new StateCondition<DiscreteState>() {
            @Override
            public boolean isInCondition(DiscreteState state) {
                return true;
            }

            @Override
            public String toString() {
                return "general state cond";
            }
        }, BLOCK * 3);
        evolvingState.setRunnableStateTimer(DiscreteState.E, BLOCK / 2, () -> System.out.println("Check E!!!"));
        HookEnd hookEnd = new HookEnd();
        evolvingState.setEnterStateHook(DiscreteState.B, new Hook(hookEnd, "enter B"));
        evolvingState.setExitStateHook(DiscreteState.B, new Hook(hookEnd, "exit B"));
        evolvingState.setEnterStateHook(DiscreteState.C, new Hook(hookEnd, "enter C"));
        evolvingState.setExitStateHook(DiscreteState.D, new Hook(hookEnd, "exit D"));
        evolvingState.setExitStateHook(DiscreteState.E, new Hook(hookEnd, "exit E"));

        confirmDiscreteState(DiscreteState.A, evolvingState);
        evolvingState.evolve();
        ThreadUtil.safeSleep(BLOCK / 2);
        confirmDiscreteState(DiscreteState.B, evolvingState);
        confirmHook(hookEnd, "enter B");
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.C, evolvingState);
        confirmHook(hookEnd, "exit B");
        confirmHook(hookEnd, "enter C");
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.C, evolvingState);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.D, evolvingState);
        evolvingState.evolve();
        ThreadUtil.safeSleep(FAST_BLOCK);
        confirmDiscreteState(DiscreteState.E, evolvingState);
        confirmHook(hookEnd, "exit D");
        evolvingState.setGoal(false, false);
        ThreadUtil.safeSleep(BLOCK - FAST_BLOCK);
        confirmDiscreteState(DiscreteState.E, evolvingState);
        // this timer should not affect the general timer now running
        evolvingState.setEvolveStateTimer(DiscreteState.A, 1000);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.E, evolvingState);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.D, evolvingState);
        confirmHook(hookEnd, "exit E");
        evolvingState.evolve();
        ThreadUtil.safeSleep(FAST_BLOCK);
        confirmDiscreteState(DiscreteState.C, evolvingState);
        confirmHook(hookEnd, "exit D");
        confirmHook(hookEnd, "enter C");
        evolvingState.evolve();
        ThreadUtil.safeSleep(FAST_BLOCK);
        confirmDiscreteState(DiscreteState.C, evolvingState);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.C, evolvingState);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.C, evolvingState);
    }

    private void confirmHook(HookEnd hookEnd, String message) {
        Assert.assertTrue(hookEnd.checkRecentMessage(message));
    }


    private <S> void confirmDiscreteState(S state, EvolvingState<S, Boolean> evolvingState) {
        Assert.assertEquals(state, evolvingState.state());
        System.out.println("We are in state " + state);
    }


    private static class State2 {
        int state;

        public State2() {
            this.state = 0;
        }

        @Override
        public String toString() {
            return "State2{" + state + '}';
        }
    }

    @Test
    public void test2() {
        EvolvingState.Transitions<State2, Boolean> transitions = new EvolvingState.Transitions<State2, Boolean>() {
            @Override
            public boolean runTransition(State2 state, Boolean goal, EvolvingStateController<State2, Boolean> controller) {
                System.out.println("Evolve: " + state);
                if (goal) {
                    if (state.state == 0) {
                        state.state = 1;
                        controller.stateHasChanged();
                        return false;
                    } else {
                        state.state = 0;
                        ThreadUtil.safeSleep(500);
                        controller.stateHasChanged();
                        return true;
                    }
                }
                return true;
            }

            @Override
            public boolean hasReachedGoal(State2 state, Boolean goal) {
                return true;
            }
        };
        EvolvingState<State2, Boolean> evolvingState = new EvolvingState<>(new State2(), true, transitions);
        evolvingState.setEvolveStateTimer(state -> true, 5000L);

        System.out.println("start");
        ThreadUtil.safeSleep(41000L);
    }

    @Test
    public void increasingTimeTest() {
        EvolvingState.Transitions<Boolean, Boolean> transitions = new EvolvingState.Transitions<Boolean, Boolean>() {
            @Override
            public boolean runTransition(Boolean state, Boolean goal, EvolvingStateController<Boolean, Boolean> controller) {
                System.out.println("Evolve: " + state);
                updateRetryTime();
                return true;
            }

            @Override
            public boolean hasReachedGoal(Boolean state, Boolean goal) {
                return true;
            }
        };
        dynamicState = new EvolvingState<>(true, true, transitions);
        setRetryTime(MAX_RETRY);

        System.out.println("start");
        checkDisconnections();
        ThreadUtil.safeSleep(35000L);
    }

    private static final long MIN_RETRY = 1000L;

    private static final long MAX_RETRY = 90000L;

    private long currentRetry;

    private EvolvingState<Boolean, Boolean> dynamicState;

    private static final StateCondition<Boolean> trueStateCondition = state -> true;

    private void updateRetryTime() {
        setRetryTime(Math.min(currentRetry * 2, MAX_RETRY));
    }

    private void setRetryTime(long time) {
        // replace previously set time with new one
        System.out.println("set retry time: " + time);
        dynamicState.setEvolveStateTimer(trueStateCondition, time);
        currentRetry = time;
    }

    public void checkDisconnections() {
        setRetryTime(MIN_RETRY);
    }


}
