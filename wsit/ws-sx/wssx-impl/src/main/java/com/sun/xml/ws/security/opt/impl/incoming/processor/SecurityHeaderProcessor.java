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

package com.sun.xml.ws.security.opt.impl.incoming.processor;

import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.TokenValidator;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.incoming.KerberosBinarySecurityToken;
import com.sun.xml.ws.security.opt.impl.incoming.X509BinarySecurityToken;
import com.sun.xml.ws.security.opt.impl.incoming.DerivedKeyToken;
import com.sun.xml.ws.security.opt.impl.incoming.EncryptedData;
import com.sun.xml.ws.security.opt.impl.incoming.EncryptedKey;
import com.sun.xml.ws.security.opt.impl.incoming.GenericSecuredHeader;
import com.sun.xml.ws.security.opt.impl.incoming.SAMLAssertion;
import com.sun.xml.ws.security.opt.impl.incoming.SecurityContextToken;
import com.sun.xml.ws.security.opt.impl.incoming.Signature;
import com.sun.xml.ws.security.opt.impl.incoming.SignatureConfirmation;
import com.sun.xml.ws.security.opt.impl.incoming.TimestampHeader;
import com.sun.xml.ws.security.opt.impl.incoming.UsernameTokenHeader;
import com.sun.xml.wss.BasicSecurityProfile;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.logging.Level;
import com.sun.xml.wss.logging.impl.opt.LogStringsMessages;
import static com.sun.xml.wss.BasicSecurityProfile.*;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityHeaderProcessor {
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_DOMAIN,
            LogDomainConstants.IMPL_OPT_DOMAIN_BUNDLE);
    
    private static final  int TIMESTAMP_ELEMENT = 1;
    private static final  int USERNAMETOKEN_ELEMENT = 2;
    private static final  int BINARYSECURITY_TOKEN_ELEMENT = 3;
    private static final  int ENCRYPTED_DATA_ELEMENT = 4;
    private static final  int ENCRYPTED_KEY_ELEMENT = 5;
    private static final  int SIGNATURE_ELEMENT = 6;
    private static final  int REFERENCE_LIST_ELEMENT = 7;
    private static final  int DERIVED_KEY_ELEMENT = 8;
    private static final  int SIGNATURE_CONFIRMATION_ELEMENT = 9;
    private static final  int SECURITY_CONTEXT_TOKEN = 10;
    private static final  int SAML_ASSERTION_ELEMEMENT = 11;
    private Map<String,String> currentParentNS = new HashMap<String,String>();
    private JAXBFilterProcessingContext context;
    private XMLInputFactory staxIF = null;
    private StreamReaderBufferCreator creator = null;
    private BasicSecurityProfile bspContext = null;
    /** Creates a new instance of SecurityHeaderProcessor */
    
    public SecurityHeaderProcessor(JAXBFilterProcessingContext context,Map<String,String> namespaceList,XMLInputFactory xi,StreamReaderBufferCreator sbc) {
        this.context = context;
        this.currentParentNS = namespaceList;
        this.staxIF = xi;
        this.context = context;
        this.creator = sbc;
        this.bspContext = context.getBSPContext();
    }
    /**
     * gets the SecurityElementType from the message and creates and processes such header
     * @param message XMLStreamReader
     * @return SecurityHeaderElement
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    @SuppressWarnings("unchecked")
    public SecurityHeaderElement createHeader(XMLStreamReader message)throws XWSSecurityException{
        
        int eventType = getSecurityElementType(message);
        try{
            while(eventType != -1){
                switch (eventType){
                    case TIMESTAMP_ELEMENT : {
                        if(context.isBSP() && bspContext.isTimeStampFound()){
                            log_bsp_3203();
                        }
                        bspContext.setTimeStampFound(true);
                        TimestampHeader timestamp = new TimestampHeader(message,creator,(HashMap) currentParentNS, context);
                        ((TokenValidator)timestamp).validate(context);
                        context.getSecurityContext().getProcessedSecurityHeaders().add(timestamp);
                        context.getInferredSecurityPolicy().append(timestamp.getPolicy());
                        return timestamp;
                        
                    }
                    case BINARYSECURITY_TOKEN_ELEMENT : {
                        String valueType = message.getAttributeValue(MessageConstants.WSSE_NS,MessageConstants.WSE_VALUE_TYPE);
                        if(valueType == MessageConstants.KERBEROS_V5_GSS_APREQ_1510){
                            KerberosBinarySecurityToken kbst = new KerberosBinarySecurityToken(message,creator,(HashMap) currentParentNS, staxIF);
                            ((TokenValidator)kbst).validate(context);
                            context.getSecurityContext().getProcessedSecurityHeaders().add(kbst);
                            context.getInferredSecurityPolicy().append(kbst.getPolicy());
                            if(context.isTrustMessage() && !context.isClient()){
                                IssuedTokenContext ctx = null;
                                if(context.getTrustContext() == null){
                                    ctx = new IssuedTokenContextImpl();
                                    ctx.setAuthnContextClass(MessageConstants.KERBEROS_AUTH_TYPE);
                                    context.setTrustContext(ctx);
                                }else{
                                    ctx = context.getTrustContext();
                                    if(ctx.getAuthnContextClass() != null){
                                        ctx.setAuthnContextClass(MessageConstants.KERBEROS_AUTH_TYPE);
                                        context.setTrustContext(ctx);
                                    }
                                }
                            }
                            return kbst;
                        } else{
                            X509BinarySecurityToken bst = new X509BinarySecurityToken(message,creator,(HashMap) currentParentNS, staxIF);
                            ((TokenValidator)bst).validate(context);
                            context.getSecurityContext().getProcessedSecurityHeaders().add(bst);
                            context.getInferredSecurityPolicy().append(bst.getPolicy());
                            if(context.isTrustMessage() && !context.isClient()){
                                IssuedTokenContext ctx = null;
                                if(context.getTrustContext() == null){
                                    ctx = new IssuedTokenContextImpl();
                                    ctx.setAuthnContextClass(MessageConstants.X509_AUTH_TYPE);
                                    context.setTrustContext(ctx);
                                }else{
                                    ctx = context.getTrustContext();
                                    if(ctx.getAuthnContextClass() != null){
                                        ctx.setAuthnContextClass(MessageConstants.X509_AUTH_TYPE);
                                        context.setTrustContext(ctx);
                                    }
                                }
                            }
                            return bst;
                        }
                    }
                    case ENCRYPTED_KEY_ELEMENT:{
                        EncryptedKey ek = new EncryptedKey(message,context,(HashMap) currentParentNS);
                        context.getSecurityContext().getProcessedSecurityHeaders().add(ek);
                        return ek;
                    }
                    case ENCRYPTED_DATA_ELEMENT :{
                        EncryptedData ed = new EncryptedData(message,context, (HashMap) currentParentNS);
                        context.getSecurityContext().getProcessedSecurityHeaders().add(ed);
                        return ed;
                    }
                    case USERNAMETOKEN_ELEMENT :{
                        UsernameTokenHeader ut = new UsernameTokenHeader(message,creator,(HashMap) currentParentNS, staxIF);
                        ut.validate(context);
                        if(context.isTrustMessage() && !context.isClient()){
                            IssuedTokenContext ctx = null;                            
                            if(context.getTrustContext() == null){
                                ctx = new IssuedTokenContextImpl();
                                if(context.isSecure()){
                                    ctx.setAuthnContextClass(MessageConstants.PASSWORD_PROTECTED_TRANSPORT_AUTHTYPE);    
                                }else{
                                    ctx.setAuthnContextClass(MessageConstants.PASSWORD_AUTH_TYPE);
                                }
                                context.setTrustContext(ctx);
                            }else{
                                ctx = context.getTrustContext();
                                if(ctx.getAuthnContextClass() != null){
                                    if(context.isSecure()){
                                        ctx.setAuthnContextClass(MessageConstants.PASSWORD_PROTECTED_TRANSPORT_AUTHTYPE);
                                    }else{
                                        ctx.setAuthnContextClass(MessageConstants.PASSWORD_AUTH_TYPE);
                                    }
                                    context.setTrustContext(ctx);
                                }
                            }
                        }
                        context.getSecurityContext().getProcessedSecurityHeaders().add(ut);
                        context.getInferredSecurityPolicy().append(ut.getPolicy());
                        return ut;
                    }
                    case DERIVED_KEY_ELEMENT:{
                        DerivedKeyToken dkt = new DerivedKeyToken(message, context, (HashMap) currentParentNS);
                        context.getSecurityContext().getProcessedSecurityHeaders().add(dkt);
                        return dkt;
                        
                    }
                    case SIGNATURE_CONFIRMATION_ELEMENT:{
                        SignatureConfirmation signConfirm = new SignatureConfirmation(message,creator,(HashMap) currentParentNS, staxIF);
                        signConfirm.validate(context);
                        context.getSecurityContext().getProcessedSecurityHeaders().add(signConfirm);
                        return signConfirm;
                        
                    }
                    case SECURITY_CONTEXT_TOKEN:{
                        SecurityContextToken sct = new SecurityContextToken(message, context, (HashMap) currentParentNS);
                        context.getSecurityContext().getProcessedSecurityHeaders().add(sct);
                        return sct;
                    }
                    case SIGNATURE_ELEMENT:{
                        Signature sig = new Signature(context,currentParentNS,creator,true);
                        sig.process(message);
                        if (sig.getReferences().size() == 0){
                            context.getSecurityContext().getProcessedSecurityHeaders().add(sig);
                        }
                        context.getInferredSecurityPolicy().append(sig.getPolicy());
                        return sig;
                    }
                    case SAML_ASSERTION_ELEMEMENT :{
                        SAMLAssertion samlAssertion = new SAMLAssertion(message,context,null,(HashMap) currentParentNS);
                        context.getSecurityContext().getProcessedSecurityHeaders().add(samlAssertion);
                        if(samlAssertion.isHOK()){
                            samlAssertion.validateSignature();
                        }
                        samlAssertion.validate(context);
                        samlAssertion.getKey();
                        // Set in the extraneous property only if not already set
                        // workaround in the case where there are two HOK assertions in the request
                        if(context.getExtraneousProperty(MessageConstants.INCOMING_SAML_ASSERTION) == null && samlAssertion.isHOK() ){
                            context.getExtraneousProperties().put(MessageConstants.INCOMING_SAML_ASSERTION,samlAssertion);
                        }
                        if(context.isTrustMessage() && !context.isClient()){
                            IssuedTokenContext ctx = null;
                            if(context.getTrustContext() == null){
                                ctx = new IssuedTokenContextImpl();
                                ctx.setAuthnContextClass(MessageConstants.PREVIOUS_SESSION_AUTH_TYPE);
                                context.setTrustContext(ctx);
                            }else{
                                ctx = context.getTrustContext();
                                if(ctx.getAuthnContextClass() != null){
                                    ctx.setAuthnContextClass(MessageConstants.PREVIOUS_SESSION_AUTH_TYPE);
                                    context.setTrustContext(ctx);
                                }
                            }                            
                        } else if(!context.isTrustMessage()){
                            context.getInferredSecurityPolicy().append(samlAssertion.getPolicy());
                        }
                        return samlAssertion;
                    }
                    default:{
                        GenericSecuredHeader gsh = new GenericSecuredHeader(message,null,creator, (HashMap) currentParentNS,staxIF, context.getEncHeaderContent());
                        // headers.add(gsh);
                    }
                }
                eventType = getSecurityElementType(message);
                // moveToNextElement();
            }
        }catch(XMLStreamException xe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1608_ERROR_SECURITY_HEADER(),xe);
            throw new XWSSecurityException(LogStringsMessages.WSS_1608_ERROR_SECURITY_HEADER(),xe);
        }catch(XMLStreamBufferException xbe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1608_ERROR_SECURITY_HEADER(),xbe);
            throw new XWSSecurityException(LogStringsMessages.WSS_1608_ERROR_SECURITY_HEADER(),xbe);
        }
        
        return null;
    }
    
    
    /**
     * checks the given XMLStreamReader is of type TimeStamp or not
     * @param reader XMLStreamReader
     * @return boolean
     */
    private boolean isTimeStamp(XMLStreamReader reader){
        if(reader.getLocalName() == MessageConstants.TIMESTAMP_LNAME && reader.getNamespaceURI() == MessageConstants.WSU_NS){
            return true;
        }
        return false;
    }
    
    /**
     * checks the given XMLStreamReader is of type BinarySecurityToken or not
     * @param reader XMLStreamReader
     * @return boolean
     */
    private boolean isBST(XMLStreamReader reader){
        if(reader.getLocalName() == MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME && reader.getNamespaceURI() == MessageConstants.WSSE_NS){
            return true;
        }
        return false;
    }
    /**
     * checks the given XMLStreamReader is of type Signature or not
     * @param reader XMLStreamReader
     * @return boolean
     */
    private boolean isSignature(XMLStreamReader reader){
        if(reader.getLocalName() == MessageConstants.SIGNATURE_LNAME && reader.getNamespaceURI() == MessageConstants.DSIG_NS){
            return true;
        }
        return false;
    }
    /**
     * checks the given XMLStreamReader is of type EncryptedKey or not
     * @param reader XMLStreamReader
     * @return boolean
     */
    private boolean isEncryptedKey(XMLStreamReader reader){
        if(reader.getLocalName() == MessageConstants.ENCRYPTEDKEY_LNAME && reader.getNamespaceURI() == MessageConstants.XENC_NS){
            return true;
        }
        return false;
    }
    /**
     * checks the given XMLStreamReader is of type EncryptedData or not
     * @param reader XMLStreamReader
     * @return boolean
     */
    private boolean isEncryptedData(XMLStreamReader reader){
        if(reader.getLocalName() == MessageConstants.ENCRYPTED_DATA_LNAME && reader.getNamespaceURI() == MessageConstants.XENC_NS){
            return true;
        }
        return false;
    }
    /**
     * checks the given XMLStreamReader is of type UsernameToken or not
     * @param reader XMLStreamReader
     * @return boolean
     */
    private boolean isUsernameToken(XMLStreamReader reader){
        if(reader.getLocalName() == MessageConstants.USERNAME_TOKEN_LNAME && reader.getNamespaceURI() == MessageConstants.WSSE_NS){
            return true;
        }
        return false;
    }
    /**
     * checks the given XMLStreamReader is of type DerivedKey or not
     * @param reader XMLStreamReader
     * @return boolean
     */
    private boolean isDerivedKey(XMLStreamReader reader){
        if(reader.getLocalName() == MessageConstants.DERIVEDKEY_TOKEN_LNAME && reader.getNamespaceURI() == MessageConstants.WSSC_NS){
            return true;
        }
        return false;
    }
    /**
     * checks the given XMLStreamReader is of type SignatureConfirmation or not
     * @param reader XMLStreamReader
     * @return boolean
     */
    private boolean isSignatureConfirmation(XMLStreamReader reader){
        if(reader.getLocalName() == MessageConstants.SIGNATURE_CONFIRMATION_LNAME && reader.getNamespaceURI() == MessageConstants.WSSE11_NS){
            return true;
        }
        return false;
    }
    /**
     * checks the given XMLStreamReader is of type SecurityContextToken or not
     * @param reader
     * @return
     */
    private boolean isSCT(XMLStreamReader reader){
        if(reader.getLocalName() == MessageConstants.SECURITY_CONTEXT_TOKEN_LNAME && reader.getNamespaceURI() == MessageConstants.WSSC_NS){
            return true;
        }
        return false;
    }
    /**
     * checks the given XMLStreamReader is of type SAML or not
     * @param message XMLStreamReader
     * @return boolean
     */
    private boolean isSAML(XMLStreamReader message){
        if(message.getLocalName() == MessageConstants.SAML_ASSERTION_LNAME ){
            String uri = message.getNamespaceURI();
            if( uri == MessageConstants.SAML_v2_0_NS || uri ==MessageConstants.SAML_v1_0_NS || uri == MessageConstants.SAML_v1_1_NS ){
                return true;
            }
        }
        return false;
    }
    
    private void moveToNextElement(XMLStreamReader reader) throws XMLStreamException{
        reader.next();
        while(reader.getEventType() != XMLStreamReader.START_ELEMENT){
            reader.next();
        }
    }
    /**
     * returns the security element type like TIMESTAMP_ELEMENT,BINARYSECURITY_TOKEN_ELEMENT,SIGNATURE_ELEMENT etc
     * @param reader XMLStreamReader
     * @return int
     */
    public int getSecurityElementType(XMLStreamReader reader){
        if(isTimeStamp(reader)){
            return TIMESTAMP_ELEMENT;
        }
        
        if(isBST(reader)){
            return BINARYSECURITY_TOKEN_ELEMENT;
        }
        
        if(isSignature(reader)){
            return SIGNATURE_ELEMENT;
        }
        
        if(isEncryptedKey(reader)){
            return ENCRYPTED_KEY_ELEMENT;
        }
        
        if(isEncryptedData(reader)){
            return ENCRYPTED_DATA_ELEMENT;
        }
        
        if(isUsernameToken(reader)){
            return USERNAMETOKEN_ELEMENT;
        }
        
        if(isSignatureConfirmation(reader)){
            return SIGNATURE_CONFIRMATION_ELEMENT;
        }
        
        if(isDerivedKey(reader)){
            return this.DERIVED_KEY_ELEMENT;
        }
        
        if(isSCT(reader)){
            return this.SECURITY_CONTEXT_TOKEN;
        }
        if(isSAML(reader)){
            return this.SAML_ASSERTION_ELEMEMENT;
        }
        return -1;
    }
    
    
}
