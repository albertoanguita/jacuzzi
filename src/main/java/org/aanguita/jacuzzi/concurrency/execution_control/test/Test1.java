package org.aanguita.jacuzzi.concurrency.execution_control.test;

import org.aanguita.jacuzzi.concurrency.SimpleSemaphore;
import org.aanguita.jacuzzi.concurrency.ThreadExecutor;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 01-sep-2010
 * Time: 10:59:45
 * To change this template use File | Settings | File Templates.
 */
public class Test1 {

    public static void main(String args[]) {

        SimpleSemaphore simpleSemaphore = new SimpleSemaphore();
        ThreadExecutor.submit(new Pauser(simpleSemaphore));
        ThreadExecutor.submit(new Accessor1(simpleSemaphore));
        ThreadExecutor.submit(new Accessor2(simpleSemaphore));
    }
}
