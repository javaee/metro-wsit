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
