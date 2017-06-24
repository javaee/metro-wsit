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

package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.xml.ws.security.opt.api.PolicyBuilder;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.incoming.processor.ReferenceListProcessor;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class ReferenceListHeader implements SecurityHeaderElement, SecurityElementWriter, PolicyBuilder{

    //private static String DATA_REFERENCE = "DataReference".intern();
    private static final int DATA_REFERENCE_ELEMENT = 1;
    
    private String id = "";
    private String namespaceURI = "";
    private String localName = "";
    private JAXBFilterProcessingContext pc = null;
    private ArrayList<String> referenceList = null;
    private ArrayList<String> pendingRefList = null;
    
    private EncryptionPolicy encPolicy = null;
    
    /** Creates a new instance of ReferenceListHeader */
    public ReferenceListHeader(XMLStreamReader reader,JAXBFilterProcessingContext pc) throws XMLStreamException{
        this.pc = pc;
        encPolicy = new EncryptionPolicy();
        encPolicy.setFeatureBinding(new EncryptionPolicy.FeatureBinding());
        process(reader);
    }

    public boolean refersToSecHdrWithId(String id) {
        throw new UnsupportedOperationException();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        throw new UnsupportedOperationException();
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getLocalPart() {
        return localName;
    }

    public XMLStreamReader readHeader() throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException();
    }

    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
   
    public List<String> getReferenceList() {
        return referenceList;
    }
    
    public List<String> getPendingReferenceList() {
        return pendingRefList;
    }
    @SuppressWarnings("unchecked")
    private void process(XMLStreamReader reader) throws XMLStreamException{
        id = reader.getAttributeValue(null,"Id");
        namespaceURI = reader.getNamespaceURI();
        localName = reader.getLocalName();
        
        ReferenceListProcessor rlp = new ReferenceListProcessor(encPolicy);
        rlp.process(reader);
        referenceList = rlp.getReferences();
        pendingRefList = (ArrayList<String>) referenceList.clone();
    }

    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public WSSPolicy getPolicy() {
        return encPolicy;
    }
    
}
