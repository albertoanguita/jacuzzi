package jacz.util.concurrency.concurrency_controller;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * The list of activities that a concurrency controller will handle, with their respective priorities (the higher the priority value, the
 * earlier it will execute)
 */
public final class ActivityListAndPriorities {

    /**
     * List of available activities for executions (each identified by means of a String), and their respective
     * priorities. The higher the int value, the higher the priority.
     */
    private final Map<String, Integer> activityListAndPriorities;

    ActivityListAndPriorities() {
        activityListAndPriorities = new HashMap<>();
    }


    public void addActivity(String activity, Integer priority) throws IllegalArgumentException {
        if (activityListAndPriorities.containsKey(activity)) {
            throw new IllegalArgumentException("Your ConcurrencyController implementation has tried to add an " +
                    "activity twice (" + activity + "). Activities can be defined only once. " +
                    "Execution should not continue.");
        }
        activityListAndPriorities.put(activity, priority);
    }

    Set<String> supportedActivitiesSet() {
        Set<String> resSet = new HashSet<>();
        for (String activity : activityListAndPriorities.keySet()) {
            resSet.add(activity);
        }
        return resSet;
    }

    Integer getPriority(String activity) {
        if (activityListAndPriorities.containsKey(activity)) {
            return activityListAndPriorities.get(activity);
        } else {
            throw new RuntimeException("Activity not found: " + activity);
        }
    }
}
