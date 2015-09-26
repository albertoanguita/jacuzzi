package jacz.util.files;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 28-abr-2008<br>
 * Last Modified: 28-abr-2008
 * todo use java.nio.file.Files, as in readBytes. Maybe with the Files class, this class has no longer sense
 */
public class FileReaderWriter {

    private static final int STRINGBUILDER_INIT_CAP = 500;

    private static final int BUFFER_CAP = 1024;

    /**
     * Reads a text file
     *
     * @param path path to the file to be read
     * @return an ArrayList of Strings (the lines of the file)
     * @throws IOException the file does not exist, or there are problems reading the file
     */
    public static ArrayList<String> readTextFileAsList(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        ArrayList<String> result = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            result.add(line);
            line = reader.readLine();
        }
        reader.close();
        return result;
    }

    /**
     * Reads a text file
     *
     * @param path path to the file to be read
     * @return the String content of the file
     * @throws IOException the file does not exist, or there are problems reading the file
     */
    public static String readTextFile(String path) throws IOException {
        StringBuilder fileData;
        fileData = new StringBuilder(STRINGBUILDER_INIT_CAP);
        BufferedReader reader = new BufferedReader(new FileReader(path));
        char[] buf = new char[BUFFER_CAP];
        int numRead;
        while ((numRead = reader.read(buf)) != -1) {
            fileData.append(buf, 0, numRead);
        }
        reader.close();
        return fileData.toString();
    }

    /**
     * Writes a text (String) into a file
     *
     * @param path    path to the file to be written (overwrite if it exists)
     * @param content content to write to the file
     * @throws IOException there are problems writing the file
     */
    public static void writeTextFile(String path, String content) throws IOException {
        FileWriter fstream = new FileWriter(path);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(content);
        out.close();
    }

    public static Object readObject(String path) throws ClassNotFoundException, IOException {
        ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(path));
        Object o = objStream.readObject();
        objStream.close();
        return o;
    }

    public static <S extends Serializable> void writeObject(String path, S o) throws IOException {
        ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(path));
        objStream.writeObject(o);
        objStream.close();
    }

    public static byte[] readBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
//        ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(path));
//        ByteArrayInputStream stream = new ByteArrayInputStream(new FileInputStream(path));
//        Object o = objStream.readObject();
//        objStream.close();
//        return o;
    }

    public static void writeBytes(String path, byte[] data) throws IOException {
        Files.write(Paths.get(path), data);
//        ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(path));
//        objStream.writeObject(o);
//        objStream.close();
    }

    public static <S extends Serializable> int sizeOfObject(S o) {
        // object is temporarily serialized into a byte array, then the number of bytes of that array is counted, giving the size
        // in bytes of the object. The object must be serializable
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(os);
            objStream.writeObject(o);
            objStream.close();
            return os.size();
        } catch (IOException e) {
            // cannot happen, as data is not written to disk
            return -1;
        }
    }

    public static <S extends Serializable> long sizeOfObject(S o, String tempFilePath) throws IOException {
        // object is temporarily serialized into a byte array, then the number of bytes of that array is counted, giving the size
        // in bytes of the object. The object must be serializable
        ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(tempFilePath));
        objStream.writeObject(o);
        objStream.close();
        File file = new File(tempFilePath);
        long length = file.length();
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        return length;
    }
}
