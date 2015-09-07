package jacz.util.hash.hashdb.test;

import jacz.util.hash.hashdb.FileHashDatabase;

import java.io.IOException;
import java.util.Map;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 9/11/12<br>
 * Last Modified: 9/11/12
 */
public class TestFileHashDatabase {


    
    public static void main(String args[]) {

        FileHashDatabase fhd = new FileHashDatabase();

        try {
            fhd.put(".\\trunk\\etc\\testFileHashDatabase\\a.jpg");
            fhd.put(".\\trunk\\etc\\testFileHashDatabase\\b.jpg");
            fhd.put(".\\trunk\\etc\\testFileHashDatabase\\c.jpg");
            fhd.put(".\\trunk\\etc\\testFileHashDatabase\\d.jpg");
            fhd.put(".\\trunk\\etc\\testFileHashDatabase\\e.jpg");
            //fhd.putDeleteIfExists(".\\trunk\\etc\\testFileHashDatabase\\aa.jpg");
//            Map<String, String> wrongEntries = fhd.deepAnalysis();

            System.out.println("FIN");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
