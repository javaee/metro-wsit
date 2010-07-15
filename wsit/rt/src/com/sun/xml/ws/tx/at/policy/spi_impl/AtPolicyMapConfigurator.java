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
package com.sun.xml.ws.tx.at.policy.spi_impl;

import com.sun.xml.ws.tx.at.policy.EjbTransactionAnnotationProcessor;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.jaxws.spi.PolicyMapConfigurator;
import com.sun.xml.ws.policy.subject.WsdlBindingSubject;
import com.sun.xml.ws.tx.at.api.WsatNamespace;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages;
import com.sun.xml.ws.tx.at.policy.AtPolicyCreator;
import com.sun.xml.ws.tx.at.policy.EjbTransactionAttributeType;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 * From CMT EJB methods generate wsdl:binding/wsdl:operations with semantically equivalent WS-AT Policy Assertion(s).
 * <p/>
 * Known limitation: not accounting for EJB deployment descriptor, only working off of TransactionAttribute annotations.
 */
public class AtPolicyMapConfigurator implements PolicyMapConfigurator {

    private static final Logger LOGGER = Logger.getLogger(AtPolicyMapConfigurator.class);
    private static boolean NON_JAVAEE_CONTAINER = false;

    /**
     * Update policy map with operation scope of correct WS-AT policy assertions.
     * <p/>
     * Only looking for this for Java to WSDL at tool time.
     *
     * @param policyMapMutator
     * @param policyMap
     * @param model
     * @param wsBinding
     */
    public Collection<PolicySubject> update(final PolicyMap policyMap, final SEIModel model, final WSBinding wsBinding) throws PolicyException {
        final Collection<PolicySubject> subjects = new LinkedList<PolicySubject>();

//        if (NON_JAVAEE_CONTAINER || model == null) {
//            return subjects;
//        }
//
//        // For each method of a CMT EJB, map its effective javax.ejb.TransactionAttribute to semantically equivalent
//        // WS-AT policy assertion.
//
//        final Collection<? extends JavaMethod> methods = model.getJavaMethods();
//        Class<?> cmtEjbClass = null;
//        EjbTransactionAttributeType classDefaultTxnAttr = null;
//        for (JavaMethod method : methods) {
//
//            if (cmtEjbClass == null) {
//                boolean isCMTEJB = false;
//                final Class theClass = method.getSEIMethod().getDeclaringClass();
//                try {
//                    isCMTEJB = EjbTransactionAnnotationProcessor.isContainerManagedEJB(theClass);
//                } catch (NoClassDefFoundError e) {
//                    // running in a container that does not support EJBs; terminate processing of EJB annotations
//                    NON_JAVAEE_CONTAINER = true;
//                    LOGGER.fine(LocalizationMessages.WSAT_1101_NON_EE_CONTAINER("NoClassDefFoundError: " + e.getLocalizedMessage()));
//                    return subjects;
//                }
//                if (isCMTEJB) {
//                    // perform class level caching of info
//                    cmtEjbClass = theClass;
//                    classDefaultTxnAttr = EjbTransactionAnnotationProcessor.getTransactionAttributeDefault(theClass);
//                } else {
//                    // not a CMT EJB, no transaction attributes to look for; just return
//                    return subjects;
//                }
//            }
//
//            // we have a CMT EJB. Map its transaction attribute to proper ws-at policy assertion.
//
//            final EjbTransactionAttributeType txnAttr = EjbTransactionAnnotationProcessor.getEffectiveTransactionAttribute(method.getSEIMethod(), classDefaultTxnAttr);
//            final String policyId = model.getBoundPortTypeName().getLocalPart() + "_" + method.getOperationName() + "_WSAT_Policy";
//            final Policy policy = AtPolicyCreator.createPolicy(policyId, WsatNamespace.WSAT200410, null, txnAttr);
//            if (policy != null) {
//                // attach ws-at policy assertion to binding/operation
//                final WsdlBindingSubject wsdlSubject = WsdlBindingSubject.createBindingOperationSubject(model.getBoundPortTypeName(),
//                        new QName(model.getTargetNamespace(), method.getOperationName()));
//                final PolicySubject generatedWsatPolicySubject = new PolicySubject(wsdlSubject, policy);
//                if (LOGGER.isLoggable(Level.FINE)) {
//                    LOGGER.fine(LocalizationMessages.WSAT_1102_ADD_AT_POLICY_ASSERTION(
//                            model.getPortName().toString(),
//                            method.getOperationName(),
//                            policy.toString(),
//                            txnAttr.toString(),
//                            cmtEjbClass.getName(),
//                            method.getMethod().getName()));
//                } else {
//                    LOGGER.info(LocalizationMessages.WSAT_1102_ADD_AT_POLICY_ASSERTION(
//                            model.getPortName().toString(),
//                            method.getOperationName(),
//                            policy.getId(),
//                            txnAttr.toString(),
//                            cmtEjbClass.getName(),
//                            method.getMethod().getName()));
//                }
//                subjects.add(generatedWsatPolicySubject);
//            }
//        } // for each method in CMT EJB
        
        return subjects;
    }
}
