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

package com.sun.xml.ws.tx.at.policy.spi_impl;

import com.sun.istack.Nullable;
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
import com.sun.xml.ws.api.tx.at.Transactional;
import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.WSATImplInjection;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages;
import com.sun.xml.ws.tx.at.policy.AtPolicyCreator;
import com.sun.xml.ws.tx.at.policy.EjbTransactionType;
import java.lang.reflect.Method;

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

//    private static final Logger LOGGER = Logger.getLogger(AtPolicyMapConfigurator.class);
    private static final Class LOGGERCLASS = AtPolicyMapConfigurator.class;

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

        Class<?> seiClass = getDeclaringClass(model);
        if (seiClass == null) {
            return subjects;
        }

        /**
         * For now we are not going to consider EJB TX annotations, because we don't have access
         * to the EJB deployment descriptor, which could lead to inconsistent WS-AT configuration
         * behavior between EJB + annotations and EJB + DD use cases
         */
        // final EjbTransactionType defaultEjbTxnAttr = EjbTransactionType.getDefaultFor(seiClass);
        final EjbTransactionType defaultEjbTxnAttr = EjbTransactionType.NOT_DEFINED;
        final Transactional defaultFeature = seiClass.getAnnotation(Transactional.class);
        for (JavaMethod method : model.getJavaMethods()) {
            final Transactional effectiveFeature = getEffectiveFeature(method.getSEIMethod(), defaultFeature);
            if (effectiveFeature == null || effectiveFeature.enabled() == false) {
                continue;
            }

            final EjbTransactionType effectiveEjbTxType = defaultEjbTxnAttr.getEffectiveType(method.getSEIMethod());

            final String policyId = model.getBoundPortTypeName().getLocalPart() + "_" + method.getOperationName() + "_WSAT_Policy";
            final Policy policy = AtPolicyCreator.createPolicy(policyId, effectiveFeature.version().namespaceVersion, effectiveFeature.value(), effectiveEjbTxType);
            if (policy != null) {
                // attach ws-at policy assertion to binding/operation
                final WsdlBindingSubject wsdlSubject = WsdlBindingSubject.createBindingOperationSubject(model.getBoundPortTypeName(),
                        new QName(model.getTargetNamespace(), method.getOperationName()));
                final PolicySubject generatedWsatPolicySubject = new PolicySubject(wsdlSubject, policy);
                if (WSATHelper.isDebugEnabled()) {
                WSATImplInjection.getInstance().getLogging().log(
                    null, LOGGERCLASS, Level.FINE, "WSAT1002_ADD_AT_POLICY_ASSERTION",
                    new Object[]{model.getPortName().toString(),
                            method.getOperationName(),
                            seiClass.getName(),
                            method.getSEIMethod().getName(),
                            effectiveFeature.value().toString(),
                            effectiveEjbTxType.toString(),
                            policy.toString()}, null);
//                    LOGGER.fine(LocalizationMessages.WSAT_1002_ADD_AT_POLICY_ASSERTION(
//                            model.getPortName().toString(),
//                            method.getOperationName(),
//                            seiClass.getName(),
//                            method.getSEIMethod().getName(),
//                            effectiveFeature.value().toString(),
//                            effectiveEjbTxType.toString(),
//                            policy.toString()));
                }
                subjects.add(generatedWsatPolicySubject);
            }
        }

        return subjects;
    }

    private Class<?> getDeclaringClass(@Nullable SEIModel model) {
        if (model == null || model.getJavaMethods().isEmpty()) {
            return null;
        }

        return model.getJavaMethods().iterator().next().getSEIMethod().getDeclaringClass();
    }

    private Transactional getEffectiveFeature(Method method, Transactional defaultFeature) {
        Transactional feature = method.getAnnotation(Transactional.class);
        if (feature != null) {
            // TODO check compatibility with (existing) default?

            return feature;
        }

        return defaultFeature;
    }
}
