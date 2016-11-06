package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Alberto on 30/10/2016.
 */
public class MetaGoalRunner<M> implements Goal<M> {

    public static enum Operator {
        AND,
        OR;

        public static Operator defaultOperator() {
            return AND;
        }
    }

    public static class GoalRunnerAndGoal<S> {

        private final GoalRunner<S> goalRunner;

        private final S goal;

        private Runnable currentTask;

        public GoalRunnerAndGoal(GoalRunner<S> goalRunner, S goal) {
            this.goalRunner = goalRunner;
            this.goal = goal;
        }

        private void run(Runnable task) {
            if (!goalRunner.state().equals(goal)) {
                currentTask = task;
                goalRunner.addEnterStateHook(goal, task, false);
                goalRunner.setGoal(goal);
            } else {
                currentTask = null;
                // todo make a submitUnregistered method
                String client = ThreadExecutor.registerClient();
                ThreadExecutor.submit(task);
                ThreadExecutor.shutdownClient(client);
            }
        }

        private void clearHook() {
            if (currentTask != null) {
                goalRunner.removeEnterStateHook(goal, currentTask);
            }
        }
    }

    public static class Step {

        private final Operator operator;

        private final Collection<GoalRunnerAndGoal<?>> goalRunners;

        public Step(GoalRunnerAndGoal<?>... goalRunners) {
            this(Operator.defaultOperator(), goalRunners);
        }

        public Step(Operator operator, GoalRunnerAndGoal<?>... goalRunners) {
            this.operator = operator;
            this.goalRunners = Arrays.asList(goalRunners);
        }
    }

    public interface MetaSteps<G> {

        Step getStep(G metaGoal);
    }

    private class Notifier extends StringIdClass implements Runnable {

        @Override
        public void run() {
            MetaGoalRunner.this.goalRunnerHasReachedGoal(getId());
        }
    }


    private final MetaSteps<M> metaSteps;

    private Step currentStep;

    private M currentGoal;

    private String currentNotifierId;

    private int remainingGoalRunners;

    private final StateHooks<M> stateHooks;

    public MetaGoalRunner(M initialMetaState, MetaSteps<M> metaSteps) {
        this.metaSteps = metaSteps;
        stateHooks = new StateHooks<>(initialMetaState);
    }

    @Override
    public synchronized void setGoal(M newGoal) {
        clearCurrentStep();
        currentGoal = newGoal;
        currentStep = metaSteps.getStep(newGoal);
        if (currentStep.goalRunners.isEmpty()) {
            goalReached();
        } else {
            remainingGoalRunners = currentStep.operator == Operator.AND ? currentStep.goalRunners.size() : 1;
            Notifier notifier = new Notifier();
            currentNotifierId = notifier.getId();
            currentStep.goalRunners.forEach((goalRunnerAndGoal) -> goalRunnerAndGoal.run(notifier));
        }
    }

    @Override
    public void addEnterStateHook(M state, Runnable task, boolean useOwnThread) {
        stateHooks.addEnterStateHook(state, task, useOwnThread);
    }

    @Override
    public void removeEnterStateHook(M state, Runnable task) {
        stateHooks.removeEnterStateHook(state, task);
    }

    @Override
    public void setPeriodicStateHook(M state, Runnable task, long delay) {
        stateHooks.setPeriodicStateHook(state, task, delay);
    }

    @Override
    public void removePeriodicStateHook(M state) {
        stateHooks.removePeriodicStateHook(state);
    }

    @Override
    public void addExitStateHook(M state, Runnable task, boolean useOwnThread) {
        stateHooks.addExitStateHook(state, task, useOwnThread);
    }

    @Override
    public void removeExitStateHook(M state, Runnable task) {
        stateHooks.removeExitStateHook(state, task);
    }

    private synchronized void goalRunnerHasReachedGoal(String notifierId) {
        if (currentNotifierId.equals(notifierId)) {
            remainingGoalRunners--;
            if (remainingGoalRunners == 0) {
                // meta goal reached!
                goalReached();
            }
        }
    }

    private void goalReached() {
        clearCurrentStep();
        stateHooks.setState(currentGoal);
    }

    private void clearCurrentStep() {
        if (currentStep != null) {
            currentStep.goalRunners.forEach(GoalRunnerAndGoal::clearHook);
        }
    }
}
