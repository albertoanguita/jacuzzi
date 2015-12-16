package jacz.util.concurrency.concurrency_controller;

import jacz.util.concurrency.daemon.Daemon;
import jacz.util.concurrency.daemon.DaemonAction;
import jacz.util.concurrency.execution_control.PausableElement;
import jacz.util.maps.ObjectCount;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * This class provides the general framework for creating classes that act as concurrency controllers
 * <p/>
 * Classes extending this class and implementing the abstract methods should be implemented in order to use it.
 * <p/>
 * Only ONE object of such classes must exist (or at least just one per schema of concurrency), in order to
 * ensure correct functioning
 */
public class ConcurrencyController implements DaemonAction {

    public static class QueueElement implements Comparable<QueueElement> {

        private final String activity;

        private final int priority;

        private final PausableElement pausableElement;

        private QueueElement(String activity, int priority) {
            this.activity = activity;
            this.priority = priority;
            pausableElement = new PausableElement();
            pausableElement.pause();
        }

        private String getActivity() {
            return activity;
        }

        /**
         * Higher priorities execute before
         *
         * @param o other QueueElement object to compare with
         * @return result of comparison
         */
        public int compareTo(@NotNull QueueElement o) {
            return o.priority - priority;
        }

        private void waitForPermissionToContinue() {
            pausableElement.access();
        }

        private void allowContinue() {
            pausableElement.resume();
        }
    }

    private static final String STOP_ACTIVITY = "@@@STOP@@@";

    /**
     * Actions implemented by the client
     */
    private final ConcurrencyControllerAction concurrencyControllerAction;

    /**
     * List of available activities for executions (each identified by means of a String), and their respective priorities
     */
//    private ActivityListAndPriorities activityListAndPriorities;

    /**
     * Queue where requests for executing specific activities are stored. This queue allows elements with higher
     * priority to be extracted first
     */
    private PriorityBlockingQueue<QueueElement> activityRequestsQueue;

    /**
     * Stores the number of executions of each activity at every moment
     */
    private ObjectCount<String> numberOfExecutionsOfActivities;

    /**
     * Maximum number of simultaneous activity executions allowed. It may be preferable to limit this number
     * taking into account the number of available cores. A value of 0 or negative indicates no limit
     */
    private int maxNumberOfExecutionsAllowed;

    /**
     * Daemon for controlling the execution of activities
     */
    private final Daemon daemon;

    /**
     * Whether this CC is alive (accepts more activities) or not
     */
    private boolean alive;

    /**
     * Default class constructor. Initializes the concurrency controller with no limit of simultaneous executions
     */
    public ConcurrencyController(ConcurrencyControllerAction concurrencyControllerAction) {
        this(concurrencyControllerAction, concurrencyControllerAction.maxNumberOfExecutionsAllowed());
    }

    /**
     * Class constructor. Initializes the concurrency controller with a specific amount of allowed simultaneous executions
     *
     * @param maxNumberOfExecutionsAllowed maximum amount of allowed simultaneous executions
     */
    public ConcurrencyController(ConcurrencyControllerAction concurrencyControllerAction, int maxNumberOfExecutionsAllowed) {
        this.concurrencyControllerAction = concurrencyControllerAction;
//        activityListAndPriorities = new ActivityListAndPriorities();
//        initializeActivityList(activityListAndPriorities);
//        addStopActivity();
        activityRequestsQueue = new PriorityBlockingQueue<>();
//        Set<String> supportedActivitiesSet = activityListAndPriorities.supportedActivitiesSet();
//        numberOfExecutionsOfActivities = new ObjectCount<>(supportedActivitiesSet, false, false);
        numberOfExecutionsOfActivities = new ObjectCount<>();
        this.maxNumberOfExecutionsAllowed = maxNumberOfExecutionsAllowed;
        daemon = new Daemon(this);
        alive = true;
    }

//    /**
//     * Initializes the activity list with the corresponding priorities. The object activityListAndPriorities
//     * has already been constructed, but it is isEmpty and must be filled here with the activities that are to
//     * be supported and their corresponding priorities. For each activity, an entry in the Map must be created.
//     * The activity itself will act as key, being its priority the associated value
//     *
//     * @param activityListAndPriorities ActivityListAndPriorities object for defining the list of supported
//     *                                  activities and their respective priorities
//     */
//    protected abstract void initializeActivityList(ActivityListAndPriorities activityListAndPriorities);

//    private void addStopActivity() {
//        // find the lowest priority
//        int lowestPriority = Integer.MAX_VALUE;
//        for (String activity : activityListAndPriorities.supportedActivitiesSet()) {
//            if (activity.equals(STOP_ACTIVITY)) {
//                throw new IllegalArgumentException("Activities cannot have the name " + STOP_ACTIVITY);
//            }
//            int priority = activityListAndPriorities.getPriority(activity);
//            if (priority < lowestPriority) {
//                lowestPriority = priority;
//            }
//        }
//        activityListAndPriorities.addActivity(STOP_ACTIVITY, lowestPriority - 1);
//    }

    /**
     * This functions provides the activityCanExecute function with the required synchronism (abstract methods
     * cannot be synchronized). This way we ensure that when the prisoner releaser is asking if a specific
     * activity can be executed, the endActivity functions cannot be invoked, and vice-versa. This avoids conflicts
     * when evaluating the conditions for activity execution
     *
     * @param activity activity pretended to be executed
     * @return true if this activity can be executed at this moment
     */
    private synchronized boolean callActivityCanExecute(String activity) {
        // first check that, if there is an actual limit for the number of simultaneous executions (given by
        // a value of maxNumberOfExecutionsAllowed greater than zero), this limit has not been reached
        // (if so, do not allow this execution to start)
        if (maxNumberOfExecutionsAllowed > 0 && numberOfExecutionsOfActivities.getTotalCount() >= maxNumberOfExecutionsAllowed) {
            return false;
        }
        // next, check if it is the STOP activity. STOP can only execute if there are no running activities
        else if (activity.equals(STOP_ACTIVITY)) {
            return numberOfExecutionsOfActivities.getTotalCount() == 0;
        }
        // just check with the client
        else {
            return concurrencyControllerAction.activityCanExecute(activity, numberOfExecutionsOfActivities);
        }
    }

//    /**
//     * This functions informs whether a specific activity can be executed, depending on other active activities
//     * and in what number each of them is being executed. This functions takes care of ensuring that no
//     * incompatible activities execute at the same time.
//     *
//     * @param activity                       activity pretended to be executed
//     * @param numberOfExecutionsOfActivities number of active executions of each active activity
//     * @return true if this activity can be executed at this moment
//     */
//    protected abstract boolean activityCanExecute(String activity, ObjectCount<String> numberOfExecutionsOfActivities);

    /**
     * Request permission for performing a registered activity. This call will block until the conditions for
     * performing such condition are fulfilled. This activity will have
     * to be released after completing the activity (by means of the endActivity function) in order to allow
     * the correct functioning of the concurrency controller.
     * <p/>
     * If the endConcurrencyController method has been previously invoked, this method will have no effect and will immediately return
     *
     * @param activity type of activity that the client pretends to execute
     */
    public final void beginActivity(String activity) {
        // the procedure is:
        // - place the executor in the priority blocking queue (by means of providing his identifier)
        // - block him
        //
        // The executor may actually be released before being blocked. He will simply get out of the block as soon
        // as he gets in, producing the expected result
        // When the blocking code is surpassed, the function must end to let the executor perform the requested
        // activity. This executor must later call the endActivity function to inform of the end of the
        // execution of his activity. He must specify the same activity he requested, as this is not recorded
        // in the concurrency controller

        // place the executor in the priority queue, so the daemon eventually takes it

        QueueElement queueElement = registerActivity(activity);

        // block this executor in these lines of code -> it will be liberated when other thread
        // releases it from its block (queueElement.allowContinue())
        beginRegisteredActivity(queueElement);

        // now the executor is free to perform the requested activity
    }

    /**
     * Request permission for performing a registered activity. This call will block until the conditions for
     * performing such condition are fulfilled. This activity will have
     * to be released after completing the activity (by means of the endActivity function) in order to allow
     * the correct functioning of the concurrency controller.
     * <p/>
     * If the endConcurrencyController method has been previously invoked, this method will have no effect and will immediately return
     *
     * @param activity type of activity that the client pretends to execute
     */
    public final QueueElement registerActivity(String activity) {
        QueueElement queueElement = new QueueElement(activity, getActivityPriority(activity));
        synchronized (this) {
            if (!alive && !activity.equals(STOP_ACTIVITY)) {
                throw new IllegalStateException("Concurrency controller has been stopped. No more activities allowed");
            }
        }
        activityRequestsQueue.put(queueElement);
        daemon.stateChange();
        return queueElement;
    }

    private int getActivityPriority(String activity) {
        if (activity.equals(STOP_ACTIVITY)) {
            return Integer.MIN_VALUE;
        } else {
            return concurrencyControllerAction.getActivityPriority(activity);
        }
    }

    public final void beginRegisteredActivity(QueueElement queueElement) {
        queueElement.waitForPermissionToContinue();
    }


    /**
     * Reports the end of the execution of a previously requested activity
     *
     * @param activity activity that has been completed
     */
    public final void endActivity(String activity) {
        synchronized (this) {
            numberOfExecutionsOfActivities.subtractObject(activity);
        }
        concurrencyControllerAction.activityHasEnded(activity, numberOfExecutionsOfActivities);
        // alert the daemon that a new opportunity for execution has raised
        daemon.stateChange();
    }

    @Override
    public boolean solveState() {
        QueueElement queueElement = activityRequestsQueue.peek();
        if (queueElement == null) {
            // the queue was empty, the state is solved
            return true;
        }
        String activity = queueElement.getActivity();
        if (callActivityCanExecute(activity)) {
            // the activity can execute now -> allow continue and remove it from the activity queue
            concurrencyControllerAction.activityIsGoingToBegin(activity, numberOfExecutionsOfActivities);
            synchronized (this) {
                numberOfExecutionsOfActivities.addObject(activity);
            }
            queueElement.allowContinue();
            activityRequestsQueue.remove();
            // allow the daemon to check for more activities in the queue
            return false;
        } else {
            // there is an activity in the queue, but it cannot execute yet -> wait for other opportunity
            return true;
        }
    }

    public void stopAndWaitForFinalization() {
        synchronized (this) {
            alive = false;
        }
        beginActivity(STOP_ACTIVITY);
        endActivity(STOP_ACTIVITY);
    }
}
