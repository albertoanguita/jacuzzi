package jacz.util.event.notification.test;

import jacz.util.event.notification.NotificationReceiver;
import jacz.util.id.AlphaNumFactory;

import java.util.List;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 28-abr-2010<br>
 * Last Modified: 28-abr-2010
 */
public class Receiver implements NotificationReceiver {

    private Emitter emitter;


    public Receiver() {
        emitter = new Emitter();
        emitter.subscribe(AlphaNumFactory.getStaticId(), this, 2000, 0.5d, 10);
    }

    @Override
    public void newEvent(String emitterID, int eventCount, List<List<Object>> nonGroupedMessages, List<Object> groupedMessages) {
        System.out.println("Receiver: ints added" + " after " + eventCount);
        System.out.println("messages");
        System.out.println("--------");
        for (List<Object> oneList : nonGroupedMessages) {
            System.out.println(oneList);
        }
        System.out.println("------------------------");
    }


    public static void main(String args[]) {

        Receiver receiver = new Receiver();

        receiver.doThings();
    }

    private void doThings() {
        try {
            emitter.add();
            emitter.add();
            emitter.add();
            emitter.add();
            Thread.sleep(800);
            emitter.add();

        } catch (Exception e) {

        }
    }
}
