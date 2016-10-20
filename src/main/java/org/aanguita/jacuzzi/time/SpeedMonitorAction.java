package org.aanguita.jacuzzi.time;

/**
 *
 */
public interface SpeedMonitorAction {

    void speedAboveRange(double speed);

    void speedBelowRange(double speed);
}
