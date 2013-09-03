
package com.sun.xml.wss.logging.impl.opt;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.opt.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1614_SAML_SIGNATURE_INVALID() {
        return messageFactory.getMessage("WSS1614.saml.signature.invalid");
    }

    /**
     * WSS1614: The signature in the SAML Assertion is invalid
     * 
     */
    public static String WSS_1614_SAML_SIGNATURE_INVALID() {
        return localizer.localize(localizableWSS_1614_SAML_SIGNATURE_INVALID());
    }

    public static Localizable localizableWSS_1610_ERROR_MARSHALLING_JBOBJECT(Object arg0) {
        return messageFactory.getMessage("WSS1610.error.marshalling.jbobject", arg0);
    }

    /**
     * WSS1610: Error occurred while marshalling {0}
     * 
     */
    public static String WSS_1610_ERROR_MARSHALLING_JBOBJECT(Object arg0) {
        return localizer.localize(localizableWSS_1610_ERROR_MARSHALLING_JBOBJECT(arg0));
    }

    public static Localizable localizableWSS_1613_UNRECOGNIZED_SECURITY_ELEMENT(Object arg0) {
        return messageFactory.getMessage("WSS1613.unrecognized.security.element", arg0);
    }

    /**
     * WSS1613: The element {0} inside security header is not supported
     * 
     */
    public static String WSS_1613_UNRECOGNIZED_SECURITY_ELEMENT(Object arg0) {
        return localizer.localize(localizableWSS_1613_UNRECOGNIZED_SECURITY_ELEMENT(arg0));
    }

    public static Localizable localizableWSS_1612_ERROR_READING_BUFFER() {
        return messageFactory.getMessage("WSS1612.error.reading.buffer");
    }

    /**
     * WSS1612: Error occurred while reading incoming SOAP message from the buffer
     * 
     */
    public static String WSS_1612_ERROR_READING_BUFFER() {
        return localizer.localize(localizableWSS_1612_ERROR_READING_BUFFER());
    }

    public static Localizable localizableWSS_1609_ERROR_SERIALIZING_ELEMENT(Object arg0) {
        return messageFactory.getMessage("WSS1609.error.serializing.element", arg0);
    }

    /**
     * WSS1609: Error while serializing {0} element
     * 
     */
    public static String WSS_1609_ERROR_SERIALIZING_ELEMENT(Object arg0) {
        return localizer.localize(localizableWSS_1609_ERROR_SERIALIZING_ELEMENT(arg0));
    }

    public static Localizable localizableWSS_1603_ERROR_READING_STREAM(Object arg0) {
        return messageFactory.getMessage("WSS1603.error.reading.stream", arg0);
    }

    /**
     * WSS1603: Stream Exception while reading incoming message: {0}
     * 
     */
    public static String WSS_1603_ERROR_READING_STREAM(Object arg0) {
        return localizer.localize(localizableWSS_1603_ERROR_READING_STREAM(arg0));
    }

    public static Localizable localizableWSS_1611_PROBLEM_CACHING() {
        return messageFactory.getMessage("WSS1611.problem.caching");
    }

    /**
     * WSS1611: Error occurred while buffering incoming SOAP message
     * 
     */
    public static String WSS_1611_PROBLEM_CACHING() {
        return localizer.localize(localizableWSS_1611_PROBLEM_CACHING());
    }

    public static Localizable localizableWSS_1601_SSL_NOT_ENABLED() {
        return messageFactory.getMessage("WSS1601.ssl.not.enabled");
    }

    /**
     * WSS1601: Security Requirements not met - Transport binding configured in policy but incoming message was not SSL enabled
     * 
     */
    public static String WSS_1601_SSL_NOT_ENABLED() {
        return localizer.localize(localizableWSS_1601_SSL_NOT_ENABLED());
    }

    public static Localizable localizableWSS_1604_ERROR_DECODING_BASE_64_DATA(Object arg0) {
        return messageFactory.getMessage("WSS1604.error.decoding.base64data", arg0);
    }

    /**
     * WSS1604: Error occurred while decoding Base64 data: {0}
     * 
     */
    public static String WSS_1604_ERROR_DECODING_BASE_64_DATA(Object arg0) {
        return localizer.localize(localizableWSS_1604_ERROR_DECODING_BASE_64_DATA(arg0));
    }

    public static Localizable localizableWSS_1607_ERROR_RSAPUBLIC_KEY() {
        return messageFactory.getMessage("WSS1607.error.rsapublic.key");
    }

    /**
     * WSS1607: Error occurred while constructing RSAPublicKey
     * 
     */
    public static String WSS_1607_ERROR_RSAPUBLIC_KEY() {
        return localizer.localize(localizableWSS_1607_ERROR_RSAPUBLIC_KEY());
    }

    public static Localizable localizableWSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING(Object arg0) {
        return messageFactory.getMessage("WSS1606.error.rsakeyinfo.base64decoding", arg0);
    }

    /**
     * WSS1606: Error occurred while Base64 decoding {0} under RSAKeyInfo
     * 
     */
    public static String WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING(Object arg0) {
        return localizer.localize(localizableWSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING(arg0));
    }

    public static Localizable localizableWSS_1602_SCCANCEL_SECURITY_UNCONFIGURED() {
        return messageFactory.getMessage("WSS1602.sccancel.security.unconfigured");
    }

    /**
     * WSS1602: Security Requirements not met for SecureConversation Cancel Message
     * 
     */
    public static String WSS_1602_SCCANCEL_SECURITY_UNCONFIGURED() {
        return localizer.localize(localizableWSS_1602_SCCANCEL_SECURITY_UNCONFIGURED());
    }

    public static Localizable localizableWSS_1608_ERROR_SECURITY_HEADER() {
        return messageFactory.getMessage("WSS1608.error.security.header");
    }

    /**
     * WSS1608: Error occurred while processing security header
     * 
     */
    public static String WSS_1608_ERROR_SECURITY_HEADER() {
        return localizer.localize(localizableWSS_1608_ERROR_SECURITY_HEADER());
    }

    public static Localizable localizableWSS_1605_ERROR_GENERATING_CERTIFICATE(Object arg0) {
        return messageFactory.getMessage("WSS1605.error.generating.certificate", arg0);
    }

    /**
     * WSS1605: Error generating X509 certificate: {0}
     * 
     */
    public static String WSS_1605_ERROR_GENERATING_CERTIFICATE(Object arg0) {
        return localizer.localize(localizableWSS_1605_ERROR_GENERATING_CERTIFICATE(arg0));
    }

}
