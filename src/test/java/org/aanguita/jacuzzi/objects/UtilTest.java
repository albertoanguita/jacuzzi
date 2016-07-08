package org.aanguita.jacuzzi.objects;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Alberto on 31/03/2016.
 */
public class UtilTest {

    @Test
    public void test() {

        Assert.assertTrue(Util.equals(null, null));
        Assert.assertFalse(Util.equals(null, 5));
        Assert.assertFalse(Util.equals("five", null));
        Assert.assertFalse(Util.equals("five", 5));
        Assert.assertTrue(Util.equals("five", "five"));
    }
}