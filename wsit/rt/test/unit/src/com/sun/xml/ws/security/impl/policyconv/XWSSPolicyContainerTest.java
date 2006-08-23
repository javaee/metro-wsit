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
 * XWSSPolicyContainerTest.java
 * JUnit based test
 *
 * Created on August 24, 2006, 3:54 AM
 */

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import junit.framework.*;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.ws.security.policy.MessageLayout;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mayank.Mishra@SUN.com
 */
public class XWSSPolicyContainerTest extends TestCase {
    
    public XWSSPolicyContainerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XWSSPolicyContainerTest.class);
        
        return suite;
    }
    
    //for client outgoing
    public void testXWSSPolicyContainer1() throws Exception {
        XWSSPolicyContainer container = new XWSSPolicyContainer(false, false);
        SignaturePolicy sigPolicy = new SignaturePolicy();
        SignatureTarget t = new SignatureTarget();
        t.setType("uri");
        t.setValue("sig1");
        SignaturePolicy.FeatureBinding featureBinding =
                (SignaturePolicy.FeatureBinding)sigPolicy.getFeatureBinding();
        
        featureBinding.addTargetBinding(t);
        ((AuthenticationTokenPolicy.X509CertificateBinding)sigPolicy.newX509CertificateKeyBinding()).setReferenceType("Direct");
        
        EncryptionPolicy encPolicy = new EncryptionPolicy();
        EncryptionTarget t1 = new EncryptionTarget();
        t.setType("uri");
        t.setValue("enc1");
        EncryptionPolicy.FeatureBinding featureBinding1 =
                (EncryptionPolicy.FeatureBinding)encPolicy.getFeatureBinding();
        
        featureBinding1.addTargetBinding(t1);
        ((AuthenticationTokenPolicy.X509CertificateBinding)encPolicy.newX509CertificateKeyBinding()).setReferenceType("Direct");
        
        
        TimestampPolicy tp = new TimestampPolicy();
        AuthenticationTokenPolicy atp = new AuthenticationTokenPolicy();
        AuthenticationTokenPolicy.UsernameTokenBinding ut =
                new AuthenticationTokenPolicy.UsernameTokenBinding();
        atp.setFeatureBinding(ut);
        
        container.setMessageMode(false, false);
        container.setPolicyContainerMode(MessageLayout.Lax);
        container.insert(sigPolicy);
        container.insert(encPolicy);
        container.insert(tp);
        container.insert(atp);
        
        print(container.getMessagePolicy());
        
        System.out.println("\n\n\n-------------------------\n");
        
    }
    
    
    //for client incoming
    public void testXWSSPolicyContainer2() throws Exception {
        XWSSPolicyContainer container = new XWSSPolicyContainer(false, true);
        SignaturePolicy sigPolicy = new SignaturePolicy();
        SignatureTarget t = new SignatureTarget();
        t.setType("uri");
        t.setValue("sig1");
        SignaturePolicy.FeatureBinding featureBinding =
                (SignaturePolicy.FeatureBinding)sigPolicy.getFeatureBinding();
        
        featureBinding.addTargetBinding(t);
        ((AuthenticationTokenPolicy.X509CertificateBinding)sigPolicy.newX509CertificateKeyBinding()).setReferenceType("Direct");
        
        EncryptionPolicy encPolicy = new EncryptionPolicy();
        EncryptionTarget t1 = new EncryptionTarget();
        t.setType("uri");
        t.setValue("enc1");
        EncryptionPolicy.FeatureBinding featureBinding1 =
                (EncryptionPolicy.FeatureBinding)encPolicy.getFeatureBinding();
        
        featureBinding1.addTargetBinding(t1);
        ((AuthenticationTokenPolicy.X509CertificateBinding)encPolicy.newX509CertificateKeyBinding()).setReferenceType("Direct");
        
        
        TimestampPolicy tp = new TimestampPolicy();
        AuthenticationTokenPolicy atp = new AuthenticationTokenPolicy();
        AuthenticationTokenPolicy.UsernameTokenBinding ut =
                new AuthenticationTokenPolicy.UsernameTokenBinding();
        atp.setFeatureBinding(ut);
        
        container.setMessageMode(false, true);
        container.setPolicyContainerMode(MessageLayout.Lax);
        container.insert(sigPolicy);
        container.insert(encPolicy);
        container.insert(tp);
        container.insert(atp);
        
        print(container.getMessagePolicy());
        
        System.out.println("\n\n\n-------------------------\n");
        
        
    }
    
    //for server outgoing
    public void testXWSSPolicyContainer3() throws Exception {
        XWSSPolicyContainer container = new XWSSPolicyContainer(true, false);
        SignaturePolicy sigPolicy = new SignaturePolicy();
        SignatureTarget t = new SignatureTarget();
        t.setType("uri");
        t.setValue("sig1");
        SignaturePolicy.FeatureBinding featureBinding =
                (SignaturePolicy.FeatureBinding)sigPolicy.getFeatureBinding();
        
        featureBinding.addTargetBinding(t);
        ((AuthenticationTokenPolicy.X509CertificateBinding)sigPolicy.newX509CertificateKeyBinding()).setReferenceType("Direct");
        
        EncryptionPolicy encPolicy = new EncryptionPolicy();
        EncryptionTarget t1 = new EncryptionTarget();
        t.setType("uri");
        t.setValue("enc1");
        EncryptionPolicy.FeatureBinding featureBinding1 =
                (EncryptionPolicy.FeatureBinding)encPolicy.getFeatureBinding();
        
        featureBinding1.addTargetBinding(t1);
        ((AuthenticationTokenPolicy.X509CertificateBinding)encPolicy.newX509CertificateKeyBinding()).setReferenceType("Direct");
        
        
        TimestampPolicy tp = new TimestampPolicy();
        AuthenticationTokenPolicy atp = new AuthenticationTokenPolicy();
        AuthenticationTokenPolicy.UsernameTokenBinding ut =
                new AuthenticationTokenPolicy.UsernameTokenBinding();
        atp.setFeatureBinding(ut);
        
        container.setMessageMode(true, false);
        container.setPolicyContainerMode(MessageLayout.Lax);
        container.insert(sigPolicy);
        container.insert(encPolicy);
        container.insert(tp);
        container.insert(atp);
        
        print(container.getMessagePolicy());
        
        System.out.println("\n\n\n-------------------------\n");
        
        
    }
    
    //for server incoming
    public void testXWSSPolicyContainer4() throws Exception {
        XWSSPolicyContainer container = new XWSSPolicyContainer(true, true);
        SignaturePolicy sigPolicy = new SignaturePolicy();
        SignatureTarget t = new SignatureTarget();
        t.setType("uri");
        t.setValue("sig1");
        SignaturePolicy.FeatureBinding featureBinding =
                (SignaturePolicy.FeatureBinding)sigPolicy.getFeatureBinding();
        
        featureBinding.addTargetBinding(t);
        ((AuthenticationTokenPolicy.X509CertificateBinding)sigPolicy.newX509CertificateKeyBinding()).setReferenceType("Direct");
        
        EncryptionPolicy encPolicy = new EncryptionPolicy();
        EncryptionTarget t1 = new EncryptionTarget();
        t.setType("uri");
        t.setValue("enc1");
        EncryptionPolicy.FeatureBinding featureBinding1 =
                (EncryptionPolicy.FeatureBinding)encPolicy.getFeatureBinding();
        
        featureBinding1.addTargetBinding(t1);
        ((AuthenticationTokenPolicy.X509CertificateBinding)encPolicy.newX509CertificateKeyBinding()).setReferenceType("Direct");
        
        
        TimestampPolicy tp = new TimestampPolicy();
        AuthenticationTokenPolicy atp = new AuthenticationTokenPolicy();
        AuthenticationTokenPolicy.UsernameTokenBinding ut =
                new AuthenticationTokenPolicy.UsernameTokenBinding();
        atp.setFeatureBinding(ut);
        
        container.setMessageMode(true, true);
        container.setPolicyContainerMode(MessageLayout.Lax);
        container.insert(sigPolicy);
        container.insert(encPolicy);
        container.insert(tp);
        container.insert(atp);
        
        print(container.getMessagePolicy());
        
        System.out.println("\n\n\n-------------------------\n");
        
        
    }
    
    public void print(MessagePolicy generated ) throws Exception {
        for ( int i = 0 ; i<generated.size() ; i++ ) {
            System.out.println("Type : " + generated.get(i).getType());
            
            if ( PolicyTypeUtil.signaturePolicy(generated.get(i))) {
                WSSPolicy p = (WSSPolicy)generated.get(i);
                System.out.println("KeyBinding : " + p.getKeyBinding().getType());
                
                SignaturePolicy.FeatureBinding f1 = (SignaturePolicy.FeatureBinding)p.getFeatureBinding();
                List<Target> t1 = f1.getTargetBindings();
                System.out.println("No of Targets : " + t1.size());
                for ( Target t : t1 ) {
                    System.out.println(t.getType() + "  " + t.getValue());
                }
            }
            
            if ( PolicyTypeUtil.encryptionPolicy(generated.get(i))) {
                WSSPolicy p = (WSSPolicy)generated.get(i);
                System.out.println("KeyBinding : " + p.getKeyBinding().getType());
                
                EncryptionPolicy.FeatureBinding f1 = (EncryptionPolicy.FeatureBinding)p.getFeatureBinding();
                List<Target> t1 = f1.getTargetBindings();
                System.out.println("No of Targets : " + t1.size());
                for ( Target t : t1 ) {
                    System.out.println(t.getType() + "  " + t.getValue());
                }
            }
        }
    }
    
    
//    /**
//     * Test of setMessageMode method, of class com.sun.xml.ws.security.impl.policyconv.XWSSPolicyContainer.
//     */
//    public void testSetMessageMode() {
//        System.out.println("setMessageMode");
//
//        boolean isServer = true;
//        boolean isIncoming = true;
//        XWSSPolicyContainer instance = null;
//
//        instance.setMessageMode(isServer, isIncoming);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setPolicyContainerMode method, of class com.sun.xml.ws.security.impl.policyconv.XWSSPolicyContainer.
//     */
//    public void testSetPolicyContainerMode() {
//        System.out.println("setPolicyContainerMode");
//
//        MessageLayout mode = null;
//        XWSSPolicyContainer instance = null;
//
//        instance.setPolicyContainerMode(mode);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of insert method, of class com.sun.xml.ws.security.impl.policyconv.XWSSPolicyContainer.
//     */
//    public void testInsert() {
//        System.out.println("insert");
//
//        SecurityPolicy secPolicy = null;
//        XWSSPolicyContainer instance = null;
//
//        instance.insert(secPolicy);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getMessagePolicy method, of class com.sun.xml.ws.security.impl.policyconv.XWSSPolicyContainer.
//     */
//    public void testGetMessagePolicy() {
//        System.out.println("getMessagePolicy");
//
//        XWSSPolicyContainer instance = null;
//
//        MessagePolicy expResult = null;
//        MessagePolicy result = instance.getMessagePolicy();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of convert method, of class com.sun.xml.ws.security.impl.policyconv.XWSSPolicyContainer.
//     */
//    public void testConvert() {
//        System.out.println("convert");
//
//        XWSSPolicyContainer instance = null;
//
//        instance.convert();
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}
