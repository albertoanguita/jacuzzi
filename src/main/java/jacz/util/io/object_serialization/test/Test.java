package jacz.util.io.object_serialization.test;

import jacz.util.io.object_serialization.FragmentedByteArray;
import jacz.util.io.object_serialization.MutableOffset;
import jacz.util.io.object_serialization.Serializer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 26-feb-2010
 * Time: 15:06:33
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public enum TestEnum {
        A,
        B,
        C,
        D,
        E
    }


    public static void main(String args[]) throws Exception {


        String str = "ññ";
        byte[] strBytes = str.getBytes(Charset.forName("UTF-8"));
//        byte[] strBytes = str.getBytes();

        Integer ii = 5;
        String ss = Serializer.serializeObjectToString(ii);
        Integer iii = (Integer) Serializer.deserializeObject(ss);


        byte[] shortValueData = Serializer.serialize((short) -1);
        Short nullShort = null;
        byte[] shortNullData = Serializer.serialize(nullShort);
        byte[] shortNotNullData = Serializer.serialize(new Short((short) 56));
        byte[] shortData = new FragmentedByteArray(shortValueData, shortNullData, shortNotNullData).generateArray();

        MutableOffset shortMutableOffset = new MutableOffset();
        short shortValue = Serializer.deserializeShortValue(shortData, shortMutableOffset);
        Short shortNull = Serializer.deserializeShort(shortData, shortMutableOffset);
        Short shortNotNull = Serializer.deserializeShort(shortData, shortMutableOffset);


        byte[] dataF = Serializer.serialize(1234.5f);
        byte[] dataD = Serializer.serialize(new Double(98765.4d));
        byte[] dataS = Serializer.serialize("qw?", "UTF-8");
        byte[] dataI = Serializer.serialize(12345);
        byte[] dataSh = Serializer.serialize((short) 25);
        byte[] dataL = Serializer.serialize(new Long(7654321L));
        byte[] dataMinB = Serializer.serialize(Byte.MIN_VALUE);
        Byte nullByte = null;
        byte[] dataNullB = Serializer.serialize(nullByte);
        byte[] eData = Serializer.serialize(TestEnum.C);

        byte[] data = Serializer.addArrays(dataF, dataD, dataS, dataI, dataSh, dataL, dataMinB, dataNullB, eData);

        MutableOffset off = new MutableOffset();
        
        Float f = Serializer.deserializeFloatValue(data, off);
        Double d = Serializer.deserializeDouble(data, off);
        String s = Serializer.deserializeString(data, "UTF-8", off);
        Integer i = Serializer.deserializeIntValue(data, off);
        Short sh = Serializer.deserializeShortValue(data, off);
        Long l = Serializer.deserializeLong(data, off);
        Byte b = Serializer.deserializeByteValue(data, off);
        Byte nullB = Serializer.deserializeByte(data, off);
        Enum<TestEnum> enum_ = Serializer.deserializeEnum(TestEnum.class, data, off);


        List<String> list = new ArrayList<>();
        list.add("alb");
        list.add(null);
        list.add("jeje");
        String serString = Serializer.serializeListToReadableString(list, "<<<", "}}}}");
        System.out.println(serString);
        List<String> list2 = Serializer.deserializeListFromReadableString(serString, "<<<", "}}}}");

        System.out.println(list2.get(2));

        System.out.println("FIN");
    }
}
