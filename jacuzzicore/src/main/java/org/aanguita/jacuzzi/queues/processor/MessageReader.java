package org.aanguita.jacuzzi.queues.processor;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 25-mar-2010<br>
 * Last Modified: 25-mar-2010
 */
public interface MessageReader<E> {

    /**
     * This method is invoked by the reader to obtain a new message object. The next message to process must be
     * returned. Throwing a {@link FinishReadingMessagesException} object is the way to tell the reader that it
     * must finish its execution
     *
     * @return the next message to process
     * @throws FinishReadingMessagesException if this reader has finished providing messages
     */
    E readMessage() throws FinishReadingMessagesException;

    /**
     * This command instructs the message reader to stop reading messages, and throw a
     * {@link FinishReadingMessagesException} in its next (or current) readMessage invocation
     * // TODO: 26/03/2017 ???? the implementation must do this??????
     */
    void stop();
}
