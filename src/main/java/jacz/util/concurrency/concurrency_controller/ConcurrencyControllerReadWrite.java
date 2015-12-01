package jacz.util.concurrency.concurrency_controller;

import jacz.util.maps.ObjectCount;

/**
 * An implementation of ConcurrencyControllerAction for a basic read-write schema
 */
public class ConcurrencyControllerReadWrite implements ConcurrencyControllerAction {

    public final static String READ_ACTIVITY = "READ";

    public final static String WRITE_ACTIVITY = "WRITE";

    @Override
    public int maxNumberOfExecutionsAllowed() {
        // no limit
        return 0;
    }

    @Override
    public int getActivityPriority(String activity) {
        // same priority for reads and writes
        return 0;
    }

    @Override
    public boolean activityCanExecute(String activity, ObjectCount<String> numberOfExecutionsOfActivities) {
        if (activity.equals(READ_ACTIVITY)) {
            return numberOfExecutionsOfActivities.getObjectCount(WRITE_ACTIVITY) == 0;
        } else {
            return numberOfExecutionsOfActivities.getTotalCount() == 0;
        }
    }

    @Override
    public void activityIsGoingToBegin(String activity, ObjectCount<String> numberOfExecutionsOfActivities) {
        // nothing to do
    }

    @Override
    public void activityHasEnded(String activity, ObjectCount<String> numberOfExecutionsOfActivities) {
        // nothing to do
    }
}
