package jacz.util.AI.resource_distribution;

import jacz.util.lists.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Lower and increase assignations according to their achieved speed. The wished distribution also affects this
 * <p/>
 * After that, check if individual limits are met (if any), and cut for those that overpass their limit
 * <p/>
 * After that, check that the global limit is met (if any), and scale down linearly all assignations if it is overpassed
 */
public class AdaptiveResourceDistributor2 {

    /**
     * This class represents the result of the calculations for resource distribution
     */
    public static class Result {

        public Result(List<Float> assignedResources, float variation) {
            this.assignedResources = assignedResources;
            this.variation = variation;
        }

        /**
         * Assigned resources to each stakeholder (in the same order as the input parameters)
         */
        public List<Float> assignedResources;

        /**
         * This value indicates how much the assignations have changed from the previous calculation. It goes from 0
         * to 1, with 0 indicating that no variation took place, and values closer to 1 indicating a higher variation.
         */
        public float variation;
    }


    private static final float MAX_ALLOWED_INCREASE_FACTOR = 2f;

    private static final float MAX_FACTOR_FOR_TOP_VARIATION = 1.2f;

    private static final float FACTOR_OF_WISHED_DISTANCE_MOVED = 0.5f;

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
     * @param stakeholdersMaxDesiredResources
     *                                 the maximum desired amount of resources by each stakeholder (if filled with null values, no limit)
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
    public static Result distributeResources(
            Float maxDesiredGivenResources,
            List<Float> stakeholdersPreviousToLastAssignedResources,
            List<Float> stakeholdersLastAssignedResources,
            List<Float> stakeholdersLastConsumedResources,
            List<Float> wishedDistribution,
            List<Float> stakeholdersMaxDesiredResources,
            float threshold,
            float loweringPercentage) throws IllegalArgumentException {
        // the max resources are distributed between all the stakeholders. We first calculate how much each
        // stakeholder could take, and if the total exceeds the maximum allowed, we retrieve linearly from each.

        // check parameters validity
        int size = checkParametersAndReturnSize(stakeholdersPreviousToLastAssignedResources, stakeholdersLastAssignedResources, stakeholdersLastConsumedResources, wishedDistribution, stakeholdersMaxDesiredResources);

        // if the previous to last assignment is null (meaning that the user does not want this correction to be
        // applied) we set it to the last assignment (this way it has no effect)
        if (stakeholdersPreviousToLastAssignedResources == null) {
            stakeholdersPreviousToLastAssignedResources = stakeholdersLastAssignedResources;
        }

        // initialize result vector and variables
        List<Float> assignedResources = new ArrayList<>(size);

        // calculate resources for each stakeholder
        float totalAssignation = 0f;
        for (int i = 0; i < size; i++) {
            float newAssignation = reevaluateResourceAssignation(
                    stakeholdersPreviousToLastAssignedResources.get(i),
                    stakeholdersLastAssignedResources.get(i),
                    stakeholdersLastConsumedResources.get(i),
                    threshold,
                    loweringPercentage);
            assignedResources.add(newAssignation);
            totalAssignation += newAssignation;
        }
        List<Float> wishedAssignedResources = generateWishedConsumedResources(wishedDistribution, totalAssignation);
        forceWishedDistribution(assignedResources, wishedAssignedResources);

        // apply max limitation
        applyMaxAllowedResources(maxDesiredGivenResources, stakeholdersMaxDesiredResources, assignedResources, size);
        float variation = calculateVariation(assignedResources, stakeholdersLastConsumedResources);
        return new Result(assignedResources, variation);
    }

    private static int checkParametersAndReturnSize(
            List<Float> stakeholdersPreviousToLastAssignedResources,
            List<Float> stakeholdersLastAssignedResources,
            List<Float> stakeholdersLastConsumedResources,
            List<Float> wishedDistribution,
            List<Float> stakeholdersMaxDesiredResources) {
        // copy the vector maxSize for future use
        int size = stakeholdersLastConsumedResources.size();
        // check parameters are correct
        Lists.checkNonNullAndEqualInSize(
                stakeholdersPreviousToLastAssignedResources,
                stakeholdersLastAssignedResources,
                stakeholdersLastConsumedResources,
                wishedDistribution,
                stakeholdersMaxDesiredResources);

        return size;
    }

    private static List<Float> generateWishedConsumedResources(List<Float> wishedDistribution, float totalAssignment) {
        // generate a list with the consumed resources that we wish we would have achieved, according to the wished distribution
        float totalWish = 0f;
        for (float oneWish : wishedDistribution) {
            totalWish += oneWish;
        }
        List<Float> wishedConsumedResources = new ArrayList<>();
        for (float oneWish : wishedDistribution) {
            wishedConsumedResources.add(oneWish * totalAssignment / totalWish);
        }
        return wishedConsumedResources;
    }


    public static float reevaluateResourceAssignation(
            Float previousToLastAssignedValue,
            float lastAssignedValue,
            float consumedValue,
            float threshold,
            float loweringPercentage) {
        // if the achieved value is under the threshold, the assigned value must be lowered, otherwise it will be
        // increased a bit (just to stay in the threshold, but not exceeding the desired value)

        // first check if we have previous to last value (if not, assign last value)
        if (previousToLastAssignedValue == null) {
            previousToLastAssignedValue = lastAssignedValue;
        }
        if (consumedValue < lastAssignedValue * threshold) {
            return lowerAssignation(lastAssignedValue, consumedValue, loweringPercentage);
        } else {
            return increaseAssignation(previousToLastAssignedValue, lastAssignedValue, consumedValue, threshold);
        }
    }

    private static float lowerAssignation(float lastAssignedValue, float consumedValue, float loweringPercentage) {
        // assigned value is lowered by a lowerAssignation factor of the difference between lastAssignedValue and achieved value
        float variation = -((lastAssignedValue - consumedValue) * loweringPercentage);
        return lastAssignedValue + variation;
    }

    private static float increaseAssignation(
            float previousToLastAssignedValue,
            float lastAssignedValue,
            float consumedValue,
            float threshold) {
        // the assigned value is set so the last achieved value stays in the threshold with respect to it
        float newAssignedValue = consumedValue / threshold;
        newAssignedValue = applyIncreaseCorrection(previousToLastAssignedValue, lastAssignedValue, newAssignedValue);
        float variation = newAssignedValue - consumedValue;
        return consumedValue + variation;
    }

    private static float applyIncreaseCorrection(float previousToLastAssignedValue, float lastAssignedValue, float newAssignedValue) {
        float factorForNewAssignation = newAssignedValue / lastAssignedValue;
        float factorForPreviousAssignation = lastAssignedValue / previousToLastAssignedValue;
        if (factorForNewAssignation > 1f && factorForPreviousAssignation > 1f) {
            // if there has been two increases in a row, we apply the correction so the increase is bigger. The actual increase will be the
            // addition of this increase with the last increase. The final increase has a limitation of 2f
            float newFactor = factorForNewAssignation + factorForPreviousAssignation - 1.0f;
            if (newFactor > MAX_ALLOWED_INCREASE_FACTOR) {
                newFactor = MAX_ALLOWED_INCREASE_FACTOR;
            }
            newAssignedValue = lastAssignedValue * newFactor;
        }
        return newAssignedValue;
    }

    private static void forceWishedDistribution(List<Float> assignedResources, List<Float> wishedAssignedResources) {
        // first calculate how different the assignment is to the wished assignment
        // from that value, calculate how much of the total assignment is going to be moved from the stakeholders to lower to the
        // stakeholders to increase. This amount is a percentage (up to a 5%) of the total assignation
        // the amount is linearly given to the stakeholders that are increased, and linearly retrieved from the decreased stakeholders
        float wishDistance = calculateWishDistance(assignedResources, wishedAssignedResources);
        if (wishDistance != 0f) {
            float amount = FACTOR_OF_WISHED_DISTANCE_MOVED * wishDistance;
            for (int i = 0; i < assignedResources.size(); i++) {
                float factor = (wishedAssignedResources.get(i) - assignedResources.get(i)) / wishDistance;
                assignedResources.set(i, assignedResources.get(i) + factor * amount);
            }
        }
    }

    private static float calculateWishDistance(List<Float> assignedResources, List<Float> wishedAssignedResources) {
        // get the total resources that should be moved to fix things
        float distance = 0f;
        for (int i = 0; i < assignedResources.size(); i++) {
            if (wishedAssignedResources.get(i) > assignedResources.get(i)) {
                distance += wishedAssignedResources.get(i) - assignedResources.get(i);
            }
        }
        return distance;
    }

    /**
     * Applies the maximum resource limitation to an array of assigned resources
     *
     * @param maxDesiredGivenResources max desired limitation of given resources (null if no limitation)
     * @param stakeholdersMaxDesiredResources
     *                                 max resources for each stakeholder
     * @param assignedResources        resources assigned to each stakeholder
     */
    private static void applyMaxAllowedResources(
            Float maxDesiredGivenResources,
            List<Float> stakeholdersMaxDesiredResources,
            List<Float> assignedResources,
            int size) {
        // first check the individual limits
        float totalAssignation = 0f;
        for (int i = 0; i < size; i++) {
            if (stakeholdersMaxDesiredResources.get(i) != null && assignedResources.get(i) > stakeholdersMaxDesiredResources.get(i)) {
                assignedResources.set(i, stakeholdersMaxDesiredResources.get(i));
            }
            totalAssignation += assignedResources.get(i);
        }
        // then, check the total limit. If the limit is surpassed, scale down all values linearly
        if (maxDesiredGivenResources != null && totalAssignation > maxDesiredGivenResources) {
            float factor = maxDesiredGivenResources / totalAssignation;
            for (int i = 0; i < size; i++) {
                assignedResources.set(i, assignedResources.get(i) * factor);
            }
        }
    }

    private static float calculateVariation(List<Float> assignedResources, List<Float> stakeholdersLastConsumedResources) {
        float variation = 0f;
        for (int i = 0; i < assignedResources.size(); i++) {
            variation = updateVariation(variation, calculateOneVariation(stakeholdersLastConsumedResources.get(i), assignedResources.get(i)));
        }
        return variation;
    }

    private static float calculateOneVariation(float oldValue, float newValue) {
        // the variation grows linearly with the factor of values. Anything above a given threshold is considered
        // as variation 1
        float factor = newValue / oldValue;
        if (factor < 1f) {
            factor = 1f / factor;
        }
        float variation = (factor - 1f) / (MAX_FACTOR_FOR_TOP_VARIATION - 1f);
        if (variation > 1.0f) {
            variation = 1.0f;
        }
        return variation;
    }

    private static float updateVariation(float oldVariation, float newVariation) {
        // we keep the max of the two values
        return oldVariation > newVariation ? oldVariation : newVariation;
    }
}
