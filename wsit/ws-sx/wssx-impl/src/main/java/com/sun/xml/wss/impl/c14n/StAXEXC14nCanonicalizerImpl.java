/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
 * StAXEC14nCanonicalizerImpl.java
 *
 * Created on August 22, 2005, 7:14 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.c14n;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.UnsyncByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class StAXEXC14nCanonicalizerImpl extends StAXC14nCanonicalizerImpl  {
    
    private List inclusivePrefixList = null;
    private HashSet visiblyUtilized = new HashSet();    
    
    private UnsyncByteArrayOutputStream tmpBuffer = null;
       
    NamespaceContextImpl exC14NContext = new NamespaceContextImpl();
    private boolean forceDefNS  =false;
    /** Creates a new instance of StAXEC14nCanonicalizerImpl */
    public StAXEXC14nCanonicalizerImpl() {
        super();
        tmpBuffer = new UnsyncByteArrayOutputStream();
        
    }        
    
    public boolean isParentToParentAdvice(){
        if(_depth > 2){
            ElementName qname = elementNames[_depth - 2];
            if(qname.getUtf8Data().getLength() == 11 || qname.getUtf8Data().getLength() == 12){
                String str = new String(qname.getUtf8Data().getBytes(), qname.getUtf8Data().getLength() - 6, 6);
                if(str.equals("Advice")){
                    return true;
                }
            }else{
                return false;
            }
        }
        return false;        
    }
    
    public void reset(){
        super.reset();
        exC14NContext.reset();
        
    }
    public void setInclusivePrefixList(List values){
        this.inclusivePrefixList = values;
    }
    
    public List getInclusivePrefixList(){
        return inclusivePrefixList;
    }

    public void forceDefaultNS(boolean isForce){
        this.forceDefNS = isForce;
    }
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        exC14NContext.declareNamespace(prefix,namespaceURI);
    }
    
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        String pf = prefix;
        if(prefix == null){
            pf = "";
        }
        super.writeStartElement(pf,localName,namespaceURI);
        _elementPrefix = pf;
        exC14NContext.push();
        
    }
    @SuppressWarnings("unchecked")
    protected void closeStartTag() throws XMLStreamException{        
        try{
            if(closeStartTag){
                if(_attrResult.size() >0){
                    collectVisiblePrefixes(_attrResult.iterator());
                }
                if(_elementPrefix != null)
                    visiblyUtilized.add(_elementPrefix);
                AttributeNS nsDecl = null;
                /*
                if(_elementPrefix != null && _elementPrefix.length() >=0){
                    AttributeNS eDecl = exC14NContext.getNamespaceDeclaration(_elementPrefix);
                    
                    if(eDecl !=null && !eDecl.isWritten()){
                        eDecl.setWritten(true);
                        _nsResult.add(eDecl);
                    }
                    
                }*/
                if(visiblyUtilized.size() > 0){
                    Iterator prefixItr = visiblyUtilized.iterator();
                    populateNamespaceDecl(prefixItr);
                }
                if(inclusivePrefixList != null){
                    populateNamespaceDecl(inclusivePrefixList.iterator());
                }

                 if (forceDefNS) {
                     forceDefNS = false;
                     if (exC14NContext.getNamespaceDeclaration("") == null
                             && "".equals(exC14NContext.getNamespaceURI(""))) {
                        AttributeNS ns = new AttributeNS();
                        ns.setPrefix("");
                        ns.setUri("");
                        if (!_nsResult.contains(ns)) {
                            _nsResult.add(ns);
                        }
                     }
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
    
    @SuppressWarnings("unchecked")
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
        if(_depth ==0 ){
            return;
        }
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
    @SuppressWarnings("unchecked")
    protected void collectVisiblePrefixes(Iterator itr) throws IOException {
        while(itr.hasNext()){
            StAXAttr attr = (StAXAttr) itr.next();
            String prefix = attr.getPrefix();
            if(prefix.length() >0){
                visiblyUtilized.add(prefix);
            }
        }
    }
    @SuppressWarnings("unchecked")
    private void populateNamespaceDecl(Iterator prefixItr){

        AttributeNS nsDecl = null;
        while(prefixItr.hasNext() ){
            String prefix = (String)prefixItr.next();
            if(prefix.equals(MessageConstants.XML_PREFIX)){
                continue;
            }
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
    
    public NamespaceContext getNamespaceContext() {
        return exC14NContext;
    }
}
