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
 * ByteArray.java
 *
 * Created on September 19, 2010, 8:19 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.impl.misc;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class ByteArray {
    byte [] iv = null;
    byte [] ed = null;
    int length = 0;
    /** Creates a new instance of ByteArray */
    public ByteArray(byte [] iv, byte [] ed) {
        this.iv = iv;
        this.ed = ed;
        if(this.iv != null){
            length = iv.length;
        }
        if(this.ed != null){
            length = length+ed.length;
        }
    }
    
    public int getLength(){
        return length;
    }
    
    public byte byteAt(int i){
        if(i < 0 || i > length){
            throw new ArrayIndexOutOfBoundsException("Index "+i +" is out of range");
        }
        if(iv != null && i < iv.length){
            return iv[i];
        }else if (iv == null || iv.length == 0){
            return ed[i];
		}else{
            int index = i-iv.length;
            return ed[index];
        }
    }
}
