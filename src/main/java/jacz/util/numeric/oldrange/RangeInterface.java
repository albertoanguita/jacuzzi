package jacz.util.numeric.oldrange;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 16-may-2010<br>
 * Last Modified: 16-may-2010
 */
public interface RangeInterface<T, Y extends Comparable<Y>> {

    Long size();

    T buildInstance(Y min, Y max);

    public boolean isEmpty();

    public Y getMin();

    public Y getMax();

    Y getZero();

    Y previous(Y value);

    Y next(Y value);

    Y add(Y value1, Y value2);

    Y substract(Y value1, Y value2);
}
