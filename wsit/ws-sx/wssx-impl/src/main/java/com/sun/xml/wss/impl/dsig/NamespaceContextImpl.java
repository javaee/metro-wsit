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

package com.sun.xml.wss.impl.dsig;

import com.sun.xml.wss.impl.MessageConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

/**
 * Implements NamespaceContext .
 *
 * TODO : Performance Improvements.
 */
public class NamespaceContextImpl implements NamespaceContext{
    
    HashMap namespaceMap = null;
    
    public NamespaceContextImpl(){
        namespaceMap = new HashMap(10);
        this.add("SOAP-ENV","http://schemas.xmlsoap.org/soap/envelope/" );
        this.add("env","http://schemas.xmlsoap.org/soap/envelope/" );
        this.add("ds", MessageConstants.DSIG_NS);
        this.add("xenc", MessageConstants.XENC_NS);        
        this.add("wsse", MessageConstants.WSSE_NS);
        this.add("wsu", MessageConstants.WSU_NS);
        this.add("saml", MessageConstants.SAML_v1_0_NS);
        this.add("S11","http://schemas.xmlsoap.org/soap/envelope/" );
        this.add("S12","http://www.w3.org/2003/05/soap-envelope" );
    }
    
    /**
     * Add prefix and namespaceuri to be associated with the prefix.
     * @param prefix Namespace Prefix
     * @param uri NamespaceURI
     */
    @SuppressWarnings("unchecked")
    public void add(String prefix,String uri){
        namespaceMap.put(prefix,uri);
        
    }
    /**
     *
     * @param prefix
     * @return
     */
    public String getNamespaceURI(String prefix) {
        return	(String)namespaceMap.get(prefix);
    }
    
    /**
     *
     * @param namespaceURI
     * @return NamespaceURI associated with the prefix
     */
    public String getPrefix(String namespaceURI) {
        Iterator iterator = namespaceMap.keySet().iterator();
        while(iterator.hasNext()){
            String prefix = (String)iterator.next();
            String uri = (String)namespaceMap.get(prefix);
            if(namespaceURI.equals(uri))
                return prefix;
            
        }
        return null;
    }
    
    /**
     *
     * @param namespaceURI
     * @return Iterator to list of prefixes associated with the namespaceURI
     */
    @SuppressWarnings("unchecked")
    public Iterator getPrefixes(String namespaceURI) {
        
        ArrayList prefixList = new ArrayList();
        Iterator iterator = namespaceMap.keySet().iterator();
        while(iterator.hasNext()){
            String prefix = (String)iterator.next();
            
            String uri = (String)namespaceMap.get(prefix);
            
            if(namespaceURI.equals(uri)){
                prefixList.add(prefix);
            }
        }
        return  prefixList.iterator();
    }
    
    public Map getMap(){
        return namespaceMap;
    }
}
