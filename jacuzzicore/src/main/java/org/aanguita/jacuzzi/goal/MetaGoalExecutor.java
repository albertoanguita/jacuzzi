package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

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
         * State that indicates if the goal executor has failed
         */
        private final S fail;

        /**
         * Task to run when the goal executor reaches the goal
         */
        private Runnable successTask;

        /**
         * Task to run when the goal executor reaches the goal
         */
        private Runnable failTask;

        public GoalExecutorAndGoal(GoalExecutor<S> goalExecutor, S goal, S fail) {
            this.goalExecutor = goalExecutor;
            this.goal = goal;
            this.fail = fail;
        }

        private void run(Runnable successTask, Runnable failTask) {
            if (!goalExecutor.getState().equals(goal)) {
                this.successTask = successTask;
                goalExecutor.addEnterStateHook(goal, successTask);
                this.failTask = failTask;
                goalExecutor.addEnterStateHook(fail, failTask);
                goalExecutor.setGoal(goal);
            } else {
                // already at goal
                this.successTask = null;
                this.failTask = null;
                ThreadExecutor.submitUnregistered(successTask);
            }
        }

        /**
         * Clears the task to run when goal is reached
         */
        private void clearHooks() {
            if (successTask != null) {
                goalExecutor.removeEnterStateHook(goal, successTask);
                successTask = null;
            }
            if (failTask != null) {
                goalExecutor.removeEnterStateHook(fail, failTask);
                failTask = null;
            }
        }
    }

    /**
     * A combination of underlying goal executors, each with its goal, and an operator to combine them
     */
    public static class Step<S> {

        private final Operator operator;

        private final S failedState;

        private final Collection<GoalExecutorAndGoal<?>> goalExecutors;

        public Step(S failedState, GoalExecutorAndGoal<?>... goalExecutors) {
            this(Operator.defaultOperator(), failedState, goalExecutors);
        }

        public Step(S failedState, List<GoalExecutorAndGoal<?>> goalExecutors) {
            this(Operator.defaultOperator(), failedState, goalExecutors);
        }

        public Step(Operator operator, S failedState, GoalExecutorAndGoal<?>... goalExecutors) {
            this(operator, failedState, Arrays.asList(goalExecutors));
        }

        public Step(Operator operator, S failedState, List<GoalExecutorAndGoal<?>> goalExecutors) {
            this.operator = operator;
            this.failedState = failedState;
            this.goalExecutors = goalExecutors;
        }
    }

    public interface MetaSteps<S> {

        Step<S> getStep(S metaState, S metaGoal);
    }

    private class SuccessNotifier extends StringIdClass implements Runnable {

        @Override
        public void run() {
            MetaGoalExecutor.this.goalRunnerHasReachedGoal(this);
        }
    }

    private class FailureNotifier extends StringIdClass implements Runnable {

        @Override
        public void run() {
            MetaGoalExecutor.this.goalRunnerHasFailed(this);
        }
    }


    private final MetaSteps<S> metaSteps;

    private Step<S> currentStep;

    private List<GoalExecutorAndGoal<?>> currentUnfinishedGoalExecutors;

    private SuccessNotifier currentSuccessNotifier;

    private FailureNotifier currentFailureNotifier;

    public MetaGoalExecutor(S initialMetaState, MetaSteps<S> metaSteps) {
        this(initialMetaState, metaSteps, ThreadUtil.invokerName(1));
    }

    public MetaGoalExecutor(S initialMetaState, MetaSteps<S> metaSteps, String threadName) {
        this(initialMetaState, metaSteps, threadName, null);
    }

    public MetaGoalExecutor(S initialMetaState, MetaSteps<S> metaSteps, String threadName, Consumer<Exception> exceptionConsumer) {
        super(initialMetaState, threadName, exceptionConsumer);
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
            if (currentStep == null || currentStep.goalExecutors.isEmpty()) {
                goalReached();
            } else {
                currentUnfinishedGoalExecutors = new ArrayList<>(currentStep.goalExecutors);
                currentSuccessNotifier = new SuccessNotifier();
                currentFailureNotifier = new FailureNotifier();
                switch (currentStep.operator) {
                    case AND:
                    case OR:
                        currentStep.goalExecutors.forEach(goalExecutorAndGoal -> goalExecutorAndGoal.run(currentSuccessNotifier, currentFailureNotifier));
                        break;
                    case AND_QUEUE:
                        currentUnfinishedGoalExecutors.get(0).run(currentSuccessNotifier, currentFailureNotifier);
                        break;
                }
            }
        }
    }

    private synchronized void goalRunnerHasReachedGoal(SuccessNotifier successNotifier) {
        if (currentSuccessNotifier.equals(successNotifier)) {
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
                        currentUnfinishedGoalExecutors.get(0).run(currentSuccessNotifier, currentFailureNotifier);
                    }
                    break;
                case OR:
                    // with one finished, we reached the goal
                    goalReached();
                    break;
            }
        }
    }

    private synchronized void goalRunnerHasFailed(FailureNotifier failureNotifier) {
        if (currentFailureNotifier.equals(failureNotifier)) {
            switch (currentStep.operator) {
                case AND:
                case AND_QUEUE:
                    // with one fail, the whole meta goal fails
                    goalFailed();
                    break;
                case OR:
                    // only if this is the last executor, the meta task fails
                    if (currentUnfinishedGoalExecutors.isEmpty()) {
                        goalFailed();
                    }
                    break;
            }
        }
    }

    private void goalReached() {
        super.setState(goal);
        clearCurrentStep();
    }

    private void goalFailed() {
        super.setState(currentStep.failedState);
        clearCurrentStep();
    }

    private void clearCurrentStep() {
        if (currentStep != null) {
            currentStep.goalExecutors.forEach(GoalExecutorAndGoal::clearHooks);
            currentStep = null;
            currentUnfinishedGoalExecutors = null;
        }
    }

    @Override
    public void stop() {
        super.stop();
        clearCurrentStep();
    }
}
