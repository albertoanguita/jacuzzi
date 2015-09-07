package jacz.util.stochastic;

import jacz.util.numeric.NumericUtil;

import java.util.ArrayList;

/**
 * A random buffer that is fed from the mouse coordinates. Time values are also used for randomisation
 */
public class MouseToRandom {

    private class Coordinate {

        private Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int x;

        int y;
    }

    private static final int COORDINATE_STACK_LENGTH = 10;

    private RandomBuffer randomBytes;

    private int totalLength;

    private ArrayList<Coordinate> lastCoordinateStack;

    private boolean hasStarted;

    private String gainedBits;

    public MouseToRandom(int length) {
        randomBytes = new RandomBuffer(length);
        totalLength = length;
        lastCoordinateStack = new ArrayList<>(COORDINATE_STACK_LENGTH + 1);
        hasStarted = false;
        gainedBits = "";
    }

    public double mouseCoords(int x, int y) {
        if (hasStarted) {
            newRandomCoords(x, y);
            // store the new coordinate in the coordinate stack
            lastCoordinateStack.add(0, new Coordinate(x, y));
            if (lastCoordinateStack.size() > COORDINATE_STACK_LENGTH) {
                lastCoordinateStack.remove(COORDINATE_STACK_LENGTH);
            }
            return (double) randomBytes.getCurrentLength() / (double) totalLength;
        } else {
            lastCoordinateStack.add(new Coordinate(x, y));
            hasStarted = true;
            return 0d;
        }
    }

    private void newRandomCoords(int x, int y) {
        int moveX = x - lastCoordinateStack.get(0).x;
        int moveY = y - lastCoordinateStack.get(0).y;
        String bitString = calculateBitString(moveX, moveY);
        gainedBits += bitString;
        int randomTime = getRandomTime();
        if (lastCoordinateStack.size() > randomTime) {
            // redo the bit gain with an older stored coordinate, obtained randomly with the nanoTime function
            moveX = x - lastCoordinateStack.get(randomTime).x;
            moveY = y - lastCoordinateStack.get(randomTime).y;
            bitString = calculateBitString(moveX, moveY);
            gainedBits += bitString;
        }
        while (gainedBits.length() >= 8) {
            // move 8 bits to the byte array
            String eightBitString = gainedBits.substring(0, 8);
            randomBytes.put((byte) Integer.parseInt(eightBitString, 2));
            gainedBits = gainedBits.substring(8);
        }
    }

    private String calculateBitString(int x, int y) {
        int D = Math.max(Math.abs(x), Math.abs(y));
        int n;
        if (x > y && x >= -y) {
            n = x + y;
        } else if (x <= y && x > -y) {
            n = y - x + 2 * D;
        } else if (x < y && x <= -y) {
            n = x - y + 4 * D;
        } else {
            n = x + y + 8 * D;
        }
        return NumericUtil.decimalToBitString(n, 0, 8 * D - 1);
    }

    private int getRandomTime() {
        long nanoTime = System.nanoTime();
        return (int) ((nanoTime / 10L) % 10L);
    }

    public boolean hasFinished() {
        return randomBytes.isFilled();
    }

    public byte[] getRandomBytes() {
        return randomBytes.getRandomBuffer();
    }
}
