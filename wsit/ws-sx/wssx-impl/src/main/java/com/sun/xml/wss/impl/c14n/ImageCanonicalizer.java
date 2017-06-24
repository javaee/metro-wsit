/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: ImageCanonicalizer.java,v 1.2 2010-10-21 15:37:19 snajper Exp $
 * $Revision: 1.2 $
 * $Date: 2010-10-21 15:37:19 $
 */

package com.sun.xml.wss.impl.c14n;

import com.sun.xml.wss.XWSSecurityException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 * Section 4.2 on Image Media types in RFC 2046
 * http://www.rfc-editor.org/rfc/rfc2046.txt
 * does not specify any rules for image canonicalization.
 *
 * So assuming that this binary data need not be canonicalized.
 *
 * @author  XWS-Security Team
 */
public class ImageCanonicalizer extends Canonicalizer {
    
    public ImageCanonicalizer() {}
    
    public ImageCanonicalizer(String charset) {
        super(charset);
    }
    
    /*
     * RFC 3851 says - http://www.rfc-archive.org/getrfc.php?rfc=3851
     * Other than text types, most types
     * have only one representation regardless of computing platform or
     * environment which can be considered their canonical representation.
     *
     * So right now we are just serializing the attachment for gif data types.
     *
     */
    public byte[] canonicalize(byte[] input) throws XWSSecurityException {
        return input;
    }
    
    public InputStream canonicalize(InputStream input,OutputStream outputStream)
    throws javax.xml.crypto.dsig.TransformException  {
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
        }catch(Exception ex){
            log.log(Level.SEVERE, "WSS1001.error.canonicalizing.image", 
                    new Object[] {ex.getMessage()});
            throw new javax.xml.crypto.dsig.TransformException(ex.getMessage());
        }
        return null;
    }
}
