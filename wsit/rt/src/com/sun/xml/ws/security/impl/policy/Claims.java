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
package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelGenerator;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.sourcemodel.XmlPolicyModelMarshaller;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class Claims extends PolicyAssertion implements com.sun.xml.ws.security.policy.Claims, SecurityAssertionValidator {

    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private boolean populated = false;
    private byte[] claimsBytes;

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

    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }

    private void populate() {
        populate(false);
    }

    private synchronized AssertionFitness populate(boolean b) {
        if (!populated) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XMLOutputFactory xof = XMLOutputFactory.newInstance();
                XMLStreamWriter writer = xof.createXMLStreamWriter(baos);

                AssertionSet set = AssertionSet.createAssertionSet(Arrays.asList(new PolicyAssertion[]{this}));
                Policy policy = Policy.createPolicy(Arrays.asList(new AssertionSet[]{set}));
                PolicySourceModel sourceModel = PolicyModelGenerator.getGenerator().translate(policy);
                XmlPolicyModelMarshaller pm = (XmlPolicyModelMarshaller) XmlPolicyModelMarshaller.getXmlMarshaller(true);
                pm.marshal(sourceModel, writer);
                claimsBytes = baos.toByteArray();
                populated = true;
            } catch (PolicyException ex) {
                Logger.getLogger(Claims.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XMLStreamException ex) {
                Logger.getLogger(Claims.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return fitness;
    }
}
