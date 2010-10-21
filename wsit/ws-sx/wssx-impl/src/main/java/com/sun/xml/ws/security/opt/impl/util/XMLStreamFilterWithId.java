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

package com.sun.xml.ws.security.opt.impl.util;

import com.sun.xml.wss.impl.c14n.BaseCanonicalizer;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.wss.impl.MessageConstants;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class XMLStreamFilterWithId  extends XMLStreamFilter{
    
    String id = null;
    boolean wroteId = false;
    
    /** Creates a new instance of XMLStreamWriterWithId */
    public XMLStreamFilterWithId(XMLStreamWriter writer, NamespaceContextEx nce ,String id) throws XMLStreamException {
        super(writer, nce);
        this.id = id;
    }
    
    public void setDefaultNamespace(String string) throws XMLStreamException {
        writer.setDefaultNamespace(string);
    }
    
    public void writeEndElement() throws XMLStreamException {
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        writer.writeEndElement();
    }
    
    public void writeStartElement(String string) throws XMLStreamException {
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        if(!seenFirstElement){
            seenFirstElement = true;
        }
        writer.writeStartElement(string);
        /*if(count == 0){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
        }*/
        count++;
    }
    
    public void writeAttribute(String string, String string0) throws XMLStreamException {
        writer.writeAttribute(string,string0);
    }
    
    public void writeNamespace(String string, String string0) throws XMLStreamException {
        writer.writeNamespace(string,string0);
    }
    
    public void writeStartElement(String string, String string0) throws XMLStreamException {
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        if(!seenFirstElement){
            seenFirstElement = true;
        }
        writer.writeStartElement(string,string0);
        /*if(count == 0){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
        }*/
        count++;
        
    }
    
    public void writeAttribute(String string, String string0, String string1) throws XMLStreamException {
        writer.writeAttribute(string,string0,string1);
    }
    
    public void writeStartElement(String string, String string0, String string1) throws XMLStreamException {
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        if(!seenFirstElement){
            seenFirstElement = true;
        }
        
        writer.writeStartElement(string,string0,string1);
        /*if(count == 0){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
        }*/
        count++;
        
    }
    
    public void writeAttribute(String string, String string0, String string1, String string2) throws XMLStreamException {
        writer.writeAttribute(string,string0,string1,string2);
    }
    
    public void writeCharacters(char[] c, int index, int len) throws XMLStreamException {
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        writer.writeCharacters(c,index,len);
    }
    
    public void writeCharacters(String string) throws XMLStreamException {
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        writer.writeCharacters(string);
    }
    
    public void writeEmptyElement(String string) throws XMLStreamException {
        if(count == 0){
            writer.setNamespaceContext(nsContext);
        }
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        writer.writeEmptyElement(string);
        
    }
    
    public void writeEmptyElement(String string, String string0, String string1) throws XMLStreamException {
        if(count == 0){
            writer.setNamespaceContext(nsContext);
        }
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        writer.writeEmptyElement(string,string0,string1);
    }
    
    public void writeEmptyElement(String string, String string0) throws XMLStreamException {
        if(count == 0){
            writer.setNamespaceContext(nsContext);
        }
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        writer.writeEmptyElement(string,string0);
    }
    
    public void writeProcessingInstruction(String string, String string0) throws XMLStreamException {
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        writer.writeProcessingInstruction(string,string0);
    }
    
    public void writeProcessingInstruction(String string) throws XMLStreamException {
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        writer.writeProcessingInstruction(string);
    }
    
    public void writeCData(String string) throws XMLStreamException {
        if(!wroteId && count == 1){
            writer.writeAttribute(MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS,
                    "Id", id);
            if(writer instanceof BaseCanonicalizer){
                writer.setNamespaceContext(nsContext);
            }
            wroteId = true;
        }
        writer.writeCData(string);
    }
}
