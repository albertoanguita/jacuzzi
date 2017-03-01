package org.aanguita.jacuzzi.goal;

/**
 * Created by Alberto on 07/11/2016.
 */
public interface GoalExecutor<S> {

    S getState();

    void setState(S newState);

    S getGoal();

    void setGoal(S newGoal);

    boolean hasReachedGoal();

    void evolve();

    void setGlobalBehavior(long millis);

    void setBehavior(S state, long millis);

    void setBehavior(S state, S goal, long millis);

    void removeGlobalBehavior();

    void removeBehavior(S state);

    void removeBehavior(S state, S goal);

    void addEnterStateHook(S state, Runnable task);

    void removeEnterStateHook(S state, Runnable task);

    void setPeriodicStateHook(S state, Runnable task, long delay);

    void removePeriodicStateHook(S state);

    void addExitStateHook(S state, Runnable task);

    void removeExitStateHook(S state, Runnable task);

    void blockUntilGoalReached();

    void stop();
}
