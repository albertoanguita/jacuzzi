package aanguita.jacuzzi.queues.event_processing.test2;

import aanguita.jacuzzi.queues.event_processing.MessageProcessor;

/**
 *
 */
public class Test2 {

    public static void main(String args[]) {

        MessageProcessor messageProcessor = new MessageProcessor("test2", new MessageReaderImpl2(), new MessageHandlerImpl2(), true);
        messageProcessor.start();

        try {
            Thread.sleep(500);

            messageProcessor.pauseReader();
            //messageProcessor.pause();
            //messageProcessor.pause();

            Thread.sleep(5000);

            messageProcessor.resume();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
