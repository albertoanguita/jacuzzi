package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by Alberto on 01/02/2017.
 */
public class StateHooksTest {

    private static final long CYCLE = 500;

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

        Mocks(State state) {
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

        void verifyNone() {
            verify(enter, never()).run();
            verify(periodic, never()).run();
            verify(exit, never()).run();
        }

        void verifyEnter() {
            verify(enter).run();
            reset(enter);
            verify(periodic, never()).run();
            verify(exit, never()).run();
            doAnswers(state);
        }

        void verifyPeriodic() {
            verify(enter, never()).run();
            verify(periodic).run();
            reset(periodic);
            verify(exit, never()).run();
            doAnswers(state);
        }

        void verifyExit() {
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

        System.out.println("Verifying...");
        mocks.get(State.A).verifyExit();
        mocks.get(State.B).verifyEnter();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        waitCycle();

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyPeriodic();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        waitCycle();

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyPeriodic();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        waitHalfCycle(sh, State.C);

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyExit();
        mocks.get(State.C).verifyEnter();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        waitCycle();

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyNone();
        mocks.get(State.C).verifyPeriodic();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        waitHalfCycle(sh, State.B);

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyEnter();
        mocks.get(State.C).verifyExit();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        waitCycle();

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyPeriodic();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

        waitHalfCycle(sh, State.E);

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyExit();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyEnter();

        waitCycle();

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyNone();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyPeriodic();

        waitCycle();

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyNone();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyPeriodic();

        waitCycle();

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyNone();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyPeriodic();

        waitHalfCycle(sh, State.D);

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyNone();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyEnter();
        mocks.get(State.E).verifyExit();

        waitCycle();

        System.out.println("Verifying...");
        mocks.get(State.A).verifyNone();
        mocks.get(State.B).verifyNone();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyPeriodic();
        mocks.get(State.E).verifyNone();

        waitHalfCycle(sh, State.A);

        System.out.println("Verifying...");
        mocks.get(State.A).verifyEnter();
        mocks.get(State.B).verifyNone();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyExit();
        mocks.get(State.E).verifyNone();

        waitCycle();

        System.out.println("Verifying...");
        mocks.get(State.A).verifyPeriodic();
        mocks.get(State.B).verifyNone();
        mocks.get(State.C).verifyNone();
        mocks.get(State.D).verifyNone();
        mocks.get(State.E).verifyNone();

    }

    private void waitCycle() {
        ThreadUtil.safeSleep(CYCLE);
        System.out.println("Enter cycle " + ++cycleCount);
    }

    private void waitHalfCycle(StateHooks<State> sh, State newState) {
        sh.setState(newState);
        ThreadUtil.safeSleep(CYCLE / 2);
        System.out.println("Enter cycle " + ++cycleCount + " entering state " + newState);
    }
}