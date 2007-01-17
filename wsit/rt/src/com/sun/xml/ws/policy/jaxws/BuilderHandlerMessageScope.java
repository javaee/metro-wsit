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

package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import java.util.Collection;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
final class BuilderHandlerMessageScope extends BuilderHandler{
    public static final PolicyLogger LOGGER = PolicyLogger.getLogger(BuilderHandlerMessageScope.class);
    
    QName service;
    QName port;
    QName operation;
    QName message;
    
    private Scope scope;
    
    enum Scope{
        InputMessageScope,
        OutputMessageScope,
        FaultMessageScope,
    };
    
    
    /** Creates a new instance of WSDLServiceScopeBuilderHandler */
    BuilderHandlerMessageScope(
            Collection<String> policyURIs
            , Map<String,PolicySourceModel> policyStore
            , Object policySubject
            , Scope scope
            , QName service, QName port, QName operation, QName message) {
        
        super(policyURIs, policyStore, policySubject);
        this.service = service;
        this.port = port;
        this.operation = operation;
        this.scope = scope;
        this.message = message;
    }
    
    protected void doPopulate(final PolicyMapExtender policyMapExtender) throws PolicyException{
        PolicyMapKey mapKey;
        
        if (Scope.FaultMessageScope == scope) {
            mapKey = PolicyMap.createWsdlFaultMessageScopeKey(service, port, operation, message);
        } else { // in|out msg scope
            mapKey = PolicyMap.createWsdlMessageScopeKey(service, port, operation);
        }
        
        if (Scope.InputMessageScope == scope) {
            for (PolicySubject subject:getPolicySubjects()) {
                policyMapExtender.putInputMessageSubject(mapKey, subject);
            }
        } else if (Scope.OutputMessageScope == scope) {
            for (PolicySubject subject:getPolicySubjects()) {
                policyMapExtender.putOutputMessageSubject(mapKey, subject);
            }
        } else if (Scope.FaultMessageScope == scope) {
            for (PolicySubject subject : getPolicySubjects()) {
                policyMapExtender.putFaultMessageSubject(mapKey, subject);
            }
        }
    }
}
