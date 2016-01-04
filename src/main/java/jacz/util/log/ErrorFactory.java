package jacz.util.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class allows generating detailed error messages and redirecting them to an error handled implementation
 */
public class ErrorFactory {

    public static void reportError(ErrorHandler errorHandler, String message, Object... data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ps.println("ERROR THROWN");
        ps.println("------------");
        ps.println(new SimpleDateFormat("YYY/MM/dd-HH:mm:ss:SSS").format(new Date()));
        ps.println();
        printError(ps, message, data);
        ps.println("-------------------------------------------------------");
        ps.println("Stack trace:");
        new RuntimeException().printStackTrace(ps);
        errorHandler.errorRaised(baos.toString());
        ps.close();
    }

    private static void printError(PrintStream ps, String message, Object... data) {
        ps.println("Message: " + message);
        ps.println("Data:");
        int i = 0;
        for (Object o : data) {
            ps.println("Data " + i + ": " + o.toString());
        }
    }

}
