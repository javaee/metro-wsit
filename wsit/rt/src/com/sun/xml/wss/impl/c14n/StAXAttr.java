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
 * StAXAttr.java
 *
 * Created on August 22, 2005, 5:24 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.c14n;

/**
 *
 * @author root
 */
public class StAXAttr implements Comparable{
    private String prefix = "";
    private String value = null;
    private String localName = null;
    private String uri = "";
    /** Creates a new instance of StAXAttr */
    public StAXAttr() {
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        if(prefix == null){
            return;
        }
        this.prefix = prefix;
    }
    
    
    
    public String getLocalName() {
        return localName;
    }
    
    public void setLocalName(String localName) {
        this.localName = localName;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        if(uri == null){
            return;
        }
        this.uri = uri;
    }
    
    public int compareTo(Object cmp) {
        return sortAttributes(cmp, this);
    }
    
    protected int sortAttributes(Object object, Object object0) {
        StAXAttr attr = (StAXAttr)object;
        StAXAttr attr0 = (StAXAttr)object0;
        String uri = attr.getUri();
        String uri0 = attr0.getUri();
        int result = uri.compareTo(uri0);
        if(result == 0){
            String lN = attr.getLocalName();
            String lN0 = attr0.getLocalName();
            result = lN.compareTo(lN0);
        }
        return result;
    }
    
}
