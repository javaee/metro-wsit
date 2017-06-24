/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * $Id: Action.java,v 1.2 2010-10-21 15:37:59 snajper Exp $
 */

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;

// makeing the implementation dummy for Appserver Release

//import com.sun.xml.wss.saml.Action;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.ActionType;
import org.w3c.dom.Element;
import java.util.logging.Logger;


/**
 *This class is designed for <code>Action</code> element in SAML core assertion.
 *The Action Element specifies an action on specified resource for which
 *permission is sought.
 */
public class Action  extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.ActionImpl implements com.sun.xml.wss.saml.Action {
    protected static final Logger log = Logger.getLogger(
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
    
     public Action(ActionType actionType) {
        setValue(actionType.getValue());
        setNamespace(actionType.getNamespace());
    }
     
     @Override
    public String getValue(){
        return super.getValue();
    }
    
    @Override
    public String getNamespace(){
        return super.getNamespace();
    }
}
