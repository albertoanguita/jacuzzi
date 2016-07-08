package aanguita.jacuzzi.sets.availableelements;

/**
 *
 */
public class AvailableElementsInteger extends AvailableElements<Integer> {

    static class ElementHandlerInteger implements ElementHandler<Integer> {

        private int nextElement;

        private final int maxElement;

        private final long maxSize;

        ElementHandlerInteger(int nextElement, int maxElement) {
            this.nextElement = nextElement;
            this.maxElement = maxElement;
            if (maxElement >= 0) {
                maxSize = maxElement - nextElement + 1L;
            } else {
                maxSize = (long) Integer.MAX_VALUE - (long) Integer.MIN_VALUE + (long) maxElement + 2L;
            }
        }

        @Override
        public Integer next(Integer element) {
            Integer result = nextElement;
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

    public AvailableElementsInteger(Integer... occupiedElements) {
        this(-1, occupiedElements);
    }

    public AvailableElementsInteger(int maxElement, Integer[] occupiedElements) {
        super(0, new ElementHandlerInteger(0, maxElement), occupiedElements);
    }
}
