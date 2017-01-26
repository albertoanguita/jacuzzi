package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.Arrays;

/**
 * A publication on an event hub.
 */
public class Publication extends StringIdClass {

    private final String eventHubName;

    private final Channel channel;

    private final long timestamp;

    private final Object[] messages;

    Publication(String eventHubName, Channel channel, long timestamp, Object[] messages) {
        super();
        this.eventHubName = eventHubName;
        this.channel = channel;
        this.timestamp = timestamp;
        this.messages = messages;
    }

    public String getEventHubName() {
        return eventHubName;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Object[] getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "Publication{" +
                "eventHubName='" + eventHubName + '\'' +
                ", channel=" + channel +
                ", timestamp=" + timestamp +
                ", messages=" + Arrays.toString(messages) +
                '}';
    }
}
