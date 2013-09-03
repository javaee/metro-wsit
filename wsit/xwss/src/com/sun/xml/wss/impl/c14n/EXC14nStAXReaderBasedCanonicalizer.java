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

package com.sun.xml.wss.impl.c14n;
import com.sun.xml.wss.impl.dsig.NamespaceContextImpl;
import com.sun.xml.wss.impl.misc.UnsyncByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class EXC14nStAXReaderBasedCanonicalizer extends BaseCanonicalizer {
    private NamespaceContextImpl _exC14NContext = new NamespaceContextImpl();
    private List _inclusivePrefixList = null;
    private UnsyncByteArrayOutputStream _tmpBuffer = null;
    private HashSet _visiblyUtilized = new HashSet();
    private int _index =0;
    /** Creates a new instance of EXC14nStAXReaderBasedCanonicalizer */
    public EXC14nStAXReaderBasedCanonicalizer() {
        _attrResult = new ArrayList();
        for(int i=0;i<4;i++){
            _attrs.add(new StAXAttr());
        }
        _tmpBuffer = new UnsyncByteArrayOutputStream();
    }
    
    public void canonicalize(XMLStreamReader reader,OutputStream stream,List inclusiveList) throws XMLStreamException, IOException{
        
        if(reader.hasNext() && reader.getEventType() != START_ELEMENT){
            throw new XMLStreamException("Reader should point to START_ELEMENT EVENT");
        }
        
        updatedNamespaceContext(reader);
        this._stream = stream;
        this._inclusivePrefixList = inclusiveList;
		int eventType = reader.getEventType();
        do{
            switch(eventType){
                case START_ELEMENT :{
                    _exC14NContext.push();
                    _index++;
                    writeStartElement(reader);
                    break;
                }
                case END_ELEMENT :{
                    _exC14NContext.pop();
                    _index--;
                    writeEndElement(reader);
                    break;
                }
                case CDATA:{
                    outputTextToWriter(reader.getTextCharacters(),reader.getTextStart(),reader.getTextLength(),_stream);
                    break;
                }
                case CHARACTERS :{
                    if(!reader.isWhiteSpace()){
                        outputTextToWriter(reader.getText(),_stream);
					}
                    break;
                }
                case COMMENT :{
                    break;
                }
                case DTD :{
                    break;
                }
                case END_DOCUMENT :{
                    
                    break;
                }
                case ENTITY_DECLARATION :{
                    break;
                }
                case ENTITY_REFERENCE :{
                    break;
                }
                case  NOTATION_DECLARATION :{
                    break;
                }
                case PROCESSING_INSTRUCTION :{
                    break;
                }
                case SPACE :{
                    break;
                }
                
                case START_DOCUMENT :{
                    break;
                }
                default :{
                    break;
                }
                
            }
            eventType = reader.next();
        } while(reader.hasNext() && _index >0);
    }
    
    private void writeStartElement(XMLStreamReader reader) throws IOException {
        final String localName = reader.getLocalName();
        final String prefix = reader.getPrefix();
        final String uri = reader.getNamespaceURI();
        writeCharToUtf8('<',_stream);
        if(prefix == null && uri == null){
            writeStringToUtf8(localName,_stream);
        }else if(prefix != null && prefix.length() > 0){
            writeStringToUtf8(prefix,_stream);
            writeStringToUtf8(":",_stream);
            writeStringToUtf8(localName,_stream);
        } else if(prefix == null){
            writeStringToUtf8(localName,_stream);
        }
        updatedNamespaceContext(reader);
        updateAttributes(reader);
        
        if(prefix != null){
            _visiblyUtilized.add(prefix);
        }
        if(_elementPrefix.length() >0){
            AttributeNS eDecl = _exC14NContext.getNamespaceDeclaration(_elementPrefix);
            
            if(eDecl !=null && !eDecl.isWritten()){
                eDecl.setWritten(true);
                _nsResult.add(eDecl);
            }
            
        }
        
        if(_visiblyUtilized.size() > 0){
            Iterator prefixItr = _visiblyUtilized.iterator();
            populateNamespaceDecl(prefixItr);
        }
        if(_inclusivePrefixList != null){
            populateNamespaceDecl(_inclusivePrefixList.iterator());
        }
        
        if ( _nsResult.size() > 0) {
            BaseCanonicalizer.sort(_nsResult);
            writeAttributesNS(_nsResult);
        }
        if ( _attrResult.size() > 0 ) {
            BaseCanonicalizer.sort(_attrResult);
            writeAttributes(_attrResult);
        }
        writeCharToUtf8('>',_stream);
        _nsResult.clear();
        _attrResult.clear();
        _visiblyUtilized.clear();
    }
    
    private void populateNamespaceDecl(Iterator prefixItr){
        AttributeNS nsDecl = null;
        while(prefixItr.hasNext() ){
            String prefix = (String)prefixItr.next();
            nsDecl = _exC14NContext.getNamespaceDeclaration(prefix);
            
            if(nsDecl !=null && !nsDecl.isWritten()){
                nsDecl.setWritten(true);
                _nsResult.add(nsDecl);
            }
        }
    }
    
    private void updatedNamespaceContext(XMLStreamReader reader){
        if(reader.getEventType() != reader.START_ELEMENT){
            return;
        }
        int count = reader.getNamespaceCount();
        for(int i=0;i<count ;i++){
            final String prefix = reader.getNamespacePrefix(i);
            final String uri = reader.getNamespaceURI(i);
            _exC14NContext.declareNamespace(prefix,uri);
        }
    }
    
    private void updateAttributes(XMLStreamReader reader) throws IOException{
        int count = reader.getAttributeCount();
        for(int i=0;i<count ;i++){
            final String localName = reader.getAttributeLocalName(i);
            final String uri = reader.getAttributeNamespace(i);
            final String prefix = reader.getAttributePrefix(i);
            final String value = reader.getAttributeValue(i);
            StAXAttr attr = getAttribute();
            attr.setLocalName(localName);
            attr.setValue(value);
            attr.setPrefix(prefix);
            attr.setUri(uri);
            _attrResult.add(attr);
        }
    }
    
    private void writeEndElement(XMLStreamReader reader) throws IOException {
        final String localName = reader.getLocalName();
        final String prefix = reader.getPrefix();
        final String uri = reader.getNamespaceURI();
        writeStringToUtf8("</",_stream);
        if(prefix == null && uri == null){
            writeStringToUtf8(localName,_stream);
        }else if(prefix != null && prefix.length() > 0){
            writeStringToUtf8(prefix,_stream);
            writeStringToUtf8(":",_stream);
            writeStringToUtf8(localName,_stream);
        } else if(prefix == null){
            writeStringToUtf8(localName,_stream);  
        }
        writeCharToUtf8('>',_stream);
    }
    
    
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
    
    protected void writeAttributesNS(List itr) throws IOException {
        
        AttributeNS attr = null;
        int size = itr.size();
        for ( int i=0; i<size; i++) {
            attr = (AttributeNS) itr.get(i);
            _tmpBuffer.reset();
            _stream.write(attr.getUTF8Data(_tmpBuffer));
        }
        
    }
    
    protected void writeAttributes(List itr) throws IOException {
        
        int size = itr.size();
        for ( int i=0; i<size; i++) {
            StAXAttr attr = (StAXAttr) itr.get(i);
            String prefix = attr.getPrefix();
            if(prefix.length() != 0){
                outputAttrToWriter(prefix, attr.getLocalName(), attr.getValue(),_stream);
            }else{
                prefix = attr.getLocalName();
                outputAttrToWriter(prefix,attr.getValue(),_stream);
            }
        }
    }
}
