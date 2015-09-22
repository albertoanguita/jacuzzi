package jacz.util.numeric.newrange;

import org.junit.Assert;
import org.junit.Test;

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

}
