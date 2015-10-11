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

    public interface Parent {

        void error();
    }

    public static Map<String, ErrorLog> registeredLogs;

    private final Parent parent;

    private final PrintStream printStream;


    static {
        registeredLogs = new HashMap<>();
    }

    public static synchronized void registerErrorLog(String name, Parent parent, PrintStream printStream) {
        registeredLogs.put(name, new ErrorLog(parent, printStream));
    }

    public ErrorLog(Parent parent, PrintStream printStream) {
        this.parent = parent;
        this.printStream = printStream;
    }

    public static void reportError(String errorLogName, String message, Object... data) {
        synchronized (ErrorLog.class) {
            if (registeredLogs.containsKey(errorLogName)) {
                ErrorLog errorLog = registeredLogs.get(errorLogName);
                if (errorLog != null) {
                    errorLog.reportError(message, data);
                }
            } else {
                System.err.println("Trying to access an unregistered error log: " + errorLogName);
                printError(System.err, message, data);
                new RuntimeException().printStackTrace();
            }
        }
    }

    private void reportError(String message, Object... data) {
        printStream.println("ERROR THROWN");
        printStream.println("------------");
        printStream.println(new SimpleDateFormat("YYY/MM/dd-HH:mm:ss:SSS").format(new Date()));
        printStream.println();
        printError(printStream, message, data);
        printStream.println("-------------------------------------------------------");
        printStream.println("Stack trace:");
        new RuntimeException().printStackTrace(printStream);
        printStream.close();
        parent.error();
    }

    private static void printError(PrintStream printStream, String message, Object... data) {
        printStream.println("Message: " + message);
        printStream.println("Data:");
        int i = 0;
        for (Object o : data) {
            printStream.println("Data " + i + ": " + o.toString());
        }
    }
}
