package aanguita.jacuzzi.sets.availableelements;

/**
 *
 */
public class AvailableElementsByte extends AvailableElements<Byte> {

    static class ElementHandlerByte implements ElementHandler<Byte> {

        private byte nextElement;

        private final byte maxElement;

        private final long maxSize;

        ElementHandlerByte(byte nextElement, byte maxElement) {
            this.nextElement = nextElement;
            this.maxElement = maxElement;
            if (maxElement >= 0) {
                maxSize = maxElement - nextElement + 1;
            } else {
                maxSize = Byte.MAX_VALUE - Byte.MIN_VALUE + maxElement + 2;
            }
        }

        @Override
        public Byte next(Byte element) {
            Byte result = nextElement;
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

    public AvailableElementsByte(Byte... occupiedElements) {
        this((byte) -1, occupiedElements);
    }

    public AvailableElementsByte(byte maxElement, Byte[] occupiedElements) {
        super((byte) 0, new ElementHandlerByte((byte) 0, maxElement), occupiedElements);
    }
}
