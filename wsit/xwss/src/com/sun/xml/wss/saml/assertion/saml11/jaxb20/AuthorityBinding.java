/*
 * $Id: AuthorityBinding.java,v 1.8 2008/07/03 05:29:12 ofung Exp $
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AuthorityBindingType;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;

/**
 * The <code>AuthorityBinding</code> element may be used to indicate
 * to a replying party receiving an <code>AuthenticationStatement</code> that
 * a SAML authority may be available to provide additional information about
 * the subject of the statement. A single SAML authority may advertise its
 * presence over multiple protocol binding, at multiple locations, and as
 * more than one kind of authority by sending multiple elements as needed.
 */
public class AuthorityBinding extends AuthorityBindingType
    implements com.sun.xml.wss.saml.AuthorityBinding {
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

 
    /**
     * Constructs an <code>AuthorityBinding</code> element from an existing XML
     * block.
     *
     * @param element representing a DOM tree element.
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public static AuthorityBindingType fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc = SAMLJAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AuthorityBindingType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    /**
     *Constructor
     *@param authKind A integer representing the type of SAML protocol queries
     *       to which the authority described by this element will
     *       respond. If you do NO specify this attribute, pass in
     *       value "-1".
     *@param location A URI describing how to locate and communicate with the
     *       authority, the exact syntax of which depends on the
     *       protocol binding in use.
     *@param binding A String representing a URI reference identifying the SAML
     *       protocol binding to use in  communicating with the authority.
     *@exception SAMLException if there is an error in the sender or in the
     *           element definition.
     */
    public AuthorityBinding(QName authKind, String location, String binding)
        {
        setAuthorityKind(authKind);
        setLocation(location);
        setBinding(binding);
    }
    
    public AuthorityBinding(AuthorityBindingType authBinType){
        setAuthorityKind(authBinType.getAuthorityKind());
        setLocation(authBinType.getLocation());
        setBinding(authBinType.getBinding());
    }
    
    @Override
    public QName getAuthorityKind(){
        return super.getAuthorityKind();
    }

    @Override
    public String getBinding(){
        return super.getBinding();
    }

    @Override
    public String getLocation(){
        return super.getLocation();
    }
}
