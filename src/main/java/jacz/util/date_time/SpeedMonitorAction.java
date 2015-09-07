package jacz.util.date_time;

/**
 *
 */
public interface SpeedMonitorAction {

    public void speedAboveRange(double speed);

    public void speedBelowRange(double speed);
}
