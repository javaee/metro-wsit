/*
 * $Id: CanonicalizerFactory.java,v 1.1 2006-05-03 22:57:40 arungupta Exp $
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

import java.util.HashMap;
import java.nio.charset.Charset;
import javax.xml.soap.AttachmentPart;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import com.sun.xml.wss.XWSSecurityException;

import com.sun.xml.wss.swa.MimeConstants;

/**
 *
 * @author  XWS-Security Team
 */
public class CanonicalizerFactory {

    static MimeHeaderCanonicalizer _mhCanonicalizer = null;

    static HashMap _canonicalizers = new HashMap(10);
   
    public static final Canonicalizer getCanonicalizer(String mimeType) throws Exception {
        ContentType contentType = new ContentType(mimeType);        
        String baseMimeType = contentType.getBaseType();
        
        if (baseMimeType.equalsIgnoreCase(MimeConstants.TEXT_PLAIN_TYPE)) {
            ensureRegisteredCharset(contentType);
        }

        // use primaryMimeType as the key.
        // i.e. text canonicalizer will apply to text/* etc.
        String primaryMimeType = contentType.getPrimaryType();
        Canonicalizer _canonicalizer = 
                           (Canonicalizer)_canonicalizers.get(primaryMimeType);
        
        if (_canonicalizer == null) {
            _canonicalizer = newCanonicalizer(primaryMimeType);
        }

        // defaults to US-ASCII
        String charset = contentType.getParameter("charset"); 
        if (charset != null) _canonicalizer.setCharset(charset);
        
        return _canonicalizer;
    }

    /*
     * Primary MimeType is the key. 
     * ImageCanonicalizer is sufficient for all image/** MIME types and so on.
     *
     * Finer grained processing as per section 4.1.4 RFC2046  has not been 
     * incorporated yet. I don't think so much processing is required at this time.
     *
     */ 
    public static final Canonicalizer newCanonicalizer(String primaryMimeType) {
        Canonicalizer canonicalizer = null; 
        
        if (primaryMimeType.equalsIgnoreCase("text"))
            canonicalizer = new TextPlainCanonicalizer();
        else
        if (primaryMimeType.equalsIgnoreCase("image"))
            canonicalizer = new ImageCanonicalizer();
        else
        if (primaryMimeType.equalsIgnoreCase("application"))
            canonicalizer = new ApplicationCanonicalizer();
        else
            ; // log n throw exception

        _canonicalizers.put(primaryMimeType, canonicalizer);

        return canonicalizer;
    }

    public static final MimeHeaderCanonicalizer getMimeHeaderCanonicalizer(String charset) {
        if (_mhCanonicalizer == null)
            _mhCanonicalizer = new MimeHeaderCanonicalizer();
        _mhCanonicalizer.setCharset(charset); 
        return _mhCanonicalizer;
    }

    public static void registerCanonicalizer(String baseMimeType,
                                             Canonicalizer implementingClass) {
         _canonicalizers.put(baseMimeType, implementingClass);
    }
    
    public static void registerCanonicalizer(String baseMimeType,
                                             String implementingClass) throws XWSSecurityException {
         try {
             Class _class = Class.forName(implementingClass);
             Canonicalizer canonicalizer = (Canonicalizer)_class.newInstance();            
             _canonicalizers.put(baseMimeType, canonicalizer);
         } catch (Exception e) {
             // log
             throw new XWSSecurityException(e);
         } 
    }

    /*
     * Ensure that the charset is a registered charset - see RFC 2633.
     * This assumes the complete content-type string will be input as the parameter.
     * (including charset param value).
     *
     * Section 4.1.2.  Charset Parameter [RFC 2046]
     * Example of charset parameter as per the RFC definition is -
     * Content-type: text/plain; charset=iso-8859-1
     */
    public static boolean ensureRegisteredCharset(ContentType contentType) 
                                           throws XWSSecurityException {
        String charsetName = contentType.getParameter("charset");
        if (charsetName != null) {            
            return Charset.forName(charsetName).isRegistered();
        }
        return true;
    }
}
