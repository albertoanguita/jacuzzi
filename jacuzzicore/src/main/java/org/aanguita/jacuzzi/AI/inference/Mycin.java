package org.aanguita.jacuzzi.AI.inference;

/**
 * This class implements methods for performing data inference following the Mycin algorithm.
 * <p/>
 * Algorithms extracted from http://www.idi.ntnu.no/~ksys/NOTES/CF-model.html
 */
public class Mycin {

    public static float combine(float firstValue, float... restOfValues) throws IllegalArgumentException {
        checkCorrectCertaintyFactor(firstValue);
        for (double oneValue : restOfValues) {
            checkCorrectCertaintyFactor(oneValue);
            firstValue = (float) combineTwoFactors(firstValue, oneValue);
        }
        return firstValue;
    }

    public static double combine(double firstValue, double... restOfValues) throws IllegalArgumentException {
        checkCorrectCertaintyFactor(firstValue);
        for (double oneValue : restOfValues) {
            checkCorrectCertaintyFactor(oneValue);
            firstValue = combineTwoFactors(firstValue, oneValue);
        }
        return firstValue;
    }

    private static double combineTwoFactors(double value_1, double value_2) {
        if (value_1 > 0.0d && value_2 > 0.0d) {
            return value_1 + value_2 - value_1 * value_2;
        } else if (value_1 < 0.0d && value_2 < 0.0d) {
            return value_1 + value_2 + value_1 * value_2;
        } else {
            return (value_1 + value_2) / (1.0d - Math.min(Math.abs(value_1), Math.abs(value_2)));
        }
    }

    private static void checkCorrectCertaintyFactor(double value) throws IllegalArgumentException {
        if (value < -1.0d || value > 1.0d) {
            throw new IllegalArgumentException("Credibility factors must be between -1 and 1, found " + value);
        }
    }

    public static double weight(double value, double weight) {
        checkCorrectCertaintyFactor(value);
        if (weight < 0d || weight > 1.0d) {
            throw new IllegalArgumentException("Weight must be between 0 and 1, found " + weight);
        }
        return value * weight;
    }
}
