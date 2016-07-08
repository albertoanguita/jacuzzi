package org.aanguita.jacuzzi.queues.event_processing;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 25-mar-2010<br>
 * Last Modified: 25-mar-2010
 */
public interface MessageReader {

    /**
     * This method is invoked by the reader to obtain a new message object. The next message to process must be
     * returned. Returning a StopReadingMessages object is the way to tell the reader that it must finish its
     * execution
     *
     * @return the next message to process, or a StopReadingMessages if the execution of the reader must finish
     */
    Object readMessage();

    /**
     * This method is invoked when the message reader receives a StopReadingMessages message (either intentionally or
     * due to some error). Note: additionally to this, a StopReadingMessages message will be propagated upwards. If
     * the MessageProcessor has been given a handler implementation, the thread for handling messages will terminate
     * (if separated from the thread for reading messages). If there is no handler implementation, meaning that the
     * client handles the messages himself, he must be ready to handle this StopReadingMessages message
     * handling messages,
     */
    void stopped();
}
