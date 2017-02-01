package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.event.hub.AbstractEventHubTest;
import org.aanguita.jacuzzi.event.hub.EventHubFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Alberto on 01/02/2017.
 */
public class StateHooksTest {

    private static final long CYCLE = 2000;

    public enum State {
        A,
        B,
        C,
        D,
        E
    }

    private static class Mocks {

        private final Runnable enter = mock(Runnable.class);
        private final Runnable periodic = mock(Runnable.class);
        private final Runnable exit = mock(Runnable.class);

        private final State state;

        public Mocks(State state) {
            this.state = state;
            doAnswers(state);
        }

        private void doAnswers(State state) {
            Mockito.doAnswer(invocationOnMock -> {
                System.out.println("Enter " + state);
                return null;
            }).when(enter).run();
            Mockito.doAnswer(invocationOnMock -> {
                System.out.println("Periodic " + state);
                return null;
            }).when(periodic).run();
            Mockito.doAnswer(invocationOnMock -> {
                System.out.println("Exit " + state);
                return null;
            }).when(exit).run();
        }

        public void verifyNone() {
            verify(enter, never()).run();
            verify(periodic, never()).run();
            verify(exit, never()).run();
        }

        public void verifyEnter() {
            verify(enter).run();
            reset(enter);
            verify(periodic, never()).run();
            verify(exit, never()).run();
            doAnswers(state);
        }

        public void verifyPeriodic() {
            verify(enter, never()).run();
            verify(periodic).run();
            reset(periodic);
            verify(exit, never()).run();
            doAnswers(state);
        }

        public void verifyExit() {
            verify(enter, never()).run();
            verify(periodic, never()).run();
            verify(exit).run();
            reset(exit);
            doAnswers(state);
        }
    }

    private Map<State, Mocks> mocks;

    private StateHooks<State> sh;

    private int cycleCount;

    @Before
    public void setUp() throws Exception {
        sh = new StateHooks<>(State.A);
        mocks = new HashMap<>();
        for (State state : State.values()) {
            mocks.put(state, new Mocks(state));
            sh.addEnterStateHook(state, mocks.get(state).enter);
            sh.setPeriodicStateHook(state, mocks.get(state).periodic, CYCLE);
            sh.addExitStateHook(state, mocks.get(state).exit);
        }
        cycleCount = 0;
    }

    @Test
    public void test() {
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyNone();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        sh.setState(State.B);
        ThreadUtil.safeSleep(CYCLE / 2);
        System.out.println("Start cycles");

        mocks.get(State.A).verifyExit();
        mocks.get(State.B).verifyEnter();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        waitCycle();

        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyPeriodic();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        waitCycle();

        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyPeriodic();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        sh.setState(State.C);
        waitCycle();

        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyExit();
        mocks.get(State.C).verifyEnter();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();
    }

    private void waitCycle() {
        ThreadUtil.safeSleep(CYCLE);
        System.out.println("Enter cycle " + ++cycleCount);
    }
}