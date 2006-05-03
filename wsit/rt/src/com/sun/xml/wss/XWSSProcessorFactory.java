/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */


package com.sun.xml.wss;


import java.io.InputStream;
import javax.security.auth.callback.CallbackHandler;


/**
 *<code>XWSSProcessorFactory</code> is a factory for creating XWSSProcessor
 * Objects.
 * An XWSSProcessor Object can be used for
 *<UL>
 *    <LI> Securing an outbound <code>SOAPMessage</Code>
 *    <LI> Verifying the security in an inbound <code>SOAPMessage</code>
 *</UL>
 */
public abstract class XWSSProcessorFactory {

    public static final String 
        XWSS_PROCESSOR_FACTORY_PROPERTY = "com.sun.xml.wss.XWSSProcessorFactory";
    
    public static final String
        DEFAULT_XWSS_PROCESSOR_FACTORY = 
              "com.sun.xml.wss.impl.misc.XWSSProcessorFactory2_0Impl";

    /**
     * Creates a new instance of <code>XWSSProcessorFactory</code>
     *
     * @return a new instance of <code>XWSSProcessorFactory</code>
     *
     * @exception XWSSecurityException if there was an error in creating the
     *            the <code>XWSSProcessorFactory</code> 
     */
    public static XWSSProcessorFactory newInstance()
        throws XWSSecurityException {
        
        ClassLoader classLoader;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Exception x) {
            throw new XWSSecurityException(x.toString(), x);
        }

        // Use the system property first
        try {
            String systemProp =
                System.getProperty(XWSS_PROCESSOR_FACTORY_PROPERTY);
            if( systemProp!=null) {
                return (XWSSProcessorFactory)newInstance(systemProp, classLoader);
            } else {
                return (XWSSProcessorFactory)newInstance(DEFAULT_XWSS_PROCESSOR_FACTORY, classLoader);
            }
        } catch (SecurityException se) {
               throw new XWSSecurityException(se.toString(), se);
        }
    }

    /**
     * Creates a new instance of <code>XWSSProcessor</code>
     *
     * @param securityConfiguration an <code>InputStream</code> 
     *        for the <code>SecurityConfiguration</code> XML to be used
     *        by the <code>XWSSProcessor</code>
     *
     * @param handler a JAAS <code>CallbackHandler</code> to be used by 
     *        the <code>XWSSProcessor</code> for Key and other Security
     *        information retrieval
     *
     * @return a new instance of <code>XWSSProcessor</code>
     *
     * @exception XWSSecurityException if there was an error in creating the
     *            the <code>XWSSProcessor</code> 
     */
    public abstract XWSSProcessor createProcessorForSecurityConfiguration(
        InputStream securityConfiguration,
        CallbackHandler handler) throws XWSSecurityException;

    /**
     * Creates a new instance of <code>XWSSProcessor</code>
     *
     * @param securityConfiguration an <code>InputStream</code> 
     *        for the <code>JAXRPCSecurityConfiguration</code> XML to be used
     *        by the <code>XWSSProcessor</code>
     *
     * @return a new instance of <code>XWSSProcessor</code>
     *
     * @exception XWSSecurityException if there was an error in creating the
     *            the <code>XWSSProcessor</code> 
    public abstract XWSSProcessor createForApplicationSecurityConfiguration(
        InputStream securityConfiguration) throws XWSSecurityException;
     */

    private static Object newInstance(String className,
                                      ClassLoader classLoader)
        throws XWSSecurityException {
        try {
            Class spiClass;
            if (classLoader == null) {
                spiClass = Class.forName(className);
            } else {
                spiClass = classLoader.loadClass(className);
            }
            return spiClass.newInstance();
        } catch (ClassNotFoundException x) {
            throw new XWSSecurityException(
                "Processor Factory " + className + " not found", x);
        } catch (Exception x) {
            throw new XWSSecurityException(
                "Processor Factory " + className + " could not be instantiated: " + x,x);
        }
    }

}
