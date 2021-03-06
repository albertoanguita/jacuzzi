package org.aanguita.jacuzzi.fsm.test;

import org.aanguita.jacuzzi.fsm.TimedFSM;
import org.aanguita.jacuzzi.io.IOUtil;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 29-abr-2010<br>
 * Last Modified: 29-abr-2010
 */
public class TestTimed {

    public static void main(String args[]) {

        TimedFSM<String, Integer> timedFSM = new TimedFSM<>(new TimedTestAction(), 5000);

        IOUtil.pauseEnter();
        boolean active = true;
        int i = 1;
        while (active) {
            active = timedFSM.newInput(i);
            i += 2;
            System.out.println("active: " + active);
        }

        System.out.println("Estado final: " + timedFSM.getCurrentState());
    }
}
