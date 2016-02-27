package jacz.util.concurrency.task_executor.example;

import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskSemaphore;

/**
 * Example of use of ParallelTaskExecutor
 * One task for counting up to a million is launched concurrently. This method waits until it is complete,
 * and then prints the result
 * <p/>
 * User: Alberto<br>
 * Date: 16-nov-2008<br>
 * Last Modified: 16-nov-2008
 */
public class ParallelTaskExample {

    public static void main(String args[]) {
        // the two tasks are launched concurrently
        CountToMillionTask ctm1 = new CountToMillionTask();
        CountToMillionTask ctm2 = new CountToMillionTask();
        TaskSemaphore tfi1 = ParallelTaskExecutor.executeTask(ctm1);
        TaskSemaphore tfi2 = ParallelTaskExecutor.executeTask(ctm2);

        // wait until the tasks are complete
        System.out.println("waiting...");
        tfi1.waitForFinalization();
        tfi2.waitForFinalization();

        // retrieve result
        System.out.println("Result of the task: " + ctm1.result() + "/" + ctm2.result());
    }
}
