package jacz.util.numeric.newrange;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Range list test
 */
public class TestRangeList {

    @Test
    public void testBasic() {
        RangeList<Integer> rangeList = new RangeList<>(new IntegerRange(1, 5));
        List<Range<Integer>> integerRangeList = new ArrayList<>();
        integerRangeList.add(new IntegerRange(1, 5));
        Assert.assertEquals(integerRangeList, rangeList.getRangesAsList());

        integerRangeList = new ArrayList<>();
        integerRangeList.add(new IntegerRange(-3, -1));
        integerRangeList.add(new IntegerRange(1, 5));
        integerRangeList.add(new IntegerRange(8, 14));
        rangeList = new RangeList<>(integerRangeList);
        integerRangeList = new ArrayList<>();
        integerRangeList.add(new IntegerRange(-3, -1));
        integerRangeList.add(new IntegerRange(1, 5));
        integerRangeList.add(new IntegerRange(8, 14));
        Assert.assertEquals(integerRangeList, rangeList.getRangesAsList());

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        integerRangeList = new ArrayList<>();
        integerRangeList.add(new IntegerRange(-3, -1));
        integerRangeList.add(new IntegerRange(1, 5));
        integerRangeList.add(new IntegerRange(8, 14));
        Assert.assertEquals(integerRangeList, rangeList.getRangesAsList());
    }

    @Test
    public void testIsEmpty() {
        RangeList<Integer> rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        Assert.assertFalse(rangeList.isEmpty());
        Assert.assertTrue(new RangeList<>(new IntegerRange(-3, -4)).isEmpty());
    }

    @Test
    public void testContainsSearch() {
        RangeList<Integer> rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        Assert.assertTrue(rangeList.contains(1));
        Assert.assertTrue(rangeList.contains(3));
        Assert.assertTrue(rangeList.contains(8));
        Assert.assertTrue(rangeList.contains(14));
        Assert.assertFalse(rangeList.contains(0));
        Assert.assertFalse(rangeList.contains(6));
        Assert.assertFalse(rangeList.contains(15));

        Assert.assertEquals(0, rangeList.search(-1));
        Assert.assertEquals(1, rangeList.search(3));
        Assert.assertEquals(2, rangeList.search(10));
        Assert.assertEquals(-1, rangeList.search(20));

        Assert.assertEquals(0, rangeList.search(new Range<>(-3, -1, Integer.class)));
        Assert.assertEquals(-1, rangeList.search(new Range<>(-3, 1, Integer.class)));
        Assert.assertEquals(1, rangeList.search(new Range<>(1, 5, Integer.class)));
        Assert.assertEquals(2, rangeList.search(new Range<>(12, 14, Integer.class)));
        Assert.assertTrue(rangeList.contains(new Range<>(-3, -1, Integer.class)));
        Assert.assertFalse(rangeList.contains(new Range<>(-3, 1, Integer.class)));
        Assert.assertTrue(rangeList.contains(new Range<>(1, 5, Integer.class)));
        Assert.assertTrue(rangeList.contains(new Range<>(12, 14, Integer.class)));
    }
}
