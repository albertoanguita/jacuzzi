package jacz.util.io.object_serialization.test;

import jacz.util.io.object_serialization.MutableOffset;
import jacz.util.io.object_serialization.Serializer;

import java.io.IOException;
import java.text.ParseException;
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

        Integer ii = 5;
        String ss = Serializer.serializeObjectToString(ii);
        Integer iii = (Integer) Serializer.deserializeObject(ss);

        byte[] dataF = Serializer.serialize(1234.5f);
        byte ttt = dataF[444];
        byte[] dataD = Serializer.serialize(98765.4d);
        byte[] dataS = Serializer.serialize("qw?");
        byte[] dataI = Serializer.serialize(12345);
        byte[] dataSh = Serializer.serialize((short) 25);
        byte[] dataL = Serializer.serialize(7654321L);
        byte[] dataMinB = Serializer.serialize(Byte.MIN_VALUE);
        Byte nullByte = null;
        byte[] dataNullB = Serializer.serialize(nullByte);
        byte[] eData = Serializer.serialize(TestEnum.C);

        byte[] data = Serializer.addArrays(dataF, dataD, dataS, dataI, dataSh, dataL, dataMinB, dataNullB, eData);

        MutableOffset off = new MutableOffset();
        
        Float f = Serializer.deserializeFloat(data, off);
        Double d = Serializer.deserializeDouble(data, off);
        String s = Serializer.deserializeString(data, off);
        Integer i = Serializer.deserializeInt(data, off);
        Short sh = Serializer.deserializeShort(data, off);
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

        System.out.println(list2.get(1));

        System.out.println("FIN");
    }
}
