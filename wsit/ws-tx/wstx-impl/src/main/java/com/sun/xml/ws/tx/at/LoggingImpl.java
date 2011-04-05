package com.sun.xml.ws.tx.at;


import java.util.logging.Level;
import java.util.logging.Logger;

//import com.sun.istack.internal.localization.LocalizableMessageFactory;
//import com.sun.istack.internal.localization.Localizer;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;

public class LoggingImpl implements Logging {

//  private final static LocalizableMessageFactory messageFactory =
//          new LocalizableMessageFactory("com.sun.xml.ws.tx.at.localization.Localization");
//  private final static Localizer localizer = new Localizer();

    public void log(Logger logger, Class loggerClass, Level level, String msgId, Object args, Throwable t) {
        log(logger, loggerClass, level, msgId, new Object[]{args}, t);
    }

    public void log(Logger logger, Class loggerClass, Level level, String msgId, Object[] args, Throwable t) {
//      String msg = localizer.localize(messageFactory.getMessage(msgId, args));
//      com.sun.istack.logging.Logger.getLogger(loggerClass).log(level, msg, t);
  }

}
