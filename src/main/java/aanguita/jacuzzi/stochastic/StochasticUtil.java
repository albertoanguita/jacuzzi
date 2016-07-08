package aanguita.jacuzzi.stochastic;

import java.util.Random;

/**
 * todo check this class... not very useful
 */
public class StochasticUtil {

    private static Random javaRandom = new Random();

    public static double randomFloat() {
        return javaRandom.nextFloat();
    }

    public static double random(float min, float max) {
        return (randomFloat() * (max - min)) + min;
    }

    public static double randomDouble() {
        return javaRandom.nextDouble();
    }

    public static double random(double min, double max) {
        return (randomDouble() * (max - min)) + min;
    }

    public static byte randomByte() {
        return (byte) randomInt();
    }

    public static byte random(byte min, byte max) {
        return (byte) random((long) min, (long) max);
    }

    public static short randomShort() {
        return (short) randomInt();
    }

    public static short random(short min, short max) {
        return (short) random((long) min, (long) max);
    }

    public static int randomInt() {
        return javaRandom.nextInt();
    }

    public static int random(int min, int max) {
        return (int) random((long) min, (long) max);
    }

    public static long randomLong() {
        return javaRandom.nextLong();
    }

    public static long random(long min, long max) {
        // we calculate a double random value between 0 and 1 and then translate it into the required range
        double randomDouble = randomDouble();
        randomDouble *= (double) (max - min + 1);
        long randomValue = (long) Math.floor(randomDouble);
        if (randomValue > max) {
            randomValue = max;
        }
        return randomValue;
    }

    public static <E extends Enum<E>> E random(Class<E> enumType) {
        int randomOrdinal = random(0, enumType.getEnumConstants().length);
        for (Enum<E> value : enumType.getEnumConstants()) {
            if (value.ordinal() == randomOrdinal) {
                //noinspection unchecked
                return (E) value;
            }
        }
        return null;
    }
}
