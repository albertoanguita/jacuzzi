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
        PeriodicTaskReminder.getInstance("name").addPeriodicTask("task", new Runnable() {
            @Override
            public void run() {
                System.out.println("Running...");
                ThreadUtil.safeSleep(5000);
                System.out.println("Finished running");
            }
        }, false, 1000, false);


        ThreadUtil.safeSleep(20000);
    }

}