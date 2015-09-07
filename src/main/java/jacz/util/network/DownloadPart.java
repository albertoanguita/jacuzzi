package jacz.util.network;

/**
 * This class stores the data related to an in-progress download, and offers the methods to control such download
 * (pause, resume, cancel...)
 */
public class DownloadPart {

    /**
     * The DownloadTask associated to this DownloadPart (object in charge of performing the actual download)
     */
    private final DownloadTask downloadTask;

    /**
     * Class constructor
     *
     * @param downloadTask task for downloading the file
     */
    DownloadPart(DownloadTask downloadTask) {
        this.downloadTask = downloadTask;
    }

    /**
     * Pause the current download. It the download was already paused, this method has no effect
     */
    public synchronized void pause() {
        downloadTask.pause();
    }

    /**
     * Resume the current download. It the download was already resumed, this method has no effect
     */
    public synchronized void resume() {
        downloadTask.resume();
    }

    public synchronized boolean isPaused() {
        return downloadTask.isPaused();
    }

    /**
     * cancel the current download. It the download was already cancelled, this method has no effect
     */
    public synchronized void cancel() {
        downloadTask.cancel();
    }

    public synchronized boolean isCancelled() {
        return downloadTask.isCancelled();
    }

    /**
     * Retrieves the path of the file generated on disk. Can be invoked during or after the download
     *
     * @return the path of the file generated on disk
     */
    public String getLocalPath() {
        return downloadTask.getLocalPath();
    }

    /**
     * Retrieves the current maxSize in bytes of the generated file on disk. Can be invoked during or after the download
     *
     * @return the current maxSize in bytes of the generated file on disk
     */
    public long getLocalSize() {
        return downloadTask.getCurrentLength();
    }

    /**
     * Retrieves the total maxSize of the file being downloaded. Can be invoked during or after the download
     *
     * @return the total maxSize of the file being downloaded in bytes, or -1 if it is not known
     */
    public long getTotalSize() {
        return downloadTask.getTotalLength();
    }
}
