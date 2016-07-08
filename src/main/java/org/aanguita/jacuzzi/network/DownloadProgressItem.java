package org.aanguita.jacuzzi.network;

import java.io.IOException;

/**
 * This class represents an item of the notification of the progress of a file download. It is composed by two values:
 * current percentage completed in decimals (an int value from 0 to 1000), and current speed (a double value >= than 0,
 * in bytes per second). There is a third component that can indicate an error during the download (stored as an
 * IOException object).
 * <p/>
 * These items are emitted periodically (the frequency can be specified at the corresponding method in URLDownloader).
 * <p/>
 * It must be noted that an object of this class is only valid for the moment it is emitted. Its values are never
 * updated, but rather new objects of this class are created with the new values.
 */
public class DownloadProgressItem {

    private final int percentage;

    private final double speed;

    private final IOException e;

    public DownloadProgressItem(int percentage, double speed) {
        this.percentage = percentage;
        this.speed = speed;
        this.e = null;
    }

    public DownloadProgressItem(IOException e) {
        this.percentage = 0;
        this.speed = 0;
        this.e = e;
    }

    public int getPercentage() {
        return percentage;
    }

    public double getSpeed() {
        return speed;
    }

    public IOException getE() {
        return e;
    }

    public boolean hasException() {
        return e != null;
    }
}
