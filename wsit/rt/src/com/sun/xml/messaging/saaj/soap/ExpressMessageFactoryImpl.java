/*
 * $Id: ExpressMessageFactoryImpl.java,v 1.1 2010-03-20 12:34:58 kumarjayanti Exp $
 * $Revision: 1.1 $
 * $Date: 2010-03-20 12:34:58 $
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
package com.sun.xml.messaging.saaj.soap;


import com.sun.xml.security.jaxws.JAXWSMessage;
import com.sun.xml.messaging.saaj.soap.ver1_1.Message1_1Impl;
import com.sun.xml.messaging.saaj.soap.ver1_2.Message1_2Impl;
//import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.spi.runtime.SOAPMessageContext;
//import com.sun.xml.ws.spi.runtime.InternalSoapEncoder;
import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.xml.soap.*;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ParseException;
import com.sun.xml.messaging.saaj.SOAPExceptionImpl;
import com.sun.xml.messaging.saaj.util.LogDomainConstants;
import com.sun.xml.messaging.saaj.util.TeeInputStream;
import com.sun.xml.messaging.saaj.soap.ver1_1.ExpressMessage1_1Impl;
import com.sun.xml.messaging.saaj.soap.ver1_2.ExpressMessage1_2Impl;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.ws.handler.MessageContext;
//import javax.xml.ws.handler.soap.SOAPMessageContext;




/**
 * A factory for creating SOAP messages.
 *
 * Converted to a placeholder for common functionality between SOAP
 * implementations.
 *
 * @author Phil Goodwin (phil.goodwin@sun.com)
 */
public class ExpressMessageFactoryImpl extends MessageFactory {
    
    private static boolean debug = false;
    protected static final Logger log =
            Logger.getLogger(LogDomainConstants.SOAP_DOMAIN,
            "com.sun.xml.messaging.saaj.soap.LocalStrings");
    
    protected static OutputStream listener;
    private static XMLOutputFactory staxOF = null;
    
    public ExpressMessageFactoryImpl()throws javax.xml.stream.FactoryConfigurationError{
        super();
    }
    
    public static OutputStream listen(OutputStream newListener) {
        OutputStream oldListener = listener;
        listener = newListener;
        return oldListener;
    }
    
    public SOAPMessage createMessage() throws SOAPException {
        throw new UnsupportedOperationException();
    }
    
    public SOAPMessage createMessage(boolean isFastInfoset,
            boolean acceptFastInfoset) throws SOAPException {
        throw new UnsupportedOperationException();
    }
    
    public SOAPMessage createMessage(MimeHeaders headers, InputStream in)
    throws SOAPException, IOException {
        String contentTypeString = MessageImpl.getContentType(headers);
        
        if (listener != null) {
            in = new TeeInputStream(in, listener);
        }
        
        try {
            ContentType contentType = new ContentType(contentTypeString);
            int stat = MessageImpl.identifyContentType(contentType);
            if (MessageImpl.isSoap1_1Content(stat)) {
                return new ExpressMessage1_1Impl(headers, in);
            } else if (MessageImpl.isSoap1_2Content(stat)) {
                return new ExpressMessage1_2Impl(headers, in);
            } else {
                log.severe("SAAJ0530.soap.unknown.Content-Type");
                throw new SOAPExceptionImpl("Unrecognized Content-Type");
            }
        } catch (ParseException e) {
            log.severe("SAAJ0531.soap.cannot.parse.Content-Type");
            throw new SOAPExceptionImpl(
                    "Unable to parse content type: " + e.getMessage());
        }
    }
    
    public static SOAPMessage createMessage(SOAPMessageContext messageContext) throws SOAPException, IOException {
        MimeHeaders headers = getMimeHeader(messageContext);
        return createMessage(headers,messageContext,true);
    }
    public static SOAPMessage createMessage(SOAPMessageContext messageContext,boolean optimized) throws SOAPException, IOException {
        MimeHeaders headers = getMimeHeader(messageContext);
        return createMessage(headers,messageContext,optimized);
    }
    
    public static SOAPMessage createMessage(MimeHeaders headers,SOAPMessageContext messageContext,boolean optimized) throws SOAPException, IOException {
        JAXWSMessage jxMessage = null;
        try {
            String fiValue = (String) messageContext.get("com.sun.xml.ws.client.ContentNegotiation");
            if(fiValue != null &&  fiValue.length() > 0 ){
                if("optimistic".equals(fiValue)){
                    SOAPMessage msg = constructSOAPMessage(messageContext);
                    if (debug) {
                        System.out.println("CONSTRUCT SOAP");
                    }
                    return msg;
                }
            }
            if(!optimized){
                return constructSOAPMessage(messageContext);
            }
            
            SOAPMessageContext mc = messageContext;
            jxMessage = new JAXWSMessage();
            jxMessage.setEncoder(mc.getEncoder());
            jxMessage.setMessageInfo(mc.getMessageInfo());
            jxMessage.setHeaders(mc.getHeaders());
            jxMessage.setBody(mc.getBody());
            String soapVersion = messageContext.getBindingId();
            String contentTypeString = MessageImpl.getContentType(headers);
            ContentType contentType = new ContentType(contentTypeString);
            int stat = MessageImpl.identifyContentType(contentType);
            SOAPMessage sm = null;
            // if (MessageImpl.isSoap1_1Content(stat)) {
            if(soapVersion == javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING){
                sm =  new ExpressMessage1_1Impl(headers, jxMessage);
                jxMessage.setSoapMessage(sm);
                addAttachments(mc,sm);
            } else if (soapVersion == javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING) {
                sm =  new ExpressMessage1_2Impl(headers, jxMessage);
                jxMessage.setSoapMessage(sm);
                addAttachments(mc,sm);
            } else {
                log.severe("SAAJ0530.soap.unknown.Content-Type");
                throw new SOAPExceptionImpl("Unrecognized Content-Type");
            }
            if(fiValue == "optimistic"){
                ((MessageImpl)sm).setIsFastInfoset(true);
            }
            return sm;
        }catch (ParseException e) {
            
            log.severe("SAAJ0531.soap.cannot.parse.Content-Type");
            throw new SOAPExceptionImpl(
                    "Unable to parse content type: " + e.getMessage());
            
        } catch(Exception ex){
            throw new SOAPException(ex);
        }
    }
    
    private static void addAttachments(SOAPMessageContext msgContext , SOAPMessage sm){
        Map attachments = (Map)msgContext.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        if(attachments != null){
            Set keys = attachments.keySet();
            Iterator itr = keys.iterator();
            while(itr.hasNext()){
                String cid = (String) itr.next();                
                DataHandler dh = (DataHandler)attachments.get(cid);
                AttachmentPart attachmentPart = sm.createAttachmentPart();
                attachmentPart.setDataHandler(dh);
                attachmentPart.setContentId(cid);
                attachmentPart.setMimeHeader("Content-transfer-encoding", "binary");
                sm.addAttachmentPart(attachmentPart);
            }
        }
//        Collection values = attachments.values();
//        Iterator itr = values.iterator();
//        while(itr.hasNext()){
//            AttachmentPart attachment = (AttachmentPart)itr.next();
//            sm.addAttachmentPart(attachment);
//        }
    }
    protected static final String getContentType(MimeHeaders headers) {
        String[] values = headers.getHeader("Content-Type");
        if (values == null){
            return null;
        }else{
            return values[0];
        }
    }
    
    private static SOAPMessage constructSOAPMessage(com.sun.xml.ws.spi.runtime.SOAPMessageContext msgContext) throws SOAPException{
        String soapVersion = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;
        soapVersion = msgContext.getBindingId();
        if (debug) {
            System.out.println("SOAP Version"+soapVersion);
        }
        SOAPMessage sm  = null;
        if(soapVersion == null || soapVersion == javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING){
            return JAXWSMessage.constructSOAPMessage(new Message1_1Impl(), msgContext);
        }else{
            return JAXWSMessage.constructSOAPMessage(new Message1_2Impl(), msgContext);
        }
    }
    
    
    private static MimeHeaders getMimeHeader(com.sun.xml.ws.spi.runtime.SOAPMessageContext msgContext){
        String fiValue = (String) msgContext.get("com.sun.xml.ws.client.ContentNegotiation");
        String soapVersion = msgContext.getBindingId();
        SOAPMessage sm  = null;
        
        MimeHeaders mh = new MimeHeaders();
        if(soapVersion == javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING){
            //System.out.println("XML Message SOAP11");
            if("optimistic" == fiValue){
                if (debug) {
                    System.out.println("FI ON");
                }
                mh.addHeader("Content-Type", JAXWSMessage.FAST_INFOSET_TYPE_SOAP11);
                return mh;
            }
            
            if(msgContext.isMtomEnabled()){
                //System.out.println("MTOM ON");
                mh.addHeader("Content-Type", JAXWSMessage.XOP_SOAP11_XML_TYPE_VALUE);
                return mh;
            }
            mh.addHeader("Content-Type", SOAPConstants.SOAP_1_1_CONTENT_TYPE);
            //System.out.println("XML Message");
            return mh;
        }else{
            // System.out.println("XML Message SOAP12");
            if("optimistic" == fiValue){
                // System.out.println("FI ON");
                mh.addHeader("Content-Type", JAXWSMessage.FAST_INFOSET_TYPE_SOAP12);
                return mh;
            }
            if(msgContext.isMtomEnabled()){
                // System.out.println("MTOM ON");
                mh.addHeader("Content-Type", JAXWSMessage.XOP_SOAP12_XML_TYPE_VALUE);
                return mh;
            }
            //System.out.println("XML Message");
            mh.addHeader("Content-Type", SOAPConstants.SOAP_1_2_CONTENT_TYPE);
            return mh;
        }
    }
}
