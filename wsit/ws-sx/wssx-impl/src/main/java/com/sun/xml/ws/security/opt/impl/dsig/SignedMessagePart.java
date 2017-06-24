/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * SignedMessagePart.java
 *
 * Created on August 24, 2006, 2:19 PM
 */

package com.sun.xml.ws.security.opt.impl.dsig;

import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SignedData;
import com.sun.xml.ws.security.opt.impl.message.SOAPBody;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class SignedMessagePart implements SecurityElement, SignedData, SecurityElementWriter{
    protected boolean isCanonicalized = false;
    private SecurityElement se = null;
    private SOAPBody body = null;
    private boolean contentOnly = false;
    private List attributeValuePrefixes = null;
    
    private ByteArrayOutputStream storedStream = new ByteArrayOutputStream();
    
    protected byte[] digestValue = null;
    
    /** Creates a new instance of SignedMessagePart */
    public SignedMessagePart(){
    }
    
    public SignedMessagePart(SecurityElement se) {
        this.se = se;
    }
    
    public SignedMessagePart(SOAPBody body, boolean contentOnly){
        this.body = body;
        this.contentOnly = contentOnly;
    }
    
    public String getId() {
        if(body != null){
            if(!contentOnly){
                return body.getId();
            } else{
                return body.getBodyContentId();
            }
        }else{
            return se.getId();
        }
    }
    
    public void setId(String id) {
        if(body != null){
            if(!contentOnly){
                body.setId(id);
            } else{
                body.setBodyContentId(id);
            }
        }else{
            se.setId(id);
        }
    }
    
    public String getNamespaceURI() {
        if(body != null){
            if(!contentOnly){
                return body.getSOAPVersion().nsUri;
            } else {
                return body.getPayloadNamespaceURI();
            }
        }else {
            return se.getNamespaceURI();
        }
    }
    
    public String getLocalPart() {
        if(body != null){
            if(!contentOnly){
                return "Body";
            } else {
                return body.getPayloadLocalPart();
            }
        }else {
            return se.getLocalPart();
        }
    }
    
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(OutputStream os) {
        try{
            if(isCanonicalized)
                writeCanonicalized(os);
        } catch(IOException ioe){
            throw new XWSSecurityRuntimeException(ioe);
        }
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {
        if(body != null){
            body.cachePayLoad();//will be replaced with 2nd round of optimization.  
            attributeValuePrefixes = body.getAttributeValuePrefixes();
            if(!contentOnly){
                body.writeTo(streamWriter);
            }else{
                body.writePayload(streamWriter);
            }            
        }else{
            ((SecurityElementWriter)se).writeTo(streamWriter);
        }
        
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {     
        writeTo(streamWriter);
    }
    
    public void writeCanonicalized(OutputStream os) throws IOException{
        if(storedStream == null)
            return;
        storedStream.writeTo(os);
    }
    
    public void setDigestValue(byte[] digestValue) {
        this.digestValue = digestValue;
    }
    
    public byte[] getDigestValue() {
        return digestValue;
    }
    
    public List getAttributeValuePrefixes(){
        return attributeValuePrefixes;
    }
}
