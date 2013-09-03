
package com.sun.xml.wss.logging.impl.misc;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.misc.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1527_X_509_NOT_VALID() {
        return messageFactory.getMessage("WSS1527.X509.notValid");
    }

    /**
     * WSS1527: X509Certificate's Validity Failed.
     * 
     */
    public static String WSS_1527_X_509_NOT_VALID() {
        return localizer.localize(localizableWSS_1527_X_509_NOT_VALID());
    }

    public static Localizable localizableWSS_1507_NO_SAML_CALLBACK_HANDLER() {
        return messageFactory.getMessage("WSS1507.no.SAMLCallbackHandler");
    }

    /**
     * WSS1507: A Required SAML Callback Handler was not specified in configuration : Cannot Populate SAML Assertion
     * 
     */
    public static String WSS_1507_NO_SAML_CALLBACK_HANDLER() {
        return localizer.localize(localizableWSS_1507_NO_SAML_CALLBACK_HANDLER());
    }

    public static Localizable localizableWSS_1506_INVALID_SAML_POLICY() {
        return messageFactory.getMessage("WSS1506.invalid.SAMLPolicy");
    }

    /**
     * WSS1506: SAML Assertion not present in the Policy.
     * 
     */
    public static String WSS_1506_INVALID_SAML_POLICY() {
        return localizer.localize(localizableWSS_1506_INVALID_SAML_POLICY());
    }

    public static Localizable localizableWSS_1525_INVALID_PASSWORD_HANDLER() {
        return messageFactory.getMessage("WSS1525.invalid.passwordHandler");
    }

    /**
     * WSS1525: Password Handler Not Configured properly using Callback and is null.(not configured)
     * 
     */
    public static String WSS_1525_INVALID_PASSWORD_HANDLER() {
        return localizer.localize(localizableWSS_1525_INVALID_PASSWORD_HANDLER());
    }

    public static Localizable localizableWSS_1521_ERROR_GETTING_USER_CLASS() {
        return messageFactory.getMessage("WSS1521.error.getting.userClass");
    }

    /**
     * WSS1512: Could not find User Class
     * 
     */
    public static String WSS_1521_ERROR_GETTING_USER_CLASS() {
        return localizer.localize(localizableWSS_1521_ERROR_GETTING_USER_CLASS());
    }

    public static Localizable localizableWSS_1513_EXCEPTION_VALIDATE_TIMESTAMP() {
        return messageFactory.getMessage("WSS1513.exception.validate.timestamp");
    }

    /**
     * WSS1513: Exception occured in Timestamp validation: An Error occurred while parsing UTC Timestamp into Date format.
     * 
     */
    public static String WSS_1513_EXCEPTION_VALIDATE_TIMESTAMP() {
        return localizer.localize(localizableWSS_1513_EXCEPTION_VALIDATE_TIMESTAMP());
    }

    public static Localizable localizableWSS_1526_FAILEDTO_GETCERTIFICATE() {
        return messageFactory.getMessage("WSS1526.failedto.getcertificate");
    }

    /**
     * WSS1526: IO Exception occured: failed to get certificate from truststore
     * 
     */
    public static String WSS_1526_FAILEDTO_GETCERTIFICATE() {
        return localizer.localize(localizableWSS_1526_FAILEDTO_GETCERTIFICATE());
    }

    public static Localizable localizableWSS_1517_X_509_EXPIRED() {
        return messageFactory.getMessage("WSS1517.X509.expired");
    }

    /**
     * WSS1517: X509Certificate Expired.
     * 
     */
    public static String WSS_1517_X_509_EXPIRED() {
        return localizer.localize(localizableWSS_1517_X_509_EXPIRED());
    }

    public static Localizable localizableWSS_1504_UNSUPPORTED_CALLBACK_TYPE() {
        return messageFactory.getMessage("WSS1504.unsupported.callbackType");
    }

    /**
     * WSS1504: Unsupported Callback Type Encountered
     * 
     */
    public static String WSS_1504_UNSUPPORTED_CALLBACK_TYPE() {
        return localizer.localize(localizableWSS_1504_UNSUPPORTED_CALLBACK_TYPE());
    }

    public static Localizable localizableWSS_1532_EXCEPTION_INSTANTIATING_ALIASSELECTOR() {
        return messageFactory.getMessage("WSS1532.exception.instantiating.aliasselector");
    }

    /**
     * WSS1532: Exception occured while instantiating User supplied AliasSelector
     * 
     */
    public static String WSS_1532_EXCEPTION_INSTANTIATING_ALIASSELECTOR() {
        return localizer.localize(localizableWSS_1532_EXCEPTION_INSTANTIATING_ALIASSELECTOR());
    }

    public static Localizable localizableWSS_1509_FAILED_INIT_TRUSTSTORE() {
        return messageFactory.getMessage("WSS1509.failed.init.truststore");
    }

    /**
     * WSS1509: Failed to initialize Trust store
     * 
     */
    public static String WSS_1509_FAILED_INIT_TRUSTSTORE() {
        return localizer.localize(localizableWSS_1509_FAILED_INIT_TRUSTSTORE());
    }

    public static Localizable localizableWSS_1502_UNSUPPORTED_DIGEST_AUTH() {
        return messageFactory.getMessage("WSS1502.unsupported.digestAuth");
    }

    /**
     * WSS1502: Digest Authentication for Password Request is Not Supported
     * 
     */
    public static String WSS_1502_UNSUPPORTED_DIGEST_AUTH() {
        return localizer.localize(localizableWSS_1502_UNSUPPORTED_DIGEST_AUTH());
    }

    public static Localizable localizableWSS_1511_FAILED_LOCATE_PEER_CERTIFICATE() {
        return messageFactory.getMessage("WSS1511.failed.locate.peerCertificate");
    }

    /**
     * WSS1511: An Error occurred while locating PEER Entity certificate in TrustStore.
     * 
     */
    public static String WSS_1511_FAILED_LOCATE_PEER_CERTIFICATE() {
        return localizer.localize(localizableWSS_1511_FAILED_LOCATE_PEER_CERTIFICATE());
    }

    public static Localizable localizableWSS_1523_ERROR_GETTING_NEW_INSTANCE_CALLBACK_HANDLER() {
        return messageFactory.getMessage("WSS1523.error.getting.newInstance.CallbackHandler");
    }

    /**
     * WSS1523: Error getting new instance of callback handler
     * 
     */
    public static String WSS_1523_ERROR_GETTING_NEW_INSTANCE_CALLBACK_HANDLER() {
        return localizer.localize(localizableWSS_1523_ERROR_GETTING_NEW_INSTANCE_CALLBACK_HANDLER());
    }

    public static Localizable localizableWSS_1518_FAILEDTO_VALIDATE_CERTIFICATE() {
        return messageFactory.getMessage("WSS1518.failedto.validate.certificate");
    }

    /**
     * WSS1518: Failed to validate certificate
     * 
     */
    public static String WSS_1518_FAILEDTO_VALIDATE_CERTIFICATE() {
        return localizer.localize(localizableWSS_1518_FAILEDTO_VALIDATE_CERTIFICATE());
    }

    public static Localizable localizableWSS_1512_FAILED_LOCATE_CERTIFICATE_PRIVATEKEY() {
        return messageFactory.getMessage("WSS1512.failed.locate.certificate.privatekey");
    }

    /**
     * WSS1512: An Error occurred while locating default certificate and privateKey in KeyStore.
     * 
     */
    public static String WSS_1512_FAILED_LOCATE_CERTIFICATE_PRIVATEKEY() {
        return localizer.localize(localizableWSS_1512_FAILED_LOCATE_CERTIFICATE_PRIVATEKEY());
    }

    public static Localizable localizableWSS_1522_ERROR_GETTING_LONG_VALUE() {
        return messageFactory.getMessage("WSS1522.error.getting.longValue");
    }

    /**
     * WSS1522: Error getting long value
     * 
     */
    public static String WSS_1522_ERROR_GETTING_LONG_VALUE() {
        return localizer.localize(localizableWSS_1522_ERROR_GETTING_LONG_VALUE());
    }

    public static Localizable localizableWSS_1533_X_509_SELF_SIGNED_CERTIFICATE_NOT_VALID() {
        return messageFactory.getMessage("WSS1533.X509.SelfSignedCertificate.notValid");
    }

    /**
     * WSS1533: Validation of self signed certificate failed.
     * 
     */
    public static String WSS_1533_X_509_SELF_SIGNED_CERTIFICATE_NOT_VALID() {
        return localizer.localize(localizableWSS_1533_X_509_SELF_SIGNED_CERTIFICATE_NOT_VALID());
    }

    public static Localizable localizableWSS_1519_NO_DIGEST_ALGORITHM() {
        return messageFactory.getMessage("WSS1519.no.digest.algorithm");
    }

    /**
     * WSS1519: Digest algorithm SHA-1 not found
     * 
     */
    public static String WSS_1519_NO_DIGEST_ALGORITHM() {
        return localizer.localize(localizableWSS_1519_NO_DIGEST_ALGORITHM());
    }

    public static Localizable localizableWSS_1528_FAILED_INITIALIZE_KEY_PASSWORD() {
        return messageFactory.getMessage("WSS1528.failed.initialize.key.password");
    }

    /**
     * WSS1528: An Error occurred while obtaining Key Password of the Keystore.
     * 
     */
    public static String WSS_1528_FAILED_INITIALIZE_KEY_PASSWORD() {
        return localizer.localize(localizableWSS_1528_FAILED_INITIALIZE_KEY_PASSWORD());
    }

    public static Localizable localizableWSS_1529_EXCEPTION_IN_CERTSTORE_CALLBACK() {
        return messageFactory.getMessage("WSS1529.exception.in.certstore.callback");
    }

    /**
     * WSS1529: Exception in CertStoreCallback 
     * 
     */
    public static String WSS_1529_EXCEPTION_IN_CERTSTORE_CALLBACK() {
        return localizer.localize(localizableWSS_1529_EXCEPTION_IN_CERTSTORE_CALLBACK());
    }

    public static Localizable localizableWSS_1505_FAILEDTO_GETKEY() {
        return messageFactory.getMessage("WSS1505.failedto.getkey");
    }

    /**
     * WSS1505: IO Exception occured: failed to get key/certificate from keystore (not necesaarily i/o excep)
     * 
     */
    public static String WSS_1505_FAILEDTO_GETKEY() {
        return localizer.localize(localizableWSS_1505_FAILEDTO_GETKEY());
    }

    public static Localizable localizableWSS_1503_UNSUPPORTED_REQUESTTYPE() {
        return messageFactory.getMessage("WSS1503.unsupported.requesttype");
    }

    /**
     * WSS1503: Unsupported Request Type for Password Validation
     * 
     */
    public static String WSS_1503_UNSUPPORTED_REQUESTTYPE() {
        return localizer.localize(localizableWSS_1503_UNSUPPORTED_REQUESTTYPE());
    }

    public static Localizable localizableWSS_1500_INVALID_USERNAME_HANDLER() {
        return messageFactory.getMessage("WSS1500.invalid.usernameHandler");
    }

    /**
     * WSS1500: Username Handler Not Configured properly using Callback and is null. (not cofigured)
     * 
     */
    public static String WSS_1500_INVALID_USERNAME_HANDLER() {
        return localizer.localize(localizableWSS_1500_INVALID_USERNAME_HANDLER());
    }

    public static Localizable localizableWSS_1531_EXCEPTION_INSTANTIATING_CERTSELECTOR() {
        return messageFactory.getMessage("WSS1531.exception.instantiating.certselector");
    }

    /**
     * WSS1531: Exception occured while instantiating User supplied CertSelector
     * 
     */
    public static String WSS_1531_EXCEPTION_INSTANTIATING_CERTSELECTOR() {
        return localizer.localize(localizableWSS_1531_EXCEPTION_INSTANTIATING_CERTSELECTOR());
    }

    public static Localizable localizableWSS_1520_ERROR_GETTING_RAW_CONTENT() {
        return messageFactory.getMessage("WSS1520.error.getting.rawContent");
    }

    /**
     * WSS1520: Error while getting certificate's raw content
     * 
     */
    public static String WSS_1520_ERROR_GETTING_RAW_CONTENT() {
        return localizer.localize(localizableWSS_1520_ERROR_GETTING_RAW_CONTENT());
    }

    public static Localizable localizableWSS_1530_EXCEPTION_IN_CERTSTORE_LOOKUP() {
        return messageFactory.getMessage("WSS1530.exception.in.certstore.lookup");
    }

    /**
     * WSS1530: Exception occured while looking up the CertStore 
     * 
     */
    public static String WSS_1530_EXCEPTION_IN_CERTSTORE_LOOKUP() {
        return localizer.localize(localizableWSS_1530_EXCEPTION_IN_CERTSTORE_LOOKUP());
    }

    public static Localizable localizableWSS_1524_UNABLETO_RESOLVE_URI_WSIT_HOME_NOTSET() {
        return messageFactory.getMessage("WSS1524.unableto.resolve.URI.WSIT_HOME.notset");
    }

    /**
     * WSS1524: The specified config URL in the WSDL could not be resolved because System Property WSIT_HOME was not set
     * 
     */
    public static String WSS_1524_UNABLETO_RESOLVE_URI_WSIT_HOME_NOTSET() {
        return localizer.localize(localizableWSS_1524_UNABLETO_RESOLVE_URI_WSIT_HOME_NOTSET());
    }

    public static Localizable localizableWSS_1508_FAILED_VALIDATE_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS1508.failed.validateSAMLAssertion");
    }

    /**
     * WSS1508: Failed to validate SAML Assertion
     * 
     */
    public static String WSS_1508_FAILED_VALIDATE_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_1508_FAILED_VALIDATE_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_1516_ERROR_CREATION_AHEAD_CURRENT_TIME() {
        return messageFactory.getMessage("WSS1516.error.creationAheadCurrent.time");
    }

    /**
     * WSS1516: The creation time is ahead of the current time
     * 
     */
    public static String WSS_1516_ERROR_CREATION_AHEAD_CURRENT_TIME() {
        return localizer.localize(localizableWSS_1516_ERROR_CREATION_AHEAD_CURRENT_TIME());
    }

    public static Localizable localizableWSS_1514_ERROR_AHEAD_CURRENT_TIME() {
        return messageFactory.getMessage("WSS1514.error.aheadCurrentTime");
    }

    /**
     * WSS1514: The current time is ahead of the expiration time in Timestamp"
     * 
     */
    public static String WSS_1514_ERROR_AHEAD_CURRENT_TIME() {
        return localizer.localize(localizableWSS_1514_ERROR_AHEAD_CURRENT_TIME());
    }

    public static Localizable localizableWSS_1501_NO_PASSWORD_VALIDATOR() {
        return messageFactory.getMessage("WSS1501.no.password.validator");
    }

    /**
     * WSS1501: Password Validator Not Specified in Configuration
     * 
     */
    public static String WSS_1501_NO_PASSWORD_VALIDATOR() {
        return localizer.localize(localizableWSS_1501_NO_PASSWORD_VALIDATOR());
    }

    public static Localizable localizableWSS_1515_ERROR_CURRENT_TIME() {
        return messageFactory.getMessage("WSS1515.error.currentTime");
    }

    /**
     * WSS1515: The creation time is older than currenttime - timestamp-freshness-limit - max-clock-skew"
     * 
     */
    public static String WSS_1515_ERROR_CURRENT_TIME() {
        return localizer.localize(localizableWSS_1515_ERROR_CURRENT_TIME());
    }

    public static Localizable localizableWSS_1510_FAILED_INIT_KEYSTORE() {
        return messageFactory.getMessage("WSS1510.failed.init.keystore");
    }

    /**
     * WSS1510: Failed to initialize Key store
     * 
     */
    public static String WSS_1510_FAILED_INIT_KEYSTORE() {
        return localizer.localize(localizableWSS_1510_FAILED_INIT_KEYSTORE());
    }

}
