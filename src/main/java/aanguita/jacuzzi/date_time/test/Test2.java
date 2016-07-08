package aanguita.jacuzzi.date_time.test;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 03-jun-2010<br>
 * Last Modified: 03-jun-2010
 */
public class Test2 {

    public static void main(String args[]) {

        //SpeedMonitorWithRemainingTime speedMeasure = new SpeedMonitorWithRemainingTime(60000, null, new SpeedLimitImpl(), new LongRange((long) 5, (long) 10), 10000, 0, 20000);
//        SpeedMonitorWithRemainingTime speedMeasure = new SpeedMonitorWithRemainingTime(60000, null, 0, new SpeedLimitImpl(), 20000, new LongRange((long) 5, (long) 10), 10000, "SpeedMeasureTest2");
//
//        speedMeasure.addCapacity(2);
//        sleep(2000);
//        speedMeasure.addProgress(2);
//        System.out.println("1");
//
//        speedMeasure.addCapacity(2);
//        sleep(2000);
//        speedMeasure.addProgress(2);
//        System.out.println("2");
//
//        speedMeasure.addCapacity(4);
//        sleep(2000);
//        speedMeasure.addProgress(4);
//        System.out.println("3");
//
//
//        speedMeasure.stop();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
