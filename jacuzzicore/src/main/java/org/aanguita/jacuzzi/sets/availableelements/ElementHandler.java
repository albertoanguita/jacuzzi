package org.aanguita.jacuzzi.sets.availableelements;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 07-may-2010<br>
 * Last Modified: 07-may-2010
 */
interface ElementHandler<T> {

    T next(T element);

    long maxSize();
}
