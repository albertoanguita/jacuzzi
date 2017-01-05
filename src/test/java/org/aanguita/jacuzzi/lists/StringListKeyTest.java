package org.aanguita.jacuzzi.lists;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alberto on 27/12/2016.
 */
public class StringListKeyTest {

    @Test
    public void test() {

        List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("underworld");
        list.add("spy");

        String s1 = StringListKey.toString(list, "///");
        String s2 = StringListKey.toString(list, "");
        String s3 = StringListKey.toString(list, null);

        Assert.assertEquals("3,5,10,3,hello///underworld///spy///", s1);
        Assert.assertEquals("3,5,10,3,hellounderworldspy", s2);
        Assert.assertEquals("3,5,10,3,hello,underworld,spy,", s3);

        Assert.assertEquals(list, StringListKey.parseToList("3,5,10,3,hello///underworld///spy///", "///"));
        Assert.assertEquals(list, StringListKey.parseToList("3,5,10,3,hellounderworldspy", ""));
        Assert.assertEquals(list, StringListKey.parseToList("3,5,10,3,hello,underworld,spy,", null));
    }
}