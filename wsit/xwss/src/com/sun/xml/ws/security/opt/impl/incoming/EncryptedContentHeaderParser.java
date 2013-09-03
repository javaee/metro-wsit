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

import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.c14n.AttributeNS;
import com.sun.xml.wss.impl.c14n.StAXAttr;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */

    class EncryptedContentHeaderParser{
        XMLStreamReader encContentReader = null;
        boolean parsed = false;
        String localName;
        String uri;
        String prefix;
        Vector attrList = new Vector();
        Vector attrNSList = new Vector();
        EncryptedData ed = null;

        private HashMap<String,String> parentNS = null;
        private JAXBFilterProcessingContext context = null;

        EncryptedContentHeaderParser(XMLStreamReader encContentReader, HashMap<String,String> parentNS,
                JAXBFilterProcessingContext context){
            this.encContentReader = encContentReader;
            this.parentNS = parentNS;
            this.context = context;
        }

        XMLStreamReader getDecryptedElement(InputStream decryptedIS) throws XMLStreamException, XWSSecurityException{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            writeStartElement(writer);
            writeEndElement(writer);
            writer.flush();
            writer.close();
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
            String outStr = out.toString();
            int pos = outStr.indexOf('>');
            String startElem = outStr.substring(0, pos+1);
            String endElem = outStr.substring(pos+1);
            try {
                tmpOut.write(startElem.getBytes());
                byte[] buf = new byte[4096];

                for(int len=-1;(len=decryptedIS.read(buf))!=-1;)
                    tmpOut.write(buf,0,len);

                tmpOut.write(endElem.getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            InputStream finalContent = new ByteArrayInputStream(tmpOut.toByteArray());
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader reader = xif.createXMLStreamReader(finalContent);
            return reader;
        }

        void writeStartElement(XMLStreamWriter xsw) throws XMLStreamException, XWSSecurityException{
            if(!parsed){
                parse();
            }
            xsw.writeStartElement(prefix,localName,uri);
            if(parentNS.containsKey(prefix)){
                xsw.writeNamespace(prefix, uri);
            }
            for(int i=0;i<attrNSList.size();i++){
                AttributeNS attrNs = (AttributeNS)attrNSList.get(i);
                xsw.writeNamespace(attrNs.getPrefix(),attrNs.getUri());
            }
            for(int i=0;i<attrList.size();i++){
                StAXAttr attr = (StAXAttr) attrList.get(i);
                if(parentNS.containsKey(attr.getPrefix())){
                    xsw.writeNamespace(attr.getPrefix(),parentNS.get(attr.getPrefix()));
                }
            }
            for(int i=0;i<attrList.size();i++){
                StAXAttr attr = (StAXAttr) attrList.get(i);
                xsw.writeAttribute(attr.getPrefix(),attr.getUri(),attr.getLocalName(),attr.getValue());
            }
        }

        void writeEndElement(XMLStreamWriter xsw) throws XMLStreamException{
            xsw.writeEndElement();
        }

        EncryptedData getEncryptedData() throws XMLStreamException, XWSSecurityException{
            if(!parsed){
                parse();
            }
            return ed;
        }

        void parse()throws XMLStreamException, XWSSecurityException{
            parsed = true;
            boolean stop = false;
            boolean parentElem = true;
            while(encContentReader.hasNext()){
                int eventType = XMLStreamConstants.START_ELEMENT;
                if(!parentElem){
                    eventType = encContentReader.next();
                }
                if(stop){
                    return;
                }
                switch(eventType){
                    case XMLStreamConstants.START_ELEMENT :{
                        if(parentElem){
                            localName = encContentReader.getLocalName();
                            uri = encContentReader.getNamespaceURI();
                            prefix = encContentReader.getPrefix();
                            if(prefix == null)
                                prefix = "";
                            int count = encContentReader.getAttributeCount();
                            for(int i=0;i<count ;i++){
                                String localName = encContentReader.getAttributeLocalName(i);
                                String uri = encContentReader.getAttributeNamespace(i);
                                String prefix = encContentReader.getAttributePrefix(i);
                                if(prefix == null)
                                    prefix = "";
                                final String value = encContentReader.getAttributeValue(i);
                                StAXAttr attr = new StAXAttr();
                                attr.setLocalName(localName);
                                attr.setValue(value);
                                attr.setPrefix(prefix);
                                attr.setUri(uri);
                                attrList.add(attr);
                            }

                            count = 0;
                            count = encContentReader.getNamespaceCount();
                            for(int i=0;i<count ;i++){
                                String prefix = encContentReader.getNamespacePrefix(i);
                                if(prefix == null)
                                    prefix = "";
                                String uri = encContentReader.getNamespaceURI(i);
                                AttributeNS attrNS = new AttributeNS();
                                attrNS.setPrefix(prefix);
                                attrNS.setUri(uri);
                                attrNSList.add(attrNS);
                            }
                            parentElem = false; // done reading parentElem
                        } else{
                            if(encContentReader.getLocalName() == MessageConstants.ENCRYPTED_DATA_LNAME &&
                                    encContentReader.getNamespaceURI() == MessageConstants.XENC_NS){
                                ed = new EncryptedData(encContentReader,context, parentNS);
                            }
                        }
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT :{
                        stop = true;
                        break;
                    }
                }

            }
        }
    }
