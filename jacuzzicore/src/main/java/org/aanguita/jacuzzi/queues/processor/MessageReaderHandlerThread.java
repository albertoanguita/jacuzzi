package org.aanguita.jacuzzi.queues.processor;

/**
 * todo change to Executor
 */
class MessageReaderHandlerThread<E> extends Thread {

    private MessageProcessor<E> messageProcessor;

    private MessageReader<E> messageReader;

    private MessageHandler<E> messageHandler;

    MessageReaderHandlerThread(String name, MessageProcessor<E> messageProcessor, MessageReader<E> messageReader, MessageHandler<E> messageHandler) {
        super(name + "/MessageReaderHandlerThread");
        this.messageProcessor = messageProcessor;
        this.messageReader = messageReader;
        this.messageHandler = messageHandler;
    }

    MessageReader<E> getMessageReader() {
        return messageReader;
    }

    public void run() {
        // We check if we are paused both before and after reading a message. This order prevents trying to read a
        // message if we are paused, but also a situation where we go through the pause ok, then we wait for a
        // message, and subsequent pauses do not affect the first message read
        boolean finished = false;
        while (!finished) {
            finished = readAndHandleMessage();
        }
        // report the handler that this process has been stopped
        messageHandler.close();
    }

    private boolean readAndHandleMessage() {
        try {
            messageProcessor.accessTrafficControl();
            E message = messageReader.readMessage();
            messageProcessor.accessTrafficControl();
            messageHandler.handleMessage(message);
            return false;
        } catch (FinishReadingMessagesException e) {
            // the message reader has finished
            return true;
        } catch (Exception e) {
            // user should not let any exceptions to reach this level -> error exposed in console
            // todo, handle better
            e.printStackTrace();
            return true;
        }
    }
}