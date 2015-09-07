package jacz.util.fsm.test;

import jacz.util.fsm.GenericFSM;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-mar-2010<br>
 * Last Modified: 09-mar-2010
 */
public class Test {

    public static void main(String args[]) {


        GenericFSM<String, Integer> g1 = new GenericFSM<String, Integer>(new TestAction());
        GenericFSM<String, Integer> g2 = g1;
        System.out.println(g1.equals(g2));


        GenericFSM<String, Integer> genericFSM = new GenericFSM<String, Integer>(new TestAction());

        boolean active = true;
        int i = 1;
        while (active) {
            active = genericFSM.newInput(i);
            i += 2;
            System.out.println("active: " + active);
        }

        System.out.println("Estado final: " + genericFSM.getCurrentState());
    }
}
