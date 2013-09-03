
package com.sun.xml.wss.logging.impl.dsig;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.dsig.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1349_ERROR_HANDLING_X_509_BINDING() {
        return messageFactory.getMessage("WSS1349.error.handlingX509Binding");
    }

    /**
     * WSS1349: Error occured handling X509 Binding
     * 
     */
    public static String WSS_1349_ERROR_HANDLING_X_509_BINDING() {
        return localizer.localize(localizableWSS_1349_ERROR_HANDLING_X_509_BINDING());
    }

    public static Localizable localizableWSS_1359_INVALID_VALUETYPE_NON_SC_TTOKEN() {
        return messageFactory.getMessage("WSS1359.invalid.valuetype.nonSCTtoken");
    }

    /**
     * WSS1359: Incorrect ValueType specified for a Non SCT Token
     * 
     */
    public static String WSS_1359_INVALID_VALUETYPE_NON_SC_TTOKEN() {
        return localizer.localize(localizableWSS_1359_INVALID_VALUETYPE_NON_SC_TTOKEN());
    }

    public static Localizable localizableWSS_1369_UNABLE_GET_SIGNATURE_TARGET_BY_URI() {
        return messageFactory.getMessage("WSS1369.unable.get.signatureTarget.by.URI");
    }

    /**
     * WSS1369: Signature Target with URI is not in the message
     * 
     */
    public static String WSS_1369_UNABLE_GET_SIGNATURE_TARGET_BY_URI() {
        return localizer.localize(localizableWSS_1369_UNABLE_GET_SIGNATURE_TARGET_BY_URI());
    }

    public static Localizable localizableWSS_1378_UNABLETO_REFER_ISSUE_TOKEN() {
        return messageFactory.getMessage("WSS1378.unableto.refer.IssueToken");
    }

    /**
     * WSS1378: Cannot determine how to reference the Issued Token in the Message
     * 
     */
    public static String WSS_1378_UNABLETO_REFER_ISSUE_TOKEN() {
        return localizer.localize(localizableWSS_1378_UNABLETO_REFER_ISSUE_TOKEN());
    }

    public static Localizable localizableWSS_1306_UNSUPPORTED_KEY_IDENTIFIER_REFERENCE_TYPE() {
        return messageFactory.getMessage("WSS1306.unsupported.KeyIdentifier.Reference.Type");
    }

    /**
     * WSS1306:unsupported KeyIdentifier Reference Type encountered
     * 
     */
    public static String WSS_1306_UNSUPPORTED_KEY_IDENTIFIER_REFERENCE_TYPE() {
        return localizer.localize(localizableWSS_1306_UNSUPPORTED_KEY_IDENTIFIER_REFERENCE_TYPE());
    }

    public static Localizable localizableWSS_1365_UNABLETO_LOCATE_SECURE_CONVERSATION_SESSION() {
        return messageFactory.getMessage("WSS1365.unableto.locate.SecureConversation.Session");
    }

    /**
     * WSS1365: Could not locate SecureConversation session for Id
     * 
     */
    public static String WSS_1365_UNABLETO_LOCATE_SECURE_CONVERSATION_SESSION() {
        return localizer.localize(localizableWSS_1365_UNABLETO_LOCATE_SECURE_CONVERSATION_SESSION());
    }

    public static Localizable localizableWSS_1310_SAML_SIGNATURE_INVALID() {
        return messageFactory.getMessage("WSS1310.saml.signature.invalid");
    }

    /**
     * WSS1310: The signature in the SAML Assertion is invalid
     * 
     */
    public static String WSS_1310_SAML_SIGNATURE_INVALID() {
        return localizer.localize(localizableWSS_1310_SAML_SIGNATURE_INVALID());
    }

    public static Localizable localizableWSS_1364_UNABLETO_VALIDATE_CERTIFICATE() {
        return messageFactory.getMessage("WSS1364.unableto.validate.certificate");
    }

    /**
     * WSS1364: Unable to validate certificate
     * 
     */
    public static String WSS_1364_UNABLETO_VALIDATE_CERTIFICATE() {
        return localizer.localize(localizableWSS_1364_UNABLETO_VALIDATE_CERTIFICATE());
    }

    public static Localizable localizableWSS_1343_NULL_ISSUED_TOKEN() {
        return messageFactory.getMessage("WSS1343.null.IssuedToken");
    }

    /**
     * WSS1343: Issued Token to be inserted into the Message was Null
     * 
     */
    public static String WSS_1343_NULL_ISSUED_TOKEN() {
        return localizer.localize(localizableWSS_1343_NULL_ISSUED_TOKEN());
    }

    public static Localizable localizableWSS_1372_NO_ATTACHMENT_FOUND() {
        return messageFactory.getMessage("WSS1372.no.attachmentFound");
    }

    /**
     * WSS1372: No attachment present in the message
     * 
     */
    public static String WSS_1372_NO_ATTACHMENT_FOUND() {
        return localizer.localize(localizableWSS_1372_NO_ATTACHMENT_FOUND());
    }

    public static Localizable localizableWSS_1358_UNABLETO_LOCATE_SCT_TOKEN() {
        return messageFactory.getMessage("WSS1358.unableto.locate.SCTToken");
    }

    /**
     * WSS1358: SCTToken with given Id not found
     * 
     */
    public static String WSS_1358_UNABLETO_LOCATE_SCT_TOKEN() {
        return localizer.localize(localizableWSS_1358_UNABLETO_LOCATE_SCT_TOKEN());
    }

    public static Localizable localizableWSS_1312_UNSUPPORTED_KEYINFO() {
        return messageFactory.getMessage("WSS1312.unsupported.keyinfo");
    }

    /**
     * WSS1312: Unsupported keyinfo block encountered
     * 
     */
    public static String WSS_1312_UNSUPPORTED_KEYINFO() {
        return localizer.localize(localizableWSS_1312_UNSUPPORTED_KEYINFO());
    }

    public static Localizable localizableWSS_1341_ILLEGAL_UNMATCHED_TYPE_URI() {
        return messageFactory.getMessage("WSS1341.illegal.unmatched.Type.Uri");
    }

    /**
     * WSS1341: Receiver requirement for SignatureTarget having type and value uri is not met
     * 
     */
    public static String WSS_1341_ILLEGAL_UNMATCHED_TYPE_URI() {
        return localizer.localize(localizableWSS_1341_ILLEGAL_UNMATCHED_TYPE_URI());
    }

    public static Localizable localizableWSS_1366_UNABLE_GENERATE_SYMMETRIC_KEY_DKT() {
        return messageFactory.getMessage("WSS1366.unable.generateSymmetricKey.DKT");
    }

    /**
     * WSS1366: Exception occured while trying to generate Symmetric key from Derived Key Token
     * 
     */
    public static String WSS_1366_UNABLE_GENERATE_SYMMETRIC_KEY_DKT() {
        return localizer.localize(localizableWSS_1366_UNABLE_GENERATE_SYMMETRIC_KEY_DKT());
    }

    public static Localizable localizableWSS_1329_NULL_PRIVATEKEYBINDING_SAML_POLICY() {
        return messageFactory.getMessage("WSS1329.null.privatekeybinding.SAMLPolicy");
    }

    /**
     * WSS1329: PrivateKey binding not set for SAML Policy by CallbackHandler
     * 
     */
    public static String WSS_1329_NULL_PRIVATEKEYBINDING_SAML_POLICY() {
        return localizer.localize(localizableWSS_1329_NULL_PRIVATEKEYBINDING_SAML_POLICY());
    }

    public static Localizable localizableWSS_1315_SIGNATURE_VERIFICATION_FAILED() {
        return messageFactory.getMessage("WSS1315.signature.verification.failed");
    }

    /**
     * WSS1315: Signature Verification Failed
     * 
     */
    public static String WSS_1315_SIGNATURE_VERIFICATION_FAILED() {
        return localizer.localize(localizableWSS_1315_SIGNATURE_VERIFICATION_FAILED());
    }

    public static Localizable localizableWSS_1317_KEYINFO_NULL() {
        return messageFactory.getMessage("WSS1317.keyinfo.null");
    }

    /**
     * WSS1317: KeyInfo object in SignatureElement is null
     * 
     */
    public static String WSS_1317_KEYINFO_NULL() {
        return localizer.localize(localizableWSS_1317_KEYINFO_NULL());
    }

    public static Localizable localizableWSS_1361_UNSUPPORTED_KEY_NAME_SAML() {
        return messageFactory.getMessage("WSS1361.unsupported.KeyName.SAML");
    }

    /**
     * WSS1361: Unsupported KeyName under SAML SubjectConfirmation
     * 
     */
    public static String WSS_1361_UNSUPPORTED_KEY_NAME_SAML() {
        return localizer.localize(localizableWSS_1361_UNSUPPORTED_KEY_NAME_SAML());
    }

    public static Localizable localizableWSS_1322_STR_TRANSFORM() {
        return messageFactory.getMessage("WSS1322.str_transform");
    }

    /**
     * WSS1322: Error occurred when performing STR-TRANSFORM
     * 
     */
    public static String WSS_1322_STR_TRANSFORM() {
        return localizer.localize(localizableWSS_1322_STR_TRANSFORM());
    }

    public static Localizable localizableWSS_1316_SIGN_FAILED() {
        return messageFactory.getMessage("WSS1316.sign.failed");
    }

    /**
     * WSS1316: Sign operation failed.
     * 
     */
    public static String WSS_1316_SIGN_FAILED() {
        return localizer.localize(localizableWSS_1316_SIGN_FAILED());
    }

    public static Localizable localizableWSS_1339_INVALID_RECEIVER_REQUIREMENTS() {
        return messageFactory.getMessage("WSS1339.invalid.ReceiverRequirements");
    }

    /**
     * WSS1339: Receiver requirement for SignatureTarget is not met
     * 
     */
    public static String WSS_1339_INVALID_RECEIVER_REQUIREMENTS() {
        return localizer.localize(localizableWSS_1339_INVALID_RECEIVER_REQUIREMENTS());
    }

    public static Localizable localizableWSS_1340_ILLEGAL_UNMATCHED_NOOF_TARGETS() {
        return messageFactory.getMessage("WSS1340.illegal.unmatched.NoofTargets");
    }

    /**
     * WSS1340: Number of Targets in the message dont match number of Targets in receiver requirements
     * 
     */
    public static String WSS_1340_ILLEGAL_UNMATCHED_NOOF_TARGETS() {
        return localizer.localize(localizableWSS_1340_ILLEGAL_UNMATCHED_NOOF_TARGETS());
    }

    public static Localizable localizableWSS_1374_FAILEDTO_PRINT_DOCUMENT() {
        return messageFactory.getMessage("WSS1374.failedto.print.document");
    }

    /**
     * Failed to print document
     * 
     */
    public static String WSS_1374_FAILEDTO_PRINT_DOCUMENT() {
        return localizer.localize(localizableWSS_1374_FAILEDTO_PRINT_DOCUMENT());
    }

    public static Localizable localizableWSS_1346_ERROR_PREPARING_SYMMETRICKEY_SIGNATURE() {
        return messageFactory.getMessage("WSS1346.error.preparing.symmetrickey.signature");
    }

    /**
     * WSS1346. Error preparing Symmetric key for Signature
     * 
     */
    public static String WSS_1346_ERROR_PREPARING_SYMMETRICKEY_SIGNATURE() {
        return localizer.localize(localizableWSS_1346_ERROR_PREPARING_SYMMETRICKEY_SIGNATURE());
    }

    public static Localizable localizableWSS_1325_DSIG_EXTERNALTARGET(Object arg0) {
        return messageFactory.getMessage("WSS1325.dsig.externaltarget", arg0);
    }

    /**
     * WSS1325: Unable to resolve external target {0}
     * 
     */
    public static String WSS_1325_DSIG_EXTERNALTARGET(Object arg0) {
        return localizer.localize(localizableWSS_1325_DSIG_EXTERNALTARGET(arg0));
    }

    public static Localizable localizableWSS_1326_UNSUPPORTED_USERNAMETOKEN_KEYBINDING() {
        return messageFactory.getMessage("WSS1326.unsupported.usernametoken.keybinding");
    }

    /**
     * WSS1326: UsernameToken as KeyBinding for SignaturePolicy is Not Yet Supported
     * 
     */
    public static String WSS_1326_UNSUPPORTED_USERNAMETOKEN_KEYBINDING() {
        return localizer.localize(localizableWSS_1326_UNSUPPORTED_USERNAMETOKEN_KEYBINDING());
    }

    public static Localizable localizableWSS_1376_FAILED_VERIFY_POLICY_NO_ELEMENTBY_ID() {
        return messageFactory.getMessage("WSS1376.failed.verify.policy.noElementbyID");
    }

    /**
     * WSS1376: Policy verification for Signature failed: Element with Id: not found in message
     * 
     */
    public static String WSS_1376_FAILED_VERIFY_POLICY_NO_ELEMENTBY_ID() {
        return localizer.localize(localizableWSS_1376_FAILED_VERIFY_POLICY_NO_ELEMENTBY_ID());
    }

    public static Localizable localizableWSS_1356_VIOLATION_BSP_R_5204() {
        return messageFactory.getMessage("WSS1356.Violation.BSP.R5204");
    }

    /**
     * WSS1356: Violation of BSP R5204: When a SECURITY_TOKEN_REFERENCE uses a Direct Reference to an INTERNAL_SECURITY_TOKEN, it MUST use a Shorthand XPointer Reference
     * 
     */
    public static String WSS_1356_VIOLATION_BSP_R_5204() {
        return localizer.localize(localizableWSS_1356_VIOLATION_BSP_R_5204());
    }

    public static Localizable localizableWSS_1348_ILLEGAL_THUMBPRINT_X_509_V_1() {
        return messageFactory.getMessage("WSS1348.illegal.thumbprint.x509v1");
    }

    /**
     * WSS1348: Thumbprint reference Type is not allowed for X509v1 Certificates
     * 
     */
    public static String WSS_1348_ILLEGAL_THUMBPRINT_X_509_V_1() {
        return localizer.localize(localizableWSS_1348_ILLEGAL_THUMBPRINT_X_509_V_1());
    }

    public static Localizable localizableWSS_1363_INVALID_SECURITY_TOKEN() {
        return messageFactory.getMessage("WSS1363.invalid.security.token");
    }

    /**
     * WSS1363: A Invalid security token was provided
     * 
     */
    public static String WSS_1363_INVALID_SECURITY_TOKEN() {
        return localizer.localize(localizableWSS_1363_INVALID_SECURITY_TOKEN());
    }

    public static Localizable localizableWSS_1367_ILLEGAL_XPATH() {
        return messageFactory.getMessage("WSS1367.illegal.xpath");
    }

    /**
     * WSS1367: XPATH parameters cannot be null
     * 
     */
    public static String WSS_1367_ILLEGAL_XPATH() {
        return localizer.localize(localizableWSS_1367_ILLEGAL_XPATH());
    }

    public static Localizable localizableWSS_1354_NULL_KEY_VALUE() {
        return messageFactory.getMessage("WSS1354.null.keyValue");
    }

    /**
     * No KeyValue element found!
     * 
     */
    public static String WSS_1354_NULL_KEY_VALUE() {
        return localizer.localize(localizableWSS_1354_NULL_KEY_VALUE());
    }

    public static Localizable localizableWSS_1362_EXCEPTION_WS_TRUST_CREATING_BINARY_SECRET() {
        return messageFactory.getMessage("WSS1362.exception.WSTrust.CreatingBinarySecret");
    }

    /**
     * WSS1362: Exception occured while trying to create BinarySecret
     * 
     */
    public static String WSS_1362_EXCEPTION_WS_TRUST_CREATING_BINARY_SECRET() {
        return localizer.localize(localizableWSS_1362_EXCEPTION_WS_TRUST_CREATING_BINARY_SECRET());
    }

    public static Localizable localizableWSS_1351_EXCEPTION_KEYSELECTOR_PUBLICKEY() {
        return messageFactory.getMessage("WSS1351.exception.keyselector.publickey");
    }

    /**
     * WSS1351: Exception occured in getting public key
     * 
     */
    public static String WSS_1351_EXCEPTION_KEYSELECTOR_PUBLICKEY() {
        return localizer.localize(localizableWSS_1351_EXCEPTION_KEYSELECTOR_PUBLICKEY());
    }

    public static Localizable localizableWSS_1373_FAILEDTO_RESOLVE_ELEMENTBY_ID() {
        return messageFactory.getMessage("WSS1373.failedto.resolve.elementbyID");
    }

    /**
     * Failed to resolve Element by URI ID
     * 
     */
    public static String WSS_1373_FAILEDTO_RESOLVE_ELEMENTBY_ID() {
        return localizer.localize(localizableWSS_1373_FAILEDTO_RESOLVE_ELEMENTBY_ID());
    }

    public static Localizable localizableWSS_1311_SAML_TIMESTAMP_INVALID() {
        return messageFactory.getMessage("WSS1311.saml.timestamp.invalid");
    }

    /**
     * WSS1311: Condition (notBefore, notOnOrAfter) validation failed for SAML assertion
     * 
     */
    public static String WSS_1311_SAML_TIMESTAMP_INVALID() {
        return localizer.localize(localizableWSS_1311_SAML_TIMESTAMP_INVALID());
    }

    public static Localizable localizableWSS_1319_ACO_TRANSFORM_ERROR() {
        return messageFactory.getMessage("WSS1319.aco.transform.error");
    }

    /**
     * WSS1319: Error occurred in AttachmentComplete Transform
     * 
     */
    public static String WSS_1319_ACO_TRANSFORM_ERROR() {
        return localizer.localize(localizableWSS_1319_ACO_TRANSFORM_ERROR());
    }

    public static Localizable localizableWSS_1334_ERROR_CREATING_ENCRYPTEDKEY() {
        return messageFactory.getMessage("WSS1334.error.creating.encryptedkey");
    }

    /**
     * WSS1334: Exception while creating encryptedkey
     * 
     */
    public static String WSS_1334_ERROR_CREATING_ENCRYPTEDKEY() {
        return localizer.localize(localizableWSS_1334_ERROR_CREATING_ENCRYPTEDKEY());
    }

    public static Localizable localizableWSS_1347_NULL_SECURE_CONVERSATION_TOKEN() {
        return messageFactory.getMessage("WSS1347.null.SecureConversationToken");
    }

    /**
     * WSS1347: SecureConversation Token not Found
     * 
     */
    public static String WSS_1347_NULL_SECURE_CONVERSATION_TOKEN() {
        return localizer.localize(localizableWSS_1347_NULL_SECURE_CONVERSATION_TOKEN());
    }

    public static Localizable localizableWSS_1352_EXCEPTION_KEYSELECTOR() {
        return messageFactory.getMessage("WSS1352.exception.keyselector");
    }

    /**
     * WSS1352: Exception occured in Key selection
     * 
     */
    public static String WSS_1352_EXCEPTION_KEYSELECTOR() {
        return localizer.localize(localizableWSS_1352_EXCEPTION_KEYSELECTOR());
    }

    public static Localizable localizableWSS_1305_UN_SUPPORTED_SECURITY_TOKEN() {
        return messageFactory.getMessage("WSS1305.UnSupported.security.token");
    }

    /**
     * WSS1305: Unsupported security token was provided
     * 
     */
    public static String WSS_1305_UN_SUPPORTED_SECURITY_TOKEN() {
        return localizer.localize(localizableWSS_1305_UN_SUPPORTED_SECURITY_TOKEN());
    }

    public static Localizable localizableWSS_1301_INVALID_DIGEST_ALGO(Object arg0) {
        return messageFactory.getMessage("WSS1301.invalid.digest.algo", arg0);
    }

    /**
     * WSS1301: Invalid Digest Algorithm {0} specified 
     * 
     */
    public static String WSS_1301_INVALID_DIGEST_ALGO(Object arg0) {
        return localizer.localize(localizableWSS_1301_INVALID_DIGEST_ALGO(arg0));
    }

    public static Localizable localizableWSS_1303_CID_ERROR() {
        return messageFactory.getMessage("WSS1303.cid_error");
    }

    /**
     * WSS1303: Content-Id is empty or is not wellformed
     * 
     */
    public static String WSS_1303_CID_ERROR() {
        return localizer.localize(localizableWSS_1303_CID_ERROR());
    }

    public static Localizable localizableWSS_1313_ILLEGAL_KEY_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS1313.illegal.key.value", arg0);
    }

    /**
     * WSS1313: Key can not be located for the KeyValue (ds:KeyInfo) due to {0}
     * 
     */
    public static String WSS_1313_ILLEGAL_KEY_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_1313_ILLEGAL_KEY_VALUE(arg0));
    }

    public static Localizable localizableWSS_1320_STR_UN_TRANSFORM_ERROR() {
        return messageFactory.getMessage("WSS1320.str_un.transform.error");
    }

    /**
     * WSS1320: Error occurred when unmarshaling transformation parameters.
     * 
     */
    public static String WSS_1320_STR_UN_TRANSFORM_ERROR() {
        return localizer.localize(localizableWSS_1320_STR_UN_TRANSFORM_ERROR());
    }

    public static Localizable localizableWSS_1307_UNSUPPORTED_DIRECTREF_MECHANISM(Object arg0) {
        return messageFactory.getMessage("WSS1307.unsupported.directref.mechanism", arg0);
    }

    /**
     * WSS1307: Unsupported DirectReference mechanism {0}
     * 
     */
    public static String WSS_1307_UNSUPPORTED_DIRECTREF_MECHANISM(Object arg0) {
        return localizer.localize(localizableWSS_1307_UNSUPPORTED_DIRECTREF_MECHANISM(arg0));
    }

    public static Localizable localizableWSS_1357_UNABLETO_LOCATE_TOKEN() {
        return messageFactory.getMessage("WSS1357.unableto.locate.Token");
    }

    /**
     * WSS1357: Token with given Id not found
     * 
     */
    public static String WSS_1357_UNABLETO_LOCATE_TOKEN() {
        return localizer.localize(localizableWSS_1357_UNABLETO_LOCATE_TOKEN());
    }

    public static Localizable localizableWSS_1327_UNSUPPORTED_ASYMMETRICBINDING_DERIVEDKEY_X_509_TOKEN() {
        return messageFactory.getMessage("WSS1327.unsupported.asymmetricbinding.derivedkey.x509token");
    }

    /**
     * WSS1327: Asymmetric Binding with DerivedKeys under X509Token Policy Not Yet Supported
     * 
     */
    public static String WSS_1327_UNSUPPORTED_ASYMMETRICBINDING_DERIVEDKEY_X_509_TOKEN() {
        return localizer.localize(localizableWSS_1327_UNSUPPORTED_ASYMMETRICBINDING_DERIVEDKEY_X_509_TOKEN());
    }

    public static Localizable localizableWSS_1335_UNSUPPORTED_KEYBINDING_SIGNATUREPOLICY() {
        return messageFactory.getMessage("WSS1335.unsupported.keybinding.signaturepolicy");
    }

    /**
     * WSS1335: Unsupported Key Binding for SignaturePolicy
     * 
     */
    public static String WSS_1335_UNSUPPORTED_KEYBINDING_SIGNATUREPOLICY() {
        return localizer.localize(localizableWSS_1335_UNSUPPORTED_KEYBINDING_SIGNATUREPOLICY());
    }

    public static Localizable localizableWSS_1318_AC_TRANSFORM_ERROR() {
        return messageFactory.getMessage("WSS1318.ac.transform.error");
    }

    /**
     * WSS1318: Error occurred in AttachmentContentOnly Transform
     * 
     */
    public static String WSS_1318_AC_TRANSFORM_ERROR() {
        return localizer.localize(localizableWSS_1318_AC_TRANSFORM_ERROR());
    }

    public static Localizable localizableWSS_1308_UNSUPPORTED_REFERENCE_MECHANISM() {
        return messageFactory.getMessage("WSS1308.unsupported.reference.mechanism");
    }

    /**
     * WSS1308: Unsupported Reference mechanism
     * 
     */
    public static String WSS_1308_UNSUPPORTED_REFERENCE_MECHANISM() {
        return localizer.localize(localizableWSS_1308_UNSUPPORTED_REFERENCE_MECHANISM());
    }

    public static Localizable localizableWSS_1304_FC_SECURITY_TOKEN_UNAVAILABLE() {
        return messageFactory.getMessage("WSS1304.FC_SECURITY_TOKEN_UNAVAILABLE");
    }

    /**
     * WSS1304: Referenced Security Token could not be retrieved
     * 
     */
    public static String WSS_1304_FC_SECURITY_TOKEN_UNAVAILABLE() {
        return localizer.localize(localizableWSS_1304_FC_SECURITY_TOKEN_UNAVAILABLE());
    }

    public static Localizable localizableWSS_1332_NULL_SAML_ASSERTION_SAML_ASSERTION_ID() {
        return messageFactory.getMessage("WSS1332.null.SAMLAssertion.SAMLAssertionId");
    }

    /**
     * WSS1332: None of SAML Assertion, SAML Assertion Id information was set into the Policy by the CallbackHandler
     * 
     */
    public static String WSS_1332_NULL_SAML_ASSERTION_SAML_ASSERTION_ID() {
        return localizer.localize(localizableWSS_1332_NULL_SAML_ASSERTION_SAML_ASSERTION_ID());
    }

    public static Localizable localizableWSS_1302_REFLIST_ERROR() {
        return messageFactory.getMessage("WSS1302.reflist_error");
    }

    /**
     * WSS1302: Error occurred while creating reference list object for Receiver requirement 
     * 
     */
    public static String WSS_1302_REFLIST_ERROR() {
        return localizer.localize(localizableWSS_1302_REFLIST_ERROR());
    }

    public static Localizable localizableWSS_1371_FAILED_RESOLVE_X_PATH() {
        return messageFactory.getMessage("WSS1371.failed.resolve.XPath");
    }

    /**
     * WSS1371: Error occured while trying to resolve XPath expression
     * 
     */
    public static String WSS_1371_FAILED_RESOLVE_X_PATH() {
        return localizer.localize(localizableWSS_1371_FAILED_RESOLVE_X_PATH());
    }

    public static Localizable localizableWSS_1368_ILLEGAL_STR_CANONCALIZATION() {
        return messageFactory.getMessage("WSS1368.illegal.str.canoncalization");
    }

    /**
     * WSS1368: STR Transform must have a canonicalization method specified
     * 
     */
    public static String WSS_1368_ILLEGAL_STR_CANONCALIZATION() {
        return localizer.localize(localizableWSS_1368_ILLEGAL_STR_CANONCALIZATION());
    }

    public static Localizable localizableWSS_1355_UNABLETO_RESOLVE_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS1355.unableto.resolve.SAMLAssertion");
    }

    /**
     * WSS1355: Error occurred while trying to resolve SAML assertion
     * 
     */
    public static String WSS_1355_UNABLETO_RESOLVE_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_1355_UNABLETO_RESOLVE_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_1342_ILLEGAL_UNMATCHED_TRANSFORMS() {
        return messageFactory.getMessage("WSS1342.illegal.unmatched.transforms");
    }

    /**
     * WSS1342: Receiver Requirements for the transforms are not met
     * 
     */
    public static String WSS_1342_ILLEGAL_UNMATCHED_TRANSFORMS() {
        return localizer.localize(localizableWSS_1342_ILLEGAL_UNMATCHED_TRANSFORMS());
    }

    public static Localizable localizableWSS_1300_DSIG_TRANSFORM_PARAM_ERROR() {
        return messageFactory.getMessage("WSS1300.dsig.transform_param.error");
    }

    /**
     * WSS1300: Error occurred while creating transform object
     * 
     */
    public static String WSS_1300_DSIG_TRANSFORM_PARAM_ERROR() {
        return localizer.localize(localizableWSS_1300_DSIG_TRANSFORM_PARAM_ERROR());
    }

    public static Localizable localizableWSS_1338_ERROR_VERIFY() {
        return messageFactory.getMessage("WSS1338.error.verify");
    }

    /**
     * WSS1338: Error occured in verifying the signature
     * 
     */
    public static String WSS_1338_ERROR_VERIFY() {
        return localizer.localize(localizableWSS_1338_ERROR_VERIFY());
    }

    public static Localizable localizableWSS_1344_ERROR_LOCATE_ISSUE_TOKEN_MESSAGE() {
        return messageFactory.getMessage("WSS1344.error.locateIssueToken.Message");
    }

    /**
     * WSS1344: Could not locate Issued Token in Message
     * 
     */
    public static String WSS_1344_ERROR_LOCATE_ISSUE_TOKEN_MESSAGE() {
        return localizer.localize(localizableWSS_1344_ERROR_LOCATE_ISSUE_TOKEN_MESSAGE());
    }

    public static Localizable localizableWSS_1337_INVALID_EMPTYPREFIXLIST() {
        return messageFactory.getMessage("WSS1337.invalid.Emptyprefixlist");
    }

    /**
     * WSS1337: Prefix List cannot be empty: violation of BSP 5407
     * 
     */
    public static String WSS_1337_INVALID_EMPTYPREFIXLIST() {
        return localizer.localize(localizableWSS_1337_INVALID_EMPTYPREFIXLIST());
    }

    public static Localizable localizableWSS_1333_UNSUPPORTED_KEYIDENTIFER_X_509_V_1() {
        return messageFactory.getMessage("WSS1333.unsupported.keyidentifer.X509v1");
    }

    /**
     * WSS1333: Key Identifier strategy in X509v1 is not supported
     * 
     */
    public static String WSS_1333_UNSUPPORTED_KEYIDENTIFER_X_509_V_1() {
        return localizer.localize(localizableWSS_1333_UNSUPPORTED_KEYIDENTIFER_X_509_V_1());
    }

    public static Localizable localizableWSS_1314_ILLEGAL_X_509_DATA(Object arg0) {
        return messageFactory.getMessage("WSS1314.illegal.x509.data", arg0);
    }

    /**
     * WSS1314: Key can not be located for the X509Data (ds:KeyInfo) due to {0}
     * 
     */
    public static String WSS_1314_ILLEGAL_X_509_DATA(Object arg0) {
        return localizer.localize(localizableWSS_1314_ILLEGAL_X_509_DATA(arg0));
    }

    public static Localizable localizableWSS_1377_ERROR_IN_RESOLVING_KEYINFO() {
        return messageFactory.getMessage("WSS1377.error.in.resolving.keyinfo");
    }

    /**
     * WSS1377: An Execption occured while trying to resolve KeyInfo
     * 
     */
    public static String WSS_1377_ERROR_IN_RESOLVING_KEYINFO() {
        return localizer.localize(localizableWSS_1377_ERROR_IN_RESOLVING_KEYINFO());
    }

    public static Localizable localizableWSS_1330_NULL_PRIVATEKEY_SAML_POLICY() {
        return messageFactory.getMessage("WSS1330.null.privatekey.SAMLPolicy");
    }

    /**
     * WSS1330: PrivateKey null inside PrivateKeyBinding set for SAML Policy
     * 
     */
    public static String WSS_1330_NULL_PRIVATEKEY_SAML_POLICY() {
        return localizer.localize(localizableWSS_1330_NULL_PRIVATEKEY_SAML_POLICY());
    }

    public static Localizable localizableWSS_1323_DSIG_KEYINFO_FACTORY() {
        return messageFactory.getMessage("WSS1323.dsig.keyinfo.factory");
    }

    /**
     * WSS1323: Error occurred while instantiating XML Digital Signature KeyInfo factory
     * 
     */
    public static String WSS_1323_DSIG_KEYINFO_FACTORY() {
        return localizer.localize(localizableWSS_1323_DSIG_KEYINFO_FACTORY());
    }

    public static Localizable localizableWSS_1336_ILLEGAL_ENVELOPEDSIGNATURE() {
        return messageFactory.getMessage("WSS1336.illegal.envelopedsignature");
    }

    /**
     * WSS1336: Enveloped signatures not permitted by BSP
     * 
     */
    public static String WSS_1336_ILLEGAL_ENVELOPEDSIGNATURE() {
        return localizer.localize(localizableWSS_1336_ILLEGAL_ENVELOPEDSIGNATURE());
    }

    public static Localizable localizableWSS_1345_UNSUPPORTED_DERIVEDKEYS_SAML_TOKEN() {
        return messageFactory.getMessage("WSS1345.unsupported.derivedkeys.SAMLToken");
    }

    /**
     * WSS1345: DerivedKeys with SAMLToken not yet supported
     * 
     */
    public static String WSS_1345_UNSUPPORTED_DERIVEDKEYS_SAML_TOKEN() {
        return localizer.localize(localizableWSS_1345_UNSUPPORTED_DERIVEDKEYS_SAML_TOKEN());
    }

    public static Localizable localizableWSS_1353_UNABLE_RESOLVE_KEY_INFORMATION() {
        return messageFactory.getMessage("WSS1353.unable.resolve.keyInformation");
    }

    /**
     * WSS1353: Error occurred while resolving key information
     * 
     */
    public static String WSS_1353_UNABLE_RESOLVE_KEY_INFORMATION() {
        return localizer.localize(localizableWSS_1353_UNABLE_RESOLVE_KEY_INFORMATION());
    }

    public static Localizable localizableWSS_1350_ILLEGAL_BSP_VIOLATION_KEY_INFO() {
        return messageFactory.getMessage("WSS1350.illegal.BSP.Violation.KeyInfo");
    }

    /**
     * WSS1350: BSP Violation of R5402: KeyInfo MUST have exactly one child
     * 
     */
    public static String WSS_1350_ILLEGAL_BSP_VIOLATION_KEY_INFO() {
        return localizer.localize(localizableWSS_1350_ILLEGAL_BSP_VIOLATION_KEY_INFO());
    }

    public static Localizable localizableWSS_1370_FAILED_PROCESS_HEADER() {
        return messageFactory.getMessage("WSS1370.failed.process.header");
    }

    /**
     * WSS1370: Failed to process headers of SOAP Message
     * 
     */
    public static String WSS_1370_FAILED_PROCESS_HEADER() {
        return localizer.localize(localizableWSS_1370_FAILED_PROCESS_HEADER());
    }

    public static Localizable localizableWSS_1328_ILLEGAL_CERTIFICATE_KEY_NULL() {
        return messageFactory.getMessage("WSS1328.illegal.Certificate.key.null");
    }

    /**
     * WSSW1328: Requestor Certificate and Proof Key are both null for Issued Token
     * 
     */
    public static String WSS_1328_ILLEGAL_CERTIFICATE_KEY_NULL() {
        return localizer.localize(localizableWSS_1328_ILLEGAL_CERTIFICATE_KEY_NULL());
    }

    public static Localizable localizableWSS_1375_NO_SIGNEDPARTS() {
        return messageFactory.getMessage("WSS1375.no.signedparts");
    }

    /**
     * WSS1375: No Signed Parts found in the Message
     * 
     */
    public static String WSS_1375_NO_SIGNEDPARTS() {
        return localizer.localize(localizableWSS_1375_NO_SIGNEDPARTS());
    }

    public static Localizable localizableWSS_1324_DSIG_FACTORY() {
        return messageFactory.getMessage("WSS1324.dsig.factory");
    }

    /**
     * WSS1324: Error occurred while instantiating XML Digital Signature SignatureFactory
     * 
     */
    public static String WSS_1324_DSIG_FACTORY() {
        return localizer.localize(localizableWSS_1324_DSIG_FACTORY());
    }

    public static Localizable localizableWSS_1360_INVALID_DERIVED_KEY_TOKEN() {
        return messageFactory.getMessage("WSS1360.invalid.DerivedKeyToken");
    }

    /**
     * WSS1360: A derived Key Token should be a top level key binding
     * 
     */
    public static String WSS_1360_INVALID_DERIVED_KEY_TOKEN() {
        return localizer.localize(localizableWSS_1360_INVALID_DERIVED_KEY_TOKEN());
    }

    public static Localizable localizableWSS_1309_SAML_SIGNATURE_VERIFY_FAILED() {
        return messageFactory.getMessage("WSS1309.saml.signature.verify.failed");
    }

    /**
     * WSS1309: Exception  during Signature verfication in SAML Assertion
     * 
     */
    public static String WSS_1309_SAML_SIGNATURE_VERIFY_FAILED() {
        return localizer.localize(localizableWSS_1309_SAML_SIGNATURE_VERIFY_FAILED());
    }

    public static Localizable localizableWSS_1321_STR_MARSHAL_TRANSFORM_ERROR() {
        return messageFactory.getMessage("WSS1321.str_marshal.transform.error");
    }

    /**
     * WSS1321: Error occurred during initialization of STR-Transform
     * 
     */
    public static String WSS_1321_STR_MARSHAL_TRANSFORM_ERROR() {
        return localizer.localize(localizableWSS_1321_STR_MARSHAL_TRANSFORM_ERROR());
    }

    public static Localizable localizableWSS_1331_UNSUPPORTED_EMBEDDED_REFERENCE_SAML() {
        return messageFactory.getMessage("WSS1331.unsupported.EmbeddedReference.SAML");
    }

    /**
     * WSS1331: Embedded Reference Type for SAML Assertions not supported yet
     * 
     */
    public static String WSS_1331_UNSUPPORTED_EMBEDDED_REFERENCE_SAML() {
        return localizer.localize(localizableWSS_1331_UNSUPPORTED_EMBEDDED_REFERENCE_SAML());
    }

}
