
package com.sun.xml.wss.logging.impl.opt.crypto;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class LogStringsMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.wss.logging.impl.opt.crypto.LogStrings");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableWSS_1950_DATAENCRYPTION_ALGORITHM_NOTSET() {
        return messageFactory.getMessage("WSS1950.dataencryption.algorithm.notset");
    }

    /**
     * WSS1950: DataEncryption Algorithm could not be obtained from algorithm suite. Using default Triple Des algorithm
     * 
     */
    public static String WSS_1950_DATAENCRYPTION_ALGORITHM_NOTSET() {
        return localizer.localize(localizableWSS_1950_DATAENCRYPTION_ALGORITHM_NOTSET());
    }

    public static Localizable localizableWSS_1919_ERROR_WRITING_ENCRYPTEDDATA(Object arg0) {
        return messageFactory.getMessage("WSS1919.error.writing.encrypteddata", arg0);
    }

    /**
     * WSS1919: Error occurred while writing EncryptedData: {0}
     * 
     */
    public static String WSS_1919_ERROR_WRITING_ENCRYPTEDDATA(Object arg0) {
        return localizer.localize(localizableWSS_1919_ERROR_WRITING_ENCRYPTEDDATA(arg0));
    }

    public static Localizable localizableWSS_1916_ERROR_WRITING_ECRYPTEDHEADER(Object arg0) {
        return messageFactory.getMessage("WSS1916.error.writing.ecryptedheader", arg0);
    }

    /**
     * WSS1916: Error occurred while writing EncryptedHeader: {0}
     * 
     */
    public static String WSS_1916_ERROR_WRITING_ECRYPTEDHEADER(Object arg0) {
        return localizer.localize(localizableWSS_1916_ERROR_WRITING_ECRYPTEDHEADER(arg0));
    }

    public static Localizable localizableWSS_1915_INVALID_ALGORITHM_PARAMETERS(Object arg0) {
        return messageFactory.getMessage("WSS1915.invalid.algorithm.parameters", arg0);
    }

    /**
     * WSS1915: Error occurred while decrypting data. Invalid algorithm parameters for algorithm {0}
     * 
     */
    public static String WSS_1915_INVALID_ALGORITHM_PARAMETERS(Object arg0) {
        return localizer.localize(localizableWSS_1915_INVALID_ALGORITHM_PARAMETERS(arg0));
    }

    public static Localizable localizableWSS_1928_UNRECOGNIZED_CIPHERTEXT_TRANSFORM(Object arg0) {
        return messageFactory.getMessage("WSS1928.unrecognized.ciphertext.transform", arg0);
    }

    /**
     * WSS1928: Unrecognized Ciphertext transform algorithm: {0}
     * 
     */
    public static String WSS_1928_UNRECOGNIZED_CIPHERTEXT_TRANSFORM(Object arg0) {
        return localizer.localize(localizableWSS_1928_UNRECOGNIZED_CIPHERTEXT_TRANSFORM(arg0));
    }

    public static Localizable localizableWSS_1903_UNSUPPORTED_KEYBINDING_ENCRYPTIONPOLICY(Object arg0) {
        return messageFactory.getMessage("WSS1903.unsupported.keybinding.encryptionpolicy", arg0);
    }

    /**
     * WSS1903: Unsupported Key Binding for EncryptionPolicy: {0}
     * 
     */
    public static String WSS_1903_UNSUPPORTED_KEYBINDING_ENCRYPTIONPOLICY(Object arg0) {
        return localizer.localize(localizableWSS_1903_UNSUPPORTED_KEYBINDING_ENCRYPTIONPOLICY(arg0));
    }

    public static Localizable localizableWSS_1924_CIPHERVAL_MISSINGIN_CIPHERDATA() {
        return messageFactory.getMessage("WSS1924.cipherval.missingin.cipherdata");
    }

    /**
     * WSS1925: No CipherValue found in CipherData
     * 
     */
    public static String WSS_1924_CIPHERVAL_MISSINGIN_CIPHERDATA() {
        return localizer.localize(localizableWSS_1924_CIPHERVAL_MISSINGIN_CIPHERDATA());
    }

    public static Localizable localizableWSS_1907_INCORRECT_BLOCK_SIZE() {
        return messageFactory.getMessage("WSS1907.incorrect.block.size");
    }

    /**
     * WSS1907: Internal error. Incorrect block size exception occurred
     * 
     */
    public static String WSS_1907_INCORRECT_BLOCK_SIZE() {
        return localizer.localize(localizableWSS_1907_INCORRECT_BLOCK_SIZE());
    }

    public static Localizable localizableWSS_1908_ERROR_WRITING_ENCRYPTEDDATA() {
        return messageFactory.getMessage("WSS1908.error.writing.encrypteddata");
    }

    /**
     * WSS1908: Error occurred while writing encrypted data
     * 
     */
    public static String WSS_1908_ERROR_WRITING_ENCRYPTEDDATA() {
        return localizer.localize(localizableWSS_1908_ERROR_WRITING_ENCRYPTEDDATA());
    }

    public static Localizable localizableWSS_1910_ERROR_WRITING_NAMESPACES_CANONICALIZER(Object arg0) {
        return messageFactory.getMessage("WSS1910.error.writing.namespaces.canonicalizer", arg0);
    }

    /**
     * WSS1910: Unable to write namespaces to exclusive canonicalizer: {0}
     * 
     */
    public static String WSS_1910_ERROR_WRITING_NAMESPACES_CANONICALIZER(Object arg0) {
        return localizer.localize(localizableWSS_1910_ERROR_WRITING_NAMESPACES_CANONICALIZER(arg0));
    }

    public static Localizable localizableWSS_1914_INVALID_CIPHER_MODE(Object arg0) {
        return messageFactory.getMessage("WSS1914.invalid.cipher.mode", arg0);
    }

    /**
     * WSS1914: Invalid Cipher mode: {0}
     * 
     */
    public static String WSS_1914_INVALID_CIPHER_MODE(Object arg0) {
        return localizer.localize(localizableWSS_1914_INVALID_CIPHER_MODE(arg0));
    }

    public static Localizable localizableWSS_1918_ILLEGAL_ENCRYPTION_TARGET(Object arg0, Object arg1) {
        return messageFactory.getMessage("WSS1918.illegal.encryption.target", arg0, arg1);
    }

    /**
     * WSS1918: Illegal Encryption Target: uri - {0}, element - {1}
     * 
     */
    public static String WSS_1918_ILLEGAL_ENCRYPTION_TARGET(Object arg0, Object arg1) {
        return localizer.localize(localizableWSS_1918_ILLEGAL_ENCRYPTION_TARGET(arg0, arg1));
    }

    public static Localizable localizableWSS_1912_DECRYPTION_ALGORITHM_NULL() {
        return messageFactory.getMessage("WSS1912.decryption.algorithm.null");
    }

    /**
     * WSS1912: Cannot decrypt a key without knowing the algorithm
     * 
     */
    public static String WSS_1912_DECRYPTION_ALGORITHM_NULL() {
        return localizer.localize(localizableWSS_1912_DECRYPTION_ALGORITHM_NULL());
    }

    public static Localizable localizableWSS_1925_EMPTY_ENCMETHOD_ED() {
        return messageFactory.getMessage("WSS1925.empty.encmethod.ed");
    }

    /**
     * WSS1925: Empty Encryption method is not allowed
     * 
     */
    public static String WSS_1925_EMPTY_ENCMETHOD_ED() {
        return localizer.localize(localizableWSS_1925_EMPTY_ENCMETHOD_ED());
    }

    public static Localizable localizableWSS_1917_CRLF_INIT_FAILED() {
        return messageFactory.getMessage("WSS1917.crlf.init.failed");
    }

    /**
     * WSS1917: Error occurred while initializing internal buffer.
     * 
     */
    public static String WSS_1917_CRLF_INIT_FAILED() {
        return localizer.localize(localizableWSS_1917_CRLF_INIT_FAILED());
    }

    public static Localizable localizableWSS_1927_ERROR_DECRYPT_ED(Object arg0) {
        return messageFactory.getMessage("WSS1927.error.decrypt.ed", arg0);
    }

    /**
     * WSS1927: Error occured while decrypting {0}
     * 
     */
    public static String WSS_1927_ERROR_DECRYPT_ED(Object arg0) {
        return localizer.localize(localizableWSS_1927_ERROR_DECRYPT_ED(arg0));
    }

    public static Localizable localizableWSS_1921_ERROR_WRITING_ENCRYPTEDKEY(Object arg0) {
        return messageFactory.getMessage("WSS1921.error.writing.encryptedkey", arg0);
    }

    /**
     * WSS1921: Error occurred while writing EncryptedKey: {0}
     * 
     */
    public static String WSS_1921_ERROR_WRITING_ENCRYPTEDKEY(Object arg0) {
        return localizer.localize(localizableWSS_1921_ERROR_WRITING_ENCRYPTEDKEY(arg0));
    }

    public static Localizable localizableWSS_1951_ENCRYPTED_DATA_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS1951.encrypted.data.value", arg0);
    }

    /**
     * WSS1951: Encrypted Data is: {0}
     * 
     */
    public static String WSS_1951_ENCRYPTED_DATA_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_1951_ENCRYPTED_DATA_VALUE(arg0));
    }

    public static Localizable localizableWSS_1902_UNSUPPORTED_USERNAMETOKEN_KEYBINDING() {
        return messageFactory.getMessage("WSS1902.unsupported.usernametoken.keybinding");
    }

    /**
     * WSS1902: UsernameToken as KeyBinding for EncryptionPolicy is Not Yet Supported
     * 
     */
    public static String WSS_1902_UNSUPPORTED_USERNAMETOKEN_KEYBINDING() {
        return localizer.localize(localizableWSS_1902_UNSUPPORTED_USERNAMETOKEN_KEYBINDING());
    }

    public static Localizable localizableWSS_1920_ERROR_CALCULATING_CIPHERVALUE() {
        return messageFactory.getMessage("WSS1920.error.calculating.ciphervalue");
    }

    /**
     * WSS1920: Error occurred while calculating Cipher Value
     * 
     */
    public static String WSS_1920_ERROR_CALCULATING_CIPHERVALUE() {
        return localizer.localize(localizableWSS_1920_ERROR_CALCULATING_CIPHERVALUE());
    }

    public static Localizable localizableWSS_1906_INVALID_KEY_ERROR() {
        return messageFactory.getMessage("WSS1906.invalid.key.error");
    }

    /**
     * WSS1906: Invalid key provided for encryption/decryption.
     * 
     */
    public static String WSS_1906_INVALID_KEY_ERROR() {
        return localizer.localize(localizableWSS_1906_INVALID_KEY_ERROR());
    }

    public static Localizable localizableWSS_1905_ERROR_INITIALIZING_CIPHER() {
        return messageFactory.getMessage("WSS1905.error.initializing.cipher");
    }

    /**
     * WSS1905: Error occurred while initializing the cipher. Padding error occurred.
     * 
     */
    public static String WSS_1905_ERROR_INITIALIZING_CIPHER() {
        return localizer.localize(localizableWSS_1905_ERROR_INITIALIZING_CIPHER());
    }

    public static Localizable localizableWSS_1913_DECRYPTION_KEY_NULL() {
        return messageFactory.getMessage("WSS1913.decryption.key.null");
    }

    /**
     * WSS1913: Key used to decrypt EncryptedKey cannot be null
     * 
     */
    public static String WSS_1913_DECRYPTION_KEY_NULL() {
        return localizer.localize(localizableWSS_1913_DECRYPTION_KEY_NULL());
    }

    public static Localizable localizableWSS_1911_ERROR_WRITING_CIPHERVALUE(Object arg0) {
        return messageFactory.getMessage("WSS1911.error.writing.ciphervalue", arg0);
    }

    /**
     * WSS1911: Unable to calculate cipher value due to: {0}
     * 
     */
    public static String WSS_1911_ERROR_WRITING_CIPHERVALUE(Object arg0) {
        return localizer.localize(localizableWSS_1911_ERROR_WRITING_CIPHERVALUE(arg0));
    }

    public static Localizable localizableWSS_1923_ERROR_PROCESSING_CIPHERVAL(Object arg0) {
        return messageFactory.getMessage("WSS1923.error.processing.cipherval", arg0);
    }

    /**
     * WSS1923: Error occurred while processing CipherValue: {0} 
     * 
     */
    public static String WSS_1923_ERROR_PROCESSING_CIPHERVAL(Object arg0) {
        return localizer.localize(localizableWSS_1923_ERROR_PROCESSING_CIPHERVAL(arg0));
    }

    public static Localizable localizableWSS_1909_UNSUPPORTED_DATAENCRYPTION_ALGORITHM(Object arg0) {
        return messageFactory.getMessage("WSS1909.unsupported.dataencryption.algorithm", arg0);
    }

    /**
     * WSS1909: Unable to compute Cipher Value/ decrypt data as {0} algorithm is not supported for data encryption
     * 
     */
    public static String WSS_1909_UNSUPPORTED_DATAENCRYPTION_ALGORITHM(Object arg0) {
        return localizer.localize(localizableWSS_1909_UNSUPPORTED_DATAENCRYPTION_ALGORITHM(arg0));
    }

    public static Localizable localizableWSS_1922_ERROR_DECODING_CIPHERVAL(Object arg0) {
        return messageFactory.getMessage("WSS1922.error.decoding.cipherval", arg0);
    }

    /**
     * WSS1922: Error occurred while decoding CipherValue: {0}
     * 
     */
    public static String WSS_1922_ERROR_DECODING_CIPHERVAL(Object arg0) {
        return localizer.localize(localizableWSS_1922_ERROR_DECODING_CIPHERVAL(arg0));
    }

    public static Localizable localizableWSS_1952_ENCRYPTION_KEYBINDING_VALUE(Object arg0) {
        return messageFactory.getMessage("WSS1952.encryption.keybinding.value", arg0);
    }

    /**
     * WSS1952: KeyBinding in Encryption is: {0}
     * 
     */
    public static String WSS_1952_ENCRYPTION_KEYBINDING_VALUE(Object arg0) {
        return localizer.localize(localizableWSS_1952_ENCRYPTION_KEYBINDING_VALUE(arg0));
    }

    public static Localizable localizableWSS_1926_ED_KEY_NOTSET() {
        return messageFactory.getMessage("WSS1926.ed.key.notset");
    }

    /**
     * WSS1926: Key not set for EncryptedData
     * 
     */
    public static String WSS_1926_ED_KEY_NOTSET() {
        return localizer.localize(localizableWSS_1926_ED_KEY_NOTSET());
    }

    public static Localizable localizableWSS_1904_UNSUPPORTED_KEYENCRYPTION_ALGORITHM(Object arg0) {
        return messageFactory.getMessage("WSS1904.unsupported.keyencryption.algorithm", arg0);
    }

    /**
     * WSS1904: Unable to compute Cipher Value / decrypt key as {0} algorithm is not supported for key encryption
     * 
     */
    public static String WSS_1904_UNSUPPORTED_KEYENCRYPTION_ALGORITHM(Object arg0) {
        return localizer.localize(localizableWSS_1904_UNSUPPORTED_KEYENCRYPTION_ALGORITHM(arg0));
    }

}
