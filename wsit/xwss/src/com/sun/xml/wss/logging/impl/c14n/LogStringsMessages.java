
package com.sun.xml.wss.logging.impl.c14n;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.c14n.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1002_ERROR_CANONICALIZING_TEXTPLAIN(Object arg0) {
        return messageFactory.getMessage("WSS1002.error.canonicalizing.textplain", arg0);
    }

    /**
     * WSS1002: Error {0} while canonicalizing Text/Plain attachment
     * 
     */
    public static String WSS_1002_ERROR_CANONICALIZING_TEXTPLAIN(Object arg0) {
        return localizer.localize(localizableWSS_1002_ERROR_CANONICALIZING_TEXTPLAIN(arg0));
    }

    public static Localizable localizableWSS_1000_ERROR_CANONICALIZING(Object arg0) {
        return messageFactory.getMessage("WSS1000.error.canonicalizing", arg0);
    }

    /**
     * WSS1000: Error {0} while canonicalizing MIME attachment
     * 
     */
    public static String WSS_1000_ERROR_CANONICALIZING(Object arg0) {
        return localizer.localize(localizableWSS_1000_ERROR_CANONICALIZING(arg0));
    }

    public static Localizable localizableWSS_1001_ERROR_CANONICALIZING_IMAGE(Object arg0) {
        return messageFactory.getMessage("WSS1001.error.canonicalizing.image", arg0);
    }

    /**
     * WSS1001: Error {0} while canonicalizing Image attachment
     * 
     */
    public static String WSS_1001_ERROR_CANONICALIZING_IMAGE(Object arg0) {
        return localizer.localize(localizableWSS_1001_ERROR_CANONICALIZING_IMAGE(arg0));
    }

}
