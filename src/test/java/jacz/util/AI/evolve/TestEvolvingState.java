package jacz.util.AI.evolve;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.numeric.range.IntegerRange;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Evolving state tests
 */
public class TestEvolvingState {

    private class HookEnd {

        private static final long RECENT = 2000;

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
    public void testDiscrete() {

        EvolvingState.Transitions<DiscreteState, Boolean> transitions = new EvolvingState.Transitions<DiscreteState, Boolean>() {
            @Override
            public boolean runTransition(DiscreteState state, Boolean goal, EvolvingStateController<DiscreteState, Boolean> controller) {
                if (goal) {
                    switch (state) {

                        case A:
                            controller.setState(DiscreteState.B);
                            return true;

                        case B:
                            controller.setState(DiscreteState.C);
                            return true;

                        case C:
                            controller.setState(DiscreteState.D);
                            return true;

                        case D:
                            controller.setState(DiscreteState.E);
                            return true;

                    }
                } else {
                    switch (state) {

                        case D:
                            controller.setState(DiscreteState.C);
                            return true;

                        case E:
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
        DiscreteEvolvingState<DiscreteState, Boolean> evolvingState = new DiscreteEvolvingState<>(DiscreteState.A, true, transitions);
        evolvingState.setStateTimer(DiscreteState.B, 2000);
        evolvingState.setStateTimer(DiscreteState.C, 4000);
        evolvingState.setGeneralTimer(6000);
        HookEnd hookEnd = new HookEnd();
        evolvingState.setEnterStateHook(DiscreteState.B, new Hook(hookEnd, "enter B"));
        evolvingState.setExitStateHook(DiscreteState.B, new Hook(hookEnd, "exit B"));
        evolvingState.setEnterStateHook(DiscreteState.C, new Hook(hookEnd, "enter C"));
        evolvingState.setExitStateHook(DiscreteState.D, new Hook(hookEnd, "exit D"));
        evolvingState.setExitStateHook(DiscreteState.E, new Hook(hookEnd, "exit E"));

        confirmDiscreteState(DiscreteState.A, evolvingState);
        evolvingState.evolve();
        ThreadUtil.safeSleep(1000);
        confirmDiscreteState(DiscreteState.B, evolvingState);
        confirmHook(hookEnd, "enter B");
        ThreadUtil.safeSleep(2000);
        confirmDiscreteState(DiscreteState.C, evolvingState);
        confirmHook(hookEnd, "exit B");
        confirmHook(hookEnd, "enter C");
        ThreadUtil.safeSleep(2000);
        confirmDiscreteState(DiscreteState.C, evolvingState);
        ThreadUtil.safeSleep(2000);
        confirmDiscreteState(DiscreteState.D, evolvingState);
        evolvingState.evolve();
        ThreadUtil.safeSleep(500);
        confirmDiscreteState(DiscreteState.E, evolvingState);
        confirmHook(hookEnd, "exit D");
        evolvingState.setGoal(false, false);
        ThreadUtil.safeSleep(1000);
        confirmDiscreteState(DiscreteState.E, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmDiscreteState(DiscreteState.E, evolvingState);
        // this timer should not affect the general timer now running
        evolvingState.setStateTimer(DiscreteState.A, 1000);
        ThreadUtil.safeSleep(1000);
        confirmDiscreteState(DiscreteState.E, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmDiscreteState(DiscreteState.E, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmDiscreteState(DiscreteState.D, evolvingState);
        confirmHook(hookEnd, "exit E");
        evolvingState.evolve();
        ThreadUtil.safeSleep(500);
        confirmDiscreteState(DiscreteState.C, evolvingState);
        confirmHook(hookEnd, "exit D");
        evolvingState.evolve();
        ThreadUtil.safeSleep(500);
        confirmDiscreteState(DiscreteState.C, evolvingState);
        confirmHook(hookEnd, "enter C");
    }

    private void confirmHook(HookEnd hookEnd, String message) {
        Assert.assertTrue(hookEnd.checkRecentMessage(message));
    }


    private <S> void confirmDiscreteState(S state, EvolvingState<S, Boolean, ?> evolvingState) {
        Assert.assertEquals(state, evolvingState.state());
        System.out.println("We are in state " + state);
    }

    private class IntegerPortion implements ContinuousEvolvingState.StatePortion<ContState> {

        private final IntegerRange range;

        public IntegerPortion(Integer min, Integer max) {
            this.range = new IntegerRange(min, max);
        }

        @Override
        public boolean isInPortion(ContState state) {
            return range.contains(state.value());
        }
    }

    private class ContState {

        public int state;

        public ContState(int state) {
            this.state = state;
        }

        public int value() {
            return state;
        }

        public void add(int value) {
            state += value;
        }
    }

    @Test
    public void testContinuous() {

        EvolvingState.Transitions<ContState, Boolean> transitions = new EvolvingState.Transitions<ContState, Boolean>() {
            @Override
            public boolean runTransition(ContState state, Boolean goal, EvolvingStateController<ContState, Boolean> controller) {
                if (goal) {
                    if (state.value() <= 3) {
                        // low
                        state.add(2);
                        controller.stateHasChanged();
                    } else if (state.value() <= 6) {
                        // medium
                        state.add(1);
                        controller.stateHasChanged();
                    } else if (state.value() <= 8) {
                        // high
                        state.add(1);
                        controller.stateHasChanged();
                    }
                } else {
                    if (state.value() >= 3 && state.value() <= 6) {
                        // medium
                        state.add(-1);
                        controller.stateHasChanged();
                    } else if (state.value() > 6) {
                        // high
                        state.add(-2);
                        controller.stateHasChanged();
                    }
                }
                return true;
            }

            @Override
            public boolean hasReachedGoal(ContState state, Boolean goal) {
                return true;
            }
        };

        ContState contState = new ContState(0);
        ContinuousEvolvingState<ContState, Boolean> evolvingState = new ContinuousEvolvingState<>(contState, true, transitions);
        evolvingState.setStateTimer(new IntegerPortion(null, 6), 4000);
        evolvingState.setStateTimer(new IntegerPortion(3, 6), 2000);
        evolvingState.setGeneralTimer(6000);
        HookEnd hookEnd = new HookEnd();
        evolvingState.setExitStateHook(new ContinuousEvolvingState.StatePortion<ContState>() {
            @Override
            public boolean isInPortion(ContState state) {
                return state.value() <= 2;
            }
        }, new Hook(hookEnd, "exit very low"));
        evolvingState.setEnterStateHook(new ContinuousEvolvingState.StatePortion<ContState>() {
            @Override
            public boolean isInPortion(ContState state) {
                return state.value() > 2 && state.value() <= 4;
            }
        }, new Hook(hookEnd, "enter low medium"));
        evolvingState.setExitStateHook(new ContinuousEvolvingState.StatePortion<ContState>() {
            @Override
            public boolean isInPortion(ContState state) {
                return state.value() >= 7;
            }
        }, new Hook(hookEnd, "exit high"));
        evolvingState.setEnterStateHook(new ContinuousEvolvingState.StatePortion<ContState>() {
            @Override
            public boolean isInPortion(ContState state) {
                return state.value() >= 7;
            }
        }, new Hook(hookEnd, "enter high"));

        confirmContinuousState(0, evolvingState);
        evolvingState.evolve();
        ThreadUtil.safeSleep(200);
        confirmContinuousState(2, evolvingState);
        ThreadUtil.safeSleep(2800);
        confirmContinuousState(2, evolvingState);
        ThreadUtil.safeSleep(2000);
        confirmContinuousState(4, evolvingState);
        confirmHook(hookEnd, "exit very low");
        confirmHook(hookEnd, "enter low medium");
        ThreadUtil.safeSleep(500);
        confirmContinuousState(4, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(5, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(5, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(6, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(6, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(7, evolvingState);
        confirmHook(hookEnd, "enter high");
        ThreadUtil.safeSleep(5000);
        confirmContinuousState(7, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(8, evolvingState);
        ThreadUtil.safeSleep(5000);
        confirmContinuousState(8, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(9, evolvingState);
        evolvingState.setGoal(false, false);
        evolvingState.evolve();
        ThreadUtil.safeSleep(200);
        confirmContinuousState(7, evolvingState);
        ThreadUtil.safeSleep(4800);
        confirmContinuousState(7, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(5, evolvingState);
        confirmHook(hookEnd, "exit high");
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(5, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(4, evolvingState);
        confirmHook(hookEnd, "enter low medium");
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(4, evolvingState);
        ThreadUtil.safeSleep(1000);
        confirmContinuousState(3, evolvingState);
        ThreadUtil.safeSleep(6000);
        confirmContinuousState(2, evolvingState);
    }

    private <S> void confirmContinuousState(int value, ContinuousEvolvingState<ContState, Boolean> evolvingState) {
        Assert.assertEquals(value, evolvingState.state().value());
        System.out.println("We are in state " + value);
    }
}
