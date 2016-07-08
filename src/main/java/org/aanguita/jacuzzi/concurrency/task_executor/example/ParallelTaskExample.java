package org.aanguita.jacuzzi.concurrency.task_executor.example;

import org.aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    public static void main(String args[]) throws ExecutionException, InterruptedException {
        // the two tasks are launched concurrently
        CountToMillionTask ctm1 = new CountToMillionTask();
        CountToMillionTask ctm2 = new CountToMillionTask();
        Future tfi1 = ThreadExecutor.submit(ctm1);
        Future tfi2 = ThreadExecutor.submit(ctm2);

        // wait until the tasks are complete
        System.out.println("waiting...");
        tfi1.get();
        tfi2.get();

        // retrieve result
        System.out.println("Result of the task: " + ctm1.result() + "/" + ctm2.result());
    }
}
