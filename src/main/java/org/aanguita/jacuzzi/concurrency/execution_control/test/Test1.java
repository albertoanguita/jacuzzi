package org.aanguita.jacuzzi.concurrency.execution_control.test;

import org.aanguita.jacuzzi.concurrency.execution_control.TrafficControl;
import org.aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;

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
        ThreadExecutor.submit(new Pauser(trafficControl));
        ThreadExecutor.submit(new Accessor1(trafficControl));
        ThreadExecutor.submit(new Accessor2(trafficControl));
    }
}
