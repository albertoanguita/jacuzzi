package jacz.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 *
 */
public class IOUtil {

    public static void pauseEnter() {
        pauseEnter("");
    }

    public static void pauseEnter(String message) {
        pauseEnter(message, System.out);
    }

    public static void pauseEnter(String message, PrintStream printStream) {
        printStream.print(message);
        try {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
