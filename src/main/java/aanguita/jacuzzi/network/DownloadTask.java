package aanguita.jacuzzi.network;

import aanguita.jacuzzi.concurrency.timer.Timer;
import aanguita.jacuzzi.concurrency.timer.TimerAction;
import aanguita.jacuzzi.date_time.SpeedMonitor;
import aanguita.jacuzzi.notification.ProgressNotification;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class implements the task of downloading a file to disk, allowing to carry out that task in parallel
 */
class DownloadTask implements Runnable, TimerAction {

    private static final int BUFFER_LENGTH = 1024;

    private static final long MILLIS_TO_MEASURE_SPEED = 5000;


    private final URL url;

    private final String localPath;

    private final ProgressNotification<DownloadProgressItem> progressNotification;

    private BufferedInputStream isr;

    private BufferedOutputStream out;

    private long totalLength;

    private long currentLength;

    private boolean isPaused;

    private ReentrantLock lock;

    private boolean isCancelled;

    private SpeedMonitor speedMonitor;

    private final Timer timer;

    DownloadTask(URL url, String localPath, ProgressNotification<DownloadProgressItem> progressNotification, long timerMillis) throws IOException {
        this(url, localPath, progressNotification, timerMillis, "");
    }

    DownloadTask(URL url, String localPath, ProgressNotification<DownloadProgressItem> progressNotification, long timerMillis, String threadName) throws IOException {
        this.url = url;
        this.localPath = localPath;
        this.progressNotification = progressNotification;
        lock = new ReentrantLock();
        isPaused = false;
        isCancelled = false;
        initializeDownload();
        speedMonitor = new SpeedMonitor(MILLIS_TO_MEASURE_SPEED);
        timer = new Timer(timerMillis, this, threadName + ":" + DownloadTask.class.getName());
    }

    private void initializeDownload() throws IOException {
        URLConnection urlConnection = url.openConnection();
        totalLength = urlConnection.getContentLength();
        currentLength = 0;
        isr = new BufferedInputStream(urlConnection.getInputStream());
        out = new BufferedOutputStream(new FileOutputStream(localPath));
    }


    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_LENGTH];
        boolean finished = false;
        while (!finished) {
            // go through the pause condition
            lock.lock();
            lock.unlock();

            // go through the pause condition
            if (checkCancellation()) {
                IOException e = cancelDownload();
                if (e != null) {
                    progressNotification.addNotification(new DownloadProgressItem(e));
                }
                break;
            }

            // read bytes from the URL and write them to disk
            int charsRead = 0;
            try {
                charsRead = isr.read(buffer);
            } catch (IOException e) {
                //noinspection ThrowableResultOfMethodCallIgnored
                cancelDownload();
                progressNotification.addNotification(new DownloadProgressItem(e));
            }
            if (charsRead == -1) {
                finished = true;
            } else {
                try {
                    out.write(buffer, 0, charsRead);
                } catch (IOException e) {
                    //noinspection ThrowableResultOfMethodCallIgnored
                    cancelDownload();
                    progressNotification.addNotification(new DownloadProgressItem(e));
                }
                synchronized (this) {
                    currentLength += charsRead;
                    speedMonitor.addProgress(charsRead);
                }
            }
        }
        try {
            closeChannels();
        } catch (IOException e) {
            //noinspection ThrowableResultOfMethodCallIgnored
            cancelDownload();
            progressNotification.addNotification(new DownloadProgressItem(e));
        }
        timer.kill();
        if (progressNotification != null && !isCancelled) {
            progressNotification.completeTask();
        }
    }

    private synchronized boolean checkCancellation() {
        return isCancelled;
    }

    private synchronized IOException cancelDownload() {
        timer.kill();
        try {
            closeChannels();
        } catch (IOException e) {
            // errors closing the channels -> return exception in addition to the cancellation
            return e;
        } finally {
            try {
                Files.delete(Paths.get(localPath));
            } catch (IOException e) {
                // ignore
            }
        }
        return null;
    }

    private void closeChannels() throws IOException {
        out.close();
        isr.close();
    }

    private static void notifyProgress(ProgressNotification<DownloadProgressItem> progressNotification, long totalLength, long currentLength, SpeedMonitor speedMonitor) {
        if (progressNotification != null && totalLength != -1) {
            int percentage = (int) (1000 * currentLength / totalLength);
            if (percentage < 0) {
                percentage = 0;
            } else if (percentage > 1000) {
                percentage = 1000;
            }
            double speed = speedMonitor.getAverageSpeed();
            progressNotification.addNotification(new DownloadProgressItem(percentage, speed));
        }
    }

    public synchronized void pause() {
        if (!isCancelled && !isPaused) {
            lock.lock();
            isPaused = true;
        }
    }

    public synchronized void resume() {
        if (isPaused) {
            lock.unlock();
            isPaused = false;
        }
    }

    public synchronized boolean isPaused() {
        return isPaused;
    }

    public synchronized void cancel() {
        isCancelled = true;
        resume();
    }

    public synchronized boolean isCancelled() {
        return isCancelled;
    }

    public String getLocalPath() {
        return localPath;
    }

    public synchronized long getCurrentLength() {
        return currentLength;
    }

    public long getTotalLength() {
        return totalLength;
    }

    @Override
    public Long wakeUp(Timer timer) {
        synchronized (this) {
            if (!isPaused()) {
                notifyProgress(progressNotification, totalLength, currentLength, speedMonitor);
            }
        }
        return null;
    }
}
