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
package com.sun.xml.wss;

import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.KeyBindingBase;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Provides Meta Data about the token Policy.
 * Can be used to obtain WS-SecurityPolicy related Meta-Data associated with the Token.
 * The meta-data is generally used to disambiguate the exact action to be performed inside 
 * a specific callback or validator. For example the Policy Meta-Data can be used to decide
 * what certificate/username to return.
 */
public class TokenPolicyMetaData {

    public static final String TOKEN_POLICY = "token.policy";
    private AuthenticationTokenPolicy tokenPolicy = null;

    /**
     *
     * @param runtimeProperties the runtime Properties of an XWSS CallbackHandler
     */
    public TokenPolicyMetaData(Map runtimeProperties) {
        this.tokenPolicy = (AuthenticationTokenPolicy) runtimeProperties.get(TOKEN_POLICY);
    }

    /**
     * @return &lt&sp:Issuer&gtwsa:EndpointReferenceType&lt/sp:Issuer&gt, null if not specified policy 
     */
    public String getIssuer() {
        if (tokenPolicy == null) {
            return null;
        }
        KeyBindingBase kb = (KeyBindingBase) tokenPolicy.getFeatureBinding();
        return kb.getIssuer();
    }

    /**
     * @return &lt&wst:Claims Dialect="..."&gt ... &lt/wst:Claims&gt, null if not specified in policy
     */
    public Element getClaims() throws XWSSecurityException{

        if (tokenPolicy == null) {
            return null;
        }
        KeyBindingBase kb = (KeyBindingBase) tokenPolicy.getFeatureBinding();
        Element claimsElement = null;
        byte[] claimBytes = kb.getClaims();
        if (claimBytes != null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new ByteArrayInputStream(claimBytes));
                claimsElement = (Element) doc.getElementsByTagNameNS("*", "Claims").item(0);
            } catch (SAXException ex) {
                Logger.getLogger(TokenPolicyMetaData.class.getName()).log(Level.SEVERE, null, ex);
                throw new XWSSecurityException(ex);
            } catch (IOException ex) {
                Logger.getLogger(TokenPolicyMetaData.class.getName()).log(Level.SEVERE, null, ex);
                throw new XWSSecurityException(ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(TokenPolicyMetaData.class.getName()).log(Level.SEVERE, null, ex);
                throw new XWSSecurityException(ex);
            }
        }
        return claimsElement;
    }
}
