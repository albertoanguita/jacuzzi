package org.aanguita.jacuzzi.network.test;

import org.aanguita.jacuzzi.network.URLDownloader;
import org.aanguita.jacuzzi.notification.ProgressNotification;

import java.io.IOException;
import java.net.URL;

/**
 *
 */
public class URLDownloaderTest implements ProgressNotification<Integer> {

    public static void main(String args[]) {
        URLDownloaderTest urlDownloaderTest = new URLDownloaderTest();
    }

    public URLDownloaderTest() {
        String url1 = "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/GEOD/E-GEOD-21068/E-GEOD-21068.mageml.tar.gz";
        String url2 = "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/TABM/E-TABM-199/E-TABM-199.mageml.tar.gz";
        String url3 = "http://localhost:8080/pelis/Alex%20Pastor%20-%20La%20ruta%20natural.avi";
        String url4 = "http://localhost:8080/pelis/test.txt";
        String url5 = "http://localhost:8080/pelis/imagen.2.txt";
        String url6 = "http://localhost:8080/pelis/file.txt";

        String url7 = "http://www893.megaupload.com/files/4236df0b713dbb81e39decd8eed5317d/Poker face.mp3";


        // this file produces a number format exception
        String url8 = "ftp://ftp.ebi.ac.uk/pub/databases/microarray/data/experiment/GEOD/E-GEOD-24012/E-GEOD-24012.mageml.tar.gz";


        try {
            URLDownloader.downloadURL(new URL(url8), "cancion.mp3", this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beginTask() {
        System.out.println("Download started...");
    }

    @Override
    public void addNotification(Integer message) {
        System.out.println(message + "%...");
    }

    @Override
    public void completeTask() {
        System.out.println("Download complete!");
    }
}
