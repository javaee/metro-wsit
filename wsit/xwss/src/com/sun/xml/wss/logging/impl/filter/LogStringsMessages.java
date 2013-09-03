
package com.sun.xml.wss.logging.impl.filter;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.filter.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1414_ERROR_EXTRACTING_SYMMETRICKEY(Object arg0) {
        return messageFactory.getMessage("WSS1414.error.extracting.symmetrickey", arg0);
    }

    /**
     * WSS1414: Error extracting symmetric key {0}
     * 
     */
    public static String WSS_1414_ERROR_EXTRACTING_SYMMETRICKEY(Object arg0) {
        return localizer.localize(localizableWSS_1414_ERROR_EXTRACTING_SYMMETRICKEY(arg0));
    }

    public static Localizable localizableWSS_1424_INVALID_USERNAME_TOKEN() {
        return messageFactory.getMessage("WSS1424.invalid.username.token");
    }

    /**
     * WSS1424: Password for the username was obtained as NULL.
     * 
     */
    public static String WSS_1424_INVALID_USERNAME_TOKEN() {
        return localizer.localize(localizableWSS_1424_INVALID_USERNAME_TOKEN());
    }

    public static Localizable localizableWSS_1410_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS1410.diag.check.1");
    }

    /**
     * Check that a Username has been passed through the configuration file or through the callback handler
     * 
     */
    public static String WSS_1410_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_1410_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_1400_NOUSERNAME_FOUND() {
        return messageFactory.getMessage("WSS1400.nousername.found");
    }

    /**
     * WSS1400: No Username token found ,Receiver requirement not met
     * 
     */
    public static String WSS_1400_NOUSERNAME_FOUND() {
        return localizer.localize(localizableWSS_1400_NOUSERNAME_FOUND());
    }

    public static Localizable localizableWSS_1408_FAILED_SENDER_AUTHENTICATION() {
        return messageFactory.getMessage("WSS1408.failed.sender.authentication");
    }

    /**
     * WSS1408: UsernameToken Authentication Failed
     * 
     */
    public static String WSS_1408_FAILED_SENDER_AUTHENTICATION() {
        return localizer.localize(localizableWSS_1408_FAILED_SENDER_AUTHENTICATION());
    }

    public static Localizable localizableWSS_1410_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS1410.diag.cause.1");
    }

    /**
     * Username is not set
     * 
     */
    public static String WSS_1410_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_1410_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_1417_EXCEPTION_PROCESSING_SIGNATURE(Object arg0) {
        return messageFactory.getMessage("WSS1417.exception.processing.signature", arg0);
    }

    /**
     * WSS1417: Error while processing signature {0}
     * 
     */
    public static String WSS_1417_EXCEPTION_PROCESSING_SIGNATURE(Object arg0) {
        return localizer.localize(localizableWSS_1417_EXCEPTION_PROCESSING_SIGNATURE(arg0));
    }

    public static Localizable localizableWSS_1411_UNABLETO_DUMP_SOAPMESSAGE(Object arg0) {
        return messageFactory.getMessage("WSS1411.unableto.dump.soapmessage", arg0);
    }

    /**
     * WSS1411: Unable to dump message {0}
     * 
     */
    public static String WSS_1411_UNABLETO_DUMP_SOAPMESSAGE(Object arg0) {
        return localizer.localize(localizableWSS_1411_UNABLETO_DUMP_SOAPMESSAGE(arg0));
    }

    public static Localizable localizableWSS_1407_NOTMET_NONONCE() {
        return messageFactory.getMessage("WSS1407.notmet.nononce");
    }

    /**
     * WSS1407: Receiver Requirement for no nonce has not been met, Received token has a nonce specified
     * 
     */
    public static String WSS_1407_NOTMET_NONONCE() {
        return localizer.localize(localizableWSS_1407_NOTMET_NONONCE());
    }

    public static Localizable localizableWSS_1419_UNSUPPORTED_KEYBINDING_SIGNATURE() {
        return messageFactory.getMessage("WSS1419.unsupported.keybinding.signature");
    }

    /**
     * WSS1419: Unsupported KeyBinding for signature
     * 
     */
    public static String WSS_1419_UNSUPPORTED_KEYBINDING_SIGNATURE() {
        return localizer.localize(localizableWSS_1419_UNSUPPORTED_KEYBINDING_SIGNATURE());
    }

    public static Localizable localizableWSS_1413_ERROR_EXTRACTING_CERTIFICATE() {
        return messageFactory.getMessage("WSS1413.error.extracting.certificate");
    }

    /**
     * WSS1413: Error extracting certificate 
     * 
     */
    public static String WSS_1413_ERROR_EXTRACTING_CERTIFICATE() {
        return localizer.localize(localizableWSS_1413_ERROR_EXTRACTING_CERTIFICATE());
    }

    public static Localizable localizableWSS_1421_NO_DEFAULT_X_509_CERTIFICATE_PROVIDED() {
        return messageFactory.getMessage("WSS1421.no.default.x509certificate.provided");
    }

    /**
     * WSS1421: No Default X509 Certificate was provided
     * 
     */
    public static String WSS_1421_NO_DEFAULT_X_509_CERTIFICATE_PROVIDED() {
        return localizer.localize(localizableWSS_1421_NO_DEFAULT_X_509_CERTIFICATE_PROVIDED());
    }

    public static Localizable localizableWSS_1404_NOTMET_DIGESTED() {
        return messageFactory.getMessage("WSS1404.notmet.digested");
    }

    /**
     * WSS1404: Receiver Requirement for Digested Password has not been met
     * 
     */
    public static String WSS_1404_NOTMET_DIGESTED() {
        return localizer.localize(localizableWSS_1404_NOTMET_DIGESTED());
    }

    public static Localizable localizableWSS_1415_SAML_ASSERTION_NOTSET() {
        return messageFactory.getMessage("WSS1415.saml.assertion.notset");
    }

    /**
     * WSS1415: SAML Assertion not set by CallbackHandler for Encryption Processing
     * 
     */
    public static String WSS_1415_SAML_ASSERTION_NOTSET() {
        return localizer.localize(localizableWSS_1415_SAML_ASSERTION_NOTSET());
    }

    public static Localizable localizableWSS_1410_ERROR_CREATING_USERNAMETOKEN() {
        return messageFactory.getMessage("WSS1410.error.creating.usernametoken");
    }

    /**
     * WSS1410: Username is not set
     * 
     */
    public static String WSS_1410_ERROR_CREATING_USERNAMETOKEN() {
        return localizer.localize(localizableWSS_1410_ERROR_CREATING_USERNAMETOKEN());
    }

    public static Localizable localizableWSS_1406_NOTMET_NONCE() {
        return messageFactory.getMessage("WSS1406.notmet.nonce");
    }

    /**
     * WSS1406: Receiver Requirement for nonce has not been met
     * 
     */
    public static String WSS_1406_NOTMET_NONCE() {
        return localizer.localize(localizableWSS_1406_NOTMET_NONCE());
    }

    public static Localizable localizableWSS_1416_UNSUPPORTED_KEYBINDING() {
        return messageFactory.getMessage("WSS1416.unsupported.keybinding");
    }

    /**
     * WSS1416: Unsupported KeyBinding for X509CertificateBinding
     * 
     */
    public static String WSS_1416_UNSUPPORTED_KEYBINDING() {
        return localizer.localize(localizableWSS_1416_UNSUPPORTED_KEYBINDING());
    }

    public static Localizable localizableWSS_1401_MORETHANONE_USERNAME_FOUND() {
        return messageFactory.getMessage("WSS1401.morethanone.username.found");
    }

    /**
     * WSS1401: More than one Username token found, Receiver requirement not met
     * 
     */
    public static String WSS_1401_MORETHANONE_USERNAME_FOUND() {
        return localizer.localize(localizableWSS_1401_MORETHANONE_USERNAME_FOUND());
    }

    public static Localizable localizableWSS_1418_SAML_INFO_NOTSET() {
        return messageFactory.getMessage("WSS1418.saml.info.notset");
    }

    /**
     * WSS1418: None of SAML Assertion, SAML AuthorityBinding information was set into the Policy by the CallbackHandler
     * 
     */
    public static String WSS_1418_SAML_INFO_NOTSET() {
        return localizer.localize(localizableWSS_1418_SAML_INFO_NOTSET());
    }

    public static Localizable localizableWSS_1412_ERROR_PROCESSING_DYNAMICPOLICY(Object arg0) {
        return messageFactory.getMessage("WSS1412.error.processing.dynamicpolicy", arg0);
    }

    /**
     * WSS1412: Error in processing dynamic policy {0}
     * 
     */
    public static String WSS_1412_ERROR_PROCESSING_DYNAMICPOLICY(Object arg0) {
        return localizer.localize(localizableWSS_1412_ERROR_PROCESSING_DYNAMICPOLICY(arg0));
    }

    public static Localizable localizableWSS_1408_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS1408.diag.cause.1");
    }

    /**
     * Invalid Username/Password pair in token
     * 
     */
    public static String WSS_1408_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_1408_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_1403_IMPORT_USERNAME_TOKEN() {
        return messageFactory.getMessage("WSS1403.import.username.token");
    }

    /**
     * WSS1403: Exception while importing Username Password Token
     * 
     */
    public static String WSS_1403_IMPORT_USERNAME_TOKEN() {
        return localizer.localize(localizableWSS_1403_IMPORT_USERNAME_TOKEN());
    }

    public static Localizable localizableWSS_1405_NOTMET_PLAINTEXT() {
        return messageFactory.getMessage("WSS1405.notmet.plaintext");
    }

    /**
     * WSS1405: Receiver Requirement for Plain-Text Password has not been met, Received token has Password-Digest
     * 
     */
    public static String WSS_1405_NOTMET_PLAINTEXT() {
        return localizer.localize(localizableWSS_1405_NOTMET_PLAINTEXT());
    }

    public static Localizable localizableWSS_1422_UNSUPPORTED_KEYBINDING_ENCRYPTION_POLICY() {
        return messageFactory.getMessage("WSS1422.unsupported.keybinding.EncryptionPolicy");
    }

    /**
     * WSS1422: Unsupported KeyBinding for EncryptionPolicy
     * 
     */
    public static String WSS_1422_UNSUPPORTED_KEYBINDING_ENCRYPTION_POLICY() {
        return localizer.localize(localizableWSS_1422_UNSUPPORTED_KEYBINDING_ENCRYPTION_POLICY());
    }

    public static Localizable localizableWSS_1423_KERBEROS_CONTEXT_NOTSET() {
        return messageFactory.getMessage("WSS1423.kerberos.context.notset");
    }

    /**
     * WSS1423: Kerberos token and keys could not be obtained. Check Kerberos setup.
     * 
     */
    public static String WSS_1423_KERBEROS_CONTEXT_NOTSET() {
        return localizer.localize(localizableWSS_1423_KERBEROS_CONTEXT_NOTSET());
    }

    public static Localizable localizableWSS_1402_ERROR_POSTHOC() {
        return messageFactory.getMessage("WSS1402.error.posthoc");
    }

    /**
     * WSS1402: Internal Error: Called UsernameTokenFilter in POSTHOC Mode
     * 
     */
    public static String WSS_1402_ERROR_POSTHOC() {
        return localizer.localize(localizableWSS_1402_ERROR_POSTHOC());
    }

    public static Localizable localizableWSS_1420_DYNAMIC_POLICY_SIGNATURE(Object arg0) {
        return messageFactory.getMessage("WSS1420.dynamic.policy.signature", arg0);
    }

    /**
     * WSS1420: Error while processing dynamic policy signature {0}
     * 
     */
    public static String WSS_1420_DYNAMIC_POLICY_SIGNATURE(Object arg0) {
        return localizer.localize(localizableWSS_1420_DYNAMIC_POLICY_SIGNATURE(arg0));
    }

    public static Localizable localizableWSS_1409_INVALID_USERNAME_TOKEN() {
        return messageFactory.getMessage("WSS1409.invalid.username.token");
    }

    /**
     * WSS1409: Invalid UsernameToken both nonce and created are absent
     * 
     */
    public static String WSS_1409_INVALID_USERNAME_TOKEN() {
        return localizer.localize(localizableWSS_1409_INVALID_USERNAME_TOKEN());
    }

}
