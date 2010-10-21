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

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.wss.impl.MessageConstants;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class WSSNamespacePrefixMapper extends NamespacePrefixMapper{
    
    private boolean soap12 = false;
    /** Creates a new instance of NamespacePrefixMapper */
    public WSSNamespacePrefixMapper() {
    }
    
    /** Creates a new instance of NamespacePrefixMapper */
    public WSSNamespacePrefixMapper(boolean soap12) {
        this.soap12 = soap12;
    }
    
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if(MessageConstants.WSSE_NS.equals(namespaceUri)){
            return MessageConstants.WSSE_PREFIX;
        }
        
        if(MessageConstants.WSSE11_NS.equals(namespaceUri)){
            return MessageConstants.WSSE11_PREFIX;
        }
        if(MessageConstants.XENC_NS.equals(namespaceUri)){
            return MessageConstants.XENC_PREFIX;
        }
        if(MessageConstants.DSIG_NS.equals(namespaceUri)){
            return MessageConstants.DSIG_PREFIX;
        }
        if(MessageConstants.WSU_NS.equals(namespaceUri)){
            return MessageConstants.WSU_PREFIX;
        }
        if(MessageConstants.WSSC_NS.equals(namespaceUri)){
            return MessageConstants.WSSC_PREFIX;
        }
        if("http://www.w3.org/2001/10/xml-exc-c14n#".equals(namespaceUri)){
            return "exc14n";
        }
        if(MessageConstants.SOAP_1_1_NS.equals(namespaceUri)){
            return "S";
        }
        
        if(MessageConstants.SOAP_1_2_NS.equals(namespaceUri)){
            return "S";
        }
        if("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)){
            return "xsi";
        }
        return null;
    }
    
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { };
    }
    
    public String[] getContextualNamespaceDecls() {
        if(!soap12){
            return new String[] {MessageConstants.WSSE_PREFIX, MessageConstants.WSSE_NS,MessageConstants.WSSE11_PREFIX,
            MessageConstants.WSSE11_NS,MessageConstants.XENC_PREFIX,MessageConstants.XENC_NS,MessageConstants.DSIG_PREFIX,MessageConstants.DSIG_NS,
            MessageConstants.WSU_PREFIX,MessageConstants.WSU_NS, MessageConstants.WSSC_PREFIX,MessageConstants.WSSC_NS,"exc14n","http://www.w3.org/2001/10/xml-exc-c14n#",
            "S",MessageConstants.SOAP_1_1_NS};
        }else{
            return new String[] {MessageConstants.WSSE_PREFIX, MessageConstants.WSSE_NS,MessageConstants.WSSE11_PREFIX,
            MessageConstants.WSSE11_NS,MessageConstants.XENC_PREFIX,MessageConstants.XENC_NS,MessageConstants.DSIG_PREFIX,MessageConstants.DSIG_NS,
            MessageConstants.WSU_PREFIX,MessageConstants.WSU_NS, MessageConstants.WSSC_PREFIX,MessageConstants.WSSC_NS,"exc14n","http://www.w3.org/2001/10/xml-exc-c14n#",
            "S",MessageConstants.SOAP_1_2_NS};
        }
    }
    
}
