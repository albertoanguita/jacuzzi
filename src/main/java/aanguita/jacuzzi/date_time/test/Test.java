package aanguita.jacuzzi.date_time.test;

import aanguita.jacuzzi.date_time.TimeElapsed;

import java.text.SimpleDateFormat;
import java.util.Date;

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

        Date date = new Date();
        System.out.println(date);
        System.out.println(new SimpleDateFormat("Y/M/d-HH:mm:ss:SSS").format(date));

        System.out.println(timeElapsed.measureTime());

    }
}
