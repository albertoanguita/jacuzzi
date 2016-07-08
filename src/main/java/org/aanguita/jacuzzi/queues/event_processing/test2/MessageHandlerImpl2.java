package org.aanguita.jacuzzi.queues.event_processing.test2;

import org.aanguita.jacuzzi.queues.event_processing.MessageHandler;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 14-abr-2010
 * Time: 18:01:44
 * To change this template use File | Settings | File Templates.
 */
public class MessageHandlerImpl2 implements MessageHandler {

    public void handleMessage(Object message) {
        System.out.println("recibido: " + message.toString());
    }

    @Override
    public void finalizeHandler() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
