package jacz.util.logger;

import java.io.PrintStream;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.HashMap;

/**
 * User: alberto
 * Date: 19-sep-2005
 * Time: 14:23:49
 * To change this template use File | Settings | File Templates.
 */
public class Logger {

    // constantes
    private static final boolean notLog = false;
    private static PrintStream out = System.out;
    private static final boolean showTime = true;
    private static final boolean showDeltaTime = false;

    private static final String TIME_INIT = "[";
    private static final String TIME_SEPARATOR = ":";
    private static final String TIME_FINALIZER = "] - ";

    private static final String DELTATIME_INIT = "[";
    private static final String DELTATIME_SEPARATOR = ":";
    private static final String DELTATIME_FINALIZER = "] - ";

    private static final String TEXT_SAVETIME = "Saved time registry: ";
    private static final String TEXT_LOADTIME_1 = "Loaded time registry: ";
    private static final String TEXT_LOADTIME_2 = ". Delta: ";

    // almacena el momento de la ultima llamada
    private static long lastNanoTime;

    // almacena instantes de llamadas registradas con nombre
    private static HashMap<String, Long> timeRegister;

    // almacena si ya ha sido inicializado
    private static boolean inicializado = false;

    // instancia de la clase Singleton
    //static private Logger instance = null;


/*
    private Logger() {
    }


    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
*/

    public static void initialize() {
        lastNanoTime = System.nanoTime();
        timeRegister = new HashMap<String, Long>();
        inicializado = true;
        println("###Logger inicializado###");
    }


    public static void saveTime(String name) {
        long time = System.nanoTime();
        timeRegister.put(name, time);
        println(TEXT_SAVETIME + name);
    }


    public static void loadTime(String name) {
        if (timeRegister.containsKey(name)) {
            long oldTime = (Long) timeRegister.get(name);
            long newTime = System.nanoTime();
            println(TEXT_LOADTIME_1 + name + TEXT_LOADTIME_2 + (newTime - oldTime));
            timeRegister.put(name, newTime);
        }
    }

    public static void removeTime(String name) {
        timeRegister.remove(name);
    }


    private static void showTime() {
        // shows current time (format is "[HH:MM:SS:MIL] - ")
        GregorianCalendar gc = new GregorianCalendar();
        out.print(TIME_INIT + gc.get(Calendar.HOUR_OF_DAY) + TIME_SEPARATOR + gc.get(Calendar.MINUTE) + TIME_SEPARATOR + gc.get(Calendar.SECOND) + TIME_SEPARATOR + gc.get(Calendar.MILLISECOND) + TIME_FINALIZER);
    }

    private static void showDeltaTime() {
        // shows time diff with last call
        long newNanoTime = System.nanoTime();
        out.print(DELTATIME_INIT + (newNanoTime - lastNanoTime) + DELTATIME_FINALIZER);
        lastNanoTime = newNanoTime;
    }

    public static void print(String text) {
        if (notLog) {
            return;
        }
        if (showTime) {
            showTime();
        }
        if (showDeltaTime) {
            showDeltaTime();
        }
        out.print(text);
    }

    public static void println() {
        if (notLog) {
            return;
        }
        out.println();
    }

    public static void println(String text) {
        if (notLog) {
            return;
        }
        print(text);
        println();
    }
}
