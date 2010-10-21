/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.stream.buffer.XMLStreamBufferMark;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.api.PolicyBuilder;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.TokenValidator;
//import com.sun.xml.ws.security.opt.api.tokens.UsernameToken;
import com.sun.xml.ws.security.opt.impl.incoming.processor.UsernameTokenProcessor;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.ws.security.opt.impl.util.XMLStreamReaderFactory;
import com.sun.xml.wss.NonceManager;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.io.OutputStream;
import java.util.HashMap;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.logging.Level;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.filter.LogStringsMessages;
import java.util.logging.Logger;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class UsernameTokenHeader implements com.sun.xml.ws.security.opt.api.tokens.UsernameToken, SecurityHeaderElement,
        TokenValidator, PolicyBuilder, NamespaceContextInfo, SecurityElementWriter{
    
    private static Logger log = Logger.getLogger(
            LogDomainConstants.IMPL_FILTER_DOMAIN,
            LogDomainConstants.IMPL_FILTER_DOMAIN_BUNDLE);
    
    private String localPart = null;
    private String namespaceURI = null;
    private String id = "";
    
    private XMLStreamBuffer mark = null;
    private UsernameTokenProcessor filter = new UsernameTokenProcessor();
    
    private AuthenticationTokenPolicy.UsernameTokenBinding utPolicy = null;
    
    private HashMap<String,String> nsDecls;
    //private UsernameToken unToken;
    
    /** Creates a new instance of UsernameTokenHeader */
    @SuppressWarnings("unchecked")
    public UsernameTokenHeader(XMLStreamReader reader, StreamReaderBufferCreator creator,
            HashMap nsDecls, XMLInputFactory  staxIF) throws XMLStreamException, XMLStreamBufferException  {
        localPart = reader.getLocalName();
        namespaceURI = reader.getNamespaceURI();
        id = reader.getAttributeValue(MessageConstants.WSU_NS,"Id");
        
        mark = new XMLStreamBufferMark(nsDecls,creator);
        XMLStreamReader utReader = XMLStreamReaderFactory.createFilteredXMLStreamReader(reader,filter) ;
        creator.createElementFragment(utReader,true);
        this.nsDecls = nsDecls;
        
        utPolicy = new AuthenticationTokenPolicy.UsernameTokenBinding();
        utPolicy.setUUID(id);
        
        utPolicy.setUsername(filter.getUsername());
        utPolicy.setPassword(filter.getPassword());        
        if (MessageConstants.PASSWORD_DIGEST_NS.equals(filter.getPasswordType())){
            utPolicy.setDigestOn(true);
        }
        if(filter.getNonce() != null){
            utPolicy.setUseNonce(true);
        }
    }
    
    public void validate(ProcessingContext context) throws XWSSecurityException {
        boolean authenticated = false;
        if (filter.getPassword() == null && filter.getPasswordDigest() == null) {
            utPolicy.setNoPassword(true);
        }
        if (filter.getSalt() != null) {
            utPolicy.setNoPassword(false);
        }
        if(filter.getPassword() == null && filter.getCreated() != null  && !MessageConstants.PASSWORD_DIGEST_NS.equals(filter.getPasswordType())){
            context.getSecurityEnvironment().validateCreationTime(
                        context.getExtraneousProperties(), filter.getCreated(), 
                        MessageConstants.MAX_CLOCK_SKEW, MessageConstants.TIMESTAMP_FRESHNESS_LIMIT);          
        } else if (filter.getPassword() == null && filter.getCreated() == null) {
            if (MessageConstants.PASSWORD_DIGEST_NS.equals(filter.getPasswordType())) {
                 throw SOAPUtil.newSOAPFaultException(
                        MessageConstants.WSSE_INVALID_SECURITY,
                        "Cannot validate Password Digest since Creation Time was not Specified",
                        null);
            }
        }else if (MessageConstants.PASSWORD_DIGEST_NS.equals(filter.getPasswordType())) {
            authenticated = context.getSecurityEnvironment().authenticateUser(
                    context.getExtraneousProperties(), filter.getUsername(), filter.getPasswordDigest(),
                    filter.getNonce(), filter.getCreated());
            if(!authenticated){
                log.log(Level.SEVERE, LogStringsMessages.WSS_1408_FAILED_SENDER_AUTHENTICATION());
                throw SOAPUtil.newSOAPFaultException(
                        MessageConstants.WSSE_FAILED_AUTHENTICATION,
                        "Authentication of Username Password Token Failed",
                        null);
            }
            if (filter.getCreated() != null) {
                context.getSecurityEnvironment().validateCreationTime(
                        context.getExtraneousProperties(), filter.getCreated(), 
                        MessageConstants.MAX_CLOCK_SKEW, MessageConstants.TIMESTAMP_FRESHNESS_LIMIT);
            }
            if (filter.getNonce() != null) {
                try {
                    if (!context.getSecurityEnvironment().validateAndCacheNonce(
                           context.getExtraneousProperties(), filter.getNonce(), filter.getCreated(),MessageConstants.MAX_NONCE_AGE)) {
                        XWSSecurityException xwse =
                                new XWSSecurityException(
                                "Invalid/Repeated Nonce value for Username Token");
                        throw SOAPUtil.newSOAPFaultException(
                                MessageConstants.WSSE_FAILED_AUTHENTICATION,
                                "Invalid/Repeated Nonce value for Username Token",
                                xwse);
                    }
                } catch (NonceManager.NonceException ex) {
                    throw SOAPUtil.newSOAPFaultException(
                            MessageConstants.WSSE_FAILED_AUTHENTICATION,
                            "Invalid/Repeated Nonce value for Username Token",
                            ex);
                }
            }
        } else{
            authenticated = context.getSecurityEnvironment().authenticateUser(context.getExtraneousProperties(),
                    filter.getUsername(), filter.getPassword());
            if(!authenticated){
                log.log(Level.SEVERE, LogStringsMessages.WSS_1408_FAILED_SENDER_AUTHENTICATION());
                throw SOAPUtil.newSOAPFaultException(
                        MessageConstants.WSSE_FAILED_AUTHENTICATION,
                        "Authentication of Username Password Token Failed",
                        null);
                
            }
        }
        
        
        if (MessageConstants.debug) {
            log.log(Level.FINEST, "Password Validated.....");
        }
        
        context.getSecurityEnvironment().updateOtherPartySubject(
                DefaultSecurityEnvironmentImpl.getSubject((FilterProcessingContext)context),filter.getUsername(), filter.getPassword());
    }
    
    public WSSPolicy getPolicy() {
        return utPolicy;
    }
    
    public boolean refersToSecHdrWithId(String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    public String getLocalPart() {
        return localPart;
    }
    
    
    public XMLStreamReader readHeader() throws XMLStreamException {
        return mark.readAsXMLStreamReader();
    }
    
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        mark.writeToXMLStreamWriter(streamWriter);
    }
    
    public String getUsernameValue() {
        return filter.getUsername();
    }
    
    public void setUsernameValue(String username) {
        throw new UnsupportedOperationException();
    }
    
    public String getPasswordValue() {
        return filter.getPassword();
    }
    
    public void setPasswordValue(String passwd) {
        throw new UnsupportedOperationException();
    }
    public void setSalt(String receivedSalt){
        throw new UnsupportedOperationException();
    }
    public String getSalt(){
        return filter.getSalt();
    }
    public void setIterations(int iterate){
        throw new UnsupportedOperationException();
    }
    public String getIterations(){
        return filter.getIterations();
    }    
    public HashMap<String, String> getInscopeNSContext() {
        return nsDecls;
    }
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
}

