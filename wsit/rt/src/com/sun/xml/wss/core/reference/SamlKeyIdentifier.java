/*
 * $Id: SamlKeyIdentifier.java,v 1.1 2006-05-03 22:57:35 arungupta Exp $
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

package com.sun.xml.wss.core.reference;

import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;

/**
 */
public class SamlKeyIdentifier extends KeyIdentifier {

    /** Defaults */
    private String valueType = 
        MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE;

    /**
     * Creates an "empty" KeyIdentifier element with default encoding type
     * and default value type.
     */
    public SamlKeyIdentifier(Document doc) throws XWSSecurityException {
        super(doc);
        // Set default attributes
        setAttribute("ValueType", valueType);
    }

    public SamlKeyIdentifier(SOAPElement element) throws XWSSecurityException {
        super(element);
    }

} 
