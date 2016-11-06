package org.aanguita.jacuzzi.concurrency.execution_control.test;

import org.aanguita.jacuzzi.concurrency.SimpleSemaphore;

/**
 *
 */
public class Accessor2 implements Runnable {

    private SimpleSemaphore simpleSemaphore;

    public Accessor2(SimpleSemaphore simpleSemaphore) {
        this.simpleSemaphore = simpleSemaphore;
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
        simpleSemaphore.access();
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
