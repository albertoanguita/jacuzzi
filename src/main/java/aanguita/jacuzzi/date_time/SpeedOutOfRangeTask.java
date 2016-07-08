package aanguita.jacuzzi.date_time;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 04-jun-2010<br>
 * Last Modified: 04-jun-2010
 */
public class SpeedOutOfRangeTask implements Runnable {

    private SpeedMonitorAction speedMonitorAction;

    private boolean speedAbove;

    private double speed;

    public SpeedOutOfRangeTask(SpeedMonitorAction speedMonitorAction, boolean speedAbove, double speed) {
        this.speedMonitorAction = speedMonitorAction;
        this.speedAbove = speedAbove;
        this.speed = speed;
    }

    @Override
    public void run() {
        if (speedAbove) {
            speedMonitorAction.speedAboveRange(speed);
        } else {
            speedMonitorAction.speedBelowRange(speed);
        }
    }
}
