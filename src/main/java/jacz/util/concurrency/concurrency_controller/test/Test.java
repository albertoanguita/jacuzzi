package jacz.util.concurrency.concurrency_controller.test;

import jacz.util.concurrency.concurrency_controller.ConcurrencyController;
import jacz.util.concurrency.concurrency_controller.ConcurrencyControllerReadWrite;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskSemaphore;

/**
 * Class description
 * <p/>
 * User: Admin<br>
 * Date: 09-may-2008<br>
 * Last Modified: 09-may-2008
 */
public class Test {


    public static void main(String args[]) {
        int x = 1000;
        ConcurrencyController cc = new ConcurrencyControllerReadWrite();

        //IOUtil.pauseEnter("pausa");
        TaskSemaphore[] tfi = new TaskSemaphore[8];

        tfi[0] = ParallelTaskExecutor.executeTask(new TestTask("ac1", 5 * x, "read"), cc, "read");
        tfi[1] = ParallelTaskExecutor.executeTask(new TestTask("ac2", 2 * x, "read"), cc, "read");
        tfi[2] = ParallelTaskExecutor.executeTask(new TestTask("ac3", 2 * x, "read"), cc, "read");
        tfi[3] = ParallelTaskExecutor.executeTask(new TestTask("ac4", 2 * x, "write"), cc, "write");
        tfi[4] = ParallelTaskExecutor.executeTask(new TestTask("ac5", 1 * x, "read"), cc, "read");
        tfi[5] = ParallelTaskExecutor.executeTask(new TestTask("ac6", 2 * x, "read"), cc, "read");
        tfi[6] = ParallelTaskExecutor.executeTask(new TestTask("ac7", 1 * x, "read"), cc, "read");
        tfi[7] = ParallelTaskExecutor.executeTask(new TestTask("ac8", 1 * x, "read"), cc, "read");

        for (int i = 0; i < 8; i++) {
            tfi[i].waitForFinalization();
            //System.out.println("task " + i + " finished!!!");
        }

        cc.stopAndWaitForFinalization();
    }
}
