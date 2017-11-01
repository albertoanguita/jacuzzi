package org.aanguita.jacuzzi.goal;

import java.util.concurrent.TimeoutException;

/**
 * Created by Alberto on 07/11/2016.
 */
public interface GoalExecutor<S> {

    S getState();

    void setState(S newState);

    S getGoal();

    void setGoal(S newGoal);

    boolean hasReachedGoal();

    void addEnterStateHook(S state, Runnable task);

    void removeEnterStateHook(S state, Runnable task);

    void setPeriodicStateHook(S state, Runnable task, long delay);

    void removePeriodicStateHook(S state);

    void addExitStateHook(S state, Runnable task);

    void removeExitStateHook(S state, Runnable task);

    void blockUntilGoalReached();

    void blockUntilGoalReached(long timeout) throws TimeoutException;

    void stop();
}
