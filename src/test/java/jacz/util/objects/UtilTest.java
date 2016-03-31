package jacz.util.objects;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Alberto on 31/03/2016.
 */
public class UtilTest {

    @Test
    public void test() {

        Assert.assertTrue(Util.compare(null, null));
        Assert.assertFalse(Util.compare(null, 5));
        Assert.assertFalse(Util.compare("five", null));
        Assert.assertFalse(Util.compare("five", 5));
        Assert.assertTrue(Util.compare("five", "five"));
    }
}