package aanguita.jacuzzi.files.test;

import aanguita.jacuzzi.files.FileReaderWriter;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Alberto
 * Date: 11/04/14
 * Time: 16:14
 * To change this template use File | Settings | File Templates.
 */
public class FileReaderWriterTest {

    public static void main(String[] args) {


        try {
            Integer i = 5;
            FileReaderWriter.writeObject("./object.txt", i);
            Integer i2 = (Integer) FileReaderWriter.readObject("./object.txt");

            System.out.println(i2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
