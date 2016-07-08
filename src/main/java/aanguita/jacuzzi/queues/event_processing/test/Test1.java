package aanguita.jacuzzi.queues.event_processing.test;

import aanguita.jacuzzi.io.IOUtil;
import aanguita.jacuzzi.queues.event_processing.MessageProcessor;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 08-abr-2010<br>
 * Last Modified: 08-abr-2010
 */
public class Test1 {

    public static void main(String args[]) {
        MessageProcessor messageProcessor = new MessageProcessor("test1", new MessageHandlerImpl1());

        /*try {
            messageProcessor.addMessage("uno");
            messageProcessor.addMessage("dos");
            messageProcessor.addMessage("tres");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        messageProcessor.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        messageProcessor.stop();
        IOUtil.pauseEnter("Fin\n");
    }
}
