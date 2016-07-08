package org.aanguita.jacuzzi.AI.evolve;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Test;

/**
 * Created by Alberto on 18/04/2016.
 */
public class StateTimersTest {

    private static final long WAIT = 1000;
    private static final long WAIT2 = 1500;

    @Test
    public void test() {

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
    }

}