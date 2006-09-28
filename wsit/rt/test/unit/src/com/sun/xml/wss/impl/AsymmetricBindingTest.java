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
/*
 * AsymmetricBindingTest.java
 *
 * Created on April 7, 2006, 6:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl;

import java.util.*;
import java.io.*;

import com.sun.xml.wss.callback.PolicyCallbackHandler1;
import com.sun.xml.wss.*;

import javax.xml.soap.*;
import com.sun.xml.wss.impl.policy.mls.*;
import com.sun.xml.wss.impl.filter.*;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.ws.security.impl.*;
import com.sun.xml.wss.core.*;
import com.sun.xml.wss.impl.ProcessingContextImpl;
//import com.sun.xml.ws.security.policy.*;
import com.sun.xml.ws.security.impl.policy.*;
import javax.xml.namespace.QName;
import java.security.SecureRandom;
import com.sun.xml.wss.impl.misc.*;
import javax.security.auth.callback.CallbackHandler;
import com.sun.xml.wss.impl.*;
import javax.xml.crypto.dsig.DigestMethod;
import com.sun.xml.ws.security.policy.AlgorithmSuiteValue;
import com.sun.xml.ws.security.policy.WSSAssertion;
import com.sun.xml.wss.impl.util.PolicyResourceLoader;
import com.sun.xml.wss.impl.util.TestUtil;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.AssertionSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class AsymmetricBindingTest extends TestCase{
    
    private static HashMap client = new HashMap();
    private static HashMap server = new HashMap();
    private static  AlgorithmSuite alg = new AlgorithmSuite();   
    /** Creates a new instance of AsymmetricBindingTest */
    public AsymmetricBindingTest(String testName) throws Exception {
        super(testName);
    }
    
                                                                                                                                                             
    protected void setUp() throws Exception {
    	
    }
                                                                                                                                                             
    protected void tearDown() throws Exception {
    }
                                                                                                                                                             
    public static Test suite() {
        TestSuite suite = new TestSuite(AsymmetricBindingTest.class);                                                                                                 return suite;
    }
    
    public static void testSymmetricBindingTest() throws Exception {
       
	    alg.setType(AlgorithmSuiteValue.Basic128);
    	    SignaturePolicy signaturePolicy = new SignaturePolicy();
            SignatureTarget st = new SignatureTarget();
	    st.setType("qname");
    	    st.setDigestAlgorithm(DigestMethod.SHA1);
            ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
                    addTargetBinding(st);
	    ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
    	            setCanonicalizationAlgorithm(MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

            QName name = new QName("X509Certificate");
	     Token tok = new Token(name);

	    AuthenticationTokenPolicy.X509CertificateBinding x509bind = 
    	            (AuthenticationTokenPolicy.X509CertificateBinding)signaturePolicy.newX509CertificateKeyBinding();
            x509bind.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);
	    x509bind.setPolicyToken(tok);
    	    x509bind.setUUID(tok.getTokenId());

            EncryptionPolicy encryptPolicy = new EncryptionPolicy();
	    EncryptionTarget et = new EncryptionTarget();
    	    et.setType("qname");
            ((EncryptionPolicy.FeatureBinding)encryptPolicy.getFeatureBinding()).
            	    addTargetBinding(st);

	    x509bind = (AuthenticationTokenPolicy.X509CertificateBinding)encryptPolicy.newX509CertificateKeyBinding();
	    x509bind.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);
    	    x509bind.setPolicyToken(tok);
            x509bind.setUUID(tok.getTokenId());
        
	    SOAPMessage msg = MessageFactory.newInstance().createMessage();
    	    SOAPBody body = msg.getSOAPBody();
            SOAPBodyElement sbe = body.addBodyElement(
            	    SOAPFactory.newInstance().createName(
                	"StockSymbol",
                    	"tru",
	                "http://fabrikam123.com/payloads"));
    	    sbe.addTextNode("QQQ");

	    //Create processing context and set the soap
    	    //message to be processed.
            ProcessingContextImpl context = new ProcessingContextImpl(client);
	    context.setSOAPMessage(msg);
        
            WSSAssertion wssAssertion = null;
            AssertionSet as = null;
            Policy wssPolicy = new PolicyResourceLoader().loadPolicy("security/policy-binding2.xml");
            Iterator<AssertionSet> i = wssPolicy.iterator();
            if(i.hasNext())
                as = i.next();
            
            for(PolicyAssertion assertion:as){
                if(assertion instanceof WSSAssertion){
                    wssAssertion = (WSSAssertion)assertion;
                }                      
            }
	    //wssAssertion.addRequiredProperty("RequireSignatureConfirmation");

     	    MessagePolicy pol = new MessagePolicy();
	    pol.append(signaturePolicy);
    	    pol.append(encryptPolicy);
            pol.setWSSAssertion(wssAssertion);
        
	    context.setAlgorithmSuite(alg);
        
    	    context.setSecurityPolicy(pol);
            CallbackHandler handler = new PolicyCallbackHandler1("client");
	    SecurityEnvironment env = new DefaultSecurityEnvironmentImpl(handler);
    	    context.setSecurityEnvironment(env);

            SecurityAnnotator.secureMessage(context);

	    SOAPMessage secMsg = context.getSOAPMessage();
    	    DumpFilter.process(context);

        	// now persist the message and read-back
	        FileOutputStream sentFile = new FileOutputStream("golden.msg");
    	    secMsg.saveChanges();
        	TestUtil.saveMimeHeaders(secMsg, "golden.mh");
	        secMsg.writeTo(sentFile);
    	    sentFile.close();

        	// now create the message
	        SOAPMessage recMsg = TestUtil.constructMessage("golden.mh", "golden.msg");
        
    	    // verify
            verify(recMsg, null, null);
        
    }

   public static ProcessingContextImpl verify(SOAPMessage msg, byte[] proofKey, Map map) throws Exception {
       //Create processing context and set the soap
       //message to be processed.
       ProcessingContextImpl context = new ProcessingContextImpl(map);
       context.setSOAPMessage(msg);
       
       WSSAssertion wssAssertion = null;
       AssertionSet as = null;
       Policy wssPolicy = new PolicyResourceLoader().loadPolicy("security/policy-binding2.xml");
       Iterator<AssertionSet> i = wssPolicy.iterator();
       if(i.hasNext())
           as = i.next();
            
       for(PolicyAssertion assertion:as){
           if(assertion instanceof WSSAssertion){
               wssAssertion = (WSSAssertion)assertion;
           }                      
       }
                                                                                                           
        MessagePolicy pol = new MessagePolicy();
        context.setAlgorithmSuite(alg);
        pol.setWSSAssertion(wssAssertion);
                                                                                                           
        context.setSecurityPolicy(pol);
        CallbackHandler handler = new PolicyCallbackHandler1("server");
        SecurityEnvironment env = new DefaultSecurityEnvironmentImpl(handler);
        context.setSecurityEnvironment(env);

        SecurityRecipient.validateMessage(context);
        System.out.println("Verfied Message");
        DumpFilter.process(context);
      
        return context;

   }
   
//   public static void main(String[] args) throws Exception{
//       testSymmetricBindingTest();
//   }
}
