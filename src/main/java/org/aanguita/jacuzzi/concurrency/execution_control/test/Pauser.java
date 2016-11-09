package org.aanguita.jacuzzi.concurrency.execution_control.test;

import org.aanguita.jacuzzi.concurrency.SimpleSemaphore;

/**
 *
 */
public class Pauser implements Runnable {

    private SimpleSemaphore simpleSemaphore;

    public Pauser(SimpleSemaphore simpleSemaphore) {
        this.simpleSemaphore = simpleSemaphore;
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
        simpleSemaphore.pause();
        System.out.println("Pauser: pause #" + i + " OK");
    }

    private void resume(int i) {
        simpleSemaphore.resume();
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
