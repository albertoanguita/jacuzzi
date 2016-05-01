package jacz.util.concurrency.timer;

import jacz.util.concurrency.ThreadUtil;
import org.junit.Test;

/**
 * Created by Alberto on 27/04/2016.
 */
public class TimerTest implements TimerAction {

    @Test
    public void test() {

        Timer timer = new Timer(2000L, this);

        ThreadUtil.safeSleep(8000L);
        timer.kill();
    }

    @Override
    public Long wakeUp(Timer timer) {
        System.out.println("now");
        return null;
    }
}