package org.aanguita.jacuzzi.queues.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * todo change to Executor
 */
class MessageReaderHandlerThread<E> extends MessageProcessorAbstractThread {

    private static final Logger logger = LoggerFactory.getLogger(MessageReaderHandlerThread.class);

    private MessageProcessor<E> messageProcessor;

    private MessageReader<E> messageReader;

    private MessageHandler<E> messageHandler;

    MessageReaderHandlerThread(String name, MessageProcessor<E> messageProcessor, MessageReader<E> messageReader, MessageHandler<E> messageHandler) {
        this(name, messageProcessor, messageReader, messageHandler, null);
    }

    MessageReaderHandlerThread(String name, MessageProcessor<E> messageProcessor, MessageReader<E> messageReader, MessageHandler<E> messageHandler, Consumer<Exception> exceptionConsumer) {
        super(name + "/MessageReaderHandlerThread", exceptionConsumer);
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
            if (logger.isDebugEnabled()) {
                logger.debug(messageProcessor.logInit("MessageReaderHandler") + "handling message");
            }
            messageHandler.handleMessage(message);
            return false;
        } catch (FinishReadingMessagesException e) {
            // the message reader has finished
            return true;
        } catch (Exception e) {
            //unexpected exception obtained. Print error and terminate
            if (logger.isErrorEnabled()) {
                logger.error("UNEXPECTED EXCEPTION THROWN BY MESSAGE READER HANDLER IMPLEMENTATION. PLEASE CORRECT THE CODE SO NO EXCEPTIONS ARE THROWN AT THIS LEVEL", e);
            }
            consumeException(e);
            return true;
        }
    }
}
