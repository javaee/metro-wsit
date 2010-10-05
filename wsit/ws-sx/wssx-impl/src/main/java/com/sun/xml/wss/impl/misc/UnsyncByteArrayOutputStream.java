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
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 1995-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.xml.wss.impl.misc;

import java.io.ByteArrayOutputStream;

/**
 * A simple Unsynced ByteArryOutputStream
 * @author raul
 *
 */
public class UnsyncByteArrayOutputStream extends ByteArrayOutputStream {
    int size=4*1024;
    //redefines member buf : But this is a class obtained from Apache, see license above
    //Findbugs shows this as a correctness issue.
    byte []buf=null;
    int pos =0;
    
    public UnsyncByteArrayOutputStream() {
        buf =new byte[size];
    }
    
    public UnsyncByteArrayOutputStream(int size) {
        this.size = size;
        buf =new byte[size];
    }
    
    /** @inheritDoc */
    public void write(byte[] arg0) {
        int newPos=pos+arg0.length;
        if (newPos>size) {
            expandSize();
        }
        System.arraycopy(arg0,0,buf,pos,arg0.length);
        pos=newPos;
    }
    /** @inheritDoc */
    public void write(byte[] arg0, int arg1, int arg2) {
        int newPos=pos+arg2;
        if (newPos>size) {
            expandSize();
        }
        System.arraycopy(arg0,arg1,buf,pos,arg2);
        pos=newPos;
    }
    /** @inheritDoc */
    public void write(int arg0) {
        if (pos>=size) {
            expandSize();
        }
        buf[pos++]=(byte)arg0;
    }
    /** @inheritDoc */
    public byte[] toByteArray() {
        byte result[]=new byte[pos];
        System.arraycopy(buf,0,result,0,pos);
        return result;
    }
    
    /** @inheritDoc */
    public void reset() {
        pos=0;
    }
    
    /** @inheritDoc */
    void expandSize() {
        int newSize=size<<2;
        byte newBuf[]=new byte[newSize];
        System.arraycopy(buf,0,newBuf,0,pos);
        buf=newBuf;
        size=newSize;
    }
    
    public int getLength(){
        return pos;
    }
    
    public byte[] getBytes(){
        return buf;
    }
}
