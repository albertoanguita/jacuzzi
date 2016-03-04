package jacz.util.concurrency.execution_control.test;

import jacz.util.concurrency.execution_control.TrafficControl;

/**
 *
 */
public class Accessor1 implements Runnable {

    private TrafficControl trafficControl;

    public Accessor1(TrafficControl trafficControl) {
        this.trafficControl = trafficControl;
    }

    @Override
    public void run() {

        access(1);

        sleep(1000);

        access(2);
        access(3);

        sleep(2000);

        access(4);
    }

    private void access(int i) {
        trafficControl.access();
        System.out.println("Accessor1: access #" + i + " OK");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
