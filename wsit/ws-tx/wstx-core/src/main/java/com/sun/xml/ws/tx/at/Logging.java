package com.sun.xml.ws.tx.at;


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: pparkins
 * Date: Apr 5, 2011
 * Time: 10:33:01 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Logging {
    public void log(Logger logger, Class loggerClass, Level level, String msgId, Object[] args, Throwable t);
    public void log(Logger logger, Class loggerClass, Level level, String msgId, Object args, Throwable t);
}
