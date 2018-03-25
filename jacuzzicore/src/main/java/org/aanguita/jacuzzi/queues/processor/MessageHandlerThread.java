package org.aanguita.jacuzzi.queues.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 08-mar-2010<br>
 * Last Modified: 08-mar-2010
 */
class MessageHandlerThread<E> extends MessageProcessorAbstractThread {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerThread.class);

    private MessageProcessor<E> messageProcessor;

    private MessageHandler<E> messageHandler;

    MessageHandlerThread(String name, MessageProcessor<E> messageProcessor, MessageHandler<E> messageHandler) {
        this(name, messageProcessor, messageHandler, null);
    }

    MessageHandlerThread(String name, MessageProcessor<E> messageProcessor, MessageHandler<E> messageHandler, Consumer<Exception> exceptionConsumer) {
        super(name + "/MessageHandlerThread", exceptionConsumer);
        this.messageProcessor = messageProcessor;
        this.messageHandler = messageHandler;
    }

    public void run() {
        boolean finished = false;
        while (!finished) {
            finished = handleMessage(messageProcessor, messageHandler);
        }
        messageHandler.close();
    }

    private boolean handleMessage(MessageProcessor<E> messageProcessor, MessageHandler<E> messageHandler) {
        try {
            E message = messageProcessor.takeMessage();
            messageProcessor.accessTrafficControl();
            messageHandler.handleMessage(message);
            if (logger.isDebugEnabled()) {
                logger.debug(messageProcessor.logInit("MessageHandler") + "handling message");
            }
            return false;
        } catch (InterruptedException e) {
            // only the MessageProcessor can interrupt this thread, cannot be an error
            return true;
        } catch (Exception e) {
            //unexpected exception obtained. Print error and terminate
            if (logger.isErrorEnabled()) {
                logger.error("UNEXPECTED EXCEPTION THROWN BY MESSAGE HANDLER IMPLEMENTATION. PLEASE CORRECT THE CODE SO NO EXCEPTIONS ARE THROWN AT THIS LEVEL", e);
            }
            consumeException(e);
            return true;
        }
    }
}
