package jacz.util.numeric.test;

import jacz.util.numeric.IntegerRange;
import jacz.util.numeric.LongRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 12-mar-2010<br>
 * Last Modified: 12-mar-2010
 */
public class TestIntegerRange {

    public static void main(String args[]) {
        IntegerRange ir = new IntegerRange(7, 16);
        System.out.println(ir);

        System.out.println(ir.contains(15));


        LongRange lr = new LongRange((long) 7, (long) 16);
        LongRange lr2 = new LongRange((long) 6, (long) 16);
        System.out.println(lr.contains((long) 6));
        System.out.println("equals: " + lr.equals(lr2));
        System.out.println(lr.overlapping(lr2));


        List<LongRange> lrList = new ArrayList<LongRange>();
        lrList.add(new LongRange((long) 5, (long) 8));
        lrList.add(new LongRange((long) 11, (long) 12));
        lrList.add(new LongRange((long) 9, (long) 16));
        lrList.add(new LongRange((long) 1, (long) 3));
        //List<IntegerRange> lrList2 = new ArrayList<IntegerRange>();

        List<LongRange> lrListS = lr.subtract(lrList);

        List<LongRange> lrListM = LongRange.merge(lrList);

        System.out.println(lrListS);
        System.out.println(lrListM);

        //System.out.println(lr2.contains((long) 15));
    }
}
