/*
 * $Id: AttachmentCompleteTransform.java,v 1.1 2006-05-03 22:57:59 arungupta Exp $
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

package com.sun.xml.wss.impl.transform;

import javax.xml.soap.AttachmentPart;

import javax.mail.internet.ContentType;

import com.sun.xml.wss.impl.c14n.Canonicalizer;
import com.sun.xml.wss.impl.c14n.CanonicalizerFactory;
import com.sun.xml.wss.impl.c14n.MimeHeaderCanonicalizer;

import com.sun.xml.wss.impl.resolver.AttachmentSignatureInput;

import com.sun.org.apache.xml.internal.security.transforms.TransformSpi;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput; 
import com.sun.org.apache.xml.internal.security.transforms.TransformationException;

public class AttachmentCompleteTransform extends TransformSpi {

   private static final String implementedTransformURI =
          "http://docs.oasis-open.org/wss/2004/XX/" + 
          "oasis-2004XX-wss-swa-profile-1.0#Attachment-Complete-Transform";

   protected String engineGetURI() {
       return implementedTransformURI;
   }

   protected XMLSignatureInput enginePerformTransform(
             XMLSignatureInput input)
             throws TransformationException {
       try {
            return new XMLSignatureInput(_canonicalize(input));
       } catch (Exception e) {
            // log
            throw new TransformationException(e.getMessage(), e);
       }  
   }

   private byte[] _canonicalize(XMLSignatureInput input) throws Exception {
       byte[] inputContentBytes = input.getBytes();
       ContentType contentType = new ContentType(((AttachmentSignatureInput)input).getContentType()); 

       // rf. RFC822 
       MimeHeaderCanonicalizer mHCanonicalizer = CanonicalizerFactory.getMimeHeaderCanonicalizer("US-ASCII");
       byte[] outputHeaderBytes = mHCanonicalizer._canonicalize( ((AttachmentSignatureInput)input).getMimeHeaders().iterator());
        
       Canonicalizer canonicalizer = 
                             CanonicalizerFactory.
                                   getCanonicalizer(((AttachmentSignatureInput)input).getContentType());
       byte[] outputContentBytes = canonicalizer.canonicalize(inputContentBytes); 

       byte[] outputBytes = new byte[outputHeaderBytes.length+outputContentBytes.length];
       System.arraycopy(outputHeaderBytes, 0, outputBytes, 0, outputHeaderBytes.length);
       System.arraycopy(outputContentBytes, 0, outputBytes, 
                                   outputHeaderBytes.length, outputContentBytes.length); 

       return outputBytes;
   }

   public boolean wantsOctetStream ()   { return true; }
   public boolean wantsNodeSet ()       { return true; }
   public boolean returnsOctetStream () { return true; }
   public boolean returnsNodeSet ()     { return false; }
}
