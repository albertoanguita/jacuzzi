package jacz.util.notification.example;

import jacz.util.notification.ProgressNotification;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 16-nov-2008<br>
 * Last Modified: 16-nov-2008
 */
public class TaskWithProgress {

    /**
     * Performs a long task. Reports progress wieh integer values indicating percentage complete (from 0 to 100)
     *
     * @param progress progress notification object (null if no progress is going to be used)
     */
    public void task(ProgressNotification<Integer> progress) {
        if (progress != null) {
            progress.addNotification(0);
        }
        for (int i = 0; i < 10000; i++) {

            int k = 0;
            for (int j = 0; j < 100000; j++) {
                k = i * j;
            }
            if (progress != null) {
                //System.out.println(i / 100);
                progress.addNotification(i / 100);
            }
        }
        if (progress != null) {
            progress.addNotification(100);
            progress.completeTask();
        }
    }
}
