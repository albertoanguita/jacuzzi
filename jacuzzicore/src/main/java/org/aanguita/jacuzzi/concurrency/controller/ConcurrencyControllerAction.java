package org.aanguita.jacuzzi.concurrency.controller;

import org.aanguita.jacuzzi.maps.ObjectCount;

/**
 * Actions requested by the concurrency controller
 */
public interface ConcurrencyControllerAction {

    /**
     * Maximum number of simultaneous activity executions allowed. It may be preferable to limit this number
     * taking into account the number of available cores. A value of 0 or negative indicates no limit
     */
    int maxNumberOfExecutionsAllowed();

    int getActivityPriority(String activity);

    boolean activityCanExecute(String activity, ObjectCount<String> numberOfExecutionsOfActivities);

    void activityIsGoingToBegin(String activity, ObjectCount<String> numberOfExecutionsOfActivities);

    void activityHasEnded(String activity, ObjectCount<String> numberOfExecutionsOfActivities);
}
