package jacz.util.notification.example;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 16-nov-2008<br>
 * Last Modified: 16-nov-2008
 */
public class ProgressExample {

    public static void main(String args[]) {

        TaskWithProgress twp = new TaskWithProgress();

        twp.task(new PrintTaskProgress());

        System.out.println("END");
    }
}
