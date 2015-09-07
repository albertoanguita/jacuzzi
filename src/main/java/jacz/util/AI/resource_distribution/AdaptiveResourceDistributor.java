package jacz.util.AI.resource_distribution;

import jacz.util.lists.Duple;
import jacz.util.lists.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * This class offers and adaptive method for distributing resources among a set of stakeholders. Resources are
 * given as evenly as possible, although limits can be set for each stakeholder. This method is meant to be invoked
 * regularly to adapt resources to evolving needs. Resources will be given or taken from stakeholders based on
 * whether these actually were able to consume the resources they were provided. For this, the main method accepts
 * parameters which indicate older resource assignations and current resource consumption. In general terms, if a
 * stakeholder was able to consume all (or almost all) resources it was assigned, its assignation will increase. If
 * it felt short, its assignation will decrease (benefiting other stakeholders). Two parameters determine how large
 * these variations are: threshold and loweringFactor.
 * <p/>
 * threshold: determines if a speed reached expectations or was lower than expected. A value of 0.97 (97%) should
 * give a good balance between space for speeds to grow and too high speed limits.
 * <p/>
 * loweringFactor: indicates how much a value is lowered when it must be lowered. It represents the percentage
 * of the difference between the expected speed and the achieved speed is removed for determining the assigned
 * speed. For example, if the assigned speed was 100, but the peer achieved only 90 (and the threshold is larger than
 * 0.90 so the assignation must be lowered) and the lowering percentage is 0.20 (20%), then the assigned speed is
 * lowered by the 20% of 10 (100 - 90), resulting a value of 98. We should avoid too high values here that produce
 * a bouncing situation, and too low values that make speed go down too slowly. 0.20 should give a good balance, if
 * calculations are repeated every few seconds.
 * </p>
 * This class should be invoked regularly. Pairs of invocations should not be too close to each other, since measures
 * would carry errors (and the extra calculations could hurt performance), and they should not be too far away from
 * each other, because changes would take too long to take effect. A few seconds should be a good balance (e.g. 3 secs)
 * </p>
 * If the total assignation exceeds a total wished, assigned values are adjusted according to the wished distribution.
 */
public class AdaptiveResourceDistributor {

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

        // copy the vector maxSize for future use
        int size = stakeholdersLastConsumedResources.size();

        // if the previous to last assignment is null (meaning that the user does not want this correction to be
        // applied) we set it to the last assignment (this way it has no effect)
        if (stakeholdersPreviousToLastAssignedResources == null) {
            stakeholdersPreviousToLastAssignedResources = stakeholdersLastAssignedResources;
        }
        // check wished distribution for null value (if so, initialize with identical values)
        if (wishedDistribution == null) {
            wishedDistribution = new ArrayList<Float>(size);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < size; i++) {
                wishedDistribution.add(1f);
            }
        }
        // check parameters are correct
        Lists.checkNonNullAndEqualInSize(
                stakeholdersPreviousToLastAssignedResources,
                stakeholdersLastAssignedResources,
                stakeholdersLastConsumedResources,
                wishedDistribution,
                stakeholdersMaxDesiredResources);

        // initialize result vector and variables
        List<Float> assignedResources = new ArrayList<Float>(size);
        Float totalAssignation = 0f;
        float variation = 0f;

        // calculate resources for each stakeholder
        for (int i = 0; i < size; i++) {
            Duple<Float, Float> newAssignationAndVariation = reevaluateResourceAssignation(
                    stakeholdersMaxDesiredResources.get(i),
                    stakeholdersPreviousToLastAssignedResources.get(i),
                    stakeholdersLastAssignedResources.get(i),
                    stakeholdersLastConsumedResources.get(i),
                    threshold,
                    loweringPercentage);
            Float newAssignation = newAssignationAndVariation.element1;
            totalAssignation += newAssignation;
            assignedResources.add(newAssignation);
            variation = updateVariation(variation, newAssignationAndVariation.element2);
        }

        // apply max limitation
        applyMaxAllowedResources(maxDesiredGivenResources, wishedDistribution, totalAssignation, assignedResources);
        return new Result(assignedResources, variation);
    }

    private static float updateVariation(float oldVariation, float newVariation) {
        // we keep the max of the two values
        return oldVariation > newVariation ? oldVariation : newVariation;
    }


    public static Duple<Float, Float> reevaluateResourceAssignation(
            Float desiredValue,
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
            return lowerAssignation(desiredValue, lastAssignedValue, consumedValue, loweringPercentage);
        } else {
            return increaseAssignation(desiredValue, previousToLastAssignedValue, lastAssignedValue, consumedValue, threshold);
        }
    }

    private static Duple<Float, Float> lowerAssignation(Float desiredValue, float lastAssignedValue, float consumedValue, float loweringPercentage) {
        // assigned value is lowered by a lowerAssignation factor of the difference between lastAssignedValue and
        // achieved value, or set to desired value if this value is even lower
        float newAssignedValue = lastAssignedValue - ((lastAssignedValue - consumedValue) * loweringPercentage);
        if (desiredValue != null) {
            // apply limitation due to max desired value
            newAssignedValue = newAssignedValue < desiredValue ? newAssignedValue : desiredValue;
        }
        float variation = calculateVariation(newAssignedValue, lastAssignedValue);
        return new Duple<Float, Float>(newAssignedValue, variation);
    }

    private static Duple<Float, Float> increaseAssignation(
            Float desiredValue,
            float previousToLastAssignedValue,
            float lastAssignedValue,
            float consumedValue,
            float threshold) {
        // the assigned value is set so the last achieved value stays in the threshold with respect to it
        // if de desired value is less than this value, then the value of desiredValue is used
        float newAssignedValue = consumedValue / threshold;
        newAssignedValue = applyIncreaseCorrection(previousToLastAssignedValue, lastAssignedValue, newAssignedValue);
        if (desiredValue != null) {
            // apply limitation due to max desired value
            newAssignedValue = newAssignedValue < desiredValue ? newAssignedValue : desiredValue;
        }
        float variation = calculateVariation(newAssignedValue, lastAssignedValue);
        return new Duple<Float, Float>(newAssignedValue, variation);
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

    private static float calculateVariation(float oldValue, float newValue) {
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

    /**
     * Applies the maximum resource limitation to an array of assigned resources
     *
     * @param maxDesiredGivenResources max desired limitation of given resources (null if no limitation)
     * @param wishedDistribution       wished distribution of given resources among stakeholders
     * @param totalAssignation         total amount of assigned resources
     * @param assignedResources        resources assigned to each stakeholder
     */
    private static void applyMaxAllowedResources(
            Float maxDesiredGivenResources,
            List<Float> wishedDistribution,
            Float totalAssignation,
            List<Float> assignedResources) {
        // we gave too many resources, retrieve a little bit from each stakeholder (so we give the max allowed)
        // the variation factor is recalculated.
        // at this stage we can apply the wished distribution. Resources are retrieved from those stakeholders that
        // exceed their wished distribution (the more it exceeds, the more is retrieved)

        // the ideal distribution can be calculated from the max allowed and the wished distribution list. Then we can compare this list to the
        // actual assigned resources. The stakeholders that don't reach the ideal value will "gift" those extra resources to the other, so the
        // others don't have to give up so much. We evaluate this recursively, eliminating stakeholders that don't reach the ideal. When there
        // are only stakeholders taking more than ideal in the list, we just apply the ideal distribution

        // the max desired resources might be limited by the achieved total assignation -> calculate it
        maxDesiredGivenResources = maxDesiredGivenResources != null ? Math.min(maxDesiredGivenResources, totalAssignation) : totalAssignation;
//        System.out.println("max desired: " + maxDesiredGivenResources);

        // ideal distribution list
        List<Float> idealAssignation = new ArrayList<Float>(wishedDistribution);
        Lists.normalizeList(idealAssignation, maxDesiredGivenResources);

        // if all stakeholders are getting more than their ideal assignation, then the correct assignation is this ideal assignation
        // otherwise, we must get some resources from the ones that overpass their ideal assignation
        if (allStakeholdersGetMoreThanIdeal(idealAssignation, assignedResources)) {
            for (int i = 0; i < assignedResources.size(); i++) {
                assignedResources.set(i, idealAssignation.get(i));
//                System.out.println("ideal: " + idealAssignation.get(i));
            }
            // todo variation??? it must be calculated some other way, comparing final with previous
        } else {
            // asses the amount of resources gifted by those stakeholders that do not reach their ideal assignation
            // also, gather which stakeholders overpass their ideal assignation and build a new list with them
            // a map is also built to maintain the correspondence of the new list with the old list
            Float maxDesiredGivenResourcesOverpass = 0f;
            List<Float> assignedResourcesOverpass = new ArrayList<Float>();
            List<Float> wishedDistributionOverpass = new ArrayList<Float>();
            Float totalAssignationOverpass = 0f;
            List<Integer> newToOldListMap = new ArrayList<Integer>();
            for (int i = 0; i < assignedResources.size(); i++) {
                if (idealAssignation.get(i) >= assignedResources.get(i)) {
                    // this stakeholder gifts resources
                    maxDesiredGivenResourcesOverpass += idealAssignation.get(i) - assignedResources.get(i);
                } else {
                    // this stakeholder overpasses its ideal assignation, so it will receive resources from others -> to the new list
                    maxDesiredGivenResourcesOverpass += idealAssignation.get(i);
                    assignedResourcesOverpass.add(assignedResources.get(i));
                    wishedDistributionOverpass.add(wishedDistribution.get(i));
                    totalAssignationOverpass += assignedResources.get(i);
                    newToOldListMap.add(i);
                }
            }
            // now perform the same calculation recursively
            applyMaxAllowedResources(maxDesiredGivenResourcesOverpass, wishedDistributionOverpass, totalAssignationOverpass, assignedResourcesOverpass);
            // apply modification of new list to old list
            for (int i = 0; i < newToOldListMap.size(); i++) {
                int oldPosition = newToOldListMap.get(i);
                assignedResources.set(oldPosition, assignedResourcesOverpass.get(i));
            }
        }
    }

    /**
     * Applies the maximum resource limitation to an array of assigned resources
     *
     * @param maxDesiredGivenResources max desired limitation of given resources (null if no limitation)
     * @param wishedDistribution       wished distribution of given resources among stakeholders
     * @param totalAssignation         total amount of assigned resources
     * @param assignedResources        resources assigned to each stakeholder
     */
    private static void applyMaxAllowedResourcesOld(
            Float maxDesiredGivenResources,
            List<Float> wishedDistribution,
            Float totalAssignation,
            List<Float> assignedResources) {
        // we gave too many resources, retrieve a little bit from each stakeholder (so we give the max allowed)
        // the variation factor is recalculated.
        // at this stage we can apply the wished distribution. Resources are retrieved from those stakeholders that
        // exceed their wished distribution (the more it exceeds, the more is retrieved)

        // the ideal distribution can be calculated from the max allowed and the wished distribution list. Then we can compare this list to the
        // actual assigned resources. The stakeholders that don't reach the ideal value will "gift" those extra resources to the other, so the
        // others don't have to give up so much. We evaluate this recursively, eliminating stakeholders that don't reach the ideal. When there
        // are only stakeholders taking more than ideal in the list, we just apply the ideal distribution
        if (maxDesiredGivenResources != null && totalAssignation > maxDesiredGivenResources) {
            // ideal distribution list
            List<Float> idealAssignation = new ArrayList<Float>(wishedDistribution);
            Lists.normalizeList(idealAssignation, maxDesiredGivenResources);

            // if all stakeholders are getting more than their ideal assignation, then the correct assignation is this ideal assignation
            // otherwise, we must get some resources from the ones that overpass their ideal assignation
            if (allStakeholdersGetMoreThanIdeal(idealAssignation, assignedResources)) {
                for (int i = 0; i < assignedResources.size(); i++) {
                    assignedResources.set(i, idealAssignation.get(i));
                }
                // todo variation??? it must be calculated some other way, comparing final with previous
            } else {
                // asses the amount of resources gifted by those stakeholders that do not reach their ideal assignation
                // also, gather which stakeholders overpass their ideal assignation and build a new list with them
                // a map is also built to maintain the correspondence of the new list with the old list
                Float maxDesiredGivenResourcesOverpass = 0f;
                List<Float> assignedResourcesOverpass = new ArrayList<Float>();
                List<Float> wishedDistributionOverpass = new ArrayList<Float>();
                Float totalAssignationOverpass = 0f;
                List<Integer> newToOldListMap = new ArrayList<Integer>();
                for (int i = 0; i < assignedResources.size(); i++) {
                    if (idealAssignation.get(i) >= assignedResources.get(i)) {
                        // this stakeholder gifts resources
                        maxDesiredGivenResourcesOverpass += idealAssignation.get(i) - assignedResources.get(i);
                    } else {
                        // this stakeholder overpasses its ideal assignation -> to the new list
                        maxDesiredGivenResourcesOverpass += idealAssignation.get(i);
                        assignedResourcesOverpass.add(assignedResources.get(i));
                        wishedDistributionOverpass.add(wishedDistribution.get(i));
                        totalAssignationOverpass += assignedResources.get(i);
                        newToOldListMap.add(i);
                    }
                }
                // now perform the same calculation recursively
                applyMaxAllowedResources(maxDesiredGivenResourcesOverpass, wishedDistributionOverpass, totalAssignationOverpass, assignedResourcesOverpass);
                // apply modification of new list to old list
                for (int i = 0; i < newToOldListMap.size(); i++) {
                    int oldPosition = newToOldListMap.get(i);
                    assignedResources.set(oldPosition, assignedResourcesOverpass.get(i));
                }
            }
        }
    }

    private static boolean allStakeholdersGetMoreThanIdeal(List<Float> idealAssignation, List<Float> assignedResources) {
        for (int i = 0; i < idealAssignation.size(); i++) {
            if (idealAssignation.get(i) >= assignedResources.get(i)) {
                return false;
            }
        }
        return true;
    }
}
