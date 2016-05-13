package jacz.util.io.serialization.localstorage;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Alberto on 08/04/2016.
 */
public class LocalStorageTest {

    private enum Letter {
        A,
        B,
        C
    }

    @Test
    public void test() throws IOException {

        List<String> stringList = new ArrayList<>();
        stringList.add("hello1");
        stringList.add("hello2");
        stringList.add("hello3");
        List<Letter> enumList = new ArrayList<>();
        enumList.add(Letter.C);
        enumList.add(Letter.B);
        enumList.add(Letter.A);
        List<Boolean> booleanList = new ArrayList<>();
        booleanList.add(false);
        booleanList.add(true);
        booleanList.add(false);
        List<Byte> byteList = new ArrayList<>();
        byteList.add((byte) 9);
        byteList.add((byte) 5);
        byteList.add((byte) -1);
        List<Short> shortList = new ArrayList<>();
        shortList.add((short) 19);
        shortList.add((short) 55);
        shortList.add((short) -21);
        List<Integer> integerList = new ArrayList<>();
        integerList.add(12);
        integerList.add(-28);
        integerList.add(38);
        List<Long> longList = new ArrayList<>();
        longList.add(91L);
        longList.add(3L);
        longList.add(-1153L);
        List<Float> floatList = new ArrayList<>();
        floatList.add(0.5f);
        floatList.add(-0.33f);
        floatList.add(11.45f);
        List<Double> doubleList = new ArrayList<>();
        doubleList.add(7d);
        doubleList.add(81.1d);
        doubleList.add(-667.4d);
        Date date1 = new Date();
        Date date2 = new Date(1460236684078L);
        Date date3 = new Date(1460236672078L);
        List<Date> dateList = new ArrayList<>();
        dateList.add(date1);
        dateList.add(date2);
        dateList.add(date3);

        LocalStorage ls = LocalStorage.createNew("localStorage.db");

        Assert.assertEquals(LocalStorage.CURRENT_VERSION, ls.getLocalStorageVersion());
        Assert.assertTrue((new Date().getTime() - ls.getCreationDate().getTime()) < 250);


        ls.setString("string", "hello");
        ls.setString("stringNull", null);
        ls.setEnum("enum", Letter.class, Letter.A);
        ls.setBoolean("bool", true);
        ls.setByte("byte", (byte) 3);
        ls.setShort("short", (short) 7);
        ls.setInteger("int", 5);
        ls.setLong("long", 25L);
        ls.setFloat("float", 0.32f);
        ls.setDouble("double", 5.23d);
        ls.setDate("date", date1);
        ls.setStringList("stringList", stringList);
        ls.setEnumList("enumList", Letter.class, enumList);
        ls.setBooleanList("booleanList", booleanList);
        ls.setByteList("byteList", byteList);
        ls.setShortList("shortList", shortList);
        ls.setIntegerList("integerList", integerList);
        ls.setLongList("longList", longList);
        ls.setFloatList("floatList", floatList);
        ls.setDoubleList("doubleList", doubleList);
        ls.setDateList("dateList", dateList);


        Assert.assertEquals(21, ls.itemCount());
        List<String> keys = Arrays.asList("string", "stringNull", "enum", "bool", "byte", "short", "int", "long", "float", "double", "date", "stringList", "enumList", "booleanList", "byteList", "shortList", "integerList", "longList", "floatList", "doubleList", "dateList");
        Assert.assertEquals(keys, ls.keys());
        Assert.assertTrue(ls.containsItem("string"));
        Assert.assertTrue(ls.containsItem("stringNull"));
        Assert.assertTrue(ls.containsItem("enum"));
        Assert.assertTrue(ls.containsItem("bool"));
        Assert.assertTrue(ls.containsItem("byte"));
        Assert.assertTrue(ls.containsItem("short"));
        Assert.assertTrue(ls.containsItem("int"));
        Assert.assertTrue(ls.containsItem("long"));
        Assert.assertTrue(ls.containsItem("float"));
        Assert.assertTrue(ls.containsItem("double"));
        Assert.assertTrue(ls.containsItem("date"));
        Assert.assertTrue(ls.containsItem("stringList"));
        Assert.assertTrue(ls.containsItem("enumList"));
        Assert.assertTrue(ls.containsItem("booleanList"));
        Assert.assertTrue(ls.containsItem("byteList"));
        Assert.assertTrue(ls.containsItem("shortList"));
        Assert.assertTrue(ls.containsItem("integerList"));
        Assert.assertTrue(ls.containsItem("longList"));
        Assert.assertTrue(ls.containsItem("floatList"));
        Assert.assertTrue(ls.containsItem("doubleList"));
        Assert.assertFalse(ls.containsItem("abcd"));

        Assert.assertEquals("hello", ls.getString("string"));
        Assert.assertEquals(null, ls.getString("stringNull"));
        Assert.assertEquals(Letter.A, ls.getEnum("enum", Letter.class));
        Assert.assertEquals(true, ls.getBoolean("bool"));
        Assert.assertEquals(new Byte((byte) 3), ls.getByte("byte"));
        Assert.assertEquals(new Short((short) 7), ls.getShort("short"));
        Assert.assertEquals(new Integer(5), ls.getInteger("int"));
        Assert.assertEquals(new Long(25), ls.getLong("long"));
        Assert.assertEquals(new Float(0.32), ls.getFloat("float"));
        Assert.assertEquals(new Double(5.23d), ls.getDouble("double"));
        Assert.assertEquals(date1, ls.getDate("date"));
        Assert.assertEquals(stringList, ls.getStringList("stringList"));
        Assert.assertEquals(enumList, ls.getEnumList("enumList", Letter.class));
        Assert.assertEquals(booleanList, ls.getBooleanList("booleanList"));
        Assert.assertEquals(byteList, ls.getByteList("byteList"));
        Assert.assertEquals(shortList, ls.getShortList("shortList"));
        Assert.assertEquals(integerList, ls.getIntegerList("integerList"));
        Assert.assertEquals(longList, ls.getLongList("longList"));
        Assert.assertEquals(floatList, ls.getFloatList("floatList"));
        Assert.assertEquals(doubleList, ls.getDoubleList("doubleList"));
        Assert.assertEquals(dateList, ls.getDateList("dateList"));

        ls.removeItem("stringNull");
        Assert.assertEquals(20, ls.itemCount());
        keys = Arrays.asList("string", "enum", "bool", "byte", "short", "int", "long", "float", "double", "date", "stringList", "enumList", "booleanList", "byteList", "shortList", "integerList", "longList", "floatList", "doubleList", "dateList");
        Assert.assertEquals(keys, ls.keys());
        Assert.assertFalse(ls.containsItem("stringNull"));
    }
}