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
