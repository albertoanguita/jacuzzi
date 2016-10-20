package org.aanguita.jacuzzi.plan.resource_delivery;

import org.aanguita.jacuzzi.time.SpeedLimiter;
import org.aanguita.jacuzzi.queues.processor.FinishReadingMessagesException;
import org.aanguita.jacuzzi.queues.processor.MessageReader;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * todo test when handling the data actually takes time
 */
class ResourceDelivererMessageReader<T, Y extends Resource> implements MessageReader {

    private PriorityBlockingQueue<TargetResource<T, Y>> targetQueue;

    private ResourceDeliverer resourceDeliverer;

    private SpeedLimiter speedLimiter;

    ResourceDelivererMessageReader(PriorityBlockingQueue<TargetResource<T, Y>> targetQueue, ResourceDeliverer resourceDeliverer, Double maxSpeed, long millisToMeasure) {
        this.targetQueue = targetQueue;
        this.resourceDeliverer = resourceDeliverer;
        speedLimiter = new SpeedLimiter(millisToMeasure, maxSpeed);
    }

    public synchronized void setMaxSpeed(Double maxSpeed) {
        speedLimiter.setSpeedLimit(maxSpeed);
    }

    @Override
    public synchronized Object readMessage() throws FinishReadingMessagesException {
        TargetResource<T, Y> targetResource = null;
        do {
            try {
                targetResource = targetQueue.take();
            } catch (InterruptedException e) {
                // ignore
            }
        } while (targetResource == null);
        if (targetResource.isFinalizationMessage()) {
            throw new FinishReadingMessagesException();
        }
        TargetAndResource<T, Y> targetAndResource = targetResource.getTargetAndResource();
        resourceDeliverer.setMaxRatio(targetResource.getRatio());
        speedLimiter.addProgress(targetAndResource.getResource().size());
        resourceDeliverer.releaseSpace(targetAndResource.getResource().size());
        if (!targetResource.empty()) {
            targetQueue.add(targetResource);
        }
        return targetAndResource;
    }

    @Override
    public void stop() {
        System.out.println("Reader stopped");
        speedLimiter.stop();
    }
}
