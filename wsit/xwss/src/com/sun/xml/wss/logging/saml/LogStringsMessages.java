
package com.sun.xml.wss.logging.saml;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.saml.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_001_SAML_ASSERTION_NOT_FOUND(Object arg0) {
        return messageFactory.getMessage("WSS001.SAML_ASSERTION_NOT_FOUND", arg0);
    }

    /**
     * WSS_SAML001: No SAML Assertion found with AssertionID: {0}
     * 
     */
    public static String WSS_001_SAML_ASSERTION_NOT_FOUND(Object arg0) {
        return localizer.localize(localizableWSS_001_SAML_ASSERTION_NOT_FOUND(arg0));
    }

    public static Localizable localizableWSS_003_FAILEDTO_MARSHAL() {
        return messageFactory.getMessage("WSS003.failedto.marshal");
    }

    /**
     * WSS003: Exception occured while trying to Marshal
     * 
     */
    public static String WSS_003_FAILEDTO_MARSHAL() {
        return localizer.localize(localizableWSS_003_FAILEDTO_MARSHAL());
    }

    public static Localizable localizableWSS_002_FAILED_CREATE_DOCUMENT() {
        return messageFactory.getMessage("WSS002.failed.create.document");
    }

    /**
     * WSS002: Unable to create Document
     * 
     */
    public static String WSS_002_FAILED_CREATE_DOCUMENT() {
        return localizer.localize(localizableWSS_002_FAILED_CREATE_DOCUMENT());
    }

}
