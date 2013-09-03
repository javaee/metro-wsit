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
package com.sun.xml.messaging.saaj.soap.ver1_2;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;

import com.sun.xml.messaging.saaj.soap.*;
import com.sun.xml.messaging.saaj.soap.impl.EnvelopeImpl;

public class ExpressSOAPPart1_2Impl extends SOAPPart1_2Impl implements SOAPConstants{
    
    protected static Logger log =
            Logger.getLogger (ExpressSOAPPart1_2Impl.class.getName (),
            "com.sun.xml.messaging.saaj.soap.ver1_2.LocalStrings");
    
    public ExpressSOAPPart1_2Impl () {
        super ();
    }
    
    protected String getContentType () {
        return "application/soap+xml";
    }
    
    protected Envelope createEmptyEnvelope (String prefix) throws SOAPException {
        return new ExpressEnvelope1_2Impl (getDocument (), prefix, true, true);
    }
    
    protected Envelope createEnvelopeFromSource () throws SOAPException {
        /* TODO: Fix this later
        String xmlDecl = lookForXmlDecl ();
        Source tmp = source;
        source = null;
        EnvelopeImpl envelope = (EnvelopeImpl)EnvelopeFactory.createEnvelope (tmp, this);
        if (!envelope.getNamespaceURI ().equals (SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
            log.severe ("SAAJ0415.ver1_2.msg.invalid.soap1.2");
            throw new SOAPException ("InputStream does not represent a valid SOAP 1.2 Message");
        }
        
        if (!omitXmlDecl) {
            envelope.setOmitXmlDecl ("no");
            envelope.setXmlDecl (xmlDecl);
        }
        return envelope;*/
        return null;
    }
    
    protected SOAPPartImpl duplicateType () {
        return new ExpressSOAPPart1_2Impl ();
    }
}
