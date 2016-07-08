package aanguita.jacuzzi.concurrency.execution_control.test;

import aanguita.jacuzzi.concurrency.execution_control.TrafficControl;

/**
 *
 */
public class Pauser implements Runnable {

    private TrafficControl trafficControl;

    public Pauser(TrafficControl trafficControl) {
        this.trafficControl = trafficControl;
    }

    @Override
    public void run() {

        sleep(500);

        pause(1);
        pause(2);

        sleep(5000);

        resume(1);
        resume(2);
    }

    private void pause(int i) {
        trafficControl.pause();
        System.out.println("Pauser: pause #" + i + " OK");
    }

    private void resume(int i) {
        trafficControl.resume();
        System.out.println("Pauser: resume #" + i + " OK");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
