package org.aanguita.jacuzzi.network.test;

import org.aanguita.jacuzzi.network.DownloadPart;
import org.aanguita.jacuzzi.network.DownloadProgressItem;
import org.aanguita.jacuzzi.network.URLDownloader;
import org.aanguita.jacuzzi.notification.ProgressNotification;

import java.io.IOException;
import java.net.URL;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 13-ene-2011<br>
 * Last Modified: 13-ene-2011
 */
public class ConcurrentDownloadTest implements ProgressNotification<DownloadProgressItem> {

    public static void main(String args[]) {
        ConcurrentDownloadTest test = new ConcurrentDownloadTest();
    }

    public ConcurrentDownloadTest() {

        String url1 = "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/GEOD/E-GEOD-21068/E-GEOD-21068.mageml.tar.gz";
        String url2 = "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/TABM/E-TABM-199/E-TABM-199.mageml.tar.gz";
        String url3 = "http://localhost:8080/pelis/Alex%20Pastor%20-%20La%20ruta%20natural.avi";
        String url4 = "http://localhost:8080/pelis/test.txt";
        String url5 = "http://localhost:8080/pelis/imagen.2.txt";
        String url6 = "http://localhost:8080/pelis/file.txt";

        String url7 = "http://arma2.co.uk/patchs/ARMA2PMC_Patch_1_01.zip";
        String url8 = "http://arma2.com/";


        try {

            DownloadPart downloadPart = URLDownloader.concurrentDownloadURL(new URL(url7), "D:\\", this);

            //Thread.sleep(5000);
            //System.out.println("cancelled..........");
            //downloadPart.cancel();
            //downloadPart.pause();
            System.out.println("paused..........");

            Thread.sleep(200);
            //downloadPart.resume();
            System.out.println("resumed..........");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beginTask() {
        System.out.println("Download started...");
    }

    @Override
    public void addNotification(DownloadProgressItem message) {
        System.out.println(" - " + message.getPercentage() + "/1000 completed, speed: " + message.getSpeed());
    }

    @Override
    public void completeTask() {
        System.out.println("Download complete!");
    }
}
