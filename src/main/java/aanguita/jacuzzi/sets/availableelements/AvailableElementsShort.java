package aanguita.jacuzzi.sets.availableelements;

/**
 *
 */
public class AvailableElementsShort extends AvailableElements<Short> {

    static class ElementHandlerShort implements ElementHandler<Short> {

        private short nextElement;

        private final short maxElement;

        private final long maxSize;

        ElementHandlerShort(short nextElement, short maxElement) {
            this.nextElement = nextElement;
            this.maxElement = maxElement;
            if (maxElement >= 0) {
                maxSize = maxElement - nextElement + 1;
            } else {
                maxSize = Short.MAX_VALUE - Short.MIN_VALUE + maxElement + 2;
            }
        }

        @Override
        public Short next(Short element) {
            Short result = nextElement;
            if (nextElement != maxElement) {
                nextElement++;
            } else {
                nextElement = 0;
            }
            return result;
        }

        @Override
        public long maxSize() {
            return maxSize;
        }
    }

    public AvailableElementsShort(Short... occupiedElements) {
        this((short) -1, occupiedElements);
    }

    public AvailableElementsShort(short maxElement, Short[] occupiedElements) {
        super((short) 0, new ElementHandlerShort((short) 0, maxElement), occupiedElements);
    }
}
