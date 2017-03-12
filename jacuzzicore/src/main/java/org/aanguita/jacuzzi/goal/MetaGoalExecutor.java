package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This class provides an implementation of the GoalExecutor interface which relies on the underlying execution of
 * other goal executors.
 */
public class MetaGoalExecutor<S> extends AbstractGoalExecutor<S> {

    public enum Operator {
        AND,
        AND_QUEUE,
        OR;

        public static Operator defaultOperator() {
            return AND;
        }
    }

    public static class GoalExecutorAndGoal<S> {

        /**
         * Underlying goal executor
         */
        private final GoalExecutor<S> goalExecutor;

        /**
         * Goal for the goal executor
         */
        private final S goal;

        /**
         * Task to run when the goal executor reaches the goal
         */
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
                // already at goal
                currentTask = null;
                // todo make a submitUnregistered method
                String client = ThreadExecutor.registerClient();
                ThreadExecutor.submit(task);
                ThreadExecutor.shutdownClient(client);
            }
        }

        /**
         * Clears the task to run when goal is reached
         */
        private void clearHook() {
            if (currentTask != null) {
                goalExecutor.removeEnterStateHook(goal, currentTask);
            }
        }
    }

    /**
     * A combination of underlying goal executors, each with its goal, and an operator to combine them
     */
    public static class Step<S> {

        private final Operator operator;

        private final Collection<GoalExecutorAndGoal<S>> goalExecutors;

        public Step(GoalExecutorAndGoal<S>... goalExecutors) {
            this(Operator.defaultOperator(), goalExecutors);
        }

        public Step(Operator operator, GoalExecutorAndGoal<S>... goalExecutors) {
            this.operator = operator;
            this.goalExecutors = Arrays.asList(goalExecutors);
        }
    }

    public interface MetaSteps<S> {

        Step<S> getStep(S metaState, S metaGoal);
    }

    private class Notifier extends StringIdClass implements Runnable {

        @Override
        public void run() {
            MetaGoalExecutor.this.goalRunnerHasReachedGoal(this);
        }
    }


    private final MetaSteps<S> metaSteps;

    private Step<S> currentStep;

    private List<GoalExecutorAndGoal<S>> currentUnfinishedGoalExecutors;

    private Notifier currentNotifier;

    public MetaGoalExecutor(S initialMetaState, MetaSteps<S> metaSteps) {
        this(initialMetaState, metaSteps, ThreadUtil.invokerName(1));
    }

    public MetaGoalExecutor(S initialMetaState, MetaSteps<S> metaSteps, String threadName) {
        super(initialMetaState, threadName);
        this.metaSteps = metaSteps;
        this.currentStep = null;
        this.currentUnfinishedGoalExecutors = null;
        this.currentUnfinishedGoalExecutors = null;
        setGoal(initialMetaState);
    }

    @Override
    public void setState(S newState) {
        if (!state.equals(newState)) {
            super.setState(newState);
            updateCurrentStep();
        }
    }

    @Override
    public synchronized void setGoal(S newGoal) {
        super.setGoal(newGoal);
        updateCurrentStep();
    }

    private void updateCurrentStep() {
        clearCurrentStep();
        if (!state.equals(goal)) {
            currentStep = metaSteps.getStep(state, goal);
            if (currentStep.goalExecutors.isEmpty()) {
                goalReached();
            } else {
                currentUnfinishedGoalExecutors = new ArrayList<>(currentStep.goalExecutors);
                currentNotifier = new Notifier();
                switch (currentStep.operator) {
                    case AND:
                    case OR:
                        currentStep.goalExecutors.forEach(goalExecutorAndGoal -> goalExecutorAndGoal.run(currentNotifier));
                        break;
                    case AND_QUEUE:
                        currentUnfinishedGoalExecutors.get(0).run(currentNotifier);
                        break;
                }
            }
        }
    }

    private synchronized void goalRunnerHasReachedGoal(Notifier notifier) {
        if (currentNotifier.equals(notifier)) {
            switch (currentStep.operator) {
                case AND:
                    // delete one goal executor (does not matter which) and check if all have finished
                    currentUnfinishedGoalExecutors.remove(0);
                    if (currentUnfinishedGoalExecutors.isEmpty()) {
                        goalReached();
                    }
                    break;
                case AND_QUEUE:
                    // delete one goal executor (does not matter which) and check if all have finished
                    // (or initiate next goal executor)
                    currentUnfinishedGoalExecutors.remove(0);
                    if (currentUnfinishedGoalExecutors.isEmpty()) {
                        goalReached();
                    } else {
                        currentUnfinishedGoalExecutors.get(0).run(currentNotifier);
                    }
                    break;
                case OR:
                    // with one finished, we reached the goal
                    goalReached();
                    break;
            }
        }
    }

    private void goalReached() {
        setState(goal);
    }

    private void clearCurrentStep() {
        if (currentStep != null) {
            currentStep.goalExecutors.forEach(GoalExecutorAndGoal::clearHook);
            currentStep = null;
            currentUnfinishedGoalExecutors = null;
        }
    }
}
