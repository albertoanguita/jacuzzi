package aanguita.jacuzzi.concurrency;

/**
 * This class offers utility methods for concurrency handling
 */
public class ConcurrencyUtil {

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
