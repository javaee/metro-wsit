/*
 * $Id: TextPlainCanonicalizer.java,v 1.1 2010-10-05 11:56:00 m_potociar Exp $
 * $Revision: 1.1 $
 * $Date: 2010-10-05 11:56:00 $
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

package com.sun.xml.wss.impl.c14n;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import com.sun.xml.wss.util.CRLFOutputStream;

import com.sun.xml.wss.XWSSecurityException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.logging.Level;

/**
 *
 * Implementation of a text/plain canonicalizer as per rules
 * defined in RFC 2046 (http://www.rfc-editor.org/rfc/rfc2046.txt)
 * Section 4.1.
 *
 * @author  XWS-Security Team
 */
public class TextPlainCanonicalizer extends Canonicalizer {
    
    public TextPlainCanonicalizer() {}
    
    public TextPlainCanonicalizer(String charset) {
        super(charset);
    }
    
    public InputStream canonicalize(InputStream input,OutputStream outputStream)
    throws javax.xml.crypto.dsig.TransformException   {
        
        int len=0;
        byte [] data= null;
        try{
            data = new byte[128];
            len = input.read(data);
        } catch (IOException e) {                        
            log.log(Level.SEVERE, "WSS1002.error.canonicalizing.textplain", 
                    new Object[] {e.getMessage()});
            throw new javax.xml.crypto.dsig.TransformException(e);
        }
        CRLFOutputStream crlfOutStream = null;
        ByteArrayOutputStream bout = null;
        if(outputStream == null){
            bout = new ByteArrayOutputStream();
            crlfOutStream = new CRLFOutputStream(bout);
        }else{
            crlfOutStream = new CRLFOutputStream(outputStream);
        }
        
        while(len > 0){
            try {
                crlfOutStream.write(data,0,len);
                len = input.read(data);
            } catch (IOException e) {
                log.log(Level.SEVERE, "WSS1002.error.canonicalizing.textplain", 
                    new Object[] {e.getMessage()});
                throw new javax.xml.crypto.dsig.TransformException(e);
            }
        }
        
        if(outputStream == null){
            byte [] inputData = bout.toByteArray();
            return new ByteArrayInputStream(inputData);
        }
        return null;
    }
    
    /*
     * Important aspects of "text" media type canonicalization include line
     * ending normalization to <CR><LF>.
     * Section 4.1.1. [RFC 2046]
     */
    public byte[] canonicalize(byte[] inputBytes) throws XWSSecurityException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        CRLFOutputStream crlfOutStream = new CRLFOutputStream(bout);
        try {
            crlfOutStream.write(inputBytes);
        } catch (IOException e) {
            log.log(Level.SEVERE, "WSS1002.error.canonicalizing.textplain", 
                    new Object[] {e.getMessage()});
            throw new XWSSecurityException(e);
        }
        return bout.toByteArray();
    }
    
}
