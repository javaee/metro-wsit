/*
 * $Id: ApplicationCanonicalizer.java,v 1.1 2006-05-03 22:57:39 arungupta Exp $
 * $Revision: 1.1 $
 * $Date: 2006-05-03 22:57:39 $
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

import com.sun.xml.wss.XWSSecurityException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Canoncializer for an Application Media Type.
 * This should work for all application/** MIME types.
 *
 * @author  XWS-Security Team
 */
public class ApplicationCanonicalizer extends Canonicalizer {

    public ApplicationCanonicalizer() {}
    
    public ApplicationCanonicalizer(String charset) {
        super(charset);
    }
    
    public byte[] canonicalize(byte[] input) throws XWSSecurityException {
        return input;
    }
    
    public InputStream canonicalize(InputStream input,OutputStream outputStream)
    throws javax.xml.crypto.dsig.TransformException {
        try{
            if(outputStream == null){
                return input;
            }else{
                byte [] data = new byte[128];
                while(true){
                    int len = input.read(data);
                    if(len <= 0)
                        break;
                    outputStream.write(data,0,len);
                }
            }
            return null;
        }catch(Exception ex){
            log.log(Level.SEVERE, "WSS1000.error.canonicalizing", 
                    new Object[] {ex.getMessage()});
            throw new javax.xml.crypto.dsig.TransformException(ex.getMessage());
        }
    }
}
