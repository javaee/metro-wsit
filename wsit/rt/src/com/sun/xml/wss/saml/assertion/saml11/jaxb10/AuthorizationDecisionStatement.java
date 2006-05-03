/*
 * $Id: AuthorizationDecisionStatement.java,v 1.1 2006-05-03 22:58:10 arungupta Exp $
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;

import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.bind.util.ListImpl;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorizationDecisionStatementTypeImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *The <code>AuthorizationDecisionStatement</code> element supplies a statement
 *by the issuer that the request for access by the specified subject to the
 *specified resource has resulted in the specified decision on the basis of
 * some optionally specified evidence.
 */
public class AuthorizationDecisionStatement extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorizationDecisionStatementImpl 
    implements com.sun.xml.wss.saml.AuthorizationDecisionStatement {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    /**
     *Default constructor
     */
    protected AuthorizationDecisionStatement() {
        super();
    }

    /**
     * Constructs an <code>AuthorizationStatement</code> element from an
     * existing XML block.
     *
     * @param element representing a DOM tree element
     * @exception SAMLException if there is an error in the sender or in
     *            the element definition.
     */
    public static AuthorizationDecisionStatementTypeImpl fromElement(Element element)
        throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb10");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AuthorizationDecisionStatementTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    private void setAction(List action) {
        _Action = new ListImpl(action);
    }
    
    /**
     * Constructs an instance of <code>AuthorizationDecisionStatement</code>.
     *
     * @param subject (required) A Subject object
     * @param resource (required) A String identifying the resource to which
     *        access authorization is sought.
     * @param decision (required) The decision rendered by the issuer with
     *        respect to the specified resource. The value is of the
     *        <code>DecisionType</code> simple type.
     * @param action (required) A List of Action objects specifying the set of
     *        actions authorized to be performed on the specified resource.
     * @param evidence (optional) An Evidence object representing a set of
     *        assertions that the issuer replied on in making decisions.
     * @exception SAMLException if there is an error in the sender.
     */
    public AuthorizationDecisionStatement(
        Subject subject, String resource, String decision, List action,
        Evidence evidence) {
        setSubject(subject);
        setResource(resource);
        setDecision(decision);
        setAction(action);
        setEvidence(evidence);
    }
}
