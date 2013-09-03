/*
 * $Id: AttachmentContentOnlyTransform.java,v 1.6 2008/07/03 05:29:06 ofung Exp $
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

package com.sun.xml.wss.impl.transform;

import javax.mail.internet.ContentType;

import com.sun.xml.wss.impl.c14n.Canonicalizer;
import com.sun.xml.wss.impl.c14n.CanonicalizerFactory;

import com.sun.xml.wss.impl.resolver.AttachmentSignatureInput;

import com.sun.org.apache.xml.internal.security.transforms.TransformSpi;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput; 
import com.sun.org.apache.xml.internal.security.transforms.TransformationException;

public class AttachmentContentOnlyTransform extends TransformSpi {

   private static final String implementedTransformURI =
          "http://docs.oasis-open.org/wss/2004/XX/" + 
          "oasis-2004XX-wss-swa-profile-1.0#Attachment-Content-Only-Transform";

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
       //ContentType contentType = new ContentType(((AttachmentSignatureInput)input).getContentType()); 

       Canonicalizer canonicalizer = 
                             CanonicalizerFactory.
                                   getCanonicalizer(((AttachmentSignatureInput)input).getContentType());

       return canonicalizer.canonicalize(inputContentBytes);    
   }

   public boolean wantsOctetStream ()   { return true; }
   public boolean wantsNodeSet ()       { return true; }
   public boolean returnsOctetStream () { return true; }
   public boolean returnsNodeSet ()     { return false; }
}
