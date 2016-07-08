package org.aanguita.jacuzzi.queues.event_processing;

/**
 *
 */
public interface MessageHandler {

    void handleMessage(Object message);

    /**
     * This method is invoked to indicate that the handling of message has finalized, in case the handler
     * implementation needs to close resources
     */
    void finalizeHandler();
}
