package jacz.util.concurrency.execution_control.test;

import jacz.util.concurrency.execution_control.PausableElement;
import jacz.util.concurrency.task_executor.Task;

/**
 *
 */
public class Accessor1 implements Task {

    private PausableElement pausableElement;

    public Accessor1(PausableElement pausableElement) {
        this.pausableElement = pausableElement;
    }

    @Override
    public void performTask() {

        access(1);

        sleep(1000);

        access(2);
        access(3);

        sleep(2000);

        access(4);
    }

    private void access(int i) {
        pausableElement.access();
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
