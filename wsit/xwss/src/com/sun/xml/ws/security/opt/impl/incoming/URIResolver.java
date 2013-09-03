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

package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.crypto.AttachmentData;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.crypto.Data;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class URIResolver implements URIDereferencer{
    private SecurityContext securityContext;
    private JAXBFilterProcessingContext pc = null;
    /** Creates a new instance of resolver */
    public URIResolver(JAXBFilterProcessingContext pc) {
        this.pc = pc;
        this.securityContext = pc.getSecurityContext();
    }
    
    public Data dereference(URIReference uRIReference, XMLCryptoContext xMLCryptoContext) throws URIReferenceException {
        
        HeaderList headers = securityContext.getNonSecurityHeaders();
        String tmpId = uRIReference.getURI();
        
        if(tmpId.startsWith("cid:")){
            return dereferenceAttachments(tmpId.substring(4));
        }
        
        String id = "";
        int index = tmpId.indexOf("#");
        if( index >=0){
            id = tmpId.substring(index+1);
        }else{
            id = tmpId;
        }
        if(headers != null && headers.size() >0){
            Iterator<Header> listItr = headers.listIterator();
            boolean found = false;
            while(listItr.hasNext()){
                GenericSecuredHeader header = (GenericSecuredHeader)listItr.next();
                if(header.hasID(id) && !header.hasEncData()){
                    return new StreamWriterData(header,((NamespaceContextInfo)header).getInscopeNSContext());
                }
            }
        }
        
        ArrayList pshList =  securityContext.getProcessedSecurityHeaders();
        for(int j=0; j< pshList.size() ; j++){
            SecurityHeaderElement  header = (SecurityHeaderElement) pshList.get(j);
            if(id.equals(header.getId())){
                if(header instanceof NamespaceContextInfo){
                    return new StreamWriterData(header,((NamespaceContextInfo)header).getInscopeNSContext());
                }else{
                    throw new URIReferenceException("Cannot derefernce this MessagePart and use if for any crypto operation " +
                              "as the message part is not cached");
                }
            }
        }
        
        // looking into buffered headers for - (Should be used only for getting the key)
        // What will happen when encrypting the content but signing the entire element? Can go wrong
        ArrayList bufList =  securityContext.getBufferedSecurityHeaders();
        for(int j=0; j< bufList.size() ; j++){
            SecurityHeaderElement  header = (SecurityHeaderElement) bufList.get(j);
            if(id.equals(header.getId())){
                if(header instanceof NamespaceContextInfo){
                    return new StreamWriterData(header,((NamespaceContextInfo)header).getInscopeNSContext());
                }else{
                    throw new URIReferenceException("Cannot derefernce this MessagePart and use if for any crypto operation " +
                              "as the message part is not cached");
                }
            }
        }
        
        Data data = null;
        data = (Data)pc.getSTRTransformCache().get(id);
        if(data != null)
            return data;
        data = (Data)pc.getElementCache().get(id);
        return data;
    }

    private Data dereferenceAttachments(String cidRef)  throws URIReferenceException{
        AttachmentSet attachments = securityContext.getDecryptedAttachmentSet();
        if(attachments == null || attachments.isEmpty()){
            attachments = securityContext.getAttachmentSet();
        }
        if(attachments == null || attachments.isEmpty()){
            throw new URIReferenceException ("Attachment Resource with Identifier  "+cidRef+" was not found");
        }
        Attachment attachment = attachments.get(cidRef);
        if(attachment == null){
            throw new URIReferenceException ("Attachment Resource with Identifier  "+cidRef+" was not found");
        }
        AttachmentData attachData = new AttachmentData(attachment);
        return attachData;
    }
    
}
