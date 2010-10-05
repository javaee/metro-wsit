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

package com.sun.xml.ws.security.opt.impl.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class XMLStreamFilter implements XMLStreamWriter{
    
    protected XMLStreamWriter writer = null;
    protected NamespaceContextEx nsContext = null;
    protected boolean seenFirstElement = false;
    protected int count = 0;
    /** Creates a new instance of XMLStreamFilter */
    public XMLStreamFilter(XMLStreamWriter writer,NamespaceContextEx nce) throws XMLStreamException {
        this.writer = writer;
        nsContext = nce;
        if(nsContext == null){
            throw new XMLStreamException("NamespaceContext cannot be null");
        }
    }
    
    
    public NamespaceContext getNamespaceContext() {
        if(count == 0)
            return nsContext;
        else
            return writer.getNamespaceContext();
    }
    
    public void close() throws XMLStreamException {
        writer.close();
    }
    
    public void flush() throws XMLStreamException {
        writer.flush();
    }
    
    public void writeEndDocument() throws XMLStreamException {
        writer.writeEndDocument();
    }
    
    public void writeEndElement() throws XMLStreamException {
        if(count ==0){
            return;
        }
        --count;
        writer.writeEndElement();
    }
    
    public void writeStartDocument() throws XMLStreamException {
        writer.writeStartDocument();
    }
    
    public void writeCharacters(char[] c, int index, int len) throws XMLStreamException {
        writer.writeCharacters(c,index,len);
    }
    
    public void setDefaultNamespace(String string) throws XMLStreamException {
        if(count == 0){
            nsContext.add("",string);
            return;
        }
        writer.writeCharacters(string);
    }
    
    public void writeCData(String string) throws XMLStreamException {
        writer.writeCData(string);
    }
    
    public void writeCharacters(String string) throws XMLStreamException {
        writer.writeCharacters(string);
    }
    
    public void writeComment(String string) throws XMLStreamException {
        writer.writeComment(string);
    }
    
    public void writeDTD(String string) throws XMLStreamException {
        writer.writeDTD(string);
    }
    
    public void writeDefaultNamespace(String string) throws XMLStreamException {
        writer.writeDefaultNamespace(string);
    }
    
    public void writeEmptyElement(String string) throws XMLStreamException {
        if(count == 0){
            writer.setNamespaceContext(nsContext);
        }
        writer.writeEmptyElement(string);
        
    }
    
    public void writeEntityRef(String string) throws XMLStreamException {
        writer.writeEntityRef(string);
    }
    
    public void writeProcessingInstruction(String string) throws XMLStreamException {
        writer.writeProcessingInstruction(string);
    }
    
    public void writeStartDocument(String string) throws XMLStreamException {
        writer.writeStartDocument(string);
    }
    
    public void writeStartElement(String string) throws XMLStreamException {
        if(!seenFirstElement){
            seenFirstElement = true;
            return;
        }
        count++;
        if(count == 1){
            writer.setNamespaceContext(nsContext);
        }
        writer.writeStartElement(string);
    }
    
    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        writer.setNamespaceContext(namespaceContext);
    }
    
    public Object getProperty(String value) throws IllegalArgumentException {
        if("com.ctc.wstx.outputUnderlyingStream".equals(value) || 
                "http://java.sun.com/xml/stream/properties/outputstream".equals(value)){
            return null;
        }
        return writer.getProperty(value);
    }
    
    
    public String getPrefix(String string) throws XMLStreamException {
        return writer.getPrefix(string);
    }
    
    public void setPrefix(String string, String string0) throws XMLStreamException {
        writer.setPrefix(string,string0);
    }
    
    public void writeAttribute(String string, String string0) throws XMLStreamException {
        if(count == 0){
            return;
        }
        writer.writeAttribute(string,string0);
    }
    
    public void writeEmptyElement(String string, String string0) throws XMLStreamException {
        if(count == 0){
            writer.setNamespaceContext(nsContext);
        }
        writer.writeEmptyElement(string,string0);
    }
    
    public void writeNamespace(String string, String string0) throws XMLStreamException {
        if(count == 0){
            nsContext.add(string,string0);
            return;
        }
        writer.writeNamespace(string,string0);
    }
    
    public void writeProcessingInstruction(String string, String string0) throws XMLStreamException {
        writer.writeProcessingInstruction(string,string0);
    }
    
    public void writeStartDocument(String string, String string0) throws XMLStreamException {
        writer.writeStartDocument(string,string0);
    }
    
    public void writeStartElement(String string, String string0) throws XMLStreamException {
        if(!seenFirstElement){
            seenFirstElement = true;
            return;
        }
        count++;
        if(count == 1){
            writer.setNamespaceContext(nsContext);
        }
        writer.writeStartElement(string,string0);
    }
    
    public void writeAttribute(String string, String string0, String string1) throws XMLStreamException {
        if(count == 0){
            return;
        }
        writer.writeAttribute(string,string0,string1);
    }
    
    public void writeEmptyElement(String string, String string0, String string1) throws XMLStreamException {
        if(count == 0){
            writer.setNamespaceContext(nsContext);
        }
        writer.writeEmptyElement(string,string0,string1);
    }
    
    public void writeStartElement(String string, String string0, String string1) throws XMLStreamException {
        if(!seenFirstElement){
            seenFirstElement = true;
            return;
        }
        count++;
        if(count == 1){
            writer.setNamespaceContext(nsContext);
        }
        writer.writeStartElement(string,string0,string1);
    }
    
    public void writeAttribute(String string, String string0, String string1, String string2) throws XMLStreamException {
        if(count == 0){
            return;
        }
        writer.writeAttribute(string,string0,string1,string2);
    }
    
    
}
