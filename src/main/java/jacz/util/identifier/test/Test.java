package jacz.util.identifier.test;

import jacz.util.identifier.UniqueIdentifier;
import jacz.util.identifier.UniqueIdentifierFactory;
import jacz.util.io.serialization.MutableOffset;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 23-mar-2010
 * Time: 18:32:12
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String args[]) throws Exception {

        Map<UniqueIdentifier, String> m = new HashMap<UniqueIdentifier, String>();
        UniqueIdentifierFactory f = new UniqueIdentifierFactory();

        m.put(f.getOneIdentifier(), "hola1");
        m.put(f.getOneIdentifier(), "hola2");

        byte[] fser = f.serialize();

        UniqueIdentifierFactory f2 = new UniqueIdentifierFactory(fser, new MutableOffset());

        m.put(f2.getOneIdentifier(), "hola3");

        System.out.println(m);
    }
}
