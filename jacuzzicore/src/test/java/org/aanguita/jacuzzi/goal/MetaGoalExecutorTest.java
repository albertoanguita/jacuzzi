package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Alberto on 12/03/2017.
 */
public class MetaGoalExecutorTest {

    private enum DiscreteState {
        A,
        B,
    }

    private enum MetaState {
        _1,
        _2,
    }

    private static final long TIME_1 = 1000;
    private static final long TIME_2 = 2000;
    private static final long TIME_3 = 3000;



    private class Transitions implements SimpleGoalExecutor.Transitions<DiscreteState> {

        private final long time;

        private GoalExecutor<DiscreteState> goalExecutor;

        public Transitions(long time) {
            this.time = time;
        }

        public void setGoalExecutor(GoalExecutor<DiscreteState> goalExecutor) {
            this.goalExecutor = goalExecutor;
        }

        @Override
        public DiscreteState runTransition(DiscreteState state, DiscreteState goal) {
            if (state == DiscreteState.A && goal == DiscreteState.B) {
                String client = ThreadExecutor.registerClient();
                ThreadExecutor.submit(() -> {
                    ThreadUtil.safeSleep(time);
                    goalExecutor.setState(DiscreteState.B);
                });
                ThreadExecutor.unregisterClient(client);
                return null;
            } else {
                return null;
            }
        }
    }

    @Test
    public void test() {

        MetaGoalExecutor.MetaSteps<MetaState> metaSteps = (metaState, metaGoal) -> {
            if (metaState.equals(MetaState._1) && metaGoal.equals(MetaState._2)) {
                Transitions transitions = new Transitions(TIME_1);
                GoalExecutor<DiscreteState> goalExecutor1 = new SimpleGoalExecutor<>(DiscreteState.A, transitions);
                transitions.setGoalExecutor(goalExecutor1);

                transitions = new Transitions(TIME_2);
                GoalExecutor<DiscreteState> goalExecutor2 = new SimpleGoalExecutor<>(DiscreteState.A, transitions);
                transitions.setGoalExecutor(goalExecutor2);

                transitions = new Transitions(TIME_3);
                GoalExecutor<DiscreteState> goalExecutor3 = new SimpleGoalExecutor<>(DiscreteState.A, transitions);
                transitions.setGoalExecutor(goalExecutor3);

                return new MetaGoalExecutor.Step(
                        MetaGoalExecutor.Operator.AND_QUEUE,
                        new MetaGoalExecutor.GoalExecutorAndGoal<>(goalExecutor1, DiscreteState.B),
                        new MetaGoalExecutor.GoalExecutorAndGoal<>(goalExecutor2, DiscreteState.B),
                        new MetaGoalExecutor.GoalExecutorAndGoal<>(goalExecutor3, DiscreteState.B));
            } else {
                return null;
            }
        };

        MetaGoalExecutor<MetaState> metaGoalExecutor = new MetaGoalExecutor<>(MetaState._1, metaSteps);

        long expectedTime = 6000;
        metaGoalExecutor.setGoal(MetaState._2);
        ThreadUtil.safeSleep(expectedTime - 50);
        assertEquals(MetaState._1, metaGoalExecutor.getState());
        ThreadUtil.safeSleep(100);
        assertEquals(MetaState._2, metaGoalExecutor.getState());
    }

}