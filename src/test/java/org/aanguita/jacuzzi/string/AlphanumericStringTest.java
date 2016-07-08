package org.aanguita.jacuzzi.string;


import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alberto on 17/03/2016.
 */
public class AlphanumericStringTest {

    private String s;

    @Test
    public void test() {

        s = "";
        List<AlphanumericString.CharType> charTypes = new ArrayList<>();
        charTypes.add(AlphanumericString.CharType.UPPERCASE);
        charTypes.add(AlphanumericString.CharType.NUMERIC);
        charTypes.add(AlphanumericString.CharType.LOWERCASE);

        Assert.assertEquals("A", next(charTypes, 1));
        Assert.assertEquals("F", next(charTypes, 5));
        Assert.assertEquals("Z", next(charTypes, 20));
        Assert.assertEquals("0", next(charTypes, 1));
        Assert.assertEquals("9", next(charTypes, 9));
        Assert.assertEquals("a", next(charTypes, 1));
        Assert.assertEquals("z", next(charTypes, 25));
        Assert.assertEquals("AA", next(charTypes, 1));
        Assert.assertEquals("AB", next(charTypes, 1));
        Assert.assertEquals("A1", next(charTypes, 26));
        Assert.assertEquals("B1", next(charTypes, 62));

    }

    private String next(List<AlphanumericString.CharType> charTypes, int times) {
        for (int i = 0; i < times; i++) {
            s = AlphanumericString.nextAlphanumericString(s, charTypes);
            System.out.println(s);
        }
        return s;
    }
}