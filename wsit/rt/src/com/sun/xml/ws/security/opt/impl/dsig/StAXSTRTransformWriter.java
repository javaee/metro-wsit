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

package com.sun.xml.ws.security.opt.impl.dsig;

import com.sun.xml.ws.security.opt.crypto.JAXBData;
import com.sun.xml.ws.security.opt.crypto.StreamWriterData;
import com.sun.xml.ws.security.opt.impl.crypto.OctectStreamData;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.crypto.Data;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.NamespaceContextEx;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class StAXSTRTransformWriter implements XMLStreamWriter,StreamWriterData{
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    private XMLStreamWriter nextWriter = null;
    private boolean ignore = false;
    private boolean derefSAMLKeyIdentifier = false;
    
    private Data data = null;
    private int index = 0;
    private NamespaceContextEx ns = null;
    private boolean directReference = false;
    private boolean first = true;
    private String directReferenceValue = "";
    private XMLCryptoContext xMLCryptoContext;
    private String strId = "";
    private JAXBFilterProcessingContext filterContext ;    
    
    /** Creates a new instance of StAXEnvelopedTransformWriter */
    public StAXSTRTransformWriter(XMLStreamWriter writer,Data data,XMLCryptoContext xMLCryptoContext) {
        this.nextWriter = writer;
        this.data = data;
        if(data instanceof JAXBData){
            ns = ((JAXBData)data).getNamespaceContext();
        }else if(data instanceof StreamWriterData){
            ns = ((StreamWriterData)data).getNamespaceContext();
        }
        this.xMLCryptoContext = xMLCryptoContext;
        filterContext = (JAXBFilterProcessingContext) xMLCryptoContext.get(MessageConstants.WSS_PROCESSING_CONTEXT);
    }
    
    public StAXSTRTransformWriter(Data data,XMLCryptoContext xMLCryptoContext,String refId) {
        this.data = data;
        if(data instanceof JAXBData){
            ns = ((JAXBData)data).getNamespaceContext();
        }else if(data instanceof StreamWriterData){
            ns = ((StreamWriterData)data).getNamespaceContext();
        }
        this.xMLCryptoContext = xMLCryptoContext;
        this.strId = refId;
        filterContext = (JAXBFilterProcessingContext) xMLCryptoContext.get(MessageConstants.WSS_PROCESSING_CONTEXT);        
    }
    
    public NamespaceContextEx getNamespaceContext() {
        return ns;
    }
    
    public void close() throws XMLStreamException {
        nextWriter.close();
    }
    
    public void flush() throws XMLStreamException {
        nextWriter.flush();
    }
    
    public void writeEndDocument() throws XMLStreamException {
        if(index >0){
            int size = index;
            for(int i=0;i<size;i++){
                writeEndElement();
            }
        }
        nextWriter.writeEndDocument();
    }
    
    public void writeEndElement() throws XMLStreamException {
        if(index == 1 && !ignore ){            
            nextWriter.writeEndElement();            
        }
        if(index > 0){
            index --;
        }
        if(index == 0){
            if(ignore){
                ignore = false;
                derefernceSTR();                            
                
            }
            nextWriter.writeEndElement();
            if (derefSAMLKeyIdentifier){
                derefSAMLKeyIdentifier = false;
            }
            if(directReference){
                directReference = false;
            }
            
            return;
        }                
    }
 
    public void writeStartDocument() throws XMLStreamException {
        if(!ignore){
            nextWriter.writeStartDocument();
        }
    }
    
    public void writeCharacters(char[] c, int index, int len) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeCharacters(c,index,len);
        }else{
            if(derefSAMLKeyIdentifier){
                this.strId = String.valueOf(c, index, len);
                if (this.strId == null){
                    throw new XMLStreamException("SAML Key Identifier is empty in SecurityTokenReference");
                }
            }
        }
    }
    
    public void setDefaultNamespace(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.setDefaultNamespace(string);
        }
    }
    
    public void writeCData(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeCData(string);
        }
    }
    
    public void writeCharacters(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeCharacters(string);        
        }else{
            if(derefSAMLKeyIdentifier){
                this.strId = string;
                if (this.strId == null){
                    throw new XMLStreamException("SAML Key Identifier is empty in SecurityTokenReference");
                }
            }
        }
    }
    
    public void writeComment(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeComment(string);
        }
    }
    
    public void writeDTD(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeDTD(string);
        }
    }
    
    public void writeDefaultNamespace(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeDefaultNamespace(string);
        }
    }
    
    public void writeEmptyElement(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeEmptyElement(string);
        }
    }
    
    public void writeEntityRef(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeEntityRef(string);
        }
    }
    
    public void writeProcessingInstruction(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeProcessingInstruction(string);
        }
    }
    
    public void writeStartDocument(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeStartDocument(string);
        }
    }
    
    public void writeStartElement(String string) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeStartElement(string);
        }
        first = false;
    }
    
    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        if(!ignore){
            nextWriter.setNamespaceContext(namespaceContext);
        }
    }
    
    public Object getProperty(String string) throws IllegalArgumentException {
        return nextWriter.getProperty(string);
    }
    
    public String getPrefix(String string) throws XMLStreamException {
        return nextWriter.getPrefix(string);
    }
    
    public void setPrefix(String string, String string0) throws XMLStreamException {
        if(!ignore){
            nextWriter.setPrefix(string,string0);
        }
    }
    
    public void writeAttribute(String localname, String value) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeAttribute(localname,value);            
        }else{
            if(directReference){
                if(localname == MessageConstants.WSSE_REFERENCE_ATTR_URI){
                    directReferenceValue = value;
                }
            }else if(MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(value) ||
                            MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE.equals(value)){                    
                    derefSAMLKeyIdentifier = true;                              
            }
        }
    }
    
    public void writeEmptyElement(String string, String string0) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeEmptyElement(string,string0);
        }
    }
    
    public void writeNamespace(String string, String string0) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeNamespace(string,string0);
        }
    }
    
    public void writeProcessingInstruction(String string, String string0) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeProcessingInstruction(string,string0);
        }
    }
    
    public void writeStartDocument(String string, String string0) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeStartDocument(string,string0);
        }
    }
    
    public void writeStartElement(String namespaceURI, String localName)  throws XMLStreamException {
        if(!ignore){
            if(first && localName == MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME && namespaceURI == MessageConstants.WSSE_NS){
                ignore = true;
                index ++;
                return;
            }
            nextWriter.writeStartElement(namespaceURI,localName);
        }else{
            index ++;
        }
        first = false;
    }
    
    public void writeAttribute(String uri, String localname, String value) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeAttribute(uri,localname,value);
        }else{
            if(directReference){
                if(localname == MessageConstants.WSSE_REFERENCE_ATTR_URI){
                    directReferenceValue = value;
                }
            }
        }
    }
    
    public void writeEmptyElement(String string, String string0, String string1) throws XMLStreamException {
        if(!ignore){
            nextWriter.writeEmptyElement(string,string0,string1);
        }
    }
    
    public void writeStartElement(String prefix, String localName, String namespaceURI)  throws XMLStreamException {
        if(!ignore){
            // Set ignore to true only if STR is top level element
            if(first && localName == MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME && namespaceURI == MessageConstants.WSSE_NS){
                ignore = true;
                index ++;
                return;
            }else if(first && localName == MessageConstants.WSSE_REFERENCE_LNAME && namespaceURI == MessageConstants.WSSE_NS){
                ignore = true;
                index ++;
                directReference = true;
                nextWriter.writeNamespace(prefix,namespaceURI);
                return;            
            }else if(first && localName == MessageConstants.KEYIDENTIFIER && namespaceURI == MessageConstants.WSSE_NS){
                ignore = true;
                index++;                
                nextWriter.writeNamespace(prefix,namespaceURI);                
            }else{
                nextWriter.writeStartElement(prefix,localName,namespaceURI);
            }
        }else{            
            if(localName == MessageConstants.WSSE_REFERENCE_LNAME && namespaceURI == MessageConstants.WSSE_NS){                
                index ++;
                directReference = true;
            }else if (localName == MessageConstants.KEYIDENTIFIER && namespaceURI == MessageConstants.WSSE_NS){                
                index ++;
                nextWriter.writeNamespace(prefix, namespaceURI);                             
            }else {
                nextWriter.writeStartElement(prefix,localName,namespaceURI);
            }
        }
        first = false;
    }
 
    public void writeAttribute(String prefix, String uri, String localName, String value) throws XMLStreamException {
        if(!ignore){
	    nextWriter.writeNamespace(prefix,uri);
            nextWriter.writeAttribute(prefix,uri,localName,value);
        }else{
            if(directReference){
                if(localName == MessageConstants.WSSE_REFERENCE_ATTR_URI){
                    directReferenceValue = value;
                }
            }
        }
    }
    /**
     *transforms the data using STR transform and writes it to the data 
     * @param writer XMLStreamWriter
     * @throws XMLStreamException
     */
    public void write(XMLStreamWriter writer) throws XMLStreamException {
        this.nextWriter = writer;
        boolean defaultNSDecl = false;
        if(data instanceof JAXBData){
            try {
                ((JAXBData)data).writeTo(this);
                NamespaceContextEx nc  = ((JAXBData)data).getNamespaceContext();
                Iterator<NamespaceContextEx.Binding> itr = nc.iterator();
                while(itr.hasNext()){
                    final NamespaceContextEx.Binding nd = itr.next();
                    
                    nextWriter.writeNamespace(nd.getPrefix(),nd.getNamespaceURI());
                    if (nd.getPrefix() == null || nd.getPrefix().equals("")) {
                        defaultNSDecl = true;
                    }
                }
                if (!defaultNSDecl) {
                    if (nextWriter instanceof StAXEXC14nCanonicalizerImpl) {
                        ((StAXEXC14nCanonicalizerImpl) nextWriter).forceDefaultNS(true);
                        nextWriter.writeDefaultNamespace("");
                    //((StAXEXC14nCanonicalizerImpl)nextWriter).forceDefaultNS(false);
                    }
                }
            } catch (XWSSecurityException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1706_ERROR_ENVELOPED_SIGNATURE(),ex);
                throw new XMLStreamException("Error occurred while performing Enveloped Signature");
            }
        }else if(data instanceof StreamWriterData){
            StreamWriterData swd = (StreamWriterData)data;
            NamespaceContextEx nc  = swd.getNamespaceContext();
            Iterator<NamespaceContextEx.Binding> itr = nc.iterator();
            
            while(itr.hasNext()){
                final NamespaceContextEx.Binding nd = itr.next();
                
                nextWriter.writeNamespace(nd.getPrefix(),nd.getNamespaceURI());
                if (nd.getPrefix() == null || nd.getPrefix().equals("")) {
                    defaultNSDecl = true;
                }
            }
            if (!defaultNSDecl) {
                if(nextWriter instanceof StAXEXC14nCanonicalizerImpl){
                    ((StAXEXC14nCanonicalizerImpl)nextWriter).forceDefaultNS(true);
                    nextWriter.writeDefaultNamespace("");
                    //((StAXEXC14nCanonicalizerImpl)nextWriter).forceDefaultNS(false);
                }                
            }
            ((StreamWriterData)data).write(this);
        }else if(data instanceof OctectStreamData){
            ((OctectStreamData)data).write(this);
        }
    }
    /**
     * tries to dereference the SecurityTokenReference element
     * @throws XMLStreamException
     */
    void derefernceSTR()throws XMLStreamException{
        Data token = null;
        URIDereferencer deRef = xMLCryptoContext.getURIDereferencer();
        final String uri ;
        if(directReference ){
            uri = directReferenceValue;
        }else if(strId != null && strId.length() >0){
            uri = strId;
        }else {
            uri = "";
        }
        
        URIReference ref = new URIReference() {
            public String getType() {
                return "";
            }
            public String getURI() {
                return uri;
            }
        };
        
        try{
            token = deRef.dereference(ref,xMLCryptoContext);
        }catch(URIReferenceException ue){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1716_ERROR_DEREFERENCE_STR_TRANSFORM(),ue);
            throw new XMLStreamException("Error occurred while dereferencing STR-Transform's Reference Element", ue);
        }
        
        if(token != null){
            if(token instanceof JAXBData){
                try {
                    ((JAXBData)token).writeTo(this);
                } catch (XWSSecurityException ex) {
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1706_ERROR_ENVELOPED_SIGNATURE(),ex);
                    throw new XMLStreamException("Error occurred while performing Enveloped Signature");
                }
            }else if(token instanceof StreamWriterData){
                ((StreamWriterData)token).write(this);                
            }else if(token instanceof OctectStreamData){
                ((OctectStreamData)token).write(this);
            }
        }
    }
}
