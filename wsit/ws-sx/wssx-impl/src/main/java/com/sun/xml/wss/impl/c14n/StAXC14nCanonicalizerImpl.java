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

/*
 * StAXC14nCanonicalizerImpl.java
 *
 * Created on August 22, 2005, 4:07 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.c14n;

import com.sun.xml.wss.impl.misc.UnsyncByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.helpers.NamespaceSupport;

/**
 *
 * @author K.Venugopal@sun.com
 */

/*
 * Test,
 * Improve performance
 * handle PI
 */
public class StAXC14nCanonicalizerImpl extends BaseCanonicalizer implements XMLStreamWriter{
    
    boolean closeStartTag = false;
    NamespaceSupport nsContext = new NamespaceSupport();
    private javax.xml.namespace.NamespaceContext namespaceContext = null;
    ElementName [] elementNames = new ElementName[10];
    
    protected UnsyncByteArrayOutputStream elemBuffer = null;
    
    /** Creates a new instance of StAXC14nCanonicalizerImpl */
    @SuppressWarnings("unchecked")
    public StAXC14nCanonicalizerImpl() {
        //_attrResult = new TreeSet(new StAXAttrSorter(false));
        _attrResult = new ArrayList();
        for(int i=0;i<4;i++){
            _attrs.add(new StAXAttr());
        }
        for(int i=0;i<10;i++){
            elementNames[i] = new ElementName();
        }
        elemBuffer = new UnsyncByteArrayOutputStream();
    }
    /**
     * This method has not effect when called.
     * 
     * @throws javax.xml._stream.XMLStreamException {@inheritDoc}
     */
    public void close() throws XMLStreamException {
        //no-op
    }
    
    
    public void flush() throws XMLStreamException {
        //no-op
    }
    
    
    
    public javax.xml.namespace.NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }
    
    public NamespaceSupport getNSContext(){
        return nsContext;
    }
    
    public String getPrefix(String namespaceURI) throws XMLStreamException {
        return nsContext.getPrefix(namespaceURI);
    }
    
    
    public Object getProperty(String str) throws IllegalArgumentException {
        //throw new UnsupportedOperationException();
        return null;
    }
    
    
    public void setDefaultNamespace(String str) throws XMLStreamException {
        nsContext.declarePrefix("",str);
    }
    
    
    public void setNamespaceContext(javax.xml.namespace.NamespaceContext namespaceContext) throws XMLStreamException {
        this.namespaceContext = namespaceContext;
    }
    
    
    public void setPrefix(String str, String str1) throws XMLStreamException {
        nsContext.declarePrefix(str,str1);
    }
    
    /**
     * Creates a DOM Atrribute @see org.w3c.dom.Node and associates it with the current DOM element @see org.w3c.dom.Node.
     * 
     * @param localName {@inheritDoc}
     * @param value {@inheritDoc}
     * @throws javax.xml._stream.XMLStreamException {@inheritDoc}
     */
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writeAttribute("","",localName,value);
    }
    
    
    public void writeAttribute(String namespaceURI,String localName,String value)throws XMLStreamException {
        String prefix = "";
        prefix = nsContext.getPrefix(namespaceURI);
        writeAttribute(prefix,"",localName,value);
    }
    
    @SuppressWarnings("unchecked")
    public void writeAttribute(String prefix,String namespaceURI,String localName,String value)throws XMLStreamException {
        StAXAttr attr = getAttribute();
        attr.setLocalName(localName);
        attr.setValue(value);
        attr.setPrefix(prefix);
        attr.setUri(namespaceURI);
        _attrResult.add(attr);
    }
    
    
    public void writeCData(String data) throws XMLStreamException {
        try {
            closeStartTag();
            outputTextToWriter(data,_stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    public void writeCharacters(String charData) throws XMLStreamException {
        try {
            closeStartTag();
            outputTextToWriter(charData,_stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    public void writeCharacters(char[] values, int param, int param2) throws XMLStreamException {
        try {
            closeStartTag();
            outputTextToWriter(values,param,param2,_stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    public void writeComment(String str) throws XMLStreamException {
        
    }
    
    
    public void writeDTD(String str) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        _defURI = namespaceURI;
        writeNamespace("",namespaceURI);
        
    }
    
    
    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeEmptyElement("",localName,"");
    }
    
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        
        String prefix = nsContext.getPrefix(namespaceURI);
        writeEmptyElement(prefix,localName,namespaceURI);
    }
    
    
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        closeStartTag();
        
        
        try {
            _stream .write('<');
            if((prefix != null && prefix.length() == 0) || prefix == null){
                writeStringToUtf8(localName,_stream);
            }else{
                writeStringToUtf8(prefix,_stream);
                writeStringToUtf8(":",_stream);
                writeStringToUtf8(localName,_stream);
                
            }
            
            _stream .write('>');
            _stream .write(_END_TAG);
            if((prefix != null && prefix.length() == 0) || prefix == null){
                writeStringToUtf8(localName,_stream);
            }else{
                writeStringToUtf8(prefix,_stream);
                writeStringToUtf8(":",_stream);
                writeStringToUtf8(localName,_stream);
                
            }
            _stream .write('>');
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
        //ElementName qname =  elementNames[--_depth];
        closeStartTag();
        if(_depth == 0){
            return;
        }
        if(_ncContextState[_depth]){
            nsContext.popContext();
        }
        try {
            _stream .write(_END_TAG);
            //writeStringToUtf8 (qname,_stream);
            ElementName en =elementNames[--_depth]; 
            _stream.write(en.getUtf8Data().getBytes(), 0,en.getUtf8Data().getLength());
            en.getUtf8Data().reset();
            _stream .write('>');
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    public void writeEntityRef(String str) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    @SuppressWarnings("unchecked")
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        String duri = nsContext.getURI(prefix);
        boolean add= false;
        if(duri == null || !duri.equals(namespaceURI)){
            add= true;
        }
        if(add && !_ncContextState[_depth-1]){
            nsContext.pushContext();
            _ncContextState[_depth-1]=true;
        }
        if(add){
            nsContext.declarePrefix(prefix,namespaceURI);
            AttributeNS attrNS = getAttributeNS();
            attrNS.setPrefix(prefix);
            attrNS.setUri(namespaceURI);
            _nsResult.add(attrNS);
        }
    }
    
    
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        try {
            outputPItoWriter(target,"",_stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        try {
            outputPItoWriter(target,data,_stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void writeStartDocument() throws XMLStreamException {
    }
    
    
    public void writeStartDocument(String version) throws XMLStreamException {
    }
    
    
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
    }
    
    
    public void writeStartElement(String localName) throws XMLStreamException {
        writeStartElement("",localName,"");
    }
    
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        String prefix = nsContext.getPrefix(namespaceURI);
        writeStartElement(prefix,localName,namespaceURI);
    }
    
    
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        
        closeStartTag();
        elemBuffer.reset();
        UnsyncByteArrayOutputStream buffWriter = null;
        try{
            if(prefix !=null && prefix.length() > 0){
                buffWriter = elementNames[_depth].getUtf8Data();
                writeStringToUtf8(prefix,buffWriter);
                writeStringToUtf8(":",buffWriter);
                writeStringToUtf8(localName,buffWriter);
                _elementPrefix = prefix;
            }else{
                buffWriter = elementNames[_depth].getUtf8Data();
                writeStringToUtf8(localName,buffWriter);
                
            }
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
        
        //byte [] data = elemBuffer.toByteArray();
        //byte [] data = elemBuffer.getBytes();
        _ncContextState[_depth]=false;
        
        
        _depth++;resize();
        resizeElementStack();
        try {
            _stream .write('<');
            
            _stream.write(buffWriter.getBytes(),0,buffWriter.getLength());
            closeStartTag = true;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected void closeStartTag() throws XMLStreamException{
        try{
            if(closeStartTag){
                //Iterator itr =  _nsResult.iterator();
                if(_defURI != null){
                    outputAttrToWriter("xmlns",_defURI,_stream);
                }
                
                //writeAttributesNS(itr);
                
                if ( _nsResult.size() > 0 ) {
                    writeAttributesNS(_nsResult.iterator());
                }
                
                if ( _attrResult.size() > 0) {
                    writeAttributes(_attrResult);
                }
                
                _nsResult.clear();
                _attrResult.clear();
                _stream .write('>');
                closeStartTag = false;
                _defURI = null;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    protected StAXAttr getAttribute(){
        if(_attrPos < _attrs.size() ){
            return  (StAXAttr)_attrs.get(_attrPos++);
        }else{
            for(int i=0;i<initalCacheSize;i++){
                _attrs.add(new StAXAttr());
            }
            return (StAXAttr)_attrs.get(_attrPos++);
        }
    }
    
    protected void resizeElementStack  (){
        if(_depth >= elementNames.length ){
            ElementName [] tmp = new ElementName[elementNames.length +10];
            System.arraycopy(elementNames,0,tmp,0,elementNames.length);
            for(int i=elementNames.length;i<elementNames.length+10;i++){
                tmp[i] = new ElementName();
            }
            elementNames = tmp;
        }
    }
    
    protected void writeAttributes(List itr) throws IOException {
        ///while(itr.hasNext()){
        int size = itr.size();
        for ( int i=0; i<size; i++) {
            StAXAttr attr = (StAXAttr) itr.get(i);
            String prefix = attr.getPrefix();
            if(prefix != null && prefix.length() != 0){
                ///    attrName.setLength(0);
                ///    attrName.append(prefix);
                ///    attrName.append(":");
                ///    attrName.append(attr.getLocalName());
                ///    prefix = attrName.toString();
                outputAttrToWriter(prefix, attr.getLocalName(), attr.getValue(),_stream);
            }else{
                prefix = attr.getLocalName();
                outputAttrToWriter(prefix,attr.getValue(),_stream);
            }
            
            //outputAttrToWriter(prefix,attr.getValue(),_stream);
        }
    }
    
    
}
