package jacz.util.numeric.newrange;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alberto on 22/09/2015.
 */
public class TestRange {

    @Test
    public void testBuildRange() {
        Range<Integer> integerRange_1 = new Range<>(5, 6, Integer.class);
        Assert.assertEquals(5, integerRange_1.getMin().intValue());
        Assert.assertEquals(6, integerRange_1.getMax().intValue());
    }


    @Test
    public void testMerge() {
        List<Range<Integer>> ranges = new ArrayList<>();
        ranges.add(new Range<>(-5, -1, Integer.class));
        ranges.add(new Range<>(5, 6, Integer.class));
        ranges.add(new Range<>(5, -4, Integer.class));
        ranges.add(new Range<>(10, 16, Integer.class));
        ranges.add(new Range<>(25, 26, Integer.class));
        ranges.add(new Range<>(27, 36, Integer.class));
        ranges.add(new Range<>(45, 50, Integer.class));

        List<Range<Integer>> mergedRanges = new Range<>(0, 1, Integer.class).union(ranges);
        System.out.println(mergedRanges);
    }

}
