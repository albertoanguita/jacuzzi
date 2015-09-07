package jacz.util.queues.event_processing.test2;

import jacz.util.queues.event_processing.MessageReader;
import jacz.util.queues.event_processing.StopReadingMessages;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 14-abr-2010
 * Time: 18:00:14
 * To change this template use File | Settings | File Templates.
 */
public class MessageReaderImpl2 implements MessageReader {

    private int count;

    public MessageReaderImpl2() {
        count = 0;
    }

    public Object readMessage() {
        if (count < 3) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            return "hola " + count;
        }
        return new StopReadingMessages();
    }

    public void stopped() {
        System.out.println("stop");
    }
}
