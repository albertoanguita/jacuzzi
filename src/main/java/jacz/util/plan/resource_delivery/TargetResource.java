package jacz.util.plan.resource_delivery;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 *
 */
class TargetResource<T, Y extends Resource> implements Comparable<TargetResource> {

    /**
     * null indicates that this is a finalization message. It has the lowest priority (highest to any other
     * TargetResource) so it is the last one processed
     */
    final T target;

    double priority;

    double resourcesSent;

    private double ratio;

    Queue<Y> resourceQueue;

    TargetResource(T target, int priority, double initialRatio) {
        this.target = target;
        this.priority = (double) priority;
        updateRatio(initialRatio);
        resourceQueue = new ArrayDeque<Y>();
    }

    synchronized boolean empty() {
        return resourceQueue.size() == 0;
    }

    boolean isFinalizationMessage() {
        return this instanceof TargetResourceFinalizationMessage;
    }

    synchronized void setPriority(int priority, double newRatio) {
        this.priority = (double) priority;
        updateRatio(newRatio);
    }

    synchronized void updateRatio(double newRatio) {
        // modify sent data to fit the new ratio
        this.ratio = newRatio;
        resourcesSent = priority * ratio;
    }

    synchronized TargetAndResource<T, Y> getTargetAndResource() {
        Y resource = resourceQueue.remove();
        if (resource != null) {
            resourcesSent += (double) resource.size();
            calculateRatio();
            return new TargetAndResource<T, Y>(target, resource);
        } else {
            return null;
        }
    }

    synchronized double getRatio() {
        return ratio;
    }

    private void calculateRatio() {
        ratio = resourcesSent / priority;
    }

    synchronized void addResource(Y resource) {
        resourceQueue.add(resource);
    }

    @Override
    public int compareTo(TargetResource o) {
        if (isFinalizationMessage()) {
            return 1;
        } else if (o.isFinalizationMessage()) {
            return -1;
        } else {
            return Double.compare(ratio, o.ratio);
        }
    }
}
