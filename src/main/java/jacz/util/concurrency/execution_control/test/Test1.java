package jacz.util.concurrency.execution_control.test;

import jacz.util.concurrency.execution_control.TrafficControl;
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

        TrafficControl trafficControl = new TrafficControl();
        ParallelTaskExecutor.executeTask(new Pauser(trafficControl));
        ParallelTaskExecutor.executeTask(new Accessor1(trafficControl));
        ParallelTaskExecutor.executeTask(new Accessor2(trafficControl));
    }
}
