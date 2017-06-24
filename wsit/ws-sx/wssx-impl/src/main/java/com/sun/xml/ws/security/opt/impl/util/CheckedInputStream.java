/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Ashutosh.Shahi@Sun.com
 */
public class CheckedInputStream extends FilterInputStream{
    
    int read;
    boolean isEmpty = false;
    boolean xmlDecl = false;
    byte[] tmpBytes = new byte[4];
    ByteArrayInputStream tmpIs = null;
    
    /** Creates a new instance of CheckedCipherInputStream */
    public CheckedInputStream(InputStream cin) throws IOException {
        super(cin);
        read = cin.read();
        if(read == -1){
            isEmpty = true;
        }else{
            cin.read(tmpBytes, 0, 4);
            tmpIs = new ByteArrayInputStream(tmpBytes);
        }
    }
    
    public int read() throws IOException{
        if(read != -1){
            int tmp = read;
            read = -1;
            
            if(tmp == '<' && "?xml".equals(new String(tmpBytes))){
                xmlDecl = true;
                int c = super.read();
                while(c != '>'){
                    //do nothing
                    c = super.read();
                }
            }
            
            if(!xmlDecl){
                return tmp;
            }
        }
        
        if(!xmlDecl){
            int c = tmpIs.read();
            if(c != -1){
                return c;
            }
        }
        
        return super.read();
    }
    
    public int read(byte [] b) throws IOException{        
        return read(b,0,b.length);
    }
    
    public int read(byte[] b , int off, int len) throws IOException{
        if(read != -1){
            
            if(read == '<' && "?xml".equals(new String(tmpBytes))){
                xmlDecl = true;
                int c = super.read();
                while(c != '>'){
                    //do nothing
                    c = super.read();
                }
            }
            
            int i = 0;
            b[off + i] = (byte) read;
            i++;
            len--;
            read = -1;
            
            if(!xmlDecl){          
                
                int c = tmpIs.read();
                while(c != -1 && len > 0){
                    b[off + i] = (byte)c;
                    i++;
                    c = tmpIs.read();
                    len--;
                }              
                
            }
            int rb = 0;
            if(len > 0){
                rb = super.read(b,off+i,len);
            }
            
            return rb+i;
        }
        return super.read(b,off,len);
    }
    public long skip(long n) throws IOException {
        if(read != -1){
            read = -1;
            return super.skip(n-1) + 1;
        }
        return super.skip(n);
    }
    
    public boolean isEmpty(){
        return isEmpty;
    }
}
