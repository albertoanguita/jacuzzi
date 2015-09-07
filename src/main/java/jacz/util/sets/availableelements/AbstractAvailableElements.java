package jacz.util.sets.availableelements;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 07-may-2010
 * Time: 12:56:25
 * To change this template use File | Settings | File Templates.
 */
abstract class AbstractAvailableElements<T> implements ElementHandler<T> {

    protected T maxElement;

    protected long size;

    protected AvailableElements<T> availableElements;

    public AbstractAvailableElements(T init, T maxElement, T... occupiedElements) {
        init(init, maxElement, occupiedElements);
    }

    private void init(T init, T maxElement, T... occupiedElements) {
        this.maxElement = maxElement;
        init = calculateInit(init, maxElement);
        size = initSize();
        availableElements = new AvailableElements<T>(init, this, occupiedElements);
    }

    protected abstract T calculateInit(T init, T maxElement);

    protected abstract long initSize();

    public T requestElement() {
        return availableElements.requestElement();
    }

    public void freeElement(T element) {
        availableElements.freeElement(element);
    }

    public synchronized void freeElements(Collection<T> elements) {
        availableElements.freeElements(elements);
    }

    @Override
    public long maxSize() {
        return size;
    }

}
