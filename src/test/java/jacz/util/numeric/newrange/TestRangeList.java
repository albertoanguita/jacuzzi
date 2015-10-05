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

    @Test
    public void testRemove() {
        RangeList<Integer> rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new Range<>(-1, 1, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, -3, -2, 2, 5, 8, 14), rangeList);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new Range<>(-1, 15, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, -3, -2), rangeList);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new Range<>(0, 0, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, -3, -1, 1, 5, 8, 14), rangeList);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new Range<>(0, -1, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, -3, -1, 1, 5, 8, 14), rangeList);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new Range<>(null, 10, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, 11, 14), rangeList);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new Range<>(3, null, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, -3, -1, 1, 2), rangeList);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new Range<>(null, null, Integer.class));
        Assert.assertEquals(new RangeList<>(), rangeList);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new RangeList<>(Integer.class, -2, 2, 9, 10));
        Assert.assertEquals(new RangeList<>(Integer.class, -3, -3, 3, 5, 8, 8, 11, 14), rangeList);
    }

    @Test
    public void testIntersection() {
        RangeList<Integer> rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        RangeList<Integer> intersection = rangeList.intersection(new Range<>(-1, 1, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, -1, -1, 1, 1), intersection);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new Range<>(-1, 15, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, -1, -1, 1, 5, 8, 14), intersection);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new Range<>(0, 0, Integer.class));
        Assert.assertEquals(new RangeList<>(), intersection);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new Range<>(0, -1, Integer.class));
        Assert.assertEquals(new RangeList<>(), intersection);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new Range<>(null, 10, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, -3, -1, 1, 5, 8, 10), intersection);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new Range<>(3, null, Integer.class));
        Assert.assertEquals(new RangeList<>(Integer.class, 3, 5, 8, 14), intersection);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new Range<>(null, null, Integer.class));
        Assert.assertEquals(rangeList, intersection);

        rangeList = new RangeList<>(Integer.class, 1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new RangeList<>(Integer.class, -2, 2, 9, 10));
        Assert.assertEquals(new RangeList<>(Integer.class, -2, -1, 1, 2, 9, 10), intersection);
    }

    @Test
    public void testGetPosition() {
    }
}
