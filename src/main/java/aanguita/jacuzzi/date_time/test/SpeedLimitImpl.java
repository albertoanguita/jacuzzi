package aanguita.jacuzzi.date_time.test;

import aanguita.jacuzzi.date_time.RemainingTimeAction;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 31-may-2010
 * Time: 13:10:22
 * To change this template use File | Settings | File Templates.
 */
public class SpeedLimitImpl implements RemainingTimeAction {

    @Override
    public void speedAboveRange(double speed) {
        System.out.println("Speed above: " + speed);
    }

    @Override
    public void speedBelowRange(double speed) {
        System.out.println("Speed below: " + speed);
    }

    @Override
    public void remainingTime(long millis) {
        System.out.println("Remaining time!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: " + millis);
    }
}
