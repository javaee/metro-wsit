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

package com.sun.xml.ws.security.trust.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class TrustNamespacePrefixMapper extends NamespacePrefixMapper {

    
    public String getPreferredPrefix(final String namespaceUri, final String suggestion, final boolean requirePrefix) {
        // I want this namespace to be mapped to "xsi"
        if( "http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri) ) {
            return "xsi";
        }
         
        // I want the namespace foo to be the default namespace.
        if( "http://schemas.xmlsoap.org/ws/2005/02/trust".equals(namespaceUri) ) {
            return "wst";
        }
        
        if( "http://docs.oasis-open.org/ws-sx/ws-trust/200512".equals(namespaceUri) ) {
            return "trust";
        }

        if( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd".equals(namespaceUri) ) {
            return "wsu";
        }
        
        if( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd".equals(namespaceUri) ) {
            return "wsse";
        }
       
        if( "http://schemas.xmlsoap.org/ws/2005/02/sc".equals(namespaceUri) ) {
            return "wssc";
        }
        
        if( "http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512".equals(namespaceUri) ) {
            return "sc";
        }
        
        if( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd".equals(namespaceUri) ) {
            return "wsse";
        }
        
        if( "http://schemas.xmlsoap.org/ws/2004/09/policy".equals(namespaceUri) ) {
            return "wsp";
        }
        
        if( "http://www.w3.org/2005/08/addressing".equals(namespaceUri) ) {
            return "wsa";
        }
        
        // otherwise I don't care. Just use the default suggestion, whatever it may be.
        return suggestion;
    }
}


