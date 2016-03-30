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
        evolvingState.setRunnableStateTimer(DiscreteState.E, BLOCK / 2, new Runnable() {
            @Override
            public void run() {
                System.out.println("Check E!!!");
            }
        });
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
}
