package jacz.util.date_time.test;

import jacz.util.date_time.DateTime;
import jacz.util.date_time.TimeElapsed;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 03-nov-2007<br>
 * Last Modified: 03-nov-2007
 */
public class Test {

    public static void main(String args[]) {

        TimeElapsed timeElapsed = new TimeElapsed();

        System.out.println(DateTime.getFormattedCurrentDateTime(
                DateTime.DateTimeElement.YYYY, "/",
                DateTime.DateTimeElement.MM, "/",
                DateTime.DateTimeElement.DD, "-",
                DateTime.DateTimeElement.hh, ":",
                DateTime.DateTimeElement.mm, ":",
                DateTime.DateTimeElement.ss));

        System.out.println(timeElapsed.measureTime());

    }
}
