package org.aanguita.jacuzzi.concurrency.execution_control.test;

import org.aanguita.jacuzzi.concurrency.execution_control.TrafficControl;

/**
 *
 */
public class Accessor2 implements Runnable {

    private TrafficControl trafficControl;

    public Accessor2(TrafficControl trafficControl) {
        this.trafficControl = trafficControl;
    }

    @Override
    public void run() {

        access(1);
        access(2);
        access(3);

        sleep(1000);

        access(4);
        access(5);

        sleep(2000);

        access(6);
    }

    private void access(int i) {
        trafficControl.access();
        System.out.println("Accessor2: access #" + i + " OK");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
