package org.aanguita.jacuzzi.numeric.range;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alberto on 07/10/2015.
 */
public class TestRangeQueue {

    @Test
    public void testBasic() {
        IntegerRangeQueue rangeQueue = new IntegerRangeQueue();
        rangeQueue.add(new IntegerRange(1, 3));
        rangeQueue.add(new IntegerRange(-1, -2));
        rangeQueue.add(new IntegerRange(4, 6));
        rangeQueue.add(new IntegerRange(-10, -5));

        List<IntegerRange> rangeList = new ArrayList<>();
        rangeList.add(new IntegerRange(1, 6));
        rangeList.add(new IntegerRange(-10, -5));
        Assert.assertEquals(rangeList, rangeQueue.getRanges());
    }

    @Test
    public void testPeek() {
        IntegerRangeQueue rangeQueue = new IntegerRangeQueue();
        rangeQueue.add(new IntegerRange(1, 3));
        rangeQueue.add(new IntegerRange(4, 6));
        rangeQueue.add(new IntegerRange(-10, -5));

        IntegerRange integerRange = rangeQueue.peek(0);
        Assert.assertEquals(new IntegerRange(0, -1), integerRange);
        integerRange = rangeQueue.peek(1);
        Assert.assertEquals(new IntegerRange(1, 1), integerRange);
        integerRange = rangeQueue.peek(2);
        Assert.assertEquals(new IntegerRange(1, 2), integerRange);
        integerRange = rangeQueue.peek(3);
        Assert.assertEquals(new IntegerRange(1, 3), integerRange);
        integerRange = rangeQueue.peek(4);
        Assert.assertEquals(new IntegerRange(1, 4), integerRange);
        integerRange = rangeQueue.peek(5);
        Assert.assertEquals(new IntegerRange(1, 5), integerRange);
        integerRange = rangeQueue.peek(6);
        Assert.assertEquals(new IntegerRange(1, 6), integerRange);
        integerRange = rangeQueue.peek(7);
        Assert.assertEquals(new IntegerRange(1, 6), integerRange);
        integerRange = rangeQueue.peek(8);
        Assert.assertEquals(new IntegerRange(1, 6), integerRange);
    }

    @Test
    public void testRemove() {
        IntegerRangeQueue rangeQueue = new IntegerRangeQueue();
        rangeQueue.add(new IntegerRange(1, 3));
        rangeQueue.add(new IntegerRange(4, 6));
        rangeQueue.add(new IntegerRange(-10, -5));

        IntegerRange integerRange = rangeQueue.remove(0);
        Assert.assertEquals(new IntegerRange(0, -1), integerRange);
        integerRange = rangeQueue.remove(1);
        Assert.assertEquals(new IntegerRange(1, 1), integerRange);
        integerRange = rangeQueue.remove(2);
        Assert.assertEquals(new IntegerRange(2, 3), integerRange);
        integerRange = rangeQueue.remove(2);
        Assert.assertEquals(new IntegerRange(4, 5), integerRange);
        rangeQueue.add(new IntegerRange(11, 20));
        integerRange = rangeQueue.remove(3);
        Assert.assertEquals(new IntegerRange(6, 6), integerRange);
        integerRange = rangeQueue.remove(5);
        Assert.assertEquals(new IntegerRange(-10, -6), integerRange);
        integerRange = rangeQueue.remove(5);
        Assert.assertEquals(new IntegerRange(-5, -5), integerRange);
        integerRange = rangeQueue.remove(5);
        Assert.assertEquals(new IntegerRange(11, 15), integerRange);
        integerRange = rangeQueue.remove(5);
        Assert.assertEquals(new IntegerRange(16, 20), integerRange);
        Assert.assertTrue(rangeQueue.isEmpty());
    }

    @Test
    public void testRemoveNonBlocking() {
        IntegerRangeQueue rangeQueue = new IntegerRangeQueue();
        rangeQueue.add(new IntegerRange(1, 3));
        rangeQueue.add(new IntegerRange(4, 6));
        rangeQueue.add(new IntegerRange(-10, -5));

        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(1, 0)));
        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(1, 1)));
        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(2, 4)));
        Assert.assertFalse(rangeQueue.removeRange(new IntegerRange(6, 6)));
        rangeQueue.add(new IntegerRange(11, 20));
        Assert.assertFalse(rangeQueue.removeRange(new IntegerRange(11, 12)));
        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(5, 5)));
        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(6, 6)));
        Assert.assertFalse(rangeQueue.removeRange(new IntegerRange(6, 6)));
        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(-10, -6)));
        Assert.assertFalse(rangeQueue.removeRange(new IntegerRange(-5, -4)));
        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(-5, -6)));
        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(-5, -5)));
        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(11, 15)));
        Assert.assertTrue(rangeQueue.removeRange(new IntegerRange(16, 20)));
        Assert.assertTrue(rangeQueue.isEmpty());
    }
}
