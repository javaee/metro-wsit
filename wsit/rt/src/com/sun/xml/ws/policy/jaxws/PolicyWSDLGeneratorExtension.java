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

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLInput;
import com.sun.xml.ws.api.model.wsdl.WSDLMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.api.wsdl.writer.WSDLGenExtnContext;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMerger;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.jaxws.spi.PolicyMapUpdateProvider;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelGenerator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * Marshals the contents of a policy map to WSDL.
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
public class PolicyWSDLGeneratorExtension extends WSDLGeneratorExtension {
    
    static enum ScopeType {
        SERVICE,
        ENDPOINT,
        OPERATION,
        INPUT_MESSAGE,
        OUTPUT_MESSAGE,
        FAULT_MESSAGE
    }
    
    private final static PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyWSDLGeneratorExtension.class);
    
    private PolicyMap policyMap;
    private SEIModel seiModel;
    private Class endpointClass;
    private final Collection<PolicySubject> subjects = new LinkedList<PolicySubject>();
    
    private PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller(true);
    private PolicyMerger merger = PolicyMerger.getMerger();
    
    public void start(final WSDLGenExtnContext context) {
        LOGGER.entering();
        try {
            this.seiModel = context.getModel();
            this.endpointClass = context.getEndpointClass();
            
            final PolicyMapUpdateProvider[] policyMapUpdateProviders = PolicyUtils.ServiceProvider.load(PolicyMapUpdateProvider.class);
            final PolicyMapExtender[] extenders = new PolicyMapExtender[policyMapUpdateProviders.length];
            for (int i=0; i < policyMapUpdateProviders.length; i++) {
                extenders[i] = PolicyMapExtender.createPolicyMapExtender();
            }
            final String configId = context.getEndpointClass().getName();
            try {
                policyMap = PolicyConfigParser.parse(configId, context.getContainer(), extenders);
            } catch (PolicyException e) {
                LOGGER.fine(LocalizationMessages.WSP_1027_FAILED_TO_READ_WSIT_CONFIG_FOR_ID(configId), e);
            }
            if (policyMap == null) {
                LOGGER.fine(LocalizationMessages.WSP_1034_CREATE_POLICY_MAP_FOR_CONFIG(configId));
                policyMap = PolicyMap.createPolicyMap(Arrays.asList(extenders));
            }
            
            final TypedXmlWriter root = context.getRoot();
            root._namespace(PolicyConstants.POLICY_NAMESPACE_URI, PolicyConstants.POLICY_NAMESPACE_PREFIX);
            root._namespace(PolicyConstants.WSU_NAMESPACE_URI, PolicyConstants.WSU_NAMESPACE_PREFIX);
            final WSBinding binding = context.getBinding();
            
            try {
                for (int i = 0; i < policyMapUpdateProviders.length; i++) {
                    policyMapUpdateProviders[i].update(extenders[i], policyMap, seiModel, binding);
                    extenders[i].disconnect();
                }
            } catch (PolicyException e) {
                throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1048_MAP_UPDATE_FAILED(), e));
            }
        } finally {
            LOGGER.exiting();
        }
    }
    
    public void addDefinitionsExtension(final TypedXmlWriter definitions) {
        try {
            LOGGER.entering();
            if (policyMap != null) {
                subjects.addAll(policyMap.getPolicySubjects());
                boolean usingPolicy = false;
                PolicyModelGenerator generator = PolicyModelGenerator.getGenerator();
                Set<String> policyIDsOrNamesWritten = null;
                for (PolicySubject subject : subjects) {
                    if (subject.getSubject() != null) {
                        if (!usingPolicy) {
                            definitions._element(PolicyConstants.USING_POLICY, TypedXmlWriter.class);
                            usingPolicy = true;
                            policyIDsOrNamesWritten = new HashSet<String>();
                        }
                        final Policy policy;
                        try {
                            policy = subject.getEffectivePolicy(merger);
                        } catch (PolicyException e) {
                            throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1029_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT(subject.toString()), e));
                        }
                        if ((null != policy.getIdOrName()) && (!policyIDsOrNamesWritten.contains(policy.getIdOrName()))) {
                            try {
                                final PolicySourceModel policyInfoset = generator.translate(policy);
                                marshaller.marshal(policyInfoset, definitions);
                            } catch (PolicyException e) {
                                throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1051_FAILED_TO_MARSHALL_POLICY(policy.getIdOrName()), e));
                            }
                            policyIDsOrNamesWritten.add(policy.getIdOrName());
                        } else {
                            LOGGER.fine(LocalizationMessages.WSP_1047_POLICY_ID_NULL_OR_DUPLICATE(policy));
                        }
                    } else {
                        LOGGER.fine(LocalizationMessages.WSP_1019_NOT_MARSHALLING_WSDL_SUBJ_NULL(subject));
                    }
                }
            } else {
                LOGGER.fine(LocalizationMessages.WSP_1020_NOT_MARSHALLING_ANY_POLICIES_POLICY_MAP_IS_NULL());
            }
        } finally {
            LOGGER.exiting();
        }
    }
    
    public void addServiceExtension(final TypedXmlWriter service) {
        LOGGER.entering();
        final String serviceName = ((null != seiModel) && (null != endpointClass)) ?
            WSEndpoint.getDefaultServiceName(endpointClass).getLocalPart() :
            null;
        selectAndProcessSubject(service, WSDLService.class, ScopeType.SERVICE, serviceName);
        LOGGER.exiting();
    }
    
    public void addPortExtension(final TypedXmlWriter port) {
        LOGGER.entering();
        final String portName = ((null != seiModel) && (null != endpointClass)) ?
            WSEndpoint.getDefaultPortName(seiModel.getServiceQName(), endpointClass).getLocalPart() :
            null;
        selectAndProcessSubject(port, WSDLPort.class, ScopeType.ENDPOINT, portName);
        LOGGER.exiting();
    }
    
    public void addPortTypeExtension(final TypedXmlWriter portType) {
        LOGGER.entering();
        final String portTypeName = (null != seiModel) ? seiModel.getPortTypeName().getLocalPart() : null;
        selectAndProcessSubject(portType, WSDLPortType.class, ScopeType.ENDPOINT, portTypeName);
        LOGGER.exiting();
    }
    
    public void addBindingExtension(final TypedXmlWriter binding) {
        LOGGER.entering();
        final QName bindingName = (null != seiModel) ? seiModel.getBoundPortTypeName() : null;
        selectAndProcessSubject(binding, WSDLBoundPortType.class, ScopeType.ENDPOINT, bindingName);
        LOGGER.exiting();
    }
    
    public void addOperationExtension(final TypedXmlWriter operation, final JavaMethod method) {
        LOGGER.entering();
        selectAndProcessSubject(operation, WSDLOperation.class, ScopeType.OPERATION, method);
        LOGGER.exiting();
    }
    
    public void addBindingOperationExtension(final TypedXmlWriter operation, final JavaMethod method) {
        LOGGER.entering();
        selectAndProcessSubject(operation, WSDLBoundOperation.class, ScopeType.OPERATION, method);
        LOGGER.exiting();
    }
    
    public void addInputMessageExtension(final TypedXmlWriter message, final JavaMethod method) {
        LOGGER.entering();
        final String messageName = (null != method) ? method.getRequestMessageName() : null;
        selectAndProcessSubject(message, WSDLMessage.class, ScopeType.INPUT_MESSAGE, messageName);
        LOGGER.exiting();
    }
    
    public void addOutputMessageExtension(final TypedXmlWriter message, final JavaMethod method) {
        LOGGER.entering();
        final String messageName = (null != method) ? method.getResponseMessageName() : null;
        selectAndProcessSubject(message, WSDLMessage.class, ScopeType.OUTPUT_MESSAGE, messageName);
        LOGGER.exiting();
    }
    
    public void addFaultMessageExtension(final TypedXmlWriter message, final JavaMethod method, final CheckedException ce) {
        LOGGER.entering();
        final String messageName = (null != ce) ? ce.getMessageName() : null;
        selectAndProcessSubject(message, WSDLMessage.class, ScopeType.FAULT_MESSAGE, messageName);
        LOGGER.exiting();
    }
    
    public void addOperationInputExtension(final TypedXmlWriter input, final JavaMethod method) {
        LOGGER.entering();
        final String messageName = (null != method) ? method.getRequestMessageName() : null;
        selectAndProcessSubject(input, WSDLInput.class, ScopeType.INPUT_MESSAGE, messageName);
        LOGGER.exiting();
    }
    
    public void addOperationOutputExtension(final TypedXmlWriter output, final JavaMethod method) {
        LOGGER.entering();
        final String messageName = (null != method) ? method.getResponseMessageName() : null;
        selectAndProcessSubject(output, WSDLOutput.class, ScopeType.OUTPUT_MESSAGE, messageName);
        LOGGER.exiting();
    }
    
    public void addOperationFaultExtension(final TypedXmlWriter fault, final JavaMethod method, final CheckedException ce) {
        LOGGER.entering();
        final String messageName = (null != ce) ? ce.getMessageName() : null;
        selectAndProcessSubject(fault, WSDLFault.class, ScopeType.FAULT_MESSAGE, messageName);
        LOGGER.exiting();
    }
    
    public void addBindingOperationInputExtension(final TypedXmlWriter input, final JavaMethod method) {
        LOGGER.entering();
        final String messageName = (null != method) ? method.getOperationName() : null;
        selectAndProcessSubject(input, WSDLBoundOperation.class, ScopeType.INPUT_MESSAGE, messageName);
        LOGGER.exiting();
    }
    
    public void addBindingOperationOutputExtension(final TypedXmlWriter output, final JavaMethod method) {
        LOGGER.entering();
        final String messageName = (null != method) ? method.getOperationName() : null;
        selectAndProcessSubject(output, WSDLBoundOperation.class, ScopeType.OUTPUT_MESSAGE, messageName);
        LOGGER.exiting();
    }
    
    public void addBindingOperationFaultExtension(final TypedXmlWriter fault, final JavaMethod method, final CheckedException ce) {
        LOGGER.entering();
        final String messageName = (null != ce) ? ce.getMessageName() : null;
        selectAndProcessSubject(fault, WSDLFault.class, ScopeType.FAULT_MESSAGE, messageName);
        LOGGER.exiting();
    }
    
    /**
     * This method should only be invoked by interface methods that deal with operations because they
     * may use JavaMethod as PolicySubject instead of a WSDL object.
     */
    private void selectAndProcessSubject(final TypedXmlWriter xmlWriter, final Class clazz, final ScopeType scopeType, final JavaMethod method) {
        LOGGER.entering(xmlWriter, clazz, scopeType, method);
        if (method != null) {
            if (subjects != null) {
                for (PolicySubject subject : subjects) {
                    if (method.equals(subject.getSubject())) {
                        writePolicyOrReferenceIt(subject, xmlWriter);
                    }
                }
            }
            selectAndProcessSubject(xmlWriter, clazz, scopeType, method.getOperationName());
        } else {
            selectAndProcessSubject(xmlWriter, clazz, scopeType, (String) null);
        }
        LOGGER.exiting();
    }
    
    /**
     * This method should only be invoked by interface methods that deal with WSDL binding because they
     * may use the QName of the WSDL binding element as PolicySubject instead of a WSDL object.
     */
    private void selectAndProcessSubject(final TypedXmlWriter xmlWriter, final Class clazz, final ScopeType scopeType, final QName bindingName) {
        LOGGER.entering(xmlWriter, clazz, scopeType, bindingName);
        if (bindingName != null) {
            if (subjects != null) {
                for (PolicySubject subject : subjects) {
                    if (bindingName.equals(subject.getSubject())) {
                        writePolicyOrReferenceIt(subject, xmlWriter);
                    }
                }
            }
            selectAndProcessSubject(xmlWriter, clazz, scopeType, bindingName.getLocalPart());
        } else {
            selectAndProcessSubject(xmlWriter, clazz, scopeType, (String) null);
        }
        LOGGER.exiting();
    }
    
    private void selectAndProcessSubject(final TypedXmlWriter xmlWriter, final Class clazz, final ScopeType scopeType, final String wsdlName) {
        LOGGER.entering();
        if (subjects != null) {
            for (PolicySubject subject : subjects) { // iterate over all subjects in policy map
                if (isCorrectType(policyMap, subject, scopeType)) {
                    final Object concreteSubject = subject.getSubject();
                    if (concreteSubject != null && clazz.isInstance(concreteSubject)) { // is it our class?
                        if (null == wsdlName) { // no name provided to check
                            writePolicyOrReferenceIt(subject, xmlWriter);
                        } else {
                            try {
                                final Method getNameMethod = clazz.getDeclaredMethod("getName");
                                if (stringEqualsToStringOrQName(wsdlName, getNameMethod.invoke(concreteSubject))) {
                                    writePolicyOrReferenceIt(subject, xmlWriter);
                                }
                            } catch (NoSuchMethodException e) {
                                throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1011_UNABLE_TO_CHECK_ELEMENT_NAME(clazz.getName(), wsdlName), e));
                            } catch (IllegalAccessException e) {
                                throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1011_UNABLE_TO_CHECK_ELEMENT_NAME(clazz.getName(), wsdlName), e));
                            } catch (InvocationTargetException e) {
                                throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1011_UNABLE_TO_CHECK_ELEMENT_NAME(clazz.getName(), wsdlName), e));
                            }
                        }
                    }
                }
            }
        }
        LOGGER.exiting();
    }
    
    private static final boolean isCorrectType(final PolicyMap map, final PolicySubject subject, final ScopeType type) {
        switch (type) {
            case OPERATION :
                return ! (map.isInputMessageSubject(subject) || map.isOutputMessageSubject(subject));
            case INPUT_MESSAGE :
                return map.isInputMessageSubject(subject);
            case OUTPUT_MESSAGE :
                return map.isOutputMessageSubject(subject);
            default:
                return true;
        }
    }
    
    private boolean stringEqualsToStringOrQName(final String first, final Object second) {
        return (second instanceof QName) ? first.equals(((QName)second).getLocalPart()) : first.equals(second) ;
    }
    
    /**
     * Adds a PolicyReference element that points to the policy of the element,
     * if the policy does not have any id or name. Writes policy inside the element otherwise.
     *
     * @param policy to be referenced or marshalled
     * @param element A TXW element to which we shall add the PolicyReference
     */
    private void writePolicyOrReferenceIt(final PolicySubject subject, final TypedXmlWriter writer) {
        final Policy policy;
        try {
            policy = subject.getEffectivePolicy(merger);
        } catch (PolicyException e) {
            throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1029_FAILED_TO_RETRIEVE_EFFECTIVE_POLICY_FOR_SUBJECT(subject.toString()), e));
        }
        if (policy != null) {
            if (null != policy.getIdOrName()) {
                final TypedXmlWriter policyReference = writer._element(PolicyConstants.POLICY_REFERENCE, TypedXmlWriter.class);
                policyReference._attribute(PolicyConstants.POLICY_URI.getLocalPart(), '#' + policy.getIdOrName());
            } else {
                final PolicyModelGenerator generator = PolicyModelGenerator.getGenerator();
                try {
                    final PolicySourceModel policyInfoset = generator.translate(policy);
                    marshaller.marshal(policyInfoset, writer);
                } catch (PolicyException pe) {
                    throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1010_UNABLE_TO_MARSHALL_POLICY_OR_POLICY_REFERENCE(), pe));
                }
            }
        }        
    }
}

