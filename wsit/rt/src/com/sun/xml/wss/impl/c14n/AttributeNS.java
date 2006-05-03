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
