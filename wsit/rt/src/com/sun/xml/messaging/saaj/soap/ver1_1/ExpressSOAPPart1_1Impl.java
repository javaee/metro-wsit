/*
 * $Id: ExpressSOAPPart1_1Impl.java,v 1.1 2010-03-20 12:35:06 kumarjayanti Exp $
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

/**
 *
 * @author SAAJ RI Development Team
 */
package com.sun.xml.messaging.saaj.soap.ver1_1;

import com.sun.xml.security.jaxws.JAXWSMessage;
import com.sun.xml.messaging.saaj.soap.ver1_1.ExpressEnvelope1_1Impl;
import com.sun.xml.messaging.saaj.soap.ver1_1.ExpressEnvelope1_1Impl;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;

import com.sun.xml.messaging.saaj.soap.*;
import com.sun.xml.messaging.saaj.soap.impl.EnvelopeImpl;
import com.sun.xml.messaging.saaj.util.LogDomainConstants;


public class ExpressSOAPPart1_1Impl extends SOAPPart1_1Impl implements SOAPConstants {
    JAXWSMessage jxMessage = null;
    protected static Logger log =
            Logger.getLogger (LogDomainConstants.SOAP_VER1_1_DOMAIN,
            "com.sun.xml.messaging.saaj.soap.ver1_1.LocalStrings");
    
    public  ExpressSOAPPart1_1Impl () {
        super ();
    }
    
    public  ExpressSOAPPart1_1Impl (ExpressMessage1_1Impl message) {
        super ( (MessageImpl) message);
        jxMessage = message.getJAXWSMessage();
    }
    
    
    
    protected String getContentType () {
        return isFastInfoset () ? "application/fastinfoset" : "text/xml";
    }
    
    protected Envelope createEnvelopeFromSource () throws SOAPException {
        // Record the presence of xml declaration before the envelope gets
        // created.
        /* TODO: Fix this later
        String xmlDecl = lookForXmlDecl ();
        Source tmp = source;
        source = null;
        EnvelopeImpl envelope =
                (EnvelopeImpl) EnvelopeFactory.createEnvelope (tmp, this);
        
        if (!envelope.getNamespaceURI ().equals (SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
            log.severe ("SAAJ0304.ver1_1.msg.invalid.SOAP1.1");
            throw new SOAPException ("InputStream does not represent a valid SOAP 1.1 Message");
        }
        
        if (!omitXmlDecl) {
            envelope.setOmitXmlDecl ("no");
            envelope.setXmlDecl (xmlDecl);
        }
        return envelope;*/
        return null;
    }
    
    protected Envelope createEmptyEnvelope (String prefix)
    throws SOAPException {
         ExpressEnvelope1_1Impl env =new  ExpressEnvelope1_1Impl (document, prefix, true, true);
         env.setJXMessage(jxMessage);
         return env;
    }
    
    protected SOAPPartImpl duplicateType () {
        return new SOAPPart1_1Impl ();
    }
   
    
}
