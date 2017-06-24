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
