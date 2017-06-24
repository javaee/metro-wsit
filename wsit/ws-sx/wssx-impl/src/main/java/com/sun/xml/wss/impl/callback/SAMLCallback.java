/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.wss.impl.callback;

import com.sun.xml.wss.saml.AuthorityBinding;
import javax.security.auth.callback.Callback;
import org.w3c.dom.Element;
import javax.xml.stream.XMLStreamReader;

public class SAMLCallback extends XWSSCallback implements Callback {

    Element assertion;
    Element authorityBinding;
    //Assertion jaxbAssertion;
    AuthorityBinding authorityInfo;
    XMLStreamReader assertionStream;
    String confirmation = null;
    String version = null;
    String assertionId = null;
    public static final String SV_ASSERTION_TYPE = "SV-Assertion";
    public static final String HOK_ASSERTION_TYPE = "HOK-Assertion";
    public static final String V10_ASSERTION = "SAML10Assertion";
    public static final String V11_ASSERTION = "SAML11Assertion";
    public static final String V20_ASSERTION = "SAML20Assertion";

    /** Creates a new instance of SAMLCallback */
    public SAMLCallback() {
    }

    public void setAssertionElement(Element samlAssertion) {
        assertion = samlAssertion;
    }

    public void setAssertionReader(XMLStreamReader samlAssertion) {
        this.assertionStream = samlAssertion;
    }

    public Element getAssertionElement() {
        return assertion;
    }

    public XMLStreamReader getAssertionReader() {
        return this.assertionStream;
    }

    public void setAuthorityBindingElement(Element authority) {
        authorityBinding = authority;
    }

    public Element getAuthorityBindingElement() {
        return authorityBinding;
    }

    public AuthorityBinding getAuthorityBinding() {
        return authorityInfo;
    }

    public void setAuthorityBinding(AuthorityBinding auth) {
        authorityInfo = auth;
    }

    public void setConfirmationMethod(String meth) {
        confirmation = meth;
    }

    public String getConfirmationMethod() {
        return confirmation;
    }

    public String getSAMLVersion() {
        return version;
    }

    public void setSAMLVersion(String ver) {
        version = ver;
    }

    public void setAssertionId(String id) {
        assertionId = id;
    }

    public String getAssertionId() {
        return assertionId;
    }
}
