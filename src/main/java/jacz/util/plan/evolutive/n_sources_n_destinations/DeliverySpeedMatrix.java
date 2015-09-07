package jacz.util.plan.evolutive.n_sources_n_destinations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DeliverySpeedMatrix<T, Y> {

    private class ResourceTypeInfo {

        int position;

        int order;

        double priority;

        double cumulativeSpeed;

        private ResourceTypeInfo(int position, int order, double priority) {
            this.position = position;
            this.order = order;
            this.priority = priority;
            cumulativeSpeed = 0.0d;
        }
    }

    private int resourceTypeCount;

    private int sourceCount;

    private int resourceTypeAllocation;

    private int sourceAllocation;

    private Map<T, ResourceTypeInfo> resourceTypesData;

    private Map<Y, Integer> sourcesPositions;


    /**
     * This matrix contains the measured incoming speeds. Each row represents a type of resource, and
     * each column represents a source sending us resources. Values in the matrix indicate the speed at
     * which we are receiving a type of resource from a specific source, always >= 0.0d. It can also hold
     * null values, indicating that the source is not trying to transfer us the resource (e.g. not connected,
     * or does not have that resource)
     * <p/>
     * This matrix can grow or shrink as needed, but it is best to not modify its dimensions very often, because
     * the whole matrix must be re-created. Non-available sources can be stored here, just in case they become
     * available soon (having idle sources does not imply much higher computational costs, so we can afford it)
     */
    private Double[][] speeds;


    public synchronized void addResourceType(T resourceType, int order, double priority) {
        addOneResourceType(resourceType, order, priority);
    }

    public synchronized void addResourceTypes(List<T> resourceTypes, List<Integer> orders, List<Double> priorities) throws IndexOutOfBoundsException {
        if (resourceTypes.size() != orders.size() || resourceTypes.size() != priorities.size()) {
            throw new IndexOutOfBoundsException("Sizes of the lists are not equal");
        }
        for (int i = 0; i < resourceTypes.size(); i++) {
            addOneResourceType(resourceTypes.get(i), orders.get(i), priorities.get(i));
        }
    }

    private void addOneResourceType(T resourceType, int order, double priority) {
        // first check the resource type does not already exist, then push it after the last row
        // the matrix must have already the allocated space for this new row, even in the case it is not
        // finally added
        // speeds for this new resource are initialized to null
        if (!resourceTypesData.containsKey(resourceType)) {
            for (int i = 0; i < sourceCount; i++) {
                speeds[resourceTypeCount][i] = null;
            }
            resourceTypesData.put(resourceType, new ResourceTypeInfo(resourceTypeCount++, order, priority));
        }
    }


    public synchronized void addSource(Y source) {
        List<Y> sources = new ArrayList<Y>(1);
        sources.add(source);
        addSources(sources);
    }

    public synchronized void addSources(List<Y> sources) {
        for (Y source : sources) {
            addOneSource(source);
        }
    }

    private void addOneSource(Y source) {
        // first check the source does not already exist, then push it after the last column
        // the matrix must have already the allocated space for this new column, even in the case it is not
        // finally added
        // speeds for this new source are initialized to null
        if (!sourcesPositions.containsKey(source)) {
            for (int i = 0; i < resourceTypeCount; i++) {
                speeds[i][sourceCount] = null;
            }
            sourcesPositions.put(source, sourceCount++);
        }
    }

    private void allocateSpace(int newResourceTypes, int newSources) {
        // todo use initial capacity and load factor as in java classes

    }

    // todo update cumulative speeds
    public synchronized void updateSpeed(T resourceType, Y source, Double speed) {
        if (resourceTypesData.containsKey(resourceType) && sourcesPositions.containsKey(source)) {
            speeds[resourceTypesData.get(resourceType).position][sourcesPositions.get(source)] = speed;
        }
    }

    public synchronized void updateSpeed(T resourceType, Map<Y, Double> sourcesAndSpeeds) {
        if (resourceTypesData.containsKey(resourceType)) {
            int resourcePosition = resourceTypesData.get(resourceType).position;
            for (Y source : sourcesAndSpeeds.keySet()) {
                if (sourcesPositions.containsKey(source)) {
                    speeds[resourcePosition][sourcesPositions.get(source)] = sourcesAndSpeeds.get(source);
                }
            }
        }
    }

    public synchronized void updateSpeed(Map<T, Double> resourceTypesAndSpeeds, Y source) {
        if (sourcesPositions.containsKey(source)) {
            int sourcePosition = sourcesPositions.get(source);
            for (T resourceType : resourceTypesAndSpeeds.keySet()) {
                if (resourceTypesData.containsKey(resourceType)) {
                    speeds[resourceTypesData.get(resourceType).position][sourcePosition] = resourceTypesAndSpeeds.get(resourceType);
                }
            }
        }
    }


    private void checkSpeeds() {
        for (Y source : sourcesPositions.keySet()) {
            checkOneSourceSpeeds(source);
        }
    }

    private void checkOneSourceSpeeds(Y source) {
        // first we check that the order preference is fulfilled. If not, we will send corrections to fix it. If order
        // is ok, we will check priorities, and send corrections if needed.

    }

    private boolean checkOrderForOneSource(Y source) {
        // todo

        return true;
    }


}
