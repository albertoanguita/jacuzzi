package org.aanguita.jacuzzi.queues.event_processing;

/**
 * This thread cannot be stopped by its corresponding MessageProcessor, because the readMessage method does not throw
 * an InterruptedException. It will therefore be responsibility of the user to stop it by means of other exceptions
 * catched in the readMessage method. By returning a StopReadingMessages object, the user will be able to indicate
 * this thread to stop reading messages.
 * <p/>
 * User: Alberto<br>
 * Date: 25-mar-2010<br>
 * Last Modified: 25-mar-2010
 */
class MessageReaderThread<E> extends Thread {

    private MessageProcessor<E> messageProcessor;

    private MessageReader<E> messageReader;

    MessageReaderThread(String name, MessageProcessor<E> messageProcessor, MessageReader<E> messageReader) {
        super(name + "/MessageReaderThread");
        this.messageProcessor = messageProcessor;
        this.messageReader = messageReader;
    }

    public void run() {
        boolean finished = false;
        while (!finished) {
            finished = readMessage(messageProcessor, messageReader);
        }
        // report both the MessageProcessor and the MessageReader implementation that this process
        // has been stopped externally
        reportStop(messageProcessor, messageReader);
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
            // user should not let any exceptions to reach this level -> error exposed in console
            // todo
            e.printStackTrace();
            return true;
        }
        return false;
    }

    static void reportStop(MessageProcessor messageProcessor, MessageReader messageReader) {
        messageProcessor.readerHasStopped();
        messageReader.stopped();
    }
}
