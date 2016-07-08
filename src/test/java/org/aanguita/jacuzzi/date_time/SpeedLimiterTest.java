package org.aanguita.jacuzzi.date_time;

import org.aanguita.jacuzzi.concurrency.timer.Timer;
import org.junit.Test;

/**
 * Created by Alberto on 07/05/2016.
 */
public class SpeedLimiterTest {

    @Test
    public void test() {

        SpeedLimiter speedLimiter = new SpeedLimiter(4000, 8d);

        Timer timer = new Timer(1000, timer1 -> {
            System.out.println("TIME!");
            return null;
        });

        for (int i = 0; i < 130; i++) {
            speedLimiter.addProgress(1);
            System.out.println("+");
        }

        timer.stop();
        speedLimiter.stop();
    }

}