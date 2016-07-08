package aanguita.jacuzzi.numeric.range;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the range class
 */
public class TestRange {

    @Test
    public void testBasic() {
        IntegerRange integerRange = new IntegerRange(5, 6);
        Assert.assertEquals(5, integerRange.getMin().intValue());
        Assert.assertEquals(6, integerRange.getMax().intValue());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[5, 6]", integerRange.toString());
        Assert.assertEquals(2L, integerRange.size().longValue());

        integerRange = new IntegerRange(null, 27);
        Assert.assertEquals(null, integerRange.getMin());
        Assert.assertEquals(27, integerRange.getMax().intValue());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[-inf, 27]", integerRange.toString());
        Assert.assertEquals(null, integerRange.size());

        integerRange = new IntegerRange(8, null);
        Assert.assertEquals(8, integerRange.getMin().intValue());
        Assert.assertEquals(null, integerRange.getMax());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[8, +inf]", integerRange.toString());
        Assert.assertEquals(null, integerRange.size());

        integerRange = new IntegerRange(null, null);
        Assert.assertEquals(null, integerRange.getMin());
        Assert.assertEquals(null, integerRange.getMax());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[-inf, +inf]", integerRange.toString());
        Assert.assertEquals(null, integerRange.size());

        integerRange = new IntegerRange(5, 4);
        Assert.assertEquals(5, integerRange.getMin().intValue());
        Assert.assertEquals(4, integerRange.getMax().intValue());
        Assert.assertTrue(integerRange.isEmpty());
        Assert.assertEquals("[5, 4]", integerRange.toString());
        Assert.assertEquals(0L, integerRange.size().longValue());
    }

    @Test
    public void testContains() {
        IntegerRange integerRange = new IntegerRange(5, 6);
        Assert.assertTrue(integerRange.contains(5));
        Assert.assertFalse(integerRange.contains(0));
        Assert.assertFalse(integerRange.contains(100));

        integerRange = new IntegerRange(null, 27);
        Assert.assertTrue(integerRange.contains(5));
        Assert.assertTrue(integerRange.contains(0));
        Assert.assertFalse(integerRange.contains(100));

        integerRange = new IntegerRange(8, null);
        Assert.assertTrue(integerRange.contains(15));
        Assert.assertFalse(integerRange.contains(0));
        Assert.assertTrue(integerRange.contains(100));

        integerRange = new IntegerRange(null, null);
        Assert.assertTrue(integerRange.contains(5));
        Assert.assertTrue(integerRange.contains(0));
        Assert.assertTrue(integerRange.contains(100));

        integerRange = new IntegerRange(5, 4);
        Assert.assertFalse(integerRange.contains(5));
        Assert.assertFalse(integerRange.contains(0));
        Assert.assertFalse(integerRange.contains(100));
    }

    @Test
    public void testCompareToValues() {
        IntegerRange integerRange = new IntegerRange(5, 6);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.RIGHT);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.LEFT);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);

        integerRange = new IntegerRange(null, 27);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.LEFT);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);

        integerRange = new IntegerRange(8, null);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.RIGHT);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.RIGHT);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);

        integerRange = new IntegerRange(null, null);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);

        integerRange = new IntegerRange(5, 4);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);
    }

    @Test
    public void testCompareRanges() {
        IntegerRange integerRange = new IntegerRange(0, 10);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, -1)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, 20)), Range.RangeComparison.LEFT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(11, 20)), Range.RangeComparison.LEFT_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 20)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 10)), Range.RangeComparison.EQUALS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 10)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 15)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 15)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 10)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 6)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 5)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -1)), Range.RangeComparison.RIGHT_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -5)), Range.RangeComparison.RIGHT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, -5)), Range.RangeComparison.RIGHT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 5)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 15)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, null)), Range.RangeComparison.LEFT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, null)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, null)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, null)), Range.RangeComparison.INSIDE);

        integerRange = new IntegerRange(null, 10);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, -1)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, 20)), Range.RangeComparison.LEFT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(11, 20)), Range.RangeComparison.LEFT_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 20)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 10)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 10)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 15)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 15)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 10)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 6)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -1)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, -5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 15)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, null)), Range.RangeComparison.LEFT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, null)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, null)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, null)), Range.RangeComparison.INSIDE);

        integerRange = new IntegerRange(0, null);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, -1)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, 20)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(11, 20)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 20)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 10)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 10)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 15)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 15)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 10)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 6)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 5)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -1)), Range.RangeComparison.RIGHT_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -5)), Range.RangeComparison.RIGHT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, -5)), Range.RangeComparison.RIGHT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 5)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 15)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, null)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, null)), Range.RangeComparison.EQUALS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, null)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, null)), Range.RangeComparison.INSIDE);

        integerRange = new IntegerRange(null, null);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, -1)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, 20)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(11, 20)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 20)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 10)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 10)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 15)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 15)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 10)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 6)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -1)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, -5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 5)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 15)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, null)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, null)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, null)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, null)), Range.RangeComparison.EQUALS);

        integerRange = new IntegerRange(1, 0);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, -1)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, 20)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(11, 20)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 20)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 10)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 10)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 15)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 15)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, 5)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 10)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(5, 6)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, 5)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -1)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, -5)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, -5)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 5)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, 15)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(15, null)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(0, null)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(-5, null)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new IntegerRange(null, null)), Range.RangeComparison.ANY_EMPTY);
    }


    @Test
    public void testIntersection() {
        IntegerRange emptyRange = new IntegerRange(0, -1);
        IntegerRange integerRange = new IntegerRange(0, 10);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -5)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -1)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(11, 15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(15, 15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 5)), new IntegerRange(0, 5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(0, 10)), new IntegerRange(0, 10));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 20)), new IntegerRange(0, 10));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(3, 5)), new IntegerRange(3, 5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(5, 15)), new IntegerRange(5, 10));

        integerRange = new IntegerRange(null, 10);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -15)), new IntegerRange(-10, -15));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -5)), new IntegerRange(-10, -5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 15)), new IntegerRange(-10, 10));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(11, 15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(15, 15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 5)), new IntegerRange(-10, 5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(0, 10)), new IntegerRange(0, 10));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 20)), new IntegerRange(-10, 10));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(3, 5)), new IntegerRange(3, 5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(5, 15)), new IntegerRange(5, 10));

        integerRange = new IntegerRange(0, null);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -5)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 15)), new IntegerRange(0, 15));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(11, 15)), new IntegerRange(11, 15));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(15, 15)), new IntegerRange(15, 15));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 5)), new IntegerRange(0, 5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(0, 10)), new IntegerRange(0, 10));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 20)), new IntegerRange(0, 20));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(3, 5)), new IntegerRange(3, 5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(5, 15)), new IntegerRange(5, 15));

        integerRange = new IntegerRange(null, null);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -15)), new IntegerRange(-10, -15));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -5)), new IntegerRange(-10, -5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 15)), new IntegerRange(-10, 15));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(11, 15)), new IntegerRange(11, 15));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(15, 15)), new IntegerRange(15, 15));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 5)), new IntegerRange(-10, 5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(0, 10)), new IntegerRange(0, 10));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 20)), new IntegerRange(-10, 20));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(3, 5)), new IntegerRange(3, 5));
        Assert.assertEquals(integerRange.intersection(new IntegerRange(5, 15)), new IntegerRange(5, 15));

        integerRange = new IntegerRange(1, 0);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, -5)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(11, 15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(15, 15)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 5)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(0, 10)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(-10, 20)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(3, 5)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new IntegerRange(5, 15)), emptyRange);
    }

    @Test
    public void testGetPosition() {
        IntegerRange integerRange = new IntegerRange(0, 10);
        Assert.assertEquals((Integer) 5, integerRange.getPosition(5L));
        try {
            integerRange.getPosition(15L);
        } catch (IndexOutOfBoundsException e) {
            Assert.assertEquals(IndexOutOfBoundsException.class, e.getClass());
        }
        try {
            integerRange.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }

        integerRange = new IntegerRange(0, -1);
        try {
            integerRange.getPosition(5L);
        } catch (IndexOutOfBoundsException e) {
            Assert.assertEquals(IndexOutOfBoundsException.class, e.getClass());
        }
        try {
            integerRange.getPosition(15L);
        } catch (IndexOutOfBoundsException e) {
            Assert.assertEquals(IndexOutOfBoundsException.class, e.getClass());
        }
        try {
            integerRange.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }

        integerRange = new IntegerRange(null, 10);
        Assert.assertEquals(null, integerRange.getPosition(5L));
        Assert.assertEquals(null, integerRange.getPosition(15L));
        try {
            integerRange.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }

        integerRange = new IntegerRange(0, null);
        Assert.assertEquals((Integer) 5, integerRange.getPosition(5L));
        Assert.assertEquals((Integer) 15, integerRange.getPosition(15L));
        try {
            integerRange.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }

        integerRange = new IntegerRange(null, null);
        Assert.assertEquals(null, integerRange.getPosition(5L));
        Assert.assertEquals(null, integerRange.getPosition(15L));
        try {
            integerRange.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }
}
