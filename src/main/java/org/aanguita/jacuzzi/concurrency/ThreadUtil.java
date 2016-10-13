package org.aanguita.jacuzzi.concurrency;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for threads
 */
public class ThreadUtil {

    public static String invokerName(int levels) {
        return Thread.currentThread().getStackTrace()[levels + 2].toString();
    }

    public static void safeSleep(long millis) {
        if (millis > 0L) {
            long initialTime = System.currentTimeMillis();
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                // sleep again the remaining time
                safeSleep(millis - System.currentTimeMillis() + initialTime);
            }
        }
    }

    public static void printThreadStacks(PrintStream ps) {
        printThreadStacks(ps, false);
    }

    public static void printThreadStacks(PrintStream ps, boolean printSystemThreads) {
        Set<String> systemThreadsNames = buildSystemThreadsNames();
        Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
        for (Thread thread : threadMap.keySet()) {
            if (printSystemThreads || !systemThreadsNames.contains(thread.getName())) {
                ps.println(thread.getName());
                StackTraceElement[] trace = threadMap.get(thread);
                for (StackTraceElement traceElement : trace) {
                    ps.println("\tat " + traceElement);
                }
            }
        }
    }

    private static Set<String> buildSystemThreadsNames() {
        Set<String> systemThreadsNames = new HashSet<>();
        systemThreadsNames.add("Attach Listener");
        systemThreadsNames.add("Monitor Ctrl-Break");
        systemThreadsNames.add("Finalizer");
        systemThreadsNames.add("Signal Dispatcher");
        systemThreadsNames.add("Reference Handler");
        return systemThreadsNames;
    }


    /**
     * Calculates a number of threads, based on system specifications and user needs. The number of desired threads
     * is specified through a parameter. The result will be this value, or the number of cores available in the system,
     * whichever is lower. Also, the result will never be less than 1
     *
     * @param threads the number of desired threads
     * @return the available threads (the desired threads is 0 < threads <= cores), 1 if threads <= 0,
     *         cores if threads > cores
     */
    public static int threadCount(int threads) {
        if (threads <= 0 || threads > Runtime.getRuntime().availableProcessors()) {
            threads = Runtime.getRuntime().availableProcessors();
        }
        return threads;
    }

    /**
     * Calculates a number of threads based on the percentage of cores of the system to use
     *
     * @param factor a double value indicating the percentage of cores we want to use (between 0 and 1)
     * @return the number of available threads, based on the specified factor (always >= 1 and <= cores
     */
    public static int threadCount(double factor) {
        int threads = Runtime.getRuntime().availableProcessors();
        if (factor > 0d) {
            threads = (int) Math.rint(((float) threads) * factor);
            threads = Math.max(threads, 1);
        }
        return threads;
    }
}
