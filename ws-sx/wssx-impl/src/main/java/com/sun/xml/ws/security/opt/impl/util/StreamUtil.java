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

import com.sun.xml.wss.impl.MessageConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.XMLStreamReaderEx;
import org.jvnet.staxex.XMLStreamWriterEx;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class StreamUtil {
    
    /** Creates a new instance of StreamUtil */
    public StreamUtil() {
    }
    
    public static boolean moveToNextElement(XMLStreamReader reader) throws XMLStreamException{
        if(reader.hasNext()){
            reader.next();
            while(reader.getEventType() != XMLStreamReader.START_ELEMENT){
                if(reader.hasNext()){
                    reader.next();
                }else{
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }
    
    public static boolean moveToNextStartOREndElement(XMLStreamReader reader) throws XMLStreamException{
        if(reader.hasNext()){
            reader.next();
            while(move(reader)){
                if(reader.hasNext()){
                    reader.next();
                }else{
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }
    
    public static boolean moveToNextStartOREndElement(XMLStreamReader reader,XMLStreamWriter writer ) throws XMLStreamException{
        if(writer == null){
            return moveToNextStartOREndElement(reader);
        }
        if(reader.hasNext()){
            reader.next();
            writeCurrentEvent(reader,writer);
            while(move(reader)){
                if(reader.hasNext()){
                    reader.next();
                    writeCurrentEvent(reader,writer);
                }else{
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }
    
    
    public static boolean isStartElement(XMLStreamReader reader){
        if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
            return true;
        }
        return false;
    }
    
    
    public static boolean _break(XMLStreamReader reader,String localName,String uri) throws XMLStreamException{
        if(reader.getEventType() == XMLStreamReader.END_ELEMENT){
            if(reader.getLocalName() == localName && 
                    (reader.getNamespaceURI() == uri || reader.getNamespaceURI() == MessageConstants.WSSC_13NS)){
                return true;
            }
        }
        return false;
    }
    
    
    private static boolean move(XMLStreamReader reader) {
        if(reader.getEventType() == XMLStreamReader.START_ELEMENT ||
                reader.getEventType() == XMLStreamReader.END_ELEMENT){
            return false;
        }
        return true;
    }
    
    
    public static void writeStartElement(XMLStreamReader reader,XMLStreamWriter writer) throws XMLStreamException{        
        String pref = reader.getPrefix();
        if (pref == null) {
            pref = "";
        }
        writer.writeStartElement(pref, reader.getLocalName(), reader.getNamespaceURI());
        
        int nsCount = reader.getNamespaceCount();
        
        for(int i=0;i< nsCount ;i++){
            String prefix = reader.getNamespacePrefix(i);
            if(prefix == null)prefix ="";
            writer.writeNamespace(prefix,reader.getNamespaceURI(i));
        }
        int atCount = reader.getAttributeCount();
        for(int i=0;i< atCount ;i++){
            if(reader.getAttributePrefix(i) == "" || reader.getAttributePrefix(i) == null){
                writer.writeAttribute(reader.getAttributeLocalName(i),reader.getAttributeValue(i));
            }else{
                writer.writeAttribute(reader.getAttributePrefix(i),reader.getAttributeNamespace(i),reader.getAttributeLocalName(i),reader.getAttributeValue(i));
            }            
        }
        
    }
    
    public static void writeCurrentEvent(XMLStreamReader reader , XMLStreamWriter writer) throws XMLStreamException{
        int event = reader.getEventType();
        switch(event){
            
            case XMLStreamReader.CDATA:{
                writer.writeCData(reader.getText());
                break;
            }
            case XMLStreamReader.CHARACTERS:{
                //writer.writeCharacters(reader.getTextCharacters(),reader.getTextStart(),reader.getTextLength());
                char[] buf = new char[2048];
                int actual= 0;
                int sourceStart = 0;
                do {
                    actual = reader.getTextCharacters(sourceStart, buf, 0, 2048);
                    if (actual > 0) {
                        writer.writeCharacters(buf, 0, actual);
                        sourceStart += actual;
                    }
                }while (actual == 2048) ;


                break;
            }
            case XMLStreamReader.COMMENT:{
                writer.writeComment(reader.getText());
                break;
            }
            case XMLStreamReader.DTD:{
                break;
            }
            case XMLStreamReader.END_DOCUMENT:{
                break;
            }
            case XMLStreamReader.END_ELEMENT:{
                writer.writeEndElement();
                break;
            }
            case XMLStreamReader.ENTITY_DECLARATION:{
                break;
            }
            case XMLStreamReader.ENTITY_REFERENCE:{
                break;
            }
            case XMLStreamReader.NAMESPACE:{
                break;
            }
            case XMLStreamReader.NOTATION_DECLARATION:{
                break;
            }
            case XMLStreamReader.PROCESSING_INSTRUCTION:{
                break;
            }
            case XMLStreamReader.SPACE:{
                writer.writeCharacters(reader.getText());
                break;
            }
            case XMLStreamReader.START_DOCUMENT:{
                
                break;
            }
            case XMLStreamReader.START_ELEMENT:{
                writeStartElement(reader,writer);
                break;
            }
        }
    }
    
    
    public static void writeCurrentEvent(XMLStreamReaderEx reader , XMLStreamWriterEx writer) throws XMLStreamException{
        int event = reader.getEventType();
        switch(event){
            
            case XMLStreamReader.CDATA:{
                writer.writeCData(reader.getText());
                break;
            }
            case XMLStreamReader.CHARACTERS:{
                writer.writeCharacters(reader.getTextCharacters(),reader.getTextStart(),reader.getTextLength());
                break;
            }
            case XMLStreamReader.COMMENT:{
                writer.writeComment(reader.getText());
                break;
            }
            case XMLStreamReader.DTD:{
                break;
            }
            case XMLStreamReader.END_DOCUMENT:{
                break;
            }
            case XMLStreamReader.END_ELEMENT:{
                writer.writeEndElement();
                break;
            }
            case XMLStreamReader.ENTITY_DECLARATION:{
                break;
            }
            case XMLStreamReader.ENTITY_REFERENCE:{
                break;
            }
            case XMLStreamReader.NAMESPACE:{
                break;
            }
            case XMLStreamReader.NOTATION_DECLARATION:{
                break;
            }
            case XMLStreamReader.PROCESSING_INSTRUCTION:{
                break;
            }
            case XMLStreamReader.SPACE:{
                writer.writeCharacters(reader.getText());
                break;
            }
            case XMLStreamReader.START_DOCUMENT:{
                
                break;
            }
            case XMLStreamReader.START_ELEMENT:{
                writeStartElement(reader,writer);
                break;
            }
        }
    }
    
    public static String getWsuId(XMLStreamReader reader){
        return reader.getAttributeValue(MessageConstants.WSU_NS,"Id");
    }
    
    public static String getId(XMLStreamReader reader){
        return reader.getAttributeValue(null,"Id");
    }
    
    public static String getCV(XMLStreamReader reader) throws  XMLStreamException{
        StringBuffer content = new StringBuffer();
        int eventType = reader.getEventType();
        while(eventType != XMLStreamReader.END_ELEMENT ) {
            if(eventType == XMLStreamReader.CHARACTERS
                    || eventType == XMLStreamReader.CDATA
                    || eventType == XMLStreamReader.SPACE
                    || eventType == XMLStreamReader.ENTITY_REFERENCE) {
                content.append(reader.getText());
            } else if(eventType == XMLStreamReader.PROCESSING_INSTRUCTION
                    || eventType == XMLStreamReader.COMMENT) {
                // skipping
            }
            eventType = reader.next();
        }
        return content.toString();
    }
    
    public static String getCV(XMLStreamReaderEx reader) throws  XMLStreamException{
        StringBuffer sb = new StringBuffer();
        while(reader.getEventType() == reader.CHARACTERS && reader.getEventType() != reader.END_ELEMENT){
            CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
            for(int i=0;i<charSeq.length();i++){
                sb.append(charSeq.charAt(i));
            }
            reader.next();
        }
        return sb.toString();
    }
    
    public static String convertDigestAlgorithm(String algo){        
        if(MessageConstants.SHA1_DIGEST.equals(algo)){
            return MessageConstants.SHA_1;
        }
        if(MessageConstants.SHA256.equals(algo)){
            return MessageConstants.SHA_256;
        }
        
        if(MessageConstants.SHA512.equals(algo)){
            return MessageConstants.SHA_512;
        }
        
        return MessageConstants.SHA_1;
    }
    
}
