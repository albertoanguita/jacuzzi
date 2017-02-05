package org.aanguita.jacuzzi.io.files.backup;

import org.aanguita.jacuzzi.io.files.RandomAccess;
import org.aanguita.jacuzzi.hash.MD5;
import org.aanguita.jacuzzi.io.serialization.Serializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * API with file backup and CRC checking functionality
 * todo is this used, finished?
 */
public class Backup {

    private static final String DEFAULT_SEPARATOR = "\n-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-CRC_SIGNATURE-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-%-\n";

    public static void addEmbeddedCRC(String path) throws IOException {
        addEmbeddedCRC(path, DEFAULT_SEPARATOR);
    }

    public static void addEmbeddedCRC(String path, String separator) throws IOException {
        addEmbeddedCRC(new File(path), separator);
    }

    public static void addEmbeddedCRC(File file) throws IOException {
        addEmbeddedCRC(file, DEFAULT_SEPARATOR);
    }

    public static void addEmbeddedCRC(File file, String separator) throws IOException {
        // read the file contents and add the CRC part
        separator = fixSeparator(separator);
        MD5 md5 = new MD5();
        byte[] crc = md5.digest(file);
        byte[] crcExtension = Serializer.addArrays(separator.getBytes(), crc);
        RandomAccess.append(file.toPath(), crcExtension);
        if (!checkEmbeddedCRC(file, separator)) {
            // remove CRC and start again
            removeEmbeddedCRC(file, separator);
            addEmbeddedCRC(file, separator);
        }
    }

    private static String fixSeparator(String separator) {
        if (!separator.startsWith("\n")) {
            separator = "\n" + separator;
        }
        if (!separator.endsWith("\n")) {
            separator = separator + "\n";
        }
        return separator;
    }

    public static boolean checkEmbeddedCRC(File file, String separator) throws IOException {
        // read lines until we find the separator (keep digesting)
        // then read rest of file and compare with digest
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
//            result.add(line);
            line = reader.readLine();
        }
        reader.close();
        return true;
    }

    public static void removeEmbeddedCRC(File file, String separator) {

    }
}
