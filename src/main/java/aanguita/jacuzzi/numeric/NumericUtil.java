package aanguita.jacuzzi.numeric;

import aanguita.jacuzzi.lists.tuple.Duple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Util numeric methods
 */
public class NumericUtil {

    public enum AmbiguityBehavior {
        MIN,
        MID,
        MAX
    }

    public static List<Integer> divide(Integer i, int numSegments) {
        List<Long> longSegments = divide((long) i, numSegments);
        List<Integer> segments = new ArrayList<>(longSegments.size());
        for (Long l : longSegments) {
            segments.add(l.intValue());
        }
        return segments;
    }

    public static List<Integer> divideEven(Integer i, int numSegments) {
        List<Long> longSegments = divideEven((long) i, numSegments);
        List<Integer> segments = new ArrayList<>(longSegments.size());
        for (Long l : longSegments) {
            segments.add(l.intValue());
        }
        return segments;
    }

    public static List<Long> divide(Long l, int numSegments) {
        if (numSegments < 1) {
            throw new IllegalArgumentException("Number of segments must be greater thant 0");
        }
        int sign = 1;
        if (l.compareTo(0L) < 0) {
            l = -l;
            sign = -1;
        }
        // maxSize of the small segments
        long segmentSizes = l / numSegments;
        // number of segments with greater maxSize
        long numLargerSegments = l % numSegments;

        List<Long> segments = new ArrayList<>(numSegments);
        for (int i = 0; i < numLargerSegments; i++) {
            segments.add(sign * (segmentSizes + 1));
        }
        for (int i = 0; i < numSegments - numLargerSegments; i++) {
            segments.add(sign * segmentSizes);
        }
        return segments;
    }

    public static List<Long> divideEven(Long l, int numSegments) {
        if (numSegments < 1) {
            throw new IllegalArgumentException("Number of segments must be greater thant 0");
        }
        // maxSize of the small segments
        long segmentSizes = l / numSegments;
        // number of segments with greater maxSize
        long numLargerSegments = l % numSegments;
        List<Long> segments = new ArrayList<>(numSegments);

        // in this case we do not place all big segments first and then the small segments, we mix them. To do so,
        // we calculate the positions for one of the types (the one appearing less)
        boolean placeLargerSegments = numLargerSegments <= numSegments / 2;
        ArrayList<Long> positions;
        if (placeLargerSegments) {
            // place the larger segments
            positions = distributeItemsInPositions(numLargerSegments, 0, numSegments - 1, false);
        } else {
            // place the smaller segments
            positions = distributeItemsInPositions(numSegments - numLargerSegments, 0, numSegments - 1, false);
        }
        for (int i = 0; i < numSegments; i++) {
            if (!positions.isEmpty() && i == positions.get(0)) {
                positions.remove(0);
                if (placeLargerSegments) {
                    segments.add(segmentSizes + 1);
                } else {
                    segments.add(segmentSizes);
                }
            } else {
                if (placeLargerSegments) {
                    segments.add(segmentSizes);
                } else {
                    segments.add(segmentSizes + 1);
                }
            }
        }
        return segments;
    }

    /**
     * Distributes a series of items in a range of positions, returning the calculated positions for the items. The
     * items are placed as evenly as possible in the given range of positions.
     *
     * @param itemCount the number of items to place (must be equal or greater than zero)
     * @param min       the min of the position range
     * @param max       the max of the position range
     * @param useEdges  whether the edges of the range should be used for placing items (true) or avoided if possible
     *                  (false)
     * @return a list containing the positions calculated for the items
     */
    public static ArrayList<Integer> distributeItemsInPositions(int itemCount, int min, int max, boolean useEdges) {
        // the positions of the items are obtained by a simple double division. The division gives as the coordinates
        // where the items should be placed. The final positions are obtained by rounding those values to the closes
        // integers
        if (itemCount < 0 || min > max) {
            throw new IllegalArgumentException("Wrong arguments: " + itemCount + ", " + min + ", " + max + ", " + useEdges);
        }
        ArrayList<Long> positionsLong = distributeItemsInPositions((long) itemCount, (long) min, (long) max, useEdges);
        ArrayList<Integer> positions = new ArrayList<>(itemCount);
        for (Long aPositionsLong : positionsLong) {
            positions.add(aPositionsLong.intValue());
        }
        return positions;
    }

    /**
     * Distributes a series of items in a range of positions, returning the calculated positions for the items. The
     * items are placed as evenly as possible in the given range of positions.
     *
     * @param itemCount the number of items to place (must be equal or greater than zero)
     * @param min       the min of the position range
     * @param max       the max of the position range
     * @param useEdges  whether the edges of the range should be used for placing items (true) or avoided if possible
     *                  (false)
     * @return a list containing the positions calculated for the items
     */
    public static ArrayList<Long> distributeItemsInPositions(long itemCount, long min, long max, boolean useEdges) {
        // the positions of the items are obtained by a simple double division. The division gives as the coordinates
        // where the items should be placed. The final positions are obtained by rounding those values to the closes
        // integers
        if (itemCount < 0 || min > max) {
            throw new IllegalArgumentException("Wrong arguments: " + itemCount + ", " + min + ", " + max + ", " + useEdges);
        }
        if (itemCount == 0) {
            return new ArrayList<>(0);
        } else if (itemCount == 1) {
            ArrayList<Long> positions = new ArrayList<>(1);
            positions.add((min + max) / 2);
            return positions;
        } else {
            // at least two items
            ArrayList<Long> positions = new ArrayList<>((int) itemCount);
            double rangeSize = (double) (max - min);
            double cellSize;
            long positionIndex;
            double minDouble = (double) min;
            if (useEdges) {
                cellSize = rangeSize / (double) (itemCount - 1);
                positionIndex = 0;
            } else {
                cellSize = rangeSize / (double) (itemCount + 1);
                positionIndex = 1;
            }
            for (long i = 0; i < itemCount; i++) {
                positions.add(Math.round(minDouble + ((double) positionIndex) * cellSize));
                positionIndex++;
            }
            return positions;
        }
    }

    public static String toString(Long number, int minDigits) {
        String str;
        if (number >= 0) {
            str = number.toString();
        } else {
            str = Long.toString(-1 * number);
        }

        while (str.length() < minDigits) {
            str = '0' + str;
        }
        if (number < 0) {
            str = '-' + str;
        }
        return str;
    }

    public static int displaceInRange(int value, int min, int max, int newMin, int newMax) {
        return displaceInRange(value, min, max, newMin, newMax, AmbiguityBehavior.MIN);
    }

    public static int displaceInRange(int value, int min, int max, int newMin, int newMax, AmbiguityBehavior ambiguityBehavior) {
        if (min > max || value < min || value > max || newMin > newMax) {
            throw new IllegalArgumentException("Wrong arguments: " + value + ", " + min + ", " + max + ", " + newMin + ", " + newMax);
        }
        if (min == max) {
            switch (ambiguityBehavior) {
                case MIN:
                    return newMin;
                case MID:
                    return (newMax - newMin) / 2;
                case MAX:
                    return newMax;
            }
        }
        if (value == min) {
            return newMin;
        } else if (value == max) {
            return newMax;
        } else {
            double factor = ((double) value - (double) min) / ((double) max - (double) min);
            double offset = factor * ((double) newMax - (double) newMin);
            int newValue = (int) ((double) newMin + offset);
            if (newValue < newMin) {
                newValue = newMin;
            } else if (newValue > newMax) {
                newValue = newMax;
            }
            return newValue;
        }
    }

    public static long displaceInRange(long value, long min, long max, long newMin, long newMax) {
        return displaceInRange(value, min, max, newMin, newMax, AmbiguityBehavior.MIN);
    }

    public static long displaceInRange(long value, long min, long max, long newMin, long newMax, AmbiguityBehavior ambiguityBehavior) {
        if (min > max || value < min || value > max || newMin > newMax) {
            throw new IllegalArgumentException("Wrong arguments: " + value + ", " + min + ", " + max + ", " + newMin + ", " + newMax);
        }
        if (min == max) {
            switch (ambiguityBehavior) {
                case MIN:
                    return newMin;
                case MID:
                    return (newMax - newMin) / 2L;
                case MAX:
                    return newMax;
            }
        }
        if (value == min) {
            return newMin;
        } else if (value == max) {
            return newMax;
        } else {
            double factor = ((double) value - (double) min) / ((double) max - (double) min);
            double offset = factor * ((double) newMax - (double) newMin);
            long newValue = (int) ((double) newMin + offset);
            if (newValue < newMin) {
                newValue = newMin;
            } else if (newValue > newMax) {
                newValue = newMax;
            }
            return newValue;
        }
    }

    public static double linearTranslation(double value, double start, double end, double newStart, double newEnd) {
        double factor = (value - start) / (end - start);
        double offset = factor * (newEnd - newStart);
        return newStart + offset;
    }

    public static int displaceInDividedRange(int value, int min, int max, int newMin, int newMax, int rangeCellCount, int moveToCell) {
        if (min > max || value < min || value > max || newMin > newMax) {
            throw new IllegalArgumentException("Wrong arguments: " + value + ", " + min + ", " + max + ", " + newMin + ", " + newMax);
        }
        // calculate min and max of the actual range and perform the displacement in that new range
        Duple<Integer, Integer> newRange = divideRange(newMin, newMax, rangeCellCount, moveToCell);
        return displaceInRange(value, min, max, newRange.element1, newRange.element2);
    }

    public static long displaceInDividedRange(long value, long min, long max, long newMin, long newMax, int rangeCellCount, int moveToCell) {
        if (min > max || value < min || value > max || newMin > newMax) {
            throw new IllegalArgumentException("Wrong arguments: " + value + ", " + min + ", " + max + ", " + newMin + ", " + newMax);
        }
        // calculate min and max of the actual range and perform the displacement in that new range
        Duple<Long, Long> newRange = divideRange(newMin, newMax, rangeCellCount, moveToCell);
        return displaceInRange(value, min, max, newRange.element1, newRange.element2);
    }

    /**
     * Divides a range in even cells, and generates the range for one of those cells
     *
     * @param min        the min value of the range
     * @param max        the max value of the range
     * @param cellCount  number of cells (sub-ranges) in which the original range is divided
     * @param moveToCell the cell to which we want to move
     * @return the range corresponding to the selected cell
     */
    public static Duple<Integer, Integer> divideRange(int min, int max, int cellCount, int moveToCell) {
        if (min > max || moveToCell < 0 || moveToCell >= cellCount) {
            throw new IllegalArgumentException("Wrong arguments: " + min + ", " + max + ", " + cellCount + ", " + moveToCell);
        }
        List<Integer> cellSizes = divideEven(max - min, cellCount);

        int newMin = min;
        int i;
        for (i = 0; i < moveToCell; i++) {
            newMin += cellSizes.get(i);
        }
        int newMax = newMin + cellSizes.get(moveToCell);
        return new Duple<>(newMin, newMax);
    }

    /**
     * Divides a range in even cells, and generates the range for one of those cells
     *
     * @param min        the min value of the range
     * @param max        the max value of the range
     * @param cellCount  number of cells (sub-ranges) in which the original range is divided
     * @param moveToCell the cell to which we want to move
     * @return the range corresponding to the selected cell
     */
    public static Duple<Long, Long> divideRange(long min, long max, int cellCount, int moveToCell) {
        if (min > max || moveToCell < 0 || moveToCell >= cellCount) {
            throw new IllegalArgumentException("Wrong arguments: " + min + ", " + max + ", " + cellCount + ", " + moveToCell);
        }
        List<Long> cellSizes = divideEven(max - min, cellCount);

        long newMin = min;
        int i;
        for (i = 0; i < moveToCell; i++) {
            newMin += cellSizes.get(i);
        }
        long newMax = newMin + cellSizes.get(moveToCell);
        return new Duple<>(newMin, newMax);
    }

    public static Duple<Integer, Integer> divideRangeRecursive(int min, int max, int[] cellCount, int[] moveToCell) {
        if (cellCount.length != moveToCell.length) {
            throw new IllegalArgumentException("Wrong arguments: need same number of cellCounts and moveToCells, found " + cellCount.length + " and " + moveToCell.length);
        }
        return divideRangeRecursiveAux(min, max, cellCount, moveToCell, 0);
    }

    private static Duple<Integer, Integer> divideRangeRecursiveAux(int min, int max, int[] cellCount, int[] moveToCell, int index) {
        if (cellCount.length == index) {
            return new Duple<>(min, max);
        } else {
            Duple<Integer, Integer> newRange = divideRange(min, max, cellCount[index], moveToCell[index]);
            return divideRangeRecursiveAux(newRange.element1, newRange.element2, cellCount, moveToCell, index + 1);
        }
    }

    public static Duple<Long, Long> divideRangeRecursive(long min, long max, int[] cellCount, int[] moveToCell) {
        if (cellCount.length != moveToCell.length) {
            throw new IllegalArgumentException("Wrong arguments: need same number of cellCounts and moveToCells, found " + cellCount.length + " and " + moveToCell.length);
        }
        return divideRangeRecursiveAux(min, max, cellCount, moveToCell, 0);
    }

    private static Duple<Long, Long> divideRangeRecursiveAux(long min, long max, int[] cellCount, int[] moveToCell, int index) {
        if (cellCount.length == index) {
            return new Duple<>(min, max);
        } else {
            Duple<Long, Long> newRange = divideRange(min, max, cellCount[index], moveToCell[index]);
            return divideRangeRecursiveAux(newRange.element1, newRange.element2, cellCount, moveToCell, index + 1);
        }
    }

    public static String decimalToBitString(int n, int min, int max) {
        max -= min;
        n -= min;
        if (max == 0 || n < 0 || n > max) {
            return "";
        }
        // find biggest power of 2 that fits between min and max
        int biggestPower = (int) logBaseN(max + 1, 2);
        int maxInBiggestPower = (int) Math.pow(2, biggestPower);
        if (n < maxInBiggestPower) {
            // n is translated to bits assuming the biggest power. The returned bit string will have "biggestPower" bits
            String bitString = Integer.toBinaryString(n);
            while (bitString.length() < biggestPower) {
                bitString = "0" + bitString;
            }
            return bitString;
        } else {
            // substract the biggest power range and recalculate
            return decimalToBitString(n - maxInBiggestPower, 0, max - maxInBiggestPower);
        }
    }

    public static double logBaseN(double n, double base) {
        return Math.log(n) / Math.log(base);
    }

    public static int min(int a, int... rest) {
        if (rest.length == 0) {
            return a;
        } else {
            int[] newRest = Arrays.copyOfRange(rest, 1, rest.length);
            return min(Math.min(a, rest[0]), newRest);
        }
    }

    public static float min(float a, float... rest) {
        if (rest.length == 0) {
            return a;
        } else {
            float[] newRest = Arrays.copyOfRange(rest, 1, rest.length);
            return min(Math.min(a, rest[0]), newRest);
        }
    }

    public static double min(double a, double... rest) {
        if (rest.length == 0) {
            return a;
        } else {
            double[] newRest = Arrays.copyOfRange(rest, 1, rest.length);
            return min(Math.min(a, rest[0]), newRest);
        }
    }

    public static long min(long a, long... rest) {
        if (rest.length == 0) {
            return a;
        } else {
            long[] newRest = Arrays.copyOfRange(rest, 1, rest.length);
            return min(Math.min(a, rest[0]), newRest);
        }
    }

    public static int max(int a, int... rest) {
        if (rest.length == 0) {
            return a;
        } else {
            int[] newRest = Arrays.copyOfRange(rest, 1, rest.length);
            return max(Math.max(a, rest[0]), newRest);
        }
    }

    public static float max(float a, float... rest) {
        if (rest.length == 0) {
            return a;
        } else {
            float[] newRest = Arrays.copyOfRange(rest, 1, rest.length);
            return max(Math.max(a, rest[0]), newRest);
        }
    }

    public static double max(double a, double... rest) {
        if (rest.length == 0) {
            return a;
        } else {
            double[] newRest = Arrays.copyOfRange(rest, 1, rest.length);
            return max(Math.max(a, rest[0]), newRest);
        }
    }

    public static long max(long a, long... rest) {
        if (rest.length == 0) {
            return a;
        } else {
            long[] newRest = Arrays.copyOfRange(rest, 1, rest.length);
            return max(Math.max(a, rest[0]), newRest);
        }
    }

    public static int limitInRange(int value, Integer min, Integer max) {
        if (min != null && value < min) {
            value = min;
        }
        if (max != null && value > max) {
            value = max;
        }
        return value;
    }

    public static long limitInRange(long value, Long min, Long max) {
        if (min != null && value < min) {
            value = min;
        }
        if (max != null && value > max) {
            value = max;
        }
        return value;
    }

    public static float limitInRange(float value, Float min, Float max) {
        if (min != null && value < min) {
            value = min;
        }
        if (max != null && value > max) {
            value = max;
        }
        return value;
    }

    public static double limitInRange(double value, Double min, Double max) {
        if (min != null && value < min) {
            value = min;
        }
        if (max != null && value > max) {
            value = max;
        }
        return value;
    }
}
