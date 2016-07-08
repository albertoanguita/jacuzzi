package org.aanguita.jacuzzi.concurrency.concurrency_controller.test;

/**
 * Class description
 * <p/>
 * User: Admin<br>
 * Date: 09-may-2008<br>
 * Last Modified: 09-may-2008
 */
public class TestTask implements Runnable {

    private String name;

    private int limit;

    private String concurrentActivity;

    public TestTask(String name, int limit, String concurrentActivity) {
        this.name = name;
        this.limit = limit;
        this.concurrentActivity = concurrentActivity;
        System.out.println(name + " (" + concurrentActivity + "): ready to execute");
    }

    public void run() {
        System.out.println(name + " (" + concurrentActivity + "): starts executing");
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                System.out.print("");
            }
        }
        System.out.println(name + " (" + concurrentActivity + "): ends");
    }
}
