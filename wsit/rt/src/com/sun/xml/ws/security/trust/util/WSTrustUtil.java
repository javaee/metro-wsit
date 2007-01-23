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
 * WSTrustUtil.java
 *
 * Created on February 7, 2006, 3:37 PM
 *
 */

package com.sun.xml.ws.security.trust.util;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.secconv.WSSCElementFactory;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.trust.impl.bindings.AttributedURI;
import com.sun.xml.ws.security.trust.impl.bindings.EndpointReference;


import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import javax.xml.soap.SOAPFault;
import javax.xml.bind.JAXBElement;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.ws.api.message.Message;

import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.trust.WSTrustSOAPFaultException;
import com.sun.xml.ws.security.secconv.WSSCConstants;

import com.sun.xml.wss.core.SecurityContextTokenImpl;

import org.w3c.dom.NodeList;

/**
 *
 * @author ws-trust-implementation-team
 */
public class WSTrustUtil {
    
    private WSTrustUtil(){
        //private constructor
    }
    
    /**
     *create and return a SOAP 1.1 Fault corresponding to this exception
     */
    public static SOAPFault createSOAP11Fault(final WSTrustSOAPFaultException sfex){
        
        throw new UnsupportedOperationException("To Do");
    }
    
    /**
     *create and return a SOAP 1.2 Fault corresponding to this exception
     */
    public static SOAPFault createSOAP12Fault(final WSTrustSOAPFaultException sfex){
        
        throw new UnsupportedOperationException("To Do");
    }
    
    /*public static String getSecurityContext(final Message msg){
        
        try {
            final SOAPMessage soapMessage = msg.readAsSOAPMessage();
            final SOAPHeader header = soapMessage.getSOAPHeader();
            if (header != null){
                final NodeList list = header.getElementsByTagNameNS(WSSCConstants.WSC_NAMESPACE, 
                                                  WSSCConstants.SECURITY_CONTEXT_TOKEN);
                SOAPElement sctElement = null;
                if (list.getLength() > 0) {
                    sctElement = (SOAPElement)list.item(0);
                }
        
                if (sctElement != null){
                    final SecurityContextToken sct = new SecurityContextTokenImpl(sctElement);
        
                    return sct.getIdentifier().toString();   
                }
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        
        return null;
    } */

    public static byte[] generateRandomSecret(final int keySize) {        
        // Create binary secret
        final SecureRandom random = new SecureRandom();
        final byte[] secret = new byte[(int)keySize];
        random.nextBytes(secret);
        return secret;
    }
    
   public static SecurityContextToken createSecurityContextToken(final WSSCElementFactory eleFac) throws WSSecureConversationException{
       final String identifier = "urn:uuid:" + UUID.randomUUID().toString();
       URI idURI;
       try{
           idURI = new URI(identifier);
       }catch (URISyntaxException ex){
           throw new WSSecureConversationException(ex.getMessage(), ex);
       }
       final String wsuId = "uuid-" + UUID.randomUUID().toString();
       
       return eleFac.createSecurityContextToken(idURI, null, wsuId);
   }
   
   public static AppliesTo createAppliesTo(final String appliesTo){
       final AttributedURI uri = new AttributedURI();
       uri.setValue(appliesTo);
       final EndpointReference epr = new EndpointReference();
       epr.setAddress(uri);
       final AppliesTo applTo = (new com.sun.xml.ws.policy.impl.bindings.ObjectFactory()).createAppliesTo();
       applTo.getAny().add((new com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory()).createEndpointReference(epr));
       
       return applTo;
   }
   
   public static String getAppliesToURI(final AppliesTo appliesTo){
       final List list = appliesTo.getAny();
       EndpointReference epr = null;
       if (!list.isEmpty()){
            for (int i = 0; i < list.size(); i++) {
                final Object obj = list.get(i);
                if (obj instanceof EndpointReference){
                    epr = (EndpointReference)obj;
                } else if (obj instanceof JAXBElement){
                    final JAXBElement ele = (JAXBElement)obj;    
                    final String local = ele.getName().getLocalPart();
                    if (local.equalsIgnoreCase("EndpointReference")) {
                        epr = (EndpointReference)ele.getValue();
                    }
                }
                
                if (epr != null){
                    final AttributedURI uri = epr.getAddress();
                    if (uri != null){
                        return uri.getValue();
                    }
                }
            }
        }
        return null;
    }
   
    public static boolean isMetadata(final PolicyAssertion assertion ) {
        if ( !isMEXNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(Metadata)) {
            return true;
        }
        
        return false;
    }
    
    public static final String MEX_NS = "http://schemas.xmlsoap.org/ws/2004/09/mex";
    public static final String Metadata = "Metadata";
    public static final String MetadataSection = "MetadataSection";
    public static final String MetadataReference = "MetadataReference";
    
    public static boolean isMEXNS(final PolicyAssertion assertion) {
        if ( MEX_NS.equals(assertion.getName().getNamespaceURI()) ) {
            return true;
        }
        return false;
    }

    public static boolean isMetadataSection(final PolicyAssertion assertion) {
        if ( !isMEXNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(MetadataSection)) {
            return true;
        }
        
        return false;
    }

    public static boolean isMetadataReference(final PolicyAssertion assertion) {
        if ( !isMEXNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(MetadataReference)) {
            return true;
        }
        
        return false;
    }

    public static boolean isAddressingMetadata(final PolicyAssertion assertion) {
        if ( !PolicyUtil.isAddressingNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(Metadata)) {
            return true;
        }        
        return false;
    }
    
   
}
