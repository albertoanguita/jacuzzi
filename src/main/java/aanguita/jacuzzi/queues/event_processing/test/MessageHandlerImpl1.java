package aanguita.jacuzzi.queues.event_processing.test;

import aanguita.jacuzzi.queues.event_processing.MessageHandler;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 08-abr-2010<br>
 * Last Modified: 08-abr-2010
 */
public class MessageHandlerImpl1 implements MessageHandler {
    @Override
    public void handleMessage(Object message) {
        System.out.println("Message received: " + message);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Message processed: " + message);
    }

    @Override
    public void finalizeHandler() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
