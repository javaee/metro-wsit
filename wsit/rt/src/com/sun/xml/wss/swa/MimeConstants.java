/*
 * $Id: MimeConstants.java,v 1.3.2.2 2010-07-14 14:09:39 m_potociar Exp $
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

package com.sun.xml.wss.swa;

public interface MimeConstants {
   // rfc822, rfc204(5|6|7) charset for mime headers
   public static final String US_ASCII  = "US-ASCII";

   // rfc822 mime headers
   public static final String CONTENT_TRANSFER_ENCODING
                                                     = "Content-Transfer-Encoding";
   public static final String CONTENT_DESCRIPTION    = "Content-Description";
   public static final String CONTENT_DISPOSITION    = "Content-Disposition";
   public static final String CONTENT_ID             = "Content-ID";
   public static final String CONTENT_LOCATION       = "Content-Location";
   public static final String CONTENT_TYPE           = "Content-Type";
   public static final String CONTENT_LENGTH         = "Content-Length";

   // rfc2045/6 canonicalized media type names
   public static final String TEXT_TYPE         = "text";
   public static final String IMAGE_TYPE        = "image";
   public static final String APPLICATION_TYPE  = "application";

   // rfc2045/6 canonicalized media sub-type names
   public static final String XML_TYPE          = "xml";
   public static final String PLAIN_TYPE        = "plain";
   public static final String JPEG_TYPE         = "jpeg";
   public static final String GIF_TYPE          = "gif";
   public static final String OCTET_STREAM_TYPE = "octet-stream";

   // rfc2045/6 canonicalized parameters for text/image/application types
   public static final String CHARSET           = "charset";
  
   // rfc2045/6 canonicalized parameter names for application type
      // for octet-stream subtype
      public static final String TYPE           = "type";
      public static final String PADDING        = "padding";
      public static final String CONVERSIONS    = "conversions";
      public static final String INTERPRETER    = "interpreter";

   //  rfc2183 canonicalized disposition type values for Content-Disposition
   public static final String INLINE            = "inline";
   public static final String ATTACHMENT        = "attachment";

   //  rfc2183 canonicalized parameter names for Content-Disposition
   public static final String FILENAME          = "filename";
   public static final String CREATION_DATE     = "creation-date";
   public static final String MODIFICATION_DATE = "modification-date";
   public static final String READ_DATE         = "read-date";
   public static final String SIZE              = "size";

   // rfc2045/6 content-types
   public static final String TEXT_XML_TYPE     = TEXT_TYPE + "/" + XML_TYPE;
   public static final String TEXT_PLAIN_TYPE   = TEXT_TYPE + "/" + PLAIN_TYPE;
   public static final String IMAGE_JPEG_TYPE   = IMAGE_TYPE + "/" + JPEG_TYPE;
   public static final String IMAGE_GIF_TYPE    = IMAGE_TYPE + "/" + GIF_TYPE;
   public static final String APPLICATION_OCTET_STREAM_TYPE 
                                   = APPLICATION_TYPE + "/" + OCTET_STREAM_TYPE;
                                                
}