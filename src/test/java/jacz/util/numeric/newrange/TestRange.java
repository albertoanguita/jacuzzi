package jacz.util.numeric.newrange;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the range class
 */
public class TestRange {

    @Test
    public void testBasic() {
        Range<Integer> integerRange = new Range<>(5, 6, Integer.class);
        Assert.assertEquals(5, integerRange.getMin().intValue());
        Assert.assertEquals(6, integerRange.getMax().intValue());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[5, 6]", integerRange.toString());
        Assert.assertEquals(2L, integerRange.size().longValue());

        integerRange = new Range<>(null, 27, Integer.class);
        Assert.assertEquals(null, integerRange.getMin());
        Assert.assertEquals(27, integerRange.getMax().intValue());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[-inf, 27]", integerRange.toString());
        Assert.assertEquals(null, integerRange.size());

        integerRange = new Range<>(8, null, Integer.class);
        Assert.assertEquals(8, integerRange.getMin().intValue());
        Assert.assertEquals(null, integerRange.getMax());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[8, +inf]", integerRange.toString());
        Assert.assertEquals(null, integerRange.size());

        integerRange = new Range<>(null, null, Integer.class);
        Assert.assertEquals(null, integerRange.getMin());
        Assert.assertEquals(null, integerRange.getMax());
        Assert.assertFalse(integerRange.isEmpty());
        Assert.assertEquals("[-inf, +inf]", integerRange.toString());
        Assert.assertEquals(null, integerRange.size());

        integerRange = new Range<>(5, 4, Integer.class);
        Assert.assertEquals(5, integerRange.getMin().intValue());
        Assert.assertEquals(4, integerRange.getMax().intValue());
        Assert.assertTrue(integerRange.isEmpty());
        Assert.assertEquals("[5, 4]", integerRange.toString());
        Assert.assertEquals(0L, integerRange.size().longValue());
    }

    @Test
    public void testContains() {
        Range<Integer> integerRange = new Range<>(5, 6, Integer.class);
        Assert.assertTrue(integerRange.contains(5));
        Assert.assertFalse(integerRange.contains(0));
        Assert.assertFalse(integerRange.contains(100));

        integerRange = new Range<>(null, 27, Integer.class);
        Assert.assertTrue(integerRange.contains(5));
        Assert.assertTrue(integerRange.contains(0));
        Assert.assertFalse(integerRange.contains(100));

        integerRange = new Range<>(8, null, Integer.class);
        Assert.assertTrue(integerRange.contains(15));
        Assert.assertFalse(integerRange.contains(0));
        Assert.assertTrue(integerRange.contains(100));

        integerRange = new Range<>(null, null, Integer.class);
        Assert.assertTrue(integerRange.contains(5));
        Assert.assertTrue(integerRange.contains(0));
        Assert.assertTrue(integerRange.contains(100));

        integerRange = new Range<>(5, 4, Integer.class);
        Assert.assertFalse(integerRange.contains(5));
        Assert.assertFalse(integerRange.contains(0));
        Assert.assertFalse(integerRange.contains(100));
    }

    @Test
    public void testCompareToValues() {
        Range<Integer> integerRange = new Range<>(5, 6, Integer.class);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.RIGHT);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.LEFT);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);

        integerRange = new Range<>(null, 27, Integer.class);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.LEFT);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);

        integerRange = new Range<>(8, null, Integer.class);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.RIGHT);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.RIGHT);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);

        integerRange = new Range<>(null, null, Integer.class);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);

        integerRange = new Range<>(5, 4, Integer.class);
        Assert.assertEquals(integerRange.compareTo(5), Range.ValueComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(0), Range.ValueComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(100), Range.ValueComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo((Integer) null), Range.ValueComparison.ANY_EMPTY);
    }

    @Test
    public void testCompareRanges() {
        Range<Integer> integerRange = new Range<>(0, 10, Integer.class);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, -1, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, 20, Integer.class)), Range.RangeComparison.LEFT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(11, 20, Integer.class)), Range.RangeComparison.LEFT_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 20, Integer.class)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 10, Integer.class)), Range.RangeComparison.EQUALS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 10, Integer.class)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 15, Integer.class)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 15, Integer.class)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 10, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 6, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 5, Integer.class)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -1, Integer.class)), Range.RangeComparison.RIGHT_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -5, Integer.class)), Range.RangeComparison.RIGHT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, -5, Integer.class)), Range.RangeComparison.RIGHT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 5, Integer.class)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 15, Integer.class)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, null, Integer.class)), Range.RangeComparison.LEFT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, null, Integer.class)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, null, Integer.class)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, null, Integer.class)), Range.RangeComparison.INSIDE);

        integerRange = new Range<>(null, 10, Integer.class);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, -1, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, 20, Integer.class)), Range.RangeComparison.LEFT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(11, 20, Integer.class)), Range.RangeComparison.LEFT_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 20, Integer.class)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 10, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 10, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 15, Integer.class)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 15, Integer.class)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 10, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 6, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -1, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, -5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 15, Integer.class)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, null, Integer.class)), Range.RangeComparison.LEFT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, null, Integer.class)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, null, Integer.class)), Range.RangeComparison.LEFT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, null, Integer.class)), Range.RangeComparison.INSIDE);

        integerRange = new Range<>(0, null, Integer.class);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, -1, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, 20, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(11, 20, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 20, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 10, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 10, Integer.class)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 15, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 15, Integer.class)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 10, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 6, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 5, Integer.class)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -1, Integer.class)), Range.RangeComparison.RIGHT_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -5, Integer.class)), Range.RangeComparison.RIGHT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, -5, Integer.class)), Range.RangeComparison.RIGHT_NO_CONTACT);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 5, Integer.class)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 15, Integer.class)), Range.RangeComparison.RIGHT_OVERLAP);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, null, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, null, Integer.class)), Range.RangeComparison.EQUALS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, null, Integer.class)), Range.RangeComparison.INSIDE);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, null, Integer.class)), Range.RangeComparison.INSIDE);

        integerRange = new Range<>(null, null, Integer.class);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, -1, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, 20, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(11, 20, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 20, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 10, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 10, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 15, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 15, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 10, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 6, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -1, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, -5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 5, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 15, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, null, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, null, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, null, Integer.class)), Range.RangeComparison.CONTAINS);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, null, Integer.class)), Range.RangeComparison.EQUALS);

        integerRange = new Range<>(1, 0, Integer.class);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, -1, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, 20, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(11, 20, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 20, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 10, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 10, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 15, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 15, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, 5, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 10, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(5, 6, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, 5, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -1, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, -5, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, -5, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 5, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, 15, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(15, null, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(0, null, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(-5, null, Integer.class)), Range.RangeComparison.ANY_EMPTY);
        Assert.assertEquals(integerRange.compareTo(new Range<>(null, null, Integer.class)), Range.RangeComparison.ANY_EMPTY);
    }


    @Test
    public void testIntersection() {
        Range<Integer> emptyRange = new Range<>(0, -1, Integer.class);
        Range<Integer> integerRange = new Range<>(0, 10, Integer.class);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -5, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -1, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(11, 15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(15, 15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 5, Integer.class)), new Range<>(0, 5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(0, 10, Integer.class)), new Range<>(0, 10, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 20, Integer.class)), new Range<>(0, 10, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(3, 5, Integer.class)), new Range<>(3, 5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(5, 15, Integer.class)), new Range<>(5, 10, Integer.class));

        integerRange = new Range<>(null, 10, Integer.class);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -15, Integer.class)), new Range<>(-10, -15, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -5, Integer.class)), new Range<>(-10, -5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 15, Integer.class)), new Range<>(-10, 10, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(11, 15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(15, 15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 5, Integer.class)), new Range<>(-10, 5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(0, 10, Integer.class)), new Range<>(0, 10, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 20, Integer.class)), new Range<>(-10, 10, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(3, 5, Integer.class)), new Range<>(3, 5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(5, 15, Integer.class)), new Range<>(5, 10, Integer.class));

        integerRange = new Range<>(0, null, Integer.class);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -5, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 15, Integer.class)), new Range<>(0, 15, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(11, 15, Integer.class)), new Range<>(11, 15, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(15, 15, Integer.class)), new Range<>(15, 15, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 5, Integer.class)), new Range<>(0, 5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(0, 10, Integer.class)), new Range<>(0, 10, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 20, Integer.class)), new Range<>(0, 20, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(3, 5, Integer.class)), new Range<>(3, 5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(5, 15, Integer.class)), new Range<>(5, 15, Integer.class));

        integerRange = new Range<>(null, null, Integer.class);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -15, Integer.class)), new Range<>(-10, -15, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -5, Integer.class)), new Range<>(-10, -5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 15, Integer.class)), new Range<>(-10, 15, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(11, 15, Integer.class)), new Range<>(11, 15, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(15, 15, Integer.class)), new Range<>(15, 15, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 5, Integer.class)), new Range<>(-10, 5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(0, 10, Integer.class)), new Range<>(0, 10, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 20, Integer.class)), new Range<>(-10, 20, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(3, 5, Integer.class)), new Range<>(3, 5, Integer.class));
        Assert.assertEquals(integerRange.intersection(new Range<>(5, 15, Integer.class)), new Range<>(5, 15, Integer.class));

        integerRange = new Range<>(1, 0, Integer.class);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, -5, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(11, 15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(15, 15, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 5, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(0, 10, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(-10, 20, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(3, 5, Integer.class)), emptyRange);
        Assert.assertEquals(integerRange.intersection(new Range<>(5, 15, Integer.class)), emptyRange);

        integerRange = new Range<>(0, 10, Integer.class);
        RangeList<Integer> rangeList = new RangeList<>(Integer.class, -10, -9, -5, -5, -2, 0, 1, 3, 6, 6, 7, 8, 10, 14, 16, 18, 20, null);
        Assert.assertEquals(new RangeList<>(Integer.class, 0, 3, 6, 8, 10, 10), integerRange.intersection(rangeList));
    }

    @Test
    public void testUnion() {
        Range<Integer> integerRange = new Range<>(0, 10, Integer.class);

        Assert.assertEquals(new RangeList<>(Integer.class, -10, -5, 0, 10), integerRange.union(new Range<>(-10, -5, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, -10, 10), integerRange.union(new Range<>(-10, -1, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, -10, 10), integerRange.union(new Range<>(-10, 5, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, -10, 100), integerRange.union(new Range<>(-10, 100, Integer.class)));

        Assert.assertEquals(new RangeList<>(Integer.class, 0, 10), integerRange.union(new Range<>(5, 6, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 0, 16), integerRange.union(new Range<>(5, 16, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 0, 16), integerRange.union(new Range<>(11, 16, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 0, 10, 12, 16), integerRange.union(new Range<>(12, 16, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 0, 10), integerRange.union(new Range<>(12, 11, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, null, 10), integerRange.union(new Range<>(null, 5, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 0, null), integerRange.union(new Range<>(6, null, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, null, null), integerRange.union(new Range<>(null, null, Integer.class)));

        Assert.assertEquals(new RangeList<>(Integer.class, -50, -40, -35, -35, -5, 10, 12, 13, 50, 100), integerRange.union(new RangeList<>(Integer.class, -50, -40, -35, -35, -5, -1, 3, 6, 12, 13, 50, 100)));
    }

    @Test
    public void testSubtract() {
        Range<Integer> integerRange = new Range<>(0, 10, Integer.class);
        Assert.assertEquals(new RangeList<>(integerRange), integerRange.subtract(new Range<>(-10, -15, Integer.class)));
        Assert.assertEquals(new RangeList<>(integerRange), integerRange.subtract(new Range<>(-10, -5, Integer.class)));
        Assert.assertEquals(new RangeList<>(integerRange), integerRange.subtract(new Range<>(-10, -1, Integer.class)));
        Assert.assertEquals(new RangeList<>(integerRange), integerRange.subtract(new Range<>(11, 15, Integer.class)));
        Assert.assertEquals(new RangeList<>(integerRange), integerRange.subtract(new Range<>(15, 15, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 6, 10), integerRange.subtract(new Range<>(-10, 5, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 0, -1), integerRange.subtract(new Range<>(0, 10, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 0, -1), integerRange.subtract(new Range<>(-10, 20, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 0, 2, 6, 10), integerRange.subtract(new Range<>(3, 5, Integer.class)));
        Assert.assertEquals(new RangeList<>(Integer.class, 0, 4), integerRange.subtract(new Range<>(5, 15, Integer.class)));

        Assert.assertEquals(new RangeList<>(Integer.class, 3, 5, 7, 7, 10, 10), integerRange.subtract(new RangeList<>(Integer.class, -10, -5, -2, 2, 6, 6, 8, 9, 11, 50)));
    }

    @Test
    public void testGetPosition() {
        Range<Integer> integerRange = new Range<>(0, 10, Integer.class);
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

        integerRange = new Range<>(0, -1, Integer.class);
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

        integerRange = new Range<>(null, 10, Integer.class);
        Assert.assertEquals(null, integerRange.getPosition(5L));
        Assert.assertEquals(null, integerRange.getPosition(15L));
        try {
            integerRange.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }

        integerRange = new Range<>(0, null, Integer.class);
        Assert.assertEquals((Integer) 5, integerRange.getPosition(5L));
        Assert.assertEquals((Integer) 15, integerRange.getPosition(15L));
        try {
            integerRange.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }

        integerRange = new Range<>(null, null, Integer.class);
        Assert.assertEquals(null, integerRange.getPosition(5L));
        Assert.assertEquals(null, integerRange.getPosition(15L));
        try {
            integerRange.getPosition(-5L);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }
}
