/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class Claims extends PolicyAssertion implements com.sun.xml.ws.security.policy.Claims, SecurityAssertionValidator {

    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private boolean populated = false;
    private byte[] claimsBytes;
    private Element claimsElement = null;

    /**
     * Creates a new instance of Issuer
     */
    public Claims() {
    }

    public Claims(AssertionData name, Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name, nestedAssertions, nestedAlternative);
    }

    public byte[] getClaimsAsBytes() {
        populate();
        return claimsBytes;
    }
    
    public Element getClaimsAsElement(){
        populate();
        if(claimsElement == null){
            try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(claimsBytes));
            claimsElement = (Element) doc.getElementsByTagNameNS("*", "Claims").item(0);
            } catch(Exception e){
                throw new WebServiceException(e);
            }
        }
        return claimsElement;
    }

    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }

    private void populate() {
        populate(false);
    }

    private synchronized AssertionFitness populate(boolean b) {
        if (!populated) {
            claimsBytes = PolicyUtil.policyAssertionToBytes(this);
            populated = true;  
        }
        return fitness;
    }
}
