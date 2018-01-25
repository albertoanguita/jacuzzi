package org.aanguita.jacuzzi.concurrency;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author aanguita
 * 25/01/2018
 */
public class PeriodicTaskReminderTest {

    @Test
    public void test() {
        PeriodicTaskReminder.getInstance("name").addPeriodicTask("task1", new Runnable() {
            @Override
            public void run() {
                System.out.println("Running 1...");
                ThreadUtil.safeSleep(5000);
                System.out.println("Finished running 1");
            }
        }, false, 1000, false);
        PeriodicTaskReminder.getInstance("name").addPeriodicTask("task2", new Runnable() {
            @Override
            public void run() {
                System.out.println("Running 2...");
                ThreadUtil.safeSleep(3000);
                System.out.println("Finished running 2");
            }
        }, false, 2000, false);


        ThreadUtil.safeSleep(20000);
    }

}