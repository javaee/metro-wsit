
package com.sun.xml.wss.logging.impl.crypto;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.crypto.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1232_FAILEDTO_DECRYPT_ATTACHMENT() {
        return messageFactory.getMessage("WSS1232.failedto.decrypt.attachment");
    }

    /**
     * WSS1232: Failed to decrypt Attachment
     * 
     */
    public static String WSS_1232_FAILEDTO_DECRYPT_ATTACHMENT() {
        return localizer.localize(localizableWSS_1232_FAILEDTO_DECRYPT_ATTACHMENT());
    }

    public static Localizable localizableWSS_1234_UNMATCHED_CONTENT_ID() {
        return messageFactory.getMessage("WSS1234.unmatched.content-id");
    }

    /**
     * WSS1234: Content-Ids in encrypted and decrypted attachments donot match
     * 
     */
    public static String WSS_1234_UNMATCHED_CONTENT_ID() {
        return localizer.localize(localizableWSS_1234_UNMATCHED_CONTENT_ID());
    }

    public static Localizable localizableWSS_1227_KEY_ENCRYPTION_ALG_VIOLATION() {
        return messageFactory.getMessage("WSS1227.keyEncryptionAlg.Violation");
    }

    /**
     * WSS1227: Violation of BSP5621.  KeyEncryption algorithm MUST be one of #rsa-1_5,#rsa-oaep-mgf1p,#kw-tripledes,#kw-aes256,#kw-aes128
     * 
     */
    public static String WSS_1227_KEY_ENCRYPTION_ALG_VIOLATION() {
        return localizer.localize(localizableWSS_1227_KEY_ENCRYPTION_ALG_VIOLATION());
    }

    public static Localizable localizableWSS_1225_ERROR_ENCRYPTING_ATTACHMENT() {
        return messageFactory.getMessage("WSS1225.error.encrypting.Attachment");
    }

    /**
     * WSS1225: Error occured while trying to encrypt attachment
     * 
     */
    public static String WSS_1225_ERROR_ENCRYPTING_ATTACHMENT() {
        return localizer.localize(localizableWSS_1225_ERROR_ENCRYPTING_ATTACHMENT());
    }

    public static Localizable localizableWSS_1200_ERROR_DECRYPTING_KEY() {
        return messageFactory.getMessage("WSS1200.error.decrypting.key");
    }

    /**
     * WSS1200: Error decrypting encryption key
     * 
     */
    public static String WSS_1200_ERROR_DECRYPTING_KEY() {
        return localizer.localize(localizableWSS_1200_ERROR_DECRYPTING_KEY());
    }

    public static Localizable localizableWSS_1219_UNABLETO_REFER_ATTACHED_ISSUE_TOKEN() {
        return messageFactory.getMessage("WSS1219.unableto.refer.Attached.IssueToken");
    }

    /**
     * WSS1219: Cannot determine how to reference the Attached Issued Token in the Message
     * 
     */
    public static String WSS_1219_UNABLETO_REFER_ATTACHED_ISSUE_TOKEN() {
        return localizer.localize(localizableWSS_1219_UNABLETO_REFER_ATTACHED_ISSUE_TOKEN());
    }

    public static Localizable localizableWSS_1216_UNABLETO_GET_SYMMETRICKEY_ENCRYPTION() {
        return messageFactory.getMessage("WSS1216.unableto.get.symmetrickey.Encryption");
    }

    /**
     * WSS1216: unable to get the symmetric key for encryption
     * 
     */
    public static String WSS_1216_UNABLETO_GET_SYMMETRICKEY_ENCRYPTION() {
        return localizer.localize(localizableWSS_1216_UNABLETO_GET_SYMMETRICKEY_ENCRYPTION());
    }

    public static Localizable localizableWSS_1211_UNSUPPORTED_KEY_IDENTIFIER_STRATEGY_X_509_V_1() {
        return messageFactory.getMessage("WSS1211.unsupported.KeyIdentifierStrategy.X509v1");
    }

    /**
     * WSS1211: Key Identifier strategy with X509v1 certificate is not allowed
     * 
     */
    public static String WSS_1211_UNSUPPORTED_KEY_IDENTIFIER_STRATEGY_X_509_V_1() {
        return localizer.localize(localizableWSS_1211_UNSUPPORTED_KEY_IDENTIFIER_STRATEGY_X_509_V_1());
    }

    public static Localizable localizableWSS_1231_NULL_SYMMETRIC_KEY() {
        return messageFactory.getMessage("WSS1231.null.SymmetricKey");
    }

    /**
     * WSS1231: Symmetric Key is null
     * 
     */
    public static String WSS_1231_NULL_SYMMETRIC_KEY() {
        return localizer.localize(localizableWSS_1231_NULL_SYMMETRIC_KEY());
    }

    public static Localizable localizableWSS_1220_UNABLETO_REFER_UN_ATTACHED_ISSUE_TOKEN() {
        return messageFactory.getMessage("WSS1220.unableto.refer.Un-Attached.IssueToken");
    }

    /**
     * WSS1220: Cannot determine how to reference the Un-Attached Issued Token in the Message
     * 
     */
    public static String WSS_1220_UNABLETO_REFER_UN_ATTACHED_ISSUE_TOKEN() {
        return localizer.localize(localizableWSS_1220_UNABLETO_REFER_UN_ATTACHED_ISSUE_TOKEN());
    }

    public static Localizable localizableWSS_1237_ERROR_PROCESSING_ENCRPYTED_DATA() {
        return messageFactory.getMessage("WSS1237.Error.Processing.EncrpytedData");
    }

    /**
     * WSS1237: Error occured in processing encrypted Data
     * 
     */
    public static String WSS_1237_ERROR_PROCESSING_ENCRPYTED_DATA() {
        return localizer.localize(localizableWSS_1237_ERROR_PROCESSING_ENCRPYTED_DATA());
    }

    public static Localizable localizableWSS_1233_FAILED_GET_DATA_ENCRYPTION_ALGORITHM() {
        return messageFactory.getMessage("WSS1233.failed.get.DataEncryptionAlgorithm");
    }

    /**
     * WSS1233: Failed to get Data Encryption Algorithm
     * 
     */
    public static String WSS_1233_FAILED_GET_DATA_ENCRYPTION_ALGORITHM() {
        return localizer.localize(localizableWSS_1233_FAILED_GET_DATA_ENCRYPTION_ALGORITHM());
    }

    public static Localizable localizableWSS_1229_ERROR_PROCESSING_ENCRPYTED_KEY() {
        return messageFactory.getMessage("WSS1229.Error.Processing.EncrpytedKey");
    }

    /**
     * WSS1229: Error occured in processing encrypted key
     * 
     */
    public static String WSS_1229_ERROR_PROCESSING_ENCRPYTED_KEY() {
        return localizer.localize(localizableWSS_1229_ERROR_PROCESSING_ENCRPYTED_KEY());
    }

    public static Localizable localizableWSS_1242_EXCEPTION_DOM() {
        return messageFactory.getMessage("WSS1242.exception.dom");
    }

    /**
     * WSS1242: DOM Exception occured while trying to change SOAP Element
     * 
     */
    public static String WSS_1242_EXCEPTION_DOM() {
        return localizer.localize(localizableWSS_1242_EXCEPTION_DOM());
    }

    public static Localizable localizableWSS_1208_FAILEDTO_GENERATE_RANDOM_SYMMETRICKEY(Object arg0) {
        return messageFactory.getMessage("WSS1208.failedto.generate.random.symmetrickey", arg0);
    }

    /**
     * WSS1208: Exception [ {0} ] when trying to generate random symmetric key
     * 
     */
    public static String WSS_1208_FAILEDTO_GENERATE_RANDOM_SYMMETRICKEY(Object arg0) {
        return localizer.localize(localizableWSS_1208_FAILEDTO_GENERATE_RANDOM_SYMMETRICKEY(arg0));
    }

    public static Localizable localizableWSS_1235_FAILEDTO_GET_TARGET_ELEMENTS() {
        return messageFactory.getMessage("WSS1235.failedto.get.targetElements");
    }

    /**
     * WSS1225: Failed to get Target Elements
     * 
     */
    public static String WSS_1235_FAILEDTO_GET_TARGET_ELEMENTS() {
        return localizer.localize(localizableWSS_1235_FAILEDTO_GET_TARGET_ELEMENTS());
    }

    public static Localizable localizableWSS_1204_CRLF_INIT_FAILED() {
        return messageFactory.getMessage("WSS1204.crlf.init.failed");
    }

    /**
     * WSS1204: Error occurred while initializing internal buffer.
     * 
     */
    public static String WSS_1204_CRLF_INIT_FAILED() {
        return localizer.localize(localizableWSS_1204_CRLF_INIT_FAILED());
    }

    public static Localizable localizableWSS_1217_NULL_ISSUE_TOKEN() {
        return messageFactory.getMessage("WSS1217.null.IssueToken");
    }

    /**
     * WSS1217: Issued Token to be inserted into the Message was Null
     * 
     */
    public static String WSS_1217_NULL_ISSUE_TOKEN() {
        return localizer.localize(localizableWSS_1217_NULL_ISSUE_TOKEN());
    }

    public static Localizable localizableWSS_1202_COULDNOT_LOCATE_SYMMETRICKEY() {
        return messageFactory.getMessage("WSS1202.couldnot.locate.symmetrickey");
    }

    /**
     * WSS1202: The symmetric key required for decryption was not found.
     * 
     */
    public static String WSS_1202_COULDNOT_LOCATE_SYMMETRICKEY() {
        return localizer.localize(localizableWSS_1202_COULDNOT_LOCATE_SYMMETRICKEY());
    }

    public static Localizable localizableWSS_1241_FAILED_RECEIVER_REQ_ENCRYPTED_DATA() {
        return messageFactory.getMessage("WSS1241.failed.receiverReq.encryptedData");
    }

    /**
     * WSS1241: Receiver requirement for EncryptedData with ID "+id+ " is not met"
     * 
     */
    public static String WSS_1241_FAILED_RECEIVER_REQ_ENCRYPTED_DATA() {
        return localizer.localize(localizableWSS_1241_FAILED_RECEIVER_REQ_ENCRYPTED_DATA());
    }

    public static Localizable localizableWSS_1210_UNSUPPORTED_USERNAME_TOKEN_AS_KEY_BINDING_ENCRYPTION_POLICY() {
        return messageFactory.getMessage("WSS1210.unsupported.UsernameToken.AsKeyBinding.EncryptionPolicy");
    }

    /**
     * WSS1210: UsernameToken as KeyBinding for EncryptionPolicy is Not Yet Supported
     * 
     */
    public static String WSS_1210_UNSUPPORTED_USERNAME_TOKEN_AS_KEY_BINDING_ENCRYPTION_POLICY() {
        return localizer.localize(localizableWSS_1210_UNSUPPORTED_USERNAME_TOKEN_AS_KEY_BINDING_ENCRYPTION_POLICY());
    }

    public static Localizable localizableWSS_1223_UNABLETO_SET_KEY_INFO_ENCRYPTED_KEY() {
        return messageFactory.getMessage("WSS1223.unableto.set.KeyInfo.EncryptedKey");
    }

    /**
     * WSS1223: unable to set keyinfo in Encrypted Key
     * 
     */
    public static String WSS_1223_UNABLETO_SET_KEY_INFO_ENCRYPTED_KEY() {
        return localizer.localize(localizableWSS_1223_UNABLETO_SET_KEY_INFO_ENCRYPTED_KEY());
    }

    public static Localizable localizableWSS_1226_ERROR_SERIALIZE_HEADERS() {
        return messageFactory.getMessage("WSS1226.error.serialize.headers");
    }

    /**
     * WSS1226: Error occured while trying to serialize headers
     * 
     */
    public static String WSS_1226_ERROR_SERIALIZE_HEADERS() {
        return localizer.localize(localizableWSS_1226_ERROR_SERIALIZE_HEADERS());
    }

    public static Localizable localizableWSS_1239_FAILED_RECEIVER_REQ_MORE() {
        return messageFactory.getMessage("WSS1239.failed.receiverReq.more");
    }

    /**
     * WSS1239: More receiver requirements specified than present in the message
     * 
     */
    public static String WSS_1239_FAILED_RECEIVER_REQ_MORE() {
        return localizer.localize(localizableWSS_1239_FAILED_RECEIVER_REQ_MORE());
    }

    public static Localizable localizableWSS_1214_UNABLETO_LOCATE_CERTIFICATE_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS1214.unableto.locate.certificate.SAMLAssertion");
    }

    /**
     * WSS1214: Could not locate Certificate corresponding to Key in SubjectConfirmation of SAML Assertion
     * 
     */
    public static String WSS_1214_UNABLETO_LOCATE_CERTIFICATE_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_1214_UNABLETO_LOCATE_CERTIFICATE_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_1212_ERROR_SAML_ASSERTION_EXCEPTION() {
        return messageFactory.getMessage("WSS1212.error.SAMLAssertionException");
    }

    /**
     * WSS1212: Error occured creating SAML Assertion
     * 
     */
    public static String WSS_1212_ERROR_SAML_ASSERTION_EXCEPTION() {
        return localizer.localize(localizableWSS_1212_ERROR_SAML_ASSERTION_EXCEPTION());
    }

    public static Localizable localizableWSS_1236_EXCEPTION_SOAP() {
        return messageFactory.getMessage("WSS1236.exception.soap");
    }

    /**
     * WSS1236: SOAP Exception occured while trying to change SOAP Element
     * 
     */
    public static String WSS_1236_EXCEPTION_SOAP() {
        return localizer.localize(localizableWSS_1236_EXCEPTION_SOAP());
    }

    public static Localizable localizableWSS_1234_INVALID_TRANSFORM() {
        return messageFactory.getMessage("WSS1234.invalid.transform");
    }

    /**
     * WSS1234: Unexpected Transform for specified Algorithm
     * 
     */
    public static String WSS_1234_INVALID_TRANSFORM() {
        return localizer.localize(localizableWSS_1234_INVALID_TRANSFORM());
    }

    public static Localizable localizableWSS_1213_NULL_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS1213.null.SAMLAssertion");
    }

    /**
     * WSS1213: SAML Assertion is NULL
     * 
     */
    public static String WSS_1213_NULL_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_1213_NULL_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_1238_FAILED_RECEIVER_REQ_ATTACHMENTS() {
        return messageFactory.getMessage("WSS1238.failed.receiverReq.attachments");
    }

    /**
     * WSS1238: Receiver requirement cid:* is not met,only few attachments out of all were encrypted
     * 
     */
    public static String WSS_1238_FAILED_RECEIVER_REQ_ATTACHMENTS() {
        return localizer.localize(localizableWSS_1238_FAILED_RECEIVER_REQ_ATTACHMENTS());
    }

    public static Localizable localizableWSS_1224_ERROR_INSERTION_HEADER_BLOCK_SECURITY_HEADER() {
        return messageFactory.getMessage("WSS1224.error.insertion.HeaderBlock.SecurityHeader");
    }

    /**
     * WSS1224: Error occured while inserting header block in security header
     * 
     */
    public static String WSS_1224_ERROR_INSERTION_HEADER_BLOCK_SECURITY_HEADER() {
        return localizer.localize(localizableWSS_1224_ERROR_INSERTION_HEADER_BLOCK_SECURITY_HEADER());
    }

    public static Localizable localizableWSS_1205_UNABLETO_INITIALIZE_XML_CIPHER() {
        return messageFactory.getMessage("WSS1205.unableto.initialize.xml.cipher");
    }

    /**
     * WSS1205: Unable to initialize XML Cipher
     * 
     */
    public static String WSS_1205_UNABLETO_INITIALIZE_XML_CIPHER() {
        return localizer.localize(localizableWSS_1205_UNABLETO_INITIALIZE_XML_CIPHER());
    }

    public static Localizable localizableWSS_1201_CID_ENCRYPT_ALL_NOTSUPPORTED() {
        return messageFactory.getMessage("WSS1201.cid_encrypt_all_notsupported");
    }

    /**
     * WSS1201: Verification requirement cid:* is not supported when EncryptedData is not included into a ReferenceList
     * 
     */
    public static String WSS_1201_CID_ENCRYPT_ALL_NOTSUPPORTED() {
        return localizer.localize(localizableWSS_1201_CID_ENCRYPT_ALL_NOTSUPPORTED());
    }

    public static Localizable localizableWSS_1222_UNSUPPORTED_KEY_BINDING_ENCRYPTION_POLICY() {
        return messageFactory.getMessage("WSS1222.unsupported.KeyBinding.EncryptionPolicy");
    }

    /**
     * WSS1222: Unsupported Key Binding for EncryptionPolicy
     * 
     */
    public static String WSS_1222_UNSUPPORTED_KEY_BINDING_ENCRYPTION_POLICY() {
        return localizer.localize(localizableWSS_1222_UNSUPPORTED_KEY_BINDING_ENCRYPTION_POLICY());
    }

    public static Localizable localizableWSS_1215_UNSUPPORTED_EMBEDDED_REFERENCE_SAML_ASSERTION() {
        return messageFactory.getMessage("WSS1215.unsupported.EmbeddedReference.SAMLAssertion");
    }

    /**
     * WSS1215: Embedded Reference Type for SAML Assertions not supported yet
     * 
     */
    public static String WSS_1215_UNSUPPORTED_EMBEDDED_REFERENCE_SAML_ASSERTION() {
        return localizer.localize(localizableWSS_1215_UNSUPPORTED_EMBEDDED_REFERENCE_SAML_ASSERTION());
    }

    public static Localizable localizableWSS_1203_UNABLETO_DECRYPT_MESSAGE(Object arg0) {
        return messageFactory.getMessage("WSS1203.unableto.decrypt.message", arg0);
    }

    /**
     * WSS1203: Exception [ {0} ] while trying to decrypt message
     * 
     */
    public static String WSS_1203_UNABLETO_DECRYPT_MESSAGE(Object arg0) {
        return localizer.localize(localizableWSS_1203_UNABLETO_DECRYPT_MESSAGE(arg0));
    }

    public static Localizable localizableWSS_1221_NULL_SECURE_CONVERSATION_TOKEN() {
        return messageFactory.getMessage("WSS1221.null.SecureConversationToken");
    }

    /**
     * WSS1221: SecureConversation Token not Found
     * 
     */
    public static String WSS_1221_NULL_SECURE_CONVERSATION_TOKEN() {
        return localizer.localize(localizableWSS_1221_NULL_SECURE_CONVERSATION_TOKEN());
    }

    public static Localizable localizableWSS_1230_FAILED_RECEIVER_REQ() {
        return messageFactory.getMessage("WSS1230.failed.receiverReq");
    }

    /**
     * WSS1230: Receiver requirement for URI is not met
     * 
     */
    public static String WSS_1230_FAILED_RECEIVER_REQ() {
        return localizer.localize(localizableWSS_1230_FAILED_RECEIVER_REQ());
    }

    public static Localizable localizableWSS_1207_UNABLETO_ENCRYPT_MESSAGE() {
        return messageFactory.getMessage("WSS1207.unableto.encrypt.message");
    }

    /**
     * WSS1207: Unable to encrypt element
     * 
     */
    public static String WSS_1207_UNABLETO_ENCRYPT_MESSAGE() {
        return localizer.localize(localizableWSS_1207_UNABLETO_ENCRYPT_MESSAGE());
    }

    public static Localizable localizableWSS_1218_UNABLETO_LOCATE_ISSUE_TOKEN_MESSAGE() {
        return messageFactory.getMessage("WSS1218.unableto.locate.IssueToken.Message");
    }

    /**
     * WSS1218: Could not locate Issued Token in Message
     * 
     */
    public static String WSS_1218_UNABLETO_LOCATE_ISSUE_TOKEN_MESSAGE() {
        return localizer.localize(localizableWSS_1218_UNABLETO_LOCATE_ISSUE_TOKEN_MESSAGE());
    }

    public static Localizable localizableWSS_1209_FAILEDTO_LOCATE_ENCRYPT_PART_MESSAGE() {
        return messageFactory.getMessage("WSS1209.failedto.locate.EncryptPart.Message");
    }

    /**
     * WSS1209: None of the specified Encryption Parts found in the Message
     * 
     */
    public static String WSS_1209_FAILEDTO_LOCATE_ENCRYPT_PART_MESSAGE() {
        return localizer.localize(localizableWSS_1209_FAILEDTO_LOCATE_ENCRYPT_PART_MESSAGE());
    }

    public static Localizable localizableWSS_1240_FAILED_RECEIVER_REQ_MORETARGETS() {
        return messageFactory.getMessage("WSS1240.failed.receiverReq.moretargets");
    }

    /**
     * WSS1240: Receiver requirement has more targets specified
     * 
     */
    public static String WSS_1240_FAILED_RECEIVER_REQ_MORETARGETS() {
        return localizer.localize(localizableWSS_1240_FAILED_RECEIVER_REQ_MORETARGETS());
    }

    public static Localizable localizableWSS_1228_DATA_ENCRYPTION_ALG_VIOLATION() {
        return messageFactory.getMessage("WSS1228.DataEncryptionAlg.Violation");
    }

    /**
     * WSS1228: Violation of BSP5620 for DataEncryption Algo permitted values
     * 
     */
    public static String WSS_1228_DATA_ENCRYPTION_ALG_VIOLATION() {
        return localizer.localize(localizableWSS_1228_DATA_ENCRYPTION_ALG_VIOLATION());
    }

    public static Localizable localizableWSS_1206_ILLEGAL_TARGET(Object arg0) {
        return messageFactory.getMessage("WSS1206.illegal.target", arg0);
    }

    /**
     * WSS1206: Illegal encryption target {0}
     * 
     */
    public static String WSS_1206_ILLEGAL_TARGET(Object arg0) {
        return localizer.localize(localizableWSS_1206_ILLEGAL_TARGET(arg0));
    }

}
