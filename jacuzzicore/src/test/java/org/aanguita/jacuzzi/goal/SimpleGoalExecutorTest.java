package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.AI.evolve.EvolvingState;
import org.aanguita.jacuzzi.AI.evolve.EvolvingStateController;
import org.aanguita.jacuzzi.AI.evolve.TestEvolvingState;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Created by Alberto on 07/02/2017.
 */
public class SimpleGoalExecutorTest {

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

        SimpleGoalExecutor.Transitions<DiscreteState> transitions = new SimpleGoalExecutor.Transitions<DiscreteState>() {
            @Override
            public DiscreteState runTransition(DiscreteState state, DiscreteState goal) {
                if (goal == DiscreteState.E) {
                    switch (state) {

                        case A:
                            System.out.println("moving state from A to B");
                            return DiscreteState.B;

                        case B:
                            System.out.println("moving state from B to C");
                            return DiscreteState.C;

                        case C:
                            System.out.println("moving state from C to D");
                            return DiscreteState.D;

                        case D:
                            System.out.println("moving state from D to E");
                            return DiscreteState.E;

                    }
                } else {
                    switch (state) {

                        case D:
                            System.out.println("moving state from D to C");
                            return DiscreteState.C;

                        case E:
                            System.out.println("moving state from E to D");
                            return DiscreteState.D;

                    }
                }
                return null;
            }

            @Override
            public Long behaviorInState(DiscreteState state, DiscreteState goal) {
                switch (state) {

                    case B:
                        return BLOCK;
                    case C:
                        return BLOCK * 2;
                    default:
                        return BLOCK * 3;
                }
            }
        };
        SimpleGoalExecutor<DiscreteState> goalExecutor = new SimpleGoalExecutor<>(DiscreteState.A, transitions);
        goalExecutor.setGoal(DiscreteState.E);
//        EvolvingState<DiscreteState, Boolean> evolvingState = new EvolvingState<>(DiscreteState.A, true, transitions);
//        goalExecutor.setPeriodicStateHook(DiscreteState.B, BLOCK);
//        goalExecutor.setEvolveStateTimer(DiscreteState.C, BLOCK * 2);
//        goalExecutor.setEvolveStateTimer(new Predicate<DiscreteState>() {
//            @Override
//            public boolean test(DiscreteState state) {
//                return true;
//            }
//
//            @Override
//            public String toString() {
//                return "general state cond";
//            }
//        }, BLOCK * 3);
        goalExecutor.setPeriodicStateHook(DiscreteState.E, () -> System.out.println("Check E!!!"), BLOCK / 2);
        HookEnd hookEnd = new HookEnd();
        goalExecutor.addEnterStateHook(DiscreteState.B, new Hook(hookEnd, "enter B"));
        goalExecutor.addExitStateHook(DiscreteState.B, new Hook(hookEnd, "exit B"));
        goalExecutor.addEnterStateHook(DiscreteState.C, new Hook(hookEnd, "enter C"));
        goalExecutor.addExitStateHook(DiscreteState.D, new Hook(hookEnd, "exit D"));
        goalExecutor.addExitStateHook(DiscreteState.E, new Hook(hookEnd, "exit E"));

        confirmDiscreteState(DiscreteState.A, goalExecutor);
        goalExecutor.evolve();
        ThreadUtil.safeSleep(BLOCK / 2);
        confirmDiscreteState(DiscreteState.B, goalExecutor);
        confirmHook(hookEnd, "enter B");
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.C, goalExecutor);
        confirmHook(hookEnd, "exit B");
        confirmHook(hookEnd, "enter C");
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.C, goalExecutor);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.D, goalExecutor);
        goalExecutor.evolve();
        ThreadUtil.safeSleep(FAST_BLOCK);
        confirmDiscreteState(DiscreteState.E, goalExecutor);
        confirmHook(hookEnd, "exit D");
        goalExecutor.setGoal(DiscreteState.A);
        ThreadUtil.safeSleep(BLOCK - FAST_BLOCK);
        confirmDiscreteState(DiscreteState.E, goalExecutor);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.E, goalExecutor);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.D, goalExecutor);
        confirmHook(hookEnd, "exit E");
        goalExecutor.evolve();
        ThreadUtil.safeSleep(FAST_BLOCK);
        confirmDiscreteState(DiscreteState.C, goalExecutor);
        confirmHook(hookEnd, "exit D");
        confirmHook(hookEnd, "enter C");
        goalExecutor.evolve();
        ThreadUtil.safeSleep(FAST_BLOCK);
        confirmDiscreteState(DiscreteState.C, goalExecutor);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.C, goalExecutor);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.C, goalExecutor);
    }

    private void confirmHook(HookEnd hookEnd, String message) {
        Assert.assertTrue(hookEnd.checkRecentMessage(message));
    }


    private <S> void confirmDiscreteState(S state, SimpleGoalExecutor<DiscreteState> goalExecutor) {
        Assert.assertEquals(state, goalExecutor.getState());
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

    private static final Predicate<Boolean> trueStateCondition = state -> true;

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