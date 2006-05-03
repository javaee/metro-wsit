/*
 * $Id: Canonicalizer.java,v 1.1 2006-05-03 22:57:40 arungupta Exp $
 * $Revision: 1.1 $
 * $Date: 2006-05-03 22:57:40 $
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

import com.sun.xml.wss.swa.MimeConstants;

import com.sun.xml.wss.XWSSecurityException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;

/**
 * Interface for defining MIME Content Canonicalizer.
 * (Section 4.3.2) "MIME Content Canonicalization"; SwA
 *
 * @author  XWS-Security Team
 */
public abstract class Canonicalizer {
    String _charset = MimeConstants.US_ASCII;

    protected static Logger log =  Logger.getLogger( 
            LogDomainConstants.IMPL_CANON_DOMAIN,
            LogDomainConstants.IMPL_CANON_DOMAIN_BUNDLE);

    public Canonicalizer() {}
    
    Canonicalizer(String charset) {
        this._charset = charset;
    }
    
    /*
     * Main method that performs the actual Canonicalization.
     */
    public abstract byte[] canonicalize(byte[] input) throws XWSSecurityException;
    public abstract InputStream canonicalize(InputStream input,OutputStream outputStream) throws javax.xml.crypto.dsig.TransformException ;
    
    void setCharset(String charset) {
        this._charset = charset;
    }
    
    public String getCharset() {
        return _charset;
    }
}
