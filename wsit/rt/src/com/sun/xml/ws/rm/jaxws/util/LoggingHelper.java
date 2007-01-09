/*
 * LoggingHelper.java
 *
 *
 * @author Mike Grogan
 * Created on January 8, 2007, 1:50 PM
 *
 */


import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Constructor;



/**
 * Helper methods for logging.
 */
public class LoggingHelper {
    
    private final Logger logger;
    
    public static final String logRoot = "javax.enterprise.xml.webservices";
    
    public static final String packageRoot = "com\\.sun\\.xml\\.ws";
    
    
    public LoggingHelper(Logger logger) {
        this.logger = logger;
    }
    
    
    /**
     * Throws an Exception of a given type with a given message.  A log
     * entry logged at the specified level with the same message is also
     * written
     *
     * @param exceptionClass The type of exception to throw
     * @param level The logging level to use
     * @param message The message in the Exception and log entry
     * @param stackTrace If true, the stack trace for the exception is logged.. If false
     * the stackTrace is not logged.
     * @throws An instance of the exceptionClass argument
     */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> void throwAndLog(Class<T> exceptionClass,
                                  Level level,
                                  String message,
                                  boolean stackTrace) throws T {
        
        //look for exceptionClass ctors of the form Exception() and Exception(String)
        Constructor oneArg = null;
        Constructor zeroArg = null;
        Constructor[] ctors = exceptionClass.getConstructors();
        
        for (Constructor ctor : ctors) {
            Class[] params = ctor.getParameterTypes();
            if (params.length == 0) {
                zeroArg = ctor;
            } else if (params.length == 1 && params[0].equals(String.class)) {
                oneArg = ctor;
            }
        }
        
        //Construct the exception
        T exception = null;
        
        try {
            if (oneArg != null) {
                //use the 1-arg ctor if possible
                exception = (T)oneArg.newInstance(message);
            } else if (zeroArg == null) {
                exception = (T)zeroArg.newInstance();
            } 
        } catch (Throwable e) {
        } 

         //write the log entry
        if (logger.isLoggable(level)) {
            if (stackTrace && exception != null) {
                logger.log(level, message, exception);
            } else {
                logger.log(level, message);
            }
        }
        
        //throw the exception
        if (exception != null) {
            throw exception;
        }
            
    }
    
    /**
     * Same as throwAndLog(Class, Level, String, boolean) with last argument
     * set to true.
     */
     public <T extends Throwable> void throwAndLog(Class<T> exceptionClass,
                                  Level level,
                                  String message) throws T {
         
         throwAndLog(exceptionClass, level, message, true);
     }
    
    /**
     * Forms a logger name by replacing "com.sun.xml.ws" in the name
     * of the specified class by "javax.enterprise.xml.webservices"
     * 
     * @param clasz The Class to use for forming the logger name
     * @return The logger name
     */
     public static String getLoggerName(Class clasz) {
        return clasz.getName().replaceFirst( packageRoot, logRoot);
    }
    
}