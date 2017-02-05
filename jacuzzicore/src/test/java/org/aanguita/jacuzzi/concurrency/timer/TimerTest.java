package org.aanguita.jacuzzi.concurrency.timer;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Test;

/**
 * Created by Alberto on 27/04/2016.
 */
public class TimerTest implements TimerAction {

    private long waitTime = 2000L;

    @Test
    public void test() {

        Timer timer = new Timer(waitTime, this);

        ThreadUtil.safeSleep(10000L);
        timer.stop();
    }

    @Test
    public void test2() {

        Timer timer = new Timer(waitTime, this, false, "name");
        Timer timer2 = new Timer(waitTime, this, false, "name2");

        ThreadUtil.safeSleep(500L);
        timer.reset();
        timer2.reset();

        ThreadUtil.safeSleep(16000L);
        timer.stop();
        timer2.stop();
    }

    @Override
    public Long wakeUp(Timer timer) {
        System.out.println("now: " + System.currentTimeMillis());
        waitTime -= 200L;
        if (waitTime < 200L) {
            waitTime = 200L;
        }
        return waitTime;
    }
}