/*
 * $Id: AttachmentSignatureInput.java,v 1.1 2006-05-03 22:57:58 arungupta Exp $
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

package com.sun.xml.wss.impl.resolver;

import java.util.Vector;
import java.util.Iterator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.soap.SOAPException;
import javax.xml.soap.AttachmentPart;

import com.sun.xml.wss.swa.MimeConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;

public class AttachmentSignatureInput extends XMLSignatureInput {
   private String _cType = null;
   private Vector _mhs = new Vector();

   public AttachmentSignatureInput(byte[] input) { 
       super(input);
   }

   public void setMimeHeaders(Vector mimeHeaders) {
       _mhs = mimeHeaders;
   }

   public Vector getMimeHeaders() {
      return _mhs;
   }

   public void setContentType(String _cType) {
       this._cType = _cType;
   }

   public String getContentType() {
       return _cType;
   }

   public static final Object[] _getSignatureInput(AttachmentPart _part) throws SOAPException, IOException {
       Iterator mhItr = _part.getAllMimeHeaders();

       Vector mhs = new Vector();
       while (mhItr.hasNext()) mhs.add(mhItr.next());        

       // extract Content
       Object content = _part.getContent();
       OutputStream baos = new ByteArrayOutputStream();  
       _part.getDataHandler().writeTo(baos);          

       return new Object[] {mhs, ((ByteArrayOutputStream)baos).toByteArray()};
   }
}


