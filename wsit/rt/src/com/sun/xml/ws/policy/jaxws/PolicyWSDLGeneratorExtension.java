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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.xml.namespace.QName;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLInput;
import com.sun.xml.ws.api.model.wsdl.WSDLMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.model.CheckedExceptionImpl;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMerger;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelGenerator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;

/**
 * Marshals the contents of a policy map to WSDL.
 */
public class PolicyWSDLGeneratorExtension extends WSDLGeneratorExtension {
    
    private final static PolicyLogger logger = PolicyLogger.getLogger(PolicyWSDLGeneratorExtension.class);

    private PolicyMap policyMap;
    private SEIModel seiModel;
    private WSDLModel configModel;
    private HashMap<String, Policy> nameToPolicy = new HashMap<String, Policy>();
    private HashMap<String, Policy> portTypeOperationToPolicy = new HashMap<String, Policy>();
    private HashMap<String, Policy> bindingOperationToPolicy = new HashMap<String, Policy>();
    private HashMap<String, Policy> messageToPolicy = new HashMap<String, Policy>();
    private HashMap<String, Policy> portTypeMessageToPolicy = new HashMap<String, Policy>();
    private HashMap<String, Policy> bindingMessageToPolicy = new HashMap<String, Policy>();
    // TODO Determine if service or port were renamed so that we can just map them like the other elements
    private Policy servicePolicy = null;
    private Policy portPolicy = null;
    // TODO Work-around to determine if MTOM was set by DD
    private Boolean isMtomEnabled = null;
    private static final String mtomNamespace = "http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization";
    private static final QName mtomName = new QName(mtomNamespace, "OptimizedMimeSerialization");
    
    public void start(TypedXmlWriter root, SEIModel model, WSBinding binding, Container container) {
        logger.entering("start");
        try {
            if (model != null) {
                this.seiModel = model;
                this.isMtomEnabled = binding.isMTOMEnabled();
                // QName serviceName = model.getServiceQName();
                this.configModel = PolicyConfigParser.parse(container);
                if (this.configModel != null) {
                    WSDLPolicyMapWrapper mapWrapper = this.configModel.getExtension(WSDLPolicyMapWrapper.class);
                    if (mapWrapper != null) {
                        this.policyMap = mapWrapper.getPolicyMap();
                        root._namespace(PolicyConstants.POLICY_NAMESPACE_URI, PolicyConstants.POLICY_NAMESPACE_PREFIX);
                    }
                }
            }
        } catch (PolicyException e) {
            logger.severe("start", "Failed to read wsit.xml", e);
        } finally {
            logger.exiting("start");
        }
    }

    public void addDefinitionsExtension(TypedXmlWriter definitions) {
        try {
            logger.entering("addDefinitionsExtension");
            if (policyMap != null) {
                Collection<PolicySubject> subjects = policyMap.getPolicySubjects();
                PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller();
                PolicyModelGenerator generator = PolicyModelGenerator.getGenerator();
                PolicyMerger merger = PolicyMerger.getMerger();
                boolean usingPolicy = false;
                for (PolicySubject subject : subjects) {
                    Object wsdlSubject = subject.getSubject();
                    if (wsdlSubject != null) {
                        if (!usingPolicy) {
                            definitions._element(PolicyConstants.USING_POLICY, TypedXmlWriter.class);
                            usingPolicy = true;
                        }
                        Policy policy = subject.getEffectivePolicy(merger);
                        if (wsdlSubject instanceof WSDLService) {
                            // TODO For now we always extract the service policy irrespective of the
                            // service name so that we do not have to deal with name changes by the app server
                            if (this.servicePolicy != null) {
                                logger.warning("addDefinitionsExtension", "WSIT configuration file seems to contain more than one service definition. Reading policy from one of them randomly.");
                            }
                            this.servicePolicy = policy;
//                            WSDLService service = (WSDLService) wsdlSubject;
//                            nameToPolicy.put(service.getName().getLocalPart(), policy);
                        }
                        else if (wsdlSubject instanceof WSDLPort) {
                            // TODO For now we always extract the port policy irrespective of the
                            // port name so that we do not have to deal with name changes by the app server
                            if (this.portPolicy != null) {
                                logger.warning("addDefinitionsExtension", "WSIT configuration file seems to contain more than one port definition. Reading policy from one of them randomly.");
                            }
                            this.portPolicy = policy;
//                            WSDLPort port = (WSDLPort) wsdlSubject;
//                            nameToPolicy.put(port.getName().getLocalPart(), policy);
                        }
                        else if (wsdlSubject instanceof WSDLPortType) {
                            WSDLPortType portType = (WSDLPortType) wsdlSubject;
                            nameToPolicy.put(portType.getName().getLocalPart(), policy);
                        }
                        else if (wsdlSubject instanceof WSDLBoundPortType) {
                            WSDLBoundPortType binding = (WSDLBoundPortType) wsdlSubject;
                            if (this.isMtomEnabled != null) {
                                policy = overrideMtom(policy);
                            }
                            nameToPolicy.put(binding.getName().getLocalPart(), policy);
                        }
                        else if (wsdlSubject instanceof WSDLOperation) {
                            WSDLOperation operation = (WSDLOperation) wsdlSubject;
                            portTypeOperationToPolicy.put(operation.getName().getLocalPart(), policy);
                        }
                        else if (wsdlSubject instanceof WSDLBoundOperation) {
                            WSDLBoundOperation operation = (WSDLBoundOperation) wsdlSubject;
                            bindingOperationToPolicy.put(operation.getName().getLocalPart(), policy);
                        }
                        else if (wsdlSubject instanceof WSDLMessage) {
                            WSDLMessage message = (WSDLMessage) wsdlSubject;
                            messageToPolicy.put(message.getName().getLocalPart(), policy);
                        }                            
                        else if (wsdlSubject instanceof WSDLInput) {
                            WSDLInput input = (WSDLInput) wsdlSubject;
                            portTypeMessageToPolicy.put(input.getName(), policy);
                        }
                        else if (wsdlSubject instanceof WSDLOutput) {
                            WSDLOutput output = (WSDLOutput) wsdlSubject;
                            portTypeMessageToPolicy.put(output.getName(), policy);
                        }
                        else if (wsdlSubject instanceof WSDLFault) {
                            WSDLFault fault = (WSDLFault) wsdlSubject;
                            portTypeMessageToPolicy.put(fault.getName(), policy);
                        }
                        PolicySourceModel policyInfoset = generator.translate(policy);
                        marshaller.marshal(policyInfoset, definitions);
                    }
                    else {
                        logger.fine("addDefinitionsExtension", "Subject was null, not marshalling attached policy: " + subject);
                    }
                }
            }
            else {
                logger.fine("addDefinitionsExtension", "Policy map was null, not marshalling any policies");
            }
        } catch (PolicyException e) {
            // TODO Throw WebServiceException
            logger.severe("addDefinitionsExtension", "Failed to marshal policies", e);
        } finally {
            logger.exiting("addDefinitionsExtension");
        }
    }
    
    public void addServiceExtension(TypedXmlWriter service) {
        logger.entering("addServiceExtension");
        if (this.seiModel != null) {
//            QName serviceName = this.seiModel.getServiceQName();
//            addPolicyReference(this.nameToPolicy, service, serviceName.getLocalPart());
            if (this.servicePolicy != null) {
                TypedXmlWriter policyReference = service._element(PolicyConstants.POLICY_REFERENCE, TypedXmlWriter.class);
                policyReference._attribute(PolicyConstants.POLICY_URI.getLocalPart(), '#' + this.servicePolicy.getIdOrName());
            }
        }
        logger.exiting("addServiceExtension");
    }
    
    public void addPortExtension(TypedXmlWriter port) {
        logger.entering("addPortExtension");
        if (this.seiModel != null) {
//            QName portName = this.seiModel.getPortName();
//            addPolicyReference(this.nameToPolicy, port, portName.getLocalPart());
            if (this.portPolicy != null) {
                TypedXmlWriter policyReference = port._element(PolicyConstants.POLICY_REFERENCE, TypedXmlWriter.class);
                policyReference._attribute(PolicyConstants.POLICY_URI.getLocalPart(), '#' + this.portPolicy.getIdOrName());
            }
        }
        logger.exiting("addPortExtension");
    }

    public void addPortTypeExtension(TypedXmlWriter portType) {
        logger.entering("addPortTypeExtension");
        if (this.seiModel != null) {
            QName portTypeName = this.seiModel.getPortTypeName();
            addPolicyReference(this.nameToPolicy, portType, portTypeName.getLocalPart());
        }
        logger.exiting("addPortTypeExtension");
    }
    
    public void addBindingExtension(TypedXmlWriter binding) {
        logger.entering("addBindingExtension");
        if (this.seiModel != null) {
            // TODO Do not rely on a naming algorithm that is private to WSDLGenerator
            String bindingName = this.seiModel.getPortName().getLocalPart() + "Binding";
            addPolicyReference(this.nameToPolicy, binding, bindingName);

            // Marshal an inline policy with MTOM setting if there wasn't one already.
            // TODO Replace with a non-hardcoded mechanism.
            Policy policy = nameToPolicy.get(bindingName);
            if (policy == null) {
                marshalMtomPolicy(binding);
            }
        }
        logger.exiting("addBindingExtension");
    }

    public void addOperationExtension(TypedXmlWriter operation, Method method) {
        logger.entering("addOperationExtension");
        if (this.seiModel != null) {
            // TODO Find a way not to down-cast
            JavaMethodImpl javaMethod = (JavaMethodImpl) this.seiModel.getJavaMethod(method);
            String operationName = javaMethod.getOperationName();
            addPolicyReference(this.portTypeOperationToPolicy, operation, operationName);
        }
        logger.exiting("addOperationExtension");
    }

    public void addBindingOperationExtension(TypedXmlWriter operation, Method method) {
        logger.entering("addBindingOperationExtension");
        if (this.seiModel != null) {
            // TODO Find a way not to down-cast
            JavaMethodImpl javaMethod = (JavaMethodImpl) this.seiModel.getJavaMethod(method);
            String operationName = javaMethod.getOperationName();
            addPolicyReference(this.nameToPolicy, operation, operationName);
        }
        logger.exiting("addBindingOperationExtension");
    }

    public void addInputMessageExtension(TypedXmlWriter message, Method method) {
        logger.entering("addInputMessageExtension");
        if (this.seiModel != null) {
            // TODO Find a way not to down-cast
            JavaMethodImpl javaMethod = (JavaMethodImpl) this.seiModel.getJavaMethod(method);
            String messageName = javaMethod.getOperationName();
            addPolicyReference(this.messageToPolicy, message, messageName);
        }
        logger.exiting("addInputMessageExtension");
    }

    public void addOutputMessageExtension(TypedXmlWriter message, Method method) {
        logger.entering("addOutputMessageExtension");
        if (this.seiModel != null) {
            // TODO Find a way not to down-cast
            JavaMethodImpl javaMethod = (JavaMethodImpl) this.seiModel.getJavaMethod(method);
            // TODO Do not rely on a naming algorithm that is private to WSDLGenerator
            String messageName = javaMethod.getOperationName() + "Response";
            addPolicyReference(this.messageToPolicy, message, messageName);
        }
        logger.exiting("addOutputMessageExtension");
    }

    public void addFaultMessageExtension(TypedXmlWriter message, Method method) {
        logger.entering("addFaultMessageExtension");
        // TODO Need access to fault message name
        logger.exiting("addFaultMessageExtension");
    }

    public void addOperationInputExtension(TypedXmlWriter input, Method method) {
        logger.entering("addOperationInputExtension");
        if (this.seiModel != null) {
            // TODO Find a way not to down-cast
            JavaMethodImpl javaMethod = (JavaMethodImpl) this.seiModel.getJavaMethod(method);
            String messageName = javaMethod.getOperationName();
            addPolicyReference(this.portTypeMessageToPolicy, input, messageName);
        }
        logger.exiting("addOperationInputExtension");
    }

    public void addOperationOutputExtension(TypedXmlWriter output, Method method) {
        logger.entering("addOperationOutputExtension");
        if (this.seiModel != null) {
            // TODO Find a way not to down-cast
            JavaMethodImpl javaMethod = (JavaMethodImpl) this.seiModel.getJavaMethod(method);
            // TODO Do not rely on a naming algorithm that is private to WSDLGenerator
            String messageName = javaMethod.getOperationName() + "Response";
            addPolicyReference(this.portTypeMessageToPolicy, output, messageName);
        }
        logger.exiting("addOperationOutputExtension");
    }

    public void addOperationFaultExtension(TypedXmlWriter fault, Method method, CheckedException ce) {
        logger.entering("addOperationFaultExtension");
        if (this.seiModel != null) {
            // TODO Find a way not to down-cast
            CheckedExceptionImpl exception = (CheckedExceptionImpl) ce;
            String messageName = exception.getMessageName();
            addPolicyReference(this.portTypeMessageToPolicy, fault, messageName);
        }
        logger.exiting("addOperationFaultExtension");
    }

    public void addBindingOperationInputExtension(TypedXmlWriter input, Method method) {
        logger.entering("addBindingOperationInputExtension");
        if (this.seiModel != null) {
            // TODO Find a way not to down-cast
            JavaMethodImpl javaMethod = (JavaMethodImpl) this.seiModel.getJavaMethod(method);
            String messageName = javaMethod.getOperationName();
            addPolicyReference(this.bindingMessageToPolicy, input, messageName);
        }
        logger.exiting("addBindingOperationInputExtension");
    }

    public void addBindingOperationOutputExtension(TypedXmlWriter output, Method method) {
        logger.entering("addBindingOperationOutputExtension");
        if (this.seiModel != null) {
            // TODO Find a way not to down-cast
            JavaMethodImpl javaMethod = (JavaMethodImpl) this.seiModel.getJavaMethod(method);
            // TODO Do not rely on a naming algorithm that is private to WSDLGenerator
            String messageName = javaMethod.getOperationName() + "Response";
            addPolicyReference(this.bindingMessageToPolicy, output, messageName);
        }
        logger.exiting("addBindingOperationOutputExtension");
    }

    public void addBindingOperationFaultExtension(TypedXmlWriter fault, Method method) {
        logger.entering("addBindingOperationFaultExtension");
        // TODO Not invoked by WSDLGenerator
        logger.exiting("addBindingOperationFaultExtension");
    }

    /**
     * Search for an element identified by its qualified name in nameToPolicy
     * and add a PolicyReference element that points to the policy of the
     * element.
     *
     * @param nameToPolicy A map from qualified names to referenced policies
     * @param element A TXW element to which we shall add the PolicyReference
     * @param name The fully qualified name of the above element
     */
    private void addPolicyReference(HashMap<String, Policy> nameToPolicy, TypedXmlWriter element, String name) {
        Policy policy = nameToPolicy.get(name);
        if (policy != null) {
            TypedXmlWriter policyReference = element._element(PolicyConstants.POLICY_REFERENCE, TypedXmlWriter.class);
            policyReference._attribute(PolicyConstants.POLICY_URI.getLocalPart(), '#' + policy.getIdOrName());
        }
    }

    /**
     * If isMtomEnabled is true, add an MTOM policy assertion to all policy alternatives.
     */
    private Policy overrideMtom(Policy policy) {
        // TODO Replace with code that does not hard-wire MTOM assertion
        return addMtomAssertion(policy);
//        if (this.isMtomEnabled.booleanValue()) {
//            return addMtomAssertion(policy);
//        }
//        else {
//            return removeMtomAssertion(policy);
//        }
    }
    
    private Policy addMtomAssertion(Policy policy) {
        ArrayList<AssertionSet> assertionSets = new ArrayList<AssertionSet>();
        for (AssertionSet assertionSet : policy) {
            if (assertionSet.contains(mtomName)) {
                assertionSets.add(assertionSet);
            }
            else {
                ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>();
                for (PolicyAssertion assertion : assertionSet) {
                    assertions.add(assertion);
                }
                assertions.add(new MtomAssertion());
                AssertionSet extendedSet = AssertionSet.createAssertionSet(assertions);
                assertionSets.add(extendedSet);
            }
        }
        return Policy.createPolicy(policy.getName(), policy.getId(), assertionSets);
    }
    
//    private Policy removeMtomAssertion(Policy policy) {
//        ArrayList<AssertionSet> assertionSets = new ArrayList<AssertionSet>();
//        for (AssertionSet assertionSet : policy) {
//            if (assertionSet.contains(mtomName)) {
//                ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>();
//                for (PolicyAssertion assertion : assertions) {
//                    if (assertion.getName().equals(mtomName)) {
//                        continue;
//                    }
//                    assertions.add(assertion);
//                }
//                AssertionSet reducedSet = AssertionSet.createAssertionSet(assertions);
//                assertionSets.add(reducedSet);
//            }
//            else {
//                assertionSets.add(assertionSet);
//            }
//        }
//        return Policy.createPolicy(policy.getId(), policy.getName(), assertionSets);
//    }

    /**
     * Creates an inline policy with an MTOM assertion if isMtomEnabled is set.
     *
     * TODO Replace with code that does not hard-wire MTOM assertion
     */
    private void marshalMtomPolicy(TypedXmlWriter binding) {
        if ((this.isMtomEnabled != null) && this.isMtomEnabled.booleanValue()) {
            try {
                ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>();
                assertions.add(new MtomAssertion());
                AssertionSet assertionSet = AssertionSet.createAssertionSet(assertions);
                ArrayList<AssertionSet> assertionSets = new ArrayList<AssertionSet>();
                assertionSets.add(assertionSet);
                Policy policy = Policy.createPolicy(assertionSets);
                binding._namespace("http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization", "wsoma");
                PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller();
                PolicyModelGenerator generator = PolicyModelGenerator.getGenerator();
                PolicySourceModel policyInfoset = generator.translate(policy);
                marshaller.marshal(policyInfoset, binding);
            } catch (PolicyException e) {
                logger.warning("addBindingExtension", "Failed to marshal MTOM policy onto WSDL", e);
            }
        }
    }
    
    static class MtomAssertion extends PolicyAssertion {

        private static final AssertionData mtomData = new AssertionData(mtomName);

        MtomAssertion() {
            super(mtomData, null, null);
        }
    }
}
