package org.aanguita.jacuzzi.queues.event_processing;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.execution_control.TrafficControl;
import org.aanguita.jacuzzi.id.AlphaNumFactory;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * todo invoke message handler finalize
 */
public class MessageProcessor {

    /**
     * Default capacity for the event queue
     */
    private final static int DEFAULT_QUEUE_CAPACITY = 1024;

    private final String id;

    /**
     * Fairness of petitions is always true
     */
    private final static boolean MESSAGE_FAIRNESS = true;

    /**
     * Queue storing messages awaiting to be processed. The first in the queue is the first to be processed
     */
    private ArrayBlockingQueue<Object> messageQueue;

    /**
     * Thread in charge of processing incoming messages
     */
    private MessageHandlerThread messageHandlerThread = null;

    /**
     * Thread in charge of reading new messages and adding them to the internal message queue (optional)
     */
    private MessageReaderThread messageReaderThread = null;

    /**
     * Thread in charge of both reading and processing messages
     */
    private MessageReaderHandlerThread messageReaderHandlerThread = null;

    /**
     * Whether reading and handling of messenger is done in separate threads (true), or in a single thread (false)
     */
    private boolean separateThreads;

    private TrafficControl readerTrafficControl;

    private TrafficControl handlerTrafficControl;

    public MessageProcessor(MessageReader messageReader) throws IllegalArgumentException {
        this(ThreadUtil.invokerName(1), messageReader, null, DEFAULT_QUEUE_CAPACITY, true);
    }

    public MessageProcessor(String name, MessageReader messageReader) throws IllegalArgumentException {
        this(name, messageReader, null, DEFAULT_QUEUE_CAPACITY, true);
    }

    public MessageProcessor(String name, MessageReader messageReader, int queueCapacity) throws IllegalArgumentException {
        this(name, messageReader, null, queueCapacity, true);
    }

    public MessageProcessor(MessageHandler messageHandler) throws IllegalArgumentException {
        this(ThreadUtil.invokerName(1), null, messageHandler, DEFAULT_QUEUE_CAPACITY, true);
    }

    public MessageProcessor(String name, MessageHandler messageHandler) throws IllegalArgumentException {
        this(name, null, messageHandler, DEFAULT_QUEUE_CAPACITY, true);
    }

    public MessageProcessor(String name, MessageHandler messageHandler, int queueCapacity) throws IllegalArgumentException {
        this(name, null, messageHandler, queueCapacity, true);
    }

    public MessageProcessor(MessageReader messageReader, MessageHandler messageHandler, boolean separateThreads) throws IllegalArgumentException {
        this(ThreadUtil.invokerName(1), messageReader, messageHandler, DEFAULT_QUEUE_CAPACITY, separateThreads);
    }

    public MessageProcessor(String name, MessageReader messageReader, MessageHandler messageHandler, boolean separateThreads) throws IllegalArgumentException {
        this(name, messageReader, messageHandler, DEFAULT_QUEUE_CAPACITY, separateThreads);
    }

    public MessageProcessor(String name, MessageReader messageReader, MessageHandler messageHandler, int queueCapacity, boolean separateThreads) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        } else if (separateThreads && messageReader == null && messageHandler == null) {
            throw new IllegalArgumentException("Either MessageReader of MessageHandler objects must be received, both null");
        } else if (!separateThreads && (messageReader == null || messageHandler == null)) {
            throw new IllegalArgumentException("Both MessageReader and MessageHandler objects must be received if no separate threads are employed");
        }
        id = AlphaNumFactory.getStaticId();
        if (separateThreads) {
            messageQueue = new ArrayBlockingQueue<>(queueCapacity, MESSAGE_FAIRNESS);
        }
        if (messageReader != null && separateThreads) {
            messageReaderThread = new MessageReaderThread(name, this, messageReader);
        }
        if (messageHandler != null && separateThreads) {
            messageHandlerThread = new MessageHandlerThread(name, this, messageHandler);
        }
        if (messageReader != null && messageHandler != null && !separateThreads) {
            messageReaderHandlerThread = new MessageReaderHandlerThread(name, this, messageReader, messageHandler);
        }
        this.separateThreads = separateThreads;
        readerTrafficControl = new TrafficControl();
        handlerTrafficControl = new TrafficControl();
    }

    public void start() {
        if (separateThreads) {
            startReading();
            startThread(messageHandlerThread);
        } else {
            startThread(messageReaderHandlerThread);
        }
    }

    public synchronized void startReading() {
        if (separateThreads) {
            startThread(messageReaderThread);
        } else {
            startThread(messageReaderHandlerThread);
        }
    }

    private synchronized void startThread(Thread thread) {
        if (thread != null && !thread.isAlive()) {
            thread.start();
        }
    }

    public void pause() {
        pauseReader();
        pauseHandler();
    }

    public void resume() {
        resumeReader();
        resumeHandler();
    }

    public void pauseReader() {
        readerTrafficControl.pause();
    }

    public void resumeReader() {
        readerTrafficControl.resume();
    }

    void accessReaderPausableElement() {
        readerTrafficControl.access();
    }

    public void pauseHandler() {
        handlerTrafficControl.pause();
    }

    public void resumeHandler() {
        handlerTrafficControl.resume();
    }

    void accessHandlerPausableElement() {
        handlerTrafficControl.access();
    }

    public void addMessage(Object message) throws InterruptedException {
        messageQueue.put(message);
    }

    public Object takeMessage() throws InterruptedException {
        return messageQueue.take();
    }

    public int queueSize() {
        return messageQueue.size();
    }

    /**
     * Only useful when no MessageReader is used, just a MessageHandler. If a MessageReader is being used and
     * needs to be stopped, it must be fed with a StopReadingMessages object. The same happens if we use only
     * one thread both both tasks.
     * <p/>
     * If both reader and handler are used in different threads, the handler will be stopped, but the reader will
     * still be working and adding messages to an internal queue. This reader must be stopped with a
     * StopReadingMessages message
     */
    public synchronized void stop() {
        // we resume the processor, in case it was paused, so the threads can advance and finish
        resume();
        if (messageReaderThread != null || messageReaderHandlerThread != null) {
            // this method is not the appropriate for stopping the processor, but issuing a stopReadingMessages to the reader implementation
            throw new IllegalStateException("stop method must not be invoked as there is a messageReader implementation active");
        } else {
            // put a stopReadingMessages so the handler thread stops
            stopProcessor();
        }
//        if (separateThreads) {
//            stopProcessor();
//        }


        // the reader cannot be stopped from here, the MessageReader implementation must stop it itself
        /* else {
            if (messageReaderHandlerThread != null) {
                MessageReaderHandlerThread moribundReaderHandler = messageReaderHandlerThread;
                messageReaderHandlerThread = null;
                moribundReaderHandler.stopThread();
            }
        }*/
    }

    /**
     * The MessageReaderThread or the MessageReaderHandlerThread reports that an StopReadingMessages object has been
     * read, and therefore it has stopped executing. This same thread will directly report our client that it has
     * been stopped. This method allows the MessageProcessor to stop additional threads (the handler thread) in case
     * it is needed
     */
    synchronized void readerHasStopped() {
        // the message reader thread has stopped, now we stop the processor (if any)
        stopProcessor();
        resume();
    }

    synchronized void readerHandlerStopped() {
        resume();
    }

    private synchronized void stopProcessor() {
        // we simply insert a StopReadingMessages message in the queue. When it reaches the handler it will stop.
        try {
            addMessage(new StopReadingMessages());
        } catch (InterruptedException e) {
            // the message was not put, try again
            stopProcessor();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageProcessor that = (MessageProcessor) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    protected synchronized void finalize() throws Throwable {
        super.finalize();
        if (messageHandlerThread != null && messageHandlerThread.isAlive()) {
            stopProcessor();
        }
    }
}
