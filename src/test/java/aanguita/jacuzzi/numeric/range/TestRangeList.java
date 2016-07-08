package aanguita.jacuzzi.numeric.range;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Range list tests
 */
public class TestRangeList {

    @Test
    public void testBasic() {
        IntegerRangeList rangeList = new IntegerRangeList(1, 5);
        List<IntegerRange> integerRangeList = new ArrayList<>();
        integerRangeList.add(new IntegerRange(1, 5));
        Assert.assertEquals(integerRangeList, rangeList.getRangesAsList());

        integerRangeList = new ArrayList<>();
        integerRangeList.add(new IntegerRange(-3, -1));
        integerRangeList.add(new IntegerRange(1, 5));
        integerRangeList.add(new IntegerRange(8, 14));
        rangeList = new IntegerRangeList(integerRangeList);
        integerRangeList = new ArrayList<>();
        integerRangeList.add(new IntegerRange(-3, -1));
        integerRangeList.add(new IntegerRange(1, 5));
        integerRangeList.add(new IntegerRange(8, 14));
        Assert.assertEquals(integerRangeList, rangeList.getRangesAsList());

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        integerRangeList = new ArrayList<>();
        integerRangeList.add(new IntegerRange(-3, -1));
        integerRangeList.add(new IntegerRange(1, 5));
        integerRangeList.add(new IntegerRange(8, 14));
        Assert.assertEquals(integerRangeList, rangeList.getRangesAsList());
    }

    @Test
    public void testIsEmpty() {
        IntegerRangeList rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        Assert.assertFalse(rangeList.isEmpty());
        Assert.assertTrue(new IntegerRangeList(new IntegerRange(-3, -4)).isEmpty());
    }

    @Test
    public void testContainsSearch() {
        IntegerRangeList rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        Assert.assertTrue(rangeList.contains(-1));
        Assert.assertTrue(rangeList.contains(1));
        Assert.assertTrue(rangeList.contains(3));
        Assert.assertTrue(rangeList.contains(8));
        Assert.assertTrue(rangeList.contains(14));
        Assert.assertFalse(rangeList.contains(0));
        Assert.assertFalse(rangeList.contains(6));
        Assert.assertFalse(rangeList.contains(15));

        Integer nullInt = null;
        Assert.assertEquals(null, rangeList.search(nullInt));
        Assert.assertEquals(new Integer(-1), rangeList.search(-5));
        Assert.assertEquals(new Integer(0), rangeList.search(-3));
        Assert.assertEquals(new Integer(-2), rangeList.search(0));
        Assert.assertEquals(new Integer(1), rangeList.search(3));
        Assert.assertEquals(new Integer(-3), rangeList.search(6));
        Assert.assertEquals(new Integer(2), rangeList.search(14));
        Assert.assertEquals(new Integer(-4), rangeList.search(20));

        Assert.assertEquals(0, rangeList.search(new IntegerRange(-3, -1)));
        Assert.assertEquals(-1, rangeList.search(new IntegerRange(-3, 1)));
        Assert.assertEquals(1, rangeList.search(new IntegerRange(1, 5)));
        Assert.assertEquals(2, rangeList.search(new IntegerRange(12, 14)));
        Assert.assertTrue(rangeList.contains(new IntegerRange(-3, -1)));
        Assert.assertFalse(rangeList.contains(new IntegerRange(-3, 1)));
        Assert.assertTrue(rangeList.contains(new IntegerRange(1, 5)));
        Assert.assertTrue(rangeList.contains(new IntegerRange(12, 14)));
    }

    @Test
    public void testRemove() {
        IntegerRangeList rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new IntegerRange(-1, 1));
        Assert.assertEquals(new IntegerRangeList(-3, -2, 2, 5, 8, 14), rangeList);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new IntegerRange(-1, 15));
        Assert.assertEquals(new IntegerRangeList(-3, -2), rangeList);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new IntegerRange(0, 0));
        Assert.assertEquals(new IntegerRangeList(-3, -1, 1, 5, 8, 14), rangeList);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new IntegerRange(0, -1));
        Assert.assertEquals(new IntegerRangeList(-3, -1, 1, 5, 8, 14), rangeList);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new IntegerRange(null, 10));
        Assert.assertEquals(new IntegerRangeList(11, 14), rangeList);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new IntegerRange(3, null));
        Assert.assertEquals(new IntegerRangeList(-3, -1, 1, 2), rangeList);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new IntegerRange(null, null));
        Assert.assertEquals(new IntegerRangeList(), rangeList);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        rangeList.remove(new IntegerRangeList(-2, 2, 9, 10));
        Assert.assertEquals(new IntegerRangeList(-3, -3, 3, 5, 8, 8, 11, 14), rangeList);
    }

    @Test
    public void testIntersection() {
        IntegerRangeList rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        IntegerRangeList intersection = rangeList.intersection(new IntegerRange(-1, 1));
        Assert.assertEquals(new IntegerRangeList(-1, -1, 1, 1), intersection);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new IntegerRange(-1, 15));
        Assert.assertEquals(new IntegerRangeList(-1, -1, 1, 5, 8, 14), intersection);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new IntegerRange(0, 0));
        Assert.assertEquals(new IntegerRangeList(), intersection);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new IntegerRange(0, -1));
        Assert.assertEquals(new IntegerRangeList(), intersection);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new IntegerRange(null, 10));
        Assert.assertEquals(new IntegerRangeList(-3, -1, 1, 5, 8, 10), intersection);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new IntegerRange(3, null));
        Assert.assertEquals(new IntegerRangeList(3, 5, 8, 14), intersection);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new IntegerRange(null, null));
        Assert.assertEquals(rangeList, intersection);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new IntegerRangeList(-2, 2, 9, 10));
        Assert.assertEquals(new IntegerRangeList(-2, -1, 1, 2, 9, 10), intersection);

        rangeList = new IntegerRangeList(1, 5, 8, 9, 10, 14, -3, -1);
        intersection = rangeList.intersection(new IntegerRange(-2, 2), (new IntegerRange(9, 10)));
        Assert.assertEquals(new IntegerRangeList(-2, -1, 1, 2, 9, 10), intersection);
    }

    @Test
    public void testGetPosition() {
        IntegerRangeList rangeList = new IntegerRangeList(-3, -1, 1, 5, 8, 14);
        Assert.assertEquals(new Integer(3), rangeList.getPosition(5L));
        Assert.assertEquals(new Integer(10), rangeList.getPosition(10L));
        try {
            rangeList.getPosition(25L);
        } catch (IndexOutOfBoundsException e) {
            Assert.assertEquals(IndexOutOfBoundsException.class, e.getClass());
        }
        try {
            rangeList.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }

        rangeList = new IntegerRangeList(null, -1, 1, 5, 8, 14);
        Assert.assertEquals(null, rangeList.getPosition(2L));
        try {
            rangeList.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    public void testSize() {
        IntegerRangeList rangeList = new IntegerRangeList(-3, -1, 1, 5, 8, 14);
        Assert.assertEquals(new Long(15L), rangeList.size());

        rangeList = new IntegerRangeList(null, -1, 1, 5, 8, 14);
        Assert.assertEquals(null, rangeList.size());
    }

    @Test
    public void testClear() {
        IntegerRangeList rangeList = new IntegerRangeList(-3, -1, 1, 5, 8, 14);
        rangeList.clear();

        Assert.assertEquals(new IntegerRangeList(), rangeList);
    }
}
