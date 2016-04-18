package jacz.util.log;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class logs error and reports to some specified class for closing resources
 * <p/>
 * We statically register ErrorLog objects, indexed by their name. This way, we can independently register several
 * error logs belonging to different environments
 */
public class ErrorLog {

    public static Map<String, ErrorLog> registeredLogs;

    private final PrintStream printStream;


    static {
        registeredLogs = new HashMap<>();
    }

    public static synchronized void registerErrorLog(String name, PrintStream printStream) {
        registeredLogs.put(name, new ErrorLog(printStream));
    }

    public ErrorLog(PrintStream printStream) {
        this.printStream = printStream;
    }

    public static void reportError(String errorLogName, String message, Object... data) {
        synchronized (ErrorLog.class) {
            if (!registeredLogs.containsKey(errorLogName)) {
                registerErrorLog(errorLogName, System.err);
            }
            registeredLogs.get(errorLogName).reportError(message, data);
        }
    }

    private void reportError(String message, Object... data) {
        printStream.println("ERROR THROWN");
        printStream.println("------------");
        printStream.println("Thread: " + Thread.currentThread().getName());
        printStream.println(new SimpleDateFormat("YYY/MM/dd-HH:mm:ss:SSS").format(new Date()));
        printStream.println();
        printError(printStream, message, data);
        printStream.println("-------------------------------------------------------");
        printStream.println("Stack trace:");
        new RuntimeException().printStackTrace(printStream);
        printStream.close();
    }

    private static void printError(PrintStream printStream, String message, Object... data) {
        printStream.println("Message: " + message);
        printStream.println("Data:");
        int i = 0;
        for (Object o : data) {
            printStream.println("Data " + i + ": " + o.toString());
            i++;
        }
    }
}
