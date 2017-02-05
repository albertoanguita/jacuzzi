package org.aanguita.jacuzzi.fsm.test;

import org.aanguita.jacuzzi.fsm.FSM;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-mar-2010<br>
 * Last Modified: 09-mar-2010
 */
public class Test {

    public static void main(String args[]) {


        FSM<String, Integer> g1 = new FSM<>(new TestAction());
        FSM<String, Integer> g2 = g1;
        System.out.println(g1.equals(g2));


        FSM<String, Integer> FSM = new FSM<>(new TestAction());

        boolean active = true;
        int i = 1;
        while (active) {
            active = FSM.newInput(i);
            i += 2;
            System.out.println("active: " + active);
        }

        System.out.println("Estado final: " + FSM.getCurrentState());
    }
}
