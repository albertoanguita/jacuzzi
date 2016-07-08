package aanguita.jacuzzi.AI.evolve.test;

import aanguita.jacuzzi.AI.evolve.StateTimers;
import aanguita.jacuzzi.concurrency.ThreadUtil;

/**
 * Created by Alberto on 06/06/2016.
 */
public class Timers {

    private static final long WAIT = 1000;
    private static final long WAIT2 = 1500;

    public static void main(String[] args) {

        StateTimers<Integer> stateTimers = new StateTimers<>(1);

        stateTimers.setStateTimer(1, WAIT, () -> System.out.println("state 1"));
        stateTimers.setStateTimer(2, WAIT / 2, () -> System.out.println("state 2"));

        ThreadUtil.safeSleep(WAIT2);
        stateTimers.setState(2);

        ThreadUtil.safeSleep(WAIT2);
        stateTimers.setState(1);

        ThreadUtil.safeSleep(WAIT2);
        stateTimers.setState(2);

        ThreadUtil.safeSleep(WAIT2);
        stateTimers.setState(1);

        ThreadUtil.safeSleep(WAIT2);

        stateTimers.stop();
    }
}
