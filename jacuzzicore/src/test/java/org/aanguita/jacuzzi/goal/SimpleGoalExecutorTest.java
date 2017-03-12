package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 07/02/2017.
 */
public class SimpleGoalExecutorTest {

    private static final long BLOCK = 500L;
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
                            System.out.println("moving state from C to D"); return DiscreteState.D;

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
        };
        SimpleGoalExecutor<DiscreteState> goalExecutor = new SimpleGoalExecutor<>(DiscreteState.A, transitions);
        goalExecutor.setBehavior(DiscreteState.B, BLOCK);
        goalExecutor.setBehavior(DiscreteState.C, BLOCK * 2);
        goalExecutor.setGlobalBehavior(BLOCK * 3);
        goalExecutor.setPeriodicStateHook(DiscreteState.E, () -> System.out.println("Check E!!!"), BLOCK / 2);
        HookEnd hookEnd = new HookEnd();
        goalExecutor.addEnterStateHook(DiscreteState.B, new Hook(hookEnd, "enter B"));
        goalExecutor.addExitStateHook(DiscreteState.B, new Hook(hookEnd, "exit B"));
        goalExecutor.addEnterStateHook(DiscreteState.C, new Hook(hookEnd, "enter C"));
        goalExecutor.addExitStateHook(DiscreteState.D, new Hook(hookEnd, "exit D"));
        goalExecutor.addExitStateHook(DiscreteState.E, new Hook(hookEnd, "exit E"));


        confirmDiscreteState(DiscreteState.A, goalExecutor);
        goalExecutor.setGoal(DiscreteState.E);
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
        ThreadUtil.safeSleep(BLOCK - FAST_BLOCK);
        confirmDiscreteState(DiscreteState.E, goalExecutor);
        ThreadUtil.safeSleep(BLOCK);
        confirmDiscreteState(DiscreteState.E, goalExecutor);
        goalExecutor.setGoal(DiscreteState.A);
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
}