package org.aanguita.jacuzzi.concurrency.concurrency_controller;

import org.aanguita.jacuzzi.maps.ObjectCount;

import java.util.function.Consumer;

/**
 * An implementation of ConcurrencyControllerAction for a basic read-write schema
 */
public class ConcurrencyControllerReadWrite extends ConcurrencyController {

    public final static String READ_ACTIVITY = "READ";

    public final static String WRITE_ACTIVITY = "WRITE";

    private static class ReadWriteAction implements ConcurrencyControllerAction {

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

    public ConcurrencyControllerReadWrite() {
        super(new ReadWriteAction());
    }

    public ConcurrencyControllerReadWrite(Consumer<String> logger, String name) {
        super(new ReadWriteAction(), logger, name);
    }
}
