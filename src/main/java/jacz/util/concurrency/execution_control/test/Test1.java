package jacz.util.concurrency.execution_control.test;

import jacz.util.concurrency.execution_control.PausableElement;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 01-sep-2010
 * Time: 10:59:45
 * To change this template use File | Settings | File Templates.
 */
public class Test1 {

    public static void main(String args[]) {

        PausableElement pausableElement = new PausableElement();
        ParallelTaskExecutor.executeTask(new Pauser(pausableElement));
        ParallelTaskExecutor.executeTask(new Accessor1(pausableElement));
        ParallelTaskExecutor.executeTask(new Accessor2(pausableElement));
    }
}
