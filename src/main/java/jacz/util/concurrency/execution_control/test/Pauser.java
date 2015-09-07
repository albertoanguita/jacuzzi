package jacz.util.concurrency.execution_control.test;

import jacz.util.concurrency.execution_control.PausableElement;
import jacz.util.concurrency.task_executor.ParallelTask;

/**
 *
 */
public class Pauser implements ParallelTask {

    private PausableElement pausableElement;

    public Pauser(PausableElement pausableElement) {
        this.pausableElement = pausableElement;
    }

    @Override
    public void performTask() {

        sleep(500);

        pause(1);
        pause(2);

        sleep(5000);

        resume(1);
        resume(2);
    }

    private void pause(int i) {
        pausableElement.pause();
        System.out.println("Pauser: pause #" + i + " OK");
    }

    private void resume(int i) {
        pausableElement.resume();
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
