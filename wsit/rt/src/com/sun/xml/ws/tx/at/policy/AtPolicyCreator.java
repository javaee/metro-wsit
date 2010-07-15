/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.tx.at.policy;

import java.util.Arrays;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.tx.at.api.Transactional;
import com.sun.xml.ws.tx.at.api.Transactional.TransactionFlowType;
import com.sun.xml.ws.tx.at.api.WsatNamespace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.sun.xml.ws.tx.at.api.Transactional.TransactionFlowType.*;
import static com.sun.xml.ws.tx.at.api.WsatNamespace.*;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class AtPolicyCreator {
    private static final Map<WsatNamespace, Map<TransactionFlowType, Map<EjbTransactionAttributeType, Collection<WsatAssertionBase>>>> SUPPORTED_COMBINATIONS;

    private static void registerCombination(WsatNamespace version, TransactionFlowType flowType, EjbTransactionAttributeType ejbTat, WsatAssertionBase... assertions) {
        if (assertions == null) {
            assertions = new WsatAssertionBase[0];
        }
        SUPPORTED_COMBINATIONS.get(version).get(flowType).put(ejbTat, Arrays.asList(assertions));
    }

    static {
        SUPPORTED_COMBINATIONS = new EnumMap<WsatNamespace, Map<TransactionFlowType, Map<EjbTransactionAttributeType, Collection<WsatAssertionBase>>>>(WsatNamespace.class);
        for (WsatNamespace ns : WsatNamespace.values()) {
            Map<TransactionFlowType, Map<EjbTransactionAttributeType, Collection<WsatAssertionBase>>> nsMap = new EnumMap<TransactionFlowType, Map<EjbTransactionAttributeType, Collection<WsatAssertionBase>>>(TransactionFlowType.class);
            for (TransactionFlowType flowType : TransactionFlowType.values()) {
                nsMap.put(flowType, new EnumMap<EjbTransactionAttributeType, Collection<WsatAssertionBase>>(EjbTransactionAttributeType.class));
            }
            SUPPORTED_COMBINATIONS.put(ns, nsMap);
        }

        registerCombination(WSAT200410, MANDATORY, EjbTransactionAttributeType.MANDATORY, new AtAssertion(WSAT200410, false));
        registerCombination(WSAT200410, MANDATORY, EjbTransactionAttributeType.REQUIRED, new AtAssertion(WSAT200410, false));
        registerCombination(WSAT200410, SUPPORTS, EjbTransactionAttributeType.REQUIRED, new AtAssertion(WSAT200410, true), new AtAlwaysCapability(false));
        registerCombination(WSAT200410, SUPPORTS, EjbTransactionAttributeType.SUPPORTS, new AtAssertion(WSAT200410, true));
        registerCombination(WSAT200410, NEVER, EjbTransactionAttributeType.REQUIRES_NEW, new AtAlwaysCapability(false));
        registerCombination(WSAT200410, NEVER, EjbTransactionAttributeType.REQUIRED, new AtAlwaysCapability(false));
        registerCombination(WSAT200410, NEVER, EjbTransactionAttributeType.NEVER); // no assertions

        // TODO WSAT200606
    }

    public static Policy createPolicy(String policyId, WsatNamespace version, Transactional.TransactionFlowType wsatFlowType, EjbTransactionAttributeType ejbTat) {
        if (wsatFlowType == null || ejbTat == null) {
            return null;
        }

        final Collection<WsatAssertionBase> assertions = AtPolicyCreator.SUPPORTED_COMBINATIONS.get(version).get(wsatFlowType).get(ejbTat);
        if (assertions == null) {
            throw new IllegalArgumentException(String.format("Unsupported combinantion: WS-AT namespace: [ %s ], WS-AT flow type: [ %s ], EJB transaction attribute: [ %s ]", version, wsatFlowType, ejbTat));
        }
        if (assertions.isEmpty()) {
            return null;
        }

        final List<AssertionSet> assertionSets = new ArrayList<AssertionSet>(1);
        assertionSets.add(AssertionSet.createAssertionSet(assertions));

        return Policy.createPolicy("", policyId, assertionSets);
    }
}
