package aanguita.jacuzzi.files.test;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 26-may-2010
 * Time: 18:13:55
 * To change this template use File | Settings | File Templates.
 * todo remove, make proper class test
 */
public class FileUtilTest {

    public static void main(String args[]) {

        //String path = ".\\trunk\\src\\jacuzzi\\util\\files\\test\\fich.txt";
        //String path2 = ".\\trunk\\src\\jacuzzi\\util\\files\\test\\fich3.txt";
        /*String path = "d:\\peli.avi";
        String path2 = "d:\\peli2.avi";

        try {
            FileGenerator.copy(path, path2, false);
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        String absPath = null;
        try {
            absPath = new File("c:\\hola.txt").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
