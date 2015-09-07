package jacz.util.date_time;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * todo remove, obsolete
 */
public class DateTime {

    public enum DateTimeElement {
        YYYY,
        YY,
        MM,
        DD,
        hh,
        mm,
        ss
    }

    public static String getFormattedCurrentDateTime(DateTimeElement dateTimeElement1) {
        return getFormattedDateTime(new GregorianCalendar(), dateTimeElement1);
    }

    public static String getFormattedDateTime(Calendar calendar, DateTimeElement dateTimeElement1) {
        switch (dateTimeElement1) {

            case YYYY:
                return integerToString(Integer.toString(calendar.get(GregorianCalendar.YEAR)), 4);
            case YY:
                return integerToString(Integer.toString(calendar.get(GregorianCalendar.YEAR)).substring(2), 2);
            case MM:
                return integerToString(Integer.toString(calendar.get(GregorianCalendar.MONTH) + 1), 2);
            case DD:
                return integerToString(Integer.toString(calendar.get(GregorianCalendar.DAY_OF_MONTH)), 2);
            case hh:
                return integerToString(Integer.toString(calendar.get(GregorianCalendar.HOUR_OF_DAY)), 2);
            case mm:
                return integerToString(Integer.toString(calendar.get(GregorianCalendar.MINUTE)), 2);
            case ss:
                return integerToString(Integer.toString(calendar.get(GregorianCalendar.SECOND)), 2);
        }
        return "";
    }


    private static String integerToString(String integerValue, int numDigits) {
        if (integerValue.length() < numDigits) {
            return "0" + integerToString(integerValue, numDigits - 1);
        } else {
            return integerValue;
        }
    }

    public static String getFormattedCurrentDateTime(DateTimeElement dateTimeElement1,
                                                     String sep1,
                                                     DateTimeElement dateTimeElement2) {
        return getFormattedDateTime(new GregorianCalendar(), dateTimeElement1, sep1, dateTimeElement2);
    }

    public static String getFormattedDateTime(Calendar calendar,
                                              DateTimeElement dateTimeElement1,
                                              String sep1,
                                              DateTimeElement dateTimeElement2) {
        return getFormattedDateTime(calendar, dateTimeElement1) +
                sep1 +
                getFormattedDateTime(calendar, dateTimeElement2);
    }

    public static String getFormattedCurrentDateTime(DateTimeElement dateTimeElement1,
                                                     String sep1,
                                                     DateTimeElement dateTimeElement2,
                                                     String sep2,
                                                     DateTimeElement dateTimeElement3) {
        return getFormattedDateTime(new GregorianCalendar(),
                dateTimeElement1,
                sep1,
                dateTimeElement2,
                sep2,
                dateTimeElement3);
    }

    public static String getFormattedDateTime(Calendar calendar,
                                              DateTimeElement dateTimeElement1,
                                              String sep1,
                                              DateTimeElement dateTimeElement2,
                                              String sep2,
                                              DateTimeElement dateTimeElement3) {
        return getFormattedDateTime(calendar, dateTimeElement1) +
                sep1 +
                getFormattedDateTime(calendar,
                        dateTimeElement2,
                        sep2,
                        dateTimeElement3);
    }

    public static String getFormattedCurrentDateTime(DateTimeElement dateTimeElement1,
                                                     String sep1,
                                                     DateTimeElement dateTimeElement2,
                                                     String sep2,
                                                     DateTimeElement dateTimeElement3,
                                                     String sep3,
                                                     DateTimeElement dateTimeElement4) {
        return getFormattedDateTime(new GregorianCalendar(),
                dateTimeElement1,
                sep1,
                dateTimeElement2,
                sep2,
                dateTimeElement3,
                sep3,
                dateTimeElement4);
    }

    public static String getFormattedDateTime(Calendar calendar,
                                              DateTimeElement dateTimeElement1,
                                              String sep1,
                                              DateTimeElement dateTimeElement2,
                                              String sep2,
                                              DateTimeElement dateTimeElement3,
                                              String sep3,
                                              DateTimeElement dateTimeElement4) {
        return getFormattedDateTime(calendar, dateTimeElement1) +
                sep1 +
                getFormattedDateTime(calendar,
                        dateTimeElement2,
                        sep2,
                        dateTimeElement3,
                        sep3,
                        dateTimeElement4);
    }

    public static String getFormattedCurrentDateTime(DateTimeElement dateTimeElement1,
                                                     String sep1,
                                                     DateTimeElement dateTimeElement2,
                                                     String sep2,
                                                     DateTimeElement dateTimeElement3,
                                                     String sep3,
                                                     DateTimeElement dateTimeElement4,
                                                     String sep4,
                                                     DateTimeElement dateTimeElement5) {
        return getFormattedDateTime(new GregorianCalendar(),
                dateTimeElement1,
                sep1,
                dateTimeElement2,
                sep2,
                dateTimeElement3,
                sep3,
                dateTimeElement4,
                sep4,
                dateTimeElement5);
    }

    public static String getFormattedDateTime(Calendar calendar,
                                              DateTimeElement dateTimeElement1,
                                              String sep1,
                                              DateTimeElement dateTimeElement2,
                                              String sep2,
                                              DateTimeElement dateTimeElement3,
                                              String sep3,
                                              DateTimeElement dateTimeElement4,
                                              String sep4,
                                              DateTimeElement dateTimeElement5) {
        return getFormattedDateTime(calendar, dateTimeElement1) +
                sep1 +
                getFormattedDateTime(calendar,
                        dateTimeElement2,
                        sep2,
                        dateTimeElement3,
                        sep3,
                        dateTimeElement4,
                        sep4,
                        dateTimeElement5);
    }

    public static String getFormattedCurrentDateTime(DateTimeElement dateTimeElement1,
                                                     String sep1,
                                                     DateTimeElement dateTimeElement2,
                                                     String sep2,
                                                     DateTimeElement dateTimeElement3,
                                                     String sep3,
                                                     DateTimeElement dateTimeElement4,
                                                     String sep4,
                                                     DateTimeElement dateTimeElement5,
                                                     String sep5,
                                                     DateTimeElement dateTimeElement6) {
        return getFormattedDateTime(new GregorianCalendar(),
                dateTimeElement1,
                sep1,
                dateTimeElement2,
                sep2,
                dateTimeElement3,
                sep3,
                dateTimeElement4,
                sep4,
                dateTimeElement5,
                sep5,
                dateTimeElement6);
    }

    public static String getFormattedDateTime(Calendar calendar,
                                              DateTimeElement dateTimeElement1,
                                              String sep1,
                                              DateTimeElement dateTimeElement2,
                                              String sep2,
                                              DateTimeElement dateTimeElement3,
                                              String sep3,
                                              DateTimeElement dateTimeElement4,
                                              String sep4,
                                              DateTimeElement dateTimeElement5,
                                              String sep5,
                                              DateTimeElement dateTimeElement6) {
        return getFormattedDateTime(calendar, dateTimeElement1) +
                sep1 +
                getFormattedDateTime(calendar,
                        dateTimeElement2,
                        sep2,
                        dateTimeElement3,
                        sep3,
                        dateTimeElement4,
                        sep4,
                        dateTimeElement5,
                        sep5,
                        dateTimeElement6);
    }

}
