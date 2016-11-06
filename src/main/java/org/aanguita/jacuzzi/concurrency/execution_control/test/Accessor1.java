package org.aanguita.jacuzzi.concurrency.execution_control.test;

import org.aanguita.jacuzzi.concurrency.SimpleSemaphore;

/**
 *
 */
public class Accessor1 implements Runnable {

    private SimpleSemaphore simpleSemaphore;

    public Accessor1(SimpleSemaphore simpleSemaphore) {
        this.simpleSemaphore = simpleSemaphore;
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
        simpleSemaphore.access();
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
