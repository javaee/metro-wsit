/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.xml.soap.SOAPFault;
import javax.xml.bind.JAXBElement;

import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.secconv.WSSCElementFactory13;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustSOAPFaultException;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
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
    
   public static SecurityContextToken createSecurityContextToken(final WSTrustElementFactory wsscEleFac) throws WSSecureConversationException{
       final String identifier = "urn:uuid:" + UUID.randomUUID().toString();
       URI idURI;
       try{
           idURI = new URI(identifier);
       }catch (URISyntaxException ex){
           throw new WSSecureConversationException(ex.getMessage(), ex);
       }
       final String wsuId = "uuid-" + UUID.randomUUID().toString();
       if(wsscEleFac instanceof com.sun.xml.ws.security.secconv.WSSCElementFactory){
           return ((WSSCElementFactory)wsscEleFac).createSecurityContextToken(idURI, null, wsuId);
       }else if(wsscEleFac instanceof com.sun.xml.ws.security.secconv.WSSCElementFactory13){
           return ((WSSCElementFactory13)wsscEleFac).createSecurityContextToken(idURI, null, wsuId);
       }
       return null;
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
   
   /*
   public static SecurityContextToken createSecurityContextToken(final WSSCElementFactory13 eleFac) throws WSSecureConversationException{
       final String identifier = "urn:uuid:" + UUID.randomUUID().toString();
       URI idURI;
       try{
           idURI = new URI(identifier);
       }catch (URISyntaxException ex){
           throw new WSSecureConversationException(ex.getMessage(), ex);
       }
       final String wsuId = "uuid-" + UUID.randomUUID().toString();
       
       return eleFac.createSecurityContextToken(idURI, null, wsuId);
   }*/
   
   public static SecurityContextToken createSecurityContextToken(final WSTrustElementFactory wsscEleFac, final String identifier) throws WSSecureConversationException{       
       URI idURI;
       try{
           idURI = new URI(identifier);
       }catch (URISyntaxException ex){
           throw new WSSecureConversationException(ex.getMessage(), ex);
       }
       final String wsuId = "uuid-" + UUID.randomUUID().toString();
       final String wsuInstance = "uuid-" + UUID.randomUUID().toString();

       if(wsscEleFac instanceof com.sun.xml.ws.security.secconv.WSSCElementFactory){
           return ((WSSCElementFactory)wsscEleFac).createSecurityContextToken(idURI, wsuInstance, wsuId);
       }else if(wsscEleFac instanceof com.sun.xml.ws.security.secconv.WSSCElementFactory13){
           return ((WSSCElementFactory13)wsscEleFac).createSecurityContextToken(idURI, wsuInstance, wsuId);
       }
       return null;       
   }
   
   public static SecurityContextToken createSecurityContextToken(final WSSCElementFactory eleFac, final String identifier) throws WSSecureConversationException{       
       URI idURI;
       try{
           idURI = new URI(identifier);
       }catch (URISyntaxException ex){
           throw new WSSecureConversationException(ex.getMessage(), ex);
       }
       final String wsuId = "uuid-" + UUID.randomUUID().toString();
       final String wsuInstance = "uuid-" + UUID.randomUUID().toString();
       
       return eleFac.createSecurityContextToken(idURI, wsuInstance, wsuId);
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
   
   public static List<Object> parseAppliesTo(final AppliesTo appliesTo){
       final List<Object> list = appliesTo.getAny();
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
                
                List<Object> result = new ArrayList<Object>();
                if (epr != null){
                    final AttributedURI uri = epr.getAddress();
                    if (uri != null){
                        result.add(uri.getValue());
                    }
                    final List<Object> content = epr.getAny();
                    for (int j = 0; j < content.size(); j++) {
                        final Object obj2 = content.get(j);
                        if (obj2 instanceof Element){
                            Element ele = (Element)obj2;
                            NodeList nodeList = ele.getElementsByTagName("Identity");
                            if (nodeList.getLength() > 0){
                                Element identity = (Element)nodeList.item(0);
                                result.add(identity); 
                            }
                        }
                    }
                }
            }
        }
        return null;
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
    
    public static String createFriendlyPPID(String displayValue){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] hashId = md.digest(com.sun.xml.wss.impl.misc.Base64.decode(displayValue));
            StringBuffer sb = new StringBuffer();
            
        }catch(Exception ex){
            return displayValue;
        }
        return displayValue;
    }
    
    public static String elemToString(final RequestSecurityTokenResponse rstr, final WSTrustVersion wstVer){
        StringWriter writer = new StringWriter();
        try{
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(WSTrustElementFactory.newInstance(wstVer).toSource(rstr), new StreamResult(writer));
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        return writer.toString();
    }

    public static String elemToString(final RequestSecurityToken rst, final WSTrustVersion wstVer){
        StringWriter writer = new StringWriter();
        try{
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(WSTrustElementFactory.newInstance(wstVer).toSource(rst), new StreamResult(writer));
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        return writer.toString();
    }      
    
    private static final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'", Locale.getDefault());
    
    public static long getCurrentTimeWithOffset(){
        final Calendar cal = new GregorianCalendar();
        int offset = cal.get(Calendar.ZONE_OFFSET);
        if (cal.getTimeZone().inDaylightTime(cal.getTime())) {
            offset += cal.getTimeZone().getDSTSavings();
        }
        
         // always send UTC/GMT time
         final long beforeTime = cal.getTimeInMillis();
         
         return beforeTime - offset;
    }
    
    public static Lifetime createLifetime(long currentTime, long lifespan, WSTrustVersion wstVer) {
        final Calendar cal = new GregorianCalendar();
        synchronized (calendarFormatter) {
            calendarFormatter.setTimeZone(cal.getTimeZone());
            cal.setTimeInMillis(currentTime);
            
            final AttributedDateTime created = new AttributedDateTime();
            created.setValue(calendarFormatter.format(cal.getTime()));
            
            final AttributedDateTime expires = new AttributedDateTime();
            cal.setTimeInMillis(currentTime + lifespan);
            expires.setValue(calendarFormatter.format(cal.getTime()));
            
            final Lifetime lifetime = WSTrustElementFactory.newInstance(wstVer).createLifetime(created, expires);
            
            return lifetime;
        }
    }
}
