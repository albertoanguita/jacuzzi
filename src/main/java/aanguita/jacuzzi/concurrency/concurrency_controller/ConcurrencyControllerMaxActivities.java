package aanguita.jacuzzi.concurrency.concurrency_controller;

import aanguita.jacuzzi.maps.ObjectCount;

/**
 * A simple concurrency controller where all tasks are equally treated, but there is a limit to the number of
 * total executing activities
 */
public class ConcurrencyControllerMaxActivities extends ConcurrencyController {

    private static class MaxActivitiesAction implements ConcurrencyControllerAction {

        private final int maxActivityCount;

        public MaxActivitiesAction(int maxActivityCount) {
            this.maxActivityCount = maxActivityCount;
        }

        @Override
        public int maxNumberOfExecutionsAllowed() {
            return maxActivityCount;
        }

        @Override
        public int getActivityPriority(String activity) {
            return 0;
        }

        @Override
        public boolean activityCanExecute(String activity, ObjectCount<String> numberOfExecutionsOfActivities) {
            return true;
        }

        @Override
        public void activityIsGoingToBegin(String activity, ObjectCount<String> numberOfExecutionsOfActivities) {
            // ignore
        }

        @Override
        public void activityHasEnded(String activity, ObjectCount<String> numberOfExecutionsOfActivities) {
            // ignore
        }
    }

    public ConcurrencyControllerMaxActivities(int maxActivityCount) {
        super(new MaxActivitiesAction(maxActivityCount));
    }
}
