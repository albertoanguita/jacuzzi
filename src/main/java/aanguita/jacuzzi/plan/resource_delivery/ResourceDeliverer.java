package aanguita.jacuzzi.plan.resource_delivery;

import aanguita.jacuzzi.concurrency.timer.TimerAction;
import aanguita.jacuzzi.concurrency.timer.Timer;
import aanguita.jacuzzi.queues.event_processing.MessageHandler;
import aanguita.jacuzzi.queues.event_processing.MessageProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class provides priority-based data processing through priority queues. It is not finalized nor well-tested.
 * It should not be used.
 * <p/>
 * todo remove if not used in a long time
 */
public class ResourceDeliverer<T, Y extends Resource> implements TimerAction {

    private static final int INITIAL_QUEUE_CAPACITY = 1024;

    private static final int DEFAULT_CAPACITY = 1024;

    private static final long DEFAULT_MILLIS_TO_STORE = 1000;

    private static final long MILLIS_FOR_CLEANUP = 20000;

    private Map<T, TargetResource<T, Y>> resources;

    private PriorityBlockingQueue<TargetResource<T, Y>> targetQueue;

    private Semaphore availableSpace;

    private ReentrantLock accessLock;

    private MessageProcessor messageProcessor;

    private ResourceDelivererMessageReader<T, Y> resourceDelivererMessageReader;

    private double maxRatio;

    private Timer cleanupTimer;


    public ResourceDeliverer(MessageHandler messageHandler) {
        this(messageHandler, "");
    }

    public ResourceDeliverer(MessageHandler messageHandler, String threadsName) {
        this(messageHandler, threadsName, DEFAULT_CAPACITY, null, DEFAULT_MILLIS_TO_STORE);
    }

    public ResourceDeliverer(MessageHandler messageHandler, int capacity, Double maxSpeed, long millisToStore) {
        this(messageHandler, "", capacity, maxSpeed, millisToStore);
    }

    public ResourceDeliverer(MessageHandler messageHandler, String threadsName, int capacity, Double maxSpeed, long millisToStore) {
        resources = new HashMap<>();
        targetQueue = new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY);
        availableSpace = new Semaphore(capacity, true);
        accessLock = new ReentrantLock(true);
        resourceDelivererMessageReader = new ResourceDelivererMessageReader<>(targetQueue, this, maxSpeed, millisToStore);
        messageProcessor = new MessageProcessor(threadsName + ":" + ResourceDeliverer.class.getName(), resourceDelivererMessageReader, messageHandler, false);
        maxRatio = 0.0d;
        cleanupTimer = new Timer(MILLIS_FOR_CLEANUP, this, threadsName + ":" + ResourceDeliverer.class.getName());
        messageProcessor.start();
    }

    public void setMaxSpeed(double speed) {
        resourceDelivererMessageReader.setMaxSpeed(speed);
    }

    public void setDestination(T destination, int priority) {
        accessLock.lock();
        try {
            if (!resources.containsKey(destination)) {
                resources.put(destination, new TargetResource<>(destination, priority, maxRatio));
            } else {
                resources.get(destination).setPriority(priority, maxRatio);
            }
        } finally {
            if (accessLock.isHeldByCurrentThread()) {
                accessLock.unlock();
            }
        }
    }

    public void removeDestination(T destination) {
        accessLock.lock();
        try {
            if (resources.containsKey(destination)) {
                resources.remove(destination);
            }
        } finally {
            if (accessLock.isHeldByCurrentThread()) {
                accessLock.unlock();
            }
        }
    }

    public void send(T destination, List<Y> data) {
        availableSpace.acquireUninterruptibly(data.size());
        accessLock.lock();
        try {
            if (resources.containsKey(destination) && data.size() != 0) {
                boolean mustInsertInTargetQueue = resources.get(destination).empty();
                for (Y datum : data) {
                    resources.get(destination).addResource(datum);
                }
                if (mustInsertInTargetQueue) {
                    resources.get(destination).updateRatio(maxRatio);
                    targetQueue.add(resources.get(destination));
                }
            }
        } finally {
            if (accessLock.isHeldByCurrentThread()) {
                accessLock.unlock();
            }
        }
    }

    void releaseSpace(int size) {
        availableSpace.release(size);
    }

    void setMaxRatio(double ratio) {
        accessLock.lock();
        try {
            if (ratio > maxRatio) {
                maxRatio = ratio;
            }
        } finally {
            if (accessLock.isHeldByCurrentThread()) {
                accessLock.unlock();
            }
        }
    }

    @Override
    public Long wakeUp(Timer timer) {
        cleanupProgress();
        return null;
    }

    private synchronized void cleanupProgress() {
        accessLock.lock();
        try {
            // todo try erase progress
        } finally {
            if (accessLock.isHeldByCurrentThread()) {
                accessLock.unlock();
            }
        }
    }

    public void stop() {
        accessLock.lock();
        try {
            resources.clear();
            targetQueue.add(new TargetResourceFinalizationMessage<>());
            if (cleanupTimer != null) {
                cleanupTimer.kill();
            }
            messageProcessor.stop();
        } finally {
            if (accessLock.isHeldByCurrentThread()) {
                accessLock.unlock();
            }
        }
    }
}
