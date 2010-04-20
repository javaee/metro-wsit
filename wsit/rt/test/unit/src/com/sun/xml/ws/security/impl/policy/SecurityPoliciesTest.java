/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.api.policy.ModelTranslator;
import com.sun.xml.ws.api.policy.ModelUnmarshaller;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicyMerger;
import com.sun.xml.ws.policy.parser.PolicyConfigParser;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.security.impl.policyconv.XWSSPolicyGenerator;
import com.sun.xml.ws.api.security.policy.SecurityPolicyVersion;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureConfirmationPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.*;

/**
 *
 * @author K.Venugopal@SUN.com
 * @author Mayank.Mishra@SUN.com
 */
public class SecurityPoliciesTest extends TestCase {
    
    public SecurityPoliciesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SecurityPoliciesTest.class);
        
        return suite;
    }
    
    public void testInteropScenario3_1() throws Exception {
        String filaname = "security/interop-1.wsdl";
        execute(filaname, false, false,1);
        execute(filaname, false, true,1);
        execute(filaname, true, false,1);
        execute(filaname, true, true,1);
    }
    
    public void testInteropScenario3_2() throws Exception {
        String filaname = "security/interop-2.wsdl";
        execute(filaname, false, false,2);
        execute(filaname, false, true,2);
        execute(filaname, true, false,2);
        execute(filaname, true, true,2);
    }
    
    public void testInteropScenario3_3() throws Exception {
        String filaname = "security/interop-3.wsdl";
        execute(filaname, false, false,3);
        execute(filaname, false, true,3);
        execute(filaname, true, false,3);
        execute(filaname, true, true,3);
    }
    
    public void testInteropScenario3_4() throws Exception {
        String filaname = "security/interop-4.wsdl";
        execute(filaname, false, false,4);
        execute(filaname, false, true,4);
        execute(filaname, true, false,4);
        execute(filaname, true, true,4);
    }
    
    public void testInteropScenario3_5() throws Exception {
        String filaname = "security/interop-5.wsdl";
        execute(filaname, false, false,5);
        execute(filaname, false, true,5);
        execute(filaname, true, false,5);
        execute(filaname, true, true,5);
    }
    
    public void testInteropScenario3_6() throws Exception {
        String filaname = "security/interop-6.wsdl";
        execute(filaname, false, false,6);
        execute(filaname, false, true,6);
        execute(filaname, true, false,6);
        execute(filaname, true, true,6);
    }
    
    public void testInteropScenario3_7() throws Exception {
        String filaname = "security/interop-7.wsdl";
        execute(filaname, false, false,7);
        execute(filaname, false, true,7);
        execute(filaname, true, false,7);
        execute(filaname, true, true,7);
    }
    
    public void testTimeStampCR6398675() throws Exception {
        String filename = "security/TimeStamp.wsdl";
        execute(filename,false,false,10);
        execute(filename,false,true,10);
        execute(filename,true,false,10);
        execute(filename,true,true,10);
    }
    
    
    public void testUMPolicy()throws Exception{
        String xmlFile = "security/interop-1.xml";
        unmarshalPolicy(xmlFile);
    }
    
    public boolean hasXPathTarget(String xpathExpr , Iterator itr){
        while(itr.hasNext()){
            if(xpathExpr.equals(itr.next())){
                return true;
            }
        }
        return false;
    }
    private PolicySourceModel unmarshalPolicyResource(String resource) throws PolicyException, IOException {
        Reader reader = getResourceReader(resource);
        PolicySourceModel model = ModelUnmarshaller.getUnmarshaller().unmarshalModel(reader);
        reader.close();
        return model;
    }
    
    private Reader getResourceReader(String resourceName) {
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName));
    }
    
    public Policy unmarshalPolicy(String xmlFile)throws Exception{
        PolicySourceModel model =  unmarshalPolicyResource(
                xmlFile);
        Policy mbp = ModelTranslator.getTranslator().translate(model);
        return mbp;
        
    }
    
    public boolean isHeaderPresent(QName expected , Iterator headers){
        while(headers.hasNext()){
            Header header = (Header) headers.next();
            if(expected.getLocalPart().equals(header.getLocalName())){
                if(expected.getNamespaceURI().equals(header.getURI())){
                    return true;
                }
            }
        }
        return false;
    }
    
    public List<SignatureTarget> createSignatureTargetList(List<String> targetType, List<String> targetValue, List<Boolean> contentOnly,
            List<List<SignatureTarget.Transform>> transformList) {
        int size = targetType.size();
        List<SignatureTarget> targetList = new ArrayList<SignatureTarget>();
        for ( int i = 0 ; i<size ; i++ ) {
            SignatureTarget t1 = new SignatureTarget();
            t1.setContentOnly(contentOnly.get(i));
            t1.setType(targetType.get(i));
            t1.setValue(targetValue.get(i));
            
            List<SignatureTarget.Transform> transList = transformList.get(i);
            for(int j=0; j<transList.size() ; j++ ) {
                t1.addTransform(transList.get(j));
            }
            targetList.add(t1);
        }
        return targetList;
    }
    
    
    public void addToSPTList(WSSPolicy policy, SignaturePolicy sp){
        SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding) sp.getFeatureBinding();
        SignatureTarget st = new SignatureTarget();
        st.setType(SignatureTarget.TARGET_TYPE_VALUE_URI);
        st.setValue(policy.getType());
        spFB.addTargetBinding(st);
    }
    
    public void addQTToSPTList(WSSPolicy policy, SignaturePolicy sp){
        SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding) sp.getFeatureBinding();
        SignatureTarget st = new SignatureTarget();
        st.setType(SignatureTarget.TARGET_TYPE_VALUE_QNAME);
        st.setValue(policy.getType());
        spFB.addTargetBinding(st);
    }
    
    public SignaturePolicy createSignaturePolicy(List<SignatureTarget> targetList) {
        SignaturePolicy policy = new SignaturePolicy();
        SignaturePolicy.FeatureBinding featureBinding =
                (SignaturePolicy.FeatureBinding)policy.getFeatureBinding();
        for ( Target t : targetList ) {
            featureBinding.addTargetBinding(t);
        }
        
        
        return policy;
    }
    
    public EncryptionPolicy createEncryptionPolicy(List<EncryptionTarget> targetList) {
        EncryptionPolicy policy = new EncryptionPolicy();
        EncryptionPolicy.FeatureBinding featureBinding =
                (EncryptionPolicy.FeatureBinding)policy.getFeatureBinding();
        for ( Target t : targetList ) {
            featureBinding.addTargetBinding(t);
        }
        
        return policy;
    }
    
    public SignaturePolicy addSignKeyBinding(SignaturePolicy policy, String type, String ref) {
        if ( "x509".equals(type) ) {
            ((AuthenticationTokenPolicy.X509CertificateBinding)policy.newX509CertificateKeyBinding()).setReferenceType(ref);
        } else if ( "symmetric".equals(type)) {
            policy.newSymmetricKeyBinding();
        } else if ( "derivedkey".equals(type)) {
            policy.newDerivedTokenKeyBinding();
        }
        
        return policy;
    }
    
    public EncryptionPolicy addEncryptKeyBinding(EncryptionPolicy policy, String type, String ref) {
        if ( "x509".equals(type) ) {
            ((AuthenticationTokenPolicy.X509CertificateBinding)policy.newX509CertificateKeyBinding()).setReferenceType(ref);
        } else if ( "symmetric".equals(type)) {
            policy.newSymmetricKeyBinding();
        } else if ( "derivedkey".equals(type)) {
            policy.newDerivedTokenKeyBinding();
        }
        
        return policy;
    }
    
    
    public AuthenticationTokenPolicy createUTPolicy(
            String username, String pass, String nonce, boolean doDigest) {
        AuthenticationTokenPolicy at = new AuthenticationTokenPolicy();
        AuthenticationTokenPolicy.UsernameTokenBinding UT =
                new AuthenticationTokenPolicy.UsernameTokenBinding();
        if ( username != null ) {
            UT.setUsername(username);
        }
        
        if ( pass != null ) {
            UT.setPassword(pass);
        }
        
        if ( nonce != null ) {
            UT.setNonce(nonce);
        }
        
        if ( doDigest ) {
            UT.setDigestOn(doDigest);
        }
        at.setFeatureBinding(UT);
        return at;
    }
    
    
    public boolean comparePolicies(MessagePolicy policy1, MessagePolicy policy2) throws Exception{
        //boolean asrt = false;
        if ( policy1.size() != policy2.size()) {
            for(int i=0;i<policy1.size();i++){
                System.out.println("Policy1:"+policy1.get(i));
            }
            for(int i=0;i<policy2.size();i++){
                System.out.println("Policy2:"+policy2.get(i));
            }
            return false;
        }
        
        for ( int i = 0 ; i<policy1.size() ; i++ ) {
            if ( policy1.get(i).getType() != policy2.get(i).getType()  ) {
                return false;
            }
            
            if ( PolicyTypeUtil.signaturePolicy(policy1.get(i))) {
                if ( !compareSignaturePolicy(((WSSPolicy) policy1.get(i)), ((WSSPolicy) policy2.get(i)) ) ) {
                    return false;
                }
            } else if ( PolicyTypeUtil.encryptionPolicy(policy1.get(i))) {
                if ( !compareEncryptionPolicy((WSSPolicy) policy1.get(i), (WSSPolicy) policy2.get(i)) ) {
                    return false;
                }
            }
        }
        return true;
    }
    
    
    
    public void modifyMessagePolicy(MessagePolicy msgPolicy ) {
        Iterator it = msgPolicy.iterator();
        HashMap<String, String> map = new HashMap<String, String>();
        while ( it.hasNext() ) {
            Object obj = it.next();
            if ( obj instanceof WSSPolicy ) {
                
                WSSPolicy pol = (WSSPolicy) obj;
                
                if ( PolicyTypeUtil.AUTH_POLICY_TYPE.equals(pol.getType())) {
                    pol = (WSSPolicy)pol.getFeatureBinding();
                }
                if ( pol.getUUID() != null ) {
                    map.put(pol.getUUID(), pol.getType());
                    pol.setUUID(pol.getType());
                }
                
                if ( PolicyTypeUtil.signaturePolicy(pol) ) {
                    SignaturePolicy sigPolicy = (SignaturePolicy)pol;
                    @SuppressWarnings("unchecked")
                    ArrayList<SignatureTarget> targetList =
                            ((SignaturePolicy.FeatureBinding)pol.getFeatureBinding()).getTargetBindings();
                    for ( SignatureTarget target : targetList ) {
                        if ( "uri".equals(target.getType()) && map.get(target.getValue()) != null ) {
                            target.setValue(map.get(target.getValue()));
                        }
                    }
                }
                
                if ( PolicyTypeUtil.encryptionPolicy(pol) ) {
                    EncryptionPolicy encPolicy = (EncryptionPolicy)pol;
                    @SuppressWarnings("unchecked")
                    ArrayList<EncryptionTarget> targetList =
                            ((EncryptionPolicy.FeatureBinding)pol.getFeatureBinding()).getTargetBindings();
                    for ( EncryptionTarget target : targetList ) {
                        if ( "uri".equals(target.getType()) && map.get(target.getValue()) != null ) {
                            target.setValue(map.get(target.getValue()));
                        }
                    }
                }
            }
        }
    }
    
    public List<SignatureTarget> createSignatureTargetEndorsingSignature(boolean contentonlyflag) {
        List<String> targetType = new ArrayList<String>();
        targetType.add("uri");
        
        List<String> targetValue = new ArrayList<String>();
        targetValue.add("#Sign");
        
        List<Boolean> contentOnly = new ArrayList<Boolean>();
        contentOnly.add(contentonlyflag);
        
        List<SignatureTarget.Transform> tl1 = new ArrayList<SignatureTarget.Transform>();
        List<List<SignatureTarget.Transform>> tl =
                new ArrayList<List<SignatureTarget.Transform>>();
        
        tl.add(tl1);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetList(targetType, targetValue, contentOnly, tl);
        
        return sigTargetList;
    }
    
    
    public List<SignatureTarget> createSignatureTargetBody(boolean contentonlyflag) {
        List<String> targetType = new ArrayList<String>();
        targetType.add("qname");
        
        List<String> targetValue = new ArrayList<String>();
        targetValue.add(Target.BODY);
        
        List<Boolean> contentOnly = new ArrayList<Boolean>();
        contentOnly.add(contentonlyflag);
        
        List<SignatureTarget.Transform> tl1 = new ArrayList<SignatureTarget.Transform>();
        List<List<SignatureTarget.Transform>> tl =
                new ArrayList<List<SignatureTarget.Transform>>();
        
        tl.add(tl1);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetList(targetType, targetValue, contentOnly, tl);
        
        return sigTargetList;
    }
    
    public List<SignatureTarget> createSignatureTargetBodyAllHeader(boolean contentonlyflag) {
        List<String> targetType = new ArrayList<String>();
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        
        
        
        List<String> targetValue = new ArrayList<String>();
        targetValue.add(Target.BODY);
        //targetValue.add(Target.ALL_MESSAGE_HEADERS);
        targetValue.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}ReplyTo");
        targetValue.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}From");
        targetValue.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}To");
        targetValue.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}Action");
        targetValue.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}RelatesTo");
        targetValue.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}MessageID");
        targetValue.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}FaultTo");
        
        
        List<Boolean> contentOnly = new ArrayList<Boolean>();
        contentOnly.add(contentonlyflag);
        contentOnly.add(contentonlyflag);
        contentOnly.add(contentonlyflag);
        contentOnly.add(contentonlyflag);
        contentOnly.add(contentonlyflag);
        contentOnly.add(contentonlyflag);
        contentOnly.add(contentonlyflag);
        contentOnly.add(contentonlyflag);
        
        
        
        
        List<SignatureTarget.Transform> tl1 = new ArrayList<SignatureTarget.Transform>();
        List<SignatureTarget.Transform> tl2 = new ArrayList<SignatureTarget.Transform>();
        List<SignatureTarget.Transform> tl3 = new ArrayList<SignatureTarget.Transform>();
        List<SignatureTarget.Transform> tl4 = new ArrayList<SignatureTarget.Transform>();
        List<SignatureTarget.Transform> tl5 = new ArrayList<SignatureTarget.Transform>();
        List<SignatureTarget.Transform> tl6 = new ArrayList<SignatureTarget.Transform>();
        List<SignatureTarget.Transform> tl7 = new ArrayList<SignatureTarget.Transform>();
        List<SignatureTarget.Transform> tl8 = new ArrayList<SignatureTarget.Transform>();
        
        List<List<SignatureTarget.Transform>> tl =
                new ArrayList<List<SignatureTarget.Transform>>();
        
        tl.add(tl1);
        tl.add(tl2);
        tl.add(tl3);
        tl.add(tl4);
        tl.add(tl5);
        tl.add(tl6);
        tl.add(tl7);
        tl.add(tl8);
        
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetList(targetType, targetValue, contentOnly, tl);
        
        return sigTargetList;
    }
    
    public List<EncryptionTarget> createEncryptionTargetBody(boolean contentonlyflag) {
        List<String> targetType = new ArrayList<String>();
        targetType.add("qname");
        
        List<String> targetValue = new ArrayList<String>();
        targetValue.add(Target.BODY);
        
        List<Boolean> contentOnly = new ArrayList<Boolean>();
        contentOnly.add(contentonlyflag);
        
        List<EncryptionTarget.Transform> tl1 = new ArrayList<EncryptionTarget.Transform>();
        List<List<EncryptionTarget.Transform>> tl =
                new ArrayList<List<EncryptionTarget.Transform>>();
        
        tl.add(tl1);
        
        List<EncryptionTarget> encTargetList =  createEncryptionTargetList(targetType, targetValue, contentOnly, tl);
        
        return encTargetList;
    }
    
    public List<EncryptionTarget> createEncryptionTargetBodyAndUT(boolean contentonlyflag) {
        List<String> targetType = new ArrayList<String>();
        targetType.add("qname");
        targetType.add("uri");
        
        List<String> targetValue = new ArrayList<String>();
        targetValue.add(Target.BODY);
        targetValue.add("UsernameTokenBinding");
        
        List<Boolean> contentOnly = new ArrayList<Boolean>();
        contentOnly.add(contentonlyflag);
        contentOnly.add(contentonlyflag);
        
        List<EncryptionTarget.Transform> tl1 = new ArrayList<EncryptionTarget.Transform>();
        List<EncryptionTarget.Transform> tl2 = new ArrayList<EncryptionTarget.Transform>();
        List<List<EncryptionTarget.Transform>> tl =
                new ArrayList<List<EncryptionTarget.Transform>>();
        
        tl.add(tl1);
        tl.add(tl2);
        
        List<EncryptionTarget> encTargetList =  createEncryptionTargetList(targetType, targetValue, contentOnly, tl);
        
        return encTargetList;
    }
    
    public List<EncryptionTarget> createEncryptionTargetList(List<String> targetType, List<String> targetValue, List<Boolean> contentOnly,
            List<List<EncryptionTarget.Transform>> transformList) {
        int size = targetType.size();
        List<EncryptionTarget> targetList = new ArrayList<EncryptionTarget>();
        for ( int i = 0 ; i<size ; i++ ) {
            EncryptionTarget t1 = new EncryptionTarget();
            t1.setContentOnly(contentOnly.get(i));
            t1.setType(targetType.get(i));
            t1.setValue(targetValue.get(i));
            
            
            List<EncryptionTarget.Transform> transList = transformList.get(i);
            for(int j=0; j<transList.size() ; j++ ) {
                t1.addCipherReferenceTransform(transList.get(j));
            }
            targetList.add(t1);
        }
        return targetList;
    }
    
    public List<EncryptionTarget> createEncryptionTargetBodyAllHeader(boolean contentonlyflag) {
        List<String> targetType = new ArrayList<String>();
        targetType.add("qname");
        targetType.add("qname");
        
        List<String> targetValue = new ArrayList<String>();
        targetValue.add(Target.BODY);
        targetValue.add(Target.ALL_MESSAGE_HEADERS);
        
        List<Boolean> contentOnly = new ArrayList<Boolean>();
        contentOnly.add(contentonlyflag);
        contentOnly.add(contentonlyflag);
        
        List<EncryptionTarget.Transform> tl1 = new ArrayList<EncryptionTarget.Transform>();
        List<EncryptionTarget.Transform> tl2 = new ArrayList<EncryptionTarget.Transform>();
        List<List<EncryptionTarget.Transform>> tl =
                new ArrayList<List<EncryptionTarget.Transform>>();
        
        tl.add(tl1);
        tl.add(tl2);
        
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetList(targetType, targetValue, contentOnly, tl);
        
        return encTargetList;
    }
    
    public List<SignatureTarget> createSignatureTargetBodySelectedHeader(List<String> targetqname, boolean contentonlyflag) {
        List<String> targetType = new ArrayList<String>();
        targetType.add("qname");
        for ( String qname : targetqname) {
            targetType.add("qname");
        }
        
        List<String> targetValue = new ArrayList<String>();
        targetValue.add(Target.BODY);
        for ( String qname : targetqname) {
            targetValue.add(qname);
        }
        
        List<Boolean> contentOnly = new ArrayList<Boolean>();
        contentOnly.add(contentonlyflag);
        for ( String qname : targetqname) {
            contentOnly.add(contentonlyflag);
        }
        
        
        List<SignatureTarget.Transform> tl1 = new ArrayList<SignatureTarget.Transform>();
        List<List<SignatureTarget.Transform>> tl =
                new ArrayList<List<SignatureTarget.Transform>>();
        
        tl.add(tl1);
        for ( String qname : targetqname) {
            List<SignatureTarget.Transform> tl2 = new ArrayList<SignatureTarget.Transform>();
            tl.add(tl2);
        }
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetList(targetType, targetValue, contentOnly, tl);
        
        return sigTargetList;
    }
    
    public List<EncryptionTarget> createEncryptionTargetBodySelectedHeader(List<String> targetqname , boolean contentonlyflag, List<String> targetType) {
        //List<String> targetType = new ArrayList<String>();
        //targetType.add("qname");
        //for ( String qname : targetqname ) {
        //    targetType.add("qname");
        //}
        
        List<String> targetValue = new ArrayList<String>();
        targetValue.add(Target.BODY);
        for ( String qname : targetqname) {
            targetValue.add(qname);
        }
        
        List<Boolean> contentOnly = new ArrayList<Boolean>();
        contentOnly.add(contentonlyflag);
        for ( String qname : targetqname) {
            contentOnly.add(contentonlyflag);
        }
        
        List<EncryptionTarget.Transform> tl1 = new ArrayList<EncryptionTarget.Transform>();
        List<List<EncryptionTarget.Transform>> tl =
                new ArrayList<List<EncryptionTarget.Transform>>();
        
        tl.add(tl1);
        for ( String qname : targetqname) {
            List<EncryptionTarget.Transform> tl2 = new ArrayList<EncryptionTarget.Transform>();
            tl.add(tl2);
        }
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetList(targetType, targetValue, contentOnly, tl);
        
        return encTargetList;
    }
    
    public List<String> createAddressingHeaderQNameList() {
        List<String> l = new ArrayList<String>();
        l.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}To");
        l.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}From");
        l.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}FaultTo");
        l.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}ReplyTo");
        l.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}MessageID");
        l.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}RelatesTo");
        l.add("{http://schemas.xmlsoap.org/ws/2004/08/addressing}Action");
        return l;
        
    }
    
    
    //Comparing two message policies
    public boolean compileMessagePolicies(MessagePolicy pol1, MessagePolicy pol2)throws Exception {
        if ( pol1.getType() != pol2.getType() ) {
            return false;
        }
        boolean asrt = true;
        ArrayList p1 = pol1.getPrimaryPolicies();
        ArrayList p2 = pol2.getPrimaryPolicies();
        
        if ( p1.size() != p2.size() ) {
            return false;
        }
        
        for(int i=0; i<p1.size(); i++ ) {
            WSSPolicy wp1 = (WSSPolicy)p1.get(i);
            WSSPolicy wp2 = (WSSPolicy)p2.get(i);
            if ( wp1.getType() != wp2.getType() ) {
                return false;
            } else {
                if ( PolicyTypeUtil.signaturePolicy(wp1)) {
                    asrt = asrt && compareSignaturePolicy(wp1, wp2);
                } else if ( PolicyTypeUtil.encryptionPolicy(wp1)) {
                    asrt = asrt && compareEncryptionPolicy(wp1, wp2);
                }
            }
            
        }
        
        return asrt;
    }
    
    public boolean compareSignaturePolicy(WSSPolicy wp1, WSSPolicy wp2) throws Exception{
        SignaturePolicy sp1 = (SignaturePolicy)wp1;
        SignaturePolicy sp2 = (SignaturePolicy)wp2;
        if ( sp1.getKeyBinding().getType() != sp2.getKeyBinding().getType() ) {
            return false;
        }
        
        //Verify targets
        SignaturePolicy.FeatureBinding f1 = (SignaturePolicy.FeatureBinding)sp1.getFeatureBinding();
       @SuppressWarnings("unchecked")
        List<Target> t1 = f1.getTargetBindings();
        
        SignaturePolicy.FeatureBinding f2 = (SignaturePolicy.FeatureBinding)sp2.getFeatureBinding();
        @SuppressWarnings("unchecked")
        List<Target> t2 = f2.getTargetBindings();
        
        if ( t1.size() != t2.size() ) {
            System.err.println("Expected number of Targets"+t1.size()+" Got "+t2.size());
            throw new Exception("Number of targets in the signature policy did not match");
        }
        
        for (int i=0; i<t1.size(); i++) {
            Target s1 = (Target)t1.get(i);
            Target s2 = (Target)t2.get(i);
            if ( s1.getType() != s2.getType() ) {
                System.err.println("Expected Target Type"+s1.getType()+" Got "+s2.getType());
                throw new Exception("Target type in signature policy did not match");
            }
            
//            if ( !s1.getValue().equals(s2.getValue() )) {
//                System.err.println("Expected Target Value"+s1.getValue()+" Got "+s2.getValue());
//                throw new Exception("Target Value in signature policydid not match");
//            }
        }
        
        return true;
    }
    
    public boolean compareEncryptionPolicy(WSSPolicy wp1, WSSPolicy wp2) throws Exception{
        EncryptionPolicy sp1 = (EncryptionPolicy)wp1;
        EncryptionPolicy sp2 = (EncryptionPolicy)wp2;
        if ( sp1.getKeyBinding().getType() != sp2.getKeyBinding().getType() ) {
            return false;
        }
        
        //Verify targets
        EncryptionPolicy.FeatureBinding f1 = (EncryptionPolicy.FeatureBinding)sp1.getFeatureBinding();
       @SuppressWarnings("unchecked")
        List<Target> t1 = f1.getTargetBindings();
        
        EncryptionPolicy.FeatureBinding f2 = (EncryptionPolicy.FeatureBinding)sp2.getFeatureBinding();
       @SuppressWarnings("unchecked")
        List<Target> t2 = f2.getTargetBindings();
        
        if ( t1.size() != t2.size() ) {
            System.err.println("Expected number of Targets"+t1.size()+" Got "+t2.size());
            throw new Exception("Number of targets in the Encryption policy did not match");
        }
        
        for (int i=0; i<t1.size(); i++) {
            Target s1 = (Target)t1.get(i);
            Target s2 = (Target)t2.get(i);
            if ( s1.getType() != s2.getType() ) {
                System.err.println("Expected Target Type"+s1.getType()+" Got "+s2.getType());
                throw new Exception("Target type in encryption policy did not match");
            }
            
            if ( s1.getValue() != s2.getValue() ) {
                System.err.println("Expected Target Value"+s1.getValue()+" Got "+s2.getValue());
                throw new Exception("Target Value in encryption policy did not match");
            }
        }
        
        return true;
    }
    
    public MessagePolicy executeTest(String fileName, boolean isServer, boolean isIncoming) throws Exception {
//        QName serviceName = new QName("PingService");
//        QName portName = new QName("Port");
//        QName operationName = new QName("Ping");
//        QName inputName = new QName("PingRequest");
//        QName outputName = new QName("PingResponse");
//        WSPolicyBuilder builder = WSPolicyBuilder.getBuilder();
//        WSPolicyFromXmlHandler handler = null;
        
        PolicyMap map = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String wsdlFile = fileName;
        URL inUrl = null;
        if(cl==null) {
            inUrl = ClassLoader.getSystemResource(wsdlFile);
        } else {
            inUrl = cl.getResource(wsdlFile);
        }
        //WSDLModel model = PolicyConfigParser.parse(buffer);
        map = PolicyConfigParser.parse(inUrl, false);
        
        
//        QName serviceName = new QName(UUID.randomUUID().toString());
//        QName portName = new QName(UUID.randomUUID().toString());
//        QName operationName = new QName(UUID.randomUUID().toString());
//        QName inputName = new QName(UUID.randomUUID().toString());
//        QName outputName = new QName(UUID.randomUUID().toString());
//
//
//        int count = 0;
//        for ( String file : policies) {
//            if ( count == 0 ) {
//                handler = new WSPolicyFromXmlHandler(file, null, serviceName, portName);
//                builder.registerHandler(handler);
//            } else if ( count == 1 ) {
//                handler = new WSPolicyFromXmlHandler(file ,WSPolicyFromXmlHandler.Scope.InputMessageScope, null, serviceName, portName,operationName,inputName,outputName);
//                builder.registerHandler(handler);
//            } else if ( count == 2 ) {
//                handler = new WSPolicyFromXmlHandler(file, WSPolicyFromXmlHandler.Scope.OutputMessageScope,null, serviceName, portName,operationName,inputName,outputName);
//                builder.registerHandler(handler);
//            }
//            count++;
//        }
//
//        PolicyMap map = builder.create();
//        PolicyMapKey endpointKey = map.createWsdlEndpointScopeKey(serviceName, portName);
//        One key should be enough , just testing.
//        PolicyMapKey inputKey =map.createWsdlOperationScopeKey(serviceName, portName,operationName);
//        PolicyMapKey outputKey = map.createWsdlOperationScopeKey(serviceName, portName,operationName);
        
        Collection c =null;
        Iterator itr = null;
        
        c = map.getAllEndpointScopeKeys();
        itr = c.iterator();
        PolicyMapKey endpointKey=null;
        while(itr.hasNext()) {
            endpointKey = (PolicyMapKey)itr.next();
        }
        
        
        c=map.getAllInputMessageScopeKeys();
        itr=c.iterator();
        PolicyMapKey inputKey=null;
        while(itr.hasNext()) {
            inputKey = (PolicyMapKey)itr.next();
        }
        
        
        c= map.getAllOutputMessageScopeKeys();
        itr=c.iterator();
        PolicyMapKey outputKey=null;
        while(itr.hasNext()) {
            outputKey = (PolicyMapKey)itr.next();
        }
        
        Policy ipEP = null;
        Policy opEP = null;
        Policy endpointEP = null;
        
        endpointEP = (Policy) map.getEndpointEffectivePolicy(endpointKey);
        ipEP = (Policy) map.getInputMessageEffectivePolicy(inputKey);
        opEP = (Policy)map.getOutputMessageEffectivePolicy(outputKey);
        
        ArrayList<Policy> pl = new ArrayList<Policy>();
        SecurityPolicyVersion spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
        if(endpointEP !=null){
            pl.add(endpointEP);
            if(endpointEP.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri)){
                spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
            } else if(endpointEP.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri)){
                spVersion = SecurityPolicyVersion.SECURITYPOLICY12NS;
            }
        }
        
        if ( ipEP != null )
            pl.add(ipEP);
        
        if ( opEP != null)
            pl.add(opEP);
        
        //Start Processing client side policy
        PolicyMerger pm = PolicyMerger.getMerger();
        Policy ep = pm.merge(pl);
        
        XWSSPolicyGenerator generator = new XWSSPolicyGenerator(ep, isServer, isIncoming, spVersion);
        generator.process();
        MessagePolicy pol = generator.getXWSSPolicy();
        System.out.println("\n\nGenerated Policies ........");
        
        for ( int i=0; i< pol.size(); i++ ) {
            System.out.println(pol.get(i).getType());
        }
        
        return pol;
        
//        System.out.println("\n\nExpected Policies ........");
//        for ( int i=0; i< expectedPolicy.size(); i++ ) {
//            System.out.println(expectedPolicy.get(i).getType());
//        }
//        System.out.println("Verification status : " + comparePolicies(expectedPolicy,pol) );
//        return comparePolicies(expectedPolicy,pol);
        
    }
    
    public void execute(String filename, boolean isServer, boolean isIncoming, int scenario) throws Exception {
        MessagePolicy generated = executeTest(filename, isServer, isIncoming);
        modifyMessagePolicy(generated);
        print(generated);
        MessagePolicy expectedPolicy;
        
        if ( !isServer && isIncoming ) {
            expectedPolicy = createClientIncomingPolicy(scenario);
        } else if ( !isServer && !isIncoming) {
            expectedPolicy = createClientOutgoingPolicy(scenario);
        } else if ( isServer && isIncoming) {
            expectedPolicy = createServerIncomingPolicy(scenario);
        } else {
            expectedPolicy = createServerOutgoingPolicy(scenario);
        }
        System.out.println("\nExpectedPolicy");
        
        print(expectedPolicy);
        compare(expectedPolicy, generated);
    }
    
    
    public void print(MessagePolicy generated ) throws Exception {
        for ( int i = 0 ; i<generated.size() ; i++ ) {
            System.out.println("Type : " + generated.get(i).getType());
            
            if ( PolicyTypeUtil.signaturePolicy(generated.get(i))) {
                WSSPolicy p = (WSSPolicy)generated.get(i);
                System.out.println("KeyBinding : " + p.getKeyBinding().getType());
                
                SignaturePolicy.FeatureBinding f1 = (SignaturePolicy.FeatureBinding)p.getFeatureBinding();
               @SuppressWarnings("unchecked")
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
               @SuppressWarnings("unchecked")
                List<Target> t1 = f1.getTargetBindings();
                System.out.println("No of Targets : " + t1.size());
                for ( Target t : t1 ) {
                    System.out.println(t.getType() + "  " + t.getValue());
                }
            }
        }
    }
    
    public void compare(MessagePolicy expectedPolicy, MessagePolicy generated) throws Exception {
        System.out.println("Comparing two message policies ...");
        if ( !comparePolicies(expectedPolicy, generated)) {
            throw new Exception("Expected and Generated policy did not match");
        }
    }
    
    public List<String> getList(String pattern) {
        //G:\\optimizedpath\\tango\\jaxrpc-sec\\Tango\\tests\\data\\
        List<String> l = new ArrayList<String>();
        l.add(pattern + ".xml");
        l.add(pattern + "-input.xml");
        l.add(pattern + "-output.xml");
        return l;
    }
    
    
    private MessagePolicy reverse(MessagePolicy tmpPolicy) throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        boolean foundSigConf = false;
        boolean foundts = false;
        SecurityPolicy sigConf = null;
        SecurityPolicy tpolicy = null;
        for ( int i=0 ; i < tmpPolicy.size(); i++) {
            if ( tmpPolicy.get(i).getType().equals(PolicyTypeUtil.SIGNATURE_CONFIRMATION_POLICY_TYPE)) {
                foundSigConf = true;
                sigConf = tmpPolicy.get(i);
            } else if (tmpPolicy.get(i).getType().equals(PolicyTypeUtil.TIMESTAMP_POLICY_TYPE)) {
                foundts = true;
                tpolicy = tmpPolicy.get(i);
            } else {
                msgPolicy.prepend(tmpPolicy.get(i));
            }
        }
        if ( foundSigConf ) {
            msgPolicy.append(sigConf);
        }
        if ( foundts ) {
            msgPolicy.prepend(tpolicy);
        }
        return msgPolicy;
    }
    
    public MessagePolicy createClientIncomingPolicy(int scenario) throws Exception {
        switch (scenario) {
            case 1:
                return createScenario1ClientIncoming();
            case 2:
                return createScenario2ClientIncoming();
            case 3:
                return createScenario3ClientIncoming();
            case 4:
                return createScenario4ClientIncoming();
            case 5:
                return createScenario5ClientIncoming();
            case 6:
                return createScenario6ClientIncoming();
            case 7:
                return createScenario7ClientIncoming();
            case 8:
                return createScenario8ClientIncoming();
            case 9:
                //return createScenario9ClientIncoming();
            case 10:
                return createScenario10ClientIncoming();
            case 11:
                return createScenario11ClientIncoming();
            case 21:
                return createScenario21ClientIncoming();
            case 22:
                return createScenario22ClientIncoming();
            case 23:
                return createScenario23ClientIncoming();
            case 24:
                return createScenario24ClientIncoming();
        }
        return null;
    }
    
    
    public MessagePolicy createClientOutgoingPolicy(int scenario) throws Exception {
        switch (scenario) {
            case 1:
                return createScenario1ClientOutgoing();
            case 2:
                return createScenario2ClientOutgoing();
            case 3:
                return createScenario3ClientOutgoing();
            case 4:
                return createScenario4ClientOutgoing();
            case 5:
                return createScenario5ClientOutgoing();
            case 6:
                return createScenario6ClientOutgoing();
            case 7:
                return createScenario7ClientOutgoing();
            case 8:
                return createScenario8ClientOutgoing();
            case 9:
                //return createScenario9ClientOutgoing();
            case 10:
                return createScenario10ClientOutgoing();
            case 11:
                return createScenario11ClientOutgoing();
            case 21:
                return createScenario21ClientOutgoing();
            case 22:
                return createScenario22ClientOutgoing();
            case 23:
                return createScenario23ClientOutgoing();
            case 24:
                return createScenario24ClientOutgoing();
        }
        return null;
    }
    
    
    public MessagePolicy createServerIncomingPolicy(int scenario) throws Exception {
        //
        
        if ( scenario == 5 ) {
            return createScenario5ServerIncoming();
        }
        if (scenario == 1) {
            return createScenario1ServerIncoming();
        }
        if(scenario == 6) {
            return createScenario6ServerIncoming();
        }
        if(scenario == 7) {
            return createScenario7ServerIncoming();
        }
        return reverse(createClientOutgoingPolicy(scenario));
        
    }
    
    public MessagePolicy createServerOutgoingPolicy(int scenario) throws Exception {
        if(scenario == 6) {
            return createScenario6ServerOutgoing();
        }
        if(scenario == 7) {
            return createScenario7ServerOutgoing();
        }
        return reverse(createClientIncomingPolicy(scenario));
    }
    
    //UserNameOverTransport_IPingService_policy
    public MessagePolicy createScenario1ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        AuthenticationTokenPolicy at =
                createUTPolicy("testuser", null, null, false);
        at.setUUID("UsernameTokenBinding");
        msgPolicy.append(at);
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario1ClientIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        return msgPolicy;
    }
    
    public MessagePolicy createScenario1ServerIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        AuthenticationTokenPolicy at =
                createUTPolicy("testuser", null, null, false);
        at.setUUID("UsernameTokenBinding");
        msgPolicy.append(at);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        return msgPolicy;
    }
    
    
    
    
    
    //MutualCertificate10SignEncrypt_IPingService_policy
    public MessagePolicy createScenario2ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBody(true);
        
//       AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//      suite.setType(AlgorithmSuiteValue.Basic256);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "x509", "Direct");
        msgPolicy.append(sigPolicy);
        addToSPTList(tp,sigPolicy);
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "x509", "Identifier");
        msgPolicy.append(encPolicy);
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario2ClientIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "x509", "Identifier");
        msgPolicy.append(encPolicy);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.Basic256);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBody(true);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "x509", "Direct");
        msgPolicy.append(sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        return msgPolicy;
        
        
    }
    
    //MutualCertificate10SignEncryptRsa15TripleDes_IPingService_policy
    public MessagePolicy createScenario3ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBody(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "x509", "Direct");
        msgPolicy.append(sigPolicy);
        addToSPTList(tp,sigPolicy);
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "x509", "Identifier");
        msgPolicy.append(encPolicy);
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario3ClientIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "x509", "Identifier");
        msgPolicy.append(encPolicy);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBody(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "x509", "Direct");
        msgPolicy.append(sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        
        return msgPolicy;
    }
    
    
    //MutualCertificate10Sign_IPingServiceSign_policy
    public MessagePolicy createScenario4ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBody(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.Basic256);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "x509", "Direct");
        msgPolicy.append(sigPolicy);
        addToSPTList(tp,sigPolicy);
        return msgPolicy;
    }
    
    public MessagePolicy createScenario4ClientIncoming() throws Exception {
        return reverse(createScenario4ClientOutgoing());
    }
    
    
    //UserNameForCertificateSignEncrypt_IPingService_policy
    public MessagePolicy createScenario5ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        AuthenticationTokenPolicy at =
                createUTPolicy("testuser", null, null, false);
        at.setUUID("UsernameTokenBinding");
        msgPolicy.append(at);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy = createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(sigPolicy);
        addToSPTList(tp,sigPolicy);
        addToSPTList(at,sigPolicy);
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBodyAndUT(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(encPolicy);
        
        return msgPolicy;
    }
    
    
    public MessagePolicy createScenario5ServerIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        AuthenticationTokenPolicy at =
                createUTPolicy("testuser", null, null, false);
        at.setUUID("UsernameTokenBinding");
        msgPolicy.append(at);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        
        addToSPTList(tp,sigPolicy);
        addToSPTList(at,sigPolicy);
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBodyAndUT(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(encPolicy);
        msgPolicy.append(sigPolicy);
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario5ClientIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        TimestampPolicy tp = new TimestampPolicy();
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "derivedkey", "Thumbprint");
        
        msgPolicy.append(encPolicy);
        msgPolicy.append(tp);
        
        msgPolicy.append(sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        
        return msgPolicy;
    }
    
    //AnonymousForCertificateSignEncrypt_IPingService_policy
    public MessagePolicy createScenario6ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        SignatureConfirmationPolicy sigConf = new SignatureConfirmationPolicy();
        msgPolicy.append(sigConf);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(sigPolicy);
        addQTToSPTList(sigConf,sigPolicy);
        addToSPTList(tp,sigPolicy);
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(encPolicy);
        
        
        
        return msgPolicy;
    }
    // if 5 then ????
    public MessagePolicy createScenario6ClientIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        SignatureConfirmationPolicy sigConf = new SignatureConfirmationPolicy();
        msgPolicy.append(sigConf);
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(encPolicy);
        
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(sigPolicy);
        addQTToSPTList(sigConf,sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        return msgPolicy;
    }
    
    
    public MessagePolicy createScenario6ServerOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        SignatureConfirmationPolicy sigConf = new SignatureConfirmationPolicy();
        msgPolicy.append(sigConf);
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(sigPolicy);
        addQTToSPTList(sigConf,sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(encPolicy);
        
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario6ServerIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        SignatureConfirmationPolicy sigConf = new SignatureConfirmationPolicy();
        msgPolicy.append(sigConf);
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(encPolicy);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(sigPolicy);
        addQTToSPTList(sigConf,sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        
        
        
        return msgPolicy;
    }
    
    
    //MutualCertificate11SignEncrypt_IPingService_policy
    public MessagePolicy createScenario7ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        SignatureConfirmationPolicy sigConf = new SignatureConfirmationPolicy();
        msgPolicy.append(sigConf);
        
        AuthenticationTokenPolicy at =
                createUTPolicy("testuser", null, null, false);
        at.setUUID("UsernameTokenBinding");
        msgPolicy.append(at);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "symmetric", "");
        msgPolicy.append(sigPolicy);
        addQTToSPTList(sigConf,sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        List<SignatureTarget> sigTargetList1 =
                createSignatureTargetEndorsingSignature(true);
        SignaturePolicy sigPolicy1 = createSignaturePolicy(sigTargetList1);
        addSignKeyBinding(sigPolicy1, "x509", "Identifier");
        msgPolicy.append(sigPolicy1);
        
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "symmetric", "");
        msgPolicy.append(encPolicy);
        
        
        
        
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario7ClientIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
//        AuthenticationTokenPolicy at =
//                createUTPolicy("testuser", null, null, false);
//        at.setUUID("UsernameTokenBinding");
//        msgPolicy.append(at);
        SignatureConfirmationPolicy sigConf = new SignatureConfirmationPolicy();
        msgPolicy.append(sigConf);
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "symmetric", "");
        msgPolicy.append(encPolicy);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
        
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "symmetric", "");
        msgPolicy.append(sigPolicy);
        addQTToSPTList(sigConf,sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        
        
        
        
        return msgPolicy;
    }
    
    //MutualCertificate11SignEncrypt_IPingService_policy
    public MessagePolicy createScenario7ServerIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        SignatureConfirmationPolicy sigConf = new SignatureConfirmationPolicy();
        msgPolicy.append(sigConf);
        
        AuthenticationTokenPolicy at =
                createUTPolicy("testuser", null, null, false);
        at.setUUID("UsernameTokenBinding");
        msgPolicy.append(at);
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "symmetric", "");
        msgPolicy.append(encPolicy);
        
        List<SignatureTarget> sigTargetList1 =
                createSignatureTargetEndorsingSignature(true);
        SignaturePolicy sigPolicy1 = createSignaturePolicy(sigTargetList1);
        addSignKeyBinding(sigPolicy1, "x509", "Identifier");
        msgPolicy.append(sigPolicy1);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "symmetric", "");
        msgPolicy.append(sigPolicy);
        addQTToSPTList(sigConf,sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        return msgPolicy;
    }
    
    //MutualCertificate11SignEncrypt_IPingService_policy
    public MessagePolicy createScenario7ServerOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        SignatureConfirmationPolicy sigConf = new SignatureConfirmationPolicy();
        msgPolicy.append(sigConf);
        
    /*    AuthenticationTokenPolicy at =
                createUTPolicy("testuser", null, null, false);
        at.setUUID("UsernameTokenBinding");
        msgPolicy.append(at);
     */
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
    /*    List<SignatureTarget> sigTargetList1 =
                createSignatureTargetEndorsingSignature(true);
        SignaturePolicy sigPolicy1 = createSignaturePolicy(sigTargetList1);
        addSignKeyBinding(sigPolicy1, "x509", "Identifier");
        msgPolicy.append(sigPolicy1);
     */
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "symmetric", "");
        msgPolicy.append(sigPolicy);
        addQTToSPTList(sigConf,sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "symmetric", "");
        msgPolicy.append(encPolicy);
        
        
        return msgPolicy;
    }
    
    //MutualCertificate11SignEncryptDerivedKeys_IPingService_policy
    public MessagePolicy createScenario8ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.TripleDesRsa15);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "");
        msgPolicy.append(sigPolicy);
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "derivedkey", "");
        msgPolicy.append(encPolicy);
        
        List<SignatureTarget> sigTargetList1 =
                createSignatureTargetEndorsingSignature(true);
        SignaturePolicy sigPolicy1 = createSignaturePolicy(sigTargetList1);
        addSignKeyBinding(sigPolicy1, "derivedkey", "");
        msgPolicy.append(sigPolicy1);
        
        SignatureConfirmationPolicy sigConf = new SignatureConfirmationPolicy();
        msgPolicy.append(sigConf);
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario8ClientIncoming() throws Exception {
        return(reverse(createScenario7ClientOutgoing()));
    }
    
    public MessagePolicy createScenario10ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        //     TimestampPolicy tp = new TimestampPolicy();
        // msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBody(true);
        
//       AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//      suite.setType(AlgorithmSuiteValue.Basic256);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "x509", "Direct");
        msgPolicy.append(sigPolicy);
        //    addToSPTList(tp,sigPolicy);
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "x509", "Identifier");
        msgPolicy.append(encPolicy);
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario10ClientIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "x509", "Identifier");
        msgPolicy.append(encPolicy);
        
        // TimestampPolicy tp = new TimestampPolicy();
        // msgPolicy.append(tp);
        
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.Basic256);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBody(true);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "x509", "Direct");
        msgPolicy.append(sigPolicy);
        //   addToSPTList(tp,sigPolicy);
        return msgPolicy;
    }
    
    
    public MessagePolicy createScenario11ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<String> targetSignType =  new ArrayList<String>();
        targetSignType.add("uri");
        List<String> targetSignValue =  new ArrayList<String>();
        targetSignValue.add("TimestampPolicy");
        List<Boolean> targetSignContent =  new ArrayList<Boolean>();
        targetSignContent.add(true);
        List<SignatureTarget.Transform> transform = new ArrayList<SignatureTarget.Transform>();
        List<List<SignatureTarget.Transform>> tl = new ArrayList<List<SignatureTarget.Transform>>();
        tl.add(transform);
        
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetList(targetSignType, targetSignValue, targetSignContent, tl);
        
//       AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//      suite.setType(AlgorithmSuiteValue.Basic256);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "symmetric", "");
        msgPolicy.append(sigPolicy);
        // addToSPTList(tp,sigPolicy);
        List<String> listHeaders = createAddressingHeaderQNameList();
        
        List<String> targetType = new ArrayList<String>();
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        
        List<EncryptionTarget> encTargetList = createEncryptionTargetBodySelectedHeader(listHeaders,true,targetType);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "symmetric", "");
        msgPolicy.append(encPolicy);
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario11ClientIncoming() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<String> targetSignType =  new ArrayList<String>();
        targetSignType.add("uri");
        List<String> targetSignValue =  new ArrayList<String>();
        targetSignValue.add("TimestampPolicy");
        List<Boolean> targetSignContent =  new ArrayList<Boolean>();
        targetSignContent.add(true);
        List<SignatureTarget.Transform> transform = new ArrayList<SignatureTarget.Transform>();
        List<List<SignatureTarget.Transform>> tl = new ArrayList<List<SignatureTarget.Transform>>();
        tl.add(transform);
        
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetList(targetSignType, targetSignValue, targetSignContent, tl);
        
//       AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//      suite.setType(AlgorithmSuiteValue.Basic256);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "symmetric", "");
        msgPolicy.append(sigPolicy);
        // addToSPTList(tp,sigPolicy);
        List<String> listHeaders = createAddressingHeaderQNameList();
        
        List<String> targetType = new ArrayList<String>();
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        targetType.add("qname");
        
        List<EncryptionTarget> encTargetList = createEncryptionTargetBodySelectedHeader(listHeaders,true,targetType);
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "symmetric", "");
        msgPolicy.append(encPolicy);
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario21ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
    /*    AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
        suite.setType(AlgorithmSuiteValue.Basic256);
     
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
     
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList, suite);
        addSignKeyBinding(sigPolicy, "derivedkey", "");
        msgPolicy.append(sigPolicy);
     
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
     
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList, suite);
        addEncryptKeyBinding(encPolicy, "derivedkey", "");
        msgPolicy.append(encPolicy);
     */
        List<SignatureTarget> sigTargetList1 =
                createSignatureTargetEndorsingSignature(true);
        SignaturePolicy sigPolicy1 = createSignaturePolicy(sigTargetList1);
        addSignKeyBinding(sigPolicy1, "derivedkey", "");
        msgPolicy.append(sigPolicy1);
        
        return msgPolicy;
    }
    
    public MessagePolicy createScenario21ClientIncoming() throws Exception {
        return(reverse(createScenario21ClientOutgoing()));
    }
    
    
    public MessagePolicy createScenario22ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.Basic256);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        
        /*  List<SignatureTarget> sigTargetList1 =
                createSignatureTargetEndorsingSignature(true);
        SignaturePolicy sigPolicy1 = createSignaturePolicy(sigTargetList1, suite);
        addSignKeyBinding(sigPolicy1, "derivedkey", "");
        msgPolicy.append(sigPolicy1);
         */
        
        return msgPolicy;
    }
    
    
    public MessagePolicy createScenario22ClientIncoming() throws Exception {
        return(reverse(createScenario22ClientOutgoing()));
    }
    
    
    public MessagePolicy createScenario23ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.Basic256);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(sigPolicy);
        addToSPTList(tp,sigPolicy);
        
        List<EncryptionTarget> encTargetList =
                createEncryptionTargetBody(true);
        
        
        EncryptionPolicy encPolicy =
                createEncryptionPolicy(encTargetList);
        addEncryptKeyBinding(encPolicy, "derivedkey", "");
        msgPolicy.append(encPolicy);
        
        
        
        return msgPolicy;
    }
    
    
    public MessagePolicy createScenario23ClientIncoming() throws Exception {
        return(reverse(createScenario23ClientOutgoing()));
    }
    
    
    
    public MessagePolicy createScenario24ClientOutgoing() throws Exception {
        MessagePolicy msgPolicy = new MessagePolicy();
        
        TimestampPolicy tp = new TimestampPolicy();
        msgPolicy.append(tp);
        
        List<SignatureTarget> sigTargetList =
                createSignatureTargetBodyAllHeader(true);
        
//        AlgorithmSuite suite = new com.sun.xml.ws.security.impl.policy.AlgorithmSuite();
//        suite.setType(AlgorithmSuiteValue.Basic256);
        
        SignaturePolicy sigPolicy =
                createSignaturePolicy(sigTargetList);
        addSignKeyBinding(sigPolicy, "derivedkey", "Thumbprint");
        msgPolicy.append(sigPolicy);
        
        addToSPTList(tp,sigPolicy);
        
        
        
        return msgPolicy;
    }
    
    
    public MessagePolicy createScenario24ClientIncoming() throws Exception {
        return(reverse(createScenario24ClientOutgoing()));
    }
    
    
}
