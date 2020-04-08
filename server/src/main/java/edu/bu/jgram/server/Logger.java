package edu.bu.jgram.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents the logging class. In typical conditions we don't need a abstraction of logging i.e. we could directly
 * use the logging library, but in our case we want to start with basic logging and eventually enhance it.
 * So abstracting the implementation will enhance future maintainability.
 */
public class Logger {
    private static Logger instance = null;
    private static Object mutex = new Object();

    private Logger() {
    }

    public static Logger getInstance() {
        Logger result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new Logger();
            }
        }
        return result;
    }

    synchronized public void info(String message) {
        System.out.println(String.format("%s : %s : %s", getCurrentTimeStamp(), "INFO", message));
    }

    synchronized public void warn(String message) {
        System.out.println(String.format("%s : %s : %s", getCurrentTimeStamp(), "WARN", message));
    }

    synchronized public void error(String message, Exception e) {
        System.err.println(String.format("%s : %s : %s", getCurrentTimeStamp(), "ERROR", message));
        System.err.println(e);
    }

    synchronized public void fatal(String message, Exception e) {
        System.err.println(String.format("%s : %s : %s", getCurrentTimeStamp(), "FATAL", message));
        System.err.println(e);
    }

    synchronized public void fatal(String message) {
        System.err.println(String.format("%s : %s : %s", getCurrentTimeStamp(), "FATAL", message));
    }

    private String getCurrentTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
