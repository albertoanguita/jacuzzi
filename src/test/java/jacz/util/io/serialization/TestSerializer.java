package jacz.util.io.serialization;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializer unit tests
 */
public class TestSerializer {

    public enum TestEnum {
        A,
        B,
        C,
        D,
        E
    }


    @Test
    public void testBasic() throws Exception {

        Integer ii = 5;
        String ss = Serializer.serializeObjectToString(ii);
        Assert.assertEquals(5, (int) Serializer.deserializeObject(ss));


        byte[] shortValueData = Serializer.serialize((short) -1);
        Short nullShort = null;
        @SuppressWarnings("ConstantConditions") byte[] shortNullData = Serializer.serialize(nullShort);
        byte[] shortNotNullData = Serializer.serialize(new Short((short) 56));
        byte[] shortData = new FragmentedByteArray(shortValueData, shortNullData, shortNotNullData).generateArray();

        MutableOffset shortMutableOffset = new MutableOffset();
        Assert.assertEquals((short) -1, Serializer.deserializeShortValue(shortData, shortMutableOffset));
        Assert.assertEquals(null, Serializer.deserializeShort(shortData, shortMutableOffset));
        Assert.assertEquals(new Short((short) 56), Serializer.deserializeShort(shortData, shortMutableOffset));


        byte[] dataF = Serializer.serialize(1234.5f);
        byte[] dataD = Serializer.serialize(new Double(98765.4d));
        byte[] dataS = Serializer.serialize("qw?", "UTF-8");
        byte[] dataI = Serializer.serialize(12345);
        byte[] dataII = Serializer.serialize(-3);
        byte[] dataSh = Serializer.serialize((short) 25);
        byte[] dataL = Serializer.serialize(new Long(5L));
        byte[] dataLL = Serializer.serialize(new Long(-1L));
        byte[] dataMinB = Serializer.serialize(Byte.MIN_VALUE);
        Byte nullByte = null;
        byte[] dataNullB = Serializer.serialize(nullByte);
        byte[] eData = Serializer.serialize(TestEnum.C);

        byte[] data = Serializer.addArrays(dataF, dataD, dataS, dataI, dataII, dataSh, dataL, dataLL, dataMinB, dataNullB, eData);

        MutableOffset off = new MutableOffset();

        Assert.assertEquals(1234.5f, Serializer.deserializeFloatValue(data, off), 0);
        Assert.assertEquals(new Double(98765.4d), Serializer.deserializeDouble(data, off));
        Assert.assertEquals("qw?", Serializer.deserializeString(data, off));
        Assert.assertEquals(12345, Serializer.deserializeIntValue(data, off));
        Assert.assertEquals(-3, Serializer.deserializeIntValue(data, off));
        Assert.assertEquals((short) 25, Serializer.deserializeShortValue(data, off));
        Assert.assertEquals(new Long(5L), Serializer.deserializeLong(data, off));
        Assert.assertEquals(new Long(-1L), Serializer.deserializeLong(data, off));
        Assert.assertEquals(Byte.MIN_VALUE, Serializer.deserializeByteValue(data, off));
        Assert.assertEquals(null, Serializer.deserializeByte(data, off));
        Assert.assertEquals(TestEnum.C, Serializer.deserializeEnum(TestEnum.class, data, off));


        List<String> list = new ArrayList<>();
        list.add("al/b");
        list.add(null);
        list.add("j//e/je");
        String serString = Serializer.serializeListToReadableString(list);
        String serString2 = Serializer.serializeListToReadableString("al/b", null, "j//e/je");
        System.out.println(serString);
        Assert.assertEquals(list, Serializer.deserializeListFromReadableString(serString));
        Assert.assertEquals(list, Serializer.deserializeListFromReadableString(serString2));
    }
}
