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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class DecryptedInputStream extends FilterInputStream{
    
    private static final int SKIP_BUFFER_SIZE = 2048;
    // skipBuffer is initialized in skip(long), if needed.
    private static byte[] skipBuffer;
    
    private StringBuilder startElement = new StringBuilder("<StartElement");
    private static final String endElement = "</StartElement>";
    private InputStream startIS = null;
    private InputStream endIS = new ByteArrayInputStream(endElement.getBytes());
    
    /** Creates a new instance of DecryptedInputStream */
    public DecryptedInputStream(InputStream is, HashMap<String,String> parentNS) {
        super(is);
        Set<Map.Entry<String, String>> set = parentNS.entrySet(); 
        Iterator<Map.Entry<String, String>> iter = set.iterator();
        while(iter.hasNext()){
           Map.Entry<String, String> entry = iter.next();
           if(!"".equals(entry.getKey())){
               startElement.append(" xmlns:"+entry.getKey()+"=\""+entry.getValue()+"\"");
           } else{
               startElement.append(" xmlns=\"" + entry.getValue()+"\"");
           }
        }
        startElement.append(" >");
        String startElem = startElement.toString();
        startIS = new ByteArrayInputStream(startElem.getBytes());
    }
    
    public int read() throws IOException{
        int readVal = startIS.read();
        if(readVal != -1){
            return readVal;
        }
        readVal = in.read();
        if(readVal != -1){
            return readVal;
        }
        return endIS.read();
    }
    
    public int read(byte [] b) throws IOException{
        return read(b,0,b.length-1);
    }
    
    public int read(byte[] b , int off, int len) throws IOException{
        if (b == null) {
	    throw new NullPointerException();
	} else if ((off < 0) || (off > b.length) || (len < 0) ||
		   ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return 0;
	}
        int readVal = read();
        if(readVal == -1){
            return -1;
        }
        b[off] = (byte)readVal;
        int i = 1;
        for(; i < len; i++){
            readVal = read();
            if(readVal == -1){
                break;
            }
            if(b != null){
                b[off+i] = (byte)readVal;
            }
        }
        return i;
    }
    
    public long skip(long n) throws IOException {
        long remaining = n;
	int nr;
	if (skipBuffer == null)
	    skipBuffer = new byte[SKIP_BUFFER_SIZE];

	byte[] localSkipBuffer = skipBuffer;
        
        if (n <= 0) {
	    return 0;
	}

	while (remaining > 0) {
	    nr = read(localSkipBuffer, 0,
		      (int) Math.min(SKIP_BUFFER_SIZE, remaining));
	    if (nr < 0) {
		break;
	    }
	    remaining -= nr;
	}
	
	return n - remaining;
    }
    
    public boolean markSupported() {
	return false;
    }
    
    public synchronized void reset() throws IOException {
	throw new IOException("mark/reset not supported");
    }
    
    public void close() throws IOException{
        startIS.close();
        in.close();
        endIS.close();
    }
    
}
