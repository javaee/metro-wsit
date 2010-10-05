/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
/*
 * EndorsingSignatureTest.java
 *
 * Created on April 7, 2006, 12:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl;

import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
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
import com.sun.xml.ws.security.impl.policy.*;
import javax.xml.namespace.QName;
import java.security.SecureRandom;
import com.sun.xml.wss.impl.misc.*;
import javax.security.auth.callback.CallbackHandler;
import com.sun.xml.wss.impl.*;
import javax.xml.crypto.dsig.DigestMethod;
import com.sun.xml.ws.security.policy.AlgorithmSuiteValue;
import com.sun.xml.wss.impl.WSSAssertion;
import com.sun.xml.wss.impl.util.PolicyResourceLoader;
import com.sun.xml.wss.impl.util.TestUtil;
import com.sun.xml.wss.impl.AlgorithmSuite;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.AssertionSet;
import junit.framework.*;

/**
 *
 * @author root
 */
public class EndorsingSignatureTest extends TestCase{
    private static HashMap client = new HashMap();
    private static HashMap server = new HashMap();    
    private static AlgorithmSuite alg = null;
    
    /** Creates a new instance of EndorsingSignatureTest */
    public EndorsingSignatureTest(String testName) {
         super(testName);
    }
    
    protected void setUp() throws Exception {
	    
    }
                                                                                                                                                             
    protected void tearDown() throws Exception {
    }
                                                                                                                                                             
    public static Test suite() {
        TestSuite suite = new TestSuite(EndorsingSignatureTest.class);
                                                                                                                                                             
        return suite;
    }
    
    
    public void testEndorsingSignatureTest() throws Exception {
     
           // alg.setType(AlgorithmSuiteValue.Basic128);
            alg = new AlgorithmSuite(AlgorithmSuiteValue.Basic128.getDigAlgorithm(), AlgorithmSuiteValue.Basic128.getEncAlgorithm(), AlgorithmSuiteValue.Basic128.getSymKWAlgorithm(), AlgorithmSuiteValue.Basic128.getAsymKWAlgorithm());
            SignaturePolicy signaturePolicy = new SignaturePolicy();
            signaturePolicy.setUUID("22222");
            SignatureTarget st = new SignatureTarget();
            st.setType("qname");
            st.setDigestAlgorithm(DigestMethod.SHA1);
            ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
                    addTargetBinding(st);
            ((SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding()).
                    setCanonicalizationAlgorithm(MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
            
            QName name = new QName("X509Certificate");
            Token tok = new Token(name);
            
    	    SymmetricKeyBinding sigKb = 
        	(SymmetricKeyBinding)signaturePolicy.newSymmetricKeyBinding();
	    AuthenticationTokenPolicy.X509CertificateBinding x509bind = 
    	            (AuthenticationTokenPolicy.X509CertificateBinding)sigKb.newX509CertificateKeyBinding();
            x509bind.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);
	    //x509bind.setPolicyToken(tok);
            x509bind.setIncludeToken(SecurityPolicyVersion.SECURITYPOLICY200507.includeTokenNever);
    	    x509bind.setUUID(new String("1002"));
            x509bind.setIncludeToken(SecurityPolicyVersion.SECURITYPOLICY200507.includeTokenNever);
            
            SignaturePolicy signaturePolicy1 = new SignaturePolicy();
            SignatureTarget st1 = new SignatureTarget();
            st1.setType("uri");
            st1.setValue("#22222");
            st1.setDigestAlgorithm(DigestMethod.SHA1);
            ((SignaturePolicy.FeatureBinding)signaturePolicy1.getFeatureBinding()).
                    addTargetBinding(st1);
            ((SignaturePolicy.FeatureBinding)signaturePolicy1.getFeatureBinding()).
                    setCanonicalizationAlgorithm(MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
            
            AuthenticationTokenPolicy.X509CertificateBinding x509bind1 =
                    (AuthenticationTokenPolicy.X509CertificateBinding)signaturePolicy1.newX509CertificateKeyBinding();
            x509bind1.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
            x509bind1.setIncludeToken(SecurityPolicyVersion.SECURITYPOLICY200507.includeTokenAlways);
	    //x509bind1.setPolicyToken(tok);
    	    x509bind1.setUUID(new String("1003"));            
            
            
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
        
            com.sun.xml.ws.security.policy.WSSAssertion wssAssertionws = null;
            WSSAssertion wssAssertion = null;
            AssertionSet as = null;
            Policy wssPolicy = new PolicyResourceLoader().loadPolicy("security/policy-binding2.xml");
            Iterator<AssertionSet> i = wssPolicy.iterator();
            if(i.hasNext())
                as = i.next();
            
            for(PolicyAssertion assertion:as){
                if(assertion.getName().getLocalPart().equals("Wss11")){
                    wssAssertionws = (com.sun.xml.ws.security.policy.WSSAssertion)assertion;
                }                      
            }
            wssAssertion = new WSSAssertion(wssAssertionws.getRequiredProperties(), "1.0");
            MessagePolicy pol = new MessagePolicy();
	    pol.append(signaturePolicy);
            pol.append(signaturePolicy1);
            pol.setWSSAssertion(wssAssertion);
            
            context.setAlgorithmSuite(alg);
            
            context.setSecurityPolicy(pol);
            CallbackHandler handler = new PolicyCallbackHandler1("client");
	    SecurityEnvironment env = new DefaultSecurityEnvironmentImpl(handler);
    	    context.setSecurityEnvironment(env);
            
            SecurityAnnotator.secureMessage(context);

	    SOAPMessage secMsg = context.getSOAPMessage();
    	    //DumpFilter.process(context);
            
            // now persist the message and read-back
            FileOutputStream sentFile = new FileOutputStream("golden.msg");
            secMsg.saveChanges();
            TestUtil.saveMimeHeaders(secMsg, "golden.mh");
            secMsg.writeTo(sentFile);
            sentFile.close();
            
            // now create the message
            SOAPMessage recMsg = TestUtil.constructMessage("golden.mh", "golden.msg");
            
            // verify
            ProcessingContextImpl context1 = verify(recMsg, null, null);
            
    }
    
   public ProcessingContextImpl verify(SOAPMessage msg, byte[] proofKey, Map map) throws Exception {
       //Create processing context and set the soap
       //message to be processed.
       ProcessingContextImpl context = new ProcessingContextImpl(map);
       context.setSOAPMessage(msg);
        
       com.sun.xml.ws.security.policy.WSSAssertion wssAssertionws = null;
       WSSAssertion wssAssertion = null;
       AssertionSet as = null;
       Policy wssPolicy = new PolicyResourceLoader().loadPolicy("security/policy-binding2.xml");
       Iterator<AssertionSet> i = wssPolicy.iterator();
       if(i.hasNext())
           as = i.next();
            
       for(PolicyAssertion assertion:as){
           if(assertion instanceof com.sun.xml.ws.security.policy.WSSAssertion){
               wssAssertionws = (com.sun.xml.ws.security.policy.WSSAssertion)assertion;
           }                      
       }
        wssAssertion = new WSSAssertion(wssAssertionws.getRequiredProperties(), "1.0");
        MessagePolicy pol = new MessagePolicy();
        context.setAlgorithmSuite(alg);
        pol.setWSSAssertion(wssAssertion);
                                                                                                           
        context.setSecurityPolicy(pol);
        CallbackHandler handler = new PolicyCallbackHandler1("server");
        SecurityEnvironment env = new DefaultSecurityEnvironmentImpl(handler);
        context.setSecurityEnvironment(env);

        SecurityRecipient.validateMessage(context);

        //System.out.println("Verfied Message");
        //DumpFilter.process(context);

        return context;
   }

//   public static void main(String[] args) throws Exception{
//       testEndorsingSignatureTest();
//   }
}
