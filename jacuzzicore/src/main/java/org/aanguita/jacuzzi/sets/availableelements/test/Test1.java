package org.aanguita.jacuzzi.sets.availableelements.test;

import org.aanguita.jacuzzi.sets.availableelements.AvailableElementsByte;
import org.aanguita.jacuzzi.sets.availableelements.AvailableElementsShort;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 4/05/12
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public class Test1 {

    public static void main(String args[]) {

        AvailableElementsByte a = new AvailableElementsByte((byte) 0, (byte) 1);

        Byte b;
        for (int i = 0; i < 275; i++) {
            b = a.requestElement();
            System.out.println(b);
            a.freeElement((byte) i);
        }

        long l = Integer.MAX_VALUE;


        AvailableElementsShort s = new AvailableElementsShort((short) 0);

        Short sh = s.requestElement();

        s.freeElement(sh);

        sh = s.requestElement();


    }
}
