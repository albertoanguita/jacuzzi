package org.aanguita.jacuzzi.AI.resource_distribution;

import org.aanguita.jacuzzi.lists.tuple.Triple;

import java.util.Collection;

/**
 * Created by Alberto on 21/09/2015.
 */
public class PriorityResourceDistribution {

    public static abstract class ResourceData {

        public abstract float getPriority();

        public abstract float getConsumption();

        public float variation;

        public float getVariation() {
            return variation;
        }
    }

    public static float distributeResources(Collection<? extends ResourceData> resourceDataSet) {
        return distributeResources(resourceDataSet, null);
    }

    public static float distributeResources(Collection<? extends ResourceData> resourceDataSet, Float totalMaxDesiredSpeed) {
        Triple<Float, Float, Float> factors = getTotalPriorityConsumptionLimitation(resourceDataSet, totalMaxDesiredSpeed);
        for (ResourceData resourceData : resourceDataSet) {
            resourceData.variation = resourceData.getPriority() * factors.element2 * factors.element3 / resourceData.getConsumption() / factors.element1;
        }
        return factors.element2;
    }

    private static Triple<Float, Float, Float> getTotalPriorityConsumptionLimitation(Collection<? extends ResourceData> values, Float totalMaxDesiredSpeed) {
        float totalPriority = 0f;
        float totalConsumption = 0f;
        for (ResourceData resourceData : values) {
            totalPriority += resourceData.getPriority();
            totalConsumption += resourceData.getConsumption();
        }
        float limitFactor = 1f;
        if (totalMaxDesiredSpeed != null && totalMaxDesiredSpeed < totalConsumption) {
            limitFactor = totalMaxDesiredSpeed / totalConsumption;
        }
        return new Triple<>(totalPriority, totalConsumption, limitFactor);
    }
}
