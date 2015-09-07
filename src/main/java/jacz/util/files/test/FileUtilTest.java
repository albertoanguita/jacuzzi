package jacz.util.files.test;

import jacz.util.files.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 26-may-2010
 * Time: 18:13:55
 * To change this template use File | Settings | File Templates.
 */
public class FileUtilTest {

    public static void main(String args[]) {

        //String path = ".\\trunk\\src\\jacuzzi\\util\\files\\test\\fich.txt";
        //String path2 = ".\\trunk\\src\\jacuzzi\\util\\files\\test\\fich3.txt";
        /*String path = "d:\\peli.avi";
        String path2 = "d:\\peli2.avi";

        try {
            FileUtil.copy(path, path2, false);
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        String absPath = null;
        try {
            absPath = new File("c:\\hola.txt").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String pathGen = FileUtil.generatePath("\\fichero.txt", "c:", "dir1", "dir2\\dir3\\");


        System.out.println(absPath);


        //String f = "c:\\test\\hola.txt";
        String f = "c:\\hola\\";

        System.out.println(FileUtil.getFileName(f));
        System.out.println(FileUtil.getFileDirectory(f));

        System.out.println(FileUtil.joinPaths("ccc", "jeje"));

        try {
            String[] files = FileUtil.getDirectoryContents("D:\\p-medicine\\periodic reports\\1st report (Feb2011 - Jan2012)");
            for (String file : files) {
                System.out.print(file + ", ");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
