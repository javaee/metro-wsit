/*
 * SignatureElementFactory.java
 *
 * Created on August 21, 2006, 12:36 PM
 */

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

package com.sun.xml.ws.security.opt.impl.dsig;

import com.sun.xml.security.core.dsig.InclusiveNamespacesType;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.impl.message.SOAPBody;
import com.sun.xml.ws.security.opt.impl.message.SecuredMessage;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.HashMap;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.ws.security.secext10.TransformationParametersType;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBSignatureFactory;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.Parameter;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBStructure;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;

import com.sun.xml.ws.api.message.Header;

import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import com.sun.xml.ws.security.opt.impl.dsig.ExcC14NParameterSpec;
import com.sun.xml.wss.impl.policy.mls.LazyKeyBinding;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;


/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class SignatureElementFactory {
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    /** Creates a new instance of SignatureElementFactory */
    public SignatureElementFactory() {
        
    }
    
    /**
     *
     * @param signInfo
     * @param keyInfo
     * @param id
     * @return XMLSignature
     */
    public XMLSignature constructSignature(SignedInfo signInfo,javax.xml.crypto.dsig.keyinfo.KeyInfo keyInfo, final String id){
        return getSignatureFactory().newXMLSignature(signInfo,keyInfo, null, id, null);
    }
    
    /**
     *
     * @param signInfo
     * @param keyInfo
     * @return XMLSignature
     */
    public XMLSignature constructSignature(SignedInfo signInfo,javax.xml.crypto.dsig.keyinfo.KeyInfo keyInfo){
        return getSignatureFactory().newXMLSignature(signInfo,keyInfo);
    }
    
    public SignedInfo constructSignedInfo(JAXBFilterProcessingContext fpContext)
    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, XWSSecurityException{
        if(PolicyTypeUtil.signaturePolicy(fpContext.getSecurityPolicy())) {
            return generateSignedInfo(fpContext);
        }
        return null;
    }
    
    private SignedInfo generateSignedInfo(JAXBFilterProcessingContext fpContext)
    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, XWSSecurityException{
        SignaturePolicy signaturePolicy = (SignaturePolicy) fpContext.getSecurityPolicy();
        SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding();
        MLSPolicy keyBinding = signaturePolicy.getKeyBinding();
        
        XMLSignatureFactory signatureFactory = getSignatureFactory();
        String canonicalAlgo = featureBinding.getCanonicalizationAlgorithm();
        ArrayList targetList = featureBinding.getTargetBindings();
        ArrayList cloneList = targetList;
        if (signaturePolicy.getKeyBinding() instanceof LazyKeyBinding) {
            LazyKeyBinding lkb = (LazyKeyBinding) signaturePolicy.getKeyBinding();
            if (lkb.getRealId() != null) {
                cloneList = (ArrayList) targetList.clone();
                Iterator it = cloneList.iterator();
                while (it.hasNext()) {
                    SignatureTarget o = (SignatureTarget) it.next();
                    if (o.getValue().equals("#" + lkb.getSTRID())) {
                        o.setValue("#" + lkb.getRealId());
                    }
                }
            }
        }
        String keyAlgo = MessageConstants.RSA_SHA1_SIGMETHOD;
        
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
            DerivedTokenKeyBinding dtkBinding = (DerivedTokenKeyBinding)keyBinding;
            //This check is done because DerivedKeys is ignored for Assymetric case
            if(fpContext.getTrustContext() != null && fpContext.getTrustContext().getProofKey() == null &&
                   PolicyTypeUtil.issuedTokenKeyBinding( dtkBinding.getOriginalKeyBinding())){
                keyAlgo = MessageConstants.RSA_SHA1_SIGMETHOD;
                //keyAlgo = fpContext.getAlgorithmSuite().getAsymmetricKeySignatureAlgorithm();
            }
        } else if (PolicyTypeUtil.issuedTokenKeyBinding(keyBinding)) {
            //TODO: verify if this is always correct
            if(fpContext.getTrustContext() != null){                
                keyAlgo = fpContext.getTrustContext().getSignWith();
            }
            if(keyAlgo == null){
                keyAlgo = MessageConstants.HMAC_SHA1_SIGMETHOD;
            }
            //keyAlgo = fpContext.getAlgorithmSuite().getSymmetricKeySignatureAlgorithm();
            if(fpContext.getTrustContext() != null  && fpContext.getTrustContext().getProofKey() == null){
                //keyAlgo = fpContext.getAlgorithmSuite().getAsymmetricKeySignatureAlgorithm();
                if(fpContext.getTrustContext().getSignWith() == null){
                    keyAlgo = MessageConstants.RSA_SHA1_SIGMETHOD;
                }
            }            
        }else if (PolicyTypeUtil.keyValueTokenBinding(keyBinding)) {
            keyAlgo = MessageConstants.RSA_SHA1_SIGMETHOD;
        }else{
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1703_UNSUPPORTED_KEYBINDING_SIGNATUREPOLICY(keyBinding));
            throw new XWSSecurityException("Unsupported KeyBinding for SignaturePolicy");
        }
        C14NMethodParameterSpec spec = null;
        if (MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS.equalsIgnoreCase(canonicalAlgo)) {
            if(!fpContext.getDisableIncPrefix()){
                List inc = new ArrayList();
                inc.add("wsse"); inc.add("S");
                spec = new ExcC14NParameterSpec(inc);
            }
            ((NamespaceContextEx)fpContext.getNamespaceContext()).addExc14NS();
        }
        CanonicalizationMethod canonicalMethod =
                signatureFactory.newCanonicalizationMethod(canonicalAlgo,spec);
        if(!fpContext.getDisableIncPrefix()){
            List contentList = setInclusiveNamespaces((ExcC14NParameterSpec)spec);
            ((com.sun.xml.ws.security.opt.crypto.dsig.CanonicalizationMethod)canonicalMethod).setContent(contentList);
        }
        
        SignatureMethod signatureMethod = signatureFactory.newSignatureMethod(keyAlgo, null);
        //Note : Signature algorithm parameters null for now , fix me.
        SignedInfo signedInfo = signatureFactory.newSignedInfo(canonicalMethod,signatureMethod,
                generateReferenceList(cloneList, signatureFactory, fpContext, false), null);
        //Note : Id is now null
        return signedInfo;
    }
    
    /**
     *
     * @return XMLSignatureFactory
     */
    private XMLSignatureFactory getSignatureFactory() {
        try {
            return JAXBSignatureFactory.newInstance();
        }catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private List generateReferenceList(List targetList, XMLSignatureFactory signatureFactory,
            JAXBFilterProcessingContext fpContext, boolean verify)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, XWSSecurityException {
        ListIterator iterator = targetList.listIterator();
        ArrayList references = new ArrayList();
        if(logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, LogStringsMessages.WSS_1751_NUMBER_TARGETS_SIGNATURE(targetList.size()));
        }
        
        while(iterator.hasNext()) {
            SignatureTarget signatureTarget = (SignatureTarget)iterator.next();
            String digestAlgo = signatureTarget.getDigestAlgorithm();
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST, LogStringsMessages.WSS_1752_SIGNATURE_TARGET_VALUE(signatureTarget.getValue()));
                logger.log(Level.FINEST, LogStringsMessages.WSS_1753_TARGET_DIGEST_ALGORITHM(digestAlgo));
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
            while(transformIterator.hasNext()) {
                SignatureTarget.Transform transformInfo = (SignatureTarget.Transform)transformIterator.next();
                String transformAlgo = transformInfo.getTransform();
                Transform transform = null;
                
                if(logger.isLoggable(Level.FINEST))
                    logger.log(Level.FINEST, "Transform Algorithm is "+transformAlgo);
                if(transformAlgo == Transform.XPATH || transformAlgo.equals(Transform.XPATH)){
                    /*TransformParameterSpec spec =(TransformParameterSpec) transformInfo.getAlgorithmParameters();
                    //XPathFilterParameterSpec spec = null;
                    if(spec == null){
                        throw new XWSSecurityException("XPATH parameters cannot be null");
                    }
                    //XPATH2,XSLTC , ..
                    transform = signatureFactory.newTransform(transformAlgo,spec);*/
                    throw new UnsupportedOperationException("XPATH not supported");
                } else if(transformAlgo == Transform.XPATH2 || transformAlgo.equals(Transform.XPATH2)){
                    /*TransformParameterSpec transformParams = (TransformParameterSpec)transformInfo.getAlgorithmParameters();
                    transform= signatureFactory.newTransform(transformAlgo,transformParams);*/
                    throw new UnsupportedOperationException("XPATH not supported");
                } else if (transformAlgo == MessageConstants.STR_TRANSFORM_URI || transformAlgo.equals(MessageConstants.STR_TRANSFORM_URI)){
                    Parameter transformParams =(Parameter) transformInfo.getAlgorithmParameters();
                    String  algo = null;
                    if(transformParams.getParamName().equals("CanonicalizationMethod")){
                        algo = transformParams.getParamValue();
                    }
                    if(algo == null){
                        throw new XWSSecurityException("STR Transform must have a"+
                                "canonicalization method specified");
                    }
                    if(logger.isLoggable(Level.FINEST)){
                        logger.log(Level.FINEST, "CanonicalizationMethod is " + algo);
                    }
                    //CanonicalizationMethod cm = null;
                    C14NMethodParameterSpec spec = null;
                    try{
                        TransformationParametersType tp =
                                new com.sun.xml.ws.security.secext10.ObjectFactory().createTransformationParametersType();
                        com.sun.xml.ws.security.opt.crypto.dsig.CanonicalizationMethod cm =
                                new com.sun.xml.ws.security.opt.crypto.dsig.CanonicalizationMethod();
                        cm.setAlgorithm(algo);
                        tp.getAny().add(cm);
                        JAXBElement<TransformationParametersType> tpElement =
                                new com.sun.xml.ws.security.secext10.ObjectFactory().createTransformationParameters(tp);
                        XMLStructure transformSpec = new JAXBStructure(tpElement);
                        transform = signatureFactory.newTransform(transformAlgo,transformSpec);
                        if(signatureTarget.getType() == SignatureTarget.TARGET_TYPE_VALUE_URI){
                            String targetURI = signatureTarget.getValue();
                            ((com.sun.xml.ws.security.opt.crypto.dsig.Transform)transform).setReferenceId(targetURI);
                        }
                        
                    } catch(Exception ex){
                        logger.log(Level.SEVERE,"WSS1300.dsig.transform_param.error",ex);
                        throw new XWSSecurityException(ex.getMessage());
                    }
                } else if (MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS.equalsIgnoreCase(transformAlgo)) {
                    // should be there by default...
                    // As per R 5412, last child of ds:Transforms must be either excl-c14n, or attachment-content only or attachment-complete transform
                    exclTransformToBeAdded = true;
                } else {
                    transform = signatureFactory.newTransform(transformAlgo,(TransformParameterSpec)null);
                    //throw new XWSSecurityException(transformAlgo + " not supported as Signature transform");
                    
                }
                if (!MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS.equalsIgnoreCase(transformAlgo)) {
                    // will add c14n transform in the end; later
                    transformList.add(transform);
                }
            }
            String targetURI = "";
            String signatureType = signatureTarget.getType();
            SecuredMessage secMessage = fpContext.getSecuredMessage();
            //SecurityHeader secHeader = fpContext.getSecurityHeader();
            //boolean headersOnly = signatureTarget.isSOAPHeadersOnly();
            
            if(signatureType.equals(SignatureTarget.TARGET_TYPE_VALUE_QNAME)){
                
                String expr = null;
                List<SignedMessagePart> targets = new ArrayList<SignedMessagePart>();
                
                String targetValue = signatureTarget.getValue();
                boolean optimized = false;
                if(fpContext.getConfigType() == MessageConstants.SIGN_BODY ||
                        fpContext.getConfigType() == MessageConstants.SIGN_ENCRYPT_BODY){
                    optimized = true;
                }
                
                if(targetValue.equals(SignatureTarget.BODY )){
                    Object body = secMessage.getBody();
                    if(body instanceof SignedMessagePart){
                        targets.add((SignedMessagePart)body);
                    } else if(body instanceof SecurityElement){
                        SignedMessagePart smp = new SignedMessagePart((SecurityElement)body);
                        targets.add(smp);
                        
                    } else{
                        // replace SOAPBody with securityElement and add
                        // to targets
                        boolean contentOnly = signatureTarget.getContentOnly();
                        SOAPBody soapBody = (SOAPBody)body;
                        if(!contentOnly){
                            if(soapBody.getId() == null || "".equals(soapBody.getId()))
                                soapBody.setId(fpContext.generateID());
                            SignedMessagePart smp = new SignedMessagePart(soapBody, contentOnly);
                            secMessage.replaceBody(smp);
                            targets.add(smp);
                        } else{
                            String id = null;
                            if(soapBody.getBodyContentId() == null || "".equals(soapBody.getBodyContentId())){
                                id = fpContext.generateID();
                                soapBody.setBodyContentId(id);
                            }
                            
                            SignedMessagePart smp = new SignedMessagePart(soapBody, contentOnly);
                            SOAPBody newBody =  new SOAPBody(smp,fpContext.getSOAPVersion());
                            newBody.setId(soapBody.getId());
                            secMessage.replaceBody(newBody);
                            targets.add(smp);
                        }
                    }
                } else{
                    //if QName is of the form "{NS-URI}" then this method throws
                    //illegalArgumentException with JDK 1.6
                    //QName name = QName.valueOf(targetValue);
                    QName name = null;
                    if (targetValue.endsWith("}")) {
                        String nsURI = targetValue.substring(1,targetValue.length() -1);
                        name = new QName(nsURI,"");
                    } else {
                        name = QName.valueOf(targetValue);
                    }
                    //boolean contentOnly = signatureTarget.getContentOnly();
                    Iterator headers = null;
                    if(name.getNamespaceURI().equals(MessageConstants.ADDRESSING_MEMBER_SUBMISSION_NAMESPACE) ||
                            name.getNamespaceURI().equals(MessageConstants.ADDRESSING_W3C_NAMESPACE)){
                        if(!"".equals(name.getLocalPart()))
                            headers = secMessage.getHeaders(name.getLocalPart(), null);
                        else{
                            headers = secMessage.getHeaders(MessageConstants.ADDRESSING_MEMBER_SUBMISSION_NAMESPACE);
                            if(!headers.hasNext())
                                headers = secMessage.getHeaders(MessageConstants.ADDRESSING_W3C_NAMESPACE);
                        }
                    } else {
                        if(!"".equals(name.getLocalPart()))
                            headers = secMessage.getHeaders(name.getLocalPart(), name.getNamespaceURI());
                        else
                            headers = secMessage.getHeaders(name.getNamespaceURI());
                    }
                    
                    while(headers.hasNext()){
                        Object next = headers.next();
                        if(next instanceof SignedMessageHeader){
                            targets.add((SignedMessageHeader)next);
                        } else if(next instanceof SecurityHeaderElement){
                            SecurityHeaderElement she = (SecurityHeaderElement)next;
                            SignedMessageHeader smh = new SignedMessageHeader(she);
                            secMessage.replaceHeader(she, smh);
                            targets.add(smh);
                        } else if(next instanceof Header){
                            Header header = (Header)next;
                            SignedMessageHeader smh = toSignedMessageHeader(header, fpContext);
                            secMessage.replaceHeader(header, smh);
                            targets.add(smh);
                        }
                    }
                    
                    SecurityHeader sh = fpContext.getSecurityHeader();
                    headers = sh.getHeaders(name.getLocalPart(), name.getNamespaceURI());
                    while(headers.hasNext()){
                        SecurityHeaderElement she = (SecurityHeaderElement) headers.next();
                        if(she instanceof SignedMessageHeader){
                            targets.add((SignedMessageHeader)she);
                        } else{
                            if(she.getId() == null){
                                she.setId(fpContext.generateID());
                            }
                            SignedMessageHeader smh = new SignedMessageHeader(she);
                            targets.add(smh);
                        }
                    }
                }
                
                if(targets.size() <= 0){
                    if(signatureTarget.getEnforce()){
                        throw new XWSSecurityException("SignatureTarget with URI "+signatureTarget.getValue()+
                                " is not in the message");
                    } else
                        continue;
                }
                
                if(logger.isLoggable(Level.FINEST)){
                    logger.log(Level.FINEST, "Number of nodes "+ targets.size());
                    logger.log(Level.FINEST, "+++++++++++++++END+++++++++++++++");
                }
                
                HashMap elementCache = null;
                if(fpContext != null ){
                    elementCache = fpContext.getElementCache();
                }
                
                for(int i = 0; i < targets.size(); i++){
                    SignedMessagePart targetRef = targets.get(i);
                    ArrayList clonedTransformList = (ArrayList)transformList.clone();
                    if (exclTransformToBeAdded) {
                        // exc-14-n must be one of the last transforms under ReferenceList by default.
                        String transformAlgo  = MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS;
                        ((NamespaceContextEx)fpContext.getNamespaceContext()).addExc14NS();
                        ExcC14NParameterSpec spec = null;
                        if(!fpContext.getDisableIncPrefix()){
                            ArrayList list = new ArrayList();
                            /*list.add("wsu");list.add("wsse");*/ list.add("S");
                            spec = new ExcC14NParameterSpec(list); //TO BE SET
                        }
                        Transform transform = signatureFactory.newTransform(transformAlgo,spec);
                        // Commenting this - content is now set directly in com.sun.xml.ws.security.opt.crypto.dsig.Transform
                        // class
//                        if(!fpContext.getDisableIncPrefix()){
//                            List contentList = setInclusiveNamespaces(spec);
//                            ((com.sun.xml.ws.security.opt.crypto.dsig.Transform)transform).setContent(contentList);
//                        }
                        clonedTransformList.add(transform);
                    }
                    
                    String id = targetRef.getId();
                    if (id == null || id.equals("")) {
                        id = fpContext.generateID();
                        if(!verify){
                            targetRef.setId(id);
                        } else{
                            //add to context. dont modify the message.
                            elementCache.put(id, targetRef);
                        }
                    }
                    
                    if(logger.isLoggable(Level.FINEST))
                        logger.log(Level.FINEST, "SignedInfo val id "+id);
                    
                    targetURI = "#"+id;
                    
                    Reference reference = null;
                    reference = signatureFactory.newReference(targetURI,digestMethod,clonedTransformList,null,null);
                    references.add(reference);
                }
                continue;
            } else if(signatureType ==SignatureTarget.TARGET_TYPE_VALUE_URI){
                targetURI = signatureTarget.getValue();
                
                if(targetURI == null){
                    targetURI="";
                }
                QName policyName = signatureTarget.getPolicyQName();
                if(policyName != null && policyName == MessageConstants.SCT_NAME){
                    String _uri = targetURI;
                    if(targetURI.length() > 0 && targetURI.charAt(0)=='#'){
                        _uri = targetURI.substring(1);
                    }
                    com.sun.xml.ws.security.IssuedTokenContext ictx  = fpContext.getIssuedTokenContext(_uri);
                    com.sun.xml.ws.security.SecurityContextToken sct1 =(com.sun.xml.ws.security.SecurityContextToken)ictx.getSecurityToken();
                    targetURI = sct1.getWsuId();                    
                }
                if(MessageConstants.PROCESS_ALL_ATTACHMENTS.equals(targetURI)){
                    AttachmentSet as = secMessage.getAttachments();
                    if(as != null && as.isEmpty()){
                        logger.log(Level.WARNING, "No attachment part present in the message to be secured");
                        continue;
                    }
                    for(Attachment attachment : as){
                        String cid = "cid:" + attachment.getContentId();
                        
                        Reference reference = signatureFactory.newReference(cid, digestMethod, transformList, null, null);
                        references.add(reference);
                    }
                    continue;
                } else{
                    if (exclTransformToBeAdded) {
                        String _uri = targetURI;
                        if(targetURI.length() > 0 && targetURI.charAt(0)=='#'){
                            _uri = targetURI.substring(1);
                        }
                        Object reqdPart = getPartFromId(fpContext, _uri);
                        if(reqdPart != null){
                            String transformAlgo  = MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS;
                            ExcC14NParameterSpec spec = null;
                            if(!fpContext.getDisableIncPrefix()){
                                ArrayList list = new ArrayList();
                                list.add("wsu");list.add("wsse"); list.add("S");
                                spec = new ExcC14NParameterSpec(list);
                            }
                            Transform transform = signatureFactory.newTransform(transformAlgo,spec);
                            // Commenting this - content is now set directly in com.sun.xml.ws.security.opt.crypto.dsig.Transform
                            // class
//                            if(!fpContext.getDisableIncPrefix()){
//                                List contentList = setInclusiveNamespaces(spec);
//                                ((com.sun.xml.ws.security.opt.crypto.dsig.Transform)transform).setContent(contentList);
//                            }
                            transformList.add(transform);
                        }
                    }
                    if(targetURI.equals(SignatureTarget.ALL_MESSAGE_HEADERS)){
                        //throw new UnsupportedOperationException(SignatureTarget.ALL_MESSAGE_HEADERS +
                        //        " not supported yet");
                        ArrayList headers = secMessage.getHeaders();
                        for(int i = 0; i < headers.size(); i++){
                            Object header = headers.get(i);
                            String tmpUri = null;
                            if(header instanceof SignedMessageHeader){
                                tmpUri = "#" + ((SignedMessageHeader)header).getId();
                                
                            } else if(header instanceof SecurityHeaderElement){
                                SecurityHeaderElement she = (SecurityHeaderElement)header;
                                SignedMessageHeader smh = new SignedMessageHeader(she);
                                String id = smh.getId();
                                if(id == null){
                                    id = fpContext.generateID();
                                    smh.setId(id);
                                }
                                secMessage.replaceHeader(she, smh);
                                tmpUri = "#" + id;
                            } else if(header instanceof Header){
                                Header jwHeader = (Header)header;
                                tmpUri = fpContext.generateID();
                                SignedMessageHeader smh = createSignedMessageHeader(jwHeader, tmpUri, fpContext);
                                secMessage.replaceHeader(jwHeader, smh);
                                /*if (!tmpUri.startsWith("#")) {
                                    tmpUri = "#" + tmpUri;
                                }*/
                            }
                            if(tmpUri != null){
                                Reference reference = signatureFactory.newReference(tmpUri,digestMethod,transformList,null,null);
                                references.add(reference);
                            }
                        }
                        continue;
                    }
                    
                }
            }
            Reference reference = null;
            reference = signatureFactory.newReference(targetURI,digestMethod,transformList,null,null);
            references.add(reference);
            
        }
        if (references.isEmpty()) {
            logger.log(Level.WARNING, "No Signed Parts found in the Message");
        }
        return references;
    }
    
    private List setInclusiveNamespaces(ExcC14NParameterSpec spec){
        if(spec == null){
            return null;
         }
        com.sun.xml.security.core.dsig.ObjectFactory objFac = new com.sun.xml.security.core.dsig.ObjectFactory();
        InclusiveNamespacesType incList = objFac.createInclusiveNamespaces();
        List prefixList = spec.getPrefixList();
        for(int j = 0; j < prefixList.size(); j++){
            String prefix = (String)prefixList.get(j);
            incList.addToPrefixList(prefix);
        }
        JAXBElement<InclusiveNamespacesType> je = objFac.createInclusiveNamespaces(incList);
        List contentList = new ArrayList();
        contentList.add(je);
        return contentList;
    }
    
    private SignedMessageHeader toSignedMessageHeader(Header header, JAXBFilterProcessingContext context) {
        return createSignedMessageHeader(header, context.generateID(), context);
    }
    
    private SignedMessageHeader createSignedMessageHeader(com.sun.xml.ws.api.message.Header header, String string, JAXBFilterProcessingContext context) {
        return new SignedMessageHeader(header, string, context);
    }
    
    private Object getPartFromId(JAXBFilterProcessingContext fpContext, final String id)
    throws XWSSecurityException{
        SecuredMessage secMessage = fpContext.getSecuredMessage();
        Object reqdHeader = secMessage.getHeader(id);
        if(reqdHeader != null){
            return reqdHeader;
        }
        SecurityHeader secHeader = fpContext.getSecurityHeader();
        SecurityHeaderElement she = secHeader.getChildElement(id);
        if(she != null)
            return she;
        Object body = secMessage.getBody();
        if(body instanceof SOAPBody){
            SOAPBody soapBody = (SOAPBody)body;
            if(id.equals(soapBody.getId()))
                return soapBody;
        } else if(body instanceof SecurityElement){
            SecurityElement se = (SecurityElement)body;
            if(id.equals(se.getId()))
                return se;
        }
        return null;
    }
    
}
