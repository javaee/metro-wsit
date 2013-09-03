
package com.sun.xml.wss.logging;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_0267_INVALID_CONFIGURED_POLICY_USERNAME() {
        return messageFactory.getMessage("WSS0267.invalid.configuredPolicy.Username");
    }

    /**
     * WSS0267: Policy Verification error: UsernameToken not found in configured policy but occurs in message
     * 
     */
    public static String WSS_0267_INVALID_CONFIGURED_POLICY_USERNAME() {
        return localizer.localize(localizableWSS_0267_INVALID_CONFIGURED_POLICY_USERNAME());
    }

    public static Localizable localizableWSS_0167_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0167.diag.check.1");
    }

    /**
     * Make sure the signature was not tampered with in transit
     * 
     */
    public static String WSS_0167_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0167_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0392_INVALID_X_509_CERT_VERSION(Object arg0) {
        return messageFactory.getMessage("WSS0392.invalid.X509cert.version", arg0);
    }

    /**
     * WSS0392: Version [3] X509Certificate is expected, version found: [{0}]
     * 
     */
    public static String WSS_0392_INVALID_X_509_CERT_VERSION(Object arg0) {
        return localizer.localize(localizableWSS_0392_INVALID_X_509_CERT_VERSION(arg0));
    }

    public static Localizable localizableWSS_0194_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0194.diag.cause.2");
    }

    /**
     * SOAP-ENV:Body can not fully be encrypted
     * 
     */
    public static String WSS_0194_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0194_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0194_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0194.diag.cause.1");
    }

    /**
     * SOAP-ENV:Header can not fully be encrypted
     * 
     */
    public static String WSS_0194_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0194_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0320_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0320.diag.cause.1");
    }

    /**
     * Could not get KeyName from KeyInfo
     * 
     */
    public static String WSS_0320_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0320_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0325_EXCEPTION_ADDING_REFERENCE_TO_SIGNEDINFO() {
        return messageFactory.getMessage("WSS0325.exception.adding.reference.to.signedinfo");
    }

    /**
     * WSS0325: Exception while adding a Reference to SignedInfo
     * 
     */
    public static String WSS_0325_EXCEPTION_ADDING_REFERENCE_TO_SIGNEDINFO() {
        return localizer.localize(localizableWSS_0325_EXCEPTION_ADDING_REFERENCE_TO_SIGNEDINFO());
    }

    public static Localizable localizableWSS_0762_UNSUPPORTED_ENCODINGTYPE(Object arg0) {
        return messageFactory.getMessage("WSS0762.unsupported.encodingtype", arg0);
    }

    /**
     * WSS0762: Unsupported EncodingType {0} On KeyIdentifier
     * 
     */
    public static String WSS_0762_UNSUPPORTED_ENCODINGTYPE(Object arg0) {
        return localizer.localize(localizableWSS_0762_UNSUPPORTED_ENCODINGTYPE(arg0));
    }

    public static Localizable localizableWSS_0268_ERROR_POLICY_VERIFICATION() {
        return messageFactory.getMessage("WSS0268.error.policy.verification");
    }

    /**
     * WSS0268: Policy verification error: Missing Signature Element
     * 
     */
    public static String WSS_0268_ERROR_POLICY_VERIFICATION() {
        return localizer.localize(localizableWSS_0268_ERROR_POLICY_VERIFICATION());
    }

    public static Localizable localizableWSS_0233_INVALID_EXPIRE_BEFORE_CREATION() {
        return messageFactory.getMessage("WSS0233.invalid.expire.before.creation");
    }

    /**
     * WSS0233: Expiration time is before Creation Time
     * 
     */
    public static String WSS_0233_INVALID_EXPIRE_BEFORE_CREATION() {
        return localizer.localize(localizableWSS_0233_INVALID_EXPIRE_BEFORE_CREATION());
    }

    public static Localizable localizableWSS_0349_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0349.diag.check.1");
    }

    /**
     * Check that the SOAPElement passed to EncryptedKeyHeaderBlock() is valid as per spec.
     * 
     */
    public static String WSS_0349_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0349_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0252_FAILEDTO_GET_CHILD_ELEMENT() {
        return messageFactory.getMessage("WSS0252.failedto.getChildElement");
    }

    /**
     * WSS0252: Failed to get child element
     * 
     */
    public static String WSS_0252_FAILEDTO_GET_CHILD_ELEMENT() {
        return localizer.localize(localizableWSS_0252_FAILEDTO_GET_CHILD_ELEMENT());
    }

    public static Localizable localizableWSS_0376_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0376.diag.cause.1");
    }

    /**
     * Error importing the SOAPElement representing the header block to the document corresponding to the SOAPMessage to which the header is being added
     * 
     */
    public static String WSS_0376_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0376_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0321_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0321.diag.cause.1");
    }

    /**
     * Could not retrieve element from KeyInfo or could not import the node
     * 
     */
    public static String WSS_0321_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0321_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0356_ERROR_CREATING_X_509_DATA(Object arg0) {
        return messageFactory.getMessage("WSS0356.error.creating.x509data", arg0);
    }

    /**
     * WSS0356: Error creating X509Data due to {0}
     * 
     */
    public static String WSS_0356_ERROR_CREATING_X_509_DATA(Object arg0) {
        return localizer.localize(localizableWSS_0356_ERROR_CREATING_X_509_DATA(arg0));
    }

    public static Localizable localizableWSS_0100_CREATE_FOR_CREATING_IMPL(Object arg0) {
        return messageFactory.getMessage("WSS0100.createFor.creating.impl", arg0);
    }

    /**
     * WSS0100: Method Processor.createFor creating instance of {0}
     * 
     */
    public static String WSS_0100_CREATE_FOR_CREATING_IMPL(Object arg0) {
        return localizer.localize(localizableWSS_0100_CREATE_FOR_CREATING_IMPL(arg0));
    }

    public static Localizable localizableWSS_0218_CANNOT_LOCATE_DEFAULT_CERT() {
        return messageFactory.getMessage("WSS0218.cannot.locate.default.cert");
    }

    /**
     * WSS0218: Unable to locate a default certificate using Callback Handler. If you are using WSIT, make sure appropriate Keystore/Truststore assertions are present in wsit-client.xml/wsit-*.xml.
     * 
     */
    public static String WSS_0218_CANNOT_LOCATE_DEFAULT_CERT() {
        return localizer.localize(localizableWSS_0218_CANNOT_LOCATE_DEFAULT_CERT());
    }

    public static Localizable localizableWSS_0607_STR_TRANSFORM_EXCEPTION() {
        return messageFactory.getMessage("WSS0607.str.transform.exception");
    }

    /**
     * WSS0607: DOMException in updating SOAPElement representing X509Token
     * 
     */
    public static String WSS_0607_STR_TRANSFORM_EXCEPTION() {
        return localizer.localize(localizableWSS_0607_STR_TRANSFORM_EXCEPTION());
    }

    public static Localizable localizableWSS_0144_UNABLETO_DECODE_BASE_64_DATA(Object arg0) {
        return messageFactory.getMessage("WSS0144.unableto.decode.base64.data", arg0);
    }

    /**
     * WSS0144: Exception [ {0} ] while trying to decode Base64 encoded data
     * 
     */
    public static String WSS_0144_UNABLETO_DECODE_BASE_64_DATA(Object arg0) {
        return localizer.localize(localizableWSS_0144_UNABLETO_DECODE_BASE_64_DATA(arg0));
    }

    public static Localizable localizableWSS_0377_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0377.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.SOAPElement for SecurityTokenReference 
     * 
     */
    public static String WSS_0377_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0377_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0503_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0503.diag.cause.1");
    }

    /**
     * Element encountered does not match valid element expected
     * 
     */
    public static String WSS_0503_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0503_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0753_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0753.diag.check.1");
    }

    /**
     * Check that the token element is conformant to WSS spec.
     * 
     */
    public static String WSS_0753_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0753_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0227_INVALID_OLDER_CREATION_TIME() {
        return messageFactory.getMessage("WSS0227.invalid.older.CreationTime");
    }

    /**
     * WSS0227: The creation time is older than (currenttime-timestamp-freshness-limit-max-clock-skew)
     * 
     */
    public static String WSS_0227_INVALID_OLDER_CREATION_TIME() {
        return localizer.localize(localizableWSS_0227_INVALID_OLDER_CREATION_TIME());
    }

    public static Localizable localizableWSS_0215_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0215.diag.check.1");
    }

    /**
     * Check the handler implementation 
     * 
     */
    public static String WSS_0215_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0215_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0306_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0306.diag.check.1");
    }

    /**
     * Password type must match that specified by the WSS specification
     * 
     */
    public static String WSS_0306_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0306_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0333_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0333.diag.cause.1");
    }

    /**
     * Keystore URL is null
     * 
     */
    public static String WSS_0333_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0333_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0809_FAULT_WSSSOAP() {
        return messageFactory.getMessage("WSS0809.fault.WSSSOAP");
    }

    /**
     * WSS0809: WSS SOAP Fault Occured
     * 
     */
    public static String WSS_0809_FAULT_WSSSOAP() {
        return localizer.localize(localizableWSS_0809_FAULT_WSSSOAP());
    }

    public static Localizable localizableWSS_0128_UNABLETO_ENCRYPT_MESSAGE() {
        return messageFactory.getMessage("WSS0128.unableto.encrypt.message");
    }

    /**
     * WSS0128: Unable to encrypt element
     * 
     */
    public static String WSS_0128_UNABLETO_ENCRYPT_MESSAGE() {
        return localizer.localize(localizableWSS_0128_UNABLETO_ENCRYPT_MESSAGE());
    }

    public static Localizable localizableWSS_0389_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0389.diag.cause.1");
    }

    /**
     * Base64 nonce encoding type has not been specified
     * 
     */
    public static String WSS_0389_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0389_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0334_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0334.diag.cause.1");
    }

    /**
     * KeyIdentifier holds invalid ValueType
     * 
     */
    public static String WSS_0334_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0334_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0801_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0801.diag.check.1");
    }

    /**
     * Check that policy is one of WSSPolicy or MessagePolicy or ApplicationSecurityConfiguration
     * 
     */
    public static String WSS_0801_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0801_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0516_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0516.diag.cause.1");
    }

    /**
     * Non-permissable duplicate element on a Security Configuration Element
     * 
     */
    public static String WSS_0516_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0516_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0371_ERROR_GENERATE_FAULT(Object arg0) {
        return messageFactory.getMessage("WSS0371.error.generate.fault", arg0);
    }

    /**
     * WSS0371: Error occurred in generating fault message due to {0}
     * 
     */
    public static String WSS_0371_ERROR_GENERATE_FAULT(Object arg0) {
        return localizer.localize(localizableWSS_0371_ERROR_GENERATE_FAULT(arg0));
    }

    public static Localizable localizableWSS_0341_CREATED_OLDER_THAN_TIMESTAMP_FRESHNESS() {
        return messageFactory.getMessage("WSS0341.created.older.than.timestamp.freshness");
    }

    /**
     * WSS0341: The creation time is older than currenttime - timestamp-freshness-limit - max-clock-skew
     * 
     */
    public static String WSS_0341_CREATED_OLDER_THAN_TIMESTAMP_FRESHNESS() {
        return localizer.localize(localizableWSS_0341_CREATED_OLDER_THAN_TIMESTAMP_FRESHNESS());
    }

    public static Localizable localizableWSS_0607_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0607.diag.cause.1");
    }

    /**
     * DOMException in removing "EncodingType" attribute on SOAPElement representing X509Token
     * 
     */
    public static String WSS_0607_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0607_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0391_ERROR_CREATING_X_509_SECURITY_TOKEN(Object arg0) {
        return messageFactory.getMessage("WSS0391.error.creating.X509SecurityToken", arg0);
    }

    /**
     * WSS0391: Expected wsse:BinarySecurityToken, found {0}
     * 
     */
    public static String WSS_0391_ERROR_CREATING_X_509_SECURITY_TOKEN(Object arg0) {
        return localizer.localize(localizableWSS_0391_ERROR_CREATING_X_509_SECURITY_TOKEN(arg0));
    }

    public static Localizable localizableWSS_0137_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0137.diag.check.1");
    }

    /**
     * Look at root exception for more clues
     * 
     */
    public static String WSS_0137_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0137_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0284_WSS_SOAP_FAULT_EXCEPTION() {
        return messageFactory.getMessage("WSS0284.WSS.SOAP.Fault.Exception");
    }

    /**
     * WSS0284: SOAP Fault Exception Occured
     * 
     */
    public static String WSS_0284_WSS_SOAP_FAULT_EXCEPTION() {
        return localizer.localize(localizableWSS_0284_WSS_SOAP_FAULT_EXCEPTION());
    }

    public static Localizable localizableWSS_0271_FAILEDTO_RESOLVE_POLICY() {
        return messageFactory.getMessage("WSS0271.failedto.resolve.policy");
    }

    /**
     * WSS0271: Policy has to resolve to MessagePolicy
     * 
     */
    public static String WSS_0271_FAILEDTO_RESOLVE_POLICY() {
        return localizer.localize(localizableWSS_0271_FAILEDTO_RESOLVE_POLICY());
    }

    public static Localizable localizableWSS_0319_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0319.diag.check.1");
    }

    /**
     * Check KeyInfo and KeyName have been instantiated without exceptions
     * 
     */
    public static String WSS_0319_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0319_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0200_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0200.diag.cause.1");
    }

    /**
     * CallbackHandler to obtain Username/Password at runtime was ineffective
     * 
     */
    public static String WSS_0200_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0200_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0414_SAML_MISSING_ATTRIBUTE_VALUE() {
        return messageFactory.getMessage("WSS0414.saml.missing.attribute.value");
    }

    /**
     * WSS0414: Missing attribute value
     * 
     */
    public static String WSS_0414_SAML_MISSING_ATTRIBUTE_VALUE() {
        return localizer.localize(localizableWSS_0414_SAML_MISSING_ATTRIBUTE_VALUE());
    }

    public static Localizable localizableWSS_0346_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0346.diag.cause.1");
    }

    /**
     * Invalid SOAPElement passed to EncryptedDataHeaderBlock constructor
     * 
     */
    public static String WSS_0346_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0346_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0316_ENCTYPE_INVALID() {
        return messageFactory.getMessage("WSS0316.enctype.invalid");
    }

    /**
     * WSS0316: Encoding type invalid
     * 
     */
    public static String WSS_0316_ENCTYPE_INVALID() {
        return localizer.localize(localizableWSS_0316_ENCTYPE_INVALID());
    }

    public static Localizable localizableBSP_3031_VALUE_TYPE_NOT_PRESENT() {
        return messageFactory.getMessage("BSP3031.ValueType.NotPresent");
    }

    /**
     * BSP3031: Any wsse:BinarySecurityToken element in a SECURE_ENVELOPE MUST have a ValueType attribute.  
     * 
     */
    public static String BSP_3031_VALUE_TYPE_NOT_PRESENT() {
        return localizer.localize(localizableBSP_3031_VALUE_TYPE_NOT_PRESENT());
    }

    public static Localizable localizableWSS_0347_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0347.diag.cause.2");
    }

    /**
     * CipherData may not have been set on the EncryptedType
     * 
     */
    public static String WSS_0347_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0347_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0347_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0347.diag.cause.1");
    }

    /**
     * SOAPElement used to initialize EncryptedType may not have CipherData element
     * 
     */
    public static String WSS_0347_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0347_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0750_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0750.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.SOAPElement
     * 
     */
    public static String WSS_0750_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0750_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0279_FAILED_CHECK_SEC_SECURITY() {
        return messageFactory.getMessage("WSS0279.failed.check.secSecurity");
    }

    /**
     * WSS0279: failed to check For Extra Secondary Security
     * 
     */
    public static String WSS_0279_FAILED_CHECK_SEC_SECURITY() {
        return localizer.localize(localizableWSS_0279_FAILED_CHECK_SEC_SECURITY());
    }

    public static Localizable localizableWSS_0371_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0371.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0371_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0371_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0802_SECURITYPOLICY_NOTSATISFIED(Object arg0) {
        return messageFactory.getMessage("WSS0802.securitypolicy.notsatisfied", arg0);
    }

    /**
     * SecurityPolicy {0} is not satisfied
     * 
     */
    public static String WSS_0802_SECURITYPOLICY_NOTSATISFIED(Object arg0) {
        return localizer.localize(localizableWSS_0802_SECURITYPOLICY_NOTSATISFIED(arg0));
    }

    public static Localizable localizableWSS_0212_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0212.diag.cause.1");
    }

    /**
     * Receiver requirement for digested password in UsernameToken not met
     * 
     */
    public static String WSS_0212_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0212_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0420_SAML_CANNOT_FIND_SUBJECTCONFIRMATION_KEYINFO() {
        return messageFactory.getMessage("WSS0420.saml.cannot.find.subjectconfirmation.keyinfo");
    }

    /**
     * WSS0420: Unable to locate KeyInfo inside SubjectConfirmation element of SAML Assertion
     * 
     */
    public static String WSS_0420_SAML_CANNOT_FIND_SUBJECTCONFIRMATION_KEYINFO() {
        return localizer.localize(localizableWSS_0420_SAML_CANNOT_FIND_SUBJECTCONFIRMATION_KEYINFO());
    }

    public static Localizable localizableWSS_0751_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0751.diag.cause.2");
    }

    /**
     * The namespace URI of the SOAPElement passed does not conform to WSS Spec.
     * 
     */
    public static String WSS_0751_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0751_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0363_ERROR_ADDING_DATAREFERENCE(Object arg0) {
        return messageFactory.getMessage("WSS0363.error.adding.datareference", arg0);
    }

    /**
     * WSS0363: Can not add xenc:DataReference element to xenc:ReferenceList due to {0}
     * 
     */
    public static String WSS_0363_ERROR_ADDING_DATAREFERENCE(Object arg0) {
        return localizer.localize(localizableWSS_0363_ERROR_ADDING_DATAREFERENCE(arg0));
    }

    public static Localizable localizableWSS_0751_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0751.diag.cause.1");
    }

    /**
     * The localname of the SOAPElement passed is not "Reference"
     * 
     */
    public static String WSS_0751_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0751_DIAG_CAUSE_1());
    }

    public static Localizable localizableBSP_3227_SINGLE_TIMESTAMP() {
        return messageFactory.getMessage("BSP3227.Single.Timestamp");
    }

    /**
     * BSP3227: A SECURITY_HEADER MUST NOT contain more than one TIMESTAMP.
     * 
     */
    public static String BSP_3227_SINGLE_TIMESTAMP() {
        return localizer.localize(localizableBSP_3227_SINGLE_TIMESTAMP());
    }

    public static Localizable localizableWSS_0755_SOAP_EXCEPTION(Object arg0) {
        return messageFactory.getMessage("WSS0755.soap.exception", arg0);
    }

    /**
     * WSS0755: Error embedding token in TokenReference due to {0}
     * 
     */
    public static String WSS_0755_SOAP_EXCEPTION(Object arg0) {
        return localizer.localize(localizableWSS_0755_SOAP_EXCEPTION(arg0));
    }

    public static Localizable localizableWSS_0213_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0213.diag.cause.1");
    }

    /**
     * Receiver requirement for nonce in UsernameToken not met
     * 
     */
    public static String WSS_0213_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0213_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0359_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0359.diag.cause.1");
    }

    /**
     * Error adding com.sun.org.apache.xml.internal.security.keys.content.X509Data to KeyInfo
     * 
     */
    public static String WSS_0359_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0359_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0306_INVALID_PASSWD_TYPE(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0306.invalid.passwd.type", arg0, arg1);
    }

    /**
     * WSS0306: Invalid password type. Must be one of {0} or {1}
     * 
     */
    public static String WSS_0306_INVALID_PASSWD_TYPE(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0306_INVALID_PASSWD_TYPE(arg0, arg1));
    }

    public static Localizable localizableWSS_0266_FAILEDTO_PROCESS_SECONDARY_POLICY() {
        return messageFactory.getMessage("WSS0266.failedto.process.secondary.policy");
    }

    /**
     * WSS0266: Failed to process Secondary Policy
     * 
     */
    public static String WSS_0266_FAILEDTO_PROCESS_SECONDARY_POLICY() {
        return localizer.localize(localizableWSS_0266_FAILEDTO_PROCESS_SECONDARY_POLICY());
    }

    public static Localizable localizableWSS_0275_INVALID_POLICY_NO_USERNAME_SEC_HEADER() {
        return messageFactory.getMessage("WSS0275.invalid.policy.NoUsername.SecHeader");
    }

    /**
     * WSS0275: Message does not conform to configured policy: UsernameToken element not found in security header
     * 
     */
    public static String WSS_0275_INVALID_POLICY_NO_USERNAME_SEC_HEADER() {
        return localizer.localize(localizableWSS_0275_INVALID_POLICY_NO_USERNAME_SEC_HEADER());
    }

    public static Localizable localizableWSS_0806_STATIC_CONTEXT_NULL() {
        return messageFactory.getMessage("WSS0806.static.context.null");
    }

    /**
     * Static context is null
     * 
     */
    public static String WSS_0806_STATIC_CONTEXT_NULL() {
        return localizer.localize(localizableWSS_0806_STATIC_CONTEXT_NULL());
    }

    public static Localizable localizableWSS_0763_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0763.diag.cause.1");
    }

    /**
     * Exception while getting Issuer Name
     * 
     */
    public static String WSS_0763_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0763_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0134_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0134.diag.cause.1");
    }

    /**
     * Unable to Initialize XMLCipher with the given Key
     * 
     */
    public static String WSS_0134_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0134_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0238_FAILED_RESOLVE_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS0238.failed.Resolve.SAMLAssertion");
    }

    /**
     * WSS0238: Failed to resolve SAML Assertion
     * 
     */
    public static String WSS_0238_FAILED_RESOLVE_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_0238_FAILED_RESOLVE_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_0384_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0384.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0384_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0384_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0315_NOT_A_CERT_ENTRY(Object arg0) {
        return messageFactory.getMessage("WSS0315.not.a.cert.entry", arg0);
    }

    /**
     * WSS0315: KeyStore entry with alias={0} is not a certificate entry
     * 
     */
    public static String WSS_0315_NOT_A_CERT_ENTRY(Object arg0) {
        return localizer.localize(localizableWSS_0315_NOT_A_CERT_ENTRY(arg0));
    }

    public static Localizable localizableWSS_0253_INVALID_MESSAGE() {
        return messageFactory.getMessage("WSS0253.invalid.Message");
    }

    /**
     * WSS0253: Message does not conform to configured policy: No Security Header found in message
     * 
     */
    public static String WSS_0253_INVALID_MESSAGE() {
        return localizer.localize(localizableWSS_0253_INVALID_MESSAGE());
    }

    public static Localizable localizableWSS_0511_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0511.diag.check.1");
    }

    /**
     * Check that the boolean value strings are either "true" or "false"
     * 
     */
    public static String WSS_0511_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0511_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0602_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0602.diag.check.1");
    }

    /**
     * Check that the certificate referred to is valid and present in the Keystores
     * 
     */
    public static String WSS_0602_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0602_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0317_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0317.diag.cause.1");
    }

    /**
     * Could not find X.509 certificate
     * 
     */
    public static String WSS_0317_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0317_DIAG_CAUSE_1());
    }

    public static Localizable localizableBSP_3057_STR_NOT_REF_STR() {
        return messageFactory.getMessage("BSP3057.str.not.ref.str");
    }

    /**
     * BSP 3057 : Reference element under SecurityTokenReference MUST NOT reference another SecurityTokenReference element.
     * 
     */
    public static String BSP_3057_STR_NOT_REF_STR() {
        return localizer.localize(localizableBSP_3057_STR_NOT_REF_STR());
    }

    public static Localizable localizableWSS_0808_NO_BODY_ELEMENT_OPERATION() {
        return messageFactory.getMessage("WSS0808.no.body.element.operation");
    }

    /**
     * No body element identifying an operation is found
     * 
     */
    public static String WSS_0808_NO_BODY_ELEMENT_OPERATION() {
        return localizer.localize(localizableWSS_0808_NO_BODY_ELEMENT_OPERATION());
    }

    public static Localizable localizableWSS_0341_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0341.diag.check.1");
    }

    /**
     * Check system time and ensure it is correct
     * 
     */
    public static String WSS_0341_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0341_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0215_FAILED_PROPERTYCALLBACK() {
        return messageFactory.getMessage("WSS0215.failed.propertycallback");
    }

    /**
     * WSS0215: Handler failed to handle PropertyCallback, got Exception
     * 
     */
    public static String WSS_0215_FAILED_PROPERTYCALLBACK() {
        return localizer.localize(localizableWSS_0215_FAILED_PROPERTYCALLBACK());
    }

    public static Localizable localizableWSS_0147_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0147.diag.cause.1");
    }

    /**
     * TransformationConfiguration exception while trying to use stylesheet to pretty print
     * 
     */
    public static String WSS_0147_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0147_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0217_CALLBACKHANDLER_HANDLE_EXCEPTION_LOG() {
        return messageFactory.getMessage("WSS0217.callbackhandler.handle.exception.log");
    }

    /**
     * WSS0217: An Error occurred using Callback Handler handle() Method.
     * 
     */
    public static String WSS_0217_CALLBACKHANDLER_HANDLE_EXCEPTION_LOG() {
        return localizer.localize(localizableWSS_0217_CALLBACKHANDLER_HANDLE_EXCEPTION_LOG());
    }

    public static Localizable localizableWSS_0312_EXCEPTION_IN_CERTPATH_VALIDATE(Object arg0) {
        return messageFactory.getMessage("WSS0312.exception.in.certpath.validate", arg0);
    }

    /**
     * WSS0312: Exception [ {0} ] while validating certPath
     * 
     */
    public static String WSS_0312_EXCEPTION_IN_CERTPATH_VALIDATE(Object arg0) {
        return localizer.localize(localizableWSS_0312_EXCEPTION_IN_CERTPATH_VALIDATE(arg0));
    }

    public static Localizable localizableWSS_0137_UNABLETO_DECRYPT_MESSAGE(Object arg0) {
        return messageFactory.getMessage("WSS0137.unableto.decrypt.message", arg0);
    }

    /**
     * WSS0137: Exception [ {0} ] while trying to decrypt message
     * 
     */
    public static String WSS_0137_UNABLETO_DECRYPT_MESSAGE(Object arg0) {
        return localizer.localize(localizableWSS_0137_UNABLETO_DECRYPT_MESSAGE(arg0));
    }

    public static Localizable localizableWSS_0239_FAILED_PROCESS_SECURITY_TOKEN_REFERENCE() {
        return messageFactory.getMessage("WSS0239.failed.process.SecurityTokenReference");
    }

    /**
     * WSS0239: Failed to process Security Token Reference
     * 
     */
    public static String WSS_0239_FAILED_PROCESS_SECURITY_TOKEN_REFERENCE() {
        return localizer.localize(localizableWSS_0239_FAILED_PROCESS_SECURITY_TOKEN_REFERENCE());
    }

    public static Localizable localizableWSS_0361_ERROR_CREATING_RLHB(Object arg0) {
        return messageFactory.getMessage("WSS0361.error.creating.rlhb", arg0);
    }

    /**
     * WSS0361: Error creating ReferenceListHeaderBlock due to {0}
     * 
     */
    public static String WSS_0361_ERROR_CREATING_RLHB(Object arg0) {
        return localizer.localize(localizableWSS_0361_ERROR_CREATING_RLHB(arg0));
    }

    public static Localizable localizableWSS_0354_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0354.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation 
     * 
     */
    public static String WSS_0354_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0354_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0381_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0381.diag.cause.1");
    }

    /**
     * Error in setting the passed ReferenceElement on SecurityTokenReference
     * 
     */
    public static String WSS_0381_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0381_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0519_ILLEGAL_ATTRIBUTE_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS0519.illegal.attribute.value", arg0);
    }

    /**
     * WSS0519: Illegal/Missing attribute value for: {0}
     * 
     */
    public static String WSS_0519_ILLEGAL_ATTRIBUTE_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_0519_ILLEGAL_ATTRIBUTE_VALUE(arg0));
    }

    public static Localizable localizableWSS_0382_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0382.diag.cause.1");
    }

    /**
     * Error appending ds:Object element to ds:Signature
     * 
     */
    public static String WSS_0382_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0382_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0287_ERROR_EXTRACTING_ATTACHMENTPART() {
        return messageFactory.getMessage("WSS0287.error.extracting.attachmentpart");
    }

    /**
     * WSS0287: Exception occured while trying to extract attachment part
     * 
     */
    public static String WSS_0287_ERROR_EXTRACTING_ATTACHMENTPART() {
        return localizer.localize(localizableWSS_0287_ERROR_EXTRACTING_ATTACHMENTPART());
    }

    public static Localizable localizableWSS_0367_NO_ENCRYPTEDDATA_FOUND() {
        return messageFactory.getMessage("WSS0367.no.encrypteddata.found");
    }

    /**
     * WSS0367: No xenc:EncryptedData found
     * 
     */
    public static String WSS_0367_NO_ENCRYPTEDDATA_FOUND() {
        return localizer.localize(localizableWSS_0367_NO_ENCRYPTEDDATA_FOUND());
    }

    public static Localizable localizableWSS_0220_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0220.diag.check.1");
    }

    /**
     * Check the handler implementation for DecryptionKeyCallback.AliasSymmetricKeyRequest
     * 
     */
    public static String WSS_0220_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0220_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0511_ILLEGAL_BOOLEAN_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS0511.illegal.boolean.value", arg0);
    }

    /**
     * WSS0511: Boolean value strings can be either "true"|"false", found: {0}
     * 
     */
    public static String WSS_0511_ILLEGAL_BOOLEAN_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_0511_ILLEGAL_BOOLEAN_VALUE(arg0));
    }

    public static Localizable localizableWSS_0600_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0600.diag.cause.1");
    }

    /**
     * A Key can not be located in SecurityEnvironment for the Token Reference
     * 
     */
    public static String WSS_0600_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0600_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0185_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0185.diag.check.1");
    }

    /**
     * Check that the URI is valid and subjectkeyidentifier parameter is set in configuration
     * 
     */
    public static String WSS_0185_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0185_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0311_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0311.diag.check.1");
    }

    /**
     * Check that the algorithm passed to MessageDigest is valid
     * 
     */
    public static String WSS_0311_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0311_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0654_SOAP_EXCEPTION(Object arg0) {
        return messageFactory.getMessage("WSS0654.soap.exception", arg0);
    }

    /**
     * WSS0654: Error creating javax.xml.soap.Name for wsu:Id due to {0}
     * 
     */
    public static String WSS_0654_SOAP_EXCEPTION(Object arg0) {
        return localizer.localize(localizableWSS_0654_SOAP_EXCEPTION(arg0));
    }

    public static Localizable localizableWSS_0161_UNABLETO_FIND_MATCHING_PRIVATEKEY() {
        return messageFactory.getMessage("WSS0161.unableto.find.matching.privatekey");
    }

    /**
     * WSS0161: Unable to find matching PrivateKey
     * 
     */
    public static String WSS_0161_UNABLETO_FIND_MATCHING_PRIVATEKEY() {
        return localizer.localize(localizableWSS_0161_UNABLETO_FIND_MATCHING_PRIVATEKEY());
    }

    public static Localizable localizableWSS_0208_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0208.diag.cause.1");
    }

    /**
     * Extra security than required by the receiver side policy found in the message
     * 
     */
    public static String WSS_0208_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0208_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0394_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0394.diag.cause.1");
    }

    /**
     * Error parsing date. 
     * 
     */
    public static String WSS_0394_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0394_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0713_ERROR_IN_CERTSTORE_LOOKUP() {
        return messageFactory.getMessage("WSS0713.error.in.certstore.lookup");
    }

    /**
     * WSS0713: An Exception occurred while looking up Certstore
     * 
     */
    public static String WSS_0713_ERROR_IN_CERTSTORE_LOOKUP() {
        return localizer.localize(localizableWSS_0713_ERROR_IN_CERTSTORE_LOOKUP());
    }

    public static Localizable localizableWSS_0209_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0209.diag.cause.1");
    }

    /**
     * The message filter is in-correctly configured to process an inbound msg
     * 
     */
    public static String WSS_0209_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0209_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0395_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0395.diag.cause.1");
    }

    /**
     * Error while creating a CipherData element
     * 
     */
    public static String WSS_0395_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0395_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0703_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0703.diag.cause.1");
    }

    /**
     * insertKey(SecurityTokenReference, SecurableSoapMessage) is not supported on KeyNameStrategy
     * 
     */
    public static String WSS_0703_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0703_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0165_UNABLE_TO_ENCRYPT() {
        return messageFactory.getMessage("WSS0165.unable.to.encrypt");
    }

    /**
     * WSS0165: XPath does not correspond to a DOM Element
     * 
     */
    public static String WSS_0165_UNABLE_TO_ENCRYPT() {
        return localizer.localize(localizableWSS_0165_UNABLE_TO_ENCRYPT());
    }

    public static Localizable localizableWSS_0324_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0324.diag.check.1");
    }

    /**
     * Check that the message signed using corresponding private key, and has not been tampered with
     * 
     */
    public static String WSS_0324_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0324_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0198_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0198.diag.check.1");
    }

    /**
     * Check that RSA_SHA1 signature algorithm is specified
     * 
     */
    public static String WSS_0198_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0198_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0759_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0759.diag.cause.1");
    }

    /**
     * An X509IssuerSerial instance can not be created
     * 
     */
    public static String WSS_0759_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0759_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0704_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0704.diag.cause.1");
    }

    /**
     * Agreement name: SESSION-KEY-VALUE, has not been set on the SecurityEnvironment instance
     * 
     */
    public static String WSS_0704_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0704_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0714_ERROR_GETTING_USER_CLASS(Object arg0) {
        return messageFactory.getMessage("WSS0714.error.getting.userClass", arg0);
    }

    /**
     * WSS0714: Could not find User Class {0}
     * 
     */
    public static String WSS_0714_ERROR_GETTING_USER_CLASS(Object arg0) {
        return localizer.localize(localizableWSS_0714_ERROR_GETTING_USER_CLASS(arg0));
    }

    public static Localizable localizableWSS_0129_NO_DSSIGNATURE_IN_SECURITY_HEADERBLOCK(Object arg0) {
        return messageFactory.getMessage("WSS0129.no.dssignature.in.security.headerblock", arg0);
    }

    /**
     * WSS0129: Could not retrieve the ds:Signature element from the wsse:Security header block, got Exception [ {0} ]
     * 
     */
    public static String WSS_0129_NO_DSSIGNATURE_IN_SECURITY_HEADERBLOCK(Object arg0) {
        return localizer.localize(localizableWSS_0129_NO_DSSIGNATURE_IN_SECURITY_HEADERBLOCK(arg0));
    }

    public static Localizable localizableWSS_0240_INVALID_ENCRYPTED_KEY_SHA_1_REFERENCE() {
        return messageFactory.getMessage("WSS0240.invalid.EncryptedKeySHA1.reference");
    }

    /**
     * WSS0240: EncryptedKeySHA1 reference not correct
     * 
     */
    public static String WSS_0240_INVALID_ENCRYPTED_KEY_SHA_1_REFERENCE() {
        return localizer.localize(localizableWSS_0240_INVALID_ENCRYPTED_KEY_SHA_1_REFERENCE());
    }

    public static Localizable localizableWSS_0352_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0352.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.Name for CipherValue
     * 
     */
    public static String WSS_0352_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0352_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0270_FAILEDTO_GET_SECURITY_POLICY_MESSAGE_POLICY() {
        return messageFactory.getMessage("WSS0270.failedto.get.SecurityPolicy.MessagePolicy");
    }

    /**
     * WSS0270: Failed to get security policy from message policy
     * 
     */
    public static String WSS_0270_FAILEDTO_GET_SECURITY_POLICY_MESSAGE_POLICY() {
        return localizer.localize(localizableWSS_0270_FAILEDTO_GET_SECURITY_POLICY_MESSAGE_POLICY());
    }

    public static Localizable localizableWSS_0231_UNSUPPORTED_VALIDATING_SAML_USER() {
        return messageFactory.getMessage("WSS0231.unsupported.Validating.SAMLUser");
    }

    /**
     * WSS0231: SAML User Validation not yet supported.
     * 
     */
    public static String WSS_0231_UNSUPPORTED_VALIDATING_SAML_USER() {
        return localizer.localize(localizableWSS_0231_UNSUPPORTED_VALIDATING_SAML_USER());
    }

    public static Localizable localizableWSS_0807_NO_BODY_ELEMENT() {
        return messageFactory.getMessage("WSS0807.no.body.element");
    }

    /**
     * No body element is found
     * 
     */
    public static String WSS_0807_NO_BODY_ELEMENT() {
        return localizer.localize(localizableWSS_0807_NO_BODY_ELEMENT());
    }

    public static Localizable localizableWSS_0608_ILLEGAL_REFERENCE_MECHANISM() {
        return messageFactory.getMessage("WSS0608.illegal.reference.mechanism");
    }

    /**
     * WSS0608: Illegal Reference Mechanism in SecurityTokenReference
     * 
     */
    public static String WSS_0608_ILLEGAL_REFERENCE_MECHANISM() {
        return localizer.localize(localizableWSS_0608_ILLEGAL_REFERENCE_MECHANISM());
    }

    public static Localizable localizableWSS_0298_X_509_EXPIRED() {
        return messageFactory.getMessage("WSS0298.X509.expired");
    }

    /**
     * WSS0298: X509Certificate Expired.
     * 
     */
    public static String WSS_0298_X_509_EXPIRED() {
        return localizer.localize(localizableWSS_0298_X_509_EXPIRED());
    }

    public static Localizable localizableWSS_0182_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0182.diag.cause.1");
    }

    /**
     * FilterParameterConstants.REFERENCE_LIST parameter has a null value
     * 
     */
    public static String WSS_0182_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0182_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0337_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0337.diag.check.1");
    }

    /**
     * Check DirectReference's ValueType, it is not supported
     * 
     */
    public static String WSS_0337_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0337_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0808_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0808.diag.cause.1");
    }

    /**
     * SOAPBody element identifying operation not found
     * 
     */
    public static String WSS_0808_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0808_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0761_CONTEXT_NOT_INSTANCEOF_SERVLETENDPOINTCONTEXT() {
        return messageFactory.getMessage("WSS0761.context.not.instanceof.servletendpointcontext");
    }

    /**
     * WSS0761: Context supplied is not an instanceof ServletEndpointContext or com.sun.xml.wss.ProcessingContext
     * 
     */
    public static String WSS_0761_CONTEXT_NOT_INSTANCEOF_SERVLETENDPOINTCONTEXT() {
        return localizer.localize(localizableWSS_0761_CONTEXT_NOT_INSTANCEOF_SERVLETENDPOINTCONTEXT());
    }

    public static Localizable localizableWSS_0338_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0338.diag.check.1");
    }

    /**
     * Check reference is one of X509IssuerSerial, DirectReference, KeyIdentifier 
     * 
     */
    public static String WSS_0338_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0338_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0601_UNSUPPORTED_KEYINFO_WSS_0601_ILLEGAL_KEY_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS0601.unsupported.keyinfoWSS0601.illegal.key.value", arg0);
    }

    /**
     * WSS0601: Key can not be located for the KeyValue (ds:KeyInfo) due to {0}
     * 
     */
    public static String WSS_0601_UNSUPPORTED_KEYINFO_WSS_0601_ILLEGAL_KEY_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_0601_UNSUPPORTED_KEYINFO_WSS_0601_ILLEGAL_KEY_VALUE(arg0));
    }

    public static Localizable localizableWSS_0650_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0650.diag.check.1");
    }

    /**
     * Check that the system property com.sun.xml.wss.usersFile is set
     * 
     */
    public static String WSS_0650_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0650_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0348_ERROR_CREATING_EKHB(Object arg0) {
        return messageFactory.getMessage("WSS0348.error.creating.ekhb", arg0);
    }

    /**
     * WSS0348: Error creating EncryptedKeyHeaderBlock due to {0}
     * 
     */
    public static String WSS_0348_ERROR_CREATING_EKHB(Object arg0) {
        return localizer.localize(localizableWSS_0348_ERROR_CREATING_EKHB(arg0));
    }

    public static Localizable localizableWSS_0365_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0365.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.SOAPElement for namespace node
     * 
     */
    public static String WSS_0365_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0365_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0189_UNSUPPORTED_DATA_DECRYPTION_ALGORITHM() {
        return messageFactory.getMessage("WSS0189.unsupported.data.decryption.algorithm");
    }

    /**
     * WSS0189: Unsupported data decryption algorithm
     * 
     */
    public static String WSS_0189_UNSUPPORTED_DATA_DECRYPTION_ALGORITHM() {
        return localizer.localize(localizableWSS_0189_UNSUPPORTED_DATA_DECRYPTION_ALGORITHM());
    }

    public static Localizable localizableWSS_0203_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0203.diag.check.1");
    }

    /**
     * Check that the message meets the security requirements 
     * 
     */
    public static String WSS_0203_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0203_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0168_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0168.diag.check.1");
    }

    /**
     * Verify that the KeyGenerator has been properly initialized
     * 
     */
    public static String WSS_0168_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0168_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0513_ILLEGAL_CONFIGURATION_ELEMENT(Object arg0) {
        return messageFactory.getMessage("WSS0513.illegal.configuration.element", arg0);
    }

    /**
     * WSS0513: Unexpected element: {0} in xwss:SecurityConfiguration
     * 
     */
    public static String WSS_0513_ILLEGAL_CONFIGURATION_ELEMENT(Object arg0) {
        return localizer.localize(localizableWSS_0513_ILLEGAL_CONFIGURATION_ELEMENT(arg0));
    }

    public static Localizable localizableWSS_0272_FAILEDTO_DEREFER_TARGETS() {
        return messageFactory.getMessage("WSS0272.failedto.derefer.targets");
    }

    /**
     * WSS0272: Failed to Dereference targets
     * 
     */
    public static String WSS_0272_FAILEDTO_DEREFER_TARGETS() {
        return localizer.localize(localizableWSS_0272_FAILEDTO_DEREFER_TARGETS());
    }

    public static Localizable localizableWSS_0195_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0195.diag.cause.1");
    }

    /**
     * ReferenceListBlock not set on the calling thread
     * 
     */
    public static String WSS_0195_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0195_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0397_SOAP_FACTORY_EXCEPTION() {
        return messageFactory.getMessage("WSS0397.soap.factory.exception");
    }

    /**
     * WSS0397: Error getting SOAPFactory instance
     * 
     */
    public static String WSS_0397_SOAP_FACTORY_EXCEPTION() {
        return localizer.localize(localizableWSS_0397_SOAP_FACTORY_EXCEPTION());
    }

    public static Localizable localizableWSS_0304_MESSAGE_DOESNOT_CONTAIN_HEADER() {
        return messageFactory.getMessage("WSS0304.message.doesnot.contain.header");
    }

    /**
     * WSS0304: Message does not contain a Header
     * 
     */
    public static String WSS_0304_MESSAGE_DOESNOT_CONTAIN_HEADER() {
        return localizer.localize(localizableWSS_0304_MESSAGE_DOESNOT_CONTAIN_HEADER());
    }

    public static Localizable localizableWSS_0282_UNSUPPORTED_KEY_IDENTIFIER_REFERENCE_DKT() {
        return messageFactory.getMessage("WSS0282.unsupported.KeyIdentifier.Reference.DKT");
    }

    /**
     * WSS0282: Unsupported KeyIdentifierReference under DerivedKey
     * 
     */
    public static String WSS_0282_UNSUPPORTED_KEY_IDENTIFIER_REFERENCE_DKT() {
        return localizer.localize(localizableWSS_0282_UNSUPPORTED_KEY_IDENTIFIER_REFERENCE_DKT());
    }

    public static Localizable localizableWSS_0322_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0322.diag.cause.1");
    }

    /**
     * Exception while parsing and creating the Signature element
     * 
     */
    public static String WSS_0322_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0322_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0318_EXCEPTION_WHILE_CREATING_KEYINFOBLOCK() {
        return messageFactory.getMessage("WSS0318.exception.while.creating.keyinfoblock");
    }

    /**
     * WSS0318: Exception while constructing KeyInfo Header block
     * 
     */
    public static String WSS_0318_EXCEPTION_WHILE_CREATING_KEYINFOBLOCK() {
        return localizer.localize(localizableWSS_0318_EXCEPTION_WHILE_CREATING_KEYINFOBLOCK());
    }

    public static Localizable localizableWSS_0378_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0378.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.SOAPElement for SecurityTokenReference 
     * 
     */
    public static String WSS_0378_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0378_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0216_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0216.diag.check.1");
    }

    /**
     * Check the handler implementation 
     * 
     */
    public static String WSS_0216_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0216_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_3027_STR_KEY_NAME() {
        return messageFactory.getMessage("BSP3027.str.key.name");
    }

    /**
     * BSP 3027 : wsse:SecurityTokenReference  MUST NOT contain KeyName as ds:KeyName.
     * 
     */
    public static String BSP_3027_STR_KEY_NAME() {
        return localizer.localize(localizableBSP_3027_STR_KEY_NAME());
    }

    public static Localizable localizableWSS_0208_POLICY_VIOLATION_EXCEPTION() {
        return messageFactory.getMessage("WSS0208.policy.violation.exception");
    }

    /**
     * WSS0208: Extra security than required found 
     * 
     */
    public static String WSS_0208_POLICY_VIOLATION_EXCEPTION() {
        return localizer.localize(localizableWSS_0208_POLICY_VIOLATION_EXCEPTION());
    }

    public static Localizable localizableWSS_0751_INVALID_DATA_REFERENCE(Object arg0) {
        return messageFactory.getMessage("WSS0751.invalid.data.reference", arg0);
    }

    /**
     * WSS0751: Invalid SOAPElement ({0}) passed to DirectReference()
     * 
     */
    public static String WSS_0751_INVALID_DATA_REFERENCE(Object arg0) {
        return localizer.localize(localizableWSS_0751_INVALID_DATA_REFERENCE(arg0));
    }

    public static Localizable localizableWSS_0307_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0307.diag.check.1");
    }

    /**
     * Nonce encoding type namespace seems invalid
     * 
     */
    public static String WSS_0307_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0307_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0335_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0335.diag.cause.1");
    }

    /**
     * KeyReference Type not supported
     * 
     */
    public static String WSS_0335_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0335_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0246_UNABLETO_LOCATE_SECURE_CONVERSATION_SESSION() {
        return messageFactory.getMessage("WSS0246.unableto.locate.SecureConversationSession");
    }

    /**
     * WSS0246: Could not locate SecureConversation session for Id
     * 
     */
    public static String WSS_0246_UNABLETO_LOCATE_SECURE_CONVERSATION_SESSION() {
        return localizer.localize(localizableWSS_0246_UNABLETO_LOCATE_SECURE_CONVERSATION_SESSION());
    }

    public static Localizable localizableWSS_0517_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0517.diag.cause.1");
    }

    /**
     * Non-permissable duplicate element on a Security Configuration Element
     * 
     */
    public static String WSS_0517_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0517_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0652_ERROR_PARSING_FILE(Object arg0) {
        return messageFactory.getMessage("WSS0652.error.parsing.file", arg0);
    }

    /**
     * WSS0652: Error parsing {0} file
     * 
     */
    public static String WSS_0652_ERROR_PARSING_FILE(Object arg0) {
        return localizer.localize(localizableWSS_0652_ERROR_PARSING_FILE(arg0));
    }

    public static Localizable localizableWSS_0608_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0608.diag.cause.1");
    }

    /**
     * The Reference Mechanism in the SecurityTokenReference is not supported
     * 
     */
    public static String WSS_0608_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0608_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0220_CANNOT_LOCATE_SYMMETRICKEY_FOR_DECRYPT() {
        return messageFactory.getMessage("WSS0220.cannot.locate.symmetrickey.for.decrypt");
    }

    /**
     * WSS0220: Unable to locate symmetric key for decryption using Callback Handler
     * 
     */
    public static String WSS_0220_CANNOT_LOCATE_SYMMETRICKEY_FOR_DECRYPT() {
        return localizer.localize(localizableWSS_0220_CANNOT_LOCATE_SYMMETRICKEY_FOR_DECRYPT());
    }

    public static Localizable localizableWSS_0285_ERROR_NO_ELEMENT() {
        return messageFactory.getMessage("WSS0285.error.NoElement");
    }

    /**
     * WSS0285: No elements exist with Id/WsuId
     * 
     */
    public static String WSS_0285_ERROR_NO_ELEMENT() {
        return localizer.localize(localizableWSS_0285_ERROR_NO_ELEMENT());
    }

    public static Localizable localizableWSS_0242_UNABLETO_LOCATE_SCT() {
        return messageFactory.getMessage("WSS0242.unableto.locate.SCT");
    }

    /**
     * WSS0242: Failed to locate SCT with given ID
     * 
     */
    public static String WSS_0242_UNABLETO_LOCATE_SCT() {
        return localizer.localize(localizableWSS_0242_UNABLETO_LOCATE_SCT());
    }

    public static Localizable localizableWSS_0803_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0803.diag.check.1");
    }

    /**
     * Ensure that SOAPMessage is set appropriately in the ProcessingContext
     * 
     */
    public static String WSS_0803_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0803_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0517_DUPLICATE_CONFIGURATION_ELEMENT(Object arg0) {
        return messageFactory.getMessage("WSS0517.duplicate.configuration.element", arg0);
    }

    /**
     * WSS0517: Multiple configuration elements not allowed on: {0}
     * 
     */
    public static String WSS_0517_DUPLICATE_CONFIGURATION_ELEMENT(Object arg0) {
        return localizer.localize(localizableWSS_0517_DUPLICATE_CONFIGURATION_ELEMENT(arg0));
    }

    public static Localizable localizableWSS_0399_SOAP_ENVELOPE_EXCEPTION() {
        return messageFactory.getMessage("WSS0399.soap.envelope.exception");
    }

    /**
     * WSS0399: Error getting SOAPEnvelope
     * 
     */
    public static String WSS_0399_SOAP_ENVELOPE_EXCEPTION() {
        return localizer.localize(localizableWSS_0399_SOAP_ENVELOPE_EXCEPTION());
    }

    public static Localizable localizableWSS_0165_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0165.diag.cause.1");
    }

    /**
     * XPath does not correspond to a DOM element
     * 
     */
    public static String WSS_0165_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0165_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0322_EXCEPTION_CREATING_SIGNATUREBLOCK() {
        return messageFactory.getMessage("WSS0322.exception.creating.signatureblock");
    }

    /**
     * WSS0322: Exception while creating Signature Header block
     * 
     */
    public static String WSS_0322_EXCEPTION_CREATING_SIGNATUREBLOCK() {
        return localizer.localize(localizableWSS_0322_EXCEPTION_CREATING_SIGNATUREBLOCK());
    }

    public static Localizable localizableWSS_0360_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0360.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0360_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0360_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0201_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0201.diag.cause.1");
    }

    /**
     * CallbackHandler to obtain Username at runtime was ineffective
     * 
     */
    public static String WSS_0201_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0201_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0204_ILLEGAL_HEADER_BLOCK(Object arg0) {
        return messageFactory.getMessage("WSS0204.illegal.header.block", arg0);
    }

    /**
     * WSS0204: Unexpected {0} element in the header
     * 
     */
    public static String WSS_0204_ILLEGAL_HEADER_BLOCK(Object arg0) {
        return localizer.localize(localizableWSS_0204_ILLEGAL_HEADER_BLOCK(arg0));
    }

    public static Localizable localizableWSS_0269_ERROR_ENCRYPTIONPOLICY_VERIFICATION() {
        return messageFactory.getMessage("WSS0269.error.Encryptionpolicy.verification");
    }

    /**
     * WSS0269: Encryption Policy verification error: Missing encryption element
     * 
     */
    public static String WSS_0269_ERROR_ENCRYPTIONPOLICY_VERIFICATION() {
        return localizer.localize(localizableWSS_0269_ERROR_ENCRYPTIONPOLICY_VERIFICATION());
    }

    public static Localizable localizableWSS_0311_PASSWD_DIGEST_COULDNOT_BE_CREATED(Object arg0) {
        return messageFactory.getMessage("WSS0311.passwd.digest.couldnot.be.created", arg0);
    }

    /**
     * WSS0311: Exception [ {0} ] while creating Password Digest.
     * 
     */
    public static String WSS_0311_PASSWD_DIGEST_COULDNOT_BE_CREATED(Object arg0) {
        return localizer.localize(localizableWSS_0311_PASSWD_DIGEST_COULDNOT_BE_CREATED(arg0));
    }

    public static Localizable localizableWSS_0167_SIGNATURE_VERIFICATION_FAILED() {
        return messageFactory.getMessage("WSS0167.signature.verification.failed");
    }

    /**
     * WSS0167: Signature Verification Failed
     * 
     */
    public static String WSS_0167_SIGNATURE_VERIFICATION_FAILED() {
        return localizer.localize(localizableWSS_0167_SIGNATURE_VERIFICATION_FAILED());
    }

    public static Localizable localizableWSS_0348_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0348.diag.cause.1");
    }

    /**
     * Error creating SOAPElement for EncryptedKeyHeaderBlock
     * 
     */
    public static String WSS_0348_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0348_DIAG_CAUSE_1());
    }

    public static Localizable localizableBSP_3104_ENVELOPED_SIGNATURE_DISCORAGED() {
        return messageFactory.getMessage("BSP3104.envelopedSignature.discoraged");
    }

    /**
     * BSP3104: Enveloped Signature is discouraged by BSP 1.1
     * 
     */
    public static String BSP_3104_ENVELOPED_SIGNATURE_DISCORAGED() {
        return localizer.localize(localizableBSP_3104_ENVELOPED_SIGNATURE_DISCORAGED());
    }

    public static Localizable localizableWSS_0141_UNABLETO_DECRYPT_SYMMETRIC_KEY(Object arg0) {
        return messageFactory.getMessage("WSS0141.unableto.decrypt.symmetric.key", arg0);
    }

    /**
     * WSS0141: Exception [ {0} ] while trying to decrypt symmetric key
     * 
     */
    public static String WSS_0141_UNABLETO_DECRYPT_SYMMETRIC_KEY(Object arg0) {
        return localizer.localize(localizableWSS_0141_UNABLETO_DECRYPT_SYMMETRIC_KEY(arg0));
    }

    public static Localizable localizableWSS_0190_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0190.diag.check.1");
    }

    /**
     * Check that the data references for encryption (in message) match the requirements
     * 
     */
    public static String WSS_0190_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0190_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0324_EXCEPTION_IN_GETTING_SIGNATUREVALUE() {
        return messageFactory.getMessage("WSS0324.exception.in.getting.signaturevalue");
    }

    /**
     * WSS0324: Exception in getting SignatureValue
     * 
     */
    public static String WSS_0324_EXCEPTION_IN_GETTING_SIGNATUREVALUE() {
        return localizer.localize(localizableWSS_0324_EXCEPTION_IN_GETTING_SIGNATUREVALUE());
    }

    public static Localizable localizableWSS_0704_NULL_SESSION_KEY() {
        return messageFactory.getMessage("WSS0704.null.session.key");
    }

    /**
     * WSS0704: Session KeyName has not been set on the SecurityEnvironment instance
     * 
     */
    public static String WSS_0704_NULL_SESSION_KEY() {
        return localizer.localize(localizableWSS_0704_NULL_SESSION_KEY());
    }

    public static Localizable localizableWSS_0752_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0752.diag.cause.2");
    }

    /**
     * The namespace URI of the SOAPElement passed does not conform to WSS Spec.
     * 
     */
    public static String WSS_0752_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0752_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0752_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0752.diag.cause.1");
    }

    /**
     * The localname of the SOAPElement passed is not "Embedded"
     * 
     */
    public static String WSS_0752_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0752_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0345_ERROR_CREATING_EDHB(Object arg0) {
        return messageFactory.getMessage("WSS0345.error.creating.edhb", arg0);
    }

    /**
     * WSS0345: Error creating EncryptedData Header Block due to {0}
     * 
     */
    public static String WSS_0345_ERROR_CREATING_EDHB(Object arg0) {
        return localizer.localize(localizableWSS_0345_ERROR_CREATING_EDHB(arg0));
    }

    public static Localizable localizableWSS_0362_ERROR_CREATING_RLHB(Object arg0) {
        return messageFactory.getMessage("WSS0362.error.creating.rlhb", arg0);
    }

    /**
     * WSS0362: Expected xenc:ReferenceList SOAPElement, found {0}
     * 
     */
    public static String WSS_0362_ERROR_CREATING_RLHB(Object arg0) {
        return localizer.localize(localizableWSS_0362_ERROR_CREATING_RLHB(arg0));
    }

    public static Localizable localizableWSS_0214_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0214.diag.cause.1");
    }

    /**
     * Invalid Username/Password pair in token
     * 
     */
    public static String WSS_0214_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0214_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0712_ERROR_ADJUST_SKEW_FRESHNESS_TIME() {
        return messageFactory.getMessage("WSS0712.error.adjust.skew.freshness.time");
    }

    /**
     * WSS0712: An Error occurred while adjusting Current time with Skewed & Freshness time .
     * 
     */
    public static String WSS_0712_ERROR_ADJUST_SKEW_FRESHNESS_TIME() {
        return localizer.localize(localizableWSS_0712_ERROR_ADJUST_SKEW_FRESHNESS_TIME());
    }

    public static Localizable localizableWSS_0419_SAML_SIGNATURE_VERIFY_FAILED() {
        return messageFactory.getMessage("WSS0419.saml.signature.verify.failed");
    }

    /**
     * WSS0419: Exception  during Signature verfication in SAML Assertion
     * 
     */
    public static String WSS_0419_SAML_SIGNATURE_VERIFY_FAILED() {
        return localizer.localize(localizableWSS_0419_SAML_SIGNATURE_VERIFY_FAILED());
    }

    public static Localizable localizableWSS_0230_UNSUPPORTED_VALIDATING_SAML_ISSUER() {
        return messageFactory.getMessage("WSS0230.unsupported.Validating.SAMLIssuer");
    }

    /**
     * WSS0230: SAML Issuer Validation not yet supported.
     * 
     */
    public static String WSS_0230_UNSUPPORTED_VALIDATING_SAML_ISSUER() {
        return localizer.localize(localizableWSS_0230_UNSUPPORTED_VALIDATING_SAML_ISSUER());
    }

    public static Localizable localizableWSS_0385_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0385.diag.check.1");
    }

    /**
     * Check that the Localname and NamespaceURI of the SOAPElement used to create Timestamp() are correct as per spec.
     * 
     */
    public static String WSS_0385_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0385_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0330_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0330.diag.check.1");
    }

    /**
     * Make sure first child of wsse:UsernameToken is Username in wsse namespace
     * 
     */
    public static String WSS_0330_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0330_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0656_KEYSTORE_FILE_NOTFOUND() {
        return messageFactory.getMessage("WSS0656.keystore.file.notfound");
    }

    /**
     * WSS0656: Keystore file not found
     * 
     */
    public static String WSS_0656_KEYSTORE_FILE_NOTFOUND() {
        return localizer.localize(localizableWSS_0656_KEYSTORE_FILE_NOTFOUND());
    }

    public static Localizable localizableWSS_0229_FAILED_VALIDATING_TIME_STAMP() {
        return messageFactory.getMessage("WSS0229.failed.Validating.TimeStamp");
    }

    /**
     * WSS0229: Exception occured in validating Timestamp
     * 
     */
    public static String WSS_0229_FAILED_VALIDATING_TIME_STAMP() {
        return localizer.localize(localizableWSS_0229_FAILED_VALIDATING_TIME_STAMP());
    }

    public static Localizable localizableWSS_0350_ERROR_SETTING_CIPHERVALUE(Object arg0) {
        return messageFactory.getMessage("WSS0350.error.setting.ciphervalue", arg0);
    }

    /**
     * WSS0350: Error setting CipherValue in EncryptedKey due to {0}
     * 
     */
    public static String WSS_0350_ERROR_SETTING_CIPHERVALUE(Object arg0) {
        return localizer.localize(localizableWSS_0350_ERROR_SETTING_CIPHERVALUE(arg0));
    }

    public static Localizable localizableWSS_0512_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0512.diag.check.1");
    }

    /**
     * Check that the configuration file is consistent with the security configuration schema
     * 
     */
    public static String WSS_0512_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0512_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0247_FAILED_RESOLVE_DERIVED_KEY_TOKEN() {
        return messageFactory.getMessage("WSS0247.failed.resolve.DerivedKeyToken");
    }

    /**
     * WSS0247: Failed to resolve Derived Key Token
     * 
     */
    public static String WSS_0247_FAILED_RESOLVE_DERIVED_KEY_TOKEN() {
        return localizer.localize(localizableWSS_0247_FAILED_RESOLVE_DERIVED_KEY_TOKEN());
    }

    public static Localizable localizableWSS_0386_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0386.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0386_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0386_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0603_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0603.diag.check.1");
    }

    /**
     * Refer your XPathAPI documentation
     * 
     */
    public static String WSS_0603_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0603_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0318_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0318.diag.cause.1");
    }

    /**
     * Error while parsing and creating the KeyInfo instance
     * 
     */
    public static String WSS_0318_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0318_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0276_INVALID_POLICY_NO_TIMESTAMP_SEC_HEADER() {
        return messageFactory.getMessage("WSS0276.invalid.policy.NoTimestamp.SecHeader");
    }

    /**
     * WSS0276: Message does not conform to configured policy: Timestamp element not found in security header
     * 
     */
    public static String WSS_0276_INVALID_POLICY_NO_TIMESTAMP_SEC_HEADER() {
        return localizer.localize(localizableWSS_0276_INVALID_POLICY_NO_TIMESTAMP_SEC_HEADER());
    }

    public static Localizable localizableBSP_5624_ENCRYPTEDDATA_IDATTRIBUTE() {
        return messageFactory.getMessage("BSP5624.encrypteddata.idattribute");
    }

    /**
     * BSP 5624 : EncryptedData element MUST have ID attribute.
     * 
     */
    public static String BSP_5624_ENCRYPTEDDATA_IDATTRIBUTE() {
        return localizer.localize(localizableBSP_5624_ENCRYPTEDDATA_IDATTRIBUTE());
    }

    public static Localizable localizableWSS_0716_FAILED_VALIDATE_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS0716.failed.validateSAMLAssertion");
    }

    /**
     * WSS0716: Failed to validate SAML Assertion
     * 
     */
    public static String WSS_0716_FAILED_VALIDATE_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_0716_FAILED_VALIDATE_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_0342_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0342.diag.check.1");
    }

    /**
     * Check that valueType for BinarySecurity token is valid as per spec.
     * 
     */
    public static String WSS_0342_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0342_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0703_UNSUPPORTED_OPERATION() {
        return messageFactory.getMessage("WSS0703.unsupported.operation");
    }

    /**
     * WSS0703: Unsupported Operation - insertKey(SecurityTokenReference, SecurableSoapMessage) - on KeyNameStrategy
     * 
     */
    public static String WSS_0703_UNSUPPORTED_OPERATION() {
        return localizer.localize(localizableWSS_0703_UNSUPPORTED_OPERATION());
    }

    public static Localizable localizableWSS_0148_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0148.diag.cause.1");
    }

    /**
     * Exception while trying to pretty print using transform
     * 
     */
    public static String WSS_0148_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0148_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0343_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0343.diag.check.1");
    }

    /**
     * Check that all required values are set on the Binary Security Token, including TextNode value. 
     * 
     */
    public static String WSS_0343_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0343_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0336_CANNOT_LOCATE_PUBLICKEY_FOR_SIGNATURE_VERIFICATION() {
        return messageFactory.getMessage("WSS0336.cannot.locate.publickey.for.signature.verification");
    }

    /**
     * WSS0336:Couldn't locate the public key for signature verification 
     * 
     */
    public static String WSS_0336_CANNOT_LOCATE_PUBLICKEY_FOR_SIGNATURE_VERIFICATION() {
        return localizer.localize(localizableWSS_0336_CANNOT_LOCATE_PUBLICKEY_FOR_SIGNATURE_VERIFICATION());
    }

    public static Localizable localizableWSS_0382_ERROR_APPENDING_OBJECT(Object arg0) {
        return messageFactory.getMessage("WSS0382.error.appending.object", arg0);
    }

    /**
     * WSS0382: Can not append ds:Object due to {0}
     * 
     */
    public static String WSS_0382_ERROR_APPENDING_OBJECT(Object arg0) {
        return localizer.localize(localizableWSS_0382_ERROR_APPENDING_OBJECT(arg0));
    }

    public static Localizable localizableWSS_0235_FAILED_LOCATE_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS0235.failed.locate.SAMLAssertion");
    }

    /**
     * WSS0235: failed to locate SAML Assertion
     * 
     */
    public static String WSS_0235_FAILED_LOCATE_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_0235_FAILED_LOCATE_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_0314_CERT_NOT_TRUSTED_REMOTE_CERT() {
        return messageFactory.getMessage("WSS0314.cert.not.trusted.remote.cert");
    }

    /**
     * WSS0314: Certificate is not a trusted remote certificate
     * 
     */
    public static String WSS_0314_CERT_NOT_TRUSTED_REMOTE_CERT() {
        return localizer.localize(localizableWSS_0314_CERT_NOT_TRUSTED_REMOTE_CERT());
    }

    public static Localizable localizableWSS_0185_FILTERPARAMETER_NOT_SET(Object arg0) {
        return messageFactory.getMessage("WSS0185.filterparameter.not.set", arg0);
    }

    /**
     * WSS0185: filter parameter [ {0} ] was not set
     * 
     */
    public static String WSS_0185_FILTERPARAMETER_NOT_SET(Object arg0) {
        return localizer.localize(localizableWSS_0185_FILTERPARAMETER_NOT_SET(arg0));
    }

    public static Localizable localizableWSS_0718_EXCEPTION_INVOKING_SAML_HANDLER() {
        return messageFactory.getMessage("WSS0718.exception.invoking.samlHandler");
    }

    /**
     * WSS0718: An exception occured when invoking the user supplied SAML CallbackHandler
     * 
     */
    public static String WSS_0718_EXCEPTION_INVOKING_SAML_HANDLER() {
        return localizer.localize(localizableWSS_0718_EXCEPTION_INVOKING_SAML_HANDLER());
    }

    public static Localizable localizableWSS_0355_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0355.diag.check.1");
    }

    /**
     * Check that a non-null SOAPElement is passed to addXXXKeyValue()
     * 
     */
    public static String WSS_0355_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0355_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0602_ILLEGAL_X_509_DATA(Object arg0) {
        return messageFactory.getMessage("WSS0602.illegal.x509.data", arg0);
    }

    /**
     * WSS0602: Key can not be located for the X509Data (ds:KeyInfo) due to {0}
     * 
     */
    public static String WSS_0602_ILLEGAL_X_509_DATA(Object arg0) {
        return localizer.localize(localizableWSS_0602_ILLEGAL_X_509_DATA(arg0));
    }

    public static Localizable localizableWSS_0169_TOKEN_NOT_SET_PARAMETER_LIST() {
        return messageFactory.getMessage("WSS0169.token.not.set.parameter.list");
    }

    /**
     * WSS0169: Token to be exported was not set in the Parameter List
     * 
     */
    public static String WSS_0169_TOKEN_NOT_SET_PARAMETER_LIST() {
        return localizer.localize(localizableWSS_0169_TOKEN_NOT_SET_PARAMETER_LIST());
    }

    public static Localizable localizableWSS_0356_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0356.diag.check.1");
    }

    /**
     * Check that a non-null SOAPElement is passed to addX509Data()
     * 
     */
    public static String WSS_0356_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0356_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0509_DEFAULTS_ALREADY_SET() {
        return messageFactory.getMessage("WSS0509.defaults.already.set");
    }

    /**
     * WSS0509: Can not specify custom settings after default settings are added
     * 
     */
    public static String WSS_0509_DEFAULTS_ALREADY_SET() {
        return localizer.localize(localizableWSS_0509_DEFAULTS_ALREADY_SET());
    }

    public static Localizable localizableWSS_0221_CANNOT_LOCATE_CERT(Object arg0) {
        return messageFactory.getMessage("WSS0221.cannot.locate.cert", arg0);
    }

    /**
     * WSS0221: Unable to locate matching certificate for {0} using Callback Handler.
     * 
     */
    public static String WSS_0221_CANNOT_LOCATE_CERT(Object arg0) {
        return localizer.localize(localizableWSS_0221_CANNOT_LOCATE_CERT(arg0));
    }

    public static Localizable localizableWSS_0383_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0383.diag.cause.1");
    }

    /**
     * Owner document of ds:Signature SOAPElement is null
     * 
     */
    public static String WSS_0383_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0383_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0349_ERROR_CREATING_EKHB(Object arg0) {
        return messageFactory.getMessage("WSS0349.error.creating.ekhb", arg0);
    }

    /**
     * WSS0349: Expected xenc:EncryptedKey SOAPElement, found {0}
     * 
     */
    public static String WSS_0349_ERROR_CREATING_EKHB(Object arg0) {
        return localizer.localize(localizableWSS_0349_ERROR_CREATING_EKHB(arg0));
    }

    public static Localizable localizableWSS_0332_USERNAMETOKEN_NULL_USERNAME() {
        return messageFactory.getMessage("WSS0332.usernametoken.null.username");
    }

    /**
     * WSS0332: Username token does not contain the username
     * 
     */
    public static String WSS_0332_USERNAMETOKEN_NULL_USERNAME() {
        return localizer.localize(localizableWSS_0332_USERNAMETOKEN_NULL_USERNAME());
    }

    public static Localizable localizableWSS_0264_FAILEDTO_FIRST_PRIMARY_POLICY() {
        return messageFactory.getMessage("WSS0264.failedto.first.primary.policy");
    }

    /**
     * WSS0265: Failed to get First Primary Policy
     * 
     */
    public static String WSS_0264_FAILEDTO_FIRST_PRIMARY_POLICY() {
        return localizer.localize(localizableWSS_0264_FAILEDTO_FIRST_PRIMARY_POLICY());
    }

    public static Localizable localizableWSS_0706_NO_MATCHING_CERT(Object arg0) {
        return messageFactory.getMessage("WSS0706.no.matching.cert", arg0);
    }

    /**
     * WSS0706: Error: No Matching Certificate for : {0} found in KeyStore or TrustStore.
     * 
     */
    public static String WSS_0706_NO_MATCHING_CERT(Object arg0) {
        return localizer.localize(localizableWSS_0706_NO_MATCHING_CERT(arg0));
    }

    public static Localizable localizableWSS_0657_CONFIG_FILE_NOTFOUND() {
        return messageFactory.getMessage("WSS0657.config.file.notfound");
    }

    /**
     * WSS0657: tomcat-users.xml can not be found
     * 
     */
    public static String WSS_0657_CONFIG_FILE_NOTFOUND() {
        return localizer.localize(localizableWSS_0657_CONFIG_FILE_NOTFOUND());
    }

    public static Localizable localizableWSS_0801_ILLEGAL_SECURITYPOLICY() {
        return messageFactory.getMessage("WSS0801.illegal.securitypolicy");
    }

    /**
     * Illegal SecurityPolicy Type 
     * 
     */
    public static String WSS_0801_ILLEGAL_SECURITYPOLICY() {
        return localizer.localize(localizableWSS_0801_ILLEGAL_SECURITYPOLICY());
    }

    public static Localizable localizableWSS_0510_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0510.diag.cause.1");
    }

    /**
     * Named keys can not be used for conveying public key information
     * 
     */
    public static String WSS_0510_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0510_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0221_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0221.diag.check.1");
    }

    /**
     * Check the handler implementation for SignatureKeyCallback and/or EncryptionKeyCallback, check keystores and truststores
     * 
     */
    public static String WSS_0221_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0221_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0656_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0656.diag.cause.2");
    }

    /**
     * A Keystore file does not exist in $user.home
     * 
     */
    public static String WSS_0656_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0656_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0754_TOKEN_ALREADY_SET() {
        return messageFactory.getMessage("WSS0754.token.already.set");
    }

    /**
     * WSS0754: Token on EmbeddedReference has already been set
     * 
     */
    public static String WSS_0754_TOKEN_ALREADY_SET() {
        return localizer.localize(localizableWSS_0754_TOKEN_ALREADY_SET());
    }

    public static Localizable localizableWSS_0656_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0656.diag.cause.1");
    }

    /**
     * The Keystore URL is not specified/invalid in server.xml 
     * 
     */
    public static String WSS_0656_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0656_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0601_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0601.diag.cause.1");
    }

    /**
     * A Key can not be located in SecurityEnvironment for the KeyValue
     * 
     */
    public static String WSS_0601_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0601_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0715_EXCEPTION_CREATING_NEWINSTANCE() {
        return messageFactory.getMessage("WSS0715.exception.creating.newinstance");
    }

    /**
     * WSS0715: Exception occured while creating new instance
     * 
     */
    public static String WSS_0715_EXCEPTION_CREATING_NEWINSTANCE() {
        return localizer.localize(localizableWSS_0715_EXCEPTION_CREATING_NEWINSTANCE());
    }

    public static Localizable localizableWSS_0222_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0222.diag.check.1");
    }

    /**
     * Check keystore path and ensure that the right keys are present
     * 
     */
    public static String WSS_0222_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0222_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0657_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0657.diag.cause.1");
    }

    /**
     * tomcat-users.xml can not be found
     * 
     */
    public static String WSS_0657_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0657_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0368_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0368.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0368_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0368_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0408_SAML_ELEMENT_OVERFLOW() {
        return messageFactory.getMessage("WSS0408.saml.element.overflow");
    }

    /**
     * WSS0408: Attempted to add more elements than allowed
     * 
     */
    public static String WSS_0408_SAML_ELEMENT_OVERFLOW() {
        return localizer.localize(localizableWSS_0408_SAML_ELEMENT_OVERFLOW());
    }

    public static Localizable localizableWSS_0340_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0340.diag.cause.1");
    }

    /**
     * Creation time cannot be ahead of current UTC time
     * 
     */
    public static String WSS_0340_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0340_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0337_UNSUPPORTED_DIRECTREF_MECHANISM(Object arg0) {
        return messageFactory.getMessage("WSS0337.unsupported.directref.mechanism", arg0);
    }

    /**
     * WSS0337: Unsupported DirectReference mechanism {0}
     * 
     */
    public static String WSS_0337_UNSUPPORTED_DIRECTREF_MECHANISM(Object arg0) {
        return localizer.localize(localizableWSS_0337_UNSUPPORTED_DIRECTREF_MECHANISM(arg0));
    }

    public static Localizable localizableWSS_0296_NULL_CHAIN_CERT() {
        return messageFactory.getMessage("WSS0296.null.chain.cert");
    }

    /**
     * WSS0296: Error: Empty certificate chain returned by PrivateKeyCallback.
     * 
     */
    public static String WSS_0296_NULL_CHAIN_CERT() {
        return localizer.localize(localizableWSS_0296_NULL_CHAIN_CERT());
    }

    public static Localizable localizableWSS_0369_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0369.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0369_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0369_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0396_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0396.diag.cause.1");
    }

    /**
     * Element should be a child of Security Header
     * 
     */
    public static String WSS_0396_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0396_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0507_TARGET_NOT_SPECIFIED_DECRYPT() {
        return messageFactory.getMessage("WSS0507.target.not.specified.decrypt");
    }

    /**
     * WSS0507: Target not specified in decrypt.
     * 
     */
    public static String WSS_0507_TARGET_NOT_SPECIFIED_DECRYPT() {
        return localizer.localize(localizableWSS_0507_TARGET_NOT_SPECIFIED_DECRYPT());
    }

    public static Localizable localizableWSS_0199_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0199.diag.check.1");
    }

    /**
     * Check that non-null creation time is used to instantiate the filter
     * 
     */
    public static String WSS_0199_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0199_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0705_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0705.diag.cause.1");
    }

    /**
     * setCertificate(X509Certificate) is not supported on KeyNameStrategy
     * 
     */
    public static String WSS_0705_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0705_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0708_NO_DIGEST_ALGORITHM() {
        return messageFactory.getMessage("WSS0708.no.digest.algorithm");
    }

    /**
     * WSS0708: Digest algorithm SHA-1 not found
     * 
     */
    public static String WSS_0708_NO_DIGEST_ALGORITHM() {
        return localizer.localize(localizableWSS_0708_NO_DIGEST_ALGORITHM());
    }

    public static Localizable localizableWSS_0299_X_509_NOT_VALID() {
        return messageFactory.getMessage("WSS0299.X509.notValid");
    }

    /**
     * WSS0299: X509Certificate's Validity Failed.
     * 
     */
    public static String WSS_0299_X_509_NOT_VALID() {
        return localizer.localize(localizableWSS_0299_X_509_NOT_VALID());
    }

    public static Localizable localizableWSS_0406_SAML_INVALID_ELEMENT() {
        return messageFactory.getMessage("WSS0406.saml.invalid.element");
    }

    /**
     * WSS0406: Input has invalid element
     * 
     */
    public static String WSS_0406_SAML_INVALID_ELEMENT() {
        return localizer.localize(localizableWSS_0406_SAML_INVALID_ELEMENT());
    }

    public static Localizable localizableWSS_0353_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0353.diag.cause.1");
    }

    /**
     * No CipherValue element(s) are present in CipherData
     * 
     */
    public static String WSS_0353_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0353_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0508_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0508.diag.check.2");
    }

    /**
     * Check that no sender or receiver settings are programmatically added
     * 
     */
    public static String WSS_0508_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0508_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0234_FAILED_VALIDATE_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS0234.failed.Validate.SAMLAssertion");
    }

    /**
     * WSS0234: An Error occurred while Validating SAML Assertion in Policy.
     * 
     */
    public static String WSS_0234_FAILED_VALIDATE_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_0234_FAILED_VALIDATE_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_0508_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0508.diag.check.1");
    }

    /**
     * Check that no sender operations or receiver requirements are specified in a config. file
     * 
     */
    public static String WSS_0508_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0508_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0156_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0156.diag.check.1");
    }

    /**
     * Check that the token contains a valid Certificate
     * 
     */
    public static String WSS_0156_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0156_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0134_UNABLETO_INITIALIZE_XML_CIPHER() {
        return messageFactory.getMessage("WSS0134.unableto.initialize.xml.cipher");
    }

    /**
     * WSS0134: Unable to initialize XML Cipher
     * 
     */
    public static String WSS_0134_UNABLETO_INITIALIZE_XML_CIPHER() {
        return localizer.localize(localizableWSS_0134_UNABLETO_INITIALIZE_XML_CIPHER());
    }

    public static Localizable localizableWSS_0183_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0183.diag.cause.1");
    }

    /**
     * Could not locate a valid symmetric key needed for decryption.
     * 
     */
    public static String WSS_0183_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0183_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0140_ENCRYPTEDKEY_DOESNOT_CONTAIN_CHIPERDATA() {
        return messageFactory.getMessage("WSS0140.encryptedkey.doesnot.contain.chiperdata");
    }

    /**
     * WSS0140: Message xenc:EncryptedKey does not contain xenc:CipherData/xenc:CipherValue
     * 
     */
    public static String WSS_0140_ENCRYPTEDKEY_DOESNOT_CONTAIN_CHIPERDATA() {
        return localizer.localize(localizableWSS_0140_ENCRYPTEDKEY_DOESNOT_CONTAIN_CHIPERDATA());
    }

    public static Localizable localizableWSS_0184_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0184.diag.cause.1");
    }

    /**
     * Could not retrieve security domain from the Securable SOAP message
     * 
     */
    public static String WSS_0184_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0184_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0310_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0310.diag.cause.1");
    }

    /**
     * NoSuchAlgorithmException: Invalid algorithm
     * 
     */
    public static String WSS_0310_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0310_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0339_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0339.diag.check.1");
    }

    /**
     * Check ds:KeyInfo matches schema
     * 
     */
    public static String WSS_0339_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0339_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0293_FAILED_RSA_KEY_VALUE() {
        return messageFactory.getMessage("WSS0293.failed.RSAKeyValue");
    }

    /**
     * WSS0293: Exception occured while trying to get RSA Key Value
     * 
     */
    public static String WSS_0293_FAILED_RSA_KEY_VALUE() {
        return localizer.localize(localizableWSS_0293_FAILED_RSA_KEY_VALUE());
    }

    public static Localizable localizableWSS_0366_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0366.diag.cause.1");
    }

    /**
     * More than one xenc:EncryptedData has the same Id attribute value
     * 
     */
    public static String WSS_0366_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0366_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0281_UNSUPPORTED_OPERATION() {
        return messageFactory.getMessage("WSS0281.unsupported.operation");
    }

    /**
     * WSS0281: Unsupported operation get Token Value of username Token
     * 
     */
    public static String WSS_0281_UNSUPPORTED_OPERATION() {
        return localizer.localize(localizableWSS_0281_UNSUPPORTED_OPERATION());
    }

    public static Localizable localizableWSS_0364_ERROR_APACHE_XPATH_API(Object arg0) {
        return messageFactory.getMessage("WSS0364.error.apache.xpathAPI", arg0);
    }

    /**
     * WSS0364: Can not find xenc:EncryptedData elements due to {0}
     * 
     */
    public static String WSS_0364_ERROR_APACHE_XPATH_API(Object arg0) {
        return localizer.localize(localizableWSS_0364_ERROR_APACHE_XPATH_API(arg0));
    }

    public static Localizable localizableWSS_0803_SOAPMESSAGE_NOTSET() {
        return messageFactory.getMessage("WSS0803.soapmessage.notset");
    }

    /**
     * javax.xml.soap.SOAPMessage parameter not set in the ProcessingContext
     * 
     */
    public static String WSS_0803_SOAPMESSAGE_NOTSET() {
        return localizer.localize(localizableWSS_0803_SOAPMESSAGE_NOTSET());
    }

    public static Localizable localizableWSS_0204_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0204.diag.check.1");
    }

    /**
     * Check that the message is SOAP Security specification compliant
     * 
     */
    public static String WSS_0204_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0204_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0390_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0390.diag.check.1");
    }

    /**
     * Refer your J2SE Documentation
     * 
     */
    public static String WSS_0390_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0390_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0169_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0169.diag.check.1");
    }

    /**
     * Check that direct referencestrategy is set before exporting the certificate
     * 
     */
    public static String WSS_0169_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0169_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0384_ERROR_CREATING_TIMESTAMP(Object arg0) {
        return messageFactory.getMessage("WSS0384.error.creating.timestamp", arg0);
    }

    /**
     * WSS0384: Can not create Timestamp due to {0}
     * 
     */
    public static String WSS_0384_ERROR_CREATING_TIMESTAMP(Object arg0) {
        return localizer.localize(localizableWSS_0384_ERROR_CREATING_TIMESTAMP(arg0));
    }

    public static Localizable localizableBSP_3058_STR_VALUE_TYPE_NOTEMPTY() {
        return messageFactory.getMessage("BSP3058.str.value.type.notempty");
    }

    /**
     * BSP 3058 : ValueType attribute for Reference element under wsse:SecurityTokenReference MUST have a value specified.
     * 
     */
    public static String BSP_3058_STR_VALUE_TYPE_NOTEMPTY() {
        return localizer.localize(localizableBSP_3058_STR_VALUE_TYPE_NOTEMPTY());
    }

    public static Localizable localizableWSS_0196_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0196.diag.cause.1");
    }

    /**
     * An instance of SecurityEnvironment class for the operating environment was not set on SecurableSoapMessage
     * 
     */
    public static String WSS_0196_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0196_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0205_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0205.diag.check.1");
    }

    /**
     * Check that the message meets the security requirements
     * 
     */
    public static String WSS_0205_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0205_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0391_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0391.diag.check.1");
    }

    /**
     * Check that the Localname and NamespaceURI of the SOAPElement used to create X509SecurityToken are valid (as per spec.)
     * 
     */
    public static String WSS_0391_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0391_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0346_ERROR_CREATING_EDHB(Object arg0) {
        return messageFactory.getMessage("WSS0346.error.creating.edhb", arg0);
    }

    /**
     * WSS0346: Expected xenc:EncryptedData SOAPElement, found {0}
     * 
     */
    public static String WSS_0346_ERROR_CREATING_EDHB(Object arg0) {
        return localizer.localize(localizableWSS_0346_ERROR_CREATING_EDHB(arg0));
    }

    public static Localizable localizableWSS_0197_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0197.diag.cause.1");
    }

    /**
     * Can not instantiate filter with null KeyInfoStrategy
     * 
     */
    public static String WSS_0197_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0197_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0323_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0323.diag.cause.1");
    }

    /**
     * Exception while trying to sign
     * 
     */
    public static String WSS_0323_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0323_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0606_STR_TRANSFORM_EXCEPTION() {
        return messageFactory.getMessage("WSS0606.str.transform.exception");
    }

    /**
     * WSS0606: Input Node Set to STR Transform is empty
     * 
     */
    public static String WSS_0606_STR_TRANSFORM_EXCEPTION() {
        return localizer.localize(localizableWSS_0606_STR_TRANSFORM_EXCEPTION());
    }

    public static Localizable localizableWSS_0379_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0379.diag.cause.1");
    }

    /**
     * SOAPElement passed to SecurityTokenReference() is not a valid SecurityTokenReference element as per spec. 
     * 
     */
    public static String WSS_0379_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0379_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0700_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0700.diag.check.2");
    }

    /**
     * Check that SetSecurityEnvironmentFilter is applied on SecurableSoapMessage
     * 
     */
    public static String WSS_0700_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0700_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0512_ILLEGAL_ATTRIBUTE_NAME(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0512.illegal.attribute.name", arg0, arg1);
    }

    /**
     * WSS0512: Unexpected attribute: {0} on {1}
     * 
     */
    public static String WSS_0512_ILLEGAL_ATTRIBUTE_NAME(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0512_ILLEGAL_ATTRIBUTE_NAME(arg0, arg1));
    }

    public static Localizable localizableWSS_0700_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0700.diag.check.1");
    }

    /**
     * Check that setSecurityEnvironment() is called on the SecurableSoapMessage
     * 
     */
    public static String WSS_0700_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0700_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_5620_ENCRYPTEDDATA_URI() {
        return messageFactory.getMessage("BSP5620.encrypteddata.uri");
    }

    /**
     * BSP 5620 : Data encryption algorithm MUST be one of "http://www.w3.org/2001/04/xmlenc#tripledes-cbc","http://www.w3.org/2001/04/xmlenc#aes128-cbc" or "http://www.w3.org/2001/04/xmlenc#aes256-cbc".
     * 
     */
    public static String BSP_5620_ENCRYPTEDDATA_URI() {
        return localizer.localize(localizableBSP_5620_ENCRYPTEDDATA_URI());
    }

    public static Localizable localizableWSS_0126_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0126.diag.check.1");
    }

    /**
     * Check that the signature algorithm is RSA
     * 
     */
    public static String WSS_0126_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0126_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0183_COULDNOT_LOCATE_SYMMETRICKEY() {
        return messageFactory.getMessage("WSS0183.couldnot.locate.symmetrickey");
    }

    /**
     * WSS0183: The symmetric key required for decryption was not found.
     * 
     */
    public static String WSS_0183_COULDNOT_LOCATE_SYMMETRICKEY() {
        return localizer.localize(localizableWSS_0183_COULDNOT_LOCATE_SYMMETRICKEY());
    }

    public static Localizable localizableWSS_0217_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0217.diag.check.1");
    }

    /**
     * Check the handler implementation 
     * 
     */
    public static String WSS_0217_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0217_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0131_DSKEYINFO_DOESNOT_CONTAIN_REFTO_SECTOKEN() {
        return messageFactory.getMessage("WSS0131.dskeyinfo.doesnot.contain.refto.sectoken");
    }

    /**
     * WSS0131: Message ds:KeyInfo does not contain a reference to a security token
     * 
     */
    public static String WSS_0131_DSKEYINFO_DOESNOT_CONTAIN_REFTO_SECTOKEN() {
        return localizer.localize(localizableWSS_0131_DSKEYINFO_DOESNOT_CONTAIN_REFTO_SECTOKEN());
    }

    public static Localizable localizableWSS_0756_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0756.diag.check.1");
    }

    /**
     * Check your SAAJ API Documentation
     * 
     */
    public static String WSS_0756_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0756_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0184_SECURITYDOMAIN_NULL() {
        return messageFactory.getMessage("WSS0184.securitydomain.null");
    }

    /**
     * WSS0184: Security Domain not set of Message
     * 
     */
    public static String WSS_0184_SECURITYDOMAIN_NULL() {
        return localizer.localize(localizableWSS_0184_SECURITYDOMAIN_NULL());
    }

    public static Localizable localizableBSP_5622_ENCRYPTEDKEY_MIMETYPE(Object arg0) {
        return messageFactory.getMessage("BSP5622.encryptedkey.mimetype", arg0);
    }

    /**
     * BSP 5622 :  EncryptedKey element with ID {0} MUST NOT contain an MimeType attribute.
     * 
     */
    public static String BSP_5622_ENCRYPTEDKEY_MIMETYPE(Object arg0) {
        return localizer.localize(localizableBSP_5622_ENCRYPTEDKEY_MIMETYPE(arg0));
    }

    public static Localizable localizableWSS_0218_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0218.diag.check.1");
    }

    /**
     * Check the handler implementation for SignatureKeyCallback.DefaultPrivKeyCertRequest
     * 
     */
    public static String WSS_0218_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0218_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_3064_STR_NOT_REF_STR_EMBEDDED() {
        return messageFactory.getMessage("BSP3064.str.not.ref.str.embedded");
    }

    /**
     * BSP 3064 : Reference element under wsse:SecurityTokenReference MUST NOT reference wsse:Embedded element which is  a child element of another wsse:SecurityTokenReference element.
     * 
     */
    public static String BSP_3064_STR_NOT_REF_STR_EMBEDDED() {
        return localizer.localize(localizableBSP_3064_STR_NOT_REF_STR_EMBEDDED());
    }

    public static Localizable localizableWSS_0336_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0336.diag.cause.1");
    }

    /**
     * Can't locate public key
     * 
     */
    public static String WSS_0336_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0336_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0398_SOAP_BODY_EXCEPTION() {
        return messageFactory.getMessage("WSS0398.soap.body.exception");
    }

    /**
     * WSS0398: Error getting SOAPBody
     * 
     */
    public static String WSS_0398_SOAP_BODY_EXCEPTION() {
        return localizer.localize(localizableWSS_0398_SOAP_BODY_EXCEPTION());
    }

    public static Localizable localizableWSS_0223_FAILED_CERTIFICATE_VALIDATION() {
        return messageFactory.getMessage("WSS0223.failed.certificate.validation");
    }

    /**
     * WSS0223: Certificate validation failed
     * 
     */
    public static String WSS_0223_FAILED_CERTIFICATE_VALIDATION() {
        return localizer.localize(localizableWSS_0223_FAILED_CERTIFICATE_VALIDATION());
    }

    public static Localizable localizableWSS_0338_UNSUPPORTED_REFERENCE_MECHANISM() {
        return messageFactory.getMessage("WSS0338.unsupported.reference.mechanism");
    }

    /**
     * WSS0338: Unsupported Reference mechanism
     * 
     */
    public static String WSS_0338_UNSUPPORTED_REFERENCE_MECHANISM() {
        return localizer.localize(localizableWSS_0338_UNSUPPORTED_REFERENCE_MECHANISM());
    }

    public static Localizable localizableWSS_0804_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0804.diag.check.1");
    }

    /**
     * Ensure that the Callback handler is set appropriately in the ProcessingContext
     * 
     */
    public static String WSS_0804_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0804_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0302_UNABLETO_CREATE_X_509_CERT() {
        return messageFactory.getMessage("WSS0302.unableto.create.x509cert");
    }

    /**
     * WSS0302: Unable to create X509Certificate from data in token
     * 
     */
    public static String WSS_0302_UNABLETO_CREATE_X_509_CERT() {
        return localizer.localize(localizableWSS_0302_UNABLETO_CREATE_X_509_CERT());
    }

    public static Localizable localizableWSS_0519_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0519.diag.cause.1");
    }

    /**
     * Non-permissable/missing attribute value
     * 
     */
    public static String WSS_0519_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0519_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0361_DIAG_CHECK_3() {
        return messageFactory.getMessage("WSS0361.diag.check.3");
    }

    /**
     * Check that a non-Null Document is passed to the ReferenceListHeaderBlock()
     * 
     */
    public static String WSS_0361_DIAG_CHECK_3() {
        return localizer.localize(localizableWSS_0361_DIAG_CHECK_3());
    }

    public static Localizable localizableWSS_0361_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0361.diag.check.2");
    }

    /**
     * Check that the QName specified is not malformed (Ref J2SE Documention for more)
     * 
     */
    public static String WSS_0361_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0361_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0361_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0361.diag.check.1");
    }

    /**
     * Check that the Namespace specified does not contain any illegal characters as per XML 1.0 specification 
     * 
     */
    public static String WSS_0361_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0361_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0202_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0202.diag.cause.1");
    }

    /**
     * No wsse:Security in the message
     * 
     */
    public static String WSS_0202_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0202_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0167_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0167.diag.cause.1");
    }

    /**
     * Invalid signature; verification failed
     * 
     */
    public static String WSS_0167_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0167_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0349_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0349.diag.cause.1");
    }

    /**
     * Invalid SOAPElement passed to EncryptedKeyHeaderBlock()
     * 
     */
    public static String WSS_0349_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0349_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0191_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0191.diag.check.2");
    }

    /**
     * Check that a valid KeyStore URL is used to instantiate the SecurityEnvironment and it contains a matching SecretKey
     * 
     */
    public static String WSS_0191_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0191_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0191_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0191.diag.check.1");
    }

    /**
     * Check that ExportEncryptedKeyFilter is called before 
     * 
     */
    public static String WSS_0191_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0191_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0753_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0753.diag.cause.1");
    }

    /**
     * An embedded token in wsse:Embedded element is missing
     * 
     */
    public static String WSS_0753_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0753_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0228_INVALID_AHEAD_CREATION_TIME() {
        return messageFactory.getMessage("WSS0228.invalid.ahead.CreationTime");
    }

    /**
     * WSS0227: The creation time is ahead of current time
     * 
     */
    public static String WSS_0228_INVALID_AHEAD_CREATION_TIME() {
        return localizer.localize(localizableWSS_0228_INVALID_AHEAD_CREATION_TIME());
    }

    public static Localizable localizableWSS_0236_NULL_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS0236.null.SAMLAssertion");
    }

    /**
     * WSS0236: SAML Assertion is not set into Policy by CallbackHandler.
     * 
     */
    public static String WSS_0236_NULL_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_0236_NULL_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_0500_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0500.diag.check.1");
    }

    /**
     * Check that the class implements MessageFilter
     * 
     */
    public static String WSS_0500_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0500_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0215_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0215.diag.cause.1");
    }

    /**
     * handle() call for a PropertyCallback on the handler threw exception
     * 
     */
    public static String WSS_0215_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0215_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0306_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0306.diag.cause.1");
    }

    /**
     * Invalid password type
     * 
     */
    public static String WSS_0306_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0306_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0351_ERROR_SETTING_ENCRYPTION_METHOD(Object arg0) {
        return messageFactory.getMessage("WSS0351.error.setting.encryption.method", arg0);
    }

    /**
     * WSS0351: Error setting Encryption method on EncryptedType due to {0}
     * 
     */
    public static String WSS_0351_ERROR_SETTING_ENCRYPTION_METHOD(Object arg0) {
        return localizer.localize(localizableWSS_0351_ERROR_SETTING_ENCRYPTION_METHOD(arg0));
    }

    public static Localizable localizableWSS_0416_SAML_SIGNATURE_INVALID() {
        return messageFactory.getMessage("WSS0416.saml.signature.invalid");
    }

    /**
     * WSS0416: The signature in the SAML Assertion is invalid
     * 
     */
    public static String WSS_0416_SAML_SIGNATURE_INVALID() {
        return localizer.localize(localizableWSS_0416_SAML_SIGNATURE_INVALID());
    }

    public static Localizable localizableWSS_0801_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0801.diag.cause.1");
    }

    /**
     * SecurityPolicy Type is illegal
     * 
     */
    public static String WSS_0801_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0801_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0707_NULL_TRUSTSTORE() {
        return messageFactory.getMessage("WSS0707.null.truststore");
    }

    /**
     * WSS0707: Error: No entry in TrustStore populated by TrustStoreCallback.
     * 
     */
    public static String WSS_0707_NULL_TRUSTSTORE() {
        return localizer.localize(localizableWSS_0707_NULL_TRUSTSTORE());
    }

    public static Localizable localizableWSS_0331_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0331.diag.check.1");
    }

    /**
     * Check that the UsernameToken matches the token schema
     * 
     */
    public static String WSS_0331_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0331_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_5629_ENCRYPTEDDATA_KEYINFO() {
        return messageFactory.getMessage("BSP5629.encrypteddata.keyinfo");
    }

    /**
     * BSP 5629 : EncryptedData MUST contain KeyInfo child element as it is not being referenced from EncryptedKey element.
     * 
     */
    public static String BSP_5629_ENCRYPTEDDATA_KEYINFO() {
        return localizer.localize(localizableBSP_5629_ENCRYPTEDDATA_KEYINFO());
    }

    public static Localizable localizableWSS_0191_SYMMETRICKEY_NOT_SET() {
        return messageFactory.getMessage("WSS0191.symmetrickey.not.set");
    }

    /**
     * WSS0191: SymmetricKey for encryption not set 
     * 
     */
    public static String WSS_0191_SYMMETRICKEY_NOT_SET() {
        return localizer.localize(localizableWSS_0191_SYMMETRICKEY_NOT_SET());
    }

    public static Localizable localizableWSS_0317_CANNOT_FIND_X_509_CERT_BECAUSE(Object arg0) {
        return messageFactory.getMessage("WSS0317.cannot.find.x509cert.because", arg0);
    }

    /**
     * WSS0317: Cannot find X509Certificate because of {0}
     * 
     */
    public static String WSS_0317_CANNOT_FIND_X_509_CERT_BECAUSE(Object arg0) {
        return localizer.localize(localizableWSS_0317_CANNOT_FIND_X_509_CERT_BECAUSE(arg0));
    }

    public static Localizable localizableWSS_0137_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0137.diag.cause.1");
    }

    /**
     * An appropriate JCE provider is not configured in the JRE
     * 
     */
    public static String WSS_0137_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0137_DIAG_CAUSE_1());
    }

    public static Localizable localizableBSP_3059_STR_VALUE_TYPE() {
        return messageFactory.getMessage("BSP3059.str.value.type");
    }

    /**
     * BSP 3059 : Reference element under wsse:SecurityTokenReference MUST have a ValueType attribute.
     * 
     */
    public static String BSP_3059_STR_VALUE_TYPE() {
        return localizer.localize(localizableBSP_3059_STR_VALUE_TYPE());
    }

    public static Localizable localizableWSS_0513_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0513.diag.check.1");
    }

    /**
     * Check that the configuration file is consistent with the security configuration schema
     * 
     */
    public static String WSS_0513_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0513_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0387_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0387.diag.check.1");
    }

    /**
     * Check that a Username has been passed through the configuration file or through the callback handler
     * 
     */
    public static String WSS_0387_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0387_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0710_NO_MATCHING_CERT_KEYSTORE(Object arg0) {
        return messageFactory.getMessage("WSS0710.no.matching.cert.keystore", arg0);
    }

    /**
     * WSS0710: Error: No Matching Certificate for : {0} found in KeyStore.
     * 
     */
    public static String WSS_0710_NO_MATCHING_CERT_KEYSTORE(Object arg0) {
        return localizer.localize(localizableWSS_0710_NO_MATCHING_CERT_KEYSTORE(arg0));
    }

    public static Localizable localizableWSS_0250_FAILED_PROCESS_STR() {
        return messageFactory.getMessage("WSS0250.failed.process.STR");
    }

    /**
     * WSS0250: Failed to process Security token reference
     * 
     */
    public static String WSS_0250_FAILED_PROCESS_STR() {
        return localizer.localize(localizableWSS_0250_FAILED_PROCESS_STR());
    }

    public static Localizable localizableWSS_0319_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0319.diag.cause.1");
    }

    /**
     * Could not add keyname to KeyInfo Header block
     * 
     */
    public static String WSS_0319_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0319_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0505_USING_DEFAULT_TARGET_VALUES() {
        return messageFactory.getMessage("WSS0505.using.default.target.values");
    }

    /**
     * WSS0505: Target not specified. Using default values.
     * 
     */
    public static String WSS_0505_USING_DEFAULT_TARGET_VALUES() {
        return localizer.localize(localizableWSS_0505_USING_DEFAULT_TARGET_VALUES());
    }

    public static Localizable localizableWSS_0196_SECURITYENVIRONMENT_NOT_SET() {
        return messageFactory.getMessage("WSS0196.securityenvironment.not.set");
    }

    /**
     * WSS0196: SecurityEnvironment not set on SecurableSoapMessage
     * 
     */
    public static String WSS_0196_SECURITYENVIRONMENT_NOT_SET() {
        return localizer.localize(localizableWSS_0196_SECURITYENVIRONMENT_NOT_SET());
    }

    public static Localizable localizableBSP_3221_CREATED_BEFORE_EXPIRES_TIMESTAMP() {
        return messageFactory.getMessage("BSP3221.CreatedBeforeExpires.Timestamp");
    }

    /**
     * BSP3221: wsu:Expires must appear after wsu:Created in the Timestamp
     * 
     */
    public static String BSP_3221_CREATED_BEFORE_EXPIRES_TIMESTAMP() {
        return localizer.localize(localizableBSP_3221_CREATED_BEFORE_EXPIRES_TIMESTAMP());
    }

    public static Localizable localizableWSS_0344_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0344.diag.check.1");
    }

    /**
     * Check to see that the encoding format of the Binary Security Token is Base64Binary
     * 
     */
    public static String WSS_0344_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0344_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0308_COULDNOT_DECODE_HEX_NONCE(Object arg0) {
        return messageFactory.getMessage("WSS0308.couldnot.decode.hex.nonce", arg0);
    }

    /**
     * WSS0308: Exception [ {0} ] while decoding hex nonce
     * 
     */
    public static String WSS_0308_COULDNOT_DECODE_HEX_NONCE(Object arg0) {
        return localizer.localize(localizableWSS_0308_COULDNOT_DECODE_HEX_NONCE(arg0));
    }

    public static Localizable localizableWSS_0212_POLICY_VIOLATION_EXCEPTION() {
        return messageFactory.getMessage("WSS0212.policy.violation.exception");
    }

    /**
     * WSS0212: Receiver requirement for digested password not met
     * 
     */
    public static String WSS_0212_POLICY_VIOLATION_EXCEPTION() {
        return localizer.localize(localizableWSS_0212_POLICY_VIOLATION_EXCEPTION());
    }

    public static Localizable localizableWSS_0371_DIAG_CAUSE_3() {
        return messageFactory.getMessage("WSS0371.diag.cause.3");
    }

    /**
     * Error in adding fault to SOAPBody
     * 
     */
    public static String WSS_0371_DIAG_CAUSE_3() {
        return localizer.localize(localizableWSS_0371_DIAG_CAUSE_3());
    }

    public static Localizable localizableWSS_0371_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0371.diag.cause.2");
    }

    /**
     * Error in creating javax.xml.soap.Name for setting the fault on SOAPBody
     * 
     */
    public static String WSS_0371_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0371_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0371_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0371.diag.cause.1");
    }

    /**
     * Error in getting the SOAPBody from the SOAPMessage
     * 
     */
    public static String WSS_0371_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0371_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0373_ERROR_APACHE_XPATH_API(Object arg0) {
        return messageFactory.getMessage("WSS0373.error.apache.xpathAPI", arg0);
    }

    /**
     * WSS0373: Can not find elements with wsu:Id attribute due to {0}
     * 
     */
    public static String WSS_0373_ERROR_APACHE_XPATH_API(Object arg0) {
        return localizer.localize(localizableWSS_0373_ERROR_APACHE_XPATH_API(arg0));
    }

    public static Localizable localizableWSS_0303_UNABLETO_GET_ENCODED_X_509_CERT() {
        return messageFactory.getMessage("WSS0303.unableto.get.encoded.x509cert");
    }

    /**
     * WSS0303: Unable to get encoded representation of X509Certificate
     * 
     */
    public static String WSS_0303_UNABLETO_GET_ENCODED_X_509_CERT() {
        return localizer.localize(localizableWSS_0303_UNABLETO_GET_ENCODED_X_509_CERT());
    }

    public static Localizable localizableWSS_0290_FAILED_GET_MESSAGE_PARTS_URI() {
        return messageFactory.getMessage("WSS0290.failed.getMessageParts.URI");
    }

    /**
     * WSS0290: failed to get Message Parts of using URI targettype
     * 
     */
    public static String WSS_0290_FAILED_GET_MESSAGE_PARTS_URI() {
        return localizer.localize(localizableWSS_0290_FAILED_GET_MESSAGE_PARTS_URI());
    }

    public static Localizable localizableWSS_0288_FAILED_GET_MESSAGE_PARTS_QNAME() {
        return messageFactory.getMessage("WSS0288.failed.getMessageParts.Qname");
    }

    /**
     * WSS0288: failed to get Message Parts of using QName targettype
     * 
     */
    public static String WSS_0288_FAILED_GET_MESSAGE_PARTS_QNAME() {
        return localizer.localize(localizableWSS_0288_FAILED_GET_MESSAGE_PARTS_QNAME());
    }

    public static Localizable localizableWSS_0210_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0210.diag.check.1");
    }

    /**
     * Check that the Key Encryption Algorithm used in the inbound msg is RSAv1.5
     * 
     */
    public static String WSS_0210_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0210_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0352_ERROR_GETTING_CIPHER_VALUES(Object arg0) {
        return messageFactory.getMessage("WSS0352.error.getting.cipherValues", arg0);
    }

    /**
     * WSS0352: Error getting CipherValues on CipherData due to {0}
     * 
     */
    public static String WSS_0352_ERROR_GETTING_CIPHER_VALUES(Object arg0) {
        return localizer.localize(localizableWSS_0352_ERROR_GETTING_CIPHER_VALUES(arg0));
    }

    public static Localizable localizableWSS_0301_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0301.diag.check.1");
    }

    /**
     * Check data is base64 encoded
     * 
     */
    public static String WSS_0301_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0301_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0193_INVALID_TARGET() {
        return messageFactory.getMessage("WSS0193.invalid.target");
    }

    /**
     * WSS0193: Target does not correspond to a valid message part
     * 
     */
    public static String WSS_0193_INVALID_TARGET() {
        return localizer.localize(localizableWSS_0193_INVALID_TARGET());
    }

    public static Localizable localizableWSS_0357_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0357.diag.check.2");
    }

    /**
     * Check that the index (begining with 0) used to refer the ds:KeyValue element is valid 
     * 
     */
    public static String WSS_0357_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0357_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0357_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0357.diag.check.1");
    }

    /**
     * Check that the ds:KeyInfo element has ds:KeyValue elements
     * 
     */
    public static String WSS_0357_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0357_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0384_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0384.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.Name for Timestamp
     * 
     */
    public static String WSS_0384_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0384_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0259_INVALID_SEC_USERNAME() {
        return messageFactory.getMessage("WSS0259.invalid.SEC.username");
    }

    /**
     * WSS0259: More than one wsse:UsernameToken element present in security header
     * 
     */
    public static String WSS_0259_INVALID_SEC_USERNAME() {
        return localizer.localize(localizableWSS_0259_INVALID_SEC_USERNAME());
    }

    public static Localizable localizableWSS_0705_UNSUPPORTED_OPERATION() {
        return messageFactory.getMessage("WSS0705.unsupported.operation");
    }

    /**
     * WSS0705: Unsupported Operation - setCertificate(X509Certificate) - on KeyNameStrategy
     * 
     */
    public static String WSS_0705_UNSUPPORTED_OPERATION() {
        return localizer.localize(localizableWSS_0705_UNSUPPORTED_OPERATION());
    }

    public static Localizable localizableWSS_0511_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0511.diag.cause.1");
    }

    /**
     * Non-permissable boolean value string - valid strings are "true" and "false"
     * 
     */
    public static String WSS_0511_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0511_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0402_SAML_MISSING_ELEMENT_VALUE() {
        return messageFactory.getMessage("WSS0402.saml.missing.element.value");
    }

    /**
     * WSS0402: Value of this Element is missing
     * 
     */
    public static String WSS_0402_SAML_MISSING_ELEMENT_VALUE() {
        return localizer.localize(localizableWSS_0402_SAML_MISSING_ELEMENT_VALUE());
    }

    public static Localizable localizableWSS_0327_EXCEPTION_CONVERTING_SIGNATURE_TOSOAPELEMENT() {
        return messageFactory.getMessage("WSS0327.exception.converting.signature.tosoapelement");
    }

    /**
     * WSS0327: Exception while converting signature block to SOAPElement
     * 
     */
    public static String WSS_0327_EXCEPTION_CONVERTING_SIGNATURE_TOSOAPELEMENT() {
        return localizer.localize(localizableWSS_0327_EXCEPTION_CONVERTING_SIGNATURE_TOSOAPELEMENT());
    }

    public static Localizable localizableWSS_0761_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0761.diag.check.1");
    }

    /**
     * Check the context argument passed to getRequesterSubject
     * 
     */
    public static String WSS_0761_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0761_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0602_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0602.diag.cause.1");
    }

    /**
     * A Key can not be located in SecurityEnvironment for the X509Data
     * 
     */
    public static String WSS_0602_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0602_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0410_SAML_ELEMENT_UNDERFLOW() {
        return messageFactory.getMessage("WSS0410.saml.element.underflow");
    }

    /**
     * WSS0410: Attempted to add less element(s) than required
     * 
     */
    public static String WSS_0410_SAML_ELEMENT_UNDERFLOW() {
        return localizer.localize(localizableWSS_0410_SAML_ELEMENT_UNDERFLOW());
    }

    public static Localizable localizableBSP_5626_KEYENCRYPTIONALGO() {
        return messageFactory.getMessage("BSP5626.keyencryptionalgo");
    }

    /**
     * BSP 5626 : Key encryption algorithm MUST be "http://www.w3.org/2001/04/xmlenc#rsa-1_5" or "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p" or "http://www.w3.org/2001/04/xmlenc#kw-tripledes" or "http://www.w3.org/2001/04/xmlenc#kw-aes128" or "http://www.w3.org/2001/04/xmlenc#kw-aes256".
     * 
     */
    public static String BSP_5626_KEYENCRYPTIONALGO() {
        return localizer.localize(localizableBSP_5626_KEYENCRYPTIONALGO());
    }

    public static Localizable localizableWSS_0341_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0341.diag.cause.1");
    }

    /**
     * Creation time is very old
     * 
     */
    public static String WSS_0341_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0341_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0147_UNABLETO_USE_STYLESHEET(Object arg0) {
        return messageFactory.getMessage("WSS0147.unableto.use.stylesheet", arg0);
    }

    /**
     * WSS0147: Exception [ {0} ] while trying to use stylesheet
     * 
     */
    public static String WSS_0147_UNABLETO_USE_STYLESHEET(Object arg0) {
        return localizer.localize(localizableWSS_0147_UNABLETO_USE_STYLESHEET(arg0));
    }

    public static Localizable localizableWSS_0353_MISSING_CIPHER_VALUE() {
        return messageFactory.getMessage("WSS0353.missing.cipherValue");
    }

    /**
     * WSS0353: CipherValue is not present in CipherData
     * 
     */
    public static String WSS_0353_MISSING_CIPHER_VALUE() {
        return localizer.localize(localizableWSS_0353_MISSING_CIPHER_VALUE());
    }

    public static Localizable localizableWSS_0133_EXCEPTION_WHILE_VERIFYING_SIGNATURE(Object arg0) {
        return messageFactory.getMessage("WSS0133.exception.while.verifying.signature", arg0);
    }

    /**
     * WSS0133: Exception [ {0} ] while verifying signature
     * 
     */
    public static String WSS_0133_EXCEPTION_WHILE_VERIFYING_SIGNATURE(Object arg0) {
        return localizer.localize(localizableWSS_0133_EXCEPTION_WHILE_VERIFYING_SIGNATURE(arg0));
    }

    public static Localizable localizableWSS_0370_ERROR_DELETING_SECHEADER() {
        return messageFactory.getMessage("WSS0370.error.deleting.secheader");
    }

    /**
     * WSS0370: Error deleting SecurityHeader due to exception.
     * 
     */
    public static String WSS_0370_ERROR_DELETING_SECHEADER() {
        return localizer.localize(localizableWSS_0370_ERROR_DELETING_SECHEADER());
    }

    public static Localizable localizableWSS_0752_INVALID_EMBEDDED_REFERENCE(Object arg0) {
        return messageFactory.getMessage("WSS0752.invalid.embedded.reference", arg0);
    }

    /**
     * WSS0751: Invalid SOAPElement ({0}) passed to EmbeddedReference()
     * 
     */
    public static String WSS_0752_INVALID_EMBEDDED_REFERENCE(Object arg0) {
        return localizer.localize(localizableWSS_0752_INVALID_EMBEDDED_REFERENCE(arg0));
    }

    public static Localizable localizableWSS_0144_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0144.diag.check.1");
    }

    /**
     * Check that the data is valid base64 encoded
     * 
     */
    public static String WSS_0144_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0144_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0190_ENCRYPTION_REQUIREMENTS_NOT_MET() {
        return messageFactory.getMessage("WSS0190.encryption.requirements.not.met");
    }

    /**
     * WSS0190: The number of elements encrypted is less than required/allowed
     * 
     */
    public static String WSS_0190_ENCRYPTION_REQUIREMENTS_NOT_MET() {
        return localizer.localize(localizableWSS_0190_ENCRYPTION_REQUIREMENTS_NOT_MET());
    }

    public static Localizable localizableWSS_0379_ERROR_CREATING_STR(Object arg0) {
        return messageFactory.getMessage("WSS0379.error.creating.str", arg0);
    }

    /**
     * WSS0379: Expected wsse:SecurityTokenReference SOAPElement, found {0} 
     * 
     */
    public static String WSS_0379_ERROR_CREATING_STR(Object arg0) {
        return localizer.localize(localizableWSS_0379_ERROR_CREATING_STR(arg0));
    }

    public static Localizable localizableWSS_0378_ERROR_CREATING_STR(Object arg0) {
        return messageFactory.getMessage("WSS0378.error.creating.str", arg0);
    }

    /**
     * WSS0378: Can not create SecurityTokenReference due to {0}
     * 
     */
    public static String WSS_0378_ERROR_CREATING_STR(Object arg0) {
        return localizer.localize(localizableWSS_0378_ERROR_CREATING_STR(arg0));
    }

    public static Localizable localizableWSS_0241_UNABLETO_SET_EKSHA_1_ON_CONTEXT() {
        return messageFactory.getMessage("WSS0241.unableto.set.EKSHA1.OnContext");
    }

    /**
     * WSS0241: Failed to set EKSHA1 value on Context properties
     * 
     */
    public static String WSS_0241_UNABLETO_SET_EKSHA_1_ON_CONTEXT() {
        return localizer.localize(localizableWSS_0241_UNABLETO_SET_EKSHA_1_ON_CONTEXT());
    }

    public static Localizable localizableWSS_0650_USERPWD_FILE_NOTFOUND() {
        return messageFactory.getMessage("WSS0650.userpwd.file.notfound");
    }

    /**
     * WSS0650: Username/Password data file not found
     * 
     */
    public static String WSS_0650_USERPWD_FILE_NOTFOUND() {
        return localizer.localize(localizableWSS_0650_USERPWD_FILE_NOTFOUND());
    }

    public static Localizable localizableWSS_0331_INVALID_USERNAMETOKEN() {
        return messageFactory.getMessage("WSS0331.invalid.usernametoken");
    }

    /**
     * WSS0331: Element passed was not a SOAPElement or is not a proper UsernameToken
     * 
     */
    public static String WSS_0331_INVALID_USERNAMETOKEN() {
        return localizer.localize(localizableWSS_0331_INVALID_USERNAMETOKEN());
    }

    public static Localizable localizableBSP_3226_EXPIRES_VALUE_TYPE_TIMESTAMP() {
        return messageFactory.getMessage("BSP3226.expiresValueType.Timestamp");
    }

    /**
     * BSP3226: A wsu:Expires element within a TIMESTAMP MUST NOT include a ValueType attribute.
     * 
     */
    public static String BSP_3226_EXPIRES_VALUE_TYPE_TIMESTAMP() {
        return localizer.localize(localizableBSP_3226_EXPIRES_VALUE_TYPE_TIMESTAMP());
    }

    public static Localizable localizableWSS_0313_CERT_IS_NULL() {
        return messageFactory.getMessage("WSS0313.cert.is.null");
    }

    /**
     * WSS0313: Certificate is null
     * 
     */
    public static String WSS_0313_CERT_IS_NULL() {
        return localizer.localize(localizableWSS_0313_CERT_IS_NULL());
    }

    public static Localizable localizableWSS_0327_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0327.diag.check.1");
    }

    /**
     * Check the element to be converted to SOAPElement
     * 
     */
    public static String WSS_0327_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0327_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0354_DIAG_CAUSE_4() {
        return messageFactory.getMessage("WSS0354.diag.cause.4");
    }

    /**
     * An error may have occured creating javax.xml.soap.Name for EncryptionProperties
     * 
     */
    public static String WSS_0354_DIAG_CAUSE_4() {
        return localizer.localize(localizableWSS_0354_DIAG_CAUSE_4());
    }

    public static Localizable localizableWSS_0354_DIAG_CAUSE_3() {
        return messageFactory.getMessage("WSS0354.diag.cause.3");
    }

    /**
     * An error may have occured creating javax.xml.soap.Name for CipherData 
     * 
     */
    public static String WSS_0354_DIAG_CAUSE_3() {
        return localizer.localize(localizableWSS_0354_DIAG_CAUSE_3());
    }

    public static Localizable localizableWSS_0354_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0354.diag.cause.2");
    }

    /**
     * An error may have occured creating javax.xml.soap.Name for KeyInfo 
     * 
     */
    public static String WSS_0354_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0354_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0354_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0354.diag.cause.1");
    }

    /**
     * An error may have occured creating javax.xml.soap.Name for EncryptionMethod
     * 
     */
    public static String WSS_0354_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0354_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0509_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0509.diag.check.1");
    }

    /**
     * Check that no default settings are programmatically added
     * 
     */
    public static String WSS_0509_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0509_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0369_SOAP_EXCEPTION(Object arg0) {
        return messageFactory.getMessage("WSS0369.soap.exception", arg0);
    }

    /**
     * WSS0369: Error getting SOAPHeader from SOAPEnvelope due to {0}
     * 
     */
    public static String WSS_0369_SOAP_EXCEPTION(Object arg0) {
        return localizer.localize(localizableWSS_0369_SOAP_EXCEPTION(arg0));
    }

    public static Localizable localizableWSS_0330_USERNAMETOKEN_FIRSTCHILD_MUSTBE_USERNAME() {
        return messageFactory.getMessage("WSS0330.usernametoken.firstchild.mustbe.username");
    }

    /**
     * WSS0330: The first child of a UsernameToken Element, should be a Username
     * 
     */
    public static String WSS_0330_USERNAMETOKEN_FIRSTCHILD_MUSTBE_USERNAME() {
        return localizer.localize(localizableWSS_0330_USERNAMETOKEN_FIRSTCHILD_MUSTBE_USERNAME());
    }

    public static Localizable localizableWSS_0651_PARSER_CONFIG_ERROR() {
        return messageFactory.getMessage("WSS0651.parser.config.error");
    }

    /**
     * WSS0651: Failed to create JAXP DocumentBuilder instance
     * 
     */
    public static String WSS_0651_PARSER_CONFIG_ERROR() {
        return localizer.localize(localizableWSS_0651_PARSER_CONFIG_ERROR());
    }

    public static Localizable localizableWSS_0220_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0220.diag.cause.1");
    }

    /**
     * handle() call on the handler failed to set the Callback
     * 
     */
    public static String WSS_0220_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0220_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0185_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0185.diag.cause.1");
    }

    /**
     * Could not find the certificate associated with the direct reference strategy
     * 
     */
    public static String WSS_0185_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0185_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0311_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0311.diag.cause.1");
    }

    /**
     * Password digest could not be created 
     * 
     */
    public static String WSS_0311_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0311_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0199_NULL_CREATION_TIME() {
        return messageFactory.getMessage("WSS0199.null.creation.time");
    }

    /**
     * WSS0199: Timestamp creation time can not be null
     * 
     */
    public static String WSS_0199_NULL_CREATION_TIME() {
        return localizer.localize(localizableWSS_0199_NULL_CREATION_TIME());
    }

    public static Localizable localizableWSS_0367_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0367.diag.cause.1");
    }

    /**
     * A valid xenc:EncryptedData element has not been referenced from the xenc:ReferenceList
     * 
     */
    public static String WSS_0367_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0367_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0805_POLICY_NULL() {
        return messageFactory.getMessage("WSS0805.policy.null");
    }

    /**
     * Policy is null
     * 
     */
    public static String WSS_0805_POLICY_NULL() {
        return localizer.localize(localizableWSS_0805_POLICY_NULL());
    }

    public static Localizable localizableWSS_0198_UNSUPPORTED_SIGNATURE_ALGORITHM(Object arg0) {
        return messageFactory.getMessage("WSS0198.unsupported.signature.algorithm", arg0);
    }

    /**
     * WSS0198: Unsupported Signature Alogrithm: {0}
     * 
     */
    public static String WSS_0198_UNSUPPORTED_SIGNATURE_ALGORITHM(Object arg0) {
        return localizer.localize(localizableWSS_0198_UNSUPPORTED_SIGNATURE_ALGORITHM(arg0));
    }

    public static Localizable localizableWSS_0206_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0206.diag.check.1");
    }

    /**
     * Check that the message meets the security requirements
     * 
     */
    public static String WSS_0206_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0206_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0324_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0324.diag.cause.1");
    }

    /**
     * Could not validate signature based on the public key of the certificate passed
     * 
     */
    public static String WSS_0324_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0324_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0198_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0198.diag.cause.1");
    }

    /**
     * Only RSA_SHA1 Signature algorithm is supported
     * 
     */
    public static String WSS_0198_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0198_DIAG_CAUSE_1());
    }

    public static Localizable localizableBSP_5601_ENCRYPTEDDATA_ENCRYPTIONMETHOD(Object arg0) {
        return messageFactory.getMessage("BSP5601.encrypteddata.encryptionmethod", arg0);
    }

    /**
     * BSP 5601 : EncryptedData element ID {0} MUST contain EncryptionMethod child element.
     * 
     */
    public static String BSP_5601_ENCRYPTEDDATA_ENCRYPTIONMETHOD(Object arg0) {
        return localizer.localize(localizableBSP_5601_ENCRYPTEDDATA_ENCRYPTIONMETHOD(arg0));
    }

    public static Localizable localizableWSS_0383_DOCUMENT_NOT_SET() {
        return messageFactory.getMessage("WSS0383.document.not.set");
    }

    /**
     * WSS0383: Owner document of ds:Signature SOAPElement is not set
     * 
     */
    public static String WSS_0383_DOCUMENT_NOT_SET() {
        return localizer.localize(localizableWSS_0383_DOCUMENT_NOT_SET());
    }

    public static Localizable localizableWSS_0701_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0701.diag.check.1");
    }

    /**
     * Check that a default certificate is available and/or a valid alias is used
     * 
     */
    public static String WSS_0701_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0701_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0407_SAML_MISSING_ATTRIBUTE() {
        return messageFactory.getMessage("WSS0407.saml.missing.attribute");
    }

    /**
     * WSS0407: Missing attribute
     * 
     */
    public static String WSS_0407_SAML_MISSING_ATTRIBUTE() {
        return localizer.localize(localizableWSS_0407_SAML_MISSING_ATTRIBUTE());
    }

    public static Localizable localizableWSS_0757_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0757.diag.check.1");
    }

    /**
     * Check your SAAJ API Documentation
     * 
     */
    public static String WSS_0757_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0757_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_3006_ENCRYPTEDKEY_REFURI() {
        return messageFactory.getMessage("BSP3006.encryptedkey.refuri");
    }

    /**
     * BSP 3006 : DataReference element under EncryptedKey MUST contain a URI attribute containing a shorthand XPointer.
     * 
     */
    public static String BSP_3006_ENCRYPTEDKEY_REFURI() {
        return localizer.localize(localizableBSP_3006_ENCRYPTEDKEY_REFURI());
    }

    public static Localizable localizableBSP_5204_STR_INTERNAL_STR_REFERENCE() {
        return messageFactory.getMessage("BSP5204.str.internal.str.reference");
    }

    /**
     * BSP 5204 : having an ID attribute MUST contain a URI attribute with a Shorthand XPointer value.
     * 
     */
    public static String BSP_5204_STR_INTERNAL_STR_REFERENCE() {
        return localizer.localize(localizableBSP_5204_STR_INTERNAL_STR_REFERENCE());
    }

    public static Localizable localizableWSS_0763_EXCEPTION_ISSUERNAME(Object arg0) {
        return messageFactory.getMessage("WSS0763.exception.issuername", arg0);
    }

    /**
     * WSS0763: Exception {0} while getting Issuer Name
     * 
     */
    public static String WSS_0763_EXCEPTION_ISSUERNAME(Object arg0) {
        return localizer.localize(localizableWSS_0763_EXCEPTION_ISSUERNAME(arg0));
    }

    public static Localizable localizableWSS_0219_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0219.diag.check.1");
    }

    /**
     * Check the handler implementation for SignatureKeyCallback.DefaultPrivKeyCertRequest
     * 
     */
    public static String WSS_0219_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0219_DIAG_CHECK_1());
    }

    public static Localizable localizableSS_0655_DIAG_CAUSE_1() {
        return messageFactory.getMessage("SS0655.diag.cause.1");
    }

    /**
     * The Class object does not correspond to a header block identified by the SOAPElement
     * 
     */
    public static String SS_0655_DIAG_CAUSE_1() {
        return localizer.localize(localizableSS_0655_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0337_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0337.diag.cause.1");
    }

    /**
     * Could not resolve URI
     * 
     */
    public static String WSS_0337_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0337_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0261_INVALID_MESSAGE_POLICYSET() {
        return messageFactory.getMessage("WSS0261.invalid.Message.policyset");
    }

    /**
     * WSS0261: Message does not conform to configured policy: policy set not present in receiver requirements
     * 
     */
    public static String WSS_0261_INVALID_MESSAGE_POLICYSET() {
        return localizer.localize(localizableWSS_0261_INVALID_MESSAGE_POLICYSET());
    }

    public static Localizable localizableWSS_0338_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0338.diag.cause.1");
    }

    /**
     * Key Reference Mechanism not supported
     * 
     */
    public static String WSS_0338_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0338_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0237_FAILED_DYNAMIC_POLICY_CALLBACK() {
        return messageFactory.getMessage("WSS0237.failed.DynamicPolicyCallback");
    }

    /**
     * WSS0237: An Error occurred while populating SAML Policy in Dynamic Policy Callback
     * 
     */
    public static String WSS_0237_FAILED_DYNAMIC_POLICY_CALLBACK() {
        return localizer.localize(localizableWSS_0237_FAILED_DYNAMIC_POLICY_CALLBACK());
    }

    public static Localizable localizableWSS_0650_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0650.diag.cause.1");
    }

    /**
     * Username/Password data file not found
     * 
     */
    public static String WSS_0650_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0650_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0135_UNABLETO_ENCRYPT_SYMMETRIC_KEY(Object arg0) {
        return messageFactory.getMessage("WSS0135.unableto.encrypt.symmetric.key", arg0);
    }

    /**
     * WSS0135: Exception [ {0} ] while trying to encrypt symmetric key
     * 
     */
    public static String WSS_0135_UNABLETO_ENCRYPT_SYMMETRIC_KEY(Object arg0) {
        return localizer.localize(localizableWSS_0135_UNABLETO_ENCRYPT_SYMMETRIC_KEY(arg0));
    }

    public static Localizable localizableWSS_0362_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0362.diag.check.1");
    }

    /**
     * Check that the SOAPElement passed to ReferenceListHeaderBlock() is valid as per spec.
     * 
     */
    public static String WSS_0362_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0362_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0203_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0203.diag.cause.1");
    }

    /**
     * Header block corresponding to the desired requirement not found
     * 
     */
    public static String WSS_0203_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0203_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0244_INVALID_LEVEL_DKT() {
        return messageFactory.getMessage("WSS0244.invalid.level.DKT");
    }

    /**
     * WSS0244: A derived Key Token should be a top level key binding
     * 
     */
    public static String WSS_0244_INVALID_LEVEL_DKT() {
        return localizer.localize(localizableWSS_0244_INVALID_LEVEL_DKT());
    }

    public static Localizable localizableWSS_0168_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0168.diag.cause.1");
    }

    /**
     * Unable to generate a random symmetric key
     * 
     */
    public static String WSS_0168_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0168_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0421_SAML_CANNOT_SUBJECTCONFIRMATION_KEYINFO_NOT_UNIQUE() {
        return messageFactory.getMessage("WSS0421.saml.cannot.subjectconfirmation.keyinfo.not.unique");
    }

    /**
     * WSS0421: KeyInfo not unique inside SAML SubjectConfirmation 
     * 
     */
    public static String WSS_0421_SAML_CANNOT_SUBJECTCONFIRMATION_KEYINFO_NOT_UNIQUE() {
        return localizer.localize(localizableWSS_0421_SAML_CANNOT_SUBJECTCONFIRMATION_KEYINFO_NOT_UNIQUE());
    }

    public static Localizable localizableWSS_0204_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0204.diag.cause.1");
    }

    /**
     * Illegal security header block found in the security header
     * 
     */
    public static String WSS_0204_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0204_DIAG_CAUSE_1());
    }

    public static Localizable localizableBSP_3225_CREATED_VALUE_TYPE_TIMESTAMP() {
        return messageFactory.getMessage("BSP3225.createdValueType.Timestamp");
    }

    /**
     * BSP3225: A wsu:Created element within a TIMESTAMP MUST NOT include a ValueType attribute.
     * 
     */
    public static String BSP_3225_CREATED_VALUE_TYPE_TIMESTAMP() {
        return localizer.localize(localizableBSP_3225_CREATED_VALUE_TYPE_TIMESTAMP());
    }

    public static Localizable localizableWSS_0390_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0390.diag.cause.1");
    }

    /**
     * UTF-8 Charset is unsupported for byte-encoding (a string)
     * 
     */
    public static String WSS_0390_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0390_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0516_DUPLICATE_CONFIGURATION_ELEMENT(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0516.duplicate.configuration.element", arg0, arg1);
    }

    /**
     * WSS0516: Duplicate element: {0} in {1}
     * 
     */
    public static String WSS_0516_DUPLICATE_CONFIGURATION_ELEMENT(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0516_DUPLICATE_CONFIGURATION_ELEMENT(arg0, arg1));
    }

    public static Localizable localizableWSS_0192_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0192.diag.check.1");
    }

    /**
     * Check that a non-null target list is used to instantiate the filter
     * 
     */
    public static String WSS_0192_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0192_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0422_SAML_ISSUER_VALIDATION_FAILED() {
        return messageFactory.getMessage("WSS0422.saml.issuer.validation.failed");
    }

    /**
     * WSS0422: Issuer validation failed for SAML Assertion
     * 
     */
    public static String WSS_0422_SAML_ISSUER_VALIDATION_FAILED() {
        return localizer.localize(localizableWSS_0422_SAML_ISSUER_VALIDATION_FAILED());
    }

    public static Localizable localizableWSS_0193_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0193.diag.check.1");
    }

    /**
     * Check that a valid XPath/QName/wsuId are specified
     * 
     */
    public static String WSS_0193_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0193_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0754_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0754.diag.cause.2");
    }

    /**
     * A SOAPElement representation of EmbeddedReference containing the Token is used to create the EmbeddedReference instance
     * 
     */
    public static String WSS_0754_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0754_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0754_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0754.diag.cause.1");
    }

    /**
     * Token on EmbeddedReference has already been set
     * 
     */
    public static String WSS_0754_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0754_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0339_UNSUPPORTED_KEYINFO() {
        return messageFactory.getMessage("WSS0339.unsupported.keyinfo");
    }

    /**
     * WSS0339: Unsupported keyinfo block encountered
     * 
     */
    public static String WSS_0339_UNSUPPORTED_KEYINFO() {
        return localizer.localize(localizableWSS_0339_UNSUPPORTED_KEYINFO());
    }

    public static Localizable localizableWSS_0216_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0216.diag.cause.1");
    }

    /**
     * handle() call on the handler threw exception
     * 
     */
    public static String WSS_0216_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0216_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0310_NO_SUCH_ALGORITHM(Object arg0) {
        return messageFactory.getMessage("WSS0310.no.such.algorithm", arg0);
    }

    /**
     * WSS0310: {0}, No such algorithm found
     * 
     */
    public static String WSS_0310_NO_SUCH_ALGORITHM(Object arg0) {
        return localizer.localize(localizableWSS_0310_NO_SUCH_ALGORITHM(arg0));
    }

    public static Localizable localizableWSS_0307_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0307.diag.cause.1");
    }

    /**
     * Nonce encoding namespace check failed
     * 
     */
    public static String WSS_0307_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0307_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0502_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0502.diag.check.1");
    }

    /**
     * Check that the xml file follows schema for defining configuration
     * 
     */
    public static String WSS_0502_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0502_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0217_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0217.diag.cause.1");
    }

    /**
     * handle() call on the handler threw exception
     * 
     */
    public static String WSS_0217_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0217_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0323_EXCEPTION_WHILE_SIGNING() {
        return messageFactory.getMessage("WSS0323.exception.while.signing");
    }

    /**
     * WSS0323: Exception while signing
     * 
     */
    public static String WSS_0323_EXCEPTION_WHILE_SIGNING() {
        return localizer.localize(localizableWSS_0323_EXCEPTION_WHILE_SIGNING());
    }

    public static Localizable localizableWSS_0320_EXCEPTION_GETTING_KEYNAME() {
        return messageFactory.getMessage("WSS0320.exception.getting.keyname");
    }

    /**
     * WSS0320: Exception while getting keyname from KeyInfo Header block
     * 
     */
    public static String WSS_0320_EXCEPTION_GETTING_KEYNAME() {
        return localizer.localize(localizableWSS_0320_EXCEPTION_GETTING_KEYNAME());
    }

    public static Localizable localizableBSP_3071_STR_ENCODING_TYPE() {
        return messageFactory.getMessage("BSP3071.str.encodingType");
    }

    /**
     * BSP 3071 : EncodingType attribute for wsse:SecurityTokenReference element MUST have a value "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary".
     * 
     */
    public static String BSP_3071_STR_ENCODING_TYPE() {
        return localizer.localize(localizableBSP_3071_STR_ENCODING_TYPE());
    }

    public static Localizable localizableWSS_0332_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0332.diag.check.1");
    }

    /**
     * Check UsernameToken contains a valid Username
     * 
     */
    public static String WSS_0332_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0332_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0127_UNABLETO_SIGN_MESSAGE() {
        return messageFactory.getMessage("WSS0127.unableto.sign.message");
    }

    /**
     * WSS0127: Unable to sign message
     * 
     */
    public static String WSS_0127_UNABLETO_SIGN_MESSAGE() {
        return localizer.localize(localizableWSS_0127_UNABLETO_SIGN_MESSAGE());
    }

    public static Localizable localizableWSS_0506_TARGET_NOT_SPECIFIED_VERIFY() {
        return messageFactory.getMessage("WSS0506.target.not.specified.verify");
    }

    /**
     * WSS0506: Target not specified in verify.
     * 
     */
    public static String WSS_0506_TARGET_NOT_SPECIFIED_VERIFY() {
        return localizer.localize(localizableWSS_0506_TARGET_NOT_SPECIFIED_VERIFY());
    }

    public static Localizable localizableWSS_0504_CLASS_NOT_FOUND(Object arg0) {
        return messageFactory.getMessage("WSS0504.class.not.found", arg0);
    }

    /**
     * WSS0504: Class {0} was not found on the class path
     * 
     */
    public static String WSS_0504_CLASS_NOT_FOUND(Object arg0) {
        return localizer.localize(localizableWSS_0504_CLASS_NOT_FOUND(arg0));
    }

    public static Localizable localizableWSS_0803_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0803.diag.cause.1");
    }

    /**
     * SOAPMessage is null
     * 
     */
    public static String WSS_0803_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0803_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0514_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0514.diag.check.1");
    }

    /**
     * Check that the configuration file is consistent with the security configuration schema
     * 
     */
    public static String WSS_0514_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0514_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0388_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0388.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0388_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0388_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_4201_PASSWORD_TYPE_USERNAME() {
        return messageFactory.getMessage("BSP4201.PasswordType.Username");
    }

    /**
     * BSP4201: A wsse:UsernameToken/wsse:Password element in a SECURITY_HEADER MUST specify a Type attribute. 
     * 
     */
    public static String BSP_4201_PASSWORD_TYPE_USERNAME() {
        return localizer.localize(localizableBSP_4201_PASSWORD_TYPE_USERNAME());
    }

    public static Localizable localizableWSS_0248_NULL_STR() {
        return messageFactory.getMessage("WSS0248.null.STR");
    }

    /**
     * WSS0248: Invalid DerivedKey Token encountered, no STR found
     * 
     */
    public static String WSS_0248_NULL_STR() {
        return localizer.localize(localizableWSS_0248_NULL_STR());
    }

    public static Localizable localizableWSS_0360_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0360.diag.cause.1");
    }

    /**
     * An error may have occured creating javax.xml.soap.Name for ReferenceList
     * 
     */
    public static String WSS_0360_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0360_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0219_CANNOT_LOCATE_DEFAULT_PRIVKEY() {
        return messageFactory.getMessage("WSS0219.cannot.locate.default.privkey");
    }

    /**
     * WSS0219: Unable to locate a default private key using Callback Handler
     * 
     */
    public static String WSS_0219_CANNOT_LOCATE_DEFAULT_PRIVKEY() {
        return localizer.localize(localizableWSS_0219_CANNOT_LOCATE_DEFAULT_PRIVKEY());
    }

    public static Localizable localizableWSS_0515_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0515.diag.check.1");
    }

    /**
     * Check that the configuration file is consistent with the security configuration schema
     * 
     */
    public static String WSS_0515_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0515_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0520_ILLEGAL_CONFIGURATION_STATE() {
        return messageFactory.getMessage("WSS0520.illegal.configuration.state");
    }

    /**
     * WSS0520: Illegal configuration state of element
     * 
     */
    public static String WSS_0520_ILLEGAL_CONFIGURATION_STATE() {
        return localizer.localize(localizableWSS_0520_ILLEGAL_CONFIGURATION_STATE());
    }

    public static Localizable localizableWSS_0606_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0606.diag.check.1");
    }

    /**
     * Check that the Referenced Node (to be STR transformed) in ds:SignedInfo is valid
     * 
     */
    public static String WSS_0606_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0606_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0291_UNSUPPORTED_OPERATION_GET_ATTACHMENT() {
        return messageFactory.getMessage("WSS0291.unsupported.operation.getAttachment");
    }

    /**
     * WSS0291: Unsupported Operation get Attachment
     * 
     */
    public static String WSS_0291_UNSUPPORTED_OPERATION_GET_ATTACHMENT() {
        return localizer.localize(localizableWSS_0291_UNSUPPORTED_OPERATION_GET_ATTACHMENT());
    }

    public static Localizable localizableWSS_0213_POLICY_VIOLATION_EXCEPTION() {
        return messageFactory.getMessage("WSS0213.policy.violation.exception");
    }

    /**
     * WSS0213: Receiver requirement for use nonce not met
     * 
     */
    public static String WSS_0213_POLICY_VIOLATION_EXCEPTION() {
        return localizer.localize(localizableWSS_0213_POLICY_VIOLATION_EXCEPTION());
    }

    public static Localizable localizableWSS_0388_ERROR_CREATING_USERNAMETOKEN(Object arg0) {
        return messageFactory.getMessage("WSS0388.error.creating.usernametoken", arg0);
    }

    /**
     * WSS0388: Error creating SOAPElement representation of UsernameToken due to {0}
     * 
     */
    public static String WSS_0388_ERROR_CREATING_USERNAMETOKEN(Object arg0) {
        return localizer.localize(localizableWSS_0388_ERROR_CREATING_USERNAMETOKEN(arg0));
    }

    public static Localizable localizableWSS_0190_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0190.diag.cause.1");
    }

    /**
     * The number of elements encrypted is less than required/allowed
     * 
     */
    public static String WSS_0190_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0190_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0345_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0345.diag.check.1");
    }

    /**
     * If SOAPElement is used to create EncryptedData HeaderBlock, check to see that it is valid as per spec. 
     * 
     */
    public static String WSS_0345_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0345_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0756_INVALID_KEY_IDENTIFIER(Object arg0) {
        return messageFactory.getMessage("WSS0756.invalid.key.identifier", arg0);
    }

    /**
     * WSS0756: Invalid SOAPElement ({0}) passed to KeyIdentifier()
     * 
     */
    public static String WSS_0756_INVALID_KEY_IDENTIFIER(Object arg0) {
        return localizer.localize(localizableWSS_0756_INVALID_KEY_IDENTIFIER(arg0));
    }

    public static Localizable localizableWSS_0372_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0372.diag.cause.1");
    }

    /**
     * An Internal XPathAPI transformation error occurred 
     * 
     */
    public static String WSS_0372_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0372_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0225_FAILED_PASSWORD_VALIDATION_CALLBACK() {
        return messageFactory.getMessage("WSS0225.failed.PasswordValidationCallback");
    }

    /**
     * WSS0225: Exception occured in Password Validation Callback
     * 
     */
    public static String WSS_0225_FAILED_PASSWORD_VALIDATION_CALLBACK() {
        return localizer.localize(localizableWSS_0225_FAILED_PASSWORD_VALIDATION_CALLBACK());
    }

    public static Localizable localizableWSS_0344_ERROR_DECODING_BST() {
        return messageFactory.getMessage("WSS0344.error.decoding.bst");
    }

    /**
     * WSS0344: BinarySecurityToken is not Base64 Encoded
     * 
     */
    public static String WSS_0344_ERROR_DECODING_BST() {
        return localizer.localize(localizableWSS_0344_ERROR_DECODING_BST());
    }

    public static Localizable localizableWSS_0373_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0373.diag.cause.1");
    }

    /**
     * An Internal XPathAPI transformation error occurred 
     * 
     */
    public static String WSS_0373_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0373_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0302_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0302.diag.check.1");
    }

    /**
     * Data stream used to create the x509 certificate maybe corrupted
     * 
     */
    public static String WSS_0302_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0302_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0358_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0358.diag.check.2");
    }

    /**
     * Check that the index (begining with 0) used to refer the ds:X509Data element is valid
     * 
     */
    public static String WSS_0358_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0358_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0358_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0358.diag.check.1");
    }

    /**
     * Check that the ds:KeyInfo element has ds:X509Data elements
     * 
     */
    public static String WSS_0358_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0358_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0385_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0385.diag.cause.1");
    }

    /**
     * The SOAPElement used to instantiate Timestamp() is not valid (as per spec.)
     * 
     */
    public static String WSS_0385_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0385_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0400_SAML_NULL_INPUT() {
        return messageFactory.getMessage("WSS0400.saml.null.input");
    }

    /**
     * WSS0400: Null Input
     * 
     */
    public static String WSS_0400_SAML_NULL_INPUT() {
        return localizer.localize(localizableWSS_0400_SAML_NULL_INPUT());
    }

    public static Localizable localizableWSS_0330_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0330.diag.cause.1");
    }

    /**
     * Username not first child of UsernameToken
     * 
     */
    public static String WSS_0330_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0330_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0386_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0386.diag.cause.2");
    }

    /**
     * Error adding child SOAPElements to the Timestamp element
     * 
     */
    public static String WSS_0386_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0386_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0512_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0512.diag.cause.1");
    }

    /**
     * Non-permissable attribute on a Security Configuration element
     * 
     */
    public static String WSS_0512_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0512_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0300_NO_JCE_PROVIDER(Object arg0) {
        return messageFactory.getMessage("WSS0300.no.jce.provider", arg0);
    }

    /**
     * WSS0300: Unable to locate a JCE provider for {0}
     * 
     */
    public static String WSS_0300_NO_JCE_PROVIDER(Object arg0) {
        return localizer.localize(localizableWSS_0300_NO_JCE_PROVIDER(arg0));
    }

    public static Localizable localizableWSS_0386_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0386.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.SOAPElement for Timestamp
     * 
     */
    public static String WSS_0386_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0386_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0762_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0762.diag.check.1");
    }

    /**
     * Check the value of the encodingType property on KeyIdentifier
     * 
     */
    public static String WSS_0762_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0762_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0603_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0603.diag.cause.1");
    }

    /**
     * XPathAPI TransformerException in finding element with matching wsu:Id/Id/SAMLAssertionID
     * 
     */
    public static String WSS_0603_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0603_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0347_MISSING_CIPHER_DATA() {
        return messageFactory.getMessage("WSS0347.missing.cipher.data");
    }

    /**
     * WSS0347: CipherData in EncryptedType is not present
     * 
     */
    public static String WSS_0347_MISSING_CIPHER_DATA() {
        return localizer.localize(localizableWSS_0347_MISSING_CIPHER_DATA());
    }

    public static Localizable localizableWSS_0417_SAML_TIMESTAMP_INVALID() {
        return messageFactory.getMessage("WSS0417.saml.timestamp.invalid");
    }

    /**
     * WSS0417: Condition (notBefore, notOnOrAfter) validation failed for SAML assertion
     * 
     */
    public static String WSS_0417_SAML_TIMESTAMP_INVALID() {
        return localizer.localize(localizableWSS_0417_SAML_TIMESTAMP_INVALID());
    }

    public static Localizable localizableWSS_0189_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0189.diag.check.1");
    }

    /**
     * Check that the encryption algorithm used is either 3DES, AES128_CBC, AES256_CBC
     * 
     */
    public static String WSS_0189_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0189_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_5608_ENCRYPTEDDATA_REFURI() {
        return messageFactory.getMessage("BSP5608.encrypteddata.refuri");
    }

    /**
     * BSP 5608 : DataReference element under EncryptedData MUST contain a URI attribute containing a shorthand XPointer.
     * 
     */
    public static String BSP_5608_ENCRYPTEDDATA_REFURI() {
        return localizer.localize(localizableBSP_5608_ENCRYPTEDDATA_REFURI());
    }

    public static Localizable localizableWSS_0750_SOAP_EXCEPTION(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0750.soap.exception", arg0, arg1);
    }

    /**
     * WSS0750: Error creating javax.xml.soap.SOAPElement for {0} due to {1}
     * 
     */
    public static String WSS_0750_SOAP_EXCEPTION(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0750_SOAP_EXCEPTION(arg0, arg1));
    }

    public static Localizable localizableWSS_0375_ERROR_APACHE_XPATH_API(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0375.error.apache.xpathAPI", arg0, arg1);
    }

    /**
     * WSS0375: Can not find element with Id attribute value {0} due to {1}
     * 
     */
    public static String WSS_0375_ERROR_APACHE_XPATH_API(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0375_ERROR_APACHE_XPATH_API(arg0, arg1));
    }

    public static Localizable localizableWSS_0342_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0342.diag.cause.1");
    }

    /**
     * BinarySecurity Token's Value type is invalid
     * 
     */
    public static String WSS_0342_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0342_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0295_PASSWORD_VAL_NOT_CONFIG_USERNAME_VAL() {
        return messageFactory.getMessage("WSS0295.password.val.not.config.username.val");
    }

    /**
     * WSS0295: Error: No PasswordValidator Configured for UsernameToken Validation.
     * 
     */
    public static String WSS_0295_PASSWORD_VAL_NOT_CONFIG_USERNAME_VAL() {
        return localizer.localize(localizableWSS_0295_PASSWORD_VAL_NOT_CONFIG_USERNAME_VAL());
    }

    public static Localizable localizableWSS_0343_DIAG_CAUSE_1(Object arg0) {
        return messageFactory.getMessage("WSS0343.diag.cause.1", arg0);
    }

    /**
     * Error in creating the BST due to {0} 
     * 
     */
    public static String WSS_0343_DIAG_CAUSE_1(Object arg0) {
        return localizer.localize(localizableWSS_0343_DIAG_CAUSE_1(arg0));
    }

    public static Localizable localizableWSS_0377_ERROR_CREATING_STR(Object arg0) {
        return messageFactory.getMessage("WSS0377.error.creating.str", arg0);
    }

    /**
     * WSS0377: Can not create SecurityTokenReference due to {0}
     * 
     */
    public static String WSS_0377_ERROR_CREATING_STR(Object arg0) {
        return localizer.localize(localizableWSS_0377_ERROR_CREATING_STR(arg0));
    }

    public static Localizable localizableWSS_0166_NO_BINARY_SECURITY_TOKEN_IN_HEADER(Object arg0) {
        return messageFactory.getMessage("WSS0166.no.binary.security.token.in.header", arg0);
    }

    /**
     * WSS0166: Could not retrieve a Binary Security Token needed for Verifying the Signature from the wsse:Security header, got Exception [ {0} ]
     * 
     */
    public static String WSS_0166_NO_BINARY_SECURITY_TOKEN_IN_HEADER(Object arg0) {
        return localizer.localize(localizableWSS_0166_NO_BINARY_SECURITY_TOKEN_IN_HEADER(arg0));
    }

    public static Localizable localizableWSS_0203_POLICY_VIOLATION_EXCEPTION(Object arg0) {
        return messageFactory.getMessage("WSS0203.policy.violation.exception", arg0);
    }

    /**
     * WSS0203: Unexpected {0} element in the header
     * 
     */
    public static String WSS_0203_POLICY_VIOLATION_EXCEPTION(Object arg0) {
        return localizer.localize(localizableWSS_0203_POLICY_VIOLATION_EXCEPTION(arg0));
    }

    public static Localizable localizableWSS_0328_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0328.diag.check.1");
    }

    /**
     * Check date format is in UTC. Check it is "yyyy-MM-dd'T'HH:mm:ss'Z'" or "yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'" 
     * 
     */
    public static String WSS_0328_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0328_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0355_DIAG_CAUSE_3() {
        return messageFactory.getMessage("WSS0355.diag.cause.3");
    }

    /**
     * Error creating com.sun.org.apache.xml.internal.security.keys.content.KeyValue
     * 
     */
    public static String WSS_0355_DIAG_CAUSE_3() {
        return localizer.localize(localizableWSS_0355_DIAG_CAUSE_3());
    }

    public static Localizable localizableWSS_0355_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0355.diag.cause.2");
    }

    /**
     * Error creating com.sun.org.apache.xml.internal.security.keys.content.keyvalues.RSAKeyValue
     * 
     */
    public static String WSS_0355_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0355_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0192_ENCRYPTION_TARGETS_NOT_SPECIFIED() {
        return messageFactory.getMessage("WSS0192.encryption.targets.not.specified");
    }

    /**
     * WSS0192: Targets for encryption not specified
     * 
     */
    public static String WSS_0192_ENCRYPTION_TARGETS_NOT_SPECIFIED() {
        return localizer.localize(localizableWSS_0192_ENCRYPTION_TARGETS_NOT_SPECIFIED());
    }

    public static Localizable localizableWSS_0355_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0355.diag.cause.1");
    }

    /**
     * Error creating com.sun.org.apache.xml.internal.security.keys.content.keyvalues.DSAKeyValue
     * 
     */
    public static String WSS_0355_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0355_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0427_UNABLETO_DECODE_BASE_64() {
        return messageFactory.getMessage("WSS0427.unableto.decode.base64");
    }

    /**
     * WSS0427: Unable to decode Base64 encoded data
     * 
     */
    public static String WSS_0427_UNABLETO_DECODE_BASE_64() {
        return localizer.localize(localizableWSS_0427_UNABLETO_DECODE_BASE_64());
    }

    public static Localizable localizableWSS_0356_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0356.diag.cause.1");
    }

    /**
     * Error creating com.sun.org.apache.xml.internal.security.keys.content.X509Data
     * 
     */
    public static String WSS_0356_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0356_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0380_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0380.diag.check.1");
    }

    /**
     * Check that a SOAPElement with ds:Reference (child element) is not used to instantiate SecurityTokenReference
     * 
     */
    public static String WSS_0380_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0380_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0221_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0221.diag.cause.1");
    }

    /**
     * handle() call on the handler failed to set the Callback
     * 
     */
    public static String WSS_0221_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0221_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0700_SECURITY_ENVIRONMENT_NOTSET() {
        return messageFactory.getMessage("WSS0700.security.environment.notset");
    }

    /**
     * WSS0700: An instance of SecurityEnvironment is not set on SecurableSoapMessage
     * 
     */
    public static String WSS_0700_SECURITY_ENVIRONMENT_NOTSET() {
        return localizer.localize(localizableWSS_0700_SECURITY_ENVIRONMENT_NOTSET());
    }

    public static Localizable localizableWSS_0757_ERROR_SETTING_REFERENCE() {
        return messageFactory.getMessage("WSS0757.error.setting.reference");
    }

    /**
     * WSS0757: Error adding KeyIdentifier Value to wsse:KeyIdentifier
     * 
     */
    public static String WSS_0757_ERROR_SETTING_REFERENCE() {
        return localizer.localize(localizableWSS_0757_ERROR_SETTING_REFERENCE());
    }

    public static Localizable localizableWSS_0222_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0222.diag.cause.1");
    }

    /**
     * handle() call on the handler failed to set the Callback
     * 
     */
    public static String WSS_0222_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0222_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0368_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0368.diag.cause.1");
    }

    /**
     * Error getting SOAPEnvelope from SOAPPart
     * 
     */
    public static String WSS_0368_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0368_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0197_KEYINFOSTRATEGY_NULL() {
        return messageFactory.getMessage("WSS0197.keyinfostrategy.null");
    }

    /**
     * WSS0197: KeyInfoStrategy must be non-null
     * 
     */
    public static String WSS_0197_KEYINFOSTRATEGY_NULL() {
        return localizer.localize(localizableWSS_0197_KEYINFOSTRATEGY_NULL());
    }

    public static Localizable localizableWSS_0387_ERROR_CREATING_USERNAMETOKEN() {
        return messageFactory.getMessage("WSS0387.error.creating.usernametoken");
    }

    /**
     * WSS0387: Username is not set
     * 
     */
    public static String WSS_0387_ERROR_CREATING_USERNAMETOKEN() {
        return localizer.localize(localizableWSS_0387_ERROR_CREATING_USERNAMETOKEN());
    }

    public static Localizable localizableWSS_0357_ERROR_GETTING_KEYVALUE(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0357.error.getting.keyvalue", arg0, arg1);
    }

    /**
     * WSS0357: Can not get KeyValue for index [{0}] due to {1}
     * 
     */
    public static String WSS_0357_ERROR_GETTING_KEYVALUE(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0357_ERROR_GETTING_KEYVALUE(arg0, arg1));
    }

    public static Localizable localizableWSS_0224_UNSUPPORTED_ASSOCIATED_SUBJECT() {
        return messageFactory.getMessage("WSS0224.unsupported.AssociatedSubject");
    }

    /**
     * WSS0224: This environment does not have an associated Subject
     * 
     */
    public static String WSS_0224_UNSUPPORTED_ASSOCIATED_SUBJECT() {
        return localizer.localize(localizableWSS_0224_UNSUPPORTED_ASSOCIATED_SUBJECT());
    }

    public static Localizable localizableWSS_0654_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0654.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0654_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0654_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0369_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0369.diag.cause.2");
    }

    /**
     * Error creating SOAPHeader 
     * 
     */
    public static String WSS_0369_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0369_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0328_ERROR_PARSING_CREATIONTIME() {
        return messageFactory.getMessage("WSS0328.error.parsing.creationtime");
    }

    /**
     * WSS0328: Error while parsing creation time
     * 
     */
    public static String WSS_0328_ERROR_PARSING_CREATIONTIME() {
        return localizer.localize(localizableWSS_0328_ERROR_PARSING_CREATIONTIME());
    }

    public static Localizable localizableWSS_0369_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0369.diag.cause.1");
    }

    /**
     * Error getting SOAPHeader from SOAPEnvelope
     * 
     */
    public static String WSS_0369_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0369_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0280_FAILED_CREATE_USERNAME_TOKEN() {
        return messageFactory.getMessage("WSS0280.failed.create.UsernameToken");
    }

    /**
     * WSS0280: Exception occured while trying to create username token
     * 
     */
    public static String WSS_0280_FAILED_CREATE_USERNAME_TOKEN() {
        return localizer.localize(localizableWSS_0280_FAILED_CREATE_USERNAME_TOKEN());
    }

    public static Localizable localizableWSS_0335_UNSUPPORTED_REFERENCETYPE() {
        return messageFactory.getMessage("WSS0335.unsupported.referencetype");
    }

    /**
     * WSS0335:unsupported Reference Type encountered 
     * 
     */
    public static String WSS_0335_UNSUPPORTED_REFERENCETYPE() {
        return localizer.localize(localizableWSS_0335_UNSUPPORTED_REFERENCETYPE());
    }

    public static Localizable localizableWSS_0182_REFERENCELIST_PARAMETER_NULL() {
        return messageFactory.getMessage("WSS0182.referencelist.parameter.null");
    }

    /**
     * WSS0182: The xenc:Referencelist parameter required by DecryptReferenceList filter is null.
     * 
     */
    public static String WSS_0182_REFERENCELIST_PARAMETER_NULL() {
        return localizer.localize(localizableWSS_0182_REFERENCELIST_PARAMETER_NULL());
    }

    public static Localizable localizableWSS_0393_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0393.diag.check.1");
    }

    /**
     * Check system time and ensure it is correct
     * 
     */
    public static String WSS_0393_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0393_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_5420_DIGEST_METHOD() {
        return messageFactory.getMessage("BSP5420.digest.method");
    }

    /**
     * BSP 5420 : A Digest algorithm should have value "http://www.w3.org/2000/09/xmldsig#sha1".
     * 
     */
    public static String BSP_5420_DIGEST_METHOD() {
        return localizer.localize(localizableBSP_5420_DIGEST_METHOD());
    }

    public static Localizable localizableWSS_0355_ERROR_CREATING_KEYVAL(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0355.error.creating.keyval", arg0, arg1);
    }

    /**
     * WSS0355: Error creating {0}KeyValue due to {1}
     * 
     */
    public static String WSS_0355_ERROR_CREATING_KEYVAL(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0355_ERROR_CREATING_KEYVAL(arg0, arg1));
    }

    public static Localizable localizableWSS_0226_FAILED_VALIDATING_DEFAULT_CREATION_TIME() {
        return messageFactory.getMessage("WSS0226.failed.Validating.DefaultCreationTime");
    }

    /**
     * WSS0226: An Error occurred while parsing default creation time into Date format.
     * 
     */
    public static String WSS_0226_FAILED_VALIDATING_DEFAULT_CREATION_TIME() {
        return localizer.localize(localizableWSS_0226_FAILED_VALIDATING_DEFAULT_CREATION_TIME());
    }

    public static Localizable localizableWSS_0199_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0199.diag.cause.1");
    }

    /**
     * Timestamp creation time can not be null
     * 
     */
    public static String WSS_0199_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0199_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0394_ERROR_PARSING_EXPIRATIONTIME() {
        return messageFactory.getMessage("WSS0394.error.parsing.expirationtime");
    }

    /**
     * WSS0394: An Error occurred while parsing expiration/creation time into Date format.
     * 
     */
    public static String WSS_0394_ERROR_PARSING_EXPIRATIONTIME() {
        return localizer.localize(localizableWSS_0394_ERROR_PARSING_EXPIRATIONTIME());
    }

    public static Localizable localizableWSS_0520_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0520.diag.check.1");
    }

    /**
     * Check that the configuration file is consistent with the security configuration schema
     * 
     */
    public static String WSS_0520_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0520_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0386_ERROR_CREATING_TIMESTAMP(Object arg0) {
        return messageFactory.getMessage("WSS0386.error.creating.timestamp", arg0);
    }

    /**
     * WSS0386: Can not create SOAPElement representation of Timestamp due to {0}
     * 
     */
    public static String WSS_0386_ERROR_CREATING_TIMESTAMP(Object arg0) {
        return localizer.localize(localizableWSS_0386_ERROR_CREATING_TIMESTAMP(arg0));
    }

    public static Localizable localizableBSP_5603_ENCRYPTEDKEY_ENCRYPTIONMEHOD(Object arg0) {
        return messageFactory.getMessage("BSP5603.encryptedkey.encryptionmehod", arg0);
    }

    /**
     * BSP5603: EncryptedKey element with ID {0} MUST contain an EncryptionMethod child element.
     * 
     */
    public static String BSP_5603_ENCRYPTEDKEY_ENCRYPTIONMEHOD(Object arg0) {
        return localizer.localize(localizableBSP_5603_ENCRYPTEDKEY_ENCRYPTIONMEHOD(arg0));
    }

    public static Localizable localizableWSS_0605_STR_TRANSFORM_EXCEPTION(Object arg0) {
        return messageFactory.getMessage("WSS0605.str.transform.exception", arg0);
    }

    /**
     * WSS0605: Error applying STR Transform due to {0}
     * 
     */
    public static String WSS_0605_STR_TRANSFORM_EXCEPTION(Object arg0) {
        return localizer.localize(localizableWSS_0605_STR_TRANSFORM_EXCEPTION(arg0));
    }

    public static Localizable localizableWSS_0702_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0702.diag.check.1");
    }

    /**
     * Check that a valid X509v3 Certificate is present in Keystores
     * 
     */
    public static String WSS_0702_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0702_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0508_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0508.diag.cause.1");
    }

    /**
     * Default settings can not be specified after custom settings are specified
     * 
     */
    public static String WSS_0508_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0508_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0758_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0758.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0758_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0758_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0350_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0350.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0350_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0350_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0129_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0129.diag.check.1");
    }

    /**
     * Check proper signature was generated while signing
     * 
     */
    public static String WSS_0129_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0129_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0760_WARNING_OPTIONALTARGET_ENFORCE_IGNORED() {
        return messageFactory.getMessage("WSS0760.warning.optionaltarget.enforce.ignored");
    }

    /**
     * WSS0760: Warning enforce attribute value 'true' on Optional Target ignored.
     * 
     */
    public static String WSS_0760_WARNING_OPTIONALTARGET_ENFORCE_IGNORED() {
        return localizer.localize(localizableWSS_0760_WARNING_OPTIONALTARGET_ENFORCE_IGNORED());
    }

    public static Localizable localizableWSS_0156_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0156.diag.cause.1");
    }

    /**
     * Error in certificate used for validation
     * 
     */
    public static String WSS_0156_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0156_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0411_SAML_MISSING_ELEMENT() {
        return messageFactory.getMessage("WSS0411.saml.missing.element");
    }

    /**
     * WSS0411: Missing element
     * 
     */
    public static String WSS_0411_SAML_MISSING_ELEMENT() {
        return localizer.localize(localizableWSS_0411_SAML_MISSING_ELEMENT());
    }

    public static Localizable localizableWSS_0339_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0339.diag.cause.1");
    }

    /**
     * Support for processing information in the given ds:KeyInfo is not present
     * 
     */
    public static String WSS_0339_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0339_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0278_FAILEDTO_GET_LOCAL_NAME() {
        return messageFactory.getMessage("WSS0278.failedto.get.localName");
    }

    /**
     * WSS0278: Exception occured in getting localName of SOAPElement
     * 
     */
    public static String WSS_0278_FAILEDTO_GET_LOCAL_NAME() {
        return localizer.localize(localizableWSS_0278_FAILEDTO_GET_LOCAL_NAME());
    }

    public static Localizable localizableWSS_0651_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0651.diag.cause.1");
    }

    /**
     * Failed to create JAXP DocumentBuilder instance
     * 
     */
    public static String WSS_0651_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0651_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0181_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0181.diag.check.1");
    }

    /**
     * Check that the user is authorized 
     * 
     */
    public static String WSS_0181_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0181_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0652_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0652.diag.cause.1");
    }

    /**
     * Error parsing xws-security-users file
     * 
     */
    public static String WSS_0652_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0652_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0363_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0363.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0363_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0363_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0359_ERROR_ADDING_X_509_DATA(Object arg0) {
        return messageFactory.getMessage("WSS0359.error.adding.x509data", arg0);
    }

    /**
     * WSS0359: Error adding X509Data due to {0}
     * 
     */
    public static String WSS_0359_ERROR_ADDING_X_509_DATA(Object arg0) {
        return localizer.localize(localizableWSS_0359_ERROR_ADDING_X_509_DATA(arg0));
    }

    public static Localizable localizableWSS_0807_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0807.diag.check.1");
    }

    /**
     * Look at underlying exception for clues
     * 
     */
    public static String WSS_0807_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0807_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0169_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0169.diag.cause.1");
    }

    /**
     * Value of FilterParameterConstants.BINARY_SEC_TOKEN is not set
     * 
     */
    public static String WSS_0169_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0169_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0514_ILLEGAL_NESTED_ELEMENT(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0514.illegal.nested.element", arg0, arg1);
    }

    /**
     * WSS0514: Unexpected child element: {0} in {1}
     * 
     */
    public static String WSS_0514_ILLEGAL_NESTED_ELEMENT(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0514_ILLEGAL_NESTED_ELEMENT(arg0, arg1));
    }

    public static Localizable localizableWSS_0205_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0205.diag.cause.1");
    }

    /**
     * Requirement for wsu:Timestamp has not been met
     * 
     */
    public static String WSS_0205_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0205_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0391_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0391.diag.cause.1");
    }

    /**
     * Invalid Localname and NamespaceURI of the SOAPElement used for creating the token
     * 
     */
    public static String WSS_0391_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0391_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0319_EXCEPTION_ADDING_KEYNAME() {
        return messageFactory.getMessage("WSS0319.exception.adding.keyname");
    }

    /**
     * WSS0319: Exception while adding keyname to KeyInfo Header block
     * 
     */
    public static String WSS_0319_EXCEPTION_ADDING_KEYNAME() {
        return localizer.localize(localizableWSS_0319_EXCEPTION_ADDING_KEYNAME());
    }

    public static Localizable localizableWSS_0138_UNABLETO_FIND_WSSE_KEYIDENTIFIER() {
        return messageFactory.getMessage("WSS0138.unableto.find.wsse.keyidentifier");
    }

    /**
     * WSS0138: Unable to find wsse:KeyIdentifier
     * 
     */
    public static String WSS_0138_UNABLETO_FIND_WSSE_KEYIDENTIFIER() {
        return localizer.localize(localizableWSS_0138_UNABLETO_FIND_WSSE_KEYIDENTIFIER());
    }

    public static Localizable localizableBSP_3030_ENCODING_TYPE_INVALID() {
        return messageFactory.getMessage("BSP3030.EncodingType.Invalid");
    }

    /**
     * BSP3030: EncodingType attribute value in wsse:BinarySecurityToken is invalid.
     * 
     */
    public static String BSP_3030_ENCODING_TYPE_INVALID() {
        return localizer.localize(localizableBSP_3030_ENCODING_TYPE_INVALID());
    }

    public static Localizable localizableWSS_0194_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0194.diag.check.1");
    }

    /**
     * Check that a valid XPath/QName/wsuId are specified complying to the spec.
     * 
     */
    public static String WSS_0194_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0194_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0320_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0320.diag.check.1");
    }

    /**
     * Make sure the KeyName exists in the KeyInfo 
     * 
     */
    public static String WSS_0320_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0320_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0755_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0755.diag.cause.1");
    }

    /**
     * Error embedding token in TokenReference
     * 
     */
    public static String WSS_0755_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0755_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0700_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0700.diag.cause.1");
    }

    /**
     * An instance of SecurityEnvironment is not set on SecurableSoapMessage
     * 
     */
    public static String WSS_0700_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0700_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0126_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0126.diag.cause.1");
    }

    /**
     * Unsupported algorithm type. Only RSA supported.
     * 
     */
    public static String WSS_0126_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0126_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0376_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0376.diag.check.2");
    }

    /**
     * Refer J2SE Documentation for more
     * 
     */
    public static String WSS_0376_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0376_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0376_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0376.diag.check.1");
    }

    /**
     * Check that the SecurityHeaderBlock can be transformed to a valid SOAPElement
     * 
     */
    public static String WSS_0376_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0376_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0321_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0321.diag.check.1");
    }

    /**
     * Check the element to be converted to SOAPElement
     * 
     */
    public static String WSS_0321_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0321_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_5623_ENCRYPTEDKEY_ENCODING(Object arg0) {
        return messageFactory.getMessage("BSP5623.encryptedkey.encoding", arg0);
    }

    /**
     * BSP 5623 :  EncryptedKey element with ID {0} MUST NOT contain an encoding attribute.
     * 
     */
    public static String BSP_5623_ENCRYPTEDKEY_ENCODING(Object arg0) {
        return localizer.localize(localizableBSP_5623_ENCRYPTEDKEY_ENCODING(arg0));
    }

    public static Localizable localizableWSS_0301_UNABLETO_DECODE_DATA() {
        return messageFactory.getMessage("WSS0301.unableto.decode.data");
    }

    /**
     * WSS0301: Unable to decode data from token
     * 
     */
    public static String WSS_0301_UNABLETO_DECODE_DATA() {
        return localizer.localize(localizableWSS_0301_UNABLETO_DECODE_DATA());
    }

    public static Localizable localizableWSS_0756_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0756.diag.cause.1");
    }

    /**
     * Error creating SOAPElement for wsse:KeyIdentifier
     * 
     */
    public static String WSS_0756_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0756_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0376_ERROR_INSERTING_HEADER(Object arg0) {
        return messageFactory.getMessage("WSS0376.error.inserting.header", arg0);
    }

    /**
     * WSS0376: Can not insert header block due to {0}
     * 
     */
    public static String WSS_0376_ERROR_INSERTING_HEADER(Object arg0) {
        return localizer.localize(localizableWSS_0376_ERROR_INSERTING_HEADER(arg0));
    }

    public static Localizable localizableWSS_0503_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0503.diag.check.1");
    }

    /**
     * Check that the xml file follows schema for defining configuration
     * 
     */
    public static String WSS_0503_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0503_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0218_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0218.diag.cause.1");
    }

    /**
     * handle() call on the handler failed to set the Callback
     * 
     */
    public static String WSS_0218_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0218_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0333_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0333.diag.check.1");
    }

    /**
     * Check that the property javax.net.ssl.keyStore is set properly
     * 
     */
    public static String WSS_0333_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0333_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0804_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0804.diag.cause.1");
    }

    /**
     * Callback handler is null
     * 
     */
    public static String WSS_0804_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0804_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0389_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0389.diag.check.1");
    }

    /**
     * Check that the nonce encoding type used to create UsernameToken is Base64
     * 
     */
    public static String WSS_0389_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0389_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0334_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0334.diag.check.1");
    }

    /**
     * Check KeyIdentifier ValueType's value
     * 
     */
    public static String WSS_0334_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0334_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0424_NULL_OWNER_DOCUMENT_ELEMENT() {
        return messageFactory.getMessage("WSS0424.null.OwnerDocument.element");
    }

    /**
     * WSS0424: Element does not have an owner document
     * 
     */
    public static String WSS_0424_NULL_OWNER_DOCUMENT_ELEMENT() {
        return localizer.localize(localizableWSS_0424_NULL_OWNER_DOCUMENT_ELEMENT());
    }

    public static Localizable localizableWSS_0361_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0361.diag.cause.2");
    }

    /**
     * The org.w3c.dom.Document object passed ReferenceListHeaderBlock() may be null
     * 
     */
    public static String WSS_0361_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0361_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0361_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0361.diag.cause.1");
    }

    /**
     * An error may have occured creating org.w3c.dom.Element for ReferenceList
     * 
     */
    public static String WSS_0361_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0361_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0516_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0516.diag.check.1");
    }

    /**
     * Check that the configuration file is consistent with the security configuration schema
     * 
     */
    public static String WSS_0516_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0516_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0381_ERROR_SETTING_REFERENCE() {
        return messageFactory.getMessage("WSS0381.error.setting.reference");
    }

    /**
     * WSS0381: Can not set ds:Reference on SecurityTokenReference
     * 
     */
    public static String WSS_0381_ERROR_SETTING_REFERENCE() {
        return localizer.localize(localizableWSS_0381_ERROR_SETTING_REFERENCE());
    }

    public static Localizable localizableBSP_5416_SIGNATURE_TRANSFORM() {
        return messageFactory.getMessage("BSP5416.signature_transform");
    }

    /**
     * BSP 5416 : A Signature MUST contain a Transform child element.
     * 
     */
    public static String BSP_5416_SIGNATURE_TRANSFORM() {
        return localizer.localize(localizableBSP_5416_SIGNATURE_TRANSFORM());
    }

    public static Localizable localizableWSS_0251_INVALID_SECURITY_POLICY_INSTANCE() {
        return messageFactory.getMessage("WSS0251.invalid.SecurityPolicyInstance");
    }

    /**
     * WSS0251: SecurityPolicy instance should be of type WSSPolicy OR MessagePolicy OR DynamicSecurityPolicy
     * 
     */
    public static String WSS_0251_INVALID_SECURITY_POLICY_INSTANCE() {
        return localizer.localize(localizableWSS_0251_INVALID_SECURITY_POLICY_INSTANCE());
    }

    public static Localizable localizableWSS_0211_ERROR_DECRYPTING_KEY() {
        return messageFactory.getMessage("WSS0211.error.decrypting.key");
    }

    /**
     * WSS0211: Error decrypting encryption key
     * 
     */
    public static String WSS_0211_ERROR_DECRYPTING_KEY() {
        return localizer.localize(localizableWSS_0211_ERROR_DECRYPTING_KEY());
    }

    public static Localizable localizableWSS_0191_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0191.diag.cause.2");
    }

    /**
     * KeyName specified could not locate a key in the security environment
     * 
     */
    public static String WSS_0191_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0191_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0191_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0191.diag.cause.1");
    }

    /**
     * A SymmetricKey was not generated earlier that is set on the calling thread
     * 
     */
    public static String WSS_0191_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0191_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0346_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0346.diag.check.1");
    }

    /**
     * Check that the SOAPElement passed to EncryptedDataHeaderBlock is valid as per spec.
     * 
     */
    public static String WSS_0346_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0346_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0192_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0192.diag.cause.1");
    }

    /**
     * Atleast one target needs to be specified for encryption
     * 
     */
    public static String WSS_0192_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0192_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0347_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0347.diag.check.2");
    }

    /**
     * Check to see setCipherData() is called on the EncryptedType
     * 
     */
    public static String WSS_0347_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0347_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0347_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0347.diag.check.1");
    }

    /**
     * Check to see SOAPElement used to initialize EncryptedType has CipherData
     * 
     */
    public static String WSS_0347_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0347_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0500_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0500.diag.cause.1");
    }

    /**
     * Classname not a recognized classname for a MessageFilter
     * 
     */
    public static String WSS_0500_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0500_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0374_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0374.diag.cause.1");
    }

    /**
     * An Internal XPathAPI transformation error occurred
     * 
     */
    public static String WSS_0374_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0374_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0750_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0750.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0750_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0750_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0503_BAD_READER_STATE_2(Object arg0, Object arg1, Object arg2, Object arg3) {
        return messageFactory.getMessage("WSS0503.bad.reader.state.2", arg0, arg1, arg2, arg3);
    }

    /**
     * WSS0503: Bad reader state. Expected  {0} or {1} or {2} but got {3}
     * 
     */
    public static String WSS_0503_BAD_READER_STATE_2(Object arg0, Object arg1, Object arg2, Object arg3) {
        return localizer.localize(localizableWSS_0503_BAD_READER_STATE_2(arg0, arg1, arg2, arg3));
    }

    public static Localizable localizableWSS_0502_BAD_READER_STATE_1(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0502.bad.reader.state.1", arg0, arg1);
    }

    /**
     * WSS0502: Bad reader state. Expected  {0} but got {1}
     * 
     */
    public static String WSS_0502_BAD_READER_STATE_1(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0502_BAD_READER_STATE_1(arg0, arg1));
    }

    public static Localizable localizableWSS_0305_MESSAGE_DOESNOT_CONTAIN_SECHEADERBLOCK() {
        return messageFactory.getMessage("WSS0305.message.doesnot.contain.secheaderblock");
    }

    /**
     * WSS0305: Message does not contain wsse:Security header block
     * 
     */
    public static String WSS_0305_MESSAGE_DOESNOT_CONTAIN_SECHEADERBLOCK() {
        return localizer.localize(localizableWSS_0305_MESSAGE_DOESNOT_CONTAIN_SECHEADERBLOCK());
    }

    public static Localizable localizableWSS_0210_UNSUPPORTED_KEY_ENCRYPTION_ALGORITHM(Object arg0) {
        return messageFactory.getMessage("WSS0210.unsupported.key.encryption.algorithm", arg0);
    }

    /**
     * WSS0210: Unsupported Key Encryption Algorithm: {0}
     * 
     */
    public static String WSS_0210_UNSUPPORTED_KEY_ENCRYPTION_ALGORITHM(Object arg0) {
        return localizer.localize(localizableWSS_0210_UNSUPPORTED_KEY_ENCRYPTION_ALGORITHM(arg0));
    }

    public static Localizable localizableWSS_0212_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0212.diag.check.1");
    }

    /**
     * Check that the message meets the security requirements
     * 
     */
    public static String WSS_0212_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0212_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0303_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0303.diag.check.1");
    }

    /**
     * Check that the x509 data is valid. Could not extract raw bytes from it.
     * 
     */
    public static String WSS_0303_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0303_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0307_NONCE_ENCTYPE_INVALID() {
        return messageFactory.getMessage("WSS0307.nonce.enctype.invalid");
    }

    /**
     * WSS0307: Nonce encoding type invalid
     * 
     */
    public static String WSS_0307_NONCE_ENCTYPE_INVALID() {
        return localizer.localize(localizableWSS_0307_NONCE_ENCTYPE_INVALID());
    }

    public static Localizable localizableWSS_0213_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0213.diag.check.1");
    }

    /**
     * Check that the message meets the security requirements
     * 
     */
    public static String WSS_0213_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0213_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0194_ILLEGAL_TARGET(Object arg0) {
        return messageFactory.getMessage("WSS0194.illegal.target", arg0);
    }

    /**
     * WSS0194: Can not encrypt: {0}
     * 
     */
    public static String WSS_0194_ILLEGAL_TARGET(Object arg0) {
        return localizer.localize(localizableWSS_0194_ILLEGAL_TARGET(arg0));
    }

    public static Localizable localizableWSS_0501_PROPERTY_NOT_DEFINED(Object arg0) {
        return messageFactory.getMessage("WSS0501.property.not.defined", arg0);
    }

    /**
     * WSS0501: Property {0} not defined. Returning null
     * 
     */
    public static String WSS_0501_PROPERTY_NOT_DEFINED(Object arg0) {
        return localizer.localize(localizableWSS_0501_PROPERTY_NOT_DEFINED(arg0));
    }

    public static Localizable localizableWSS_0359_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0359.diag.check.1");
    }

    /**
     * Check that a valid com.sun.org.apache.xml.internal.security.keys.content.X509Data (as per specs.) is passed to addX509Data()
     * 
     */
    public static String WSS_0359_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0359_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0331_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0331.diag.cause.1");
    }

    /**
     * Element may not be a  proper UsernameToken
     * 
     */
    public static String WSS_0331_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0331_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0758_SOAP_EXCEPTION(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0758.soap.exception", arg0, arg1);
    }

    /**
     * WSS0758: Error creating javax.xml.soap.Name for {0} due to {1}
     * 
     */
    public static String WSS_0758_SOAP_EXCEPTION(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0758_SOAP_EXCEPTION(arg0, arg1));
    }

    public static Localizable localizableWSS_0513_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0513.diag.cause.1");
    }

    /**
     * Non-permissable element on xwss:SecurityConfiguration
     * 
     */
    public static String WSS_0513_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0513_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0387_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0387.diag.cause.1");
    }

    /**
     * Username is not set
     * 
     */
    public static String WSS_0387_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0387_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0763_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0763.diag.check.1");
    }

    /**
     * Check IssuerName is correctly present in the SOAP Message
     * 
     */
    public static String WSS_0763_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0763_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0804_CALLBACK_HANDLER_NOTSET() {
        return messageFactory.getMessage("WSS0804.callback.handler.notset");
    }

    /**
     * SecurityEnvironment/javax.security.auth.callback.CallbackHandler implementation not set in the ProcessingContext
     * 
     */
    public static String WSS_0804_CALLBACK_HANDLER_NOTSET() {
        return localizer.localize(localizableWSS_0804_CALLBACK_HANDLER_NOTSET());
    }

    public static Localizable localizableWSS_0604_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0604.diag.cause.1");
    }

    /**
     * An element with the given wsu:Id/Id/SAMLAssertionID can not be located
     * 
     */
    public static String WSS_0604_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0604_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0134_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0134.diag.check.1");
    }

    /**
     * Check that the XMLCipher was initialized properly
     * 
     */
    public static String WSS_0134_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0134_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0514_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0514.diag.cause.1");
    }

    /**
     * Non-permissable child element in a Security Configuration element
     * 
     */
    public static String WSS_0514_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0514_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0605_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0605.diag.cause.1");
    }

    /**
     * Error applying STR Transform
     * 
     */
    public static String WSS_0605_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0605_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0316_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0316.diag.check.1");
    }

    /**
     * Check that encoding value for BinarySecurity token is valid as per spec.
     * 
     */
    public static String WSS_0316_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0316_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0214_FAILED_SENDER_AUTHENTICATION() {
        return messageFactory.getMessage("WSS0214.failed.sender.authentication");
    }

    /**
     * WSS0214: UsernameToken Authentication Failed
     * 
     */
    public static String WSS_0214_FAILED_SENDER_AUTHENTICATION() {
        return localizer.localize(localizableWSS_0214_FAILED_SENDER_AUTHENTICATION());
    }

    public static Localizable localizableWSS_0317_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0317.diag.check.1");
    }

    /**
     * Ensure certificate path is not empty and certificate type is correct
     * 
     */
    public static String WSS_0317_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0317_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0205_POLICY_VIOLATION_EXCEPTION() {
        return messageFactory.getMessage("WSS0205.policy.violation.exception");
    }

    /**
     * WSS0205: Requirement for wsu:Timestamp not met
     * 
     */
    public static String WSS_0205_POLICY_VIOLATION_EXCEPTION() {
        return localizer.localize(localizableWSS_0205_POLICY_VIOLATION_EXCEPTION());
    }

    public static Localizable localizableWSS_0344_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0344.diag.cause.1");
    }

    /**
     * The binary data in the Security Token can not be decoded, expected Base64 encoding 
     * 
     */
    public static String WSS_0344_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0344_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0372_ERROR_APACHE_XPATH_API(Object arg0) {
        return messageFactory.getMessage("WSS0372.error.apache.xpathAPI", arg0);
    }

    /**
     * WSS0372: Can not find elements with Id attribute due to {0}
     * 
     */
    public static String WSS_0372_ERROR_APACHE_XPATH_API(Object arg0) {
        return localizer.localize(localizableWSS_0372_ERROR_APACHE_XPATH_API(arg0));
    }

    public static Localizable localizableWSS_0147_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0147.diag.check.1");
    }

    /**
     * Make sure style sheet is valid
     * 
     */
    public static String WSS_0147_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0147_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0202_MISSING_SECURITY_HEADER() {
        return messageFactory.getMessage("WSS0202.missing.security.header");
    }

    /**
     * WSS0202: No wsse:Security element in the message
     * 
     */
    public static String WSS_0202_MISSING_SECURITY_HEADER() {
        return localizer.localize(localizableWSS_0202_MISSING_SECURITY_HEADER());
    }

    public static Localizable localizableWSS_0759_ERROR_CREATING_ISSUERSERIAL() {
        return messageFactory.getMessage("WSS0759.error.creating.issuerserial");
    }

    /**
     * WSS0759: Error creating X509IssuerSerial instance
     * 
     */
    public static String WSS_0759_ERROR_CREATING_ISSUERSERIAL() {
        return localizer.localize(localizableWSS_0759_ERROR_CREATING_ISSUERSERIAL());
    }

    public static Localizable localizableWSS_0653_ERROR_READING_FILE(Object arg0) {
        return messageFactory.getMessage("WSS0653.error.reading.file", arg0);
    }

    /**
     * WSS0653: Error reading {0} file
     * 
     */
    public static String WSS_0653_ERROR_READING_FILE(Object arg0) {
        return localizer.localize(localizableWSS_0653_ERROR_READING_FILE(arg0));
    }

    public static Localizable localizableWSS_0329_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0329.diag.check.1");
    }

    /**
     * Check that the next element is UsernameToken
     * 
     */
    public static String WSS_0329_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0329_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0210_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0210.diag.cause.1");
    }

    /**
     * Only RSAv1.5 Key Encryption Algorithm is supported
     * 
     */
    public static String WSS_0210_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0210_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0301_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0301.diag.cause.1");
    }

    /**
     * Data malformed. Base 64 decoding error
     * 
     */
    public static String WSS_0301_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0301_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0286_INVALID_NOOF_ELEMENTS() {
        return messageFactory.getMessage("WSS0286.invalid.NoofElements");
    }

    /**
     * WSS0286: More than one element exists with Id/WsuId
     * 
     */
    public static String WSS_0286_INVALID_NOOF_ELEMENTS() {
        return localizer.localize(localizableWSS_0286_INVALID_NOOF_ELEMENTS());
    }

    public static Localizable localizableBSP_5423_SIGNATURE_TRANSFORM_ALGORITHM() {
        return messageFactory.getMessage("BSP5423.signature_transform_algorithm");
    }

    /**
     * BSP 5423 : A Signature transform algorithm MUST have a value of "http://www.w3.org/2001/10/xml-exc-c14n#" or "http://www.w3.org/2002/06/xmldsig-filter2" or "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#STR-Transform" or "http://www.w3.org/2000/09/xmldsig#enveloped-signature" or "http://docs.oasis-open.org/wss/oasis-wss-SwAProfile-1.1#Attachment-Content-Signature-Transform" or "http://docs.oasis-open.org/wss/oasis-wss-SwAProfile-1.1#Attachment-Complete-Signature-Transform".
     * 
     */
    public static String BSP_5423_SIGNATURE_TRANSFORM_ALGORITHM() {
        return localizer.localize(localizableBSP_5423_SIGNATURE_TRANSFORM_ALGORITHM());
    }

    public static Localizable localizableWSS_0357_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0357.diag.cause.1");
    }

    /**
     * Error getting KeyValue from KeyInfo for the given index
     * 
     */
    public static String WSS_0357_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0357_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0393_CURRENT_AHEAD_OF_EXPIRES() {
        return messageFactory.getMessage("WSS0393.current.ahead.of.expires");
    }

    /**
     * WSS0393: The current time is ahead of the expiration time in Timestamp
     * 
     */
    public static String WSS_0393_CURRENT_AHEAD_OF_EXPIRES() {
        return localizer.localize(localizableWSS_0393_CURRENT_AHEAD_OF_EXPIRES());
    }

    public static Localizable localizableWSS_0711_ERROR_MATCH_CERT_FOR_DECODED_STRING() {
        return messageFactory.getMessage("WSS0711.error.match.cert.for.decoded.string");
    }

    /**
     * WSS0711: Error: An Error occurred while matching certification for Decoded String.
     * 
     */
    public static String WSS_0711_ERROR_MATCH_CERT_FOR_DECODED_STRING() {
        return localizer.localize(localizableWSS_0711_ERROR_MATCH_CERT_FOR_DECODED_STRING());
    }

    public static Localizable localizableWSS_0381_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0381.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0381_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0381_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0263_INVALID_MESSAGE_POLICY() {
        return messageFactory.getMessage("WSS0263.invalid.Message.policy");
    }

    /**
     * WSS0263: Message does not conform to configured policy
     * 
     */
    public static String WSS_0263_INVALID_MESSAGE_POLICY() {
        return localizer.localize(localizableWSS_0263_INVALID_MESSAGE_POLICY());
    }

    public static Localizable localizableWSS_0366_MULTIPLE_ENCRYPTEDDATA_FOUND() {
        return messageFactory.getMessage("WSS0366.multiple.encrypteddata.found");
    }

    /**
     * WSS0366: More than one xenc:EncryptedData found
     * 
     */
    public static String WSS_0366_MULTIPLE_ENCRYPTEDDATA_FOUND() {
        return localizer.localize(localizableWSS_0366_MULTIPLE_ENCRYPTEDDATA_FOUND());
    }

    public static Localizable localizableWSS_0148_UNABLETO_PROCESS_SOAPMESSAGE(Object arg0) {
        return messageFactory.getMessage("WSS0148.unableto.process.soapmessage", arg0);
    }

    /**
     * WSS0148: Exception [ {0} ] while processing SOAPMessage
     * 
     */
    public static String WSS_0148_UNABLETO_PROCESS_SOAPMESSAGE(Object arg0) {
        return localizer.localize(localizableWSS_0148_UNABLETO_PROCESS_SOAPMESSAGE(arg0));
    }

    public static Localizable localizableWSS_0761_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0761.diag.cause.1");
    }

    /**
     * require context argument to be either a ServletEndpointContext or a com.sun.xml.wss.ProcessingContext
     * 
     */
    public static String WSS_0761_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0761_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0187_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0187.diag.cause.1");
    }

    /**
     * Can not instantiate/initialize filter with null prefix
     * 
     */
    public static String WSS_0187_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0187_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0382_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0382.diag.check.2");
    }

    /**
     * Check that a non-null SOAPElement is passed to appendObject()
     * 
     */
    public static String WSS_0382_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0382_DIAG_CHECK_2());
    }

    public static Localizable localizableBSP_3062_STR_URIATTRIBUTE() {
        return messageFactory.getMessage("BSP3062.str.uriattribute");
    }

    /**
     * BSP 3062 : Reference element under wsse:SecurityTokenReference MUST have a URI attribute.
     * 
     */
    public static String BSP_3062_STR_URIATTRIBUTE() {
        return localizer.localize(localizableBSP_3062_STR_URIATTRIBUTE());
    }

    public static Localizable localizableWSS_0382_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0382.diag.check.1");
    }

    /**
     * Check that a valid ds:Object SOAPElement (as per spec.) is passed to appendObject()
     * 
     */
    public static String WSS_0382_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0382_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0188_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0188.diag.cause.1");
    }

    /**
     * Can not instantiate/initialize filter with null namespace
     * 
     */
    public static String WSS_0188_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0188_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0701_CANNOT_LOCATE_CERTIFICATE(Object arg0) {
        return messageFactory.getMessage("WSS0701.cannot.locate.certificate", arg0);
    }

    /**
     * WSS0701: Can not locate an X509v3 Certificate to obtain the KeyIdentifier value for alias: {0}
     * 
     */
    public static String WSS_0701_CANNOT_LOCATE_CERTIFICATE(Object arg0) {
        return localizer.localize(localizableWSS_0701_CANNOT_LOCATE_CERTIFICATE(arg0));
    }

    public static Localizable localizableWSS_0404_SAML_INVALID_VERSION() {
        return messageFactory.getMessage("WSS0404.saml.invalid.version");
    }

    /**
     * WSS0404: Invalid SAML version Encountered.
     * 
     */
    public static String WSS_0404_SAML_INVALID_VERSION() {
        return localizer.localize(localizableWSS_0404_SAML_INVALID_VERSION());
    }

    public static Localizable localizableWSS_0655_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0655.diag.check.1");
    }

    /**
     * Check that the Class object corresponds to the header block identified by the SOAPElement
     * 
     */
    public static String WSS_0655_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0655_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0370_ERROR_PROCESSING_SECHEADER() {
        return messageFactory.getMessage("WSS0370.error.processing.secheader");
    }

    /**
     * WSS0370: Error processing SecurityHeader
     * 
     */
    public static String WSS_0370_ERROR_PROCESSING_SECHEADER() {
        return localizer.localize(localizableWSS_0370_ERROR_PROCESSING_SECHEADER());
    }

    public static Localizable localizableWSS_0222_CANNOT_LOCATE_PRIVKEY(Object arg0) {
        return messageFactory.getMessage("WSS0222.cannot.locate.privkey", arg0);
    }

    /**
     * WSS0222: Unable to locate matching private key for {0} using Callback Handler.
     * 
     */
    public static String WSS_0222_CANNOT_LOCATE_PRIVKEY(Object arg0) {
        return localizer.localize(localizableWSS_0222_CANNOT_LOCATE_PRIVKEY(arg0));
    }

    public static Localizable localizableWSS_0342_VALTYPE_INVALID() {
        return messageFactory.getMessage("WSS0342.valtype.invalid");
    }

    /**
     * WSS0342: ValueType type invalid
     * 
     */
    public static String WSS_0342_VALTYPE_INVALID() {
        return localizer.localize(localizableWSS_0342_VALTYPE_INVALID());
    }

    public static Localizable localizableWSS_0709_ERROR_GETTING_RAW_CONTENT() {
        return messageFactory.getMessage("WSS0709.error.getting.rawContent");
    }

    /**
     * WSS0709: Error while getting certificate's raw content
     * 
     */
    public static String WSS_0709_ERROR_GETTING_RAW_CONTENT() {
        return localizer.localize(localizableWSS_0709_ERROR_GETTING_RAW_CONTENT());
    }

    public static Localizable localizableWSS_0144_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0144.diag.cause.1");
    }

    /**
     * Base64Decoding exception is the root cause
     * 
     */
    public static String WSS_0144_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0144_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0208_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0208.diag.check.1");
    }

    /**
     * Check that the message strictly meets the security requirements
     * 
     */
    public static String WSS_0208_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0208_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0394_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0394.diag.check.1");
    }

    /**
     * Check date format is in UTC. Check it is "yyyy-MM-dd'T'HH:mm:ss'Z'" or "yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'"
     * 
     */
    public static String WSS_0394_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0394_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0209_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0209.diag.check.1");
    }

    /**
     * Check that the filters are correctly configured to process inbound msgs
     * 
     */
    public static String WSS_0209_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0209_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0395_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0395.diag.check.1");
    }

    /**
     * Refer SAAJ APIs
     * 
     */
    public static String WSS_0395_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0395_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_3222_ELEMENT_NOT_ALLOWED_UNDER_TIMESTMP(Object arg0) {
        return messageFactory.getMessage("BSP3222.element_not_allowed_under_timestmp", arg0);
    }

    /**
     * BSP3222: {0} is not allowed under TIMESTAMP. 
     * 
     */
    public static String BSP_3222_ELEMENT_NOT_ALLOWED_UNDER_TIMESTMP(Object arg0) {
        return localizer.localize(localizableBSP_3222_ELEMENT_NOT_ALLOWED_UNDER_TIMESTMP(arg0));
    }

    public static Localizable localizableWSS_0327_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0327.diag.cause.1");
    }

    /**
     * Could not retrieve element from Signature or could not import the node
     * 
     */
    public static String WSS_0327_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0327_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0255_INVALID_CONFIGURED_POLICY_TIMESTAMP() {
        return messageFactory.getMessage("WSS0255.invalid.configuredPolicy.Timestamp");
    }

    /**
     * WSS0255: Timestamp not found in configured policy but occurs in message
     * 
     */
    public static String WSS_0255_INVALID_CONFIGURED_POLICY_TIMESTAMP() {
        return localizer.localize(localizableWSS_0255_INVALID_CONFIGURED_POLICY_TIMESTAMP());
    }

    public static Localizable localizableWSS_0334_UNSUPPORTED_KEYIDENTIFIER() {
        return messageFactory.getMessage("WSS0334.unsupported.keyidentifier");
    }

    /**
     * WSS0334:unsupported KeyIdentifier Reference Type encountered 
     * 
     */
    public static String WSS_0334_UNSUPPORTED_KEYIDENTIFIER() {
        return localizer.localize(localizableWSS_0334_UNSUPPORTED_KEYIDENTIFIER());
    }

    public static Localizable localizableWSS_0509_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0509.diag.cause.1");
    }

    /**
     * Custom settings can not be specified after default settings are specified
     * 
     */
    public static String WSS_0509_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0509_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0412_SAML_CONDITION_NOT_SUPPORTED() {
        return messageFactory.getMessage("WSS0412.saml.condition.not.supported");
    }

    /**
     * WSS0412: The specified condition is not supported
     * 
     */
    public static String WSS_0412_SAML_CONDITION_NOT_SUPPORTED() {
        return localizer.localize(localizableWSS_0412_SAML_CONDITION_NOT_SUPPORTED());
    }

    public static Localizable localizableWSS_0249_UNSUPPORTED_TOKEN_TYPE_DKT() {
        return messageFactory.getMessage("WSS0249.unsupported.TokenType.DKT");
    }

    /**
     * WSS0249: Unsupported TokenType under DerivedKey Token
     * 
     */
    public static String WSS_0249_UNSUPPORTED_TOKEN_TYPE_DKT() {
        return localizer.localize(localizableWSS_0249_UNSUPPORTED_TOKEN_TYPE_DKT());
    }

    public static Localizable localizableWSS_0759_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0759.diag.check.1");
    }

    /**
     * Check that the SOAPElement passed to the constructor is conformant to spec. (and has X509IssuerSerial child elements)
     * 
     */
    public static String WSS_0759_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0759_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0704_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0704.diag.check.1");
    }

    /**
     * Check that the agreement name: SESSION-KEY-VALUE, is set on SecurityEnvironment using setAgreementProperty() 
     * 
     */
    public static String WSS_0704_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0704_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0201_INEFFECTIVE_CALLBACK_HANDLER(Object arg0) {
        return messageFactory.getMessage("WSS0201.ineffective.callback.handler", arg0);
    }

    /**
     * WSS0201: Ineffective XWSSCallbackHandler due to: {0}
     * 
     */
    public static String WSS_0201_INEFFECTIVE_CALLBACK_HANDLER(Object arg0) {
        return localizer.localize(localizableWSS_0201_INEFFECTIVE_CALLBACK_HANDLER(arg0));
    }

    public static Localizable localizableWSS_0351_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0351.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0351_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0351_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0510_CANNOT_USE_KEYNAME() {
        return messageFactory.getMessage("WSS0510.cannot.use.keyname");
    }

    /**
     * WSS0510: Public Key information to verify a signature should be conveyed in the message
     * 
     */
    public static String WSS_0510_CANNOT_USE_KEYNAME() {
        return localizer.localize(localizableWSS_0510_CANNOT_USE_KEYNAME());
    }

    public static Localizable localizableWSS_0352_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0352.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0352_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0352_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0274_INVALID_SEC_TIMESTAMP() {
        return messageFactory.getMessage("WSS0274.invalid.SEC.Timestamp");
    }

    /**
     * WSS0274: More than one wsu:Timestamp element present in security header
     * 
     */
    public static String WSS_0274_INVALID_SEC_TIMESTAMP() {
        return localizer.localize(localizableWSS_0274_INVALID_SEC_TIMESTAMP());
    }

    public static Localizable localizableWSS_0182_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0182.diag.check.1");
    }

    /**
     * The reference list that needs to be decrypted usually set by ImportEncryptedKeyFilter
     * 
     */
    public static String WSS_0182_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0182_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0653_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0653.diag.cause.1");
    }

    /**
     * Error reading xws-security-users file
     * 
     */
    public static String WSS_0653_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0653_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0808_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0808.diag.check.1");
    }

    /**
     * SOAPBody should contain child with operation
     * 
     */
    public static String WSS_0808_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0808_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0365_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0365.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0365_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0365_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0206_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0206.diag.cause.1");
    }

    /**
     * Not all receiver requirements for security have been met
     * 
     */
    public static String WSS_0206_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0206_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0392_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0392.diag.cause.1");
    }

    /**
     * A version [3] X509Certificate is expected
     * 
     */
    public static String WSS_0392_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0392_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0292_UNSUPPORTED_OPERATION_REMOVE_ATTACHMENT() {
        return messageFactory.getMessage("WSS0292.unsupported.operation.removeAttachment");
    }

    /**
     * WSS0292: Unsupported Operation remove Attachment
     * 
     */
    public static String WSS_0292_UNSUPPORTED_OPERATION_REMOVE_ATTACHMENT() {
        return localizer.localize(localizableWSS_0292_UNSUPPORTED_OPERATION_REMOVE_ATTACHMENT());
    }

    public static Localizable localizableWSS_0209_UNEXPECTED_HEADER_BLOCK(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0209.unexpected.header.block", arg0, arg1);
    }

    /**
     * WSS0209: Expected {0}, found {1} in security header
     * 
     */
    public static String WSS_0209_UNEXPECTED_HEADER_BLOCK(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0209_UNEXPECTED_HEADER_BLOCK(arg0, arg1));
    }

    public static Localizable localizableWSS_0195_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0195.diag.check.2");
    }

    /**
     * Check that ExportReferenceListFilter is called before
     * 
     */
    public static String WSS_0195_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0195_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0254_FAILEDTO_PROCESS_PRIMARY_O_RSECONDARY_POLICY() {
        return messageFactory.getMessage("WSS0254.failedto.process.primaryORsecondary.policy");
    }

    /**
     * WSS0254: Failed to process Primary or Secondary Policy
     * 
     */
    public static String WSS_0254_FAILEDTO_PROCESS_PRIMARY_O_RSECONDARY_POLICY() {
        return localizer.localize(localizableWSS_0254_FAILEDTO_PROCESS_PRIMARY_O_RSECONDARY_POLICY());
    }

    public static Localizable localizableWSS_0195_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0195.diag.check.1");
    }

    /**
     * Check that ExportEncryptedKeyFilter is called before
     * 
     */
    public static String WSS_0195_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0195_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0701_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0701.diag.cause.2");
    }

    /**
     * If no alias has been specified for signing, no default certificate is available
     * 
     */
    public static String WSS_0701_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0701_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0701_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0701.diag.cause.1");
    }

    /**
     * No X509v3 Certificate can be located for the alias in Keystore
     * 
     */
    public static String WSS_0701_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0701_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0377_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0377.diag.check.1");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0377_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0377_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0322_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0322.diag.check.1");
    }

    /**
     * Check that a fully initialized XML Signature was passed
     * 
     */
    public static String WSS_0322_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0322_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0757_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0757.diag.cause.1");
    }

    /**
     * Error adding KeyIdentifier value to wsse:KeyIdentifer
     * 
     */
    public static String WSS_0757_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0757_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0378_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0378.diag.check.2");
    }

    /**
     * Refer your SAAJ API Documentation
     * 
     */
    public static String WSS_0378_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0378_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0717_NO_SAML_CALLBACK_HANDLER() {
        return messageFactory.getMessage("WSS0717.no.SAMLCallbackHandler");
    }

    /**
     * WSS0717: A Required SAML Callback Handler was not specified in configuration : Cannot Populate SAML Assertion
     * 
     */
    public static String WSS_0717_NO_SAML_CALLBACK_HANDLER() {
        return localizer.localize(localizableWSS_0717_NO_SAML_CALLBACK_HANDLER());
    }

    public static Localizable localizableWSS_0378_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0378.diag.check.1");
    }

    /**
     * Check that the org.w3c.dom.Document object passed to SecurityTokenReference() is non-null
     * 
     */
    public static String WSS_0378_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0378_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0219_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0219.diag.cause.1");
    }

    /**
     * handle() call on the handler failed to set the Callback
     * 
     */
    public static String WSS_0219_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0219_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0603_XPATHAPI_TRANSFORMER_EXCEPTION(Object arg0) {
        return messageFactory.getMessage("WSS0603.xpathapi.transformer.exception", arg0);
    }

    /**
     * WSS0603: XPathAPI TransformerException due to {0} in finding element with matching wsu:Id/Id/SAMLAssertionID 
     * 
     */
    public static String WSS_0603_XPATHAPI_TRANSFORMER_EXCEPTION(Object arg0) {
        return localizer.localize(localizableWSS_0603_XPATHAPI_TRANSFORMER_EXCEPTION(arg0));
    }

    public static Localizable localizableWSS_0515_ILLEGAL_KEYREFERENCE(Object arg0) {
        return messageFactory.getMessage("WSS0515.illegal.keyreference", arg0);
    }

    /**
     * WSS0515: Impermissable value for key reference string: {0}
     * 
     */
    public static String WSS_0515_ILLEGAL_KEYREFERENCE(Object arg0) {
        return localizer.localize(localizableWSS_0515_ILLEGAL_KEYREFERENCE(arg0));
    }

    public static Localizable localizableWSS_0277_INVALID_ADDTIONAL_SEC_MESSAGE_POLICY() {
        return messageFactory.getMessage("WSS0277.invalid.AddtionalSEC.Message.policy");
    }

    /**
     * WSS0277: Message does not conform to configured policy Additional security than required found
     * 
     */
    public static String WSS_0277_INVALID_ADDTIONAL_SEC_MESSAGE_POLICY() {
        return localizer.localize(localizableWSS_0277_INVALID_ADDTIONAL_SEC_MESSAGE_POLICY());
    }

    public static Localizable localizableBSP_5602_ENCRYPTEDKEY_RECIPIENT(Object arg0) {
        return messageFactory.getMessage("BSP5602.encryptedkey.recipient", arg0);
    }

    /**
     * BSP5602 : EncryptedKey with ID {0} MUST NOT contain Recipient attribute.
     * 
     */
    public static String BSP_5602_ENCRYPTEDKEY_RECIPIENT(Object arg0) {
        return localizer.localize(localizableBSP_5602_ENCRYPTEDKEY_RECIPIENT(arg0));
    }

    public static Localizable localizableWSS_0335_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0335.diag.check.1");
    }

    /**
     * KeyReference type chould be one of KeyIdentifier, Reference, X509Data
     * 
     */
    public static String WSS_0335_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0335_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0362_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0362.diag.cause.1");
    }

    /**
     * Invalid SOAPElement passed to ReferenceListHeaderBlock()
     * 
     */
    public static String WSS_0362_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0362_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0518_ILLEGAL_CONTENT_ONLY_USE() {
        return messageFactory.getMessage("WSS0518.illegal.contentOnly.use");
    }

    /**
     * WSS0518: ContentOnly flag may only be present on Target elements that are child elements of Encrypt or requireEncryption
     * 
     */
    public static String WSS_0518_ILLEGAL_CONTENT_ONLY_USE() {
        return localizer.localize(localizableWSS_0518_ILLEGAL_CONTENT_ONLY_USE());
    }

    public static Localizable localizableWSS_0517_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0517.diag.check.1");
    }

    /**
     * Check that the configuration file is consistent with the security configuration schema
     * 
     */
    public static String WSS_0517_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0517_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0608_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0608.diag.check.1");
    }

    /**
     * Check that the Reference Mechanism is either Direct/KeyIdentifier/X509IssuerSerial
     * 
     */
    public static String WSS_0608_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0608_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0283_UNSUPPORTED_REFERENCE_TYPE_DKT() {
        return messageFactory.getMessage("WSS0283.unsupported.ReferenceType.DKT");
    }

    /**
     * WSS0283: Unsupported ReferenceType under DerivedKey
     * 
     */
    public static String WSS_0283_UNSUPPORTED_REFERENCE_TYPE_DKT() {
        return localizer.localize(localizableWSS_0283_UNSUPPORTED_REFERENCE_TYPE_DKT());
    }

    public static Localizable localizableWSS_0245_FAILED_RESOLVE_SECURITY_TOKEN() {
        return messageFactory.getMessage("WSS0245.failed.resolve.SecurityToken");
    }

    /**
     * WSS0245: Exception occured while trying to resolve security token
     * 
     */
    public static String WSS_0245_FAILED_RESOLVE_SECURITY_TOKEN() {
        return localizer.localize(localizableWSS_0245_FAILED_RESOLVE_SECURITY_TOKEN());
    }

    public static Localizable localizableWSS_0165_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0165.diag.check.1");
    }

    /**
     * Check that the node represented by the XPath is a valid DOM element
     * 
     */
    public static String WSS_0165_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0165_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0193_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0193.diag.cause.1");
    }

    /**
     * Target specified does not correspond to a valid message part
     * 
     */
    public static String WSS_0193_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0193_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0348_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0348.diag.check.1");
    }

    /**
     * If SOAPElement is used to create EncryptedKeyHeaderBlock, check to see that it is valid as per spec. 
     * 
     */
    public static String WSS_0348_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0348_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0200_INEFFECTIVE_CALLBACK_HANDLER() {
        return messageFactory.getMessage("WSS0200.ineffective.callback.handler");
    }

    /**
     * WSS0200: Ineffective XWSSCallbackHandler
     * 
     */
    public static String WSS_0200_INEFFECTIVE_CALLBACK_HANDLER() {
        return localizer.localize(localizableWSS_0200_INEFFECTIVE_CALLBACK_HANDLER());
    }

    public static Localizable localizableBSP_3224_ONEEXPIRES_TIMESTAMP() {
        return messageFactory.getMessage("BSP3224.Oneexpires.Timestamp");
    }

    /**
     * BSP3203: A TIMESTAMP MUST have exactly one wsu:Expires element child.
     * 
     */
    public static String BSP_3224_ONEEXPIRES_TIMESTAMP() {
        return localizer.localize(localizableBSP_3224_ONEEXPIRES_TIMESTAMP());
    }

    public static Localizable localizableWSS_0375_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0375.diag.cause.1");
    }

    /**
     * An Internal XPathAPI transformation error occurred
     * 
     */
    public static String WSS_0375_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0375_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0243_INVALID_VALUE_TYPE_NON_SCT_TOKEN() {
        return messageFactory.getMessage("WSS0243.invalid.valueType.NonSCTToken");
    }

    /**
     * WSS0243: Incorrect ValueType specified for a Non SCT Token
     * 
     */
    public static String WSS_0243_INVALID_VALUE_TYPE_NON_SCT_TOKEN() {
        return localizer.localize(localizableWSS_0243_INVALID_VALUE_TYPE_NON_SCT_TOKEN());
    }

    public static Localizable localizableBSP_3029_ENCODING_TYPE_NOT_PRESENT() {
        return messageFactory.getMessage("BSP3029.EncodingType.NotPresent");
    }

    /**
     * BSP3029: Any wsse:BinarySecurityToken in a SECURE_ENVELOPE MUST have an EncodingType attribute.
     * 
     */
    public static String BSP_3029_ENCODING_TYPE_NOT_PRESENT() {
        return localizer.localize(localizableBSP_3029_ENCODING_TYPE_NOT_PRESENT());
    }

    public static Localizable localizableWSS_0751_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0751.diag.check.1");
    }

    /**
     * Check that a SOAPElement conformant to spec. is passed
     * 
     */
    public static String WSS_0751_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0751_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0502_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0502.diag.cause.1");
    }

    /**
     * Element encountered does not match element expected
     * 
     */
    public static String WSS_0502_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0502_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0752_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0752.diag.check.1");
    }

    /**
     * Check that a SOAPElement conformant to spec. is passed
     * 
     */
    public static String WSS_0752_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0752_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0332_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0332.diag.cause.1");
    }

    /**
     * Username was null
     * 
     */
    public static String WSS_0332_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0332_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0388_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0388.diag.cause.2");
    }

    /**
     * Error adding child SOAPElements to the UsernameToken element
     * 
     */
    public static String WSS_0388_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0388_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0388_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0388.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.SOAPElement for UsernameToken
     * 
     */
    public static String WSS_0388_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0388_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0515_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0515.diag.cause.1");
    }

    /**
     * Impermissable value for key reference string
     * 
     */
    public static String WSS_0515_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0515_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0256_FAILED_CONFIGURE_ASC() {
        return messageFactory.getMessage("WSS0256.failed.configure.ASC");
    }

    /**
     * WSS0256: Exception occured while trying to configure Application Security Configuration
     * 
     */
    public static String WSS_0256_FAILED_CONFIGURE_ASC() {
        return localizer.localize(localizableWSS_0256_FAILED_CONFIGURE_ASC());
    }

    public static Localizable localizableWSS_0206_POLICY_VIOLATION_EXCEPTION() {
        return messageFactory.getMessage("WSS0206.policy.violation.exception");
    }

    /**
     * WSS0206: Security Requirements have fully not been met
     * 
     */
    public static String WSS_0206_POLICY_VIOLATION_EXCEPTION() {
        return localizer.localize(localizableWSS_0206_POLICY_VIOLATION_EXCEPTION());
    }

    public static Localizable localizableWSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION(Object arg0) {
        return messageFactory.getMessage("WSS0216.callbackhandler.handle.exception", arg0);
    }

    /**
     * WSS0216: An Error occurred using Callback Handler for : {0}
     * 
     */
    public static String WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION(Object arg0) {
        return localizer.localize(localizableWSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION(arg0));
    }

    public static Localizable localizableWSS_0606_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0606.diag.cause.1");
    }

    /**
     * Input Node Set to STR Transform is emtpy
     * 
     */
    public static String WSS_0606_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0606_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0389_UNRECOGNIZED_NONCE_ENCODING(Object arg0) {
        return messageFactory.getMessage("WSS0389.unrecognized.nonce.encoding", arg0);
    }

    /**
     * WSS0389: Expected Base64 EncodingType, found {0}
     * 
     */
    public static String WSS_0389_UNRECOGNIZED_NONCE_ENCODING(Object arg0) {
        return localizer.localize(localizableWSS_0389_UNRECOGNIZED_NONCE_ENCODING(arg0));
    }

    public static Localizable localizableWSS_0262_INVALID_MESSAGE_POLICYTYPE() {
        return messageFactory.getMessage("WSS0262.invalid.Message.policytype");
    }

    /**
     * WSS0262: Message does not conform to configured policy: policy type not present in receiver requirements
     * 
     */
    public static String WSS_0262_INVALID_MESSAGE_POLICYTYPE() {
        return localizer.localize(localizableWSS_0262_INVALID_MESSAGE_POLICYTYPE());
    }

    public static Localizable localizableWSS_0318_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0318.diag.check.1");
    }

    /**
     * Check values passed to KeyInfo constructor
     * 
     */
    public static String WSS_0318_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0318_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0326_EXCEPTION_VERIFYING_SIGNATURE() {
        return messageFactory.getMessage("WSS0326.exception.verifying.signature");
    }

    /**
     * WSS0326: Exception while verifying signature
     * 
     */
    public static String WSS_0326_EXCEPTION_VERIFYING_SIGNATURE() {
        return localizer.localize(localizableWSS_0326_EXCEPTION_VERIFYING_SIGNATURE());
    }

    public static Localizable localizableWSS_0345_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0345.diag.cause.1");
    }

    /**
     * Error creating SOAPElement for EncryptedDataHeaderBlock
     * 
     */
    public static String WSS_0345_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0345_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0655_ERROR_CREATING_HEADERBLOCK(Object arg0) {
        return messageFactory.getMessage("WSS0655.error.creating.headerblock", arg0);
    }

    /**
     * WSS0655: Error creating an instance of header block for element {0}
     * 
     */
    public static String WSS_0655_ERROR_CREATING_HEADERBLOCK(Object arg0) {
        return localizer.localize(localizableWSS_0655_ERROR_CREATING_HEADERBLOCK(arg0));
    }

    public static Localizable localizableWSS_0368_SOAP_EXCEPTION(Object arg0) {
        return messageFactory.getMessage("WSS0368.soap.exception", arg0);
    }

    /**
     * WSS0368: Error getting SOAPEnvelope from SOAPPart due to {0}
     * 
     */
    public static String WSS_0368_SOAP_EXCEPTION(Object arg0) {
        return localizer.localize(localizableWSS_0368_SOAP_EXCEPTION(arg0));
    }

    public static Localizable localizableWSS_0508_UNABLETO_SET_DEFAULTS() {
        return messageFactory.getMessage("WSS0508.unableto.set.defaults");
    }

    /**
     * WSS0508: Can not specify default settings after custom settings are added
     * 
     */
    public static String WSS_0508_UNABLETO_SET_DEFAULTS() {
        return localizer.localize(localizableWSS_0508_UNABLETO_SET_DEFAULTS());
    }

    public static Localizable localizableWSS_0207_UNSUPPORTED_OPERATION_EXCEPTION() {
        return messageFactory.getMessage("WSS0207.unsupported.operation.exception");
    }

    /**
     * WSS0207: Operation not supported on calling object
     * 
     */
    public static String WSS_0207_UNSUPPORTED_OPERATION_EXCEPTION() {
        return localizer.localize(localizableWSS_0207_UNSUPPORTED_OPERATION_EXCEPTION());
    }

    public static Localizable localizableWSS_0500_CLASS_NOT_MESSAGEFILTER(Object arg0) {
        return messageFactory.getMessage("WSS0500.class.not.messagefilter", arg0);
    }

    /**
     * WSS0500: Class {0} does not implement MessageFilter
     * 
     */
    public static String WSS_0500_CLASS_NOT_MESSAGEFILTER(Object arg0) {
        return localizer.localize(localizableWSS_0500_CLASS_NOT_MESSAGEFILTER(arg0));
    }

    public static Localizable localizableWSS_0418_SAML_IMPORT_EXCEPTION() {
        return messageFactory.getMessage("WSS0418.saml.import.exception");
    }

    /**
     * WSS0418: Exception while importing SAML Token
     * 
     */
    public static String WSS_0418_SAML_IMPORT_EXCEPTION() {
        return localizer.localize(localizableWSS_0418_SAML_IMPORT_EXCEPTION());
    }

    public static Localizable localizableWSS_0156_EXCEPTION_IN_CERT_VALIDATE(Object arg0) {
        return messageFactory.getMessage("WSS0156.exception.in.cert.validate", arg0);
    }

    /**
     * WSS0156: Exception [ {0} ] while validating certificate
     * 
     */
    public static String WSS_0156_EXCEPTION_IN_CERT_VALIDATE(Object arg0) {
        return localizer.localize(localizableWSS_0156_EXCEPTION_IN_CERT_VALIDATE(arg0));
    }

    public static Localizable localizableWSS_0148_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0148.diag.check.1");
    }

    /**
     * Make sure the origingal SOAP Message and style sheet are both correct
     * 
     */
    public static String WSS_0148_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0148_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0426_FAILED_DSA_KEY_VALUE() {
        return messageFactory.getMessage("WSS0426.failed.DSAKeyValue");
    }

    /**
     * WSS0426: Exception occured while trying to get DSA Key Value
     * 
     */
    public static String WSS_0426_FAILED_DSA_KEY_VALUE() {
        return localizer.localize(localizableWSS_0426_FAILED_DSA_KEY_VALUE());
    }

    public static Localizable localizableWSS_0390_UNSUPPORTED_CHARSET_EXCEPTION() {
        return messageFactory.getMessage("WSS0390.unsupported.charset.exception");
    }

    /**
     * WSS0390: Charset UTF-8 is Unsupported
     * 
     */
    public static String WSS_0390_UNSUPPORTED_CHARSET_EXCEPTION() {
        return localizer.localize(localizableWSS_0390_UNSUPPORTED_CHARSET_EXCEPTION());
    }

    public static Localizable localizableWSS_0273_FAILEDTO_PROCESS_POLICY() {
        return messageFactory.getMessage("WSS0273.failedto.process.policy");
    }

    /**
     * WSS0273: Failed to process message policy
     * 
     */
    public static String WSS_0273_FAILEDTO_PROCESS_POLICY() {
        return localizer.localize(localizableWSS_0273_FAILEDTO_PROCESS_POLICY());
    }

    public static Localizable localizableWSS_0211_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0211.diag.cause.1");
    }

    /**
     * Only TripleDES Keys are supported
     * 
     */
    public static String WSS_0211_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0211_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0302_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0302.diag.cause.1");
    }

    /**
     * Certificate parsing problem
     * 
     */
    public static String WSS_0302_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0302_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0415_SAML_EMPTY_ELEMENT() {
        return messageFactory.getMessage("WSS0415.saml.empty.element");
    }

    /**
     * WSS0415: Element has not content
     * 
     */
    public static String WSS_0415_SAML_EMPTY_ELEMENT() {
        return localizer.localize(localizableWSS_0415_SAML_EMPTY_ELEMENT());
    }

    public static Localizable localizableWSS_0181_SUBJECT_NOT_AUTHORIZED(Object arg0) {
        return messageFactory.getMessage("WSS0181.subject.not.authorized", arg0);
    }

    /**
     * WSS0181: Subject [ {0} ] is not an authorized subject
     * 
     */
    public static String WSS_0181_SUBJECT_NOT_AUTHORIZED(Object arg0) {
        return localizer.localize(localizableWSS_0181_SUBJECT_NOT_AUTHORIZED(arg0));
    }

    public static Localizable localizableWSS_0413_SAML_INVALID_DATE_FORMAT() {
        return messageFactory.getMessage("WSS0413.saml.invalid.date.format");
    }

    /**
     * WSS0413: Invalid date format
     * 
     */
    public static String WSS_0413_SAML_INVALID_DATE_FORMAT() {
        return localizer.localize(localizableWSS_0413_SAML_INVALID_DATE_FORMAT());
    }

    public static Localizable localizableWSS_0358_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0358.diag.cause.1");
    }

    /**
     * Error getting X509Data from KeyInfo for the given index
     * 
     */
    public static String WSS_0358_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0358_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0303_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0303.diag.cause.1");
    }

    /**
     * Certificate encoding exception
     * 
     */
    public static String WSS_0303_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0303_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0762_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0762.diag.cause.1");
    }

    /**
     * The encodingType on KeyIdentifier is not supported
     * 
     */
    public static String WSS_0762_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0762_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0257_FAILEDTO_APPEND_SECURITY_POLICY_MESSAGE_POLICY() {
        return messageFactory.getMessage("WSS0257.failedto.append.SecurityPolicy.MessagePolicy");
    }

    /**
     * WSS0257: Failed to append security policy in message policy
     * 
     */
    public static String WSS_0257_FAILEDTO_APPEND_SECURITY_POLICY_MESSAGE_POLICY() {
        return localizer.localize(localizableWSS_0257_FAILEDTO_APPEND_SECURITY_POLICY_MESSAGE_POLICY());
    }

    public static Localizable localizableWSS_0374_ERROR_APACHE_XPATH_API(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0374.error.apache.xpathAPI", arg0, arg1);
    }

    /**
     * WSS0374: Can not find element with wsu:Id attribute value {0} due to {1}
     * 
     */
    public static String WSS_0374_ERROR_APACHE_XPATH_API(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0374_ERROR_APACHE_XPATH_API(arg0, arg1));
    }

    public static Localizable localizableWSS_0383_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0383.diag.check.1");
    }

    /**
     * Check that the Document used to instantiate SignatureHeaderBlock() is not null
     * 
     */
    public static String WSS_0383_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0383_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0321_EXCEPTION_CONVERTING_KEYINFO_TOSOAPELEM() {
        return messageFactory.getMessage("WSS0321.exception.converting.keyinfo.tosoapelem");
    }

    /**
     * WSS0321: Exception converting KeyInfo Header block to SOAPElement
     * 
     */
    public static String WSS_0321_EXCEPTION_CONVERTING_KEYINFO_TOSOAPELEM() {
        return localizer.localize(localizableWSS_0321_EXCEPTION_CONVERTING_KEYINFO_TOSOAPELEM());
    }

    public static Localizable localizableWSS_0600_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0600.diag.check.1");
    }

    /**
     * Check that the certificate referred to is valid and present in the Keystores
     * 
     */
    public static String WSS_0600_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0600_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0343_ERROR_CREATING_BST() {
        return messageFactory.getMessage("WSS0343.error.creating.bst");
    }

    /**
     * WSS0343: Error creating BinarySecurityToken
     * 
     */
    public static String WSS_0343_ERROR_CREATING_BST() {
        return localizer.localize(localizableWSS_0343_ERROR_CREATING_BST());
    }

    public static Localizable localizableWSS_0189_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0189.diag.cause.1");
    }

    /**
     * Data decryption algorithm has to be either Triple-DES, AES128-CBC, AES256-CBC
     * 
     */
    public static String WSS_0189_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0189_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0656_DIAG_CHECK_2() {
        return messageFactory.getMessage("WSS0656.diag.check.2");
    }

    /**
     * Check that a keystore file exists in $user.home
     * 
     */
    public static String WSS_0656_DIAG_CHECK_2() {
        return localizer.localize(localizableWSS_0656_DIAG_CHECK_2());
    }

    public static Localizable localizableWSS_0656_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0656.diag.check.1");
    }

    /**
     * Check that the keystoreFile attribute is specified on SSL Coyote HTTP/1.1 Connector element in server.xml and is valid
     * 
     */
    public static String WSS_0656_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0656_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0601_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0601.diag.check.1");
    }

    /**
     * Check that the certificate referred to is valid and present in the Keystores
     * 
     */
    public static String WSS_0601_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0601_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0316_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0316.diag.cause.1");
    }

    /**
     * BinarySecurity Token's Encoding type is invalid
     * 
     */
    public static String WSS_0316_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0316_DIAG_CAUSE_1());
    }

    public static Localizable localizableBSP_3028_STR_KEYIDENTIFIER() {
        return messageFactory.getMessage("BSP3028.str.keyidentifier");
    }

    /**
     * BSP 3028 : ValueType attribute for wsse:SecurityTokenReference element MUST NOT be empty.
     * 
     */
    public static String BSP_3028_STR_KEYIDENTIFIER() {
        return localizer.localize(localizableBSP_3028_STR_KEYIDENTIFIER());
    }

    public static Localizable localizableWSS_0396_NOTCHILD_SECURITY_HEADER(Object arg0) {
        return messageFactory.getMessage("WSS0396.notchild.securityHeader", arg0);
    }

    /**
     * WSS0396: Element {0} not child SecurityHeader
     * 
     */
    public static String WSS_0396_NOTCHILD_SECURITY_HEADER(Object arg0) {
        return localizer.localize(localizableWSS_0396_NOTCHILD_SECURITY_HEADER(arg0));
    }

    public static Localizable localizableWSS_0810_METHOD_INVOCATION_FAILED() {
        return messageFactory.getMessage("WSS0810.method.invocation.failed");
    }

    /**
     * WSS0810: Method Invocation Failed
     * 
     */
    public static String WSS_0810_METHOD_INVOCATION_FAILED() {
        return localizer.localize(localizableWSS_0810_METHOD_INVOCATION_FAILED());
    }

    public static Localizable localizableWSS_0604_CANNOT_FIND_ELEMENT() {
        return messageFactory.getMessage("WSS0604.cannot.find.element");
    }

    /**
     * WSS0604: Can not find element with matching wsu:Id/Id/SAMLAssertionID
     * 
     */
    public static String WSS_0604_CANNOT_FIND_ELEMENT() {
        return localizer.localize(localizableWSS_0604_CANNOT_FIND_ELEMENT());
    }

    public static Localizable localizableWSS_0289_FAILED_GET_MESSAGE_PARTS_X_PATH() {
        return messageFactory.getMessage("WSS0289.failed.getMessageParts.XPath");
    }

    /**
     * WSS0289: failed to get Message Parts of using XPath targettype
     * 
     */
    public static String WSS_0289_FAILED_GET_MESSAGE_PARTS_X_PATH() {
        return localizer.localize(localizableWSS_0289_FAILED_GET_MESSAGE_PARTS_X_PATH());
    }

    public static Localizable localizableWSS_0753_MISSING_EMBEDDED_TOKEN() {
        return messageFactory.getMessage("WSS0753.missing.embedded.token");
    }

    /**
     * WSS0753: Missing embedded token in wsse:Embedded element
     * 
     */
    public static String WSS_0753_MISSING_EMBEDDED_TOKEN() {
        return localizer.localize(localizableWSS_0753_MISSING_EMBEDDED_TOKEN());
    }

    public static Localizable localizableWSS_0340_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0340.diag.check.1");
    }

    /**
     * Check system time and ensure it is correct
     * 
     */
    public static String WSS_0340_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0340_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0396_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0396.diag.check.1");
    }

    /**
     * Parent of the child should be SecurityHeader
     * 
     */
    public static String WSS_0396_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0396_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0232_EXPIRED_MESSAGE() {
        return messageFactory.getMessage("WSS0232.expired.Message");
    }

    /**
     * WSS0232: Message Expired
     * 
     */
    public static String WSS_0232_EXPIRED_MESSAGE() {
        return localizer.localize(localizableWSS_0232_EXPIRED_MESSAGE());
    }

    public static Localizable localizableWSS_0329_USERNAMETOKEN_EXPECTED(Object arg0) {
        return messageFactory.getMessage("WSS0329.usernametoken.expected", arg0);
    }

    /**
     * WSS0329: Expected UsernameToken Element, but found [ {0} ]
     * 
     */
    public static String WSS_0329_USERNAMETOKEN_EXPECTED(Object arg0) {
        return localizer.localize(localizableWSS_0329_USERNAMETOKEN_EXPECTED(arg0));
    }

    public static Localizable localizableWSS_0328_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0328.diag.cause.1");
    }

    /**
     * Error parsing date. 
     * 
     */
    public static String WSS_0328_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0328_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0600_ILLEGAL_TOKEN_REFERENCE() {
        return messageFactory.getMessage("WSS0600.illegal.token.reference");
    }

    /**
     * WSS0600: Key can not be located for the TokenReference (ds:KeyInfo)
     * 
     */
    public static String WSS_0600_ILLEGAL_TOKEN_REFERENCE() {
        return localizer.localize(localizableWSS_0600_ILLEGAL_TOKEN_REFERENCE());
    }

    public static Localizable localizableWSS_0329_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0329.diag.cause.1");
    }

    /**
     * Expecting UsernameToken Element
     * 
     */
    public static String WSS_0329_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0329_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0168_FAILEDTO_GENERATE_RANDOM_SYMMETRICKEY(Object arg0) {
        return messageFactory.getMessage("WSS0168.failedto.generate.random.symmetrickey", arg0);
    }

    /**
     * WSS0168: Exception [ {0} ] when trying to generate random symmetric key
     * 
     */
    public static String WSS_0168_FAILEDTO_GENERATE_RANDOM_SYMMETRICKEY(Object arg0) {
        return localizer.localize(localizableWSS_0168_FAILEDTO_GENERATE_RANDOM_SYMMETRICKEY(arg0));
    }

    public static Localizable localizableWSS_0187_PREFIX_NULL() {
        return messageFactory.getMessage("WSS0187.prefix.null");
    }

    /**
     * WSS0187: Prefix can not be null
     * 
     */
    public static String WSS_0187_PREFIX_NULL() {
        return localizer.localize(localizableWSS_0187_PREFIX_NULL());
    }

    public static Localizable localizableWSS_0353_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0353.diag.check.1");
    }

    /**
     * Check to see if setCipherValue() is called on EncryptedType
     * 
     */
    public static String WSS_0353_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0353_DIAG_CHECK_1());
    }

    public static Localizable localizableBSP_5421_SIGNATURE_METHOD() {
        return messageFactory.getMessage("BSP5421.signature.method");
    }

    /**
     * BSP5421 : Signature Method should have value of "http://www.w3.org/2000/09/xmldsig#hmac-sha1" or "http://www.w3.org/2000/09/xmldsig#rsa-sha1".
     * 
     */
    public static String BSP_5421_SIGNATURE_METHOD() {
        return localizer.localize(localizableBSP_5421_SIGNATURE_METHOD());
    }

    public static Localizable localizableWSS_0380_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0380.diag.cause.1");
    }

    /**
     * The ds:Reference would already have been set using the constructors
     * 
     */
    public static String WSS_0380_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0380_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0358_ERROR_GETTING_X_509_DATA(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS0358.error.getting.x509data", arg0, arg1);
    }

    /**
     * WSS0358: Can not get X509Data for index [{0}] due to {1}
     * 
     */
    public static String WSS_0358_ERROR_GETTING_X_509_DATA(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_0358_ERROR_GETTING_X_509_DATA(arg0, arg1));
    }

    public static Localizable localizableWSS_0183_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0183.diag.check.1");
    }

    /**
     * Value of symmetric key seems to be null. Check its value.
     * 
     */
    public static String WSS_0183_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0183_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0195_REFERENCELIST_NOT_SET() {
        return messageFactory.getMessage("WSS0195.referencelist.not.set");
    }

    /**
     * WSS0195: ReferenceListBlock not set on the calling thread
     * 
     */
    public static String WSS_0195_REFERENCELIST_NOT_SET() {
        return localizer.localize(localizableWSS_0195_REFERENCELIST_NOT_SET());
    }

    public static Localizable localizableWSS_0654_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0654.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.Name for wsu:Id
     * 
     */
    public static String WSS_0654_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0654_DIAG_CAUSE_1());
    }

    public static Localizable localizableBSP_3203_ONECREATED_TIMESTAMP() {
        return messageFactory.getMessage("BSP3203.Onecreated.Timestamp");
    }

    /**
     * BSP3203: A TIMESTAMP MUST have exactly one wsu:Created element child.
     * 
     */
    public static String BSP_3203_ONECREATED_TIMESTAMP() {
        return localizer.localize(localizableBSP_3203_ONECREATED_TIMESTAMP());
    }

    public static Localizable localizableWSS_0184_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0184.diag.check.1");
    }

    /**
     * Make sure the SecurityEnvironment factory has set the right security environment.
     * 
     */
    public static String WSS_0184_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0184_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0310_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0310.diag.check.1");
    }

    /**
     * Check that the algorithm passed to SecureRandom is valid
     * 
     */
    public static String WSS_0310_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0310_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0126_UNSUPPORTED_PRIVATEKEY_TYPE() {
        return messageFactory.getMessage("WSS0126.unsupported.privatekey.type");
    }

    /**
     * WSS0126: Unsupported PrivateKey type
     * 
     */
    public static String WSS_0126_UNSUPPORTED_PRIVATEKEY_TYPE() {
        return localizer.localize(localizableWSS_0126_UNSUPPORTED_PRIVATEKEY_TYPE());
    }

    public static Localizable localizableWSS_0425_UNABLETO_RESOLVE_XPATH() {
        return messageFactory.getMessage("WSS0425.unableto.resolve.xpath");
    }

    /**
     * WSS0425: Unable to resolve XPath
     * 
     */
    public static String WSS_0425_UNABLETO_RESOLVE_XPATH() {
        return localizer.localize(localizableWSS_0425_UNABLETO_RESOLVE_XPATH());
    }

    public static Localizable localizableWSS_0258_INVALID_REQUIREMENTS() {
        return messageFactory.getMessage("WSS0258.invalid.requirements");
    }

    /**
     * WSS0258: More Receiver requirements specified than present in the message
     * 
     */
    public static String WSS_0258_INVALID_REQUIREMENTS() {
        return localizer.localize(localizableWSS_0258_INVALID_REQUIREMENTS());
    }

    public static Localizable localizableWSS_0207_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0207.diag.cause.1");
    }

    /**
     * Operation not supported on calling object
     * 
     */
    public static String WSS_0207_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0207_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0395_CREATING_CIPHER_DATA() {
        return messageFactory.getMessage("WSS0395.creating.cipherData");
    }

    /**
     * WSS0395: Error creating CipherData
     * 
     */
    public static String WSS_0395_CREATING_CIPHER_DATA() {
        return localizer.localize(localizableWSS_0395_CREATING_CIPHER_DATA());
    }

    public static Localizable localizableWSS_0260_INVALID_DSP() {
        return messageFactory.getMessage("WSS0260.invalid.DSP");
    }

    /**
     * WSS0260: Invalid dynamic security policy returned by callback handler
     * 
     */
    public static String WSS_0260_INVALID_DSP() {
        return localizer.localize(localizableWSS_0260_INVALID_DSP());
    }

    public static Localizable localizableWSS_0393_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0393.diag.cause.1");
    }

    /**
     * The expiration time in Timestamp cannot be before current UTC time
     * 
     */
    public static String WSS_0393_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0393_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0186_CERTIFICATE_NOT_FOUND() {
        return messageFactory.getMessage("WSS0186.certificate.not.found");
    }

    /**
     * WSS0186: No key identifier was set (implicitly or explicitly) and no certificate could be found on the calling thread.
     * 
     */
    public static String WSS_0186_CERTIFICATE_NOT_FOUND() {
        return localizer.localize(localizableWSS_0186_CERTIFICATE_NOT_FOUND());
    }

    public static Localizable localizableWSS_0188_PREFIX_NULL() {
        return messageFactory.getMessage("WSS0188.prefix.null");
    }

    /**
     * WSS0188: Namespace can not be null
     * 
     */
    public static String WSS_0188_PREFIX_NULL() {
        return localizer.localize(localizableWSS_0188_PREFIX_NULL());
    }

    public static Localizable localizableWSS_0520_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0520.diag.cause.1");
    }

    /**
     * xwss:SymmetricKey is not permitted along with xwss:X509Token
     * 
     */
    public static String WSS_0520_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0520_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0702_NO_SUBJECT_KEYIDENTIFIER(Object arg0) {
        return messageFactory.getMessage("WSS0702.no.subject.keyidentifier", arg0);
    }

    /**
     * WSS0702: The X509v3 Certificate (for alias: [{0}]) does not contain an Subject Key Identifier
     * 
     */
    public static String WSS_0702_NO_SUBJECT_KEYIDENTIFIER(Object arg0) {
        return localizer.localize(localizableWSS_0702_NO_SUBJECT_KEYIDENTIFIER(arg0));
    }

    public static Localizable localizableWSS_0333_JAVAX_NET_SSL_KEY_STORE_NOTSET() {
        return messageFactory.getMessage("WSS0333.javax.net.ssl.keyStore.notset");
    }

    /**
     * WSS0333: property javax.net.ssl.keyStore, required for initializing the Security Environment  not set
     * 
     */
    public static String WSS_0333_JAVAX_NET_SSL_KEY_STORE_NOTSET() {
        return localizer.localize(localizableWSS_0333_JAVAX_NET_SSL_KEY_STORE_NOTSET());
    }

    public static Localizable localizableWSS_0385_ERROR_CREATING_TIMESTAMP(Object arg0) {
        return messageFactory.getMessage("WSS0385.error.creating.timestamp", arg0);
    }

    /**
     * WSS0385: Expected wsu:Timestamp SOAPElement, found {0}
     * 
     */
    public static String WSS_0385_ERROR_CREATING_TIMESTAMP(Object arg0) {
        return localizer.localize(localizableWSS_0385_ERROR_CREATING_TIMESTAMP(arg0));
    }

    public static Localizable localizableWSS_0196_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0196.diag.check.1");
    }

    /**
     * Check that SetSecurityEnvironmentFilter processed the message before 
     * 
     */
    public static String WSS_0196_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0196_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0265_ERROR_PRIMARY_POLICY() {
        return messageFactory.getMessage("WSS0265.error.primary.policy");
    }

    /**
     * WSS0265: Primary Policy Violation occured
     * 
     */
    public static String WSS_0265_ERROR_PRIMARY_POLICY() {
        return localizer.localize(localizableWSS_0265_ERROR_PRIMARY_POLICY());
    }

    public static Localizable localizableBSP_5426_ENCRYPTEDKEYINFO(Object arg0) {
        return messageFactory.getMessage("BSP5426.encryptedkeyinfo", arg0);
    }

    /**
     * BSP 5426 : KeyInfo element under EncryptedKey or EncryptedData with ID {0} MUST contain SecurityTokenReference child element.
     * 
     */
    public static String BSP_5426_ENCRYPTEDKEYINFO(Object arg0) {
        return localizer.localize(localizableBSP_5426_ENCRYPTEDKEYINFO(arg0));
    }

    public static Localizable localizableWSS_0702_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0702.diag.cause.1");
    }

    /**
     * The X509v3 Certificate for the given alias does not contain a subject key identifier
     * 
     */
    public static String WSS_0702_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0702_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0719_ERROR_GETTING_LONG_VALUE() {
        return messageFactory.getMessage("WSS0719.error.getting.longValue");
    }

    /**
     * WSS0719: Error getting long value
     * 
     */
    public static String WSS_0719_ERROR_GETTING_LONG_VALUE() {
        return localizer.localize(localizableWSS_0719_ERROR_GETTING_LONG_VALUE());
    }

    public static Localizable localizableWSS_0323_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0323.diag.check.1");
    }

    /**
     * Check the key used to sign
     * 
     */
    public static String WSS_0323_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0323_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0758_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0758.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.Name 
     * 
     */
    public static String WSS_0758_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0758_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0350_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0350.diag.cause.1");
    }

    /**
     * Error creating/updating CipherData SOAPElement (in EncryptedKeyHeaderBlock)
     * 
     */
    public static String WSS_0350_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0350_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0129_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0129.diag.cause.1");
    }

    /**
     * Malformed message ds:Signature element missing from the wsse:Security header block
     * 
     */
    public static String WSS_0129_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0129_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0401_SAML_INCORRECT_INPUT() {
        return messageFactory.getMessage("WSS0401.saml.incorrect.input");
    }

    /**
     * WSS0401: Incorrect Input
     * 
     */
    public static String WSS_0401_SAML_INCORRECT_INPUT() {
        return localizer.localize(localizableWSS_0401_SAML_INCORRECT_INPUT());
    }

    public static Localizable localizableWSS_0379_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0379.diag.check.1");
    }

    /**
     * Check that a valid SOAPElement as per spec. is passed to SecurityTokenReference()
     * 
     */
    public static String WSS_0379_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0379_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0139_UNABLETO_FIND_MATCHING_PRIVATEKEY(Object arg0) {
        return messageFactory.getMessage("WSS0139.unableto.find.matching.privatekey", arg0);
    }

    /**
     * WSS0139: Exception [ {0} ] while trying to find matching PrivateKey
     * 
     */
    public static String WSS_0139_UNABLETO_FIND_MATCHING_PRIVATEKEY(Object arg0) {
        return localizer.localize(localizableWSS_0139_UNABLETO_FIND_MATCHING_PRIVATEKEY(arg0));
    }

    public static Localizable localizableWSS_0351_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0351.diag.cause.1");
    }

    /**
     * Error creating EncryptionMethod SOAPElement
     * 
     */
    public static String WSS_0351_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0351_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0354_ERROR_INITIALIZING_ENCRYPTED_TYPE(Object arg0) {
        return messageFactory.getMessage("WSS0354.error.initializing.encryptedType", arg0);
    }

    /**
     * WSS0354: Error initializing EncryptedType due to {0}
     * 
     */
    public static String WSS_0354_ERROR_INITIALIZING_ENCRYPTED_TYPE(Object arg0) {
        return localizer.localize(localizableWSS_0354_ERROR_INITIALIZING_ENCRYPTED_TYPE(arg0));
    }

    public static Localizable localizableWSS_0405_SAML_LIST_ADD_ERROR() {
        return messageFactory.getMessage("WSS0405.saml.list.add.error");
    }

    /**
     * WSS0405: Failed to add an object to the list
     * 
     */
    public static String WSS_0405_SAML_LIST_ADD_ERROR() {
        return localizer.localize(localizableWSS_0405_SAML_LIST_ADD_ERROR());
    }

    public static Localizable localizableWSS_0403_SAML_INVALID_ACTION() {
        return messageFactory.getMessage("WSS0403.saml.invalid.action");
    }

    /**
     * WSS0403: The action is invalid in its specified namespace
     * 
     */
    public static String WSS_0403_SAML_INVALID_ACTION() {
        return localizer.localize(localizableWSS_0403_SAML_INVALID_ACTION());
    }

    public static Localizable localizableWSS_0423_SAML_SUBJECT_NAMEID_VALIDATION_FAILED() {
        return messageFactory.getMessage("WSS0423.saml.subject.nameid.validation.failed");
    }

    /**
     * WSS0423: Subject NameIdentifier validation failed for SAML Assertion
     * 
     */
    public static String WSS_0423_SAML_SUBJECT_NAMEID_VALIDATION_FAILED() {
        return localizer.localize(localizableWSS_0423_SAML_SUBJECT_NAMEID_VALIDATION_FAILED());
    }

    public static Localizable localizableWSS_0360_ERROR_CREATING_RLHB(Object arg0) {
        return messageFactory.getMessage("WSS0360.error.creating.rlhb", arg0);
    }

    /**
     * WSS0360: Can not create ReferenceListHeaderBlock due to {0}
     * 
     */
    public static String WSS_0360_ERROR_CREATING_RLHB(Object arg0) {
        return localizer.localize(localizableWSS_0360_ERROR_CREATING_RLHB(arg0));
    }

    public static Localizable localizableWSS_0181_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0181.diag.cause.1");
    }

    /**
     * Subject not authorized; validation failed
     * 
     */
    public static String WSS_0181_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0181_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0365_ERROR_CREATING_NAMESPACE_NODE(Object arg0) {
        return messageFactory.getMessage("WSS0365.error.creating.namespaceNode", arg0);
    }

    /**
     * WSS0365: Error creating namespace node due to {0}
     * 
     */
    public static String WSS_0365_ERROR_CREATING_NAMESPACE_NODE(Object arg0) {
        return localizer.localize(localizableWSS_0365_ERROR_CREATING_NAMESPACE_NODE(arg0));
    }

    public static Localizable localizableWSS_0336_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0336.diag.check.1");
    }

    /**
     * Check public key retrieved should not be null
     * 
     */
    public static String WSS_0336_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0336_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0363_DIAG_CAUSE_3() {
        return messageFactory.getMessage("WSS0363.diag.cause.3");
    }

    /**
     * Error setting URI attribute on javax.xml.soap.SOAPElement for xenc:DataReference
     * 
     */
    public static String WSS_0363_DIAG_CAUSE_3() {
        return localizer.localize(localizableWSS_0363_DIAG_CAUSE_3());
    }

    public static Localizable localizableWSS_0363_DIAG_CAUSE_2() {
        return messageFactory.getMessage("WSS0363.diag.cause.2");
    }

    /**
     * Error adding xenc:DataReference (SOAPElement) as child element of xenc:DataReference (SOAPElement)
     * 
     */
    public static String WSS_0363_DIAG_CAUSE_2() {
        return localizer.localize(localizableWSS_0363_DIAG_CAUSE_2());
    }

    public static Localizable localizableWSS_0363_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0363.diag.cause.1");
    }

    /**
     * Error creating javax.xml.soap.SOAPElement for xenc:DataReference
     * 
     */
    public static String WSS_0363_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0363_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0340_CREATED_AHEAD_OF_CURRENT() {
        return messageFactory.getMessage("WSS0340.created.ahead.of.current");
    }

    /**
     * WSS0340: The creation time is ahead of the current time.
     * 
     */
    public static String WSS_0340_CREATED_AHEAD_OF_CURRENT() {
        return localizer.localize(localizableWSS_0340_CREATED_AHEAD_OF_CURRENT());
    }

    public static Localizable localizableWSS_0807_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0807.diag.cause.1");
    }

    /**
     * SOAPBody element is not found in the message
     * 
     */
    public static String WSS_0807_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0807_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0380_ERROR_SETTING_REFERENCE() {
        return messageFactory.getMessage("WSS0380.error.setting.reference");
    }

    /**
     * WSS0380: Can not set ds:Reference on SecurityTokenReference
     * 
     */
    public static String WSS_0380_ERROR_SETTING_REFERENCE() {
        return localizer.localize(localizableWSS_0380_ERROR_SETTING_REFERENCE());
    }

    public static Localizable localizableWSS_0364_DIAG_CAUSE_1() {
        return messageFactory.getMessage("WSS0364.diag.cause.1");
    }

    /**
     * An Internal XPathAPI transformation error occurred 
     * 
     */
    public static String WSS_0364_DIAG_CAUSE_1() {
        return localizer.localize(localizableWSS_0364_DIAG_CAUSE_1());
    }

    public static Localizable localizableWSS_0409_SAML_MISSING_STATEMENT() {
        return messageFactory.getMessage("WSS0409.saml.missing.statement");
    }

    /**
     * WSS0409: Assertion has no statements
     * 
     */
    public static String WSS_0409_SAML_MISSING_STATEMENT() {
        return localizer.localize(localizableWSS_0409_SAML_MISSING_STATEMENT());
    }

    public static Localizable localizableWSS_0519_DIAG_CHECK_1() {
        return messageFactory.getMessage("WSS0519.diag.check.1");
    }

    /**
     * Check that the configuration file is consistent with the security configuration schema
     * 
     */
    public static String WSS_0519_DIAG_CHECK_1() {
        return localizer.localize(localizableWSS_0519_DIAG_CHECK_1());
    }

    public static Localizable localizableWSS_0294_FAILED_X_509_DATA() {
        return messageFactory.getMessage("WSS0294.failed.X509Data");
    }

    /**
     * WSS0294: Exception occured while trying to add X509 Certificate into X509 Data
     * 
     */
    public static String WSS_0294_FAILED_X_509_DATA() {
        return localizer.localize(localizableWSS_0294_FAILED_X_509_DATA());
    }

    public static Localizable localizableWSS_0309_COULDNOT_DECODE_BASE_64_NONCE(Object arg0) {
        return messageFactory.getMessage("WSS0309.couldnot.decode.base64.nonce", arg0);
    }

    /**
     * WSS0309: Exception [ {0} ] while decoding base64 nonce
     * 
     */
    public static String WSS_0309_COULDNOT_DECODE_BASE_64_NONCE(Object arg0) {
        return localizer.localize(localizableWSS_0309_COULDNOT_DECODE_BASE_64_NONCE(arg0));
    }

}
