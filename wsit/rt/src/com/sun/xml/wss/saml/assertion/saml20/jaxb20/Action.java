/*
 * $Id: Action.java,v 1.1.2.2 2010-07-14 14:08:54 m_potociar Exp $
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

package com.sun.xml.wss.saml.assertion.saml20.jaxb20;

// makeing the implementation dummy for Appserver Release

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.ActionType;
import org.w3c.dom.Element;
import java.util.logging.Logger;


/**
 *This class is designed for <code>Action</code> element in SAML core assertion.
 *The Action Element specifies an action on specified resource for which
 *permission is sought.
 */
public class Action  extends com.sun.xml.wss.saml.internal.saml20.jaxb20.ActionType implements com.sun.xml.wss.saml.Action {
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