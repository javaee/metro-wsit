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
 * StAXEC14nCanonicalizerImpl.java
 *
 * Created on August 22, 2005, 7:14 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.c14n;

import com.sun.xml.wss.impl.misc.UnsyncByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static javax.xml.stream.XMLStreamConstants.*;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class StAXEXC14nCanonicalizerImpl extends StAXC14nCanonicalizerImpl  {
    
    private List inclusivePrefixList = null;
    private HashSet visiblyUtilized = new HashSet();
    
    private UnsyncByteArrayOutputStream tmpBuffer = null;
    
    
    NamespaceContextImpl exC14NContext = new NamespaceContextImpl();
    /** Creates a new instance of StAXEC14nCanonicalizerImpl */
    public StAXEXC14nCanonicalizerImpl() {
        super();
        tmpBuffer = new UnsyncByteArrayOutputStream();
        
    }
    
    
    public void reset(){
        super.reset();
        exC14NContext.reset();
        
    }
    public void setInclusivePrefixList(List values){
        this.inclusivePrefixList = values;
    }
    
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        if(prefix.length() == 0){
            _defURI = namespaceURI;
        }
        exC14NContext.declareNamespace(prefix,namespaceURI);
        
    }
    
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        
        super.writeStartElement(prefix,localName,namespaceURI);
        /*if(prefix.length() >0){
            _elementPrefix = prefix;
        }*/
        _elementPrefix = prefix;
        exC14NContext.push();
        
    }
    
    protected void closeStartTag() throws XMLStreamException{
        try{
            if(closeStartTag){
//                if(_defURI != null){
//                    outputAttrToWriter("xmlns",_defURI,_stream);
//                }
                if(_attrResult.size() >0){
                    collectVisiblePrefixes(_attrResult.iterator());
                }
                if(_elementPrefix != null)
                    visiblyUtilized.add(_elementPrefix);
                AttributeNS nsDecl = null;
                if(_elementPrefix.length() >0){
                    AttributeNS eDecl = exC14NContext.getNamespaceDeclaration(_elementPrefix);
                    
                    if(eDecl !=null && !eDecl.isWritten()){
                        eDecl.setWritten(true);
                        _nsResult.add(eDecl);
                    }
                    
                }
                /*if(_elementPrefix.length() == 0){
                    visiblyUtilized.remove("");
                }*/
                if(visiblyUtilized.size() > 0){
                    Iterator prefixItr = visiblyUtilized.iterator();
                    populateNamespaceDecl(prefixItr);
                }
                if(inclusivePrefixList != null){
                    populateNamespaceDecl(inclusivePrefixList.iterator());
                }
                
                if ( _nsResult.size() > 0) {
                    BaseCanonicalizer.sort(_nsResult);
                    writeAttributesNS(_nsResult);
                }
                
                if  ( _attrResult.size() > 0 ) {
                    BaseCanonicalizer.sort(_attrResult);
                    writeAttributes(_attrResult);
                }
                visiblyUtilized.clear();
                _nsResult.clear();
                _attrResult.clear();
                _stream .write('>');
                closeStartTag = false;
                _elementPrefix = null;
                _defURI = null;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
       /* String prefix = nsContext.getPrefix (namespaceURI);
        writeEmptyElement (prefix,localName,namespaceURI);*/
        //TODO
        throw new UnsupportedOperationException();
    }
    
    
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        
        closeStartTag();
        exC14NContext.push();
        
        
        
        try {
            _stream .write('<');
            elemBuffer.reset();
            if(prefix.length() == 0){
                writeStringToUtf8(localName,elemBuffer);
            }else{
                writeStringToUtf8(prefix,elemBuffer);
                writeStringToUtf8(":",elemBuffer);
                writeStringToUtf8(localName,elemBuffer);
                
            }
            byte [] endElem = elemBuffer.getBytes();
            int len = elemBuffer.getLength();
            visiblyUtilized.add(prefix);
            AttributeNS nsDecl = null;
            _stream.write(endElem, 0, len);
            
            if ( visiblyUtilized.size() > 0 ) {
                Iterator prefixItr = visiblyUtilized.iterator();
                populateNamespaceDecl(prefixItr);
            }
            if(inclusivePrefixList != null){
                populateNamespaceDecl(inclusivePrefixList.iterator());
            }
            
            if ( _nsResult.size() > 0 ) {
                BaseCanonicalizer.sort(_nsResult);
                writeAttributesNS(_nsResult);
            }
            
            if ( _attrResult.size() > 0 ) {
                BaseCanonicalizer.sort(_attrResult);
                writeAttributes(_attrResult);
            }
            
            
            visiblyUtilized.clear();
            _nsResult.clear();
            _attrResult.clear();
            // _stream .write('>');
            closeStartTag = false;
            _elementPrefix = "";
            _defURI = null;
            
            _stream .write('>');
            _stream .write(_END_TAG);
            //writeStringToUtf8(name,_stream);
            _stream.write(endElem, 0, len);
            _stream .write('>');
            exC14NContext.pop();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void writeEndDocument() throws XMLStreamException {
        while(_depth > 0){
            writeEndElement();
        }
    }
    
    
    public void writeEndElement() throws XMLStreamException {
        closeStartTag();
        ElementName qname =  elementNames[--_depth];
        
        
        exC14NContext.pop();
        
        try {
            _stream .write(_END_TAG);
            //writeStringToUtf8(qname,_stream);
            _stream.write(qname.getUtf8Data().getBytes(), 0, qname.getUtf8Data().getLength());
            qname.getUtf8Data().reset();
            _stream .write('>');
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected void collectVisiblePrefixes(Iterator itr) throws IOException {
        while(itr.hasNext()){
            StAXAttr attr = (StAXAttr) itr.next();
            String prefix = attr.getPrefix();
            if(prefix.length() >0){
                visiblyUtilized.add(prefix);
            }
        }
    }
    
    private void populateNamespaceDecl(Iterator prefixItr){
        AttributeNS nsDecl = null;
        while(prefixItr.hasNext() ){
            String prefix = (String)prefixItr.next();
            nsDecl = exC14NContext.getNamespaceDeclaration(prefix);
            
            if(nsDecl !=null && !nsDecl.isWritten()){
                nsDecl.setWritten(true);
                _nsResult.add(nsDecl);
            }
        }
    }
    
    protected void writeAttributesNS(List itr) throws IOException {
        
        AttributeNS attr = null;
        int size = itr.size();
        for ( int i=0; i<size; i++) {
            attr = (AttributeNS) itr.get(i);
            tmpBuffer.reset();
            _stream.write(attr.getUTF8Data(tmpBuffer));
        }
        
    }
}
