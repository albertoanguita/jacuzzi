package jacz.util.concurrency.execution_control.test;

import jacz.util.concurrency.execution_control.PausableElement;

/**
 *
 */
public class Accessor2 implements Runnable {

    private PausableElement pausableElement;

    public Accessor2(PausableElement pausableElement) {
        this.pausableElement = pausableElement;
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
        pausableElement.access();
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
