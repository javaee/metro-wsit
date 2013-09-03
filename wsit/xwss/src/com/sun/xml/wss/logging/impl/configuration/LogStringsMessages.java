
package com.sun.xml.wss.logging.impl.configuration;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.configuration.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1100_CLASSCAST_TARGET(Object arg0) {
        return messageFactory.getMessage("WSS1100.classcast.target", arg0);
    }

    /**
     * WS1100: Classcast Exception for Target {0}
     * 
     */
    public static String WSS_1100_CLASSCAST_TARGET(Object arg0) {
        return localizer.localize(localizableWSS_1100_CLASSCAST_TARGET(arg0));
    }

}
