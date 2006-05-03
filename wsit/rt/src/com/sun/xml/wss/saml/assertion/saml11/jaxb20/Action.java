/*
 * $Id: Action.java,v 1.1 2006-05-03 22:58:12 arungupta Exp $
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

// makeing the implementation dummy for Appserver Release

//import com.sun.xml.wss.saml.Action;
import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *This class is designed for <code>Action</code> element in SAML core assertion.
 *The Action Element specifies an action on specified resource for which
 *permission is sought.
 */
public class Action  extends com.sun.xml.wss.saml.internal.saml11.jaxb20.ActionType implements com.sun.xml.wss.saml.Action {
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);


    /**
     * Constructs an action element from an existing XML block.
     *
     * @param element representing a DOM tree element.
     * @exception SAMLException if there is an error in the sender or in
     *            the element definition.
     */
    public Action(Element element) {
        setValue(element.getLocalName());
        setNamespace(element.getNamespaceURI());
    }

    /**
     * Convenience constructor of <code>Action</code>
     * @param namespace The attribute "namespace" of
     *        <code>Action</code> element
     * @param action A String representing an action
     * @exception SAMLException if there is an error in the sender or in
     *            the element definition.
     */
    public Action(String namespace, String action) {
        setValue(action);
        setNamespace(namespace);
    }
}
