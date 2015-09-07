package jacz.util.AI.resource_distribution;

import jacz.util.concurrency.timer.SimpleTimerAction;
import jacz.util.concurrency.timer.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements an adaptive resource distributor which is capable of automatically detecting if the total
 * resource distribution system is struggling to provide resources (for example, we are reaching the limit of an
 * internet connection) and limiting it to avoid it. This is useful in situations in which reaching the limit
 * worsens the performance (e.g. an internet connection). The system automatically sets a limit under the struggling
 * point to avoid the situation.
 * <p/>
 * The system works in a dynamic fashion. The limit situation is abandoned after a while to test again, in case the
 * struggling point has increased.
 * <p/>
 * A struggling situation is established if total resources provided are under the given limit (if any) and all
 * stakeholders would like to receive more resources (there is not any stakeholder limiting the transfer)
 * <p/>
 * The result object inherits from the result in the native AdaptiveResourceDistributor, adding a boolean field
 * which indicates if the system is currently being restricted
 */
public class AdaptiveResourceDistributionAutoLimitation implements SimpleTimerAction {

    /**
     * The result for each assessment. It resembles the basic adaptive algorithm result, adding a variable which
     * indicates if the system is currently restricted
     */
    public static class Result extends AdaptiveResourceDistributor.Result {

        public boolean restrictingSystem;

        public Result(AdaptiveResourceDistributor.Result result, boolean restrictingSystem) {
            super(result.assignedResources, result.variation);
            this.restrictingSystem = restrictingSystem;
        }
    }

    /**
     * Possible states of this class:
     * - Normal loading queue: not restricted and not measuring (it needs to load the queue first)
     * - Normal full queue: not restricted and measuring (it has enough data to measure)
     * - Restricted: restriction mode
     */
    private static enum State {
        NORMAL_LOADING_QUEUE,
        NORMAL_FULL_QUEUE,
        RESTRICTED
    }

    /**
     * A resource quantity (float) with the time in millis in which it was registered
     */
    private static class ResourceWithTimestamp {

        private float resource;

        private long millis;

        private ResourceWithTimestamp(float resource, long millis) {
            this.resource = resource;
            this.millis = millis;
        }
    }

    /**
     * Time in millis that the algorithm takes measures for assessing saturation (e.g. 10 seconds)
     */
    private long millisToMeasureSaturation;

    /**
     * factor of the highest value in the measured series in which we considered the rest of numbers to mean saturation
     * (e.g. 0.95 --> <100, 99, 95> means saturation, but <100, 99, 94> does not)
     */
    private float factorToConsiderSaturation;

    /**
     * Factor for limiting the maximum allowed. Typical values are close to 1 (e.g. 0.95 so the 95% of what was
     * achieved so far is the new limit)
     */
    private float factorToLimit;

    /**
     * Time in millis that the limitation is maintained (e.g. 60 seconds)
     */
    private long millisToMaintainLimitation;

    private State state;

    /**
     * Queue storing the last resource consumptions (includes timestamps)
     */
    private List<ResourceWithTimestamp> resourceQueue;

    private Float minResourceStored;

    private Float maxResourceStored;

    private float limitationInRestriction;

    private final Timer timer;

    public AdaptiveResourceDistributionAutoLimitation(
            long millisToMeasureSaturation,
            float factorToConsiderSaturation,
            float factorToLimit,
            long millisToMaintainLimitation)
            throws IllegalArgumentException {
        this.millisToMeasureSaturation = millisToMeasureSaturation;
        this.factorToConsiderSaturation = factorToConsiderSaturation;
        this.factorToLimit = factorToLimit;
        this.millisToMaintainLimitation = millisToMaintainLimitation;
        state = State.NORMAL_LOADING_QUEUE;
        resourceQueue = new ArrayList<ResourceWithTimestamp>();
        minResourceStored = null;
        maxResourceStored = null;
        timer = new Timer(millisToMeasureSaturation, this);
    }

    /**
     * @param maxDesiredGivenResources the maximum total amount of resources that we wish to give to the stakeholders
     *                                 (null if there is no limit)
     * @param stakeholdersPreviousToLastAssignedResources
     *                                 the previous to the last assigned resources to each stakeholder.
     *                                 This parameter helps to better adapt increases in assignations (for several
     *                                 increases in a row, the increases grow bigger).
     *                                 If we do not want this correction to be applied, we can set this to null
     * @param stakeholdersLastAssignedResources
     *                                 the amount of resources that were assigned to each stakeholder in
     *                                 the last assessment
     * @param stakeholdersLastConsumedResources
     *                                 the amount of resources that each stakeholder actually consumed
     * @param wishedDistribution       the desired distribution ratio among the stakeholders. The values represent
     *                                 the proportions that should ideally be distributed. It does not need to
     *                                 be normalized
     *                                 A null value indicates that an equitable distribution is desired
     * @param stakeholdersMaxDesiredResources
     *                                 the maximum desired amount of resources by each stakeholder
     * @param threshold                threshold for estimating that an achieved value did not reach expectations
     *                                 with respect to assigned value. This value is a percentage. If an achieved
     *                                 value is below this percentage of the assigned value, the assigned value will
     *                                 have to be lowered.
     * @param loweringPercentage       This value indicates how much an assigned value if reduced, in case it must
     *                                 be reduced. The value indicates the percentage of the difference between the
     *                                 assigned value and the achieved value that the assignation is reduced.
     * @return a Result object containing the new assignations for the stakeholders and the degree of total variation
     *         in this assessment
     * @throws IllegalArgumentException if any of the arrays are different in maxSize
     */
    public Result distributeResources(
            Float maxDesiredGivenResources,
            List<Float> stakeholdersPreviousToLastAssignedResources,
            List<Float> stakeholdersLastAssignedResources,
            List<Float> stakeholdersLastConsumedResources,
            List<Float> wishedDistribution,
            List<Float> stakeholdersMaxDesiredResources,
            float threshold,
            float loweringPercentage) throws IllegalArgumentException {

        if (state == State.NORMAL_LOADING_QUEUE || state == State.NORMAL_FULL_QUEUE) {
            updateQueue(stakeholdersLastConsumedResources, System.currentTimeMillis());
        }
        if (state == State.NORMAL_FULL_QUEUE && evaluateRestrictionMode(maxDesiredGivenResources, stakeholdersLastConsumedResources, stakeholdersMaxDesiredResources)) {
            state = State.RESTRICTED;
            limitationInRestriction = maxResourceStored * factorToLimit;
            timer.reset(millisToMaintainLimitation);
        }
        if (state == State.RESTRICTED) {
            maxDesiredGivenResources = limitationInRestriction;
        }
        AdaptiveResourceDistributor.Result result =
                AdaptiveResourceDistributor.distributeResources(
                        maxDesiredGivenResources,
                        stakeholdersPreviousToLastAssignedResources,
                        stakeholdersLastAssignedResources,
                        stakeholdersLastConsumedResources,
                        wishedDistribution,
                        stakeholdersMaxDesiredResources,
                        threshold,
                        loweringPercentage);
        return new Result(result, state == State.RESTRICTED);
    }

    private boolean evaluateRestrictionMode(Float maxDesiredGivenResources, List<Float> stakeholdersLastConsumedResources, List<Float> stakeholdersMaxDesiredResources) {
        // check that the total amount delivered is not limited by the total limitation itself
        if (maxResourceStored >= factorToConsiderSaturation * maxDesiredGivenResources) {
            return false;
        }
        // check that no stakeholder is limited by its own max desired amount
        for (int i = 0; i < stakeholdersLastConsumedResources.size(); i++) {
            if (stakeholdersLastConsumedResources.get(i) >= factorToConsiderSaturation * stakeholdersMaxDesiredResources.get(i)) {
                // this stakeholder is self-limited -> the system is not struggling
                return false;
            }
        }
        // check if the last provided resources are all within a certain stripe
        return minResourceStored != null && maxResourceStored != null && minResourceStored >= maxResourceStored * factorToConsiderSaturation;
    }

    private void updateQueue(List<Float> stakeholdersLastConsumedResources, long currentTime) {
        // clean the resource queue from too old elements
        cleanResourceQueue(currentTime);

        float totalConsumedResources = 0f;
        for (Float f : stakeholdersLastConsumedResources) {
            totalConsumedResources += f;
        }

        resourceQueue.add(new ResourceWithTimestamp(totalConsumedResources, currentTime));
        if (minResourceStored == null || totalConsumedResources < minResourceStored) {
            minResourceStored = totalConsumedResources;
        }
        if (maxResourceStored == null || totalConsumedResources > maxResourceStored) {
            maxResourceStored = totalConsumedResources;
        }
    }

    private void cleanResourceQueue(long currentTime) {
        boolean mustEvaluateMinMax = false;
        while (resourceQueue.size() > 0 && resourceQueue.get(0).millis < currentTime - millisToMeasureSaturation) {
            ResourceWithTimestamp resourceWithTimestamp = resourceQueue.remove(0);
            if (resourceWithTimestamp.resource == minResourceStored || resourceWithTimestamp.resource == maxResourceStored) {
                mustEvaluateMinMax = true;
            }
        }
        if (mustEvaluateMinMax) {
            evaluateMinMaxResourcesStored();
        }
    }

    private void evaluateMinMaxResourcesStored() {
        minResourceStored = null;
        maxResourceStored = null;
        for (ResourceWithTimestamp resourceWithTimestamp : resourceQueue) {
            if (minResourceStored == null || resourceWithTimestamp.resource < minResourceStored) {
                minResourceStored = resourceWithTimestamp.resource;
            }
            if (maxResourceStored == null || resourceWithTimestamp.resource > maxResourceStored) {
                maxResourceStored = resourceWithTimestamp.resource;
            }
        }
    }

    @Override
    public Long wakeUp(Timer timer) {
        if (state == State.NORMAL_LOADING_QUEUE) {
            state = State.NORMAL_FULL_QUEUE;
            return -1L;
        } else if (state == State.RESTRICTED) {
            state = State.NORMAL_LOADING_QUEUE;
            return millisToMeasureSaturation;
        }
        return -1L;
    }

    public void stop() {
        timer.kill();
    }
}
