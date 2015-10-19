package jacz.util.stochastic;

import jacz.util.numeric.NumericUtil;

import java.util.ArrayList;

/**
 * A random buffer that is fed from the mouse coordinates. Time values can also be used for randomisation
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

    private final byte[] randomBytes;

    private final boolean useTime;

    private int pos;

    private final int totalLength;

    private ArrayList<Coordinate> lastCoordinateStack;

    private String gainedBits;

    public MouseToRandom(int length) {
        this(length, false);
    }

    public MouseToRandom(int length, boolean useTime) {
        randomBytes = new byte[length];
        this.useTime = useTime;
        pos = 0;
        totalLength = length;
        lastCoordinateStack = new ArrayList<>();
        gainedBits = "";
    }

    /**
     * Adds a new mouse coordinate for randomly generating bytes
     *
     * @param x random x coordinate
     * @param y random y coordinate
     * @return the percentage of completion
     */
    public int mouseCoords(int x, int y) {
        if (!lastCoordinateStack.isEmpty()) {
            newRandomCoords(x, y);
            // store the new coordinate in the coordinate stack
            lastCoordinateStack.add(0, new Coordinate(x, y));
            while (lastCoordinateStack.size() > COORDINATE_STACK_LENGTH) {
                lastCoordinateStack.remove(COORDINATE_STACK_LENGTH);
            }
            return progress();
        } else {
            lastCoordinateStack.add(new Coordinate(x, y));
            return 0;
        }
    }

    private int progress() {
        int progress = new Double((double) (100 * pos) / (double) totalLength).intValue();
        if (progress == 100 && pos < totalLength) {
            progress = 99;
        } else if (pos == totalLength) {
            progress = 100;
        }
        return progress;
    }

    private void newRandomCoords(int x, int y) {
        int moveX = x - lastCoordinateStack.get(0).x;
        int moveY = y - lastCoordinateStack.get(0).y;
        String bitString = calculateBitString(moveX, moveY);
        gainedBits += bitString;
        if (useTime) {
            int randomTime = getRandomTime();
            if (lastCoordinateStack.size() > randomTime) {
                // redo the bit gain with an older stored coordinate, obtained randomly with the nanoTime function
                moveX = x - lastCoordinateStack.get(randomTime).x;
                moveY = y - lastCoordinateStack.get(randomTime).y;
                bitString = calculateBitString(moveX, moveY);
                gainedBits += bitString;
            }
        }
        while (gainedBits.length() >= 8) {
            // move 8 bits to the byte array
            String eightBitString = gainedBits.substring(0, 8);
            addByteToBuffer((byte) Integer.parseInt(eightBitString, 2));
            gainedBits = gainedBits.substring(8);
        }
    }

    private void addByteToBuffer(byte b) {
        if (pos < totalLength) {
            randomBytes[pos++] = b;
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
        long millis = System.currentTimeMillis();
        return (int) (millis % COORDINATE_STACK_LENGTH);
    }

    public boolean hasFinished() {
        return pos == totalLength;
    }

    public byte[] getRandomBytes() {
        return randomBytes;
    }
}
