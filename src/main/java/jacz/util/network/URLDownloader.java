package jacz.util.network;

import jacz.util.concurrency.task_executor.ThreadExecutor;
import jacz.util.notification.ProgressNotification;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;

/**
 * This class contains methods for downloading files from an URL to disk.
 * <p/>
 * There are two main methods (each overloaded with several versions). The first, downloadURL, is a non-concurrent
 * method for downloading a file to disk. The invocation will not return until the file is totally downloaded (or
 * an exception is raised). Optionally, we can ask it to report the progress of the download. These types of downloads
 * cannot be paused, resumed or cancelled.
 * <p/>
 * The second type of download, concurrentDownloadURL, is a concurrent version of the first. A new thread will
 * be created for the download itself, so the invocation to the method will return immediately. In addition,
 * with the invocation the client obtains an object of the DownloadPart class which allows him to control some
 * aspects of the download (pause, resume, cancel). Optionally, we can receive notifications of the download
 * progress, with a specific periodicity.
 * <p/>
 * These methods always require the URL to download from and the disk destination path. If the given path is a
 * directory, then the method will create a file named equal to the file contained in the URL (if possible)
 *
 * todo can be replaced with FileUtils.copyURLtoFile
 */
public class URLDownloader {

    private static final int BUFFER_LENGTH = 1024;

    private static final long DEFAULT_TIMER_MILLIS = 1000;


    /**
     * @param url      the URL containing the file to download
     * @param filePath the path where the downloaded file must be placed (can be a directory if the URL contains a file)
     * @throws IOException problems accessing the given URL, or problems writing the file in the disk
     */
    public static void downloadURL(URL url, String filePath) throws IOException {
        downloadURL(url, filePath, null);
    }

    /**
     * @param url                  the URL containing the file to download
     * @param filePath             the path where the downloaded file must be placed (can be a directory if the URL
     *                             contains a file)
     * @param progressNotification object to notify the progress of the download process. Integer values from 0 to 100
     *                             will be submitted (indicating the percentage completed), with a completeTask when
     *                             the download is completed. Notifications are given each time the percentage value
     *                             increases. A null value for this object means that no progress notification is given
     * @throws IOException problems accessing the given URL, or problems writing the file in the disk
     */
    public static void downloadURL(URL url, String filePath, ProgressNotification<Integer> progressNotification) throws IOException {
        // if the given local path is a directory, form a file path with the url
        filePath = generateFinalFilePath(url, filePath);

        URLConnection urlConnection = url.openConnection();
        BufferedInputStream isr = new BufferedInputStream(urlConnection.getInputStream());
        long totalLength = -1;
        if (progressNotification != null) {
            totalLength = urlConnection.getContentLength();
        }
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filePath));

        byte[] buffer = new byte[BUFFER_LENGTH];
        boolean finished = false;
        long currentLength = 0;
        int lastNotificationValue = -1;
        while (!finished) {
            int charsRead = isr.read(buffer);
            if (charsRead == -1) {
                finished = true;
            } else {
                out.write(buffer, 0, charsRead);
                currentLength += charsRead;
                lastNotificationValue = notifyProgress(progressNotification, totalLength, currentLength, lastNotificationValue);
            }
        }
        isr.close();
        out.close();
        if (progressNotification != null) {
            progressNotification.completeTask();
        }
    }

    private static int notifyProgress(ProgressNotification<Integer> progressNotification, long totalLength, long currentLength, int lastNotificationValue) {
        if (progressNotification != null && totalLength != -1) {
            int currentNotificationValue = (int) (100 * currentLength / totalLength);
            if (currentNotificationValue < 0) {
                currentNotificationValue = 0;
            } else if (currentNotificationValue > 100) {
                currentNotificationValue = 100;
            }
            if (currentNotificationValue > lastNotificationValue) {
                progressNotification.addNotification(currentNotificationValue);
                return currentNotificationValue;
            } else {
                return lastNotificationValue;
            }
        }
        return 0;
    }

    /**
     * This method allows performing a concurrent download of a file. The method creates a thread in charge of
     * downloading the file, so it returns immediately. In addition, it returns a DownloadPArt object associated to
     * the download that allows controlling some aspects of the download.
     *
     * @param url      the URL containing the file to download
     * @param filePath the path where the downloaded file must be placed (can be a directory if the URL contains a file)
     * @return a DownloadPart object for controlling the download process
     * @throws IOException if the URL cannot be found or the file cannot be created in the disk
     */
    public static DownloadPart concurrentDownloadURL(URL url, String filePath) throws IOException {
        return concurrentDownloadURL(url, filePath, null, DEFAULT_TIMER_MILLIS);
    }

    /**
     * This method allows performing a concurrent download of a file. The method creates a thread in charge of
     * downloading the file, so it returns immediately. In addition, it returns a DownloadPArt object associated to
     * the download that allows controlling some aspects of the download. Notifications of this download include
     * percentage completed, average speed for the last 5 seconds and exceptions raised during the download.
     *
     * @param url                  the URL containing the file to download
     * @param filePath             the path where the downloaded file must be placed (can be a directory if the URL
     *                             contains a file)
     * @param progressNotification object to notify the progress of the download process. Integer values from 0 to 1000
     *                             will be submitted (indicating the percentage completed), with a double value
     *                             indicating average speed in the last 5 seconds and exceptions raised. A
     *                             completeTask invocation is made when
     *                             the download is completed. A null value for this object means that no progress
     *                             notification is given. Notifications are given each second.
     * @return a DownloadPart object for controlling the download process
     * @throws IOException if the URL cannot be found or the file cannot be created in the disk
     */
    public static DownloadPart concurrentDownloadURL(URL url, String filePath, ProgressNotification<DownloadProgressItem> progressNotification) throws IOException {
        System.out.println(getURLFile(url));
        return concurrentDownloadURL(url, filePath, progressNotification, DEFAULT_TIMER_MILLIS);
    }

    /**
     * This method allows performing a concurrent download of a file. The method creates a thread in charge of
     * downloading the file, so it returns immediately. In addition, it returns a DownloadPArt object associated to
     * the download that allows controlling some aspects of the download. Notifications of this download include
     * percentage completed, average speed for the last 5 seconds and exceptions raised during the download.
     *
     * @param url                  the URL containing the file to download
     * @param filePath             the path where the downloaded file must be placed (can be a directory if the URL
     *                             contains a file)
     * @param progressNotification object to notify the progress of the download process. Integer values from 0 to 1000
     *                             will be submitted (indicating the percentage completed), with a double value
     *                             indicating average speed in the last 5 seconds and exceptions raised. A
     *                             completeTask invocation is made when
     *                             the download is completed. A null value for this object means that no progress
     *                             notification is given
     * @param timerMillis          the time in milliseconds between one notification and another. If zero or
     *                             negative is given, the default value of 1 second is used. Note that a very small
     *                             value can bring performance problems
     * @return a DownloadPart object for controlling the download process
     * @throws IOException if the URL cannot be found or the file cannot be created in the disk
     */
    public static DownloadPart concurrentDownloadURL(URL url, String filePath, ProgressNotification<DownloadProgressItem> progressNotification, long timerMillis) throws IOException {
        // if the given local path is a directory, form a file path with the url
        filePath = generateFinalFilePath(url, filePath);
        if (timerMillis < 1) {
            timerMillis = 1;
        }
        DownloadTask downloadTask = new DownloadTask(url, filePath, progressNotification, timerMillis);
        DownloadPart downloadPart = new DownloadPart(downloadTask);
        ThreadExecutor.submit(downloadTask);
        return downloadPart;
    }

    private static String generateFinalFilePath(URL url, String filePath) throws IOException {
        if (new File(filePath).isDirectory()) {
            String urlFile = getURLFile(url);
            if (urlFile == null || urlFile.length() == 0) {
                throw new IOException("Cannot form a correct local path");
            }
            filePath = Paths.get(filePath, urlFile).toString();
        }
        return filePath;
    }

    private static String getURLFile(URL url) {
        String urlFile = url.getPath();
        if (urlFile == null) {
            return null;
        } else {
            if (urlFile.endsWith("/")) {
                urlFile = urlFile.substring(0, urlFile.length() - 1);
            }
            return urlFile.substring(urlFile.lastIndexOf('/') + 1);
        }
    }
}
