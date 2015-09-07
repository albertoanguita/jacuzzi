package jacz.util.stochastic;

import java.util.Arrays;

/**
 * A buffer that can be filled with random data. Every new random byte will be efficiently put in a new position
 */
public class RandomBuffer {

    private final byte[] randomBuffer;

    private int pos;

    private boolean increasing;

    private boolean isFilled;

    public RandomBuffer(int length) {
        randomBuffer = new byte[length];
        pos = 0;
        increasing = true;
        isFilled = false;
    }

    public void put(byte randomByte) {
        randomBuffer[pos] = randomByte;
        movePos();
    }

    private void movePos() {
        checkReverse();
        pos = (increasing) ? pos + 1 : pos - 1;
    }

    private void checkReverse() {
        if (increasing && pos == randomBuffer.length - 1) {
            increasing = false;
            isFilled = true;
        } else if (!increasing && pos == 0) {
            increasing = true;
        }
    }

    public byte[] getRandomBuffer() {
        return Arrays.copyOf(randomBuffer, randomBuffer.length);
    }

    public boolean isFilled() {
        return isFilled;
    }

    public int getCurrentLength() {
        return (isFilled) ? randomBuffer.length : pos;
    }
}
