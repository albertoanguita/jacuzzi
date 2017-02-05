package org.aanguita.jacuzzi.stochastic;

/**
 * A probabilistic density function. The method get must return values for arguments between 0 and 1 (inclusive).
 * <p/>
 * The returned values are equal or greater than 0.
 * <p/>
 * It is not mandatory that the integral of the functions is 1.
 */
public interface DensityFunction {

    double get(double value) throws IllegalArgumentException;
}
