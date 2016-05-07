package jacz.util.concurrency.timer;

import jacz.util.concurrency.ThreadUtil;
import org.junit.Test;

/**
 * Created by Alberto on 27/04/2016.
 */
public class TimerTest implements TimerAction {

    private long waitTime = 2000L;

    @Test
    public void test() {

        Timer timer = new Timer(waitTime, this);

        ThreadUtil.safeSleep(12000L);
        timer.kill();
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