package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Alberto on 30/10/2016.
 */
public class MetaGoalExecutor<S> implements GoalExecutor<S> {

    public enum Operator {
        AND,
        OR;

        public static Operator defaultOperator() {
            return AND;
        }
    }

    public static class GoalExecutorAndGoal<S> {

        private final GoalExecutor<S> goalExecutor;

        private final S goal;

        private Runnable currentTask;

        public GoalExecutorAndGoal(SimpleGoalExecutor<S> goalExecutor, S goal) {
            this.goalExecutor = goalExecutor;
            this.goal = goal;
        }

        private void run(Runnable task) {
            if (!goalExecutor.getState().equals(goal)) {
                currentTask = task;
                goalExecutor.addEnterStateHook(goal, task);
                goalExecutor.setGoal(goal);
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
                goalExecutor.removeEnterStateHook(goal, currentTask);
            }
        }
    }

    public static class Step {

        private final Operator operator;

        private final Collection<GoalExecutorAndGoal<?>> goalExecutors;

        public Step(GoalExecutorAndGoal<?>... goalExecutors) {
            this(Operator.defaultOperator(), goalExecutors);
        }

        public Step(Operator operator, GoalExecutorAndGoal<?>... goalExecutors) {
            this.operator = operator;
            this.goalExecutors = Arrays.asList(goalExecutors);
        }
    }

    public interface MetaSteps<G> {

        Step getStep(G metaGoal);
    }

    private class Notifier extends StringIdClass implements Runnable {

        @Override
        public void run() {
            MetaGoalExecutor.this.goalRunnerHasReachedGoal(this);
        }
    }


    private S metaState;

    private S metaGoal;

    private final MetaSteps<S> metaSteps;

    private Step currentStep;

    private Notifier currentNotifier;

    private int remainingGoalExecutors;

    private final StateHooks<S> stateHooks;

    public MetaGoalExecutor(S initialMetaState, MetaSteps<S> metaSteps) {
        this.metaState = initialMetaState;
        this.metaGoal = initialMetaState;
        this.metaSteps = metaSteps;
        this.currentStep = null;
        stateHooks = new StateHooks<>(initialMetaState);
        setGoal(initialMetaState);
    }

    @Override
    public S getState() {
        return metaState;
    }

    @Override
    public void setState(S newState) {
        // todo
    }

    @Override
    public S getGoal() {
        return metaGoal;
    }

    @Override
    public synchronized void setGoal(S newGoal) {
        clearCurrentStep();
        metaGoal = newGoal;
        currentStep = metaSteps.getStep(newGoal);
        if (currentStep.goalExecutors.isEmpty()) {
            goalReached();
        } else {
            remainingGoalExecutors = currentStep.operator == Operator.AND ? currentStep.goalExecutors.size() : 1;
            currentNotifier = new Notifier();
            currentStep.goalExecutors.forEach((goalExecutorAndGoal) -> goalExecutorAndGoal.run(currentNotifier));
        }
    }

    @Override
    public boolean hasReachedGoal() {
        return false;
    }

    @Override
    public void evolve() {

    }

    @Override
    public void setGlobalBehavior(long millis) {
        // todo all
    }

    @Override
    public void setBehavior(S state, long millis) {

    }

    @Override
    public void setBehavior(S state, S goal, long millis) {

    }

    @Override
    public void removeGlobalBehavior() {

    }

    @Override
    public void removeBehavior(S state) {

    }

    @Override
    public void removeBehavior(S state, S goal) {

    }

    @Override
    public void addEnterStateHook(S state, Runnable task) {
        stateHooks.addEnterStateHook(state, task);
    }

    @Override
    public void removeEnterStateHook(S state, Runnable task) {
        stateHooks.removeEnterStateHook(state, task);
    }

    @Override
    public void setPeriodicStateHook(S state, Runnable task, long delay) {
        stateHooks.setPeriodicStateHook(state, task, delay);
    }

    @Override
    public void removePeriodicStateHook(S state) {
        stateHooks.removePeriodicStateHook(state);
    }

    @Override
    public void addExitStateHook(S state, Runnable task) {
        stateHooks.addExitStateHook(state, task);
    }

    @Override
    public void removeExitStateHook(S state, Runnable task) {
        stateHooks.removeExitStateHook(state, task);
    }

    @Override
    public void blockUntilGoalReached() {
        // todo
    }

    @Override
    public void stop() {
        // todo
    }

    private synchronized void goalRunnerHasReachedGoal(Notifier notifier) {
        if (currentNotifier.equals(notifier)) {
            remainingGoalExecutors--;
            if (remainingGoalExecutors == 0) {
                // meta goal reached!
                goalReached();
            }
        }
    }

    private void goalReached() {
        clearCurrentStep();
        stateHooks.setState(metaGoal);
    }

    private void clearCurrentStep() {
        if (currentStep != null) {
            currentStep.goalExecutors.forEach(GoalExecutorAndGoal::clearHook);
            currentStep = null;
        }
    }
}
