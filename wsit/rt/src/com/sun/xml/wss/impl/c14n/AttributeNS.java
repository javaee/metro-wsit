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

/*
 * AttributeNS.java
 *
 * Created on August 21, 2005, 9:37 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.c14n;

import java.io.ByteArrayOutputStream;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class AttributeNS implements Cloneable , Comparable {
    private String uri;
    private String prefix;
    private boolean written = false;
    byte [] utf8Data = null;
    int code = 0;
    /** Creates a new instance of AttributeNS */
    public AttributeNS() {
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public boolean isWritten() {
        return written;
    }
    
    public void setWritten(boolean written) {
        this.written = written;
    }
    
    public Object clone() throws CloneNotSupportedException {
        AttributeNS attrNS = new AttributeNS();
        attrNS.setPrefix(this.prefix);
        attrNS.setUri(this.uri);
        return attrNS;
    }
    
    public boolean equals(Object obj) {
        if(!(obj instanceof AttributeNS)){
            return false;
        }
        AttributeNS attrNS = (AttributeNS)obj;
        if(this.uri == null || this.prefix == null){
            return false;
        }
        if(this.prefix.equals(attrNS.getPrefix()) && this.uri.equals(attrNS.getUri())){
            return true;
        }
        return false;
    }
    
    public int hashCode(){
        if(code ==0){
            if(uri!=null){
                code =uri.hashCode();
            }
            if(prefix !=null){
                code =code+prefix.hashCode();
            }
        }
        return code;
    }
    
    public byte [] getUTF8Data(ByteArrayOutputStream tmpBuffer){
        if(utf8Data == null){
            try{
                BaseCanonicalizer.outputAttrToWriter("xmlns",prefix,uri,tmpBuffer);
                utf8Data = tmpBuffer.toByteArray();
            }catch(Exception ex){
                utf8Data = null;
                //should not occur
                //log
            }
        }
        return utf8Data;
    }
    
    public int compareTo(Object cmp) {
        return sortNamespaces(cmp, this);
    }
    
    protected int sortNamespaces(Object object, Object object0) {
        AttributeNS attr = (AttributeNS)object;
        AttributeNS attr0 = (AttributeNS)object0;
        //assume namespace processing is on.
        String lN = attr.getPrefix();
        String lN0 = attr0.getPrefix();
        return lN.compareTo(lN0);
    }
    
    public void reset(){
        utf8Data = null;
        prefix = null;
        written = false;
        uri = null;
    }
}
