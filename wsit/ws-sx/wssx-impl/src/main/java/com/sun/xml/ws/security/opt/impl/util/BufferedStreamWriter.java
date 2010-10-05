/*
 * BufferedStreamWriter.java
 *
 * Created on August 7, 2006, 11:39 AM
 */

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

package com.sun.xml.ws.security.opt.impl.util;

import java.io.IOException;
import javax.crypto.CipherOutputStream;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class BufferedStreamWriter extends java.io.OutputStream {
    int size=4*1024;
    byte []buf=null;
    int pos =0;
    CipherOutputStream cos = null;
    /** Creates a new instance of BufferedStreamWriter */
    public BufferedStreamWriter(CipherOutputStream cos) {
        buf =new byte[this.size];
        this.cos = cos;
    }
    public BufferedStreamWriter(CipherOutputStream cos, int size) {
        buf =new byte[size];
        this.cos = cos;
    }
    public void write(byte[] arg0)throws IOException {
        int newPos=pos+arg0.length;
        if (newPos>=size) {
            flush();
            System.arraycopy(arg0,0,buf,0,arg0.length);
            pos = arg0.length;
        }else{
            System.arraycopy(arg0,0,buf,pos,arg0.length);
            pos=newPos;
        }
    }
    /** @inheritDoc */
    public void write(byte[] arg0, int arg1, int arg2)throws IOException {
        int newPos=pos+arg2;
        if (newPos>=size) {
            flush();
            System.arraycopy(arg0,arg1,buf,0,arg2);
            pos = arg2;
        }else{
            System.arraycopy(arg0,arg1,buf,pos,arg2);
            pos=newPos;
        }
    }
    /** @inheritDoc */
    public void write(int arg0)throws IOException {
        if (pos>=size) {
            flush();
        }
        buf[pos++]=(byte)arg0;
    }
    
    public void flush() throws IOException {
        cos.write(buf,0,pos);
        pos = 0;
    }
}
