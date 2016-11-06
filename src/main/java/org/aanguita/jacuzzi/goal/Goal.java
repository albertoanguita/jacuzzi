package org.aanguita.jacuzzi.goal;

/**
 * Created by Alberto on 07/11/2016.
 */
public interface Goal<S> {

    void setGoal(S newGoal);

    void addEnterStateHook(S state, Runnable task, boolean useOwnThread);

    void removeEnterStateHook(S state, Runnable task);

    void setPeriodicStateHook(S state, Runnable task, long delay);

    void removePeriodicStateHook(S state);

    void addExitStateHook(S state, Runnable task, boolean useOwnThread);

    void removeExitStateHook(S state, Runnable task);
}
