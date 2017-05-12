package org.aanguita.jacuzzi.concurrency;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author aanguita
 *         11/05/2017
 */
public class TimeAlertTest {

    private static final String TIME_ALERT = "time";
    private static final String ALERT_1 = "alert_1";
    private static final String ALERT_2 = "alert_2";
    private static final String ALERT_3 = "alert_3";

    private static class Printer implements Runnable {

        private final String name;

        public Printer(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println(name);
        }
    }

    @Test
    public void test() {

        addAlert1();
        addAlert2();
        addAlert3();

        int seconds = 0;
        while (seconds < 60) {
            ThreadUtil.safeSleep(1000);
            System.out.println(++seconds);
            if (seconds < 10) {
                addAlert1();
            }
            System.out.println("Time for alert 1: " + TimeAlert.getInstance(TIME_ALERT).getAlertRemainingTime(ALERT_1));
            System.out.println("Time for alert 2: " + TimeAlert.getInstance(TIME_ALERT).getAlertRemainingTime(ALERT_2));
            System.out.println("Time for alert 3: " + TimeAlert.getInstance(TIME_ALERT).getAlertRemainingTime(ALERT_3));
        }
    }

    private void addAlert1() {
        addAlert(ALERT_1, 3000);
    }

    private void addAlert2() {
        addAlert(ALERT_2, 4000);
    }

    private void addAlert3() {
        addAlert(ALERT_3, 5000);
    }

    private void addAlert(String name, long millis) {
        TimeAlert.getInstance(TIME_ALERT).addAlert(name, millis, new Printer(name));
    }
}