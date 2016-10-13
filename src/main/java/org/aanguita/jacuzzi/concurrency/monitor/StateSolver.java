package org.aanguita.jacuzzi.concurrency.monitor;

/**
 * Action that a monitor must perform to solve a state problem. Synchronization issues must be solved by the class implementing this interface
 */
public interface StateSolver {

    /**
     * Performs an action to solve the state
     *
     * @return true if the state is now in the desired point, false otherwise. No exceptions must be raised.
     */
    boolean solveState();
}
