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

package com.sun.xml.ws.security.opt.impl.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class FilteredXMLStreamReader implements XMLStreamReader{
    
    private XMLStreamReader reader = null;
    private int startElemCounter = 0;
    
    /** Creates a new instance of FilteredXMLStreamReader */
    public FilteredXMLStreamReader(XMLStreamReader reader) {
        this.reader = reader;
    }
    
    public int getAttributeCount() {
        return reader.getAttributeCount();
    }
    
    public int getEventType() {
        return reader.getEventType();
    }
    
    public int getNamespaceCount() {
        return reader.getNamespaceCount();
    }
    
    public int getTextLength() {
        return reader.getTextLength();
    }
    
    public int getTextStart() {
        return reader.getTextStart();
    }
    
    public int next() throws XMLStreamException {
        int nextEvent = reader.next();
        if(nextEvent == XMLStreamReader.START_ELEMENT){
            startElemCounter++;
            if(startElemCounter == 1){
                return next();
            }
        }
        if(nextEvent == XMLStreamReader.END_ELEMENT){
            startElemCounter--;
            if(startElemCounter == 1){
                return next();
            }
        }
        return nextEvent;
    }
    
    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip whitespace
        || (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) // skip whitespace
        || eventType == XMLStreamConstants.SPACE
                || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                || eventType == XMLStreamConstants.COMMENT) {
            eventType = next();
        }
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("expected start or end tag", getLocation());
        }
        return eventType;
    }
    
    public void close() throws XMLStreamException {
        reader.close();
    }
    
    public boolean hasName() {
        return reader.hasName();
    }
    
    public boolean hasNext() throws XMLStreamException {
        return reader.hasNext();
    }
    
    public boolean hasText() {
        return reader.hasText();
    }
    
    public boolean isCharacters() {
        return reader.isCharacters();
    }
    
    public boolean isEndElement() {
        return reader.isEndElement();
    }
    
    public boolean isStandalone() {
        return reader.isStandalone();
    }
    
    public boolean isStartElement() {
        return reader.isStartElement();
    }
    
    public boolean isWhiteSpace() {
        return reader.isWhiteSpace();
    }
    
    public boolean standaloneSet() {
        return reader.standaloneSet();
    }
    
    public char[] getTextCharacters() {
        return reader.getTextCharacters();
    }
    
    public boolean isAttributeSpecified(int i) {
        return reader.isAttributeSpecified(i);
    }
    
    public int getTextCharacters(int i, char[] c, int i0, int i1) throws XMLStreamException {
        return reader.getTextCharacters(i, c, i0, i1);
    }
    
    public String getCharacterEncodingScheme() {
        return reader.getCharacterEncodingScheme();
    }
    
    public String getElementText() throws XMLStreamException {
        return reader.getElementText();
    }
    
    public String getEncoding() {
        return reader.getEncoding();
    }
    
    public String getLocalName() {
        return reader.getLocalName();
    }
    
    public String getNamespaceURI() {
        return reader.getNamespaceURI();
    }
    
    public String getPIData() {
        return reader.getPIData();
    }
    
    public String getPITarget() {
        return reader.getPITarget();
    }
    
    public String getPrefix() {
        return reader.getPrefix();
    }
    
    public String getText() {
        return reader.getText();
    }
    
    public String getVersion() {
        return reader.getVersion();
    }
    
    public String getAttributeLocalName(int i) {
        return reader.getAttributeLocalName(i);
    }
    
    public String getAttributeNamespace(int i) {
        return reader.getAttributeNamespace(i);
    }
    
    public String getAttributePrefix(int i) {
        return reader.getAttributePrefix(i);
    }
    
    public String getAttributeType(int i) {
        return reader.getAttributeType(i);
    }
    
    public String getAttributeValue(int i) {
        return reader.getAttributeValue(i);
    }
    
    public String getNamespacePrefix(int i) {
        return reader.getNamespacePrefix(i);
    }
    
    public String getNamespaceURI(int i) {
        return reader.getNamespaceURI(i);
    }
    
    public NamespaceContext getNamespaceContext() {
        return reader.getNamespaceContext();
    }
    
    public QName getName() {
        return reader.getName();
    }
    
    public QName getAttributeName(int i) {
        return reader.getAttributeName(i);
    }
    
    public Location getLocation() {
        return reader.getLocation();
    }
    
    public Object getProperty(String string) throws IllegalArgumentException {
        return reader.getProperty(string);
    }
    
    public void require(int i, String string, String string0) throws XMLStreamException {
        reader.require(i, string, string0);
    }
    
    public String getNamespaceURI(String string) {
        return reader.getNamespaceURI(string);
    }
    
    public String getAttributeValue(String string, String string0) {
        return reader.getAttributeValue(string, string0);
    }
    
}
