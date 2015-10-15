package jacz.util.date_time.test;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.date_time.SpeedRegistry;

/**
 * Created by Alberto on 15/10/2015.
 */
public class TestSpeedRegistry {

    public static void main(String[] args) {

        SpeedRegistry speedRegistry = new SpeedRegistry(1000, 1000, 10000);

        ThreadUtil.safeSleep(500);
        speedRegistry.addProgress(20);
        ThreadUtil.safeSleep(500);
        speedRegistry.addProgress(30);
        ThreadUtil.safeSleep(500);
        speedRegistry.addProgress(40);
        ThreadUtil.safeSleep(500);
        speedRegistry.addProgress(50);
        ThreadUtil.safeSleep(500);
        speedRegistry.addProgress(60);
        ThreadUtil.safeSleep(500);
        speedRegistry.stop();

        System.out.println("END");
    }
}
