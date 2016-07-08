package aanguita.jacuzzi.concurrency.task_executor;

import aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Test;

/**
 * Created by Alberto on 04/04/2016.
 */
public class ThreadExecutorTest {

    private static class RunnableTask implements Runnable {

        private final String name;

        public RunnableTask(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println("Start runnable: '" + name + " = " + Thread.currentThread().getName() + "'");
            ThreadUtil.safeSleep(4000);
            System.out.println("End runnable: '" + name + " = " + Thread.currentThread().getName() + "'");
        }
    }

    @Test
    public void test() {

        ThreadExecutor.submit(new RunnableTask("-"));
        ThreadExecutor.submit(new RunnableTask("runnable1"), "runnable1");
        ThreadExecutor.submit(new RunnableTask("runnable2"), "runnable2");

        ThreadExecutor.submit(() -> System.out.println("hello"));

        ThreadUtil.safeSleep(5000);
    }
}