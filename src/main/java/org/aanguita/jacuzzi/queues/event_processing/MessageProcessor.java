package org.aanguita.jacuzzi.queues.event_processing;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.execution_control.TrafficControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class MessageProcessor<E> {

    private Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    /**
     * Default capacity for the event queue
     */
    private final static int DEFAULT_QUEUE_CAPACITY = 1024;

    /**
     * Name of this message processor (logging purposes)
     */
    private final String name;

    /**
     * Fairness of petitions is always true
     */
    private final static boolean MESSAGE_FAIRNESS = true;

    /**
     * Queue storing messages awaiting to be processed. The first in the queue is the first to be processed
     */
    private final ArrayBlockingQueue<E> messageQueue;

    /**
     * Thread in charge of processing incoming messages
     */
    private final MessageHandlerThread messageHandlerThread;

    /**
     * Thread in charge of reading new messages and adding them to the internal message queue (optional)
     */
    private final MessageReaderThread messageReaderThread;

    /**
     * Thread in charge of both reading and processing messages
     */
    private final MessageReaderHandlerThread messageReaderHandlerThread;

    /**
     * Whether reading and handling of messenger is done in separate threads (true), or in a single thread (false)
     */
    private final boolean separateThreads;

    /**
     * Traffic control object for pausing the message processing system
     */
    private final TrafficControl trafficControl;

    /**
     * Indicates if this message processor is alive. Once stopped, a message processor cannot come to live again
     */
    private final AtomicBoolean alive;


    //********************//
    // CONSTRUCTORS       //
    //********************//

    public MessageProcessor(MessageReader<E> messageReader) throws IllegalArgumentException {
        this(ThreadUtil.invokerName(1), messageReader, null, DEFAULT_QUEUE_CAPACITY, true);
    }

    public MessageProcessor(String name, MessageReader<E> messageReader) throws IllegalArgumentException {
        this(name, messageReader, null, DEFAULT_QUEUE_CAPACITY, true);
    }

    public MessageProcessor(String name, MessageReader<E> messageReader, int queueCapacity) throws IllegalArgumentException {
        this(name, messageReader, null, queueCapacity, true);
    }

    public MessageProcessor(MessageHandler<E> messageHandler) throws IllegalArgumentException {
        this(ThreadUtil.invokerName(1), null, messageHandler, DEFAULT_QUEUE_CAPACITY, true);
    }

    public MessageProcessor(String name, MessageHandler<E> messageHandler) throws IllegalArgumentException {
        this(name, null, messageHandler, DEFAULT_QUEUE_CAPACITY, true);
    }

    public MessageProcessor(String name, MessageHandler<E> messageHandler, int queueCapacity) throws IllegalArgumentException {
        this(name, null, messageHandler, queueCapacity, true);
    }

    public MessageProcessor(MessageReader<E> messageReader, MessageHandler<E> messageHandler, boolean separateThreads) throws IllegalArgumentException {
        this(ThreadUtil.invokerName(1), messageReader, messageHandler, DEFAULT_QUEUE_CAPACITY, separateThreads);
    }

    public MessageProcessor(String name, MessageReader<E> messageReader, MessageHandler<E> messageHandler, boolean separateThreads) throws IllegalArgumentException {
        this(name, messageReader, messageHandler, DEFAULT_QUEUE_CAPACITY, separateThreads);
    }

    public MessageProcessor(String name, MessageReader<E> messageReader, MessageHandler<E> messageHandler, int queueCapacity, boolean separateThreads) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        } else if (separateThreads && messageReader == null && messageHandler == null) {
            throw new IllegalArgumentException("Either MessageReader of MessageHandler objects must be received, both null");
        } else if (!separateThreads && (messageReader == null || messageHandler == null)) {
            throw new IllegalArgumentException("Both MessageReader and MessageHandler objects must be received if no separate threads are employed");
        }
        this.name = name;
        messageQueue = initializeMessageQueue(separateThreads, queueCapacity);
        messageReaderThread = initializeMessageReaderThread(messageReader, separateThreads, name);
        messageHandlerThread = initializeMessageHandlerThread(messageHandler, separateThreads, name);
        messageReaderHandlerThread = initializeMessageReaderHandlerThread(messageReader, messageHandler, separateThreads, name);
        this.separateThreads = separateThreads;
        trafficControl = new TrafficControl();
        alive = new AtomicBoolean(true);
        LOGGER.debug(logInit() + ") initialized");
    }

    private ArrayBlockingQueue<E> initializeMessageQueue(boolean separateThreads, int queueCapacity) {
        if (separateThreads) {
            return new ArrayBlockingQueue<>(queueCapacity, MESSAGE_FAIRNESS);
        } else {
            return null;
        }
    }

    private MessageReaderThread initializeMessageReaderThread(MessageReader messageReader, boolean separateThreads, String name) {
        if (messageReader != null && separateThreads) {
            return new MessageReaderThread(name, this, messageReader);
        } else {
            return null;
        }
    }

    private MessageHandlerThread initializeMessageHandlerThread(MessageHandler messageHandler, boolean separateThreads, String name) {
        if (messageHandler != null && separateThreads) {
            return new MessageHandlerThread(name, this, messageHandler);
        } else {
            return null;
        }
    }

    private MessageReaderHandlerThread initializeMessageReaderHandlerThread(MessageReader messageReader, MessageHandler messageHandler, boolean separateThreads, String name) {
        if (messageReader != null && messageHandler != null && !separateThreads) {
            return new MessageReaderHandlerThread(name, this, messageReader, messageHandler);
        } else {
            return null;
        }
    }

    public void start() {
        if (separateThreads) {
            startThread(messageReaderThread);
            startThread(messageHandlerThread);
        } else {
            startThread(messageReaderHandlerThread);
        }
        LOGGER.debug(logInit() + ") started");
    }

    private synchronized void startThread(Thread thread) {
        if (thread != null && !thread.isAlive()) {
            thread.start();
        }
    }

    public void pause() {
        if (alive.get()) {
            trafficControl.pause();
            LOGGER.debug(logInit() + ") paused");
        }
    }

    public void resume() {
        trafficControl.resume();
        LOGGER.debug(logInit() + ") resumed");
    }

    /**
     * @deprecated use pause
     */
    public void pauseReader() {
        pause();
    }

    /**
     * @deprecated use resume
     */
    public void resumeReader() {
        resume();
    }

    boolean accessTrafficControl() {
        trafficControl.access();
        return alive.get();
    }

    /**
     * @deprecated use pause
     */
    public void pauseHandler() {
        pause();
    }

    /**
     * @deprecated use resume
     */
    public void resumeHandler() {
        resume();
    }

    public void addMessage(E message) throws InterruptedException {
        if (messageQueue != null) {
            LOGGER.debug(logInit() + ") added message");
            messageQueue.put(message);
        } else {
            throw new IllegalStateException("Tried to add a message with no queue configured");
        }
    }

    public E takeMessage() throws InterruptedException {
        if (messageQueue != null) {
            LOGGER.debug(logInit() + ") removed message");
            return messageQueue.take();
        } else {
            throw new IllegalStateException("Tried to retrieve a message with no queue configured");
        }
    }

    public int queueSize() {
        if (messageQueue != null) {
            return messageQueue.size();
        } else {
            return 0;
        }
    }

    /**
     * Stops the processes associated to this message processor. In case an implementation of message reader is
     * being used, its stop method will be invoked. It is responsibility of the implementation to leave any blocking
     * process that it might be waiting on, and clean its resources.
     * <p>
     * If an implementation of message handler is being used, its close method will be invoked so it closes any
     * open resources
     */
    public synchronized void stop() {
        // we resume the processor, in case it was paused, so the threads can advance and finish
        if (alive.getAndSet(false)) {
            resume();
            if (messageReaderThread != null) {
                messageReaderThread.getMessageReader().stop();
            }
            if (messageHandlerThread != null) {
                messageHandlerThread.interrupt();
            }
            if (messageReaderHandlerThread != null) {
                messageReaderHandlerThread.getMessageReader().stop();
                messageReaderHandlerThread.interrupt();
            }
            LOGGER.debug(logInit() + ") stopped");
        }
    }

    /**
     * The MessageReaderThread or the MessageReaderHandlerThread reports that an StopReadingMessages object has been
     * read, and therefore it has stopped executing. This same thread will directly report our client that it has
     * been stopped. This method allows the MessageProcessor to stop additional threads (the handler thread) in case
     * it is needed
     */
    synchronized void readerHasStopped() {
        if (alive.getAndSet(false)) {
            resume();
            if (messageHandlerThread != null) {
                messageHandlerThread.interrupt();
            }
        }
    }

    @Override
    protected synchronized void finalize() throws Throwable {
        super.finalize();
        if (alive.getAndSet(false)) {
            stop();
        }
    }

    private String logInit() {
        return "Message processor (" + name;
    }
}
