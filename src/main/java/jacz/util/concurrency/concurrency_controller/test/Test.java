package jacz.util.concurrency.concurrency_controller.test;

import jacz.util.concurrency.concurrency_controller.ConcurrencyControllerReadWrite;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskFinalizationIndicator;
import jacz.util.lists.Duple;

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
        CC cc = new CC();

        //IOUtil.pauseEnter("pausa");
        TaskFinalizationIndicator[] tfi = new TaskFinalizationIndicator[8];

        tfi[0] = ParallelTaskExecutor.executeTask(new TestParallelTask("ac1", 5 * x, "read"), cc, "read");
        tfi[1] = ParallelTaskExecutor.executeTask(new TestParallelTask("ac2", 2 * x, "read"), cc, "read");
        tfi[2] = ParallelTaskExecutor.executeTask(new TestParallelTask("ac3", 2 * x, "read"), cc, "read");
        tfi[3] = ParallelTaskExecutor.executeTask(new TestParallelTask("ac4", 2 * x, "write"), cc, "write");
        tfi[4] = ParallelTaskExecutor.executeTask(new TestParallelTask("ac5", 1 * x, "read"), cc, "read");
        tfi[5] = ParallelTaskExecutor.executeTask(new TestParallelTask("ac6", 2 * x, "read"), cc, "read");
        tfi[6] = ParallelTaskExecutor.executeTask(new TestParallelTask("ac7", 1 * x, "read"), cc, "read");
        tfi[7] = ParallelTaskExecutor.executeTask(new TestParallelTask("ac8", 1 * x, "read"), cc, "read");

        for (int i = 0; i < 8; i++) {
            tfi[i].waitForFinalization();
            //System.out.println("task " + i + " finished!!!");
        }
        //cc.endConcurrencyController();
    }
}
