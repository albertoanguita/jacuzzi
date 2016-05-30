package jacz.util.date_time;

/**
 *
 */
public interface SpeedMonitorAction {

    void speedAboveRange(double speed);

    void speedBelowRange(double speed);
}
