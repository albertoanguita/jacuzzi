package jacz.util.numeric.newrange;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Alberto on 22/09/2015.
 */
public class TestRange {

    @Test
    public void testBuildRange() {
        Range<Integer> integerRange = new Range<>(5, 6, Integer.class);
        Assert.assertEquals(5, integerRange.getMin().intValue());
        Assert.assertEquals(6, integerRange.getMax().intValue());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[5, 6]", integerRange.toString());
        Assert.assertEquals(2L, integerRange.size().longValue());
        Assert.assertTrue(integerRange.contains(5));
        Assert.assertFalse(integerRange.contains(0));
        Assert.assertFalse(integerRange.contains(10));

        integerRange = new Range<>(null, 27, Integer.class);
        Assert.assertEquals(null, integerRange.getMin());
        Assert.assertEquals(27, integerRange.getMax().intValue());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[-inf, 27]", integerRange.toString());
        Assert.assertEquals(null, integerRange.size());
        Assert.assertTrue(integerRange.contains(5));
        Assert.assertTrue(integerRange.contains(0));
        Assert.assertFalse(integerRange.contains(100));

        integerRange = new Range<>(8, null, Integer.class);
        Assert.assertEquals(8, integerRange.getMin().intValue());
        Assert.assertEquals(null, integerRange.getMax());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[8, +inf]", integerRange.toString());
        Assert.assertEquals(null, integerRange.size());
        Assert.assertTrue(integerRange.contains(15));
        Assert.assertFalse(integerRange.contains(0));
        Assert.assertTrue(integerRange.contains(100));

        integerRange = new Range<>(null, null, Integer.class);
        Assert.assertEquals(null, integerRange.getMin());
        Assert.assertEquals(null, integerRange.getMax());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[-inf, +inf]", integerRange.toString());
        Assert.assertEquals(null, integerRange.size());
        Assert.assertTrue(integerRange.contains(5));
        Assert.assertTrue(integerRange.contains(0));
        Assert.assertTrue(integerRange.contains(10));

        integerRange = new Range<>(5, 4, Integer.class);
        Assert.assertEquals(5, integerRange.getMin().intValue());
        Assert.assertEquals(4, integerRange.getMax().intValue());
        Assert.assertTrue(integerRange.isEmpty());
        Assert.assertEquals("[5, 4]", integerRange.toString());
        Assert.assertEquals(0L, integerRange.size().longValue());
        Assert.assertFalse(integerRange.contains(5));
        Assert.assertFalse(integerRange.contains(0));
        Assert.assertFalse(integerRange.contains(10));

    }

//    @Test
//    public void testEquals() {
//    }
//
//    @Test
//    public void testMerge() {
//        List<Range<Integer>> ranges = new ArrayList<>();
//        ranges.add(new Range<>(-5, -1, Integer.class));
//        ranges.add(new Range<>(5, 6, Integer.class));
//        ranges.add(new Range<>(5, -4, Integer.class));
//        ranges.add(new Range<>(10, 16, Integer.class));
//        ranges.add(new Range<>(25, 26, Integer.class));
//        ranges.add(new Range<>(27, 36, Integer.class));
//        ranges.add(new Range<>(45, 50, Integer.class));
//
////        List<Range<Integer>> mergedRanges = new Range<>(0, 1, Integer.class).union(ranges);
////        System.out.println(mergedRanges);
//    }

}
