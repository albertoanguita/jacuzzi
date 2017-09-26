package org.aanguita.jacuzzi.concurrency;

import org.junit.Test;

/**
 * @author aanguita
 *         26/09/2017
 */
public class BlockingStateTest {

    public enum State {
        A,
        B,
        C,
        D
    }

    private static final long CYCLE = 1000L;

    @Test
    public void test() {
        BlockingState<State> blockingState = new BlockingState<>(State.A, "blockingStateTest");

        setThread("1", blockingState, State.A);
        setThread("1'", blockingState, State.A, 1);

        setThread("2", blockingState, State.B);
        setThread("3", blockingState, State.B, 3);

        sleep(1);
        set(blockingState, State.B);

        setThread("4", blockingState, State.C);
        setThread("5", blockingState, State.C, 1);
        setThread("6", blockingState, State.C, 1);
        setThread("7", blockingState, State.C, 2);
        setThread("8", blockingState, State.C, 2);
        setThread("9", blockingState, State.C, 4);
        setThread("10", blockingState, State.C, 4);
        setThread("11", blockingState, State.D);

        sleep(3);
        set(blockingState, State.C);

        sleep(5);
        set(blockingState, State.D);

        sleep(1);

        System.out.println("END");
    }

    private void setThread(String name, BlockingState<State> blockingState, State expectedState) {
        setThread(name, blockingState, expectedState, null);
    }

    private void setThread(String name, BlockingState<State> blockingState, State expectedState, Integer cycles) {
        ThreadExecutor.submitUnregistered(() -> {
            if (cycles == null) {
                log(name, expectedState, "setting blocking thread with no timeout");
                try {
                    blockingState.blockUntil(expectedState);
                    log(name, expectedState, "expected value reached!");
                } catch (InterruptedException e) {
                    log(name, expectedState, "blocking thread unexpectedly interrupted!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
            } else {
                log(name, expectedState, "setting blocking thread with timeout = " + cycles);
                try {
                    blockingState.blockUntil(expectedState, cycles * CYCLE);
                    log(name, expectedState, "expected value reached!");
                } catch (InterruptedException e) {
                    log(name, expectedState, "blocking thread interrupted after timeout passed");
                }
            }
        });
    }

    private void log(String name, State expectedState, String message) {
        System.out.println(System.currentTimeMillis() + ": " + name + "(" + expectedState + "): " + message);
    }

    private static void sleep(int i) {
        ThreadUtil.safeSleep(i * CYCLE);
    }

    private void set(BlockingState<State> blockingState, State newValue) {
        System.out.println(System.currentTimeMillis() + ": " + "Setting new value for blocking state: " + newValue);
        blockingState.set(newValue);
    }
}