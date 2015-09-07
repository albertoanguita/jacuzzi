package jacz.util.concurrency.concurrency_controller;

import jacz.util.lists.Duple;
import jacz.util.maps.ObjectCount;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 28-abr-2008<br>
 * Last Modified: 28-abr-2008
 */
public abstract class ConcurrencyControllerReadWrite extends ConcurrencyController {

    public final static String READ_ACTIVITY = "READ";

    public final static String WRITE_ACTIVITY = "WRITE";

    public ConcurrencyControllerReadWrite() {
        super();
    }

    public ConcurrencyControllerReadWrite(int maxNumberOfExecutionsAllowed) {
        super(maxNumberOfExecutionsAllowed);
    }

    protected final boolean activityCanExecute(String activity, ObjectCount<String> numberOfExecutionsOfActivities) {
        if (activity.equals(READ_ACTIVITY)) {
            return numberOfWritersExecuting(numberOfExecutionsOfActivities) == 0;
        } else {
            return numberOfActivitiesExecuting(numberOfExecutionsOfActivities) == 0;
        }
    }

    @Override
    protected void initializeActivityList(ActivityListAndPriorities activityListAndPriorities) {
        Duple<Integer, Integer> readWritePriorities = readWritePriorities();
        activityListAndPriorities.addActivity(ConcurrencyControllerReadWrite.READ_ACTIVITY, readWritePriorities.element1);
        activityListAndPriorities.addActivity(ConcurrencyControllerReadWrite.WRITE_ACTIVITY, readWritePriorities.element2);
    }

    public abstract Duple<Integer, Integer> readWritePriorities();

    private int numberOfWritersExecuting(ObjectCount <String> numberOfExecutionsOfActivities) {
        int count = 0;
        for (String activity : numberOfExecutionsOfActivities.objectSet()) {
            if (activity.equals(WRITE_ACTIVITY)) {
                try {
                    count += numberOfExecutionsOfActivities.getObjectCount(activity);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
        return count;
    }

    private int numberOfActivitiesExecuting(ObjectCount<String> numberOfExecutionsOfActivities) {
        int count = 0;
        for (String activity : numberOfExecutionsOfActivities.objectSet()) {
            try {
                count += numberOfExecutionsOfActivities.getObjectCount(activity);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return count;
    }
}
