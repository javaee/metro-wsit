/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.wss.impl;

import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.impl.policy.Token;
import com.sun.xml.ws.security.policy.AlgorithmSuiteValue;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.wss.SecurityEnvironment;

import com.sun.xml.wss.callback.PolicyCallbackHandler1;
import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.reference.KeyIdentifier;
import com.sun.xml.wss.core.reference.SamlKeyIdentifier;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.Conditions;
import com.sun.xml.wss.saml.NameIdentifier;
import com.sun.xml.wss.saml.SAMLAssertionFactory;
import com.sun.xml.wss.saml.Subject;
import com.sun.xml.wss.saml.SubjectConfirmation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TrustDKTTest extends TestCase{

    public static final String holderOfKeyConfirmation =
    "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";
    public static final String senderVouchesConfirmation =
    "urn:oasis:names:tc:SAML:1.0:cm:sender-vouches";


    private static Hashtable<String, IssuedTokenContextImpl> map = new Hashtable<String, IssuedTokenContextImpl>();
    private static  AlgorithmSuite alg = null;
    
    public TrustDKTTest(String testName) throws Exception {
        super(testName);
    }
                                                                                                                                                             
    protected void setUp() throws Exception {
    	
    }
                                                                                                                                                             
    protected void tearDown() throws Exception {
    }
                                                                                                                                                             
    public static Test suite() {
        TestSuite suite = new TestSuite(TrustDKTTest.class);
                                                                                                                                                             
        return suite;
    }

    public static void testTrustIntegrationTest() throws Exception {
        
                //System.setProperty("com.sun.xml.wss.saml.binding.jaxb", "true");
	       // alg.setType(AlgorithmSuiteValue.Basic128);
                alg = new AlgorithmSuite(AlgorithmSuiteValue.Basic128.getDigAlgorithm(), AlgorithmSuiteValue.Basic128.getEncAlgorithm(), AlgorithmSuiteValue.Basic128.getSymKWAlgorithm(), AlgorithmSuiteValue.Basic128.getAsymKWAlgorithm());
    	        SignaturePolicy signaturePolicy = new SignaturePolicy();
        	SignatureTarget st = new SignatureTarget();
	        st.setType("qname");
    	        st.setDigestAlgorithm(DigestMethod.SHA1);
        	SignatureTarget.Transform trans = new SignatureTarget.Transform();
	        trans.setTransform(MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
    	        st.addTransform(trans);

        	((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
            	        addTargetBinding(st);
	        ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
    	                setCanonicalizationAlgorithm(MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

        	IssuedTokenKeyBinding isKB = 
            	(IssuedTokenKeyBinding)signaturePolicy.newIssuedTokenKeyBinding();

                DerivedTokenKeyBinding dktSigKB = (DerivedTokenKeyBinding)signaturePolicy.newDerivedTokenKeyBinding();
                dktSigKB.setOriginalKeyBinding(isKB);

	        EncryptionPolicy encryptPolicy = new EncryptionPolicy();
    	        EncryptionTarget et = new EncryptionTarget();
        	et.setType("qname");
	        ((EncryptionPolicy.FeatureBinding)encryptPolicy.getFeatureBinding()).
    	                addTargetBinding(st);
        	((EncryptionPolicy.FeatureBinding)encryptPolicy.getFeatureBinding()).setDataEncryptionAlgorithm(MessageConstants.AES_BLOCK_ENCRYPTION_128);
	        IssuedTokenKeyBinding ieKB = 
    	        (IssuedTokenKeyBinding)encryptPolicy.newIssuedTokenKeyBinding();

                 DerivedTokenKeyBinding dktEncKB = (DerivedTokenKeyBinding)encryptPolicy.newDerivedTokenKeyBinding();
                 dktEncKB.setOriginalKeyBinding(ieKB);

        	QName name = new QName("IssuedToken");
	        Token tok = new Token(name);
    	        //isKB.setPolicyToken(tok);
        	//ieKB.setPolicyToken(tok);
                isKB.setUUID(new String("12015"));
                ieKB.setUUID(new String("12015"));
    	        MessagePolicy pol = new MessagePolicy();
                //pol.dumpMessages(true);
        	signaturePolicy.setUUID("22222");
	        pol.append(encryptPolicy);
    	        pol.append(signaturePolicy);
        
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
        	ProcessingContextImpl context = new ProcessingContextImpl();
	        context.setSOAPMessage(msg);
                context.hasIssuedToken(true);
    	        // create a new IssuedTokenContext
        	IssuedTokenContextImpl impl = new IssuedTokenContextImpl();
        
    	        SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
        	byte[] keyBytes = new byte[16];
	        rnd.nextBytes(keyBytes);
    	        impl.setProofKey(keyBytes);

	        // create a SAML Token and set it here
                Assertion assertion = createHOKAssertion(keyBytes, msg.getSOAPPart());
                
                // Get the client's public and private key to sign SAML Assertion
                                                                                                                                                             
                SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                    new SignatureKeyCallback.DefaultPrivKeyCertRequest();
                Callback skc = new SignatureKeyCallback(request);
                Callback[] callbacks = {skc};
                CallbackHandler handler = new PolicyCallbackHandler1("client");
                handler.handle(callbacks);
                PrivateKey stsPrivKey = request.getPrivateKey();
                                                                                                                                                             
                // Sign the assertion with Client's private key
                Element signedSamlElem = assertion.sign(request.getX509Certificate(), stsPrivKey);
                                                                                                                                                             
                impl.setSecurityToken(new GenericToken(signedSamlElem));
	        
                SecurityTokenReference str = new SecurityTokenReference(msg.getSOAPPart());
                KeyIdentifier samlRef = new SamlKeyIdentifier(msg.getSOAPPart());
                samlRef.setReferenceValue(assertion.getAssertionID());
                str.setReference(samlRef);           
                impl.setAttachedSecurityTokenReference(str);
                impl.setUnAttachedSecurityTokenReference(str);
                
                map.put(new String("12015"), impl);
	        //map.put(tok.getTokenId(), impl);
    	        context.setIssuedTokenContextMap(map);
        	context.setAlgorithmSuite(alg);
	        context.setSecurityPolicy(pol);
    	        //CallbackHandler handler = new PolicyCallbackHandler1("client");
        	SecurityEnvironment env = new DefaultSecurityEnvironmentImpl(handler);
	        context.setSecurityEnvironment(env);

    	        SecurityAnnotator.secureMessage(context);

        	SOAPMessage secMsg = context.getSOAPMessage();

    	        // now persist the message and read-back
        	FileOutputStream sentFile = new FileOutputStream("golden.msg");
	        secMsg.saveChanges();
    	        saveMimeHeaders(secMsg, "golden.mh");
	        msg.writeTo(sentFile);
    	        sentFile.close();

        	// now create the message
	        SOAPMessage recMsg = constructMessage("golden.mh", "golden.msg");
        
	        // verify
    	        ProcessingContextImpl ctxImpl = verify(recMsg);
                SOAPMessage vMsg = ctxImpl.getSOAPMessage();
                vMsg.saveChanges();
                ctxImpl.setSOAPMessage(vMsg);
 
                SOAPMessage newMsg = testResponse(ctxImpl);
                newMsg.saveChanges();
                context.setSOAPMessage(newMsg);

    }


    public static void saveMimeHeaders(SOAPMessage msg, String fileName)
    throws IOException {
                                                                                                                                                 
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
                                                                                                                                                 
        Hashtable<String, Object> hashTable = new Hashtable<String, Object>();
        MimeHeaders mimeHeaders = msg.getMimeHeaders();
        Iterator iterator = mimeHeaders.getAllHeaders();
                                                                                                                                                 
        while(iterator.hasNext()) {
            MimeHeader mimeHeader = (MimeHeader) iterator.next();
            hashTable.put(mimeHeader.getName(), mimeHeader.getValue());
        }
                                                                                                                                                 
        oos.writeObject(hashTable);
        oos.flush();
        oos.close();
                                                                                                                                                 
        fos.flush();
        fos.close();
    }

    public  static SOAPMessage constructMessage(String mimeHdrsFile, String msgFile)
    throws Exception {
        SOAPMessage message;
                                                                                                                                                 
        MimeHeaders mimeHeaders = new MimeHeaders();
        FileInputStream fis = new FileInputStream(msgFile);
                                                                                                                                                 
        ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream(mimeHdrsFile));
        Hashtable hashTable = (Hashtable) ois.readObject();
        ois.close();
                                                                                                                                                 
        if(hashTable.isEmpty()) {
          //  System.out.println("MimeHeaders Hashtable is empty");
        } else {
            for(int i=0; i < hashTable.size(); i++) {
                Enumeration keys = hashTable.keys();
                Enumeration values = hashTable.elements();
                while (keys.hasMoreElements() && values.hasMoreElements()) {
                    String name = (String) keys.nextElement();
                    String value = (String) values.nextElement();
                    mimeHeaders.addHeader(name, value);
                }
            }
        }
                                                                                                                                                 
        MessageFactory messageFactory = MessageFactory.newInstance();
        message = messageFactory.createMessage(mimeHeaders, fis);
                                                                                                                                                 
        message.saveChanges();
                                                                                                                                                 
        return message;
    }

   public static ProcessingContextImpl verify(SOAPMessage msg) throws Exception {
       //Create processing context and set the soap
       //message to be processed.
       ProcessingContextImpl context = new ProcessingContextImpl();
       context.setSOAPMessage(msg);
       context.hasIssuedToken(true);                                                                                                        
        MessagePolicy pol = new MessagePolicy();
        //pol.dumpMessages(true);
        //pol.append(signaturePolicy);
        context.setAlgorithmSuite(alg);
                                                                                                           
        context.setSecurityPolicy(pol);
        CallbackHandler handler = new PolicyCallbackHandler1("server");
        SecurityEnvironment env = new DefaultSecurityEnvironmentImpl(handler);
        context.setSecurityEnvironment(env);

        SecurityRecipient.validateMessage(context);

        //context.getSOAPMessage().writeTo(System.out);

        return context;
   }
   @SuppressWarnings("unchecked")
    private static Assertion createHOKAssertion(byte[] keyBytes, Document doc) {
        
        Assertion assertion = null;
        try {
                             
            SAMLAssertionFactory factory = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
                                                                                                
            // create the assertion id
            String assertionID = String.valueOf(System.currentTimeMillis());
            String issuer = "CN=Assertion Issuer,OU=AI,O=Assertion Issuer,L=Waltham,ST=MA,C=US";
                                                                                                                             
                                                                                                                             
            GregorianCalendar c = new GregorianCalendar();
            long beforeTime = c.getTimeInMillis();
            // roll the time by one hour
            long offsetHours = 60*60*1000;
                                                                                                                             
            c.setTimeInMillis(beforeTime - offsetHours);
            GregorianCalendar before= (GregorianCalendar)c.clone();
                                                                                                                             
            c = new GregorianCalendar();
            long afterTime = c.getTimeInMillis();
            c.setTimeInMillis(afterTime + offsetHours);
            GregorianCalendar after = (GregorianCalendar)c.clone();
                                                                                                                             
            GregorianCalendar issueInstant = new GregorianCalendar();
            // statements
            List statements = new LinkedList();
            NameIdentifier nmId =
            factory.createNameIdentifier(
            "CN=SAML User,OU=SU,O=SAML User,L=Los Angeles,ST=CA,C=US",
            null, // not sure abt this value
            "urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName");           

            //default priv key cert req
           
            SOAPElement elem = (SOAPElement)doc.createElementNS(WSTrustConstants.WST_NAMESPACE, "wst:BinarySecret");
            
            elem.addTextNode(Base64.encode(keyBytes));
            
            KeyInfoHeaderBlock kiHB = new KeyInfoHeaderBlock(doc);
            
            SOAPElement binSecret = null;
            kiHB.addBinarySecret(elem);
            
            List subConfirmation = new ArrayList();
            subConfirmation.add(senderVouchesConfirmation);
                     
            SubjectConfirmation scf =
            factory.createSubjectConfirmation(subConfirmation, null, kiHB.getAsSoapElement());
                                                                                                                             
                                                                                                                             
            Subject subj = factory.createSubject(nmId, scf);
                                                                                                                      
            LinkedList attributes = new LinkedList();
            
            List attributeValues = new LinkedList();
            attributeValues.add("ATTRIBUTE1");
            attributes.add( factory.createAttribute(
                "attribute1",
                "urn:com:sun:xml:wss:attribute",
                attributeValues));
                                                                                             
            statements.add(
            factory.createAttributeStatement(subj, attributes));
                                                                                                                           
            Conditions conditions = factory.createConditions(before, after, null, null, null);
                                                                                                                           
            assertion = factory.createAssertion(assertionID, issuer, issueInstant,
            conditions, null, statements);
            assertion.setMajorVersion(BigInteger.ONE);
            assertion.setMinorVersion(BigInteger.ONE);
 
            return assertion;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
    }  

    private static SOAPMessage testResponse(ProcessingContextImpl context) throws Exception {
    	        SignaturePolicy signaturePolicy = new SignaturePolicy();
        	SignatureTarget st = new SignatureTarget();
	        st.setType("qname");
    	        st.setDigestAlgorithm(DigestMethod.SHA1);
        	SignatureTarget.Transform trans = new SignatureTarget.Transform();
	        trans.setTransform(MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
    	        st.addTransform(trans);

        	((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
            	        addTargetBinding(st);
	        ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
    	                setCanonicalizationAlgorithm(MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

        	IssuedTokenKeyBinding isKB = 
            	(IssuedTokenKeyBinding)signaturePolicy.newIssuedTokenKeyBinding();
                isKB.setIncludeToken(SecurityPolicyVersion.SECURITYPOLICY200507.includeTokenNever);

                 DerivedTokenKeyBinding dktSigKB = (DerivedTokenKeyBinding)signaturePolicy.newDerivedTokenKeyBinding();
                dktSigKB.setOriginalKeyBinding(isKB);


	        EncryptionPolicy encryptPolicy = new EncryptionPolicy();
    	        EncryptionTarget et = new EncryptionTarget();
        	et.setType("qname");
	        ((EncryptionPolicy.FeatureBinding)encryptPolicy.getFeatureBinding()).
    	                addTargetBinding(st);
        	((EncryptionPolicy.FeatureBinding)encryptPolicy.getFeatureBinding()).setDataEncryptionAlgorithm(MessageConstants.AES_BLOCK_ENCRYPTION_128);
	        IssuedTokenKeyBinding ieKB = 
    	        (IssuedTokenKeyBinding)encryptPolicy.newIssuedTokenKeyBinding();
                ieKB.setIncludeToken(SecurityPolicyVersion.SECURITYPOLICY200507.includeTokenNever);
                DerivedTokenKeyBinding dktEncKB = (DerivedTokenKeyBinding)encryptPolicy.newDerivedTokenKeyBinding();
                 dktEncKB.setOriginalKeyBinding(ieKB);

        	QName name = new QName("IssuedToken");
	        Token tok = new Token(name);
    	        //isKB.setPolicyToken(tok);
        	//ieKB.setPolicyToken(tok);
                isKB.setUUID(new String("11016"));
                ieKB.setUUID(new String("11016"));
    	        MessagePolicy pol = new MessagePolicy();
                //pol.dumpMessages(true);
        	signaturePolicy.setUUID("22222");
	        pol.append(encryptPolicy);
    	        pol.append(signaturePolicy);
 
                context.setSecurityPolicy(pol);

    	        SecurityAnnotator.secureMessage(context);

                return context.getSOAPMessage();
    }
}
