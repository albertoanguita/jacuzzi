package org.aanguita.jacuzzi.queues.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 08-mar-2010<br>
 * Last Modified: 08-mar-2010
 */
class MessageHandlerThread<E> extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerThread.class);

    private MessageProcessor<E> messageProcessor;

    private MessageHandler<E> messageHandler;

    MessageHandlerThread(String name, MessageProcessor<E> messageProcessor, MessageHandler<E> messageHandler) {
        super(name + "/MessageHandlerThread");
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
        } catch (Throwable e) {
            // user should not let any exceptions to reach this level -> error exposed in console
            // todo how to handle this
            e.printStackTrace();
            return true;
        }
    }
}
