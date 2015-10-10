package jacz.util.numeric.test;

import jacz.util.numeric.oldrange.LongRange;
import jacz.util.numeric.oldrange.RangeSet;

/**
 *
 */
public class TestRangeSet {

    public static void main(String args[]) {

        RangeSet<LongRange, Long> rs = new RangeSet<LongRange, Long>();

        rs.add(new LongRange(1L, 2L));
        rs.add(new LongRange(3L, 3L));
        rs.add(new LongRange(8L, 10L));
        rs.add(new LongRange(7L, 12L));
        rs.add(new LongRange(15L, 20L));
        rs.add(new LongRange(5L, 5L));
        rs.add(new LongRange(-10L, -5L));
        rs.remove(new LongRange(1L, 2L));
        rs.remove(new LongRange(100L, 102L));
        rs.add(new LongRange(0L, -1L));

        System.out.println(rs);

        RangeSet<LongRange, Long> rs2 = new RangeSet<LongRange, Long>();

        rs2.add(new LongRange(0L, 1L));
        boolean b = rs2.contains(new LongRange(0L, 1L));
        System.out.println(b);


        System.out.println("FIN");
    }
}
