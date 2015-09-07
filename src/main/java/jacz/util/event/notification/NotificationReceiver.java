package jacz.util.event.notification;

import jacz.util.identifier.UniqueIdentifier;

import java.util.List;

/**
 * Interface for receiving notifications in the notification API
 */
public interface NotificationReceiver {

    public void newEvent(UniqueIdentifier emitterID, int eventCount, List<List<Object>> messages);
}
