package org.aanguita.jacuzzi.maps;

import org.aanguita.jacuzzi.lists.tuple.Triple;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by Alberto on 31/03/2016.
 */
public class DoubleKeyMapTest {

    @Test
    public void test() {

        DoubleKeyMap<String, Integer, String> map = new DoubleKeyMap<>();
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());

        map.put("a", 1, "aaa");
        map.put("b", 2, "bbb");
        map.put("c", 3, "ccc");

        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(3, map.size());
        Assert.assertTrue(map.containsKey("a"));
        Assert.assertTrue(map.containsKey("b"));
        Assert.assertTrue(map.containsKey("c"));
        Assert.assertFalse(map.containsKey("d"));
        Assert.assertTrue(map.containsSecondaryKey(1));
        Assert.assertTrue(map.containsSecondaryKey(2));
        Assert.assertTrue(map.containsSecondaryKey(3));
        Assert.assertFalse(map.containsSecondaryKey(4));

        Assert.assertEquals("aaa", map.get("a"));
        Assert.assertEquals(new Integer(1), map.getSecondaryKey("a"));

        Assert.assertEquals("aaa", map.getSecondary(1));
        Assert.assertEquals("a", map.getMainKey(1));

        Set<String> mainKeys = new HashSet<>();
        mainKeys.add("a");
        mainKeys.add("b");
        mainKeys.add("c");
        Set<Integer> secondaryKeys = new HashSet<>();
        secondaryKeys.add(1);
        secondaryKeys.add(2);
        secondaryKeys.add(3);
        ArrayList<String> values = new ArrayList<>();
        values.add("aaa");
        values.add("bbb");
        values.add("ccc");
        Collection<Triple<String, Integer, String>> entries = new ArrayList<>();
        entries.add(new Triple<>("a", 1, "aaa"));
        entries.add(new Triple<>("b", 2, "bbb"));
        entries.add(new Triple<>("c", 3, "ccc"));
        Assert.assertEquals(mainKeys, map.keySet());
        Assert.assertEquals(secondaryKeys, map.secondaryKeySet());
        Assert.assertEquals(values, map.values());
        Assert.assertTrue(CollectionUtils.isEqualCollection(entries, map.entrySet()));

        map.remove("a");
        map.removeSecondary(2);
        Assert.assertEquals(1, map.size());
        Assert.assertFalse(map.containsKey("a"));
        Assert.assertFalse(map.containsKey("b"));
        Assert.assertTrue(map.containsKey("c"));
        Assert.assertFalse(map.containsKey("d"));
        Assert.assertFalse(map.containsSecondaryKey(1));
        Assert.assertFalse(map.containsSecondaryKey(2));
        Assert.assertTrue(map.containsSecondaryKey(3));
        Assert.assertFalse(map.containsSecondaryKey(4));
    }
}