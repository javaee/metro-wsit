/*
 * $Id: TextPlainCanonicalizer.java,v 1.1 2006-05-03 22:57:42 arungupta Exp $
 * $Revision: 1.1 $
 * $Date: 2006-05-03 22:57:42 $
 */

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

package com.sun.xml.wss.impl.c14n;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import com.sun.mail.util.CRLFOutputStream;

import com.sun.xml.wss.XWSSecurityException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

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
