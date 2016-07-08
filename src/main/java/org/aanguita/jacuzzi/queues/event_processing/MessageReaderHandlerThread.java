package org.aanguita.jacuzzi.queues.event_processing;

/**
 * todo change to Executor
 */
class MessageReaderHandlerThread extends Thread {

    private MessageProcessor messageProcessor;

    private MessageReader messageReader;

    private MessageHandler messageHandler;

    public MessageReaderHandlerThread(String name, MessageProcessor messageProcessor, MessageReader messageReader, MessageHandler messageHandler) {
        super(name + "/MessageReaderHandlerThread");
        this.messageProcessor = messageProcessor;
        this.messageReader = messageReader;
        this.messageHandler = messageHandler;
    }

    public void run() {
        // We check if we are paused both before and after reading a message. This order prevents trying to read a
        // message if we are paused, but also a situation where we go through the pause ok, then we wait for a
        // message, and subsequent pauses do not affect the first message read
        boolean finished = false;
        while (!finished) {
            Object message = null;
            try {
                messageProcessor.accessReaderPausableElement();
                message = messageReader.readMessage();
                messageProcessor.accessReaderPausableElement();
            } catch (Exception e) {
                // user should not let any exceptions to reach this level -> error exposed in console
                // todo, handle better
                e.printStackTrace();
                finished = true;
            }
            messageProcessor.accessHandlerPausableElement();
            if (!finished) {
                finished = MessageHandlerThread.handleMessageAux(messageHandler, message);
            }
        }
        // report both the MessageReader implementation that this process has been stopped
        messageProcessor.readerHandlerStopped();
        messageReader.stopped();
        messageHandler.finalizeHandler();
    }
}
