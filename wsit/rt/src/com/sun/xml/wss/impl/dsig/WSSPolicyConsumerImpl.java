/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * WSSPolicyConsumerImpl.java
 *
 * Created on January 18, 2005, 1:50 PM
 */

package com.sun.xml.wss.impl.dsig;

import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.util.NodeListImpl;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.Parameter;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;

import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;

import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPException;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import javax.xml.xpath.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;


/**
 *
 * @author K.venugopal@sun.com
 */

/*
 * WSSPolicyConsumerImpl is to be used as a helper class to construct JSR105 objects from signaturePolicy.
 * To see if document templates are more efficient
 */
public class WSSPolicyConsumerImpl {
    private static Logger logger = Logger.getLogger(LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE,
            LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE);
    public static final String defaultJSR105Provider = "org.jcp.xml.dsig.internal.dom.XMLDSigRI";
    private String providerName = null;
    private String pMT = null;
    private static volatile WSSPolicyConsumerImpl wpcInstance = null;
    private URIDereferencer externalURIResolver = null;
    private Provider provider = null;
    /** Creates a new instance of WSSPolicyConsumerImpl */
    private WSSPolicyConsumerImpl() { //for now.
        providerName = System.getProperty("jsr105Provider", defaultJSR105Provider);
        pMT = System.getProperty("jsr105MechanismType","DOM");
        /*
        try{
            provider = (Provider) Class.forName(providerName).newInstance();
        }catch(Exception ex){
            logger.log(Level.SEVERE,"WSS1324.dsig.factory",ex);
        }
        */
        try{
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            ClassLoader loader = null;
            if(tccl == null){
                loader = this.getClass().getClassLoader();
            }else{
                loader = tccl;
            }
            Class providerClass = Class.forName(providerName,true,loader);
            provider = (Provider) providerClass.newInstance();
        }catch(Exception ex){
            logger.log(Level.SEVERE,"WSS1324.dsig.factory",ex);
        }

        if(logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST,"JSR 105 provider is : "+providerName);
            logger.log(Level.FINEST,"JSR 105 provider mechanism is : "+pMT);
        }
        AccessController.doPrivileged(new java.security.PrivilegedAction<Object>() {
            public Object run() {
                Security.addProvider(provider);
                Security.addProvider(new WSSProvider());
                return null;
            }
        });
    }
    
    /**
     * @return instance of WSSPolicyConsumerImpl
     */
    public static WSSPolicyConsumerImpl getInstance(){
        if(wpcInstance == null){
            synchronized(WSSPolicyConsumerImpl.class){
                if(wpcInstance == null){
                    wpcInstance = new WSSPolicyConsumerImpl();
                }
            }
        }
        return wpcInstance;
    }
    
    /**
     *
     * @return
     * @throws PolicyGenerationException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws XWSSecurityException
     */
    public SignedInfo constructSignedInfo(FilterProcessingContext fpContext)throws
            PolicyGenerationException,NoSuchAlgorithmException,InvalidAlgorithmParameterException,XWSSecurityException {
        
        if(PolicyTypeUtil.signaturePolicy(fpContext.getSecurityPolicy())) {
            SignedInfo signInfo = generateSignedInfo(fpContext);
            return signInfo;
        }
        return null;
    }
    
    /**
     *
     * @param signInfo
     * @param keyInfo
     * @return XMLSignature
     */
    public XMLSignature constructSignature(SignedInfo signInfo,KeyInfo keyInfo){
        return getSignatureFactory().newXMLSignature(signInfo,keyInfo);
    }

    /**
     *
     * @param signInfo
     * @param keyInfo
     * @param id
     * @return XMLSignature
     */
    public XMLSignature constructSignature(SignedInfo signInfo,KeyInfo keyInfo, String id){
        return getSignatureFactory().newXMLSignature(signInfo,keyInfo, null, id, null);
    }
    
    /**
     *
     * @param signaturePolicy
     * @param reference
     * @throws PolicyGenerationException
     * @throws SOAPException
     * @throws XWSSecurityException
     * @return KeyInfo
     */
    public KeyInfo constructKeyInfo(MLSPolicy signaturePolicy,SecurityTokenReference reference) throws PolicyGenerationException,SOAPException,XWSSecurityException {
        
        if(PolicyTypeUtil.signaturePolicy(signaturePolicy)) {
            //SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding();
            //WSSPolicy keyBinding =(WSSPolicy) signaturePolicy.getKeyBinding();
            KeyInfoFactory keyFactory = getKeyInfoFactory();
            
            DOMStructure domKeyInfo = new DOMStructure(reference.getAsSoapElement());
            
            KeyInfo keyInfo = keyFactory.newKeyInfo(Collections.singletonList(domKeyInfo));
            return keyInfo;
            
        }
        
        return null;
        
    }
    
    /**
     *
     * @param signaturePolicy
     * @param KeyName
     * @throws PolicyGenerationException
     * @throws SOAPException
     * @throws XWSSecurityException
     * @return KeyInfo
     */
     @SuppressWarnings("unchecked")
    public KeyInfo constructKeyInfo(MLSPolicy signaturePolicy,String KeyName) throws PolicyGenerationException,SOAPException,XWSSecurityException {
        
        if(PolicyTypeUtil.signaturePolicy(signaturePolicy)) {
            //SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding();
            //WSSPolicy keyBinding =(WSSPolicy) signaturePolicy.getKeyBinding();
            KeyInfoFactory keyFactory = getKeyInfoFactory();
            javax.xml.crypto.dsig.keyinfo.KeyName keyName = keyFactory.newKeyName(KeyName);
            java.util.List list = new java.util.ArrayList();
            list.add(keyName);
            
            KeyInfo keyInfo = keyFactory.newKeyInfo(list);
            
            return keyInfo;
            
        }
        
        return null;
        
    }
    
    /**
     *
     * @return XMLSignatureFactory
     */
    public XMLSignatureFactory getSignatureFactory() {
        try {
            
            return XMLSignatureFactory.getInstance("DOM",provider);
            //XMLSignatureFactory.getInstance (pMT,providerName);
        }catch(Exception ex) {
            logger.log(Level.SEVERE,"WSS1324.dsig.factory",ex);
            throw new RuntimeException(ex);
        }
    }
    
    /**
     *
     * @return KeyInfoFactory
     */
    public KeyInfoFactory getKeyInfoFactory() {
        try {
            return getSignatureFactory().getKeyInfoFactory();
        }catch(Exception ex) {
            logger.log(Level.SEVERE,"WSS1323.dsig.keyinfo.factory",ex);
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * @param signedInfo
     * @return SignaturePolicy
     */
    public SignaturePolicy constructSignaturePolicy(SignedInfo signedInfo, boolean isBSP){
        SignaturePolicy policy = new SignaturePolicy();
        constructSignaturePolicy(signedInfo,isBSP,policy);
        return policy;
    }
    
    public void constructSignaturePolicy(SignedInfo signedInfo, boolean isBSP,SignaturePolicy policy){
        List referencesList = signedInfo.getReferences();
        //SignatureMethod sm = signedInfo.getSignatureMethod();
        CanonicalizationMethod cm = signedInfo.getCanonicalizationMethod();
        
        policy.isBSP(isBSP);
        SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding )policy.getFeatureBinding();
        featureBinding.setCanonicalizationAlgorithm(cm.getAlgorithm());
        Iterator itr = referencesList.iterator();
        while(itr.hasNext()){
            Reference ref = (Reference) itr.next();
            SignatureTarget.Transform transform = getSignatureTransform(ref);
            SignatureTarget target = new SignatureTarget();
            target.isBSP(isBSP);
            if(transform != null){
                target.addTransform(transform);
            }
            target.setDigestAlgorithm(ref.getDigestMethod().getAlgorithm());
			if(ref.getURI().length() >0){
               target.setValue(SecurableSoapMessage.getIdFromFragmentRef(ref.getURI()));
			}else{
               target.setValue(ref.getURI());
			}
            target.setType(SignatureTarget.TARGET_TYPE_VALUE_URI);
            featureBinding.addTargetBinding(target);
        }
    }
    
    public void constructSignaturePolicy(SignedInfo signedInfo, SignaturePolicy policy,
            SecurableSoapMessage secMsg) throws XWSSecurityException{
        List referencesList = signedInfo.getReferences();
        //SignatureMethod sm = signedInfo.getSignatureMethod();
        CanonicalizationMethod cm = signedInfo.getCanonicalizationMethod();
        
        //policy.isBSP(isBSP);
        SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding )policy.getFeatureBinding();
        featureBinding.setCanonicalizationAlgorithm(cm.getAlgorithm());
        Iterator itr = referencesList.iterator();
        while(itr.hasNext()){
            Reference ref = (Reference) itr.next();
            SignatureTarget.Transform transform = getSignatureTransform(ref);
            SignatureTarget target = new SignatureTarget();
            //target.isBSP(isBSP);
            if(transform != null){
                target.addTransform(transform);
            }
            target.setDigestAlgorithm(ref.getDigestMethod().getAlgorithm());
            if(ref.getURI().length() >0){
                String Id = SecurableSoapMessage.getIdFromFragmentRef(ref.getURI());
                //SOAPElement se = secMsg.getElementByWsuId(Id);
                SOAPElement se = (SOAPElement) secMsg.getElementById(Id);
                if(se != null){
                    if(se.getNamespaceURI().equals(MessageConstants.WSSE_NS) ||
                            se.getNamespaceURI().equals(MessageConstants.WSSE11_NS) ||
                            se.getNamespaceURI().equals(MessageConstants.WSSC_NS) ||
                            se.getNamespaceURI().equals(MessageConstants.WSU_NS)){
                        target.setValue("#" + Id);
                        target.setType(SignatureTarget.TARGET_TYPE_VALUE_URI);
                        
                    } else{
                        QName qname = new QName(se.getNamespaceURI(), se.getLocalName());
                        target.setQName(qname);
                        target.setType(SignatureTarget.TARGET_TYPE_VALUE_QNAME);
                    }
                } else{
                    logger.log(Level.SEVERE, "WSS1376.failed.verify.policy.noElementbyID");
                    throw new XWSSecurityException("Policy verification for Signature failed: Element with Id: " + Id
                            + "not found in message" );
                }
	    }
            
            featureBinding.addTargetBinding(target);
        }
    }
    
    /**
     *
     * @param reference
     * @return Transform
     */
    public SignatureTarget.Transform getSignatureTransform(Reference reference ){
        List transformList = reference.getTransforms();
        Iterator transformItr = transformList.iterator();
        SignatureTarget.Transform transform = null;
        while(transformItr.hasNext()){
            Transform trObj = (Transform)transformItr.next();
            String algorithm = trObj.getAlgorithm();
            transform = new SignatureTarget.Transform();
            transform.setTransform(algorithm);
            AlgorithmParameterSpec paramSpec = trObj.getParameterSpec();
            // ArrayList paramList = new HashMap();
            //  addCanonicalizationParams(paramSpec,paramList);
            transform.setAlgorithmParameters(paramSpec);
        }
        return transform;
    }
    
    /**
     *
     * @param algoSpec
     * @param paramList
     */
     @SuppressWarnings("unchecked")
    public void addCanonicalizationParams(AlgorithmParameterSpec algoSpec,HashMap paramList){
        //TODO::FixMe:  Fill this appropriately.
        if(algoSpec instanceof XPathFilterParameterSpec){
            XPathFilterParameterSpec spec = (XPathFilterParameterSpec)algoSpec;
            paramList.put("XPATH",spec.getXPath());
        }else if(algoSpec instanceof XPathFilter2ParameterSpec){
            XPathFilter2ParameterSpec spec = (XPathFilter2ParameterSpec)algoSpec;
            paramList.put("XPATH2",spec.getXPathList());
        }
    }
    
    private SignedInfo generateSignedInfo(FilterProcessingContext fpContext)
    throws PolicyGenerationException,NoSuchAlgorithmException,InvalidAlgorithmParameterException ,XWSSecurityException{
        SignaturePolicy signaturePolicy = (SignaturePolicy) fpContext.getSecurityPolicy();
        SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding();
        MLSPolicy keyBinding = signaturePolicy.getKeyBinding();
        XMLSignatureFactory signatureFactory = getSignatureFactory();
        SecurableSoapMessage secureMessage = fpContext.getSecurableSoapMessage();
        String canonicalAlgo = featureBinding.getCanonicalizationAlgorithm();
        boolean disableInclusivePrefix = featureBinding.getDisableInclusivePrefix();
        //String digestAlgo = featureBinding.getDigestAlgorithm();
        ArrayList targetList = featureBinding.getTargetBindings();
        String keyAlgo = null;
        String algo = fpContext.getAlgorithmSuite().getSignatureAlgorithm();

        keyAlgo = SecurityUtil.getKeyAlgo(algo);
                
        if(PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
            AuthenticationTokenPolicy.X509CertificateBinding certificateBinding =
                    (AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
            if (!"".equals(certificateBinding.getKeyAlgorithm())) {
                keyAlgo = certificateBinding.getKeyAlgorithm();
            }
        } else if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {
            AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding =
                    (AuthenticationTokenPolicy.SAMLAssertionBinding)keyBinding;
            if (!"".equals(samlBinding.getKeyAlgorithm())) {
                keyAlgo = samlBinding.getKeyAlgorithm();
            }
        }else if(PolicyTypeUtil.symmetricKeyBinding(keyBinding)){
            SymmetricKeyBinding symmetricKeybinding = (SymmetricKeyBinding)keyBinding;
            if (!"".equals(symmetricKeybinding.getKeyAlgorithm())) {
                keyAlgo = symmetricKeybinding.getKeyAlgorithm();
            } else {
                keyAlgo = MessageConstants.HMAC_SHA1_SIGMETHOD;
            }     
        } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(keyBinding)) {
            keyAlgo = MessageConstants.HMAC_SHA1_SIGMETHOD;
        } else if (PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)) {
           keyAlgo = MessageConstants.HMAC_SHA1_SIGMETHOD;
           if(PolicyTypeUtil.issuedTokenKeyBinding(((DerivedTokenKeyBinding)keyBinding).getOriginalKeyBinding())){
               if(fpContext.getTrustContext().getProofKey() == null){
                   keyAlgo = MessageConstants.RSA_SHA1_SIGMETHOD;
               }                           
           }
       } else if (PolicyTypeUtil.issuedTokenKeyBinding(keyBinding)) {
           //TODO: verify if this is always correct
           keyAlgo = MessageConstants.HMAC_SHA1_SIGMETHOD;
           if(fpContext.getTrustContext().getProofKey() == null){
               keyAlgo = MessageConstants.RSA_SHA1_SIGMETHOD;
           }
       } else { 
            logger.log(Level.SEVERE, "WSS1335.unsupported.keybinding.signaturepolicy");
            throw new XWSSecurityException("Unsupported KeyBinding for SignaturePolicy");
       }
        
        C14NMethodParameterSpec spec = null;
        if (MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS.equalsIgnoreCase(canonicalAlgo)) {
            //List inc = getInclusiveNamespacePrefixes(secureMessage.findSecurityHeader(), false);
            //spec = new ExcC14NParameterSpec(inc);
            //NOTE: looking at BSP flag on sending side just for
            //ExC14N parameterList. Because XWSS11(xmlsec.jar) cannot
            //process the PrefixList, thereby breaking BC
            if (featureBinding.isBSP() || !disableInclusivePrefix) {
                List inc = getInclusiveNamespacePrefixes(secureMessage.findSecurityHeader(), false);
                spec = new ExcC14NParameterSpec(inc);
            } else {
                spec = null;
            }

        }
        CanonicalizationMethod canonicalMethod=
                signatureFactory.newCanonicalizationMethod(canonicalAlgo,spec);
        
        SignatureMethod signatureMethod = signatureFactory.newSignatureMethod(keyAlgo, null);
        //Note : Signature algorithm parameters null for now , fix me.
        SignedInfo signedInfo = signatureFactory.newSignedInfo(canonicalMethod,signatureMethod,
                generateReferenceList(targetList,signatureFactory,secureMessage,fpContext,false, featureBinding.isEndorsingSignature()),null);
        //Note : Id is now null , check ?,
        return signedInfo;
    }
   
     /*
      * Calculate the list of inclusive namespace prefixes.
      * Inclusive Prefixes are those that are not not visibly utilized.
      */
     @SuppressWarnings("unchecked")
    public static List getInclusiveNamespacePrefixes(Element target,
            boolean excludeVisiblePrefixes) {
        ArrayList result = new ArrayList();
        NamedNodeMap attributes;
        Node attribute;
        Node parent = target;
        
        while (!(parent instanceof Document)) {
            attributes = parent.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                attribute = attributes.item(i);
                if (attribute.getNamespaceURI() != null &&
                        attribute.getNamespaceURI().equals(MessageConstants.NAMESPACES_NS)) {
                    result.add(attribute.getLocalName());
                }
            }
            parent = parent.getParentNode();
        }
         /*
        if (excludeVisiblePrefixes == true) {
            attributes = target.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                attribute = attributes.item(i);
                if (attribute.getNamespaceURI() != null &&
attribute.getNamespaceURI().equals(MessageConstants.NAMESPACES_NS)) {
                        result.remove(attribute.getLocalName());
                }
                if (attribute.getPrefix() != null) {
                      result.remove(attribute.getLocalName());
                }
            }
          
            if (target.getPrefix() == null) {
                result.remove(MessageConstants.XMLNS_TAG);
            } else {
                result.remove(target.getPrefix());
            }
        }
          */
        return result;
    }
    
    
     /*
      * Calculate the list of visible namespace prefixes in target.
      * to be included in ds:Reference.
      */
    public static List getReferenceNamespacePrefixes(Node target) {
        ArrayList result = new ArrayList();
        
        traverseSubtree(target , result);
        
        return result;
    }
     @SuppressWarnings("unchecked")
    private static void traverseSubtree(Node node, List result) {
        SOAPElement element = (SOAPElement) node;
        Iterator visible =  element.getVisibleNamespacePrefixes();
        
        while (visible.hasNext()) {
            String  prefix = (String)visible.next();
            if (!result.contains(prefix)) {
                result.add(prefix);
            }
        }
        Iterator children = element.getChildElements();
        
        while (children.hasNext()) {
            Node child = (Node)children.next();
            if (!(child instanceof javax.xml.soap.Text)) {
                traverseSubtree(child, result);
            }
        }
    }
    
    public List generateReferenceList(List targetList,SecurableSoapMessage secureMessage,FilterProcessingContext fpContext,
            boolean verify, boolean isEndorsing)
    throws PolicyGenerationException,NoSuchAlgorithmException,InvalidAlgorithmParameterException,XWSSecurityException {
        XMLSignatureFactory factory = getSignatureFactory();
        return generateReferenceList(targetList,factory,secureMessage,fpContext,verify, isEndorsing);
    }
    
    //Time to refactor this method
    //bloated toomuch.
     @SuppressWarnings("unchecked")
    private List generateReferenceList(List targetList,XMLSignatureFactory signatureFactory,
            SecurableSoapMessage secureMessage,FilterProcessingContext fpContext,boolean verify, 
            boolean isEndorsing)
            throws PolicyGenerationException,NoSuchAlgorithmException,InvalidAlgorithmParameterException,XWSSecurityException {
        
        SignaturePolicy signaturePolicy = (SignaturePolicy) fpContext.getSecurityPolicy();
        SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding();
        ListIterator iterator = targetList.listIterator();
        ArrayList references = new ArrayList();
        if(logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Number of Targets is"+targetList.size());
        }
        
        while(iterator.hasNext()) {
            
            SignatureTarget signatureTarget = (SignatureTarget)iterator.next();
            String digestAlgo = signatureTarget.getDigestAlgorithm();
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST, "Digest Algorithm is "+digestAlgo);
                logger.log(Level.FINEST, "Targets is"+signatureTarget.getValue());
            }
            DigestMethod digestMethod =null;
            try{
                digestMethod = signatureFactory.newDigestMethod(digestAlgo, null);
            }catch(Exception ex){
                logger.log(Level.SEVERE,"WSS1301.invalid.digest.algo",digestAlgo);
                throw new XWSSecurityException(ex.getMessage());
            }
            
            boolean exclTransformToBeAdded = false;
            ArrayList transforms = signatureTarget.getTransforms();
            ListIterator transformIterator = transforms.listIterator();
            ArrayList transformList = new ArrayList(2);
            boolean disableInclusivePrefix = false;
            while(transformIterator.hasNext()) {
                SignatureTarget.Transform transformInfo = (SignatureTarget.Transform)transformIterator.next();
                String transformAlgo = transformInfo.getTransform();
                Transform transform = null;
                
                if(logger.isLoggable(Level.FINEST))
                    logger.log(Level.FINEST, "Transform Algorithm is "+transformAlgo);
                if(transformAlgo == Transform.XPATH || transformAlgo.equals(Transform.XPATH)){
                    TransformParameterSpec spec =(TransformParameterSpec) transformInfo.getAlgorithmParameters();
                    //XPathFilterParameterSpec spec = null;
                    
                    if(spec == null){
                        logger.log(Level.SEVERE, "WSS1367.illegal.xpath");
                        throw new XWSSecurityException("XPATH parameters cannot be null");
                        
                    }
                    //XPATH2,XSLTC , ..
                    transform = signatureFactory.newTransform(transformAlgo,spec);
                    
                }else if(transformAlgo == Transform.XPATH2 || transformAlgo.equals(Transform.XPATH2)){
                    TransformParameterSpec transformParams = (TransformParameterSpec)transformInfo.getAlgorithmParameters();
                    transform= signatureFactory.newTransform(transformAlgo,transformParams);
                }else if (transformAlgo == MessageConstants.STR_TRANSFORM_URI || transformAlgo.equals(MessageConstants.STR_TRANSFORM_URI)){
                    Parameter transformParams =(Parameter) transformInfo.getAlgorithmParameters();
                    String  algo = null;
                    if(transformParams.getParamName().equals("CanonicalizationMethod")){
                        algo = transformParams.getParamValue();
                    }
                    if(algo == null){
                        logger.log(Level.SEVERE, "WSS1368.illegal.str.canoncalization");
                        throw new XWSSecurityException("STR Transform must have a"+
                                "canonicalization method specified");
                    }
                    if(logger.isLoggable(Level.FINEST)){
                        logger.log(Level.FINEST, "CanonicalizationMethod is " + algo);
                    }
                    CanonicalizationMethod cm = null;
                    C14NMethodParameterSpec spec = null;
                    try{
                        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                        Element tp = doc.createElementNS(MessageConstants.WSSE_NS, "wsse:TransformationParameters");
                        Element cem = doc.createElementNS(MessageConstants.DSIG_NS, "ds:CanonicalizationMethod");
                        tp.appendChild(cem);
                        cem.setAttribute("Algorithm",algo);
                        doc.appendChild(tp);
                        XMLStructure transformSpec = new DOMStructure(tp);
                        transform = signatureFactory.newTransform(transformAlgo,transformSpec);
                    }catch(Exception ex){
                        logger.log(Level.SEVERE,"WSS1300.dsig.transform_param.error",ex);
                        throw new XWSSecurityException(ex.getMessage());
                    }
                } else if (MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS.equalsIgnoreCase(transformAlgo)) {
                    // should be there by default...
                    // As per R 5412, last child of ds:Transforms must be either excl-c14n, or attachment-content only or attachment-complete transform
                    exclTransformToBeAdded = true;
                    disableInclusivePrefix = transformInfo.getDisableInclusivePrefix();
                } else {
                    //                    XMLStructure transformSpec = null;
                    //                    transform = signatureFactory.newTransform(transformAlgo,transformSpec);
                    //                    Workaround for JSR105 bug
                    transform = signatureFactory.newTransform(transformAlgo,(TransformParameterSpec) null);
                }
                if (!MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS.equalsIgnoreCase(transformAlgo)) {
                    // will add c14n transform in the end; later
                    transformList.add(transform);
                }
            }
            String targetURI = "";
            String signatureType = signatureTarget.getType();
            SOAPMessage msg = secureMessage.getSOAPMessage();
            boolean headersOnly = signatureTarget.isSOAPHeadersOnly();
            if(signatureType.equals(SignatureTarget.TARGET_TYPE_VALUE_QNAME) || signatureType.equals(SignatureTarget.TARGET_TYPE_VALUE_XPATH)){
                
                String expr = null;
                NodeList nodes  = null;
                if( signatureType == SignatureTarget.TARGET_TYPE_VALUE_QNAME){
                    String targetValue = signatureTarget.getValue();
                    boolean optimized = false;
                    if(fpContext.getConfigType() == MessageConstants.SIGN_BODY || fpContext.getConfigType() == MessageConstants.SIGN_ENCRYPT_BODY){
                        optimized = true;
                    }
//                    if(targetValue.equals(SignatureTarget.BODY) && optimized){
//                        Reference ref =  new JAXWSDigestProcessor(fpContext,signatureTarget , digestMethod, signatureFactory).handleJAXWSSOAPBody();
//                        references.add(ref);
//                        continue;
//                    }
                    
                    
                    if(targetValue.equals(SignatureTarget.BODY )){
                        try{
                            
                            final SOAPElement se = msg.getSOAPBody();
                            
                            nodes = new NodeList(){
                                Node node = se;
                                public int getLength(){
                                    if(node == null){
                                        return 0;
                                    }else{
                                        return 1;
                                    }
                                }
                                public Node item(int num){
                                    if(num == 0){
                                        return node;
                                    }else{
                                        return null;
                                    }
                                }
                            };
                        }catch(SOAPException se){
                            logger.log(Level.SEVERE, "WSS1369.unable.get.signatureTarget.by.URI");
                            throw new XWSSecurityException("SignatureTarget with URI "+targetValue+
                                   " is not in the message");
                             //logger.log(
                             //   Level.WARNING, "Signed Part with QName " + targetValue + " is not in the message");
                             //continue;
                        }
                    }else{
                        
                        QName name = QName.valueOf(targetValue);
                        if(!headersOnly){
                            if("".equals(name.getNamespaceURI())){
                                nodes =msg.getSOAPPart().getElementsByTagName(name.getLocalPart());
                            }else{
                                if(!"".equals(name.getLocalPart()))
                                    nodes = msg.getSOAPPart().getElementsByTagNameNS(name.getNamespaceURI(), name.getLocalPart());
                                else
                                    nodes = msg.getSOAPPart().getElementsByTagNameNS(name.getNamespaceURI(), "*");                            
                            }
                        } else{
                            //process headers of soap message
                            try{
                                nodes = new NodeListImpl();
                                NodeList hdrChilds = msg.getSOAPHeader().getChildNodes();
                                for(int i = 0; i < hdrChilds.getLength(); i++){
                                    Node child = hdrChilds.item(i);
                                    if(child.getNodeType() ==  Node.ELEMENT_NODE){
                                       if("".equals(name.getNamespaceURI())){
                                           if(name.getLocalPart().equals(child.getLocalName()))
                                               ((NodeListImpl)nodes).add(child);
                                       } else{
                                           // FIXME: Hack to get addressing members from both namespaces, as microsoft uses both of them in a soap message
                                           if(name.getNamespaceURI().equals(MessageConstants.ADDRESSING_MEMBER_SUBMISSION_NAMESPACE) ||
                                                   name.getNamespaceURI().equals(MessageConstants.ADDRESSING_W3C_NAMESPACE)){
                                               if((child.getNamespaceURI().equals(MessageConstants.ADDRESSING_MEMBER_SUBMISSION_NAMESPACE) || 
                                                       child.getNamespaceURI().equals(MessageConstants.ADDRESSING_W3C_NAMESPACE))) {
                                                   if(!"".equals(name.getLocalPart())){
                                                       if(name.getLocalPart().equals(child.getLocalName()))
                                                           ((NodeListImpl)nodes).add(child);
                                                   } else{
                                                       ((NodeListImpl)nodes).add(child);
                                                   }
                                               }
                                           } else{
                                               if(!"".equals(name.getLocalPart())){
                                                   if(name.getNamespaceURI().equals(child.getNamespaceURI()) && 
                                                           name.getLocalPart().equals(child.getLocalName()))
                                                       ((NodeListImpl)nodes).add(child);
                                               } else{
                                                   if(name.getNamespaceURI().equals(child.getNamespaceURI()))
                                                       ((NodeListImpl)nodes).add(child);
                                               }
                                           }
                                       } 
                                    }
                                }
                            } catch (SOAPException se){
                                logger.log(Level.SEVERE, "WSS1370.failed.process.header");
                                throw new XWSSecurityException(se);
                            }
                        }
                    }
                }else{
                    
                    expr = signatureTarget.getValue();
                    
                    try{
                        XPathFactory xpathFactory = XPathFactory.newInstance();
                        XPath xpath = xpathFactory.newXPath();
                        xpath.setNamespaceContext(secureMessage.getNamespaceContext());
                        //              XPathExpression expr = xpath.compile("//*[@wsu:Id]");
                        //XPathExpression expr = xpath.compile("//*");
                        XPathExpression xpathExpr = xpath.compile(expr);
                        if(logger.isLoggable(Level.FINEST)){
                            logger.log(Level.FINEST, "++++++++++++++++++++++++++++++");
                            logger.log(Level.FINEST, "Expr is "+expr);
                            printDocument((Node)secureMessage.getSOAPPart());
                        }
                        nodes = (NodeList)xpathExpr.evaluate((Object)secureMessage.getSOAPPart(),XPathConstants.NODESET);
                    }catch(XPathExpressionException xpe){
                        logger.log(Level.SEVERE,"WSS1371.failed.resolve.XPath"+expr,xpe);
                        throw new XWSSecurityException(xpe);
                    }
                }
                int i=0;
                if(nodes == null || nodes.getLength() <= 0){
                    if(signatureTarget.getEnforce()){
                        logger.log(Level.SEVERE, "WSS1369.unable.get.signatureTarget.by.URI");
                        throw new XWSSecurityException("SignatureTarget with URI "+signatureTarget.getValue()+
                               " is not in the message");
                    } else{
                        continue;
                    }
                    
                     // we dont throw error since WSSecurityPolicy allows this
                     //logger.log(Level.WARNING, "Signed Part with QName/XPath " + signatureTarget.getValue() +
                     //  " is not in the message");
                     //continue;
                }
                if(logger.isLoggable(Level.FINEST)){
                    logger.log(Level.FINEST, "Number of nodes "+nodes.getLength());
                    logger.log(Level.FINEST, "+++++++++++++++END+++++++++++++++");
                }
                HashMap elementCache = null;
                if(fpContext != null ){
                    elementCache = fpContext.getElementCache();
                }
                while(i < nodes.getLength()){
                    if(logger.isLoggable(Level.FINEST))
                        logger.log(Level.FINEST, "Nodes is "+nodes.item(i));
                    Node nodeRef = nodes.item(i++);
                    if(nodeRef.getNodeType() != Node.ELEMENT_NODE) {
                        logger.log (Level.SEVERE, "WSS1371.failed.resolve.XPath");
                        throw new XWSSecurityException(
                                "XPath does not correspond to a DOM Element");
                    }
                    ArrayList clonedTransformList = (ArrayList) transformList.clone();
                    if (exclTransformToBeAdded) {
                        // exc-14-nl must be one of the last transforms under ReferenceList by default.
                        String transformAlgo  = MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS;
                        ExcC14NParameterSpec spec = null;
                        if((featureBinding != null && featureBinding.isBSP()) || !disableInclusivePrefix){
                            spec = new ExcC14NParameterSpec(getReferenceNamespacePrefixes(nodeRef));
                        }
                        Transform transform = signatureFactory.newTransform(transformAlgo,spec);
                        clonedTransformList.add(transform);
                    }
                    boolean w3cElem = false;
                    // Assume only elements with wsu:Id are signed
                    String id = ((Element)nodeRef).getAttributeNS(MessageConstants.WSU_NS, "Id");
                    if(id == null || id.equals("")){
                        if(nodeRef.getNamespaceURI() == MessageConstants.DSIG_NS ||
                                nodeRef.getNamespaceURI() == MessageConstants.XENC_NS){
                            w3cElem = true;
                            id = ((Element)nodeRef).getAttribute("Id");
                        }
                        
                    }
                    
                    if (id == null || id.equals("")) {
                        id = secureMessage.generateId();
                        if(!verify){
                            if(w3cElem){
                                XMLUtil.setIdAttr((Element)nodeRef, id);
                            }else{
                                XMLUtil.setWsuIdAttr((Element)nodeRef, id);
                            }
                        }else{
                            //add to context. dont modify the message.
                            elementCache.put(id, nodeRef);
                        }
                    }
                    
                    if(logger.isLoggable(Level.FINEST))
                        logger.log(Level.FINEST, "SignedInfo val id "+id);
                    
                    targetURI = "#"+id;
                    
                    byte [] digestValue = fpContext.getDigestValue();
                    Reference reference = null;
                    if(!verify && digestValue != null){
                        reference = signatureFactory.newReference(targetURI,digestMethod,clonedTransformList,null,null,digestValue);
                    }else{
                        reference = signatureFactory.newReference(targetURI,digestMethod,clonedTransformList,null,null);
                    }
                    references.add(reference);
                }
                continue;   
            }else if(signatureType ==SignatureTarget.TARGET_TYPE_VALUE_URI){
                targetURI = signatureTarget.getValue();
                
                if(targetURI == null){
                    targetURI="";
                }
                if(targetURI == MessageConstants.PROCESS_ALL_ATTACHMENTS){
                    Iterator itr = secureMessage.getAttachments();
                    if ( !itr.hasNext()) {
                        logger.log(Level.SEVERE, "WSS1372.no.attachmentFound");
                        throw new XWSSecurityException("No attachment present in the message");
                        //logger.log(Level.WARNING, "No Attachment Part present in the message to be secured");
                        //continue;
                    }
                    while(itr.hasNext()){
                        String cid = null;
                        AttachmentPart ap = (AttachmentPart)itr.next();
                        String _cid = ap.getContentId();
                        if (_cid.charAt(0) == '<' && _cid.charAt(_cid.length()-1) == '>'){
                            int lindex = _cid.lastIndexOf('>');
                            int sindex = _cid.indexOf('<');
                            if(lindex < sindex || lindex == sindex){
                                //log error
                                logger.log(Level.SEVERE,"WSS1303.cid_error");
                            }
                            cid = "cid:"+_cid.substring(sindex+1,lindex);
                        }else{
                            cid = "cid:"+_cid;
                        }
                        Reference reference = signatureFactory.newReference(cid,digestMethod,transformList,null,null);
                        references.add(reference);
                    }
                    continue;
                }else{
                    if (exclTransformToBeAdded) {
                        // exc-14-n must be one of the last transforms under ReferenceList by default.
//                        String transformAlgo  = MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS;
//                        ExcC14NParameterSpec spec = null;
//                        Transform transform = signatureFactory.newTransform(transformAlgo,spec);
//                        transformList.add(transform);
                        SOAPElement dataElement = null;
                        if (featureBinding != null && featureBinding.isBSP()) {
                            
//                            try {
                                String _uri = targetURI;
                                if(targetURI.length() > 0 && targetURI.charAt(0)=='#'){
                                    _uri = targetURI.substring(1);
                                }
                                dataElement =(SOAPElement) secureMessage.getElementById(_uri);
//                            } catch (TransformerException te) {
//                                logger.log(Level.SEVERE, "WSS1373.failedto.resolve.elementbyID", te);
//                                throw new XWSSecurityException(te.getMessage(), te);
//                            }
                        }
                        String transformAlgo  = MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS;
                        ExcC14NParameterSpec spec = null;
                        if(dataElement != null && !disableInclusivePrefix){
                            spec =   new ExcC14NParameterSpec(getReferenceNamespacePrefixes(dataElement));
                        }
                        Transform transform = signatureFactory.newTransform(transformAlgo,spec);
                        transformList.add(transform);
                    }
                    if(targetURI.equals(SignatureTarget.ALL_MESSAGE_HEADERS)){
                        SOAPHeader soapHeader=null;
                        try{
                            soapHeader = secureMessage.getSOAPHeader();
                        }catch(SOAPException se) {
                            se.printStackTrace();
                        }
                        NodeList headers = soapHeader.getChildNodes();
                        Reference reference = null;
                        for(int i=0;i<headers.getLength();i++){
                            if(((Node)headers.item(i)).getNodeType() ==  Node.ELEMENT_NODE){
                            Element element = (Element)headers.item(i);
                                if(!("Security".equals(element.getLocalName()) &&
                                    MessageConstants.WSSE_NS.equals(element.getNamespaceURI())) ){
                                    reference = signatureFactory.newReference("#"+generateReferenceID(element, secureMessage),digestMethod,transformList,null,null);
                                    references.add(reference);
                                }
                            }
                        }
                        continue;
                    }
                }
            }
            
            byte [] digestValue = fpContext.getDigestValue();
            Reference reference = null;
            if(!verify && digestValue != null){
                reference = signatureFactory.newReference(targetURI,digestMethod,transformList,null,null,digestValue);
            }else{
                reference = signatureFactory.newReference(targetURI,digestMethod,transformList,null,null);
            }
            
            //Note :: Id is null.
            references.add(reference);
        }

        if (references.isEmpty()) {
            if(logger.isLoggable(Level.WARNING)){
            logger.log(Level.WARNING, "WSS1375.no.signedparts");
            }
        }

        return references;
    }
    
    /**
     *
     * @param Element
     * @param SecurableSoapMessage
     * @return String
     */
    private String generateReferenceID(Element secElement, SecurableSoapMessage securableSoapMessage) {
        String id = secElement.getAttributeNS(MessageConstants.WSU_NS,"Id");
        //((Element)secElement).getAttribute(MessageConstants.WSU_ID_QNAME);
        if (id == null || id.equals("")) {
            try {
                id = securableSoapMessage.generateId();
            } catch(XWSSecurityException xse) {
                xse.printStackTrace();
            }
            XMLUtil.setWsuIdAttr((Element)secElement, id);
        }
        if(logger.isLoggable(Level.FINE)){
            logger.log(Level.FINE, "Element wsu:id attribute is: "+id);
        }
        return id;
    }
    
    public URIDereferencer getDefaultResolver(){
        if(externalURIResolver == null){
            externalURIResolver = getSignatureFactory().getURIDereferencer();
        }
        return  externalURIResolver;
    }
    /**
     *
     * @param node
     */
    public static void printDocument(Node node){
        try{
            if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST, "\n");
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            OutputStream baos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(node), new StreamResult(baos));
            byte[] bytes = ((ByteArrayOutputStream)baos).toByteArray();
            if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST, new String(bytes));
            logger.log(Level.FINEST, "\n");
            }
        }catch(Exception ex){
            logger.log(Level.SEVERE, "WSS1374.failedto.print.document", ex);
            throw new RuntimeException(ex);
        }
    }
    
//    public class JAXWSDigestProcessor {
//        
//        FilterProcessingContext fpContext= null;
//        SignatureTarget target = null;
//        DigestMethod dm  = null;
//        XMLSignatureFactory signatureFactory = null;
//        String targetURI = "";
//        SOAPMessage msg = null;
//        JAXWSDigestProcessor(FilterProcessingContext fc , SignatureTarget st, DigestMethod dm,XMLSignatureFactory sf){
//            this.fpContext = fc;
//            this.target = st;
//            this.dm = dm;
//            this.signatureFactory = sf;
//            this.msg = fpContext.getSOAPMessage();
//        }
//        
//        public Reference handleJAXWSSOAPBody()throws XWSSecurityException{
//            try{
//                
//                SOAPBody body = ((com.sun.xml.messaging.saaj.soap.ExpressMessage)msg).getEMBody();
//                
//                int optCase = fpContext.getConfigType();
//                switch(optCase){
//                    case 1 : {
//                        signBody(body);
//                        break;
//                    }
//                    case 2 : {
//                        signEncryptBody(body);
//                        break;
//                    }
//                    default :
//                        throw new  XWSSecurityException("Invalid configuration option");
//                }
//                ArrayList transforms = target.getTransforms();
//                ListIterator transformIterator = transforms.listIterator();
//                ArrayList transformList = new ArrayList(1);
//                String transformAlgo  = MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS;
//                String algo = dm.getAlgorithm();
//                MessageDigest digester = null;
//                if(algo == DigestMethod.SHA1){
//                    digester = MessageDigest.getInstance("SHA");
//                }else{
//                    //throw error
//                }
//                
//                
//                com.sun.xml.jaxws.JAXWSMessage jxm = ((com.sun.xml.messaging.saaj.soap.ExpressMessage)msg).getJAXWSMessage();
//                byte[] canonData = jxm.getCanonicalizedBody();
//                int len = jxm.getCBLength();
//                if(MessageConstants.SIGN_ENCRYPT_BODY == optCase ){
//                    byte [] data = null;
//                    jxm.setCanonicalizedBody(data,0);
//                    
//                }
//                digester.update(canonData,0,len);
//                byte[] digestValue = digester.digest();
//                
//                ExcC14NParameterSpec spec = null;
//                
//                //new ExcC14NParameterSpec(getReferenceNamespacePrefixes(nodeRef));
//                
//                Transform transform = signatureFactory.newTransform(transformAlgo,spec);
//                transformList.add(transform);
//                Reference reference = signatureFactory.newReference(targetURI,dm,transformList,null,null,digestValue);
//                return reference;
//                
//                
//            }catch(Exception ex){
//                throw new RuntimeException(ex);
//            }
//        }
//        
//        private void signBody(SOAPBody body)throws IOException,XMLStreamException,XWSSecurityException,SOAPException{
//            com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl canonicalizer = new com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl();
//            UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
//            canonicalizer.setStream(baos);
//            com.sun.xml.jaxws.JAXWSMessage jxm = ((com.sun.xml.messaging.saaj.soap.ExpressMessage)msg).getJAXWSMessage();
//            cBodyTag(canonicalizer,body);
//            if(!jxm.isBodyUsed()){
//                jxm.writeJAXWSBody(canonicalizer);
//            }else{
//                //log error
//            }
//            canonicalizer.writeEndDocument();
//            byte [] data = baos.getBytes();
//            jxm.setCanonicalizedBody(data,baos.getLength());
//            
//        }
//        
//        private void signEncryptBody(SOAPBody body)throws IOException,XMLStreamException,XWSSecurityException,SOAPException{
//            com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl canonicalizer = new com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl();
//            UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
//            canonicalizer.setStream(baos);
//            com.sun.xml.jaxws.JAXWSMessage jxm = ((com.sun.xml.messaging.saaj.soap.ExpressMessage)msg).getJAXWSMessage();
//            cBodyTag(canonicalizer,body);
//            canonicalizer.writeCharacters("");
//            byte [] bodyTag = baos.toByteArray();
//            baos.reset();
//            
//            if(!jxm.isBodyUsed()){
//                jxm.writeJAXWSBody(canonicalizer);
//                // jxm.setBodyUsed(true);
//            }else{
//                //log error
//            }
//            byte [] bodyContent = baos.toByteArray();
//            baos.reset();
//            //System.out.println("Body Content "+new String(bodyContent));
//            canonicalizer.writeEndDocument();
//            byte [] endTag = baos.getBytes();
//            byte [] canonicalizedData = new byte[bodyTag.length+bodyContent.length+baos.getLength()];
//            System.arraycopy(bodyTag,0,canonicalizedData,0,bodyTag.length);
//            System.arraycopy(bodyContent,0,canonicalizedData,bodyTag.length,bodyContent.length);
//            System.arraycopy(endTag,0,canonicalizedData,bodyTag.length+bodyContent.length,baos.getLength());
//            jxm.setCanonicalizedBody(canonicalizedData,canonicalizedData.length);
//            fpContext.setCanonicalizedData(bodyContent);
//            //  System.out.println("Canonicalized Data is "+new String(bodyContent  ));
//        }
//        
//        private void cBodyTag(XMLStreamWriter canonicalizer,SOAPBody body)throws IOException,XMLStreamException,XWSSecurityException,SOAPException{
//            String id = body.getAttributeNS(MessageConstants.WSU_NS, "Id");
//            
//            if(id == null || id.length() == 0){
//                if(body.getNamespaceURI() == MessageConstants.DSIG_NS ||
//                        body.getNamespaceURI() == MessageConstants.XENC_NS){
//                    
//                    id = (body).getAttribute("Id");
//                }
//            }
//            
//            if (id == null || id.equals("")) {
//                id = fpContext.getSecurableSoapMessage().generateId();
//                XMLUtil.setWsuIdAttr(body, id);
//            }
//            targetURI = "#"+id;
//            NamedNodeMap nodes =  body.getAttributes();
//            Vector attrs = new Vector();
//            Vector attrsNS = new Vector();
//            
//            for(int i=0;i< nodes.getLength();i++){
//                Attr attr = (Attr)nodes.item(i);
//                //System.out.println("URI"+attr.getNamespaceURI());
//                //System.out.println("value"+attr.getNodeValue());
//                if(attr.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")){
//                    attrsNS.add(attr);
//                } else{
//                    attrs.add(attr);
//                }
//            }
//            
//            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
//            NamedNodeMap parentAttrs = env.getAttributes();
//            
//            
//            for(int i=0;i< parentAttrs.getLength();i++){
//                Attr attr = (Attr)parentAttrs.item(i);
//                //System.out.println("URI"+attr.getNamespaceURI());
//                //System.out.println("value"+attr.getNodeValue());
//                if(attr.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")){
//                    canonicalizer.writeNamespace(attr.getLocalName(),attr.getValue());
//                }
//            }
//            
//            canonicalizer.writeStartDocument();
//            canonicalizer.writeStartElement(body.getPrefix(),body.getLocalName(),body.getNamespaceURI());
//            
//            
//            Iterator nsItr = attrsNS.iterator();
//            while(nsItr.hasNext()){
//                Attr attr = (Attr)nsItr.next();
//                canonicalizer.writeNamespace(attr.getLocalName(),attr.getValue());
//            }
//            Iterator attrItr = attrs.iterator();
//            while(attrItr.hasNext()){
//                Attr attr = (Attr)attrItr.next();
//                canonicalizer.writeAttribute(attr.getPrefix(),attr.getNamespaceURI(),attr.getLocalName(),attr.getValue());
//            }
//        }
//    }
//    
    /**
     * Provider to register STRTransform,Attachment-Complete and Attachment-ContentOnly Transforms
     * into XMLDSIG implementation.
     *
     */
    public static final class WSSProvider extends Provider {
        private static final String INFO = "WSS_TRANSFORM " +
                "(DOM WSS_TRANSFORM_PROVIDER)";
         @SuppressWarnings("unchecked")
        public WSSProvider() {
            /* We are the XMLDSig provider */
            super("WSS_TRANSFORM", 1.0, INFO);
            final Map map = new HashMap();
            map.put("TransformService."+MessageConstants.ATTACHMENT_COMPLETE_TRANSFORM_URI, "com.sun.xml.wss.impl.transform.ACTransform");
            map.put("TransformService."+MessageConstants.ATTACHMENT_COMPLETE_TRANSFORM_URI+" MechanismType", "DOM");
            
            map.put("TransformService."+MessageConstants.STR_TRANSFORM_URI, "com.sun.xml.wss.impl.transform.DOMSTRTransform");
            map.put("TransformService."+MessageConstants.STR_TRANSFORM_URI +" MechanismType", "DOM");
            map.put("TransformService."+MessageConstants.ATTACHMENT_CONTENT_ONLY_TRANSFORM_URI, "com.sun.xml.wss.impl.transform.ACOTransform");
            map.put("TransformService."+MessageConstants.ATTACHMENT_CONTENT_ONLY_TRANSFORM_URI+" MechanismType", "DOM");
            
            putAll(map);
        }
    }
}
