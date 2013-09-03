
package com.sun.xml.wss.logging.impl.opt.token;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.opt.token.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1801_BST_CREATION_FAILED() {
        return messageFactory.getMessage("WSS1801.bst.creation.failed");
    }

    /**
     * WSS1801: Error occurred while constructing BinarySecurityToken.
     * 
     */
    public static String WSS_1801_BST_CREATION_FAILED() {
        return localizer.localize(localizableWSS_1801_BST_CREATION_FAILED());
    }

    public static Localizable localizableWSS_1821_INVALID_DKT_TOKEN() {
        return messageFactory.getMessage("WSS1821.invalid.dkt.token");
    }

    /**
     * WSS1821: Invalid DerivedKey Token. Offset and Generation both are specified.
     * 
     */
    public static String WSS_1821_INVALID_DKT_TOKEN() {
        return localizer.localize(localizableWSS_1821_INVALID_DKT_TOKEN());
    }

    public static Localizable localizableWSS_1851_REFERENCETYPE_X_509_TOKEN(Object arg0) {
        return messageFactory.getMessage("WSS1851.referencetype.x509.token", arg0);
    }

    /**
     * WSS1851: Reference type for X509 Token: {0}
     * 
     */
    public static String WSS_1851_REFERENCETYPE_X_509_TOKEN(Object arg0) {
        return localizer.localize(localizableWSS_1851_REFERENCETYPE_X_509_TOKEN(arg0));
    }

    public static Localizable localizableWSS_1806_ERROR_GENERATING_SYMMETRIC_KEY() {
        return messageFactory.getMessage("WSS1806.error.generating.symmetric.key");
    }

    /**
     * WSS1806: Error in generating symmetric keys. The algorithm provided was incorrect.
     * 
     */
    public static String WSS_1806_ERROR_GENERATING_SYMMETRIC_KEY() {
        return localizer.localize(localizableWSS_1806_ERROR_GENERATING_SYMMETRIC_KEY());
    }

    public static Localizable localizableWSS_1817_ERROR_REFERENCE_CANWRITER(Object arg0) {
        return messageFactory.getMessage("WSS1817.error.reference.canwriter", arg0);
    }

    /**
     * WSS1817: Error occurred while writing {0} to canonicalized writer
     * 
     */
    public static String WSS_1817_ERROR_REFERENCE_CANWRITER(Object arg0) {
        return localizer.localize(localizableWSS_1817_ERROR_REFERENCE_CANWRITER(arg0));
    }

    public static Localizable localizableWSS_1807_CERT_PROOF_KEY_NULL_ISSUEDTOKEN() {
        return messageFactory.getMessage("WSS1807.cert.proofKey.null.issuedtoken");
    }

    /**
     * WSS1807: Requestor Certificate and Proof Key are both null for Issued Token
     * 
     */
    public static String WSS_1807_CERT_PROOF_KEY_NULL_ISSUEDTOKEN() {
        return localizer.localize(localizableWSS_1807_CERT_PROOF_KEY_NULL_ISSUEDTOKEN());
    }

    public static Localizable localizableWSS_1810_NULL_PRIVATEKEY_SAML() {
        return messageFactory.getMessage("WSS1810.null.privatekey.saml");
    }

    /**
     * WSS1810: Private key is set to null inside the private key binding for SAML policy used for Signature
     * 
     */
    public static String WSS_1810_NULL_PRIVATEKEY_SAML() {
        return localizer.localize(localizableWSS_1810_NULL_PRIVATEKEY_SAML());
    }

    public static Localizable localizableWSS_1814_ERROR_ENCODING_CERTIFICATE() {
        return messageFactory.getMessage("WSS1814.error.encoding.certificate");
    }

    /**
     * WSS1814: Error occurred while encoding X509 Certificate
     * 
     */
    public static String WSS_1814_ERROR_ENCODING_CERTIFICATE() {
        return localizer.localize(localizableWSS_1814_ERROR_ENCODING_CERTIFICATE());
    }

    public static Localizable localizableWSS_1823_KEY_PAIR_PROOF_KEY_NULL_ISSUEDTOKEN() {
        return messageFactory.getMessage("WSS1823.keyPair.proofKey.null.issuedtoken");
    }

    /**
     * WSS1823: Proof Key and RSA KeyPair for Supporting token (KeyValueToken or RsaToken) are both null for Issued Token
     * 
     */
    public static String WSS_1823_KEY_PAIR_PROOF_KEY_NULL_ISSUEDTOKEN() {
        return localizer.localize(localizableWSS_1823_KEY_PAIR_PROOF_KEY_NULL_ISSUEDTOKEN());
    }

    public static Localizable localizableWSS_1852_KEY_IDENTIFIER_EMPTY() {
        return messageFactory.getMessage("WSS1852.keyIdentifier.empty");
    }

    /**
     * WSS1852: KeyIdentifier value cannot be empty. Possible cause, certificate version being used does not support SubjectKeyIdentifier.
     * 
     */
    public static String WSS_1852_KEY_IDENTIFIER_EMPTY() {
        return localizer.localize(localizableWSS_1852_KEY_IDENTIFIER_EMPTY());
    }

    public static Localizable localizableWSS_1815_ERROR_PROCESSING_STR() {
        return messageFactory.getMessage("WSS1815.error.processing.str");
    }

    /**
     * WSS1815: Error while processing SecurityTokenReference in incoming message
     * 
     */
    public static String WSS_1815_ERROR_PROCESSING_STR() {
        return localizer.localize(localizableWSS_1815_ERROR_PROCESSING_STR());
    }

    public static Localizable localizableWSS_1813_UNSUPPORTED_EMBEDDEDREFERENCETYPE_SAML() {
        return messageFactory.getMessage("WSS1813.unsupported.embeddedreferencetype.saml");
    }

    /**
     * WSS1813: Embedded Reference Type for SAML Assertions not supported yet
     * 
     */
    public static String WSS_1813_UNSUPPORTED_EMBEDDEDREFERENCETYPE_SAML() {
        return localizer.localize(localizableWSS_1813_UNSUPPORTED_EMBEDDEDREFERENCETYPE_SAML());
    }

    public static Localizable localizableWSS_1822_KERBEROS_ALWAYS_NOTALLOWED() {
        return messageFactory.getMessage("WSS1822.kerberos.always.notallowed");
    }

    /**
     * WSS1822: IncludeToken Always and AlwaysToRecipient not allowed for Kerberos Tokens.
     * 
     */
    public static String WSS_1822_KERBEROS_ALWAYS_NOTALLOWED() {
        return localizer.localize(localizableWSS_1822_KERBEROS_ALWAYS_NOTALLOWED());
    }

    public static Localizable localizableWSS_1804_WRONG_ENCRYPTED_KEY() {
        return messageFactory.getMessage("WSS1804.wrong.encrypted.key");
    }

    /**
     * WSS1804: The length of encryptedKey Id is 0
     * 
     */
    public static String WSS_1804_WRONG_ENCRYPTED_KEY() {
        return localizer.localize(localizableWSS_1804_WRONG_ENCRYPTED_KEY());
    }

    public static Localizable localizableWSS_1811_NULL_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS1811.null.saml.assertion");
    }

    /**
     * WSS1811: SAML assertion is set to null for SAML Binding used for Signature
     * 
     */
    public static String WSS_1811_NULL_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_1811_NULL_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_1809_SCT_NOT_FOUND() {
        return messageFactory.getMessage("WSS1809.sct.not.found");
    }

    /**
     * WSS1809: SecureConversation token not found in the session.
     * 
     */
    public static String WSS_1809_SCT_NOT_FOUND() {
        return localizer.localize(localizableWSS_1809_SCT_NOT_FOUND());
    }

    public static Localizable localizableWSS_1816_ERROR_REFERENCE_MECHANISM(Object arg0) {
        return messageFactory.getMessage("WSS1816.error.reference.mechanism", arg0);
    }

    /**
     * WSS1816: Error occurred while resolving {0}
     * 
     */
    public static String WSS_1816_ERROR_REFERENCE_MECHANISM(Object arg0) {
        return localizer.localize(localizableWSS_1816_ERROR_REFERENCE_MECHANISM(arg0));
    }

    public static Localizable localizableWSS_1820_ERROR_NONCE_DERIVEDKEY(Object arg0) {
        return messageFactory.getMessage("WSS1820.error.nonce.derivedkey", arg0);
    }

    /**
     * WSS1820: Error occurred while decoding nonce for DerivedKey with ID: {0}
     * 
     */
    public static String WSS_1820_ERROR_NONCE_DERIVEDKEY(Object arg0) {
        return localizer.localize(localizableWSS_1820_ERROR_NONCE_DERIVEDKEY(arg0));
    }

    public static Localizable localizableWSS_1802_WRONG_TOKENINCLUSION_POLICY() {
        return messageFactory.getMessage("WSS1802.wrong.tokeninclusion.policy");
    }

    /**
     * WSS1802: IncludeToken policy is Never and WSSAssertion has KeyIdentifier/Thumbprint reference types set to false
     * 
     */
    public static String WSS_1802_WRONG_TOKENINCLUSION_POLICY() {
        return localizer.localize(localizableWSS_1802_WRONG_TOKENINCLUSION_POLICY());
    }

    public static Localizable localizableWSS_1818_ALGORITHM_NOTSET_DERIVEDKEY() {
        return messageFactory.getMessage("WSS1818.algorithm.notset.derivedkey");
    }

    /**
     * WSS1818: Algorithm not set for deriving key
     * 
     */
    public static String WSS_1818_ALGORITHM_NOTSET_DERIVEDKEY() {
        return localizer.localize(localizableWSS_1818_ALGORITHM_NOTSET_DERIVEDKEY());
    }

    public static Localizable localizableWSS_1805_DERIVEDKEYS_WITH_ASYMMETRICBINDING_UNSUPPORTED() {
        return messageFactory.getMessage("WSS1805.derivedkeys.with.asymmetricbinding.unsupported");
    }

    /**
     * WSS1805: Asymmetric Binding with DerivedKeys under X509Token Policy Not Yet Supported
     * 
     */
    public static String WSS_1805_DERIVEDKEYS_WITH_ASYMMETRICBINDING_UNSUPPORTED() {
        return localizer.localize(localizableWSS_1805_DERIVEDKEYS_WITH_ASYMMETRICBINDING_UNSUPPORTED());
    }

    public static Localizable localizableWSS_1808_ID_NOTSET_ENCRYPTED_ISSUEDTOKEN() {
        return messageFactory.getMessage("WSS1808.id.notset.encrypted.issuedtoken");
    }

    /**
     * WSS1808: Id attribute not set on Encrypted IssuedToken
     * 
     */
    public static String WSS_1808_ID_NOTSET_ENCRYPTED_ISSUEDTOKEN() {
        return localizer.localize(localizableWSS_1808_ID_NOTSET_ENCRYPTED_ISSUEDTOKEN());
    }

    public static Localizable localizableWSS_1853_REFERENCETYPE_KERBEROS_TOKEN(Object arg0) {
        return messageFactory.getMessage("WSS1853.referencetype.kerberos.token", arg0);
    }

    /**
     * WSS1853: Reference type for Kerberos Token: {0}
     * 
     */
    public static String WSS_1853_REFERENCETYPE_KERBEROS_TOKEN(Object arg0) {
        return localizer.localize(localizableWSS_1853_REFERENCETYPE_KERBEROS_TOKEN(arg0));
    }

    public static Localizable localizableWSS_1803_UNSUPPORTED_REFERENCE_TYPE(Object arg0) {
        return messageFactory.getMessage("WSS1803.unsupported.reference.type", arg0);
    }

    /**
     * WSS1803: The reference type {0} is not supported
     * 
     */
    public static String WSS_1803_UNSUPPORTED_REFERENCE_TYPE(Object arg0) {
        return localizer.localize(localizableWSS_1803_UNSUPPORTED_REFERENCE_TYPE(arg0));
    }

    public static Localizable localizableWSS_1819_ERROR_SYMMKEY_DERIVEDKEY() {
        return messageFactory.getMessage("WSS1819.error.symmkey.derivedkey");
    }

    /**
     * WSS1819: Error occurred while generating SymmetricKey for DerivedKeyToken
     * 
     */
    public static String WSS_1819_ERROR_SYMMKEY_DERIVEDKEY() {
        return localizer.localize(localizableWSS_1819_ERROR_SYMMKEY_DERIVEDKEY());
    }

    public static Localizable localizableWSS_1812_MISSING_CERT_SAMLASSERTION() {
        return messageFactory.getMessage("WSS1812.missing.cert.samlassertion");
    }

    /**
     * WSS1812: Could not locate Certificate corresponding to Key in SubjectConfirmation of SAML Assertion
     * 
     */
    public static String WSS_1812_MISSING_CERT_SAMLASSERTION() {
        return localizer.localize(localizableWSS_1812_MISSING_CERT_SAMLASSERTION());
    }

}
