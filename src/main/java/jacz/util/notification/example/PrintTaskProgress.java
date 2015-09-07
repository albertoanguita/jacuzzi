package jacz.util.notification.example;

import jacz.util.notification.ProgressNotification;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 16-nov-2008<br>
 * Last Modified: 16-nov-2008
 */
public class PrintTaskProgress implements ProgressNotification<Integer> {

    public void addNotification(Integer message) {
        System.out.println(message);
    }

    public void completeTask() {
        System.out.println("COMPLETE!!!");
    }
}
