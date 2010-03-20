/*
 * $Id: Canonicalizer.java,v 1.3 2010-03-20 12:32:21 kumarjayanti Exp $
 * $Revision: 1.3 $
 * $Date: 2010-03-20 12:32:21 $
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

import com.sun.xml.wss.swa.MimeConstants;

import com.sun.xml.wss.XWSSecurityException;
import java.io.InputStream;
import java.io.OutputStream;

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

    protected static final Logger log =  Logger.getLogger( 
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
