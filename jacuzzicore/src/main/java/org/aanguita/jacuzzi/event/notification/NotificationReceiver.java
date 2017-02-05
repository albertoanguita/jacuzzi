package org.aanguita.jacuzzi.event.notification;

import java.util.List;

/**
 * Interface for receiving notifications in the notification API
 */
public interface NotificationReceiver {

    void newEvent(String emitterID, int eventCount, List<List<Object>> nonGroupedMessages, List<Object> groupedMessages);
}
