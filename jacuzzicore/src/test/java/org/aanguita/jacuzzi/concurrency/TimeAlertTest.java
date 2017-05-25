package org.aanguita.jacuzzi.concurrency;

import org.junit.Test;

import java.util.function.Consumer;

/**
 * @author aanguita
 *         11/05/2017
 */
public class TimeAlertTest {

    private static final String TIME_ALERT = "time";
    private static final String ALERT_1 = "alert_1";
    private static final String ALERT_2 = "alert_2";
    private static final String ALERT_3 = "alert_3";

    private static class Printer implements Consumer<String> {

        private final String name;

        public Printer(String name) {
            this.name = name;
        }

        @Override
        public void accept(String alertName) {
            System.out.println(name);
        }
    }

    private static class AutoRepeat implements Consumer<String> {

        int timesLeft;

        public AutoRepeat(int timesLeft) {
            this.timesLeft = timesLeft;
        }

        @Override
        public void accept(String alertName) {
            System.out.println("AutoRepeat. Times left: " + timesLeft);
            if (timesLeft > 0) {
                TimeAlert.getInstance(TIME_ALERT).addAlert(alertName, 1000, new AutoRepeat(timesLeft - 1));
            }
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

    @Test
    public void autoRepeatTest() {
        TimeAlert.getInstance(TIME_ALERT).addAlert("auto.repeat", 1000, new AutoRepeat(10));
        ThreadUtil.safeSleep(15000);
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