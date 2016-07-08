package aanguita.jacuzzi.sets.availableelements;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Available elements...
 */
class AvailableElements<T> {

    private Set<T> occupiedElements;

    private ElementHandler<T> elementHandler;

    private T nextElement;

    public AvailableElements(T initialElement, ElementHandler<T> elementHandler, T... occupiedElements) {
        this.occupiedElements = new HashSet<>();
        nextElement = initialElement;
        this.elementHandler = elementHandler;
        this.occupiedElements.addAll(Arrays.asList(occupiedElements));
    }

    public synchronized T requestElement() {
        // no elements available at the moment
        if (occupiedElements.size() == elementHandler.maxSize()) {
            return null;
        }
        // search for an available element
        while (occupiedElements.contains(nextElement)) {
            nextElement = elementHandler.next(nextElement);
        }
        // we want next element to point to the next free element (so in case the returned element is freed right next, it is not the
        // given element if requested again). If we return the 26, then the next returned value will be 27, regardless of the 26 being
        // freed before the new request
        T returnedElement = nextElement;
        occupiedElements.add(returnedElement);
        nextElement = elementHandler.next(nextElement);

        return returnedElement;
    }

    public synchronized void freeElement(T element) {
        occupiedElements.remove(element);
    }

    public synchronized void freeElements(Collection<T> elements) {
        for (T element : elements) {
            freeElement(element);
        }
    }

    public static long size(long minValue, long maxValue, long maxElement) {
        if (maxElement >= 0) {
            return maxElement + 1;
        } else {
            return maxValue + 1 + (maxElement + minValue + 1);
        }
    }

}
