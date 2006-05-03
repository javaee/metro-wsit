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
 * SAXEXC14nCanonicalizerImpl.java
 *
 * Created on August 20, 2005, 5:01 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.c14n;

import org.xml.sax.*;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SAXEXC14nCanonicalizerImpl  implements ContentHandler{
    
    /** Creates a new instance of SAXEXC14nCanonicalizerImpl */
    public SAXEXC14nCanonicalizerImpl () {
    }
    
    public void setDocumentLocator (Locator locator) {
    }
    
    public void startDocument () throws SAXException {
    }
    
    public void endDocument () throws SAXException {
    }
    
    public void startPrefixMapping (String prefix, String uri) throws SAXException {
    }
    
    public void endPrefixMapping (String prefix) throws SAXException {
    }
    
    public void startElement (String uri, String localName, String qName, Attributes atts) throws SAXException {
    }
    
    public void endElement (String uri, String localName, String qName) throws SAXException {
    }
    
    public void characters (char[] ch, int start, int length) throws SAXException {
    }
    
    public void ignorableWhitespace (char[] ch, int start, int length) throws SAXException {
    }
    
    public void processingInstruction (String target, String data) throws SAXException {
    }
    
    public void skippedEntity (String name) throws SAXException {
    }
    
}
