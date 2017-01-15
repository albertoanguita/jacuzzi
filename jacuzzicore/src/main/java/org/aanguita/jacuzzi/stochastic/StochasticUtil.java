package org.aanguita.jacuzzi.stochastic;

import java.util.concurrent.ThreadLocalRandom;

/**
 * todo check this class... not very useful
 */
public class StochasticUtil {

    public static <E extends Enum<E>> E random(Class<E> enumType) {
        int randomOrdinal = ThreadLocalRandom.current().nextInt(enumType.getEnumConstants().length);
        for (Enum<E> value : enumType.getEnumConstants()) {
            if (value.ordinal() == randomOrdinal) {
                //noinspection unchecked
                return (E) value;
            }
        }
        return null;
    }
}
