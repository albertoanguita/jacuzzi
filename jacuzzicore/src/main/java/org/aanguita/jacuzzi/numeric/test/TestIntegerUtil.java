package org.aanguita.jacuzzi.numeric.test;

import org.aanguita.jacuzzi.lists.tuple.Duple;
import org.aanguita.jacuzzi.numeric.NumericUtil;

import java.util.List;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 13-mar-2010<br>
 * Last Modified: 13-mar-2010
 */
public class TestIntegerUtil {

    public static void main(String args[]) {

        int value = NumericUtil.displaceInDividedRange(4, 0, 4, 0, 6, 12, 11);

        List<Integer> segments = NumericUtil.divideEven(5, 15);

        System.out.println(segments);


        /*int newValue = NumericUtil.displaceInRange(5, 0, 10, 0, 100);
        int newValue2 = NumericUtil.displaceInDividedRange(250, 0, 1000, 0, 1000, 1, 0);
        System.out.println(newValue2);*/


        Duple<Integer, Integer> newRange = NumericUtil.divideRange(0, 99, 5, 3);
        int[] rangeCount = new int[3];
        int[] moveTo = new int[3];
        rangeCount[0] = 2;
        rangeCount[1] = 5;
        rangeCount[2] = 4;
        moveTo[0] = 0;
        moveTo[1] = 0;
        moveTo[2] = 1;
        Duple<Integer, Integer> newRange2 = NumericUtil.divideRangeRecursive(0, 1000, rangeCount, moveTo);


        System.out.println(newRange);

    }
}
