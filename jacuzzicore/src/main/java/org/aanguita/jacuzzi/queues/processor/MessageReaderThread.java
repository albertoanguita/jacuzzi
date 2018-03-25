package org.aanguita.jacuzzi.queues.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * This thread cannot be stopped by its corresponding MessageProcessor, because the readMessage method does not throw
 * an InterruptedException. It will therefore be responsibility of the user to stop it by means of other exceptions
 * catched in the readMessage method. By throwing a {@link FinishReadingMessagesException}, the user will be able to
 * indicate this thread to stop reading messages.
 * <p/>
 * User: Alberto<br>
 * Date: 25-mar-2010<br>
 * Last Modified: 25-mar-2010
 */
class MessageReaderThread<E> extends MessageProcessorAbstractThread {

    private static final Logger logger = LoggerFactory.getLogger(MessageReaderThread.class);

    private MessageProcessor<E> messageProcessor;

    private MessageReader<E> messageReader;

    MessageReaderThread(String name, MessageProcessor<E> messageProcessor, MessageReader<E> messageReader) {
        this(name, messageProcessor, messageReader, null);
    }

    MessageReaderThread(String name, MessageProcessor<E> messageProcessor, MessageReader<E> messageReader, Consumer<Exception> exceptionConsumer) {
        super(name + "/MessageReaderThread", exceptionConsumer);
        this.messageProcessor = messageProcessor;
        this.messageReader = messageReader;
    }

    MessageReader<E> getMessageReader() {
        return messageReader;
    }

    public void run() {
        boolean finished = false;
        while (!finished) {
            finished = readMessage(messageProcessor, messageReader);
        }
        // report both the MessageProcessor and the MessageReader implementation that this process
        // has been stopped externally
        messageProcessor.readerHasStopped();
    }

    private boolean readMessage(MessageProcessor<E> messageProcessor, MessageReader<E> messageReader) {
        // We check if we are paused both before and after reading a message. This order prevents trying to read a
        // message if we are paused, but also a situation where we go through the pause ok, then we wait for a
        // message, and subsequent pauses do not affect the first message read
        try {
            messageProcessor.accessTrafficControl();
            E message = messageReader.readMessage();
            messageProcessor.accessTrafficControl();
            messageProcessor.addMessage(message);
        } catch (FinishReadingMessagesException e) {
            // the message reader finished
            return true;
        } catch (Exception e) {
            //unexpected exception obtained. Print error and terminate
            if (logger.isErrorEnabled()) {
                logger.error("UNEXPECTED EXCEPTION THROWN BY MESSAGE READER IMPLEMENTATION. PLEASE CORRECT THE CODE SO NO EXCEPTIONS ARE THROWN AT THIS LEVEL", e);
            }
            consumeException(e);
            return true;
        }
        return false;
    }
}
