package jacz.util.concurrency.task_executor.example;

import jacz.util.concurrency.task_executor.Task;

/**
 * Count to a million task. Counts to a million and stores the result
 * <p/>
 * User: Alberto<br>
 * Date: 16-nov-2008<br>
 * Last Modified: 16-nov-2008
 */
public class CountToMillionTask implements Task {

    private int result;

    public void performTask() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        result = 0;
        for (int i = 0; i < 1000000; i++) {
            result++;
        }
    }

    public int result() {
        return result;
    }
}
