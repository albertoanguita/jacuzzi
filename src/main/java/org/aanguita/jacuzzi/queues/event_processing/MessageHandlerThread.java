package org.aanguita.jacuzzi.queues.event_processing;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 08-mar-2010<br>
 * Last Modified: 08-mar-2010
 */
class MessageHandlerThread extends Thread {


    private MessageProcessor messageProcessor;

    private MessageHandler messageHandler;

    MessageHandlerThread(String name, MessageProcessor messageProcessor, MessageHandler messageHandler) {
        super(name + "/MessageHandlerThread");
        this.messageProcessor = messageProcessor;
        this.messageHandler = messageHandler;
    }

    /*void stopThread() {
        // the thread must stop processing messages, but only after no more messages are left to process (it is
        // assumed that no more messages will be inserted in the message queue after it is isEmpty)
        boolean finished = false;
        while (!finished) {
            try {
                messageProcessor.addMessage(new StopReadingMessages());
            } catch (InterruptedException e) {
                continue;
            }
            finished = true;
        }
    }*/

    public void run() {
        boolean finished = false;
        while (!finished) {
            finished = handleMessage(messageProcessor, messageHandler);
        }
        messageHandler.finalizeHandler();
    }

    static boolean handleMessage(MessageProcessor messageProcessor, MessageHandler messageHandler) {
        try {
            Object message = messageProcessor.takeMessage();
            messageProcessor.accessHandlerPausableElement();
            return handleMessageAux(messageHandler, message);
        } catch (InterruptedException e) {
            // only the MessageProcessor can interrupt this thread, cannot be an error
            return true;
        } catch (Exception e) {
            // user should not let any exceptions to reach this level -> error exposed in console
            e.printStackTrace();
            return true;
        }
    }

    static boolean handleMessageAux(MessageHandler messageHandler, Object message) {
        if (message instanceof StopReadingMessages) {
            return true;
        } else {
            messageHandler.handleMessage(message);
            return false;
        }
    }

}
