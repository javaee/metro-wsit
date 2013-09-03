
package com.sun.xml.wss.logging.impl.opt.signature;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.opt.signature.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1763_ACTUAL_DEGEST_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS1763.actual.degest.value", arg0);
    }

    /**
     * WSS1763: Actual digest value is:{0}
     * 
     */
    public static String WSS_1763_ACTUAL_DEGEST_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_1763_ACTUAL_DEGEST_VALUE(arg0));
    }

    public static Localizable localizableWSS_1751_NUMBER_TARGETS_SIGNATURE(Object arg0) {
        return messageFactory.getMessage("WSS1751.number.targets.signature", arg0);
    }

    /**
     * WSS1751: Number of targets in Signature is: {0}.
     * 
     */
    public static String WSS_1751_NUMBER_TARGETS_SIGNATURE(Object arg0) {
        return localizer.localize(localizableWSS_1751_NUMBER_TARGETS_SIGNATURE(arg0));
    }

    public static Localizable localizableWSS_1711_ERROR_VERIFYING_SIGNATURE() {
        return messageFactory.getMessage("WSS1711.error.verifying.signature");
    }

    /**
     * WSS1711: Error occurred while reading signature for verfication
     * 
     */
    public static String WSS_1711_ERROR_VERIFYING_SIGNATURE() {
        return localizer.localize(localizableWSS_1711_ERROR_VERIFYING_SIGNATURE());
    }

    public static Localizable localizableWSS_1752_SIGNATURE_TARGET_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS1752.signature.target.value", arg0);
    }

    /**
     * WSS1752: Signature Target Value is {0}
     * 
     */
    public static String WSS_1752_SIGNATURE_TARGET_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_1752_SIGNATURE_TARGET_VALUE(arg0));
    }

    public static Localizable localizableWSS_1708_BASE_64_DECODING_ERROR(Object arg0) {
        return messageFactory.getMessage("WSS1708.base64.decoding.error", arg0);
    }

    /**
     * WSS1708: Error occurred while decoding signatureValue for Signature with ID {0}
     * 
     */
    public static String WSS_1708_BASE_64_DECODING_ERROR(Object arg0) {
        return localizer.localize(localizableWSS_1708_BASE_64_DECODING_ERROR(arg0));
    }

    public static Localizable localizableWSS_1701_SIGN_FAILED() {
        return messageFactory.getMessage("WSS1701.sign.failed");
    }

    /**
     * WSS1701: Sign operation failed.
     * 
     */
    public static String WSS_1701_SIGN_FAILED() {
        return localizer.localize(localizableWSS_1701_SIGN_FAILED());
    }

    public static Localizable localizableWSS_1725_REFERENCE_ELEMENT_NOTFOUND() {
        return messageFactory.getMessage("WSS1725.reference.element.notfound");
    }

    /**
     * No Reference Element found in SignedInfo of Signature
     * 
     */
    public static String WSS_1725_REFERENCE_ELEMENT_NOTFOUND() {
        return localizer.localize(localizableWSS_1725_REFERENCE_ELEMENT_NOTFOUND());
    }

    public static Localizable localizableWSS_1702_UNSUPPORTED_USERNAMETOKEN_KEYBINDING() {
        return messageFactory.getMessage("WSS1702.unsupported.usernametoken.keybinding");
    }

    /**
     * WSS1702: UsernameToken as KeyBinding for SignaturePolicy is Not Yet Supported
     * 
     */
    public static String WSS_1702_UNSUPPORTED_USERNAMETOKEN_KEYBINDING() {
        return localizer.localize(localizableWSS_1702_UNSUPPORTED_USERNAMETOKEN_KEYBINDING());
    }

    public static Localizable localizableWSS_1722_ERROR_REFERENCE_VALIDATION(Object arg0) {
        return messageFactory.getMessage("WSS1722.error.reference.validation", arg0);
    }

    /**
     * WSS1722: Error occurred while validating Reference with URI: {0}
     * 
     */
    public static String WSS_1722_ERROR_REFERENCE_VALIDATION(Object arg0) {
        return localizer.localize(localizableWSS_1722_ERROR_REFERENCE_VALIDATION(arg0));
    }

    public static Localizable localizableWSS_1755_MISSINGID_INCOMING_SIGNATURE(Object arg0) {
        return messageFactory.getMessage("WSS1755.missingid.incoming.signature", arg0);
    }

    /**
     * WSS1755: Id not present for Incoming signature. Generated Id: {0} for policy verification
     * 
     */
    public static String WSS_1755_MISSINGID_INCOMING_SIGNATURE(Object arg0) {
        return localizer.localize(localizableWSS_1755_MISSINGID_INCOMING_SIGNATURE(arg0));
    }

    public static Localizable localizableWSS_1712_UNBUFFERED_SIGNATURE_ERROR() {
        return messageFactory.getMessage("WSS1712.unbuffered.signature.error");
    }

    /**
     * WSS1712: Signature is not buffered , message not as per configured policy
     * 
     */
    public static String WSS_1712_UNBUFFERED_SIGNATURE_ERROR() {
        return localizer.localize(localizableWSS_1712_UNBUFFERED_SIGNATURE_ERROR());
    }

    public static Localizable localizableWSS_1760_DIGEST_INIT_ERROR() {
        return messageFactory.getMessage("WSS1760.digest.init.error");
    }

    /**
     * WSS1760: Error occurred when obtaining MessageDigest instance.
     * 
     */
    public static String WSS_1760_DIGEST_INIT_ERROR() {
        return localizer.localize(localizableWSS_1760_DIGEST_INIT_ERROR());
    }

    public static Localizable localizableWSS_1714_UNSUPPORTED_TRANSFORM_ERROR() {
        return messageFactory.getMessage("WSS1714.unsupported.transform.error");
    }

    /**
     * WSS1714: Only EXC14n Transform is supported
     * 
     */
    public static String WSS_1714_UNSUPPORTED_TRANSFORM_ERROR() {
        return localizer.localize(localizableWSS_1714_UNSUPPORTED_TRANSFORM_ERROR());
    }

    public static Localizable localizableWSS_1715_ERROR_CANONICALIZING_BODY() {
        return messageFactory.getMessage("WSS1715.error.canonicalizing.body");
    }

    /**
     * WSS1715: Error occurred while canonicalizing BodyTag
     * 
     */
    public static String WSS_1715_ERROR_CANONICALIZING_BODY() {
        return localizer.localize(localizableWSS_1715_ERROR_CANONICALIZING_BODY());
    }

    public static Localizable localizableWSS_1718_MISSING_CANON_ALGORITHM() {
        return messageFactory.getMessage("WSS1718.missing.canon.algorithm");
    }

    /**
     * WSS1718: Canonicalization Algorithm must be present in SignedInfo
     * 
     */
    public static String WSS_1718_MISSING_CANON_ALGORITHM() {
        return localizer.localize(localizableWSS_1718_MISSING_CANON_ALGORITHM());
    }

    public static Localizable localizableWSS_1762_CALCULATED_DIGEST_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS1762.calculated.digest.value", arg0);
    }

    /**
     * WSS1762: Calculated digest value is:{0}
     * 
     */
    public static String WSS_1762_CALCULATED_DIGEST_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_1762_CALCULATED_DIGEST_VALUE(arg0));
    }

    public static Localizable localizableWSS_1723_UNSUPPORTED_TRANSFORM_ELEMENT(Object arg0) {
        return messageFactory.getMessage("WSS1723.unsupported.transform.element", arg0);
    }

    /**
     * WSS1723: Unsupported Transform element: {0}
     * 
     */
    public static String WSS_1723_UNSUPPORTED_TRANSFORM_ELEMENT(Object arg0) {
        return localizer.localize(localizableWSS_1723_UNSUPPORTED_TRANSFORM_ELEMENT(arg0));
    }

    public static Localizable localizableWSS_1703_UNSUPPORTED_KEYBINDING_SIGNATUREPOLICY(Object arg0) {
        return messageFactory.getMessage("WSS1703.unsupported.keybinding.signaturepolicy", arg0);
    }

    /**
     * WSS1703: Unsupported Key Binding for SignaturePolicy: {0}
     * 
     */
    public static String WSS_1703_UNSUPPORTED_KEYBINDING_SIGNATUREPOLICY(Object arg0) {
        return localizer.localize(localizableWSS_1703_UNSUPPORTED_KEYBINDING_SIGNATUREPOLICY(arg0));
    }

    public static Localizable localizableWSS_1706_ERROR_ENVELOPED_SIGNATURE() {
        return messageFactory.getMessage("WSS1706.error.enveloped.signature");
    }

    /**
     * WSS1706: Error occurred while performing Enveloped Signature
     * 
     */
    public static String WSS_1706_ERROR_ENVELOPED_SIGNATURE() {
        return localizer.localize(localizableWSS_1706_ERROR_ENVELOPED_SIGNATURE());
    }

    public static Localizable localizableWSS_1707_ERROR_PROCESSING_SIGNEDINFO(Object arg0) {
        return messageFactory.getMessage("WSS1707.error.processing.signedinfo", arg0);
    }

    /**
     * WSS1707: Elements under Signature are not as per defined schema or error must have occurred while processing SignedInfo for Signature with ID {0}
     * 
     */
    public static String WSS_1707_ERROR_PROCESSING_SIGNEDINFO(Object arg0) {
        return localizer.localize(localizableWSS_1707_ERROR_PROCESSING_SIGNEDINFO(arg0));
    }

    public static Localizable localizableWSS_1724_SIGTYPE_VERIFICATION_FAILED(Object arg0) {
        return messageFactory.getMessage("WSS1724.sigtype.verification.failed", arg0);
    }

    /**
     * {0} Signature verification failed
     * 
     */
    public static String WSS_1724_SIGTYPE_VERIFICATION_FAILED(Object arg0) {
        return localizer.localize(localizableWSS_1724_SIGTYPE_VERIFICATION_FAILED(arg0));
    }

    public static Localizable localizableWSS_1721_REFERENCE_VALIDATION_FAILED(Object arg0) {
        return messageFactory.getMessage("WSS1721.reference.validation.failed", arg0);
    }

    /**
     * WSS1721: Validation of Reference with URI {0} failed
     * 
     */
    public static String WSS_1721_REFERENCE_VALIDATION_FAILED(Object arg0) {
        return localizer.localize(localizableWSS_1721_REFERENCE_VALIDATION_FAILED(arg0));
    }

    public static Localizable localizableWSS_1720_ERROR_URI_DEREF(Object arg0) {
        return messageFactory.getMessage("WSS1720.error.uri.deref", arg0);
    }

    /**
     * WSS1720: Error occurred while dereferencing Reference: {0}
     * 
     */
    public static String WSS_1720_ERROR_URI_DEREF(Object arg0) {
        return localizer.localize(localizableWSS_1720_ERROR_URI_DEREF(arg0));
    }

    public static Localizable localizableWSS_1761_TRANSFORM_IO_ERROR() {
        return messageFactory.getMessage("WSS1761.transform.io.error");
    }

    /**
     * WSS1761: Error occurred while flushing the outputstream. 
     * 
     */
    public static String WSS_1761_TRANSFORM_IO_ERROR() {
        return localizer.localize(localizableWSS_1761_TRANSFORM_IO_ERROR());
    }

    public static Localizable localizableWSS_1754_TRANSFORM_ALGORITHM(Object arg0) {
        return messageFactory.getMessage("WSS1754.transform.algorithm", arg0);
    }

    /**
     * WSS1754: Transform algorithm used is: {0}
     * 
     */
    public static String WSS_1754_TRANSFORM_ALGORITHM(Object arg0) {
        return localizer.localize(localizableWSS_1754_TRANSFORM_ALGORITHM(arg0));
    }

    public static Localizable localizableWSS_1713_SIGNATURE_VERIFICATION_EXCEPTION(Object arg0) {
        return messageFactory.getMessage("WSS1713.signature.verification.exception", arg0);
    }

    /**
     * WSS1713: Signature verification failed due to: {0}
     * 
     */
    public static String WSS_1713_SIGNATURE_VERIFICATION_EXCEPTION(Object arg0) {
        return localizer.localize(localizableWSS_1713_SIGNATURE_VERIFICATION_EXCEPTION(arg0));
    }

    public static Localizable localizableWSS_1756_CANONICALIZED_SIGNEDINFO_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS1756.canonicalized.signedinfo.value", arg0);
    }

    /**
     * WSS1756: Canonicalized Signed Info: {0}
     * 
     */
    public static String WSS_1756_CANONICALIZED_SIGNEDINFO_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_1756_CANONICALIZED_SIGNEDINFO_VALUE(arg0));
    }

    public static Localizable localizableWSS_1719_ERROR_DIGESTVAL_REFERENCE(Object arg0) {
        return messageFactory.getMessage("WSS1719.error.digestval.reference", arg0);
    }

    /**
     * WSS1719: Signature Reference validation error. Error occurred while decoding digestValue for Reference: {0}
     * 
     */
    public static String WSS_1719_ERROR_DIGESTVAL_REFERENCE(Object arg0) {
        return localizer.localize(localizableWSS_1719_ERROR_DIGESTVAL_REFERENCE(arg0));
    }

    public static Localizable localizableWSS_1705_INVALID_DIGEST_ALGORITHM(Object arg0) {
        return messageFactory.getMessage("WSS1705.invalid.digest.algorithm", arg0);
    }

    /**
     * WSS1705: Invalid digest algorithm {0} specified
     * 
     */
    public static String WSS_1705_INVALID_DIGEST_ALGORITHM(Object arg0) {
        return localizer.localize(localizableWSS_1705_INVALID_DIGEST_ALGORITHM(arg0));
    }

    public static Localizable localizableWSS_1709_UNRECOGNIZED_SIGNATURE_ELEMENT(Object arg0) {
        return messageFactory.getMessage("WSS1709.unrecognized.signature.element", arg0);
    }

    /**
     * WSS1709: Element name {0} is not recognized under signature.
     * 
     */
    public static String WSS_1709_UNRECOGNIZED_SIGNATURE_ELEMENT(Object arg0) {
        return localizer.localize(localizableWSS_1709_UNRECOGNIZED_SIGNATURE_ELEMENT(arg0));
    }

    public static Localizable localizableWSS_1710_SIGNATURE_VERFICATION_FAILED(Object arg0) {
        return messageFactory.getMessage("WSS1710.signature.verfication.failed", arg0);
    }

    /**
     * WSS1710: Signature Verification for Signature with ID {0} failed
     * 
     */
    public static String WSS_1710_SIGNATURE_VERFICATION_FAILED(Object arg0) {
        return localizer.localize(localizableWSS_1710_SIGNATURE_VERFICATION_FAILED(arg0));
    }

    public static Localizable localizableWSS_1757_CANONICALIZED_TARGET_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS1757.canonicalized.target.value", arg0);
    }

    /**
     * WSS1757: Canonicalized target value: {0}
     * 
     */
    public static String WSS_1757_CANONICALIZED_TARGET_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_1757_CANONICALIZED_TARGET_VALUE(arg0));
    }

    public static Localizable localizableWSS_1717_ERROR_PAYLOAD_VERIFICATION() {
        return messageFactory.getMessage("WSS1717.error.payload.verification");
    }

    /**
     * WSS1717: Error occurred while doing digest verification of body/payload
     * 
     */
    public static String WSS_1717_ERROR_PAYLOAD_VERIFICATION() {
        return localizer.localize(localizableWSS_1717_ERROR_PAYLOAD_VERIFICATION());
    }

    public static Localizable localizableWSS_1758_TRANSFORM_INIT() {
        return messageFactory.getMessage("WSS1758.transform.init");
    }

    /**
     * WSS1758: Error occured while initializing the EXC14n canonicalizer, Invalid algorithm parameters were specified.
     * 
     */
    public static String WSS_1758_TRANSFORM_INIT() {
        return localizer.localize(localizableWSS_1758_TRANSFORM_INIT());
    }

    public static Localizable localizableWSS_1764_CANONICALIZED_PAYLOAD_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS1764.canonicalized.payload.value", arg0);
    }

    /**
     * WSS1764: Canonicalized PayLoad is: {0}
     * 
     */
    public static String WSS_1764_CANONICALIZED_PAYLOAD_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_1764_CANONICALIZED_PAYLOAD_VALUE(arg0));
    }

    public static Localizable localizableWSS_1759_TRANSFORM_ERROR(Object arg0) {
        return messageFactory.getMessage("WSS1759.transform.error", arg0);
    }

    /**
     * WSS1759: Following error {0} occured while performing canonicalization {0}
     * 
     */
    public static String WSS_1759_TRANSFORM_ERROR(Object arg0) {
        return localizer.localize(localizableWSS_1759_TRANSFORM_ERROR(arg0));
    }

    public static Localizable localizableWSS_1716_ERROR_DEREFERENCE_STR_TRANSFORM() {
        return messageFactory.getMessage("WSS1716.error.dereference.str.transform");
    }

    /**
     * WSS1716: Error occurred while dereferencing STR-Transform's Reference Element
     * 
     */
    public static String WSS_1716_ERROR_DEREFERENCE_STR_TRANSFORM() {
        return localizer.localize(localizableWSS_1716_ERROR_DEREFERENCE_STR_TRANSFORM());
    }

    public static Localizable localizableWSS_1753_TARGET_DIGEST_ALGORITHM(Object arg0) {
        return messageFactory.getMessage("WSS1753.target.digest.algorithm", arg0);
    }

    /**
     * WSS1753: Digest algorithm used: {0}
     * 
     */
    public static String WSS_1753_TARGET_DIGEST_ALGORITHM(Object arg0) {
        return localizer.localize(localizableWSS_1753_TARGET_DIGEST_ALGORITHM(arg0));
    }

    public static Localizable localizableWSS_1750_URI_TOBE_DEREFERENCED(Object arg0) {
        return messageFactory.getMessage("WSS1750.uri.tobe.dereferenced", arg0);
    }

    /**
     * WSS1750: URI to be dereferenced:{0}
     * 
     */
    public static String WSS_1750_URI_TOBE_DEREFERENCED(Object arg0) {
        return localizer.localize(localizableWSS_1750_URI_TOBE_DEREFERENCED(arg0));
    }

    public static Localizable localizableWSS_1704_ERROR_RESOLVING_ID(Object arg0) {
        return messageFactory.getMessage("WSS1704.error.resolving.id", arg0);
    }

    /**
     * WSS1704: Error occurred while resolving id: {0}. Perhaps it is not present in SOAP message.
     * 
     */
    public static String WSS_1704_ERROR_RESOLVING_ID(Object arg0) {
        return localizer.localize(localizableWSS_1704_ERROR_RESOLVING_ID(arg0));
    }

}
