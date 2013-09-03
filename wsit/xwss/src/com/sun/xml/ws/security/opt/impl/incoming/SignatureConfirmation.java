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

package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.api.PolicyBuilder;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.TokenValidator;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.SignatureConfirmationPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamReader;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferCreator;
import javax.xml.stream.XMLInputFactory;
import com.sun.xml.ws.security.opt.impl.util.XMLStreamReaderFactory;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import javax.xml.stream.StreamFilter;
import com.sun.xml.stream.buffer.XMLStreamBufferMark;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.LogDomainConstants;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class SignatureConfirmation implements SecurityHeaderElement, TokenValidator, PolicyBuilder, NamespaceContextInfo, SecurityElementWriter{
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.FILTER_DOMAIN,
            LogDomainConstants.FILTER_DOMAIN_BUNDLE);
    
    private String id = "";
    private String namespaceURI = "";
    private String localName = "";
    private String signatureValue = null;
    
    private SignatureConfirmationPolicy scPolicy = null;
    private HashMap<String,String> nsDecls;
    private XMLStreamBuffer mark = null;
    
    /**
     * Creates a new instance of SignatureConfirmation
     */
    public SignatureConfirmation(XMLStreamReader reader,StreamReaderBufferCreator creator,HashMap nsDecls, XMLInputFactory  staxIF) throws XMLStreamException{
        
        namespaceURI = reader.getNamespaceURI();
        localName = reader.getLocalName();
        id = reader.getAttributeValue(MessageConstants.WSU_NS,"Id");
        
        mark = new XMLStreamBufferMark(nsDecls,creator);
        creator.createElementFragment(XMLStreamReaderFactory.createFilteredXMLStreamReader(reader,new SCProcessor()),false);
        
        this.nsDecls = nsDecls;
        
        scPolicy = new SignatureConfirmationPolicy();
        scPolicy.setSignatureValue(signatureValue);
    }
    
    public String getSignatureValue(){
        return signatureValue;
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
        return localName;
    }    
   
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        return mark.readAsXMLStreamReader();
    }
    
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {
        mark.writeToXMLStreamWriter(streamWriter);
    }
    
    public void validate(ProcessingContext context) throws XWSSecurityException {
        Object temp = context.getExtraneousProperty("SignatureConfirmation");
        List scList = null;
        if(temp != null && temp instanceof ArrayList)
            scList = (ArrayList)temp;
        if(scList != null){
            if(signatureValue == null){
                if(!scList.isEmpty()){
                    log.log(Level.SEVERE, "Failure in SignatureConfirmation Validation");
                    throw new XWSSecurityException("Failure in SignatureConfirmation Validation");
                }
            }else if(scList.contains(signatureValue)){// match the Value in received message
                //with the stored value
                scList.remove(signatureValue);
            }else{
                log.log(Level.SEVERE, "Failure in SignatureConfirmation Validation");
                throw new XWSSecurityException("Mismatch in SignatureConfirmation Element");
            }
        }
    }
    
    public WSSPolicy getPolicy() {
        return scPolicy;
    }
    
    public HashMap<String, String> getInscopeNSContext() {
        return nsDecls;
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    class SCProcessor implements StreamFilter{
        boolean elementRead = false;
        public boolean accept(XMLStreamReader reader){
            if(reader.getEventType() == XMLStreamReader.END_ELEMENT ){
                if(reader.getLocalName() == localName && reader.getNamespaceURI() == namespaceURI){
                    elementRead = true;
                }
            }
            if(!elementRead && reader.getEventType() == XMLStreamReader.START_ELEMENT){
                signatureValue = reader.getAttributeValue(null,"Value");
            }
            return true;
        }
    }
    
}
