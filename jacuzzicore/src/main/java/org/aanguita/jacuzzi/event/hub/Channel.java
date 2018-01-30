package org.aanguita.jacuzzi.event.hub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Alberto on 16/11/2016.
 */
public class Channel {

    private static final String SEPARATOR = "/";

    private static final String ONE_LEVEL_WILDCARD = "?";

    private static final String MULTILEVEL_WILDCARD = "*";

    private final String original;

    private final List<String> levels;

    Channel(String channel) {
        original = channel;
        levels = new ArrayList<>(Arrays.asList(channel.split(SEPARATOR)));
    }

    public String getOriginal() {
        return original;
    }

    public List<String> getLevels() {
        return levels;
    }

    boolean matches(Channel channel) {
        int index = 0;
        int thisSize = levels.size();
        int otherSize = channel.levels.size();
        while (index < thisSize && index < otherSize) {
            if (levels.get(index).equals(Channel.MULTILEVEL_WILDCARD)) {
                return true;
            } else if (levels.get(index).equals(Channel.ONE_LEVEL_WILDCARD) || levels.get(index).equals(channel.levels.get(index))) {
                index++;
            } else {
                return false;
            }
        }
        return thisSize == otherSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Channel channel = (Channel) o;

        return original.equals(channel.original);
    }

    @Override
    public int hashCode() {
        return original.hashCode();
    }

    @Override
    public String toString() {
        return "Channel{" + original + '}';
    }
}
