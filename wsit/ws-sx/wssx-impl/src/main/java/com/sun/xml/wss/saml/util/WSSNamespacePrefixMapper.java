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

package com.sun.xml.wss.saml.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.wss.impl.MessageConstants;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class WSSNamespacePrefixMapper extends NamespacePrefixMapper{
    private boolean soap12;
    
    /** Creates a new instance of NamespacePrefixMapper */
    public WSSNamespacePrefixMapper() {
    }
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
        if(MessageConstants.SAML_v1_1_NS.equals(namespaceUri)){
            return MessageConstants.SAML_PREFIX;
        }
        if(MessageConstants.SAML_v2_0_NS.equals(namespaceUri)){
            return MessageConstants.SAML2_PREFIX;
        }
        if("http://www.w3.org/2001/10/xml-exc-c14n#".equals(namespaceUri)){
            return "exc14n";
        }
      
        return null;
    }
    
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { };
    }

    
}
