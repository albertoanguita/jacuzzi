package org.aanguita.jacuzzi.AI.evolve;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Test;

/**
 * Created by Alberto on 18/04/2016.
 */
public class StateTimersTest {

    private static final long WAIT = 1000;
    private static final long WAIT2 = 4000;

    @Test
    public void test() {

        StateTimers<Integer> stateTimers = new StateTimers<>(1);

        //stateTimers.setStateTimer(2, WAIT, () -> System.out.println("state 2"));
        stateTimers.setStateTimer(state -> true, WAIT * 3, () -> System.out.println("general delay"));

        ThreadUtil.safeSleep(WAIT2);
        setState(stateTimers, 2);

        ThreadUtil.safeSleep(WAIT2);
        setState(stateTimers, 1);

        ThreadUtil.safeSleep(WAIT2);
        setState(stateTimers, 2);

        ThreadUtil.safeSleep(WAIT2);
        setState(stateTimers, 1);

        ThreadUtil.safeSleep(WAIT2);
    }

    private void setState(StateTimers<Integer> stateTimers, int newState) {
        System.out.println("Setting new state: " + newState);
        stateTimers.setState(newState);
    }
}